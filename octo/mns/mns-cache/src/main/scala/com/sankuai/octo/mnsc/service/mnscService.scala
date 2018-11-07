package com.sankuai.octo.mnsc.service

import java.util.concurrent.ConcurrentHashMap

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.octo.appkey.model.{AppkeyDesc, AppkeyDescResponse}
import com.sankuai.octo.mnsc.dataCache._
import com.sankuai.octo.mnsc.idl.thrift.model._
import com.sankuai.octo.mnsc.idl.thrift.model.{HttpGroup, groupNode}
import com.sankuai.octo.mnsc.model.httpGroup.httpGroup
import com.sankuai.octo.mnsc.model.service.{AppkeyTs, CacheValue}
import com.sankuai.octo.mnsc.model._
import com.sankuai.octo.mnsc.remote.zk
import com.sankuai.octo.mnsc.utils.{api, config, mnscCommon}
import com.sankuai.sgagent.thrift.model.SGService
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.collection.concurrent.TrieMap
import scala.util.control.Breaks
object mnscService {
  private val LOG: Logger = LoggerFactory.getLogger(mnscService.getClass)

  def getMnsc(req: MnsRequest) = {
    req.getProtoctol match {
      case Protocols.THRIFT =>
        getCache(req, Path.provider.toString, appProviderDataCache.getProviderCache, appProviderDataCache.updateProviderCache)
      case Protocols.HTTP =>
        getCache(req, Path.providerHttp.toString, appProviderHttpDataCache.getProviderHttpCache, appProviderHttpDataCache.updateProviderCache)
      case _ =>
        val result = new MNSResponse
        result.setCode(Constants.ILLEGAL_ARGUMENT)
        result
    }
  }

  private def getCache(req: MnsRequest, providerPath: String, fCache: (String, String, Boolean) => Option[service.CacheValue], fpar: (String, String, String, String, Boolean) => service.CacheValue) = {
    val result = new MNSResponse
    val cacheProviders = fCache(req.appkey, req.env, false)
    val path = s"${mnscCommon.rootPre}/${req.getEnv}/${req.getAppkey}/$providerPath"
    val (zkVersion, _) = zk.getNodeVersion(path)
    cacheProviders match {
      case Some(item) =>
        result.setCode(Constants.SUCCESS)
        if (zk.versionCompare(item.version, zkVersion, true, (arg1: Long, arg2: Long) => arg1 == arg2)) {
          result.setDefaultMNSCache(item.SGServices.asJava)
            .setVersion(item.version)
        } else {
          val (version,_) = zk.getNodeVersion(s"${mnscCommon.rootPre}/${req.getEnv}/${req.getAppkey}/$providerPath")
          val providersUpdate = fpar(version, req.getAppkey, req.getEnv, providerPath, false)
          if (null == providersUpdate) {
            result.setDefaultMNSCache(item.SGServices.asJava)
              .setVersion(item.version)
          } else {
            result.setDefaultMNSCache(providersUpdate.SGServices.asJava)
              .setVersion(providersUpdate.version)
          }
        }
      case None =>
        result.setCode(Constants.NOT_FOUND)
    }
    result
  }

  def getMnsc(appkey: String, version: String, env: String) = {
    val providers = appProviderDataCache.getProviderCache(appkey, env, false)
    getThriftData(providers, appkey, version, env)
  }

  private def getThriftData(providers: Option[service.CacheValue], appkey: String, version: String, env: String) = {
    val res = new MNSResponse
    providers match {
      case Some(value) =>
        // if input version is smaller than or equal to cache version,
        if (zk.versionCompare(version, value.version, true, (arg1: Long, arg2: Long) => arg1 <= arg2)) {
          res.setCode(Constants.SUCCESS)
            .setDefaultMNSCache(value.SGServices.asJava)
            .setVersion(value.version)
        } else if (Appkeys.largeAppkeys.contains(appkey)){
          val data = appProviderDataCache.getPathData(appkey,env)
          res.setCode(Constants.SUCCESS)
            .setDefaultMNSCache(data.SGServices.asJava)
            .setVersion(data.version)
        } else {
          res.setCode(Constants.NOT_MODIFIED)
        }
      case None =>
        LOG.debug(s"localCache don't exist $appkey|$env")
        res.setCode(Constants.NOT_FOUND)

    }
    res
  }


