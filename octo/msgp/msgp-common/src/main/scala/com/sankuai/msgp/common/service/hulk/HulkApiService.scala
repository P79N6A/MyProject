package com.sankuai.msgp.common.service.hulk

import java.util.concurrent.{Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy
import com.sankuai.hulk.harbor.thrift.data.ErrorCode
import com.sankuai.hulk.harbor.thrift.service.HarborService
import com.sankuai.msgp.common.dao.appkey.AppkeyDescDao
import com.sankuai.msgp.common.service.org.OpsService.token
import com.sankuai.msgp.common.utils.ExecutionContextFactory
import com.sankuai.msgp.common.utils.client.TairClient
import com.sankuai.msgp.common.utils.helper.{CommonHelper, HttpHelper}
import dispatch.{url, _}
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.Json
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}
import scala.util.control.Breaks._

object HulkApiService {
  private val LOG: Logger = LoggerFactory.getLogger(HulkApiService.getClass)

  private val HULKURL = "http://kapiserver.hulk.vip.sankuai.com/api/app/instance?env={1}&appkey={2}"

  private val ENV = if (CommonHelper.isOffline) {
    List[String]("dev", "test", "ppe")
  } else {
    List[String]("prod", "staging")
  }

  private var count = 0

  case class HulkAppKeyCheck(errorCode: Int, errorInfo: String)

  implicit val appKeyCheckReads = Json.reads[HulkAppKeyCheck]
  implicit val appKeyCheckWrites = Json.writes[HulkAppKeyCheck]

  case class HulkSetEntity(setName: String, ip: String)

  implicit val hulkSetReads = Json.reads[HulkSetEntity]
  implicit val hulkSetWrites = Json.writes[HulkSetEntity]

