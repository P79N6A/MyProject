package com.sankuai.octo.msgp.serivce.service

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.meituan.config.listener.IConfigChangeListener
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.Env.Env
import com.sankuai.msgp.common.model.Path.Path
import com.sankuai.msgp.common.model._
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.mnsc.idl.thrift.model.{MNSResponse, MnsRequest}
import com.sankuai.octo.msgp.dao.appkey.AppkeyProviderDao
import com.sankuai.octo.msgp.domain._
import com.sankuai.octo.msgp.model.ProviderNodeSortEle._
import com.sankuai.octo.msgp.model._
import com.sankuai.octo.msgp.serivce.other.PerfApi
import com.sankuai.octo.msgp.serivce.zk.ZkService
import com.sankuai.octo.msgp.utils.client.{MnsCacheClient, ZkClient}
import com.sankuai.octo.mworth.util.DateTimeUtil
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

import scala.collection.JavaConverters._
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.parallel.ForkJoinTaskSupport

object AppkeyProviderService {
  val LOG: Logger = LoggerFactory.getLogger(AppkeyProviderService.getClass)

  val executorthreadPool = Executors.newFixedThreadPool(10)
  private val threadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))
  val sankuaiPath = Path.sankuaiPath

  case class idcNode(idc: String, nodeList: List[ServiceModels.ProviderNode])

  case class statusNode(status: Int, nodeList: List[ServiceModels.ProviderNode])

  case class providerOutline(idcList: List[String], iNode: List[idcNode], statusList: List[Int], sNode: List[statusNode])

  case class providerOutlineSimple(idcList: List[String], idcCount: List[Int], hostCount: List[Int], statusList: List[Int], statusCount: List[Int])

  case class AppkeyProviderOutline(typeList: List[String], typeCount: List[Int],
                                   envList: List[Int], envCount: List[Int],
                                   statusList: List[Int], statusCount: List[Int],
                                   idcList: List[String], idcCount: List[Int]
                                  )

  case class AppkeyIps(appkey: String, ips: List[String])

  object OPSENV extends Enumeration {
    type OPSENV = Value
    val test = Value(1)
    val staging = Value(2)
    val prod = Value(3)
  }


  private var ipListCache: Option[Map[String, List[String]]] = None

  val OCTO_URL = if (CommonHelper.isOffline) {
    "http://octo.test.sankuai.com"
  } else {
    "http://octo.sankuai.com"
  }

  val env_desc = if (CommonHelper.isOffline) {
    "线下"
  } else {
    "线上"
  }

  /**
    * 从db获取所有有服务节点的appkey
    */
  def appkeys() = {
    AppkeyProviderDao.apps()
  }

  def appkeys(ips: List[String]) = {
    AppkeyProviderDao.apps(ips)
  }

  def appkeyHosts(username: String, `type`: String, env: String) = {
    val int_env = env match {
      case "prod" => 3
      case "stage" => 2
      case "test" => 1
    }
    val appkeyProvideMap = AppkeyProviderDao.appkeyProviderby(`type`, int_env).groupBy(_.appkey)
    appkeyProvideMap.map {
      x =>
        AppkeyIps(x._1, x._2.map(_.ip))
    }
  }

  def providerNode(appkey: String, envId: Int) = {
    providerNodeByType(appkey, envId, false)
  }

  /**
    * 供服务分组使用，获取指定appkey的提供者列表
    *
    * @param appkey
    * @param envId 环境 3(prod) 2(stage) 1(test)
    * @return
    */
  def getProviderNode4Route(appkey: String, envId: Int) = {
    val nodes = providerNodeByType(appkey, envId, false)
    nodes.map {
      node =>
        val ipPort = node.split(":")
        if (2 == ipPort.length) {
          val ip = ipPort.apply(0)
          val port = ipPort.apply(1)

          val hostname = if (ip.startsWith("10.")) {
            OpsService.ipToHost(ip)
          } else {
            ip
          }
          ServiceModels.ProviderNode(Some(""), Some(hostname), appkey, "",
            ip, port.toInt, 10, Some(10.0), 2, Some(0),
            0, envId, 0, "",
            Some(0), Some("thrift"), Some(""), Some(""), Some(""),
            None, Some(0))
        }
    }
  }

  def providerNodeHttp(appkey: String, envId: Int) = {
    providerNodeByType(appkey, envId, true)
  }

  private def providerNodeByType(appkey: String, envId: Int, isHttp: Boolean = false) = {
    val providerPath = if (isHttp) {
      Path.providerHttp
    } else {
      Path.provider
    }
    ZkClient.children(List(sankuaiPath, Env.apply(envId), appkey, providerPath).mkString("/")).asScala.toList
  }

  def providerNode(appkey: String, env: String) = {
    ZkClient.children(List(sankuaiPath, env, appkey, Path.provider).mkString("/")).asScala.toList
  }

  def countAliveProviderNode(appkey: String, envId: Int): Int = {
    val nodeList = try {
      provider(appkey, envId)
    } catch {
      case e: Exception =>
        LOG.error(s"查询服务节点失败 appkey:" + appkey + "，envId:" + envId, e)
        List()
    }
    if (nodeList != null && nodeList.nonEmpty) nodeList.count(n => n.status == Status.ALIVE.id)
    else 0
  }

  def searchProvide(appkey: String, envId: Int, keyword: String, page: Page) = {
    //找出所有provide, TODO 解决http
    val list = envId match {
      case 0 =>
        provider(appkey)
      case _ =>
        provider(appkey, envId)
    }

    //做filter
    def f(x: String) = {
      keyword.split(" ").filter(_ != "").forall(self => x.contains(self))
    }

    val result = list.filter(x => f(x.toString))
    page.setTotalCount(result.length)
    result.slice(page.getStart, page.getStart + page.getPageSize)
  }

  def getProviderBySearch(appkey: String, thriftHttp: Int, env: String, keyword: String, status: Int, page: Page, sort: Int = -8): List[ServiceModels.ProviderNode] = {
    var result = if (Appkeys.sgagent.toString != appkey) {
      val t1 = System.currentTimeMillis()
      val mnsSrv = MnsCacheClient.getInstance
      val req = new MnsRequest()
      val protocolType = thriftHttp match {
        case 1 =>
          com.sankuai.octo.mnsc.idl.thrift.model.Protocols.THRIFT
        case 2 =>
          com.sankuai.octo.mnsc.idl.thrift.model.Protocols.HTTP
      }
      req.setAppkey(appkey).setEnv(env).setProtoctol(protocolType)
      val t2 = System.currentTimeMillis()
      LOG.info(s"$appkey t2 - t1=${t2 - t1}ms")
      val resp = mnsSrv.getMNSCacheWithVersionCheck(req)
      val t3 = System.currentTimeMillis()
      LOG.info(s"t3 - t2=${t3 - t2}ms")

      if (resp.code == com.sankuai.octo.mnsc.idl.thrift.model.Constants.SUCCESS) {
        val t3_1 = System.currentTimeMillis()
        val list_res = resp.defaultMNSCache.asScala.map(ServiceModels.SGService2ProviderNode).filter(searchFilter(_, keyword))
        val list = if (status > -1) {
          list_res.filter(_.status == status)
        } else {
          list_res
        }
        val t3_2 = System.currentTimeMillis()
        LOG.info(s"t3_2 - t3_1=${t3_2 - t3_1}ms")
        page.setTotalCount(list.length)
        val sortedResult = sortedProviderNode(list.toList, ProviderNodeSortEle(sort)).slice(page.getStart, page.getStart + page.getPageSize)
        val t4 = System.currentTimeMillis()
        LOG.info(s"t4 - t3_1=${t4 - t3_2}ms")
        sortedResult.map(x => x.copy(name = Some(OpsService.ipToRealtimeHost(x.ip))))
      } else {
        List[ServiceModels.ProviderNode]()
      }
    } else {
      //修改为从数据库读取
      val thriftHttpDesc = thriftHttp match {
        case 2 => "http"
        case _ => "thrift"
      }
      val i_env = Env.withName(env).id
      val temp_list = AppkeyProviderDao.searchByIp(appkey, thriftHttpDesc, i_env, keyword, keyword, "lastupdatetime desc", page: Page)
      val data = temp_list.map { node =>
        ServiceModels.ProviderNode(Some(""), Some(node.hostname), node.appkey, node.version,
          node.ip, node.port, node.weight, Some(node.fweight), node.status, Some(node.enabled),
          node.role, node.env, node.lastupdatetime, node.extend,
          Some(node.servertype), Some(node.protocol), Some(""), Some(node.swimlane), Some(""),
          None, Some(node.heartbeatsupport))
      }
      data
    }
    result
  }

  private def searchFilter(x: ServiceModels.ProviderNode, keyword: String) = {

    val sb = new StringBuilder()
    val date = new Date(x.lastUpdateTime * 1000)
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val dateStr = format.format(date)

    val roleStr = if (0 == x.role) "主用" else "备机"
    val newEnabled = if (x.status == Status.STOPPED.id) 1 else 0
    val enableStr = if (0 == newEnabled) "启用" else "禁用"
    val statusStr = Status(x.status).toString

    sb.append(x.appkey).append(x.version).append(x.ip).append(x.port)
      .append(x.weight).append(x.fweight).append(enableStr).append(x.name)
      .append(roleStr).append(Env(x.env).toString).append(dateStr).append(statusStr).append(x.cell).append(x.swimlane)
    val xStr = sb.toString
    keyword.split(" ").filter(_ != "").forall(xStr.contains(_))
  }


  var blockedMessagingUsername: Set[String] = {
    MsgpConfig.addListener("blocked.messaging.username", new IConfigChangeListener() {
      override def changed(key: String, oldValue: String, newValue: String): Unit = {
        LOG.info(key + " value changed,new value" + newValue)
        blockedMessagingUsername = newValue.split(",").toSet
      }
    })
    MsgpConfig.get("blocked.messaging.username", "").split(",").toSet
  }

  /**
    * 节点更新后,发送节点变更信息给负责人
    *
    * @param appkey
    * @param oldNodes
    * @param updatedNodes
    * @return
    */
  def sendUpdateMessage(appkey: String, username: String, oldNodes: List[ServiceModels.ProviderNode], updatedNodes: List[ServiceModels.ProviderNode]) = {
    if (!blockedMessagingUsername.contains(username)) {
      val contentItems = (oldNodes zip updatedNodes).map { case (oldNode, updatedNode) =>
        if (updatedNode.status != oldNode.status) {
          val sendIp = getHostIp(updatedNode.ip)
          val itemStr = s"$sendIp port: ${updatedNode.port} " +
            s"状态: ${Status.apply(oldNode.status).toString} -> ${Status.apply(updatedNode.status).toString}"
          Some(itemStr)
        } else {
          None
        }
      }
      val validContent = contentItems.flatten
      if (validContent.nonEmpty) {
        val eventTime = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_TIME_FORMAT)
        val subject = s"$eventTime\nOCTO服务节点监控($env_desc)\n"

        val url = s"$OCTO_URL/service/detail?appkey=$appkey#supplier"
        val prefixString = s"服务[$appkey|$url]节点状态变更:\n"

        val suffixString = s"操作用户: $username"

        val contentWithIndex = validContent.zipWithIndex.map { case (item, index) => s"${index + 1}, $item" }

        val message = s"$subject$prefixString${contentWithIndex.mkString("\n")}\n$suffixString"
        ServiceCommon.sendStatusMessage(appkey, message)
      }
    }
  }

  /**
    * 修改zk节点上的SGService
    *
    * @param editNode    节点的本次变更内容
    * @param appkey      节点appkey
    * @param thrift_http 1 thrift 2 http
    * @return
    */
  def updateProvider(editNode: ServiceModels.ProviderEdit, appkey: String, thrift_http: Int): (ServiceModels.ProviderNode, ServiceModels.ProviderNode) = {
    val eNode = editNode
    val prod = getProviderNode(appkey, thrift_http, editNode.env, editNode.ip + ":" + editNode.port) match {
      case Some(oldPro) =>
        if (editNode.appkey.equalsIgnoreCase(appkey)) {
          // TODO fix nonstandard and ugly funciton name
          val pnode = updatePriovder(eNode, oldPro)
          val currentTime = System.currentTimeMillis() / 1000
          // enabled 0 : 启用 1: 停用，映射为status
          val newStatus = pnode.enabled.getOrElse(0) match {
            case 1 => Status.STOPPED.id
            case 0 => if (pnode.status == Status.STOPPED.id) Status.DEAD.id else pnode.status
          }
          val updated = pnode.copy(lastUpdateTime = currentTime, status = newStatus,
            fweight = getFweight(pnode))
          val node = s"${updated.ip}:${updated.port}"
          try {
            val updated_Node = addProviderNodeDefaultValue(updated)
            updateProviderByType(appkey, thrift_http, node, updated_Node)
            BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.updateProvider,
              oldValue = Json.toJson(oldPro).toString, newValue = Json.toJson(updated_Node).toString)
            (oldPro, updated)
          } catch {
            case e: Exception => LOG.error(s"更新服务节点状态失败 $editNode", e)
              null
          }
        } else {
          LOG.error(s"更新服务节点状态失败,appkey不一致 $editNode")
          null
        }
      case None =>
        LOG.info(s"can't updated $editNode")
        null
    }
    prod
  }

  /*
   function：用于上海侧注册接口调用，支持多次注册，和sgagent多次注册的逻辑保持一致
   */
  def saveOceanusProvider(editNode: ServiceModels.ProviderEdit, appkey: String, thrift_http: Int) = {
    val extend = getExtend(editNode, null)
    val eNode = editNode.copy(extend = Some(extend))
    //保存服务节点
    val currentTime = System.currentTimeMillis() / 1000
    val providerNode = ServiceModels.ProviderNode(id = None, name = None, version = "", appkey = eNode.appkey,
      ip = eNode.ip, port = eNode.port, weight = eNode.weight.getOrElse(10),
      fweight = eNode.fweight, status = eNode.status.getOrElse(4), enabled = eNode.enabled,
      role = eNode.role.getOrElse(0), env = eNode.env, lastUpdateTime = currentTime,
      swimlane = eNode.swimlane,
      extend = "", serviceInfo = Some(Map[String, ServiceModels.ServiceDetail]()))

    val prod = getProviderNode(appkey, thrift_http, editNode.env, editNode.ip + ":" + editNode.port) match {
      case Some(oldPro) =>
        updateProviderByType(appkey, thrift_http, eNode.ip + ":" + eNode.port, providerNode)
      case None =>
        addProviderNode(appkey, thrift_http, providerNode)
    }
    prod
  }


  def updateProviderByType(appkey: String, thrift_http: Int, node: String, providerNode: ServiceModels.ProviderNode) {
    val thrift_http_desc = if (1 == thrift_http) Path.provider else Path.providerHttp
    val providerPath = List(sankuaiPath, Env.apply(providerNode.env), appkey, thrift_http_desc).mkString("/")
    val nodePath = List(providerPath, node).mkString("/")
    val nodeData = Json.prettyPrint(Json.toJson(providerNode))
    val apppkeyTs = ServiceModels.AppkeyTs(appkey, providerNode.lastUpdateTime)
    val providerData = Json.prettyPrint(Json.toJson(apppkeyTs))
    val old_data = ZkClient.getData(nodePath)
    // 修改provider子节点时，也要修改更新provide状态，保证watcher触发
    ZkClient.client.inTransaction().setData().forPath(nodePath, nodeData.getBytes("utf-8")).and().setData().forPath(providerPath, providerData.getBytes("utf-8")).and().commit()
    val new_data = ZkClient.getData(nodePath)
    LOG.info(s"update old node data: ${old_data},new node data: ${new_data}")
  }


  private def updatePriovder(pEdit: ServiceModels.ProviderEdit, pnode: ServiceModels.ProviderNode): ServiceModels.ProviderNode = {
    val editPro = pnode.copy(
      weight = {
        if (pEdit.weight.isDefined) {
          pEdit.weight.get
        } else {
          pnode.weight
        }
      },
      fweight = {
        if (pEdit.fweight.isDefined) {
          pEdit.fweight
        } else if (pEdit.weight.isDefined) {
          Some(pEdit.weight.getOrElse(0).toDouble)
        } else {
          pnode.fweight
        }
      },
      role = {
        if (pEdit.role.isDefined) {
          pEdit.role.get
        } else {
          pnode.role
        }
      },
      enabled = {
        if (pEdit.enabled.isDefined) {
          pEdit.enabled
        } else {
          pnode.enabled
        }
      },
      groupInfo = {
        if (pEdit.groupInfo.isDefined) {
          pEdit.groupInfo
        } else {
          pnode.groupInfo
        }
      },
      status = {
        if (pEdit.status.isDefined) {
          pEdit.status.get
        } else {
          pnode.status
        }
      },
      extend = getExtend(pEdit, pnode)
    )
    editPro
  }

  def getExtend(providerNode: ServiceModels.ProviderEdit, pnode: ServiceModels.ProviderNode): String = {
    val extendOpt = providerNode.extend
    val weightOpt = providerNode.weight
    val extend = if (extendOpt.isEmpty && weightOpt.isEmpty) {
      if (pnode != null) {
        pnode.extend
      } else {
        ""
      }
    } else {
      val extend = extendOpt.getOrElse("")
      val weight = weightOpt.getOrElse("").toString
      if (StringUtil.isNotBlank(extend) && StringUtil.isNotBlank(weight)) {
        if (extend.contains("weight:")) {
          val weightInExtend = parseWeightInExtend(extend)
          if (!weight.equals(weightInExtend)) {
            getNewExtend(extend, weight)
          } else {
            extend
          }
        } else {
          extend + "|weight:" + weight
        }
      } else if (StringUtil.isNotBlank(extend)) {
        extend
      }
      else if (StringUtil.isNotBlank(weight)) {
        "|weight:" + weight
      } else {
        ""
      }
    }
    extend
  }

  def getNewExtend(extend: String, weight: String): String = {
    var newExtend = ""
    val items = extend.split("\\|")
    items.foreach({
      item => {
        if (item.contains("weight:")) {
          newExtend += "weight:" + weight + "|"
        } else {
          newExtend += item + "|"
        }
      }
    })
    if (newExtend.endsWith("|")) {
      newExtend = newExtend.substring(0, newExtend.length - 1)
    }
    newExtend
  }

  def addProviderByType(appkey: String, thrift_http: Int, json: String): String = {
    Json.parse(json).validate[ServiceModels.ProviderNode].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        if (x.appkey.equalsIgnoreCase(appkey)) {
          try {
            if (!CommonHelper.isOffline) {
              if (OpsService.isOpsTreeHost(appkey, x.ip, OPSENV.apply(x.env).toString)) {
                val updateProvider = addProviderNode(appkey, thrift_http, x)
                JsonHelper.dataJson(updateProvider)
              } else {
                JsonHelper.errorJson("添加节点服务树不存在，请先在服务树申请节点!")
              }
            } else {
              val updateProvider = addProviderNode(appkey, thrift_http, x)
              JsonHelper.dataJson(updateProvider)
            }
          } catch {
            case e: Exception =>
              LOG.error(s"添加服务提供者失败json${JsonHelper.jsonStr(x)}", e)
              JsonHelper.errorJson(e.getMessage)
          }

        } else {
          LOG.info(s"can't add $appkey $x")
          JsonHelper.errorJson("appkey不匹配")
        }
    })
  }

  @throws(classOf[Exception])
  def addProviderNode(appkey: String, thrift_http: Int, providerNode: ServiceModels.ProviderNode) = {
    val currentTime = System.currentTimeMillis() / 1000
    val updated = providerNode.copy(lastUpdateTime = currentTime, fweight = getFweight(providerNode))
    val updated_Node = addProviderNodeDefaultValue(updated)
    addProviderByType(appkey, thrift_http, updated_Node)
    BorpClient.saveOpt(actionType = ActionType.INSERT.getIndex, entityId = appkey, entityType = EntityType.increaseProvider, newValue = Json.toJson(updated).toString)
    val sendIp = getHostIp(providerNode.ip)
    val eventTime = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_TIME_FORMAT);
    val url = s"${OCTO_URL}/service/detail?appkey=${appkey}#supplier"
    ServiceCommon.sendStatusMessage(appkey, s"OCTO服务节点监控(${env_desc})\n[${appkey}|$url] 添加服务节点 ip:${sendIp},port:${providerNode.port}\n操作时间: $eventTime\n操作用户: ${UserUtils.getUser.getLogin}")
    updated
  }

  def addProviderNodeDefaultValue(providerNode: ServiceModels.ProviderNode) = {
    providerNode.copy(serverType = {
      if (providerNode.serverType.isEmpty) {
        Some(0)
      } else {
        providerNode.serverType
      }
    },
      heartbeatSupport = if (providerNode.heartbeatSupport.isEmpty) {
        Some(0)
      } else {
        providerNode.heartbeatSupport
      },
      protocol = if (providerNode.protocol.isEmpty) {
        Some("")
      } else {
        providerNode.protocol
      },
      serviceInfo = if (providerNode.serviceInfo.isEmpty) {
        Some(Map[String, ServiceModels.ServiceDetail]())
      } else {
        providerNode.serviceInfo
      })
  }

  def addProviderByType(appkey: String, thrift_http: Int, providerNode: ServiceModels.ProviderNode) {
    val thrift_http_desc = if (1 == thrift_http) Path.provider else Path.providerHttp
    val providerPath = List(sankuaiPath, Env.apply(providerNode.env), appkey, thrift_http_desc).mkString("/")
    val node = providerNode.ip + ":" + providerNode.port
    val nodePath = List(providerPath, node).mkString("/")
    val nodeData = Json.prettyPrint(Json.toJson(providerNode))
    val apppkeyTs = ServiceModels.AppkeyTs(appkey, providerNode.lastUpdateTime)
    val providerData = Json.prettyPrint(Json.toJson(apppkeyTs))
    ZkClient.client.inTransaction().create().forPath(nodePath, nodeData.getBytes("utf-8")).and().setData().forPath(providerPath, providerData.getBytes("utf-8")).and().commit()
  }


  def delProviderByType(appkey: String, thrift_http: Int, json: String): String = {
    Json.parse(json).validate[ServiceModels.ProviderEdit].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        LOG.info("del " + x)
        try {
          delProviderByType(appkey, thrift_http, x)
          BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex, entityId = appkey, entityType = EntityType.msgpDelProvider, oldValue = Json.toJson(x).toString)
          val sendIp = getHostIp(x.ip)
          val eventTime = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_TIME_FORMAT);
          val url = s"$OCTO_URL/service/detail?appkey=$appkey#supplier"
          ServiceCommon.sendStatusMessage(appkey, s"$eventTime\nOCTO服务节点监控($env_desc)\nappkey:[$appkey|$url]\n操作用户: ${UserUtils.getUser.getLogin} \n内容: 删除服务节点 ip:$sendIp,port:${x.port}")
          JsonHelper.dataJson(x)
        } catch {
          case e: Exception => LOG.error(f"删除服务提供者失败，$json", e)
            JsonHelper.errorJson(e.getMessage)
        }
    })
  }

  def delProviderListByType(appkey: String, thrift_http: Int, json: String): String = {
    Json.parse(json).validate[List[ServiceModels.ProviderEdit]].fold({
      error =>
        LOG.error(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      list =>
        val resultList = scala.collection.mutable.ArrayBuffer[ServiceModels.ProviderEdit]()
        list.foreach {
          x =>
            try {
              delProviderByType(appkey, thrift_http, x)
              BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex, entityId = appkey, entityType = EntityType.msgpDelProvider, oldValue = Json.toJson(x).toString)
              val sendIp = getHostIp(x.ip)
              resultList.append(x)
            } catch {
              case e: Exception => LOG.error(s"删除服务节点失败,$json", e)
            }
        }
        val eventTime = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_TIME_FORMAT)
        val ips = resultList.map { x => s"ip:${x.ip},port:${x.port}" }.toList
        val url = s"$OCTO_URL/service/detail?appkey=$appkey#supplier"
        ServiceCommon.sendStatusMessage(appkey, s"$eventTime\nOCTO服务节点监控($env_desc)\nappkey: [$appkey|$url]\n操作用户: ${UserUtils.getUser.getLogin}\n内容:删除服务节点 $ips")
        JsonHelper.dataJson(resultList)
    })
  }

  /**
    * 删除所有的服务提供者
    *
    * @param providerDel
    */
  def delProvider(providerDel: ProviderDel) {
    if (StringUtil.isBlank(providerDel.getAppkey)) {
      throw new RuntimeException("appkey 不能为空")
    }
    val appkey = providerDel.getAppkey
    val paths = if (StringUtil.isBlank(providerDel.getProtocol)) {
      List(Path.provider, Path.providerHttp)
    }
    else if ("thrift".equals(providerDel.getProtocol)) {
      List(Path.provider)
    } else if ("http".equals(providerDel.getProtocol)) {
      List(Path.providerHttp)
    } else {
      throw new RuntimeException("无法识别的协议类型")
    }

    val envs = if (providerDel.getEnv > -1) {
      List(Env.apply(providerDel.getEnv))
    } else {
      Env.values.toList
    }
    paths.foreach {
      path => {
        envs.foreach { env =>
          val nodes = getProviderNodesByType(providerDel.getAppkey, env.id, path)
          nodes.filter {
            node =>
              val isIp = if (StringUtil.isNotBlank(providerDel.getIp)) {
                node.ip.equals(providerDel.getIp)
              } else {
                true
              }
              val isPort = if (providerDel.getPort > -1) {
                providerDel.getPort == node.port
              } else {
                true
              }
              isIp && isPort
          }.foreach {
            node =>
              delProviderByIpPort(providerDel.getAppkey, path, env, node.ip, node.port)
              val x = Map("appkey" -> appkey, "env" -> env.toString, "ip" -> node.ip, "port" -> node.port)
              BorpClient.saveOpt(actionType = ActionType.DELETE.getIndex,
                entityId = appkey, entityType = EntityType.msgpDelProvider, oldValue = JsonHelper.jsonStr(x))
          }
        }
      }
    }
  }

  /**
    *
    * 上海册对接需要的删除serviceInfo的接口
    *
    * @param providerServiceInfoDel
    * @return
    */
  def delProviderServiceInfoByIpPort(providerServiceInfoDel: ProviderServiceInfoDel): Array[String] = {
    var result = (-1, "删除失败")
    if (StringUtil.isBlank(providerServiceInfoDel.getAppkey)) {
      result = (-1, "删除失败，appkey不能为空")
      Array(result._1.toString, result._2)
    } else if (!providerServiceInfoDel.getProtocol.equals("thrift")) {
      result = (-1, "删除失败,协议不匹配")
      Array(result._1.toString, result._2)
    } else {
      getProviderNode(providerServiceInfoDel.getAppkey, 1, providerServiceInfoDel.getEnv, providerServiceInfoDel.getIp + ":" + providerServiceInfoDel.getPort) match {
        case Some(oldNode) =>
          var serviceinfo = oldNode.serviceInfo.getOrElse(Map[String, ServiceModels.ServiceDetail]())
          if (serviceinfo.contains(providerServiceInfoDel.getServiceName)) {
            val newserviceinfo = serviceinfo.filterKeys(!_.equals(providerServiceInfoDel.getServiceName))
            val updateNode = oldNode.copy(serviceInfo = Some(newserviceinfo))
            updateProviderByType(providerServiceInfoDel.getAppkey, 1, providerServiceInfoDel.getIp + ":" + providerServiceInfoDel.getPort, updateNode)
            result = (0, "删除成功")
          } else {
            result = (-1, "删除失败，servicename不存在或已被删除")
          }
          Array(result._1.toString, result._2)
        case None =>
          result = (-1, "删除失败，节点不存在")
          Array(result._1.toString, result._2)
      }
    }
  }

  def delProviderByType(appkey: String, thrift_http: Int, providerNode: ServiceModels.ProviderEdit) {
    val thrift_http_desc = if (1 == thrift_http) Path.provider else Path.providerHttp
    delProviderByIpPort(appkey, thrift_http_desc, Env.apply(providerNode.env), providerNode.ip, providerNode.port)
  }

  def delProviderByIpPort(appkey: String, thrift_http_desc: Path, env: Env, ip: String, port: Int) = {

    val providerPath = List(sankuaiPath, env.toString, appkey, thrift_http_desc).mkString("/")
    val node = s"$ip:$port"
    val nodePath = List(providerPath, node).mkString("/")
    if (ZkClient.exist(nodePath)) {
      ZkClient.deleteWithChildren(nodePath)
      val currentTime = System.currentTimeMillis() / 1000
      val apppkeyTs = ServiceModels.AppkeyTs(appkey, currentTime)
      val providerData = Json.prettyPrint(Json.toJson(apppkeyTs))
      ZkClient.setData(providerPath, providerData)
    }
  }

  def groupAttributes(appkey: String) = {
    Env.values.filter(_.id != 0).map(x => Map("name" -> x.toString, "value" -> x.id) ++: groupAttributesByEnv(appkey, x.id)).toList
  }

  def groupAttributesByEnv(appkey: String, env: Int) = {
    // TODO consumer deal with env
    // 转化为hostname给前端
    val consumer = PerfApi.consumerNode(appkey)
    val provider = providerNode(appkey, env).map {
      x =>
        if (x.contains(":")) {
          val arrays = x.split(":")
          OpsService.ipToHost(arrays.apply(0)) + ":" + arrays.apply(1)
        } else {
          x
        }
    }
    Map("consumer" -> consumer, "provider" -> provider)
  }

  def verifyMultiCenter(appkey: String, envId: Int) = {
    val provideList = providerNode(appkey, envId)
    //区分机房查询provide
    var result: String = null
    val ipGroup = provideList.groupBy(x => {
      if (x.startsWith("10.32.") || x.startsWith("10.20.") || x.startsWith("10.21.") || x.startsWith("10.12.")) {
        "北京中心1(大兴、光环、次渠)"
      } else if (x.startsWith("10.4.") || x.startsWith("10.5.")) {
        "北京中心2(永丰)"
      } else {
        "其他"
      }
    }).map(x => (x._1, x._2.length)).toList

    //当ipGroup为空时，没有provider，开启分组也无风险
    if (ipGroup.length == 1) {
      result = "单机房部署,启用多中心有风险"
    } else {
      val list = ipGroup.foldLeft(List[String]()) {
        (list, x) =>
          if (x._2 < 2)
            list ++ List(s"${x._1}机房部署机器数少于两台")
          else
            list
      }
      if (list.nonEmpty) {
        result = list.mkString(",") + ",启用多中心有风险"
      }
    }
    result
  }


  def verifyDefault(appkey: String, envId: Int) = {
    val provideList = providerNode(appkey, envId)
    //区分机房查询provide
    var result: String = null
    val ipGroup = provideList.groupBy(x => {
      if (x.startsWith("10.32.")) {
        "大兴"
      } else if (x.startsWith("10.4.") || x.startsWith("10.5.")) {
        "永丰"
      } else if (x.startsWith("10.12.")) {
        "次渠"
      } else if (x.startsWith("10.20.") || x.startsWith("10.21.")) {
        "光环"
      } else {
        "其他"
      }
    }).map(x => (x._1, x._2.length)).toList

    //当ipGroup为空时，没有provider，开启分组也无风险
    if (ipGroup.length == 1) {
      result = "单机房部署,启用默认分组有风险"
    } else {
      val list = ipGroup.foldLeft(List[String]()) {
        (list, x) =>
          if (x._2 < 2)
            list ++ List(s"${x._1}机房部署机器数少于两台")
          else
            list
      }
      if (list.nonEmpty) {
        result = list.mkString(",") + "启用默认分组有风险"
      }
    }
    result
  }


  def validPath(path: String) = {
    val p = """^(/[a-zA-Z0-9_-[\.]]+){0,}/$""".r
    p.findAllIn(path).hasNext
  }


  def providerIps(appkey: String, env: Int) = provider(appkey, env).map(_.ip)


  def parseWeightInExtend(extend: String) = {
    val items = extend.split("\\|")
    var weight = ""
    val filterItems = items.filter(_.contains("weight:"))
    if (!filterItems.isEmpty && 2 == filterItems.length) {
      weight = filterItems(0).split(":").apply(1)
    }
    weight
  }


  def getCurrTimestamp(): Long = {
    System.currentTimeMillis() / 1000
  }

  def getProviderAllIPs(appkey: String, env: Int) = providerNode(appkey, env).map(_.split(":").apply(0)).distinct

  //服务是否有提供者
  def hasProvider(appkey: String): Boolean = {
    Env.values.foreach {
      env =>
        if (providerNodeByType(appkey, env.id, true).nonEmpty || providerNodeByType(appkey, env.id, false).nonEmpty) {
          return true
        }
    }
    false
  }

  /**
    * 修改单个节点的SGService内容
    *
    * @param appkey      节点的appkey
    * @param thrift_http 1 thrift 2 http
    * @param json
    * @return
    */
  def updateProviderByType(appkey: String, username: String, thrift_http: Int, json: String): String = {
    Json.parse(json).validate[ServiceModels.ProviderEdit].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      editNode =>
        try {
          val providerPair = updateProvider(editNode, appkey, thrift_http)
          if (null != providerPair) {
            val updatedNodes = List[ServiceModels.ProviderNode]() :+ providerPair._2
            val oldNodes = List[ServiceModels.ProviderNode]() :+ providerPair._1
            sendUpdateMessage(appkey, username, oldNodes, updatedNodes)
            JsonHelper.dataJson(providerPair._2.toRich)
          } else {
            JsonHelper.errorJson("更新失败:节点不存在")
          }
        } catch {
          case e: Exception =>
            LOG.error(s"更新服务提供者失败，$json", e)
            JsonHelper.errorJson(e.getMessage)
        }
    })
  }

  /**
    * 修改指定appkey 下节点的状态
    * 1：获取所有的服务提供者
    * 2： ip 决定修改状态
    *
    * @param appKeyProviderStatus
    * @return
    */
  def updateProviderStatus(appKeyProviderStatus: AppKeyProviderStatus): String = {
    val protocol = StringUtils.trimToEmpty(appKeyProviderStatus.getProtocol).toLowerCase

    if ("thrift".equals(protocol)) {
      JsonHelper.dataJson(updateProviderStatusByType(appKeyProviderStatus, Path.provider))
    } else if ("http".equals(protocol)) {
      JsonHelper.dataJson(updateProviderStatusByType(appKeyProviderStatus, Path.providerHttp))
    } else if (protocol.nonEmpty) {
      // invalid prototol
      JsonHelper.errorJson("invalid protocol, currently only support thrift and http.")
    } else {
      //default: compatible the original behavior of this api.
      val data = List(Path.provider, Path.providerHttp).flatMap(updateProviderStatusByType(appKeyProviderStatus, _))
      JsonHelper.dataJson(data)
    }
  }

  def updateProviderNodeStatus(appKeyProviderNodeStatus: AppKeyProviderNodeStatus): String = {
    val protocol = StringUtils.trimToEmpty(appKeyProviderNodeStatus.getProtocol).toLowerCase

    if ("thrift".equals(protocol)) {
      JsonHelper.dataJson(updateProviderNodeStatusByType(appKeyProviderNodeStatus, Path.provider))
    } else if ("http".equals(protocol)) {
      JsonHelper.dataJson(updateProviderNodeStatusByType(appKeyProviderNodeStatus, Path.providerHttp))
    } else if (protocol.nonEmpty) {
      // invalid prototol
      JsonHelper.errorJson("invalid protocol, currently only support thrift and http.")
    } else {
      //default: compatible the original behavior of this api.
      val data = List(Path.provider, Path.providerHttp).flatMap(updateProviderNodeStatusByType(appKeyProviderNodeStatus, _))
      JsonHelper.dataJson(data)
    }
  }


  private def updateProviderNodeStatusByType(appKeyProviderNodeStatus: AppKeyProviderNodeStatus, path: Path) = {
    val appkey = appKeyProviderNodeStatus.getAppkey
    val env = appKeyProviderNodeStatus.getEnv
    val ipPorts = appKeyProviderNodeStatus.getIpports
    val enabled = appKeyProviderNodeStatus.getEnabled
    val status = appKeyProviderNodeStatus.getStatus
    val providers = provider(appkey, env, path)

    val list = providers.filter { x =>
      if (ipPorts.contains(new IpPort(x.ip, x.port))) {
        var isEnable = false;
        if (enabled != null) {
          isEnable = x.enabled.getOrElse(0) != enabled
        }
        status != null || isEnable
      } else {
        false
      }
    }.map {
      node =>
        val editNode = node.toEdit
        editNode.copy(
          enabled = if (enabled != null) {
            Some(enabled)
          } else {
            editNode.enabled
          },
          status = if (status != null) {
            Some(status)
          } else {
            editNode.status
          }
        )
    }
    val thrift_http = if (path == Path.provider) {
      1
    } else {
      2
    }
    val providerPairs = list.map {
      editNode =>
        updateProvider(editNode, appkey, thrift_http)
    }
    val updatedNodes = providerPairs.map(_._2)
    val oldNodes = providerPairs.map(_._1)
    sendUpdateMessage(appkey, appKeyProviderNodeStatus.getUsername, oldNodes, updatedNodes)
    updatedNodes
  }

  private def updateProviderStatusByType(appKeyProviderStatus: AppKeyProviderStatus, path: Path) = {
    val appkey = appKeyProviderStatus.getAppkey
    val env = appKeyProviderStatus.getEnv
    val ips = appKeyProviderStatus.getIps
    val enabled = appKeyProviderStatus.getEnabled
    val status = appKeyProviderStatus.getStatus
    val providers = provider(appkey, env, path)

    val list = providers.filter { x =>
      if (ips.contains(x.ip)) {
        var isEnable = false;
        if (enabled != null) {
          isEnable = x.enabled.getOrElse(0) != enabled
        }
        status != null || isEnable
      } else {
        false
      }
    }.map {
      node =>
        val editNode = node.toEdit
        editNode.copy(
          enabled = if (enabled != null) {
            Some(enabled)
          } else {
            editNode.enabled
          },
          status = if (status != null) {
            Some(status)
          } else {
            editNode.status
          }
        )
    }
    val thrift_http = if (path == Path.provider) {
      1
    } else {
      2
    }
    val providerPairs = list.map {
      editNode =>
        updateProvider(editNode, appkey, thrift_http)
    }
    val updatedNodes = providerPairs.map(_._2)
    val oldNodes = providerPairs.map(_._1)
    sendUpdateMessage(appkey, appKeyProviderStatus.getUsername, oldNodes, updatedNodes)
    updatedNodes
  }

  /**
    * 批量修改节点的SGService内容
    *
    * @param appkey      节点的appkey
    * @param thrift_http 1 thrift 2 http
    * @param json
    * @return
    */
  def updateProviderListByType(appkey: String, username: String, thrift_http: Int, json: String): String = {
    Json.parse(json).validate[List[ServiceModels.ProviderEdit]].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      list =>
        val providerPairs = list.map { node =>
          updateProvider(node, appkey, thrift_http)
        }
        if (null == providerPairs) {
          LOG.error(s"appkey 节点更新失败,json:$json")
          JsonHelper.dataJson("更新失败")
        } else {
          val updatedNodes = providerPairs.map(_._2)
          val oldNodes = providerPairs.map(_._1)
          sendUpdateMessage(appkey, username, oldNodes, updatedNodes)
          JsonHelper.dataJson(updatedNodes)
        }
    })
  }

  def saveProviderEditList(providerEdit: AppKeyProviderEdit): String = {
    val thrift_http = providerEdit.getNodetype
    val appkey = providerEdit.getAppkey
    val providerList = providerEdit.getNodes.asScala.toList
    var isIpInvilid = 0
    var isEnvInvilid = 0
    var isWeightInvilid = 0
    var isStatusInvilid = 0
    providerList.map {
      nodeEdit =>
        if (!StringUtil.isValidIp(nodeEdit.getIp)) isIpInvilid += 1
        if (nodeEdit.getEnv != 1 && nodeEdit.getEnv != 2 && nodeEdit.getEnv != 3) isEnvInvilid += 1
        if (nodeEdit.getWeight < 0 || nodeEdit.getWeight > 100) isWeightInvilid += 1
        if (nodeEdit.getStatus != 0 && nodeEdit.getStatus != 1 && nodeEdit.getStatus != 2 && nodeEdit.getStatus != 4) isStatusInvilid += 1
    }
    var code = 200
    var data = "succesfully"
    if (isIpInvilid != 0) {
      code = 201
      data = "exist invilid ip"
    } else if (isEnvInvilid != 0) {
      code = 201
      data = "exist invilid env value"
    } else if (isWeightInvilid != 0) {
      code = 201
      data = "exist invilid weight"
    } else if (isStatusInvilid != 0) {
      code = 201
      data = "exist invilid status"
    } else {
      val providers = providerList.map { nodeEdit =>
        val node: ServiceModels.ProviderEdit = ServiceModels.ProviderEdit(appkey = appkey, ip = nodeEdit.getIp.trim, port = nodeEdit.getPort,
          env = nodeEdit.getEnv,
          weight = Some(nodeEdit.getWeight),
          fweight = Some(nodeEdit.getWeight.toDouble),
          status = Some(nodeEdit.getStatus),
          enabled = Some(nodeEdit.getEnabled), role = Some(nodeEdit.getRole), swimlane = Some(nodeEdit.getSwimlane), extend = Some(""))
        saveOceanusProvider(node, appkey, thrift_http)
      }
    }
    if (code == 200) {
      JsonHelper.dataJson(data)
    } else {
      JsonHelper.errorDataJson(data)
    }
  }


  case class ProvideGroupByVersion(version: String, providerList: List[ServiceModels.ProviderNode])

  def provideGroupByVersion(appkey: String, envId: Int) = {
    val list = envId match {
      case 0 =>
        provider(appkey)
      case _ =>
        provider(appkey, envId)
    }
    list.map(x => x.copy(name = Some(OpsService.ipToHost(x.ip))))
      .groupBy(_.version).map(x => ProvideGroupByVersion(x._1, x._2)).toList.sortBy(_.version).head
  }

  /**
    *
    * @param appkey 服务标识
    * @return 服务标识的prod环境提供者列表,选择到thrift或http的提供者
    */
  def getProvider(appkey: String): List[String] = {
    getProviderNodes(appkey).map(_.ip)
  }

  /**
    *
    * @param appkey 服务标识
    * @return 服务标识的prod环境提供者列表,选择到thrift或http的提供者
    */
  def getProviderCount(appkey: String) = {
    val providers = AppkeyProviderDao.appProviderCount(appkey)
    providers
  }


  /**
    * 获取正在使用的主机节点
    */
  def getProvider(appkey: String, env: Integer, status: Integer): List[ServiceModels.ProviderHost] = {
    val nodes = getProviderNodes(appkey, env)
    val ipPorts = if (null != status) {
      nodes.filter(_.status == status).map {
        node =>
          new IpPort(node.ip, node.port)
      }
    } else {
      nodes.map {
        node =>
          new IpPort(node.ip, node.port)
      }
    }
    ip2ProviderHost(ipPorts)
  }

  /**
    * 获取正在使用的主机节点
    */
  def getProviderProtocolStatus(appkey: String, ips: String) = {
    val paths = List(Path.provider, Path.providerHttp)
    val iplist = ips.split(",").toList
    val pathDatas = paths.flatMap {
      path =>
        getProviderStatus(appkey, "prod", iplist, path)
    }
    val data = pathDatas.groupBy(_.ip).map {
      case (ip, nodes) =>
        val nodeMap = nodes.map {
          node =>
            (node.protocol, node.status)
        }.toMap
        ServiceModels.ProviderStatus(ip, nodeMap)
    }
    JsonHelper.dataJson(data)
  }

  def getProviderNodeProtocolStatus(appkey: String, env: String, ips: String) = {
    val paths = List(Path.provider, Path.providerHttp)
    val iplist = ips.split(",").toList
    val nodeStatuss = paths.flatMap {
      path =>
        getProviderNodeStatus(appkey, env, iplist, path)
    }
    JsonHelper.dataJson(nodeStatuss)
  }

  private def getProviderStatus(appkey: String, env: String, ips: List[String], path: Path, status: Integer = 2) = {
    val providerPath = Seq(sankuaiPath, env, appkey, path).mkString("/")
    val providerNodeList = ZkClient.children(providerPath).asScala.sorted
    val protocal = path match {
      case Path.provider => "thrift"
      case Path.providerHttp => "http"
    }

    if (providerNodeList.isEmpty) {
      ips.map {
        ip =>
          ServiceModels.ProviderProtocolStatus(ip, protocal, true)
      }
    } else {
      val providerNodes = providerNodeList.flatMap { node => {
        try {
          val data = ZkClient.getData(s"$providerPath/$node")
          Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map { x => x }
        } catch {
          case e: Exception => LOG.error(s"获取服务节点失败,appkey:$appkey,env:$env", e); None
        }
      }
      }.toList
      val nodeMap = providerNodes.groupBy(_.ip)
      ips.map {
        ip =>
          if (!nodeMap.contains(ip)) {
            ServiceModels.ProviderProtocolStatus(ip, protocal, true)
          } else {
            val node = nodeMap.apply(ip)
            val status = if (node.nonEmpty) {
              node.apply(0).status match {
                case 2 => true
                case _ => false
              }
            } else {
              true
            }
            ServiceModels.ProviderProtocolStatus(ip, protocal, status)
          }
      }
    }
  }


  private def getProviderNodeStatus(appkey: String, env: String, ips: List[String], path: Path, status: Integer = 2) = {
    val protocal = path match {
      case Path.provider => "thrift"
      case Path.providerHttp => "http"
    }
    val providerPath = Seq(sankuaiPath, env, appkey, path).mkString("/")
    val providerNodes = ZkService.getProviderByPath(providerPath)
    if (providerNodes.isEmpty) {
      List()
    } else {
      val nodeMap = providerNodes.groupBy(_.ip)
      val data = ips.flatMap {
        ip =>
          if (!nodeMap.contains(ip)) {
            None
          } else {
            val nodes = nodeMap.apply(ip)
            if (nodes.nonEmpty) {
              nodes.map {
                node =>
                  ServiceModels.ProviderNodeProtocolStatus(node.ip, node.port, protocal, node.status)
              }
            } else {
              None
            }
          }
      }
      data
    }
  }


  /**
    * 通过appkey 和IP 获取所有的节点
    */
  private def getProvdierByIP(appkey: String, ip: String): List[ServiceModels.ProviderNode] = {
    val envs = List(Env.prod, Env.stage, Env.test)
    val paths = List(Path.provider, Path.providerHttp)
    val data = envs.flatMap {
      env =>
        paths.flatMap {
          path =>
            val providerPath = Seq(sankuaiPath, env, appkey, path).mkString("/")
            val providerNodeList = ZkClient.children(providerPath).asScala.sorted
            if (providerNodeList.nonEmpty) {
              val providerNodes = providerNodeList.flatMap {
                node => {
                  try {
                    if (node.contains(s"$ip:")) {
                      val data = ZkClient.getData(s"$providerPath/$node")
                      Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map {
                        x => x
                      }
                    } else {
                      None
                    }
                  } catch {
                    case e: Exception => LOG.error(s"获取服务节点失败,appkey:$appkey,env:$env", e);
                      None
                  }
                }
              }.toList
              providerNodes
            } else {
              List()
            }
        }
    }
    data
  }

  def getProvdierBy(appkey: String, ip: String, status: Integer) = {
    val providers = getProvdierByIP(appkey, ip)
    if (status != null) {
      providers.filter(_.status == status.toInt)
    } else {
      providers
    }
  }

  private def ip2ProviderHost(ipPorts: List[IpPort]) = {
    ipPorts.map {
      ipPort =>
        val hostname = OpsService.ipToHost(ipPort.getIp)
        ServiceModels.ProviderHost(ipPort.getIp, ipPort.getPort, s"${
          ipPort.getIp
        }:${
          ipPort.getPort
        }", hostname)
    }
  }


  /**
    *
    */
  def getProviderNodes(appkey: String, env: Int = 3): List[ServiceModels.ProviderNode] = {
    val paths = List(Path.provider, Path.providerHttp)
    paths.flatMap {
      pathType => {
        getProviderNodesByType(appkey, env, pathType)
      }
    }
  }

  /**
    * @param appkey 服务标识
    * @return 服务标识的prod环境提供者列表,选择到thrift或http的提供者
    */
  def getProviderNodesByType(appkey: String, env: Int = 3, pathType: Path): List[ServiceModels.ProviderNode] = {
    val providerPath = Seq(sankuaiPath, Env(env), appkey, pathType).mkString("/")
    val providerNodeList = ZkClient.children(providerPath).asScala.sorted
    providerNodeList.flatMap {
      node => {
        try {
          val data = ZkClient.getData(s"$providerPath/$node")
          Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map {
            x => x
          }
        } catch {
          case e: Exception => LOG.error(s"获取服务节点失败,appkey:$appkey,env:$env", e);
            None
        }
      }
    }.toList
  }

  def getProviderNodeBy(appkey: String, env: String = "prod", status: Int, pathType: Path): List[ServiceModels.ProviderNode] = {
    val providerPath = Seq(sankuaiPath, env, appkey, pathType).mkString("/")
    val providerNodeList = ZkClient.children(providerPath).asScala.sorted
    providerNodeList.flatMap {
      node => {
        try {
          val data = ZkClient.getData(s"$providerPath/$node")
          Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map {
            x => x
          }
        } catch {
          case e: Exception => LOG.error(s"获取服务节点失败,appkey:$appkey,env:$env", e);
            None
        }
      }
    }.toList
      .filter {
        x =>
          if (status > -1) {
            x.status == status
          } else {
            true
          }
      }
  }

  def getProviderNodeCountBy(appkey: String, env: String = "prod", status: Int, thrift_http: Int): Int = {
    val thrift_http_path = if (1 == thrift_http) Path.provider else Path.providerHttp
    getProviderNodeBy(appkey, env, status, thrift_http_path).size
  }


  def apiProvider(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page): List[ServiceModels.ProviderSimple] = {
    getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, page).map(_.toSimpe)
  }

  /**
    * 过滤节点信息中的敏感信息，使用服务负责人进行鉴权
    *
    * @param nodes
    */
  def maskSensitiveProviderNodeInfo(nodes: List[ServiceModels.ProviderNode]) = {
    if (nodes.isEmpty) {
      List[ServiceModels.ProviderNode]()
    } else {
      //暂时使用服务负责人
      val hasAuth = ServiceCommon.isOwnerLogin(nodes.head.appkey, UserUtils.getUser.getLogin)
      if (!hasAuth) {
        nodes.map(_.copy(serviceInfo = Some(Map[String, ServiceModels.ServiceDetail]())))
      } else {
        nodes
      }
    }
  }

  /**
    * 直接读取zk http分组数据
    */
  def getHttpProviderByType(appkey: String, thriftHttp: Int, env: String, ip: String, status: Int, page: Page, sort: Int = -8): List[ServiceModels.ProviderNode] = {
    if (page.getPageSize == -1) {
      List[ServiceModels.ProviderNode]()
    } else {
      val thriftHttpDesc = Path.providerHttp
      val providerPath = List(sankuaiPath, env, appkey, thriftHttpDesc).mkString("/")
      val providerNodeList = ZkClient.children(providerPath).asScala.sorted
      val list = if (StringUtils.isEmpty(ip)) {
        providerNodeList
      } else {
        val nodePar = providerNodeList.par
        nodePar.tasksupport = threadPool
        nodePar.filter {
          x =>
            x.contains(ip)
        }.toList
      }

      val temp_list = {
        page.setTotalCount(list.length)
        list.slice(page.getStart, page.getStart + 10000)
      }
      temp_list.flatMap(node => {
        try {
          val data = ZkClient.getData(s"$providerPath/$node")
          Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map {
            x =>
              val name = Some(OpsService.ipToHost(x.ip))
              // enabled 0 : 启用 1: 停用，从status映射过来
              val newEnabled = if (x.status == Status.STOPPED.id) 1 else 0
              x.copy(name = name, fweight = getFweight(x), enabled = Some(newEnabled),
                protocol = if (x.protocol.isEmpty || (x.protocol.isDefined && StringUtils.isBlank(x.protocol.get))) {
                  Some(thriftHttpDesc.toString.toLowerCase)
                } else {
                  x.protocol
                })
          }
        } catch {
          case e: Exception => LOG.error("获取服务提供者列表失败", e);
            None
        }
      }).toList
    }
  }

  /**
    * 当mns故障的时候 设置zk_provider 为true,直接读取zk，需要通知用户无法过滤
    */
  def getProviderByType(appkey: String, thriftHttp: Int, env: String, ip: String, status: Int, page: Page, sort: Int = -8): List[ServiceModels.ProviderNode] = {
    val zk_provider = MsgpConfig.get("zk_provider", "false").toBoolean
    val result = if (Appkeys.sgagent.toString == appkey || zk_provider) {
      if (page.getPageSize == -1) {
        List[ServiceModels.ProviderNode]()
      } else {
        val thriftHttpDesc = thriftHttp match {
          case 1 => Path.provider
          case 2 => Path.providerHttp
        }
        val providerPath = List(sankuaiPath, env, appkey, thriftHttpDesc).mkString("/")
        val providerNodeList = ZkClient.children(providerPath).asScala.sorted
        val list = if (StringUtils.isEmpty(ip)) {
          providerNodeList
        } else {
          val nodePar = providerNodeList.par
          nodePar.tasksupport = threadPool
          nodePar.filter {
            x =>
              x.contains(ip)
          }.toList
        }

        val temp_list = {
          page.setTotalCount(list.length)
          list.slice(page.getStart, page.getStart + page.getPageSize)
        }
        temp_list.flatMap(node => {
          try {
            val data = ZkClient.getData(s"$providerPath/$node")
            Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map {
              x =>
                val name = Some(OpsService.ipToHost(x.ip))
                // enabled 0 : 启用 1: 停用，从status映射过来
                val newEnabled = if (x.status == Status.STOPPED.id) 1 else 0
                x.copy(name = name, fweight = getFweight(x), enabled = Some(newEnabled),
                  protocol = if (x.protocol.isEmpty || (x.protocol.isDefined && StringUtils.isBlank(x.protocol.get))) {
                    Some(thriftHttpDesc.toString.toLowerCase)
                  } else {
                    x.protocol
                  })
            }
          } catch {
            case e: Exception => LOG.error("获取服务提供者列表失败", e);
              None
          }
        }).toList
      }
    } else {
      val mnsSrv = MnsCacheClient.getInstance
      val req = new MnsRequest()
      val protocolType = thriftHttp match {
        case 1 =>
          com.sankuai.octo.mnsc.idl.thrift.model.Protocols.THRIFT
        case 2 =>
          com.sankuai.octo.mnsc.idl.thrift.model.Protocols.HTTP
      }
      req.setAppkey(appkey).setEnv(env).setProtoctol(protocolType)
      val resp = try {
        mnsSrv.getMNSCacheWithVersionCheck(req)
      }
      catch {
        case e: Exception => LOG.error(s"获取服务提供者列表失败,${
          req
        }", e);
          new MNSResponse(com.sankuai.octo.mnsc.idl.thrift.model.Constants.ILLEGAL_ARGUMENT)
      }
      if (resp.code == com.sankuai.octo.mnsc.idl.thrift.model.Constants.SUCCESS) {
        val dataPar = resp.defaultMNSCache.asScala.par
        dataPar.tasksupport = threadPool
        val list = dataPar.map {
          x =>
            if (StringUtil.isBlank(x.protocol)) {
              x.setProtocol(protocolType.toString.toLowerCase)
            }
            val node = ServiceModels.SGService2ProviderNode(x)
            node
        }.toList
        val list1 = if (status == -1) {
          list
        } else {
          list.filter(_.status == status)
        }

        val list2 = if (StringUtils.isEmpty(ip)) {
          list1
        } else {
          list1.filter(_.ip == ip)
        }
        if (page.getPageSize == -1) {
          list2
        } else {
          page.setTotalCount(list2.length)
          sortedProviderNode(list2, ProviderNodeSortEle(sort)).slice(page.getStart, page.getStart + page.getPageSize)
        }
      } else {
        List[ServiceModels.ProviderNode]()
      }
    }
    result
    //maskSensitiveProviderNodeInfo(result)
  }

  def getProviderByTypeAsJava(appkey: String, thriftHttp: Int, env: String, ip: String, status: Int, page: Page, sort: Int = -8): java.util.List[ServiceModels.ProviderNode] = {
    val providers = getProviderByType(appkey, thriftHttp, env, ip, status, page, sort)
    seqAsJavaList[ServiceModels.ProviderNode](providers)
  }

  def provider(appkey: String, envId: Int, path: Path = Path.provider): List[ServiceModels.ProviderNode] = {
    val nodePath = List(sankuaiPath, Env.apply(envId), appkey, path).mkString("/")
    val nodes = ZkClient.children(nodePath).asScala
    val result = scala.collection.mutable.ArrayBuffer[Option[ServiceModels.ProviderNode]]()
    val latch = new CountDownLatch(nodes.length)
    nodes.foreach(
      node => {
        executorthreadPool.submit(new Runnable {
          override def run(): Unit = {
            val nodePath = List(sankuaiPath, Env.apply(envId), appkey, path, node).mkString("/")
            val providerNode = getProviderNode(nodePath)
            result.synchronized {
              result += providerNode
            }
            latch.countDown()
          }
        })
      }
    )
    latch.await()
    try {
      val list = result.flatMap(x => x).toList.sortBy(r => (-r.lastUpdateTime, r.ip))
      list
    } catch {
      case e: Exception =>
        LOG.error(s"获取服务提供者失败 path $nodePath ", e);
        List[ServiceModels.ProviderNode]()
    }
  }

  def getProviderNode(nodePath: String) = {
    try {
      val data = ZkClient.getData(nodePath)
      LOG.debug(List("get node data", nodePath, data).mkString(" "))
      val res = Json.parse(data).validate[ServiceModels.ProviderNode].asOpt.map {
        x =>
          // enabled 0 : 启用 1: 停用，从status映射过来
          val newEnabled = if (x.status == Status.STOPPED.id) 1 else 0
          x.copy(name = Some(OpsService.ipToHost(x.ip)), fweight = getFweight(x), enabled = Some(newEnabled))
      }
      res
    } catch {
      case e: Exception => LOG.error(s"获取服务提供者失败 path $nodePath ", e);
        None
    }
  }

  /**
    * 查询服务提供者
    *
    * @param appkey
    * @return
    */
  def provider(appkey: String): List[ServiceModels.ProviderNode] = {
    Env.values.filter(_.id != 0).flatMap(x => provider(appkey, x.id, Path.provider)).toList.sortBy(r => (-r.lastUpdateTime, r.ip))
  }

  def httpProvider(appkey: String): List[ServiceModels.ProviderNode] = {
    Env.values.filter(_.id != 0).flatMap(x => provider(appkey, x.id, Path.providerHttp)).toList.sortBy(r => (-r.lastUpdateTime, r.ip))
  }

  private def sortedProviderNode(list: List[ServiceModels.ProviderNode], sort: ProviderNodeSortEle) = {
    sort match {
      case ProviderNodeSortEle.hostnameAsc =>
        list.sortWith((x, y) => x.name.getOrElse("").compareTo(y.name.getOrElse("")) > 0)
      case ProviderNodeSortEle.IPAsc =>
        list.sortWith((x, y) => x.ip.compareTo(y.ip) > 0)
      case ProviderNodeSortEle.portAsc =>
        list.sortBy(_.port)
      case ProviderNodeSortEle.roleAsc =>
        list.sortBy(_.role)
      case ProviderNodeSortEle.versionAsc =>
        list.sortWith((x, y) => x.version.compareTo(y.version) > 0)
      case ProviderNodeSortEle.fweightAsc =>
        list.sortBy(_.fweight)
      case ProviderNodeSortEle.statusAsc =>
        list.sortBy(_.status)
      case ProviderNodeSortEle.lastUpdateTimeAsc =>
        list.sortBy(_.lastUpdateTime)
      case ProviderNodeSortEle.hostnameDesc =>
        list.sortWith((x, y) => x.name.getOrElse("").compareTo(y.name.getOrElse("")) < 0)
      case ProviderNodeSortEle.IPDesc =>
        list.sortWith((x, y) => x.ip.compareTo(y.ip) < 0)
      case ProviderNodeSortEle.portDesc =>
        list.sortBy(-_.port)
      case ProviderNodeSortEle.roleDesc =>
        list.sortBy(-_.role)
      case ProviderNodeSortEle.versionDesc =>
        list.sortWith((x, y) => x.version.compareTo(y.version) < 0)
      case ProviderNodeSortEle.fweightDesc =>
        list.sortBy(-_.fweight.getOrElse(0.0))
      case ProviderNodeSortEle.statusDesc =>
        list.sortBy(-_.status)
      case ProviderNodeSortEle.lastUpdateTimeDesc =>
        list.sortBy(-_.lastUpdateTime)
      case _ =>
        list.sortBy(-_.lastUpdateTime)
    }
  }

  /**
    *
    * @param appkey
    * @param thriftHttp
    * @param envId
    * @param ip
    * @param status
    * @param page
    * @param sort
    */
  def getIPListofProvider(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page, sort: Int = -8) = {
    val providerNodeList = getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, page, sort)
    providerNodeList.map(_.ip).distinct
  }

  /**
    * 按照机房返回provider
    *
    * @param appkey
    * @param thriftHttp
    * @param envId
    * @param ip
    * @param status
    * @param page
    * @param sort
    * @param idcName
    * @return
    */
  def getProviderByIDC(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page, sort: Int = -8, idcName: String) = {
    val pageTemp = new Page()
    pageTemp.setPageSize(-1)
    val idc = IdcName.getIdcByName(idcName)
    val providerNodeList = getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, pageTemp, sort)
    val list = if (idc == "OTHER") {
      val knownIdc = IdcName.idcNameMap.keys.toList.filter(_ != "OTHER")
      providerNodeList.filter {
        x =>
          !knownIdc.contains(CommonHelper.ip2IDC(x.ip))
      }
    } else {
      providerNodeList.filter {
        x =>
          CommonHelper.ip2IDC(x.ip).equals(idc)
      }
    }
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  /**
    * 按照状态返回provider
    *
    * @param appkey
    * @param thriftHttp
    * @param envId
    * @param ip
    * @param status
    * @param page
    * @param sort
    * @return
    */
  def getProviderByStatus(appkey: String, thriftHttp: Int, envId: Int, ip: String, status: Int, page: Page, sort: Int = -8) = {
    val pageTemp = new Page()
    pageTemp.setPageSize(-1)
    val providerNodeList = getProviderByType(appkey, thriftHttp, Env(envId).toString, ip, status, pageTemp, sort)
    val list = providerNodeList.filter(_.status == status)
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  val expiredTime = 60L
  val appsCache = CacheBuilder.newBuilder().expireAfterWrite(expiredTime, TimeUnit.MINUTES)
    .build(new CacheLoader[String, List[ServiceModels.ProviderNode]]() {
      def load(key: String) = {
        try {
          getAllAppProvideDesc(key)
        }
        catch {
          case e: Exception => LOG.error(s"key $key 获取数据失败", e)
            List[ServiceModels.ProviderNode]()
        }
      }
    })

  /**
    * 获取所有环境下 hlb、thrift的提供者节点信息
    *
    * @param nodeType
    * @return
    */
  private def getAllAppProvideDesc(nodeType: String) = {
    LOG.info("####begin getAllAppProvideDesc####")
    val allApps = ServiceDesc.appsName
    // 根据appkey并发获取所有的provide的desc
    val mnsc = MnsCacheClient.getInstance
    val appPar = allApps.par
    appPar.tasksupport = threadPool
    val list = appPar.flatMap {
      appkey =>
        val mnscRet = Env.values.flatMap {
          env =>
            if (ZkClient.children(List(sankuaiPath, env, appkey).mkString("/")).size() <= 0) {
              if (nodeType.equalsIgnoreCase("HLB")) {
                val data = mnsc.getMNSCache4HLB(appkey, "0", env.toString).getDefaultMNSCache
                if (null != data) {
                  data.asScala
                } else {
                  LOG.info(s"$nodeType,$appkey,$env MNSCache is null")
                  None
                }
              } else {
                val data = mnsc.getMNSCache(appkey, "0", env.toString).getDefaultMNSCache
                if (null != data) {
                  data.asScala
                } else {
                  LOG.info(s"$nodeType,$appkey,$env MNSCache is null")
                  None
                }
              }
            } else {
              None
            }
        }
        mnscRet.filter {
          x =>
            null != x && StringUtil.isNotBlank(x.version) && x.version.startsWith(nodeType)
        }
    }.toList

    try {
      list.map(ServiceModels.SGService2ProviderNode)
    }
    catch {
      case e: Exception =>
        LOG.error(s"getMNSCache SGService2ProviderNode  error", e)
        List()
    }
  }

  def getProviderNode(appkey: String, thrift_http: Int, env: Int, node: String) = {
    val thrift_http_desc = if (1 == thrift_http) Path.provider else Path.providerHttp
    val nodePath = List(sankuaiPath, Env.apply(env), appkey, thrift_http_desc, node).mkString("/")
    try {
      if (ZkClient.exist(nodePath)) {
        val data = ZkClient.getData(nodePath)
        Json.parse(data).validate[ServiceModels.ProviderNode].asOpt match {
          case Some(value) =>
            Some(value.copy(fweight = getFweight(value)))
          case None =>
            None
        }
      } else {
        None
      }
    } catch {
      case e: Exception => LOG.error(s"获取服务提供者失败,nodePath:$nodePath", e)
        None
    }
  }

  def getAllThriftProvider = {
    appsCache.get("thrift")
  }

  def getAllHLBProvider = {
    appsCache.get("HLB")
  }

  def getAllIpOfThriftNode(source: String) = {
    source match {
      //从数据库取数据
      case "mysql" => AppkeyProviderDao.ipListByType("thrift")
      //从MNSC取数据
      case _ => AppkeyProviderService.getAllThriftProvider.map(_.ip).filter(_.nonEmpty).distinct
    }
  }

  def getAllIpOfHLBNode(source: String) = {
    source match {
      //从数据库取数据
      case "mysql" => AppkeyProviderDao.ipListByType("http")
      //从MNSC取数据
      case _ => AppkeyProviderService.getAllHLBProvider.map(_.ip).filter(_.nonEmpty).distinct
    }
  }

  def getNodeTypeByIp(ip: String): String = {
    val ipMap = getIpListCache("mysql")
    val typeOption = ipMap.map {
      case (_type, _list) =>
        if (_list.contains(ip)) {
          Some(_type)
        } else {
          None
        }
    }

    val typeResult = typeOption.flatten
    val nodeType = typeResult.size match {
      case 2 => "thrift&http"
      case 1 => typeResult.head
      case _ => "other"
    }
    nodeType
  }

  def getIpListCache(source: String) = {
    val ret = if (ipListCache.isDefined) {
      ipListCache.get
    } else {
      val thriftIp = getAllIpOfThriftNode(source)
      val hlbIp = getAllIpOfHLBNode(source)
      Map("http" -> hlbIp, "thrift" -> thriftIp)
    }
    ipListCache = Some(ret)
    ret
  }

  private def getFweight(node: ServiceModels.ProviderNode) = {
    node.fweight match {
      case Some(value) =>
        Some(value)
      case None =>
        Some(node.weight.toDouble)
    }
  }

  def getHostIp(ip: String): String = {
    val ip_hostname = OpsService.ipToHost(ip)
    if (StringUtil.isNotBlank(ip_hostname)) {
      s"$ip_hostname($ip)"
    } else {
      ip
    }
  }

  /**
    * 返回provider的概要信息
    *
    */
  def getOutline(`type`: String, env: java.lang.Integer, status: java.lang.Integer, idc: String, idcStatus: String = "status") = {
    val counts = if (idcStatus == "status") {
      AppkeyProviderDao.providerStatusCount(`type`, env, status)
    } else {
      AppkeyProviderDao.providerIdcCount(`type`, env, status, idc)
    }
    val typeList = counts._1.map(_.`type`)
    val typeCount = counts._1.map(_.count)

    val envList = counts._2.map(_.env)
    val envCount = counts._2.map(_.count)

    val statusList = counts._3.map(_.status)
    val statusCount = counts._3.map(_.count)

    val idcList = counts._4.map(x => x.idc)
    val idcCount = counts._4.map(_.count)

    AppkeyProviderOutline(typeList, typeCount,
      envList, envCount,
      statusList, statusCount,
      idcList, idcCount
    )
  }


}