  def getMNSCache4HLB(appkey: String, version: String, env: String) = {
    val providers = appProviderHttpDataCache.getProviderHttpCache(appkey, env, false)
    getThriftOrHLBCache(providers, appkey, version, env)
  }

  private def getThriftOrHLBCache(providers: Option[service.CacheValue], appkey: String, version: String, env: String) = {
    val res = new MNSResponse
    providers match {
      case Some(value) =>
        // if input version is smaller than or equal to cache version,
        if (zk.versionCompare(version, value.version, true, (arg1: Long, arg2: Long) => arg1 <= arg2)) {
          res.setCode(Constants.SUCCESS)
            .setDefaultMNSCache(value.SGServices.asJava)
            .setVersion(value.version)
        } else {
          res.setCode(Constants.NOT_MODIFIED)
        }
      case None =>
        LOG.debug(s"localCache don't exist $appkey|$env")
        res.setCode(Constants.NOT_FOUND)

    }
    res
  }

  def getMNSCacheByAppkeys(appkeys: java.util.List[String], protocol: String) = {
    val ret = new MNSBatchResponse()
    ret.cache = new ConcurrentHashMap[String, java.util.Map[String, java.util.List[SGService]]]()
    appkeys.asScala.foreach {
      appkey =>
        Env.values.foreach {
          env =>
            val service = if ("thrift".equalsIgnoreCase(protocol)) {
              appProviderDataCache.getProviderCache(appkey, env.toString, false)
            } else if ("http".equalsIgnoreCase(protocol)) {
              appProviderHttpDataCache.getProviderHttpCache(appkey, env.toString, false)
            } else {
              None
            }
            val serviceItem = service.getOrElse(new CacheValue(s"$appkey|$env", List())).SGServices.asJava
            //            ret.cache.synchronized {
            if (null == ret.cache.get(appkey)) {
              ret.cache.put(appkey, new ConcurrentHashMap[String, java.util.List[SGService]]())
            }
            //            }
            ret.cache.get(appkey).put(env.toString, serviceItem)
        }
    }
    ret
  }

  def getDescInfo(appkey: String) = {
    val result = new AppkeyDescResponse()
    val allDescData = appDescDataCache.getDescDataAll
    if (allDescData.keySet.contains(appkey)) {
      result.setErrCode(200)
      result.setMsg("OK")
      val item = allDescData(appkey)
      result.setDesc(new AppkeyDesc(item.appkey, item.category, item.business.getOrElse(0), item.base.getOrElse(0), item.owt.getOrElse(""), item.pdl.getOrElse(""), item.regLimit.getOrElse(0), item.cell.getOrElse("")))
    } else {
      result.setErrCode(504)
      result.setMsg("Can not find appkey desc info")
    }
    result
  }

  def getAppKeyListByBusinessLine(bizCode: Int, env: String, isCell: Boolean) = {
    LOG.debug(s"[getAppKeyListByBusinessLine] Input--> bizCode=$bizCode env=$env")
    val allDescData = appDescDataCache.getDescDataAll
    if (allDescData.size <= 0) {
      val result = new AppKeyListResponse(Constants.MNSCache_UPDATE)
      result
    } else {
      val businessAppkeys = allDescData.filter(_._2.business.get == bizCode).keySet.toList
      //val businessAppkeys = allDescData.filter(_._2.business == bizCode).keySet.toList
      val providersAppkeys =
        if (isCell) {
          appProviderHttpDataCache.getAppkeysWithCell(env)
        } else {
          appProviderHttpDataCache.getAppkeysWithProviders(env)
        }

      val filterAppkeys = businessAppkeys.filter(providersAppkeys.contains)
      val result = new AppKeyListResponse(Constants.SUCCESS)
      result.setAppKeyList(filterAppkeys.asJava)
      result
    }
  }