  private val refreshThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))

  private val scheduler = Executors.newScheduledThreadPool(1)
  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)
  private implicit val timeout = Duration.create(30L, duration.SECONDS)
  private val client = {
    val proxy = new ThriftClientProxy
    proxy.setServiceInterface(classOf[HarborService])
    proxy.setAppKey("com.sankuai.inf.msgp")
    proxy.setRemoteAppkey("com.sankuai.inf.hulk.harbor")
    proxy.setTimeout(3000)
    proxy.afterPropertiesSet()
    proxy.getObject.asInstanceOf[HarborService.Iface]
  }

  val ip2hostCache = CacheBuilder.newBuilder().maximumSize(2000L).expireAfterWrite(20L, TimeUnit.SECONDS)
    .build(new CacheLoader[String, String]() {
      def load(ip: String) = {
        TairClient.get(ipkey(ip)).getOrElse(ip)
      }
    })

  def refresh() = {
    val now = System.currentTimeMillis()
    LOG.info(s"begin refresh hulk api now time is $now")
    val appkeys = AppkeyDescDao.getAllAppkey
    val appkeysPar = appkeys.par
    appkeysPar.tasksupport = refreshThreadPool
    appkeysPar.foreach {
      appkey =>
        breakable {
          var realUrl = HULKURL.replace("{2}", appkey)
          ENV.foreach {
            env =>
              try {
                val finalUrl = realUrl.replace("{1}", env)
                val hulkdata = HttpHelper.execute(url(finalUrl))
                val code = Json.parse(hulkdata.get.toString).\("code").as[Int]
                if (code != 0) {
                  break()
                } else {
                  val hulkList = Json.parse(hulkdata.get.toString).\("data").asOpt[List[HulkSetEntity]]
                  if (hulkList.isDefined) {
                    val list = hulkList.get
                    if (list.size > 0) {
                      refreshTair(list)
                    }
                  }
                }
              } catch {
                case e: Exception =>
                  LOG.error("get hulk interface error")
                  None
              }

          }
        }
    }
    val t = System.currentTimeMillis()
    LOG.info(s"end refresh hulk api cost time ${t-now} ms")
  }

  def refreshTair(list: List[HulkSetEntity]) = {
    Future {
      if (count == 100) {
        LOG.info(s"100 task has finished current list is $list")
        count = 0;
      }
      count += 1
      list.foreach {
        entity =>
          if(!"".equals(entity.ip) && !"".equals(entity.setName)){
            TairClient.put(hostkey(entity.setName), entity.ip, 1200)
            TairClient.put(ipkey(entity.ip), entity.setName, 1200)
          }
      }
    }
  }


  def r_refresh() = {
    val time = System.currentTimeMillis()
    val res = client.getRunningSetsInfo
    res.getCode match {
      case ErrorCode.OK =>
        val start = System.currentTimeMillis()
        res.getSetsInfo.asScala.map {
          info =>
            if (StringUtils.isNotBlank(info.getIp) && StringUtils.isNotBlank(info.getName)) {
              TairClient.put(ipkey(info.getIp), info.getName, 1200)
            }
            info.getIp -> info.getName
        }.toMap

        res.getSetsInfo.asScala.map {
          info =>
            if (StringUtils.isNotBlank(info.getIp) && StringUtils.isNotBlank(info.getName)) {
              TairClient.put(hostkey(info.getName), info.getIp, 1200)
            }

            info.getName -> info.getIp
        }
        val end = System.currentTimeMillis()
        LOG.debug(s"HulkApi refresh cost ${end - start}")

      case _ => LOG.error(s"HulkApi getRunningSetsInfo failed ${res.getCode}")
    }
    LOG.info(s"refresh hulk ip2host cost ${System.currentTimeMillis() - time}")
  }

  def start() {
    scheduler.scheduleAtFixedRate(new Runnable {
      def run(): Unit = {
        try {
          refresh()
        } catch {
          case e: Exception => LOG.error(s"refresh hulk ip2hosts fail", e)
        }
      }
    }, 0L, 300L, TimeUnit.SECONDS)
  }


  def ip2Host(ip: String) = {
    ip2hostCache.get(ip)
  }

  val hostUrl = if (CommonHelper.isOffline) {
    "http://hulk.test.sankuai.com"
  } else {
    "http://hulk.sankuai.com"
  }

  def ipname(ips: List[String]) = {
    val reqIps = ips.mkString(",")
    val request = s"$hostUrl/api/octo/query/hostnames?ips=${reqIps}"
    val postReq = url(request).GET
    try {
      val future = Http(postReq OK as.String)
      val text = Await.result(future, timeout)
      val ipnames = (Json.parse(text) \ "hostnames").validate[Map[String, String]].asOpt
      if (ipnames.isDefined) {
        ipnames.get.foreach {
          case (ip, name) =>
            if (StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(name)) {
              TairClient.put(hostkey(name), ip, 1200)
              TairClient.put(ipkey(ip), name, 1200)
            }
        }
      }
      if (ipnames.isDefined && ipnames.get.nonEmpty) {
        ipnames.get
      } else {
        ips.map { ip => ip -> ip }.toMap
      }

    } catch {
      case e: Exception =>
        LOG.error(s"get delete failed app", e)
        ips.map { ip => ip -> ip }.toMap
    }
  }

  def host2ip(host: String) = {
    TairClient.get(hostkey(host)).getOrElse(host)
  }

  def ipToHost(ip: String) = {
    ip2hostCache.get(ip)
  }

  private def ipkey(ip: String) = {
    s"hulk.ip2host.${ip}"
  }

  private def hostkey(host: String) = {
    s"hulk.host2ip.${host}"
  }

  def checkDeleteAppkey(appkey: String) = {
    val request = s"$hostUrl/api/delete/${appkey}/check"
    val postReq = url(request).GET
    try {
      val future = Http(postReq OK as.String)
      val text = Await.result(future, timeout)
      Json.parse(text).asOpt[HulkAppKeyCheck]
    } catch {
      case e: Exception =>
        LOG.error(s"check delete appkey error ${appkey}", e)
        None
    }
  }
}