  def getHttpPropertiesByBusinessLine(bizCode: Int, env: String) = {
    LOG.debug(s"[getHttpPropertiesByBusinessLine] Input--> bizCode=$bizCode env=$env")
    val allDescData = appDescDataCache.getDescDataAll
    val result = new HttpPropertiesResponse(Constants.SUCCESS)
    if (allDescData.size <= 0) {
      result.setCode(Constants.MNSCache_UPDATE)
    } else {
      result.setCode(Constants.SUCCESS)
      val finalAppKeyList = allDescData.filter(_._2.business.get == bizCode).keySet.toList
      //val finalAppKeyList = allDescData.filter(_._2.business == bizCode).keySet.toList
      val conversionsMap = finalAppKeyList.map {
        appkey =>
          val cache = httpPropertiesDataCache.getHttpPropertiesCacheByAppKey(appkey, env)
          val data = cache match {
            case Some(value) => value.Properties.asJava
            case None => null
          }
          appkey -> data
      }
      result.setPropertiesMap(conversionsMap.filter(x => null != x._2).toMap.asJava)
    }
    result
  }

  def getHttpPropertiesByAppkey(appkey: String, env: String) = {
    LOG.debug(s"[getHttpPropertiesByAppkey] Input--> appkey=$appkey env=$env")
    val httpProperties = httpPropertiesDataCache.getHttpPropertiesCacheByAppKey(appkey, env)
    val result = new HttpPropertiesResponse()
    httpProperties match {
      case Some(value) =>
        result.setCode(Constants.SUCCESS)
          .setPropertiesMap(Map(appkey -> value.Properties.asJava).asJava)
      case None =>
        LOG.error(s"httpPropertiesDataCache don't exist $appkey|$env")
        result.setCode(Constants.MNSCache_UPDATE)
    }
    result
  }

  def getHttpProperties4Api(appkey: String, env: String) = {
    val httpProperties = httpPropertiesDataCache.getHttpPropertiesCacheByAppKey(appkey, env)
    httpProperties match {
      case Some(value) =>
        api.dataJson200(value)
      case None =>
        api.errorJson(404, "can't find")
    }
  }

  def getAllGroups(env: String) = {
    val allGroupMap = httpGroupDataCache.getAllGroups(env)
    val envGroups: TrieMap[String, TrieMap[String, HttpGroup]] = TrieMap()
    val result = new AllHttpGroupsResponse()
    allGroupMap.foreach {
      x =>
        val gApk = x._1.split("\\|")(1)
        //遍历version
        x._2.keys.foreach {
          cacheVersion =>
            val httpGroupData = x._2.get(cacheVersion)
            httpGroupData match {
              case Some(groups) => envGroups += (gApk -> groups)
              case None => LOG.warn("Unknown data structure")
            }
        }
    }
    result.setCode(200)

    val conversionsMap: java.util.Map[String, java.util.Map[String, HttpGroup]] =
      envGroups.map(
        x => (x._1 -> x._2.toMap.asJava)
        ).asJava
    result.setAllGroups(conversionsMap)
    result
  }

  def getHlbGroupByAppkey(appkey: String, env: String) = {
    val groupMap = httpGroupDataCache.getGroupCache(appkey, env)
    groupMap match {
      case Some(groups) =>
        val result = new HttpGroupResponse()
        var httpGroups: Map[String, HttpGroup] = Map()
        groups.keys.foreach {
          cacheVersion =>
            val groupMap= groups.get(cacheVersion)
            groupMap.foreach{
              groupItem => groupItem.foreach{
                groupData =>
                httpGroups += (groupData._1 -> groupData._2)
              }
            }
        }
        result.setCode(200)
        result.setGroups(httpGroups.asJava)
        result
      case None =>
        val result = new HttpGroupResponse()
        result.setCode(500)
        result
    }
  }

  def getProvidersByIP(ip: String) = {
    LOG.debug(s"[getProviderByIP] Input--> ip=$ip")
    val ret = new MNSResponse()
    try {
      val thriftProviders = appProviderDataCache.getProvidersByIP(ip)
      val httpProviders = appProviderHttpDataCache.getProvidersByIP(ip)
      val list = thriftProviders ::: httpProviders
      ret.setCode(200)
      ret.setDefaultMNSCache(list.asJava)
    } catch {
      case e: Exception =>
        ret.setCode(500)
    }
    ret
  }

  def delProvider(appkey: String, env: Int, serverType: Int, ip: String, port: Int) = {
    val mnsRoot = mnscCommon.rootPre
    val envStr = Env(env).toString
    val providerStr = ServerType(serverType).toString
    val providerPath = s"$mnsRoot/$envStr/$appkey/$providerStr"
    val node = s"$ip:$port"
    val path = s"$providerPath/$node"

    if (zk.exist(path)) {
      try {
        val times = System.currentTimeMillis() / 1000
        val apppkeyTs = AppkeyTs(appkey, times)
        val providerData = Json.prettyPrint(Json.toJson(apppkeyTs))

        zk.client.inTransaction
          .delete().forPath(path)
          .and()
          .setData().forPath(providerPath, providerData.getBytes("utf-8"))
          .and().commit()

        true
      } catch {
        case e: Exception =>
          LOG.error(s"delete $path catch $e")
          false
      }
    } else {
      false
    }
  }

  def getAppkeysWithProviders(env: String, proto: String) = {
    proto match {
      case "http" =>
        val list = appProviderHttpDataCache.getAppkeysWithProviders(env)
        api.dataJson(200, "success", list)
      case _ =>
        api.errorJson(400, "protocol can only be http")
    }
  }

  def getAppkeyListByIP(ip: String) = {
    val appkeys = appProviderDataCache.getAppkeysByIP(ip) ++ appProviderHttpDataCache.getAppkeysByIP(ip)
    appkeys.toList.asJava
  }

  def isPigeon(version: String) = {
    val regex = """^\d+\.\d+\.\d+""".r
    version.contains("pigeon") || regex.findFirstMatchIn(version).isDefined
  }

  def handleProtocol(list: List[SGService], version: String) = {
    val reqProtocol = isPigeon(version)
    //只有pigeon和mtthrift进行双框架判断，其他协议直接允许注册
    if (!reqProtocol && !version.contains("mtthrift")) {
      true
    } else {
      var pigeon = false
      var mtthrift = false
      list.toStream.takeWhile(_ => !pigeon || !mtthrift).foreach(item
      => if (!Option(item.version).getOrElse("").isEmpty) {
          if (isPigeon(item.version)) {
            pigeon = true
          } else if (item.version.contains("mtthrift")) {
            mtthrift = true
          }
        })

      //两种协议都没有
      if (!pigeon && !mtthrift) {
        true
      } else {
        if (reqProtocol) {
          //请求是pigeon协议
          pigeon
        } else {
          mtthrift
        }
      }
    }
  }

  def registerCheck(provider : SGService) = {
    val ret = new RegisterResponse()
    ret.setCode(200)
    provider.getProtocol match {
      case "thrift" => {
        val startTime = System.currentTimeMillis

        val serviceList = appProviderDataCache.getProviderCache(provider.getAppkey, Env(provider.getEnvir).toString, false).getOrElse(CacheValue("", List()))
        val rgsChk = handleProtocol(serviceList.SGServices, provider.getVersion) ||
                     config.get("white_appkeys").split(",").contains(provider.appkey)

        //filter ip already in jenkins appkey
        var authorize = true
        if (rgsChk && !ProcessInfoUtil.isLocalHostOnline && "com.sankuai.ee.jenkins.slave" != provider.appkey) {
          val loop = new Breaks
          loop.breakable {
            Env.values.foreach {
              env =>
                if (appProviderDataCache.getProviderCache("com.sankuai.ee.jenkins.slave", env.toString, false)
                  .get.SGServices.exists(_.ip == provider.ip)) {
                  authorize = false
                  loop.break
                }
            }
          }
        }

        LOG.info(s"registerCheck cost ${System.currentTimeMillis - startTime}")
        ret.setAllowRegister(rgsChk & authorize)
      }
      case _ => {
        ret.setCode(400)
        .setAllowRegister(false)
        .setMsg("Register check must take a valid protocol.")
      }
      //TODO: other protocol
    }
    ret
  }
}
