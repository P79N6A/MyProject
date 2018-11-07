package com.sankuai.octo.msgp.serivce.service

import java.util
import java.util.concurrent.{Executors, TimeUnit}

import javax.servlet.http.Cookie
import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.meituan.config.service.{HttpClient, MtHttpRequest, MtHttpResponse}
import com.sankuai.msgp.common.model._
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, xm}
import com.sankuai.msgp.common.utils.client.{BorpClient, Messager, TairClient}
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper, Md5Helper}
import com.sankuai.octo.config.model.{AddGroupRequest, ConfigFile, ConfigFileRequest, FilelogRequest, UpdateGroupRequest, file_param_t, _}
import com.sankuai.octo.msgp.domain.MccConfigItem
import com.sankuai.octo.msgp.model._
import com.sankuai.octo.msgp.serivce.mcc.MtConfigService
import com.sankuai.octo.msgp.utils.XMUtil
import com.sankuai.octo.msgp.utils.client.{MnsCacheClient, ZkClient}
import com.sankuai.octo.mworth.utils.CronScheduler
import com.sankuai.octo.statistic.util.ExecutionContextFactory
import com.sankuai.sgagent.thrift.model.SGService
import dispatch._
import org.apache.commons.lang3.StringUtils
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.RetryUntilElapsed
import org.apache.http.HttpStatus
import org.apache.thrift.TApplicationException
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.joda.time.DateTime
import org.quartz._
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsError, Json}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object ServiceConfig {
  val LOG = LoggerFactory.getLogger(ServiceConfig.getClass)
  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)
  private implicit val timeout = Duration.create(60L, duration.SECONDS)

  private val monitorExe = Executors.newScheduledThreadPool(2)

  private val alarmUserList = List("yangjie17", "zhangcan02", "zhoufeng04", "zhangjinlu")

  private final val SGAENT_DISKFULL_ERROR_CODE = -101009

  private val old_data_base_path = "/meituan/core/webapps"
  private val new_data_base_path = "/sankuai/webapp"
  private val old_global_data_path = "/meituan/core/webapps/cfgs"
  private val new_global_data_path = "/sankuai/config"

  private val waimaiProdZkUrl = "10.4.245.47:9331,10.4.245.49:9331,10.4.245.43:9331"
  private val waimaiStageZkUrl = "10.4.234.225:2181,10.4.238.237:2181,10.4.232.92:2181"
  private val waimaiTestZkUrl = "10.4.232.164:9331,10.4.238.20:9331,10.4.233.31:9331"
  private val offlineMccZkUrl = "sgconfig-zk.sankuai.com:9331"

  private val sgZkUrl = "sgconfig-zk.vip.sankuai.com:2181"
  private val onlineMccZkUrl = "cos-zk.vip.sankuai.com:2181"

  private var waimaiTestSgZk: Option[CuratorFramework] = None
  private var waimaiStageSgZk: Option[CuratorFramework] = None
  private var waimaiProdSgZk: Option[CuratorFramework] = None
  private var sgZk: Option[CuratorFramework] = None

  private var offlineMccZk: Option[CuratorFramework] = None
  private var onlineMccZk: Option[CuratorFramework] = None
  private val mccSrv = MtConfigService.getInstance


  private val hostUrl = if (CommonHelper.isOffline) {
    "http://config.inf.test.sankuai.com/"
  } else {
    "http://config.sankuai.com/"
  }

  private case class DistributeAndEnableResponse(successList: List[DomainIP] = List(), distributeErr: List[DomainIP] = List(), enableErr: List[DomainIP] = List())

  case class DomainIP(domain: String, IP: String, idc: String = "", msg: String = "")

  case class FileContentResponse(filename: String, filepath: String, version: Long, md5: String, filecontent: String)

  case class SaveFileContent(fileName: String, filePath: String, env: Int, groupID: String, fileContent: String)

  case class jsonPushAndEnableFile(env: Int, groupID: String, fileName: String, IPs: List[String])

  case class fileConfigGroupsItem(id: Option[String], appkey: String, env: Int, groupName: String, createTime: Option[Long], updateTime: Option[Long], fileList: Option[List[String]], IPs: Option[List[String]], version: Option[String])

  case class mccStatisticData(appkey: String, time: String, businessLine: String)

  case class MCCEntry(key: String, value: String, comment: String, isDeleted: Boolean)

  case class MCCPullRequest(appkey: String, nodename: String, env: Int, note: String, data: List[MCCEntry])

  case class ConfigItemEdit(nodeName: String, nodeData: String, spaceName: String, version: String)

  case class ConfigItemEditWithRollback(rollback: Boolean, nodeName: String, nodeData: String, spaceName: String, version: String)

  case class ReviewRequest(appkey: String, prID: Int, note: String, approve: Int)

  implicit val jsonMCCEntryReads = Json.reads[MCCEntry]
  implicit val jsonMCCEntrywrites = Json.writes[MCCEntry]

  implicit val jsonMCCPullRequestReads = Json.reads[MCCPullRequest]
  implicit val jsonMCCPullRequestwrites = Json.writes[MCCPullRequest]

  implicit val jsonFileConfigGroupsReads = Json.reads[fileConfigGroupsItem]
  implicit val jsonFileConfigGroupsWrites = Json.writes[fileConfigGroupsItem]

  implicit val jsonFileConfigReads = Json.reads[SaveFileContent]
  implicit val jsonFileConfigWrites = Json.writes[SaveFileContent]

  implicit val jsonPushAndEnableFileReads = Json.reads[jsonPushAndEnableFile]
  implicit val jsonPushAndEnableFileWrites = Json.writes[jsonPushAndEnableFile]

  implicit val jsonReviewReads = Json.reads[ReviewRequest]
  implicit val jsonReviewWrites = Json.writes[ReviewRequest]

  implicit val jsonConfigEditReads = Json.reads[ConfigItemEdit]
  implicit val jsonConfigEditwrites = Json.writes[ConfigItemEdit]

  private val onlineOrOfflineStr = ServiceCommon.env_desc

  def addHeaderAndCookie(urlString: String, cookies: Array[Cookie] = null) = {
    var result = url(urlString).addHeader("X-Fe-Server-Token", "ce4d91de46edf6c8e767189e4fa7a91e").addHeader("Content-Type", "application/json;charset=utf-8")
    if (cookies != null) {
      cookies.foreach {
        x =>
          result = result.addCookie(new com.ning.http.client.cookie.Cookie(x.getName, x.getValue, x.getValue, x.getDomain, x.getPath, -1, x.getMaxAge, x.getSecure, x.isHttpOnly))
      }
    }
    result
  }

  //异步接口，不需要获取返回结果
  def addSpace(appkey: String) = {
    val url = s"$hostUrl/config2/temp/spaces/add"
    val postReq = addHeaderAndCookie(url).POST << JsonHelper.jsonStr(Map("spaceName" -> appkey))
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    LOG.info(s"create mtconfig spaceName $appkey,content ${content}")
    content
  }

  //异步接口，不需要获取返回结果
  def delSpace(appkey: String) = {
    val url = s"$hostUrl/config2/temp/spaces/delete"
    val postReq = addHeaderAndCookie(url).POST << JsonHelper.jsonStr(Map("spaceName" -> appkey))
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    LOG.info(s"delete mtconfig spaceName $appkey,content ${content}")
    content
  }

  def getNodeData(appkey: String, nodeName: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config2/group/$appkey/node/get?nodeName=$nodeName"
    //val url = s"$hostUrl/config/space/$appkey/node/get?nodeName=$nodeName"
    val getReq = addHeaderAndCookie(url, cookies)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def updateMCCSettings(appkey: String, json: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config/spaces/$appkey/settings/update"
    val getReq = addHeaderAndCookie(url, cookies).POST << json
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def settingsData(appkey: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config/spaces/$appkey/settings/data"
    val getReq = addHeaderAndCookie(url, cookies)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def addNode(appkey: String, json: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config2/temp/space/$appkey/node/add"
    val postReq = addHeaderAndCookie(url, cookies).POST << json
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def delNode(appkey: String, json: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config2/temp/space/$appkey/node/delete"
    val postReq = addHeaderAndCookie(url, cookies).POST << json
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def updateNodeData(appkey: String, configItem: MccConfigItem, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config2/temp/space/$appkey/node/update"
    val configItemEdit = ConfigItemEditWithRollback(configItem.isRollback, configItem.getNodeName, configItem.getNodeData, configItem.getSpaceName, configItem.getVersion);
    val json = JsonHelper.jsonStr(configItemEdit)
    val postReq = addHeaderAndCookie(url, cookies).POST << json
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  /**
    *动态配置回滚
    * @param appkey
    * @param json
    * @param cookies
    * @return
    */
  def configRollback(appkey: String, json: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config2/rollback/$appkey/configrollback"
    val postReq = addHeaderAndCookie(url, cookies).POST << json
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }


  def syncLog(appkey: String, queryString: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config/space/$appkey/node/operationrecord?$queryString"
    val postReq = addHeaderAndCookie(url, cookies)
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  /*操作日志*/
  def syncFileLog(appkey: String, queryString: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config/filelog/$appkey/operationfilerecord?$queryString"
    val postReq = addHeaderAndCookie(url, cookies)
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }


  def getAuthToken(token: String, authPath: String, cookies: Array[Cookie]) = {
    val url = s"$hostUrl/config2/auth/get?token=$token&authPath=$authPath"
    val getReq = addHeaderAndCookie(url, cookies)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }


  def addSpaceForAllApp(cookies: Array[Cookie]) = {
    val apps = ServiceCommon.apps().asScala
    LOG.info(apps.toString())
    apps.foreach(x => addSpace(x))
  }

  def uploadFileConfig(appkey: String,
                       env: Int,
                       groupID: String,
                       filename: String,
                       filepath: String,
                       filecontent: Array[Byte]) = {
    val configFile = new ConfigFile
    configFile.setFilename(filename)
    configFile.setFilepath(filepath)
    configFile.setType("file")
    configFile.setPrivilege("")
    configFile.setVersion(0L)
    configFile.setMd5(Md5Helper.getMD532(filecontent))
    configFile.setFilecontent(filecontent)
    configFile.setErr_code(com.sankuai.octo.config.model.Constants.SUCCESS)
    //保存用户名，暂时使用reserved
    val username = getLoginName()
    configFile.setReserved(username)

    val configFiles = List(configFile).asJava

    val fileParamT = new file_param_t
    val envDesc = Env(env).toString

    fileParamT.setAppkey(appkey)
    fileParamT.setEnv(envDesc)
    fileParamT.setConfigFiles(configFiles)
    fileParamT.setGroupId(groupID)
    try {
      val ret = mccSrv.setFileConfig(fileParamT)
      if (com.sankuai.octo.config.model.Constants.SUCCESS == ret.getErr) {
        JsonHelper.dataJson(ret)
      } else {
        LOG.error(s"mtConfigClient.setFileConfig ret is $ret")
        JsonHelper.errorJson("上传文件失败，请重试")
      }
    } catch {
      case e: Exception =>
        LOG.error(s"serviceConfig uploadFileConfig exception", e)
        JsonHelper.errorJson("上传文件失败，请重试")
    }
  }

  def getLoginName() = {
    if (null != UserUtils.getUser) {
      UserUtils.getUser.getLogin
    } else {
      "未知用户"
    }
  }

  def saveFileContent(json: String, appkey: String) = {
    Json.parse(json).validate[SaveFileContent].fold({
      error =>
        LOG.info(error.toString())
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        uploadFileConfig(appkey, x.env, x.groupID, x.fileName, x.filePath, x.fileContent.getBytes)
    })
  }

  /**
    * 删除配置文件
    *
    * @param appkey,env,groupID,filename
    * @return true or false
    */
  def deleteConfigFile(appkey: String, env: Int, groupID: String, fileName: String) = {
    val envDesc = Env(env).toString
    val username = getLoginName()
    val req = new DeleteFileRequest
    req.setAppkey(appkey)
      .setEnv(envDesc)
      .setGroupID(groupID)
      .setFileName(fileName)
      .setUsername(username)
    if (null != mccSrv) {
      Constants.SUCCESS == mccSrv.deleteFileConfig(req)
    }
    else {
      false
    }
  }

  def getFilenameList(appkey: String, env: Int, groupID: String) = {
    val envDesc = Env(env).toString
    val fileParamT = new file_param_t
    fileParamT.setGroupId(groupID)
    fileParamT.setAppkey(appkey)
    fileParamT.setEnv(envDesc)
    try {
      val ret = mccSrv.getFileList(fileParamT)
      if (com.sankuai.octo.config.model.Constants.SUCCESS == ret.getErr) {
        JsonHelper.dataJson(ret.getConfigFiles)
      } else {
        LOG.error(s"mtConfigClient.getFileList ret is $ret")
        JsonHelper.errorJson("服务器错误，mcc errorCode = " + ret.getErr)
      }
    } catch {
      case e: Exception =>
        LOG.error(s"serviceConfig getFilenameList exception $e")
        JsonHelper.errorJson("mcc服务器连接失败")
    }
  }

  def getFileContent(appkey: String, env: Int, filename: String, groupID: String) = {
    val envDesc = Env(env).toString
    val configFile = new ConfigFile
    configFile.setFilename(filename)
    val configFiles = List(configFile).asJava

    val fileParamT = new file_param_t
    fileParamT.setAppkey(appkey)
    fileParamT.setEnv(envDesc)
    fileParamT.setConfigFiles(configFiles)
    fileParamT.setGroupId(groupID)
    try {
      val ret = mccSrv.getFileConfig(fileParamT)
      if (com.sankuai.octo.config.model.Constants.SUCCESS == ret.getErr && null != ret.getConfigFiles && 1 == ret.getConfigFiles.asScala.length) {
        val tmpList = ret.getConfigFiles.asScala
        val result = tmpList.head
        val md5String = Md5Helper.getMD532(result.getFilecontent)
        if (md5String == result.getMd5) {
          JsonHelper.dataJson(FileContentResponse(result.getFilename, result.getFilepath, result.getVersion, result.getMd5, new String(result.getFilecontent)))
        } else {
          JsonHelper.errorJson("文件Md5不一致，网络传输错误")
        }
      } else {
        LOG.error(s"mtConfigClient.getFileConfig ret is $ret")
        JsonHelper.errorJson("服务器错误，请重试")
      }
    } catch {
      case e: Exception =>
        LOG.error(s"serviceConfig getFileContent exception $e")
        JsonHelper.errorJson("服务器错误，请重试")
    }
  }

  private def distributeAndEnableConfigFile(appkey: String, env: Int, groupID: String, filename: String, hosts: List[String]) = {
    val dRet = distributeConfigFile(appkey, env, groupID, filename, hosts)
    val dSuccessList = if (com.sankuai.octo.config.model.Constants.SUCCESS == dRet.code) {
      hosts
    } else {
      hosts.filterNot(dRet.hosts.contains)
    }
    val dErrList = hosts.filterNot(dSuccessList.contains)

    val eRet = enableConfigFile(appkey, env, groupID, filename, dSuccessList)
    val eErrList = if (com.sankuai.octo.config.model.Constants.SUCCESS == eRet.code) {
      List[String]()
    } else {
      eRet.hosts.asScala.toList
    }

    val successList = hosts.filterNot(x => dErrList.contains(x) || eErrList.contains(x))

    // 调用mcc保存操作记录
    try{
      val username = getLoginName()
      val filelogRequest = new FilelogRequest()
      filelogRequest.setAppkey(appkey)
        .setGroupId(groupID)
        .setEnv(Env(env).toString)
        .setFilename(filename)
        .setUserName(username)
        .setMisId("")
        .setSuccessList(successList.asJava)
        .setDErrList(dErrList.asJava)
        .setEErrList(eErrList.asJava)
        .setType("FILE_DISTRIBUTE") //指明文件下发类型
      mccSrv.saveFilelog(filelogRequest)
      if (dErrList.isEmpty && eErrList.isEmpty) {
        JsonHelper.dataJson("文件下发且生效成功")
      } else {
        JsonHelper.errorDataJson(DistributeAndEnableResponse(
          convertIPsToDomainIPs(successList),
          convertIPsToDomainIPs(dErrList, dRet.codes),
          convertIPsToDomainIPs(eErrList, eRet.codes)))
      }
    } catch {
      case e: Exception =>
        LOG.error(s"serviceConfig distributeAndEnableConfigFile exception $e")
        JsonHelper.errorJson("服务器错误，请重试")
    }

  }

  private def distributeConfigFile(appkey: String, env: Int, groupID: String, filename: String, hosts: List[String]) = {
    val configFile = new ConfigFile
    configFile.setFilename(filename)

    val configFiles = List(configFile).asJava
    val fileParamT = new file_param_t
    val envDesc = Env(env).toString
    fileParamT.setAppkey(appkey)
    fileParamT.setEnv(envDesc)
    fileParamT.setConfigFiles(configFiles)
    fileParamT.setGroupId(groupID)

    val configFileRequest = new ConfigFileRequest
    configFileRequest.setFiles(fileParamT)
    configFileRequest.setHosts(hosts.asJava)
    try {
      mccSrv.distributeConfigFile(configFileRequest)
    } catch {
      case e: Exception =>
        LOG.error(s"serviceConfig distributeConfigFile exception", e)
        val req = new ConfigFileResponse(com.sankuai.octo.config.model.Constants.UNKNOW_ERROR)
        req.setHosts(new util.ArrayList[String]())
        req
    }
  }

  private def enableConfigFile(appkey: String, env: Int, groupID: String, filename: String, hosts: List[String]) = {
    val configFile = new ConfigFile
    configFile.setFilename(filename)
    val configFiles = List(configFile).asJava

    val fileParamT = new file_param_t
    val envDesc = Env(env).toString
    fileParamT.setAppkey(appkey)
    fileParamT.setEnv(envDesc)
    fileParamT.setConfigFiles(configFiles)

    val configFileRequest = new ConfigFileRequest
    configFileRequest.setFiles(fileParamT)
    configFileRequest.setHosts(hosts.asJava)
    try {
      mccSrv.enableConfigFile(configFileRequest)
    } catch {
      case e: Exception =>
        LOG.error(s"serviceConfig distributeConfigFile exception", e)
        val req = new ConfigFileResponse(com.sankuai.octo.config.model.Constants.UNKNOW_ERROR)
        req.setHosts(new util.ArrayList[String]())
        req
    }
  }

  def configFileCodeToMsg(ip: String, codes: util.Map[String, Integer]) = {
    if (null == codes || codes.isEmpty || !codes.containsKey(ip)) {
      ""
    } else {
      if (SGAENT_DISKFULL_ERROR_CODE == codes.get(ip)) {
        "写文件失败或磁盘空间不足"
      } else {
        "未知错误"
      }
    }
  }

  def convertIPsToDomainIPs(IPs: List[String], codes: util.Map[String, Integer] = new util.HashMap[String, Integer]()) = {
    IPs.map {
      x =>
        try {
          DomainIP(OpsService.ipToHost(x), x, "", configFileCodeToMsg(x, codes))
        } catch {
          case e: Exception =>
            LOG.info(s"getAgentProvider ${e.getMessage}")
            DomainIP(x, x)
        }
    }
  }

  /**
    *
    * @param appkey
    * @param env 1:test; 2:stage; 3:prod
    * @return IPs list<String> of the providers. [distinct]
    */
  def getProviderIPByAppkeyEnv(appkey: String, env: String): java.util.List[DomainIP] = {
    val ipList = AppkeyProviderService.providerNode(appkey, env).map(_.split(":").apply(0)).distinct.sorted
    ipList.map {
      x =>
        try {
          DomainIP(OpsService.ipToHost(x), x)
        } catch {
          case e: Exception =>
            LOG.info(s"getAgentProvider ${e.getMessage}")
            DomainIP(x, x)
        }
    }.asJava
  }

  def pushFile(json: String, appkey: String) = {
    Json.parse(json).validate[jsonPushAndEnableFile].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        distributeAndEnableConfigFile(appkey, x.env, x.groupID, x.fileName, x.IPs)
    })
  }

  def getFileConfigGroups(appkey: String, env: Int, page: Page) = {
    val response = getGroups(appkey, env)
    val groups: ConfigGroups = response.getGroups
    val list = if (null != groups) {
      groups.getGroups.asScala.filter(1 == _.state).map { x =>
        fileConfigGroupsItem(Option(x.id), x.appkey, Env.withName(x.env).id, x.name, Option(x.createTime), Option(x.updateTime), None, None, Option(x.version))
      }.sortBy(_.createTime)
    } else {
      List()
    }
    page.setTotalCount(list.length)
    list.slice(page.getStart, page.getStart + page.getPageSize)
  }

  def getAllProviderIPs(appkey: String, env: Int) = AppkeyProviderService.providerNode(appkey, env).map(_.split(":").apply(0)).distinct


  private def checkGroupNameAndIPs(list: List[ConfigGroup], item: fileConfigGroupsItem, isAdd: Boolean): Boolean = {
    //check IP isExist while adding or updating group
    val myList = if (isAdd) {
      list
    } else {
      list.filter(_.id != item.id.get)
    }
    val tempList = myList.map(_.ips.asScala.toList)
    val ipList = tempList.flatten.intersect(item.IPs.get)
    if (ipList.nonEmpty) {
      return false
    }

    //check name isExist while adding a new group
    if (isAdd && list.map(_.name).contains(item.groupName)) {
      return false
    }
    true
  }

  def saveGroup(appkey: String, json: String, isAdd: Boolean): Boolean = {
    Json.parse(json).validate[fileConfigGroupsItem].fold({ error =>
      LOG.info(error.toString)
    }, {
      x =>
        val groups = getGroups(appkey, x.env).getGroups
        val existList = groups.getGroups.asScala.filter(1 == _.state)
        val IPs = x.IPs.get
        if (!checkGroupNameAndIPs(existList.toList, x, isAdd)) return false
        val envDesc = Env(x.env).toString
        if (isAdd) {
          var addGroupRequest = new AddGroupRequest()
          addGroupRequest.setAppkey(appkey)
            .setEnv(envDesc)
            .setGroupName(x.groupName)
            .setIps(IPs.asJava)
            .setUsername(getLoginName())
          val response = mccSrv.addFileGroup(addGroupRequest);
          if (com.sankuai.octo.config.model.Constants.SUCCESS == response.getCode) return true
        } else {
          val ret = mccSrv.getGroupInfo(appkey, envDesc, x.id.get)
          if (com.sankuai.octo.config.model.Constants.SUCCESS != ret.code) {
            LOG.info("fail to get config group")
            return false
          } else {
            val updateGroupRequest = new UpdateGroupRequest(appkey, envDesc, x.id.get, IPs.asJava)
            updateGroupRequest.setVersion(x.version.getOrElse("0"))
              .setUserName(getLoginName())
            // 更新分组信息
            val response = mccSrv.updateFileGroup(updateGroupRequest)
            if (com.sankuai.octo.config.model.Constants.SUCCESS == response.getCode) return true
          }
        }

    })
    false
  }

  def delFileGroup(appkey: String, env: Int, groupID: String): Boolean = {
    val envDesc = Env(env).toString
    val deleteGroupRequest = new DeleteGroupRequest()
    deleteGroupRequest.setAppkey(appkey)
      .setEnv(envDesc)
      .setGroupId(groupID)
      .setUsername(getLoginName())
    0 == mccSrv.deleteFileGroup(deleteGroupRequest)
  }

  def getFileGroupInfo(appkey: String, env: Int, groupID: String): fileConfigGroupsItem = {
    val envDesc = Env(env).toString
    val ret = mccSrv.getGroupInfo(appkey, envDesc, groupID)
    if (com.sankuai.octo.config.model.Constants.SUCCESS != ret.code) {
      null
    } else {
      val group = ret.getGroup
      val id = Env.withName(group.env).id
      fileConfigGroupsItem(Option(group.id), group.appkey, id, group.name, Option(group.createTime), Option(group.updateTime), None, Option(group.ips.asScala.distinct.toList), Option(group.version))
    }
  }

  def getGroupsIPs(appkey: String, env: Int): List[String] = {
    val response = getGroups(appkey, env)
    val groups: ConfigGroups = response.getGroups
    if (null != groups) {
      groups.getGroups.asScala.filter(1 == _.state).flatMap(_.ips.asScala).distinct.toList
    } else {
      List[String]()
    }
  }

  def existGroupNames(appkey: String, env: Int) = {
    val response = getGroups(appkey, env)
    val groups: ConfigGroups = response.getGroups
    if (null != groups) {
      groups.getGroups.asScala.filter(1 == _.state).map(_.name).distinct
    } else {
      List[String]()
    }
  }

  def existGroupIPs(appkey: String, env: Int, groupID: String): List[String] = {
    val response = getGroups(appkey, env)
    val groups: ConfigGroups = response.getGroups
    if (null != groups) {
      groups.getGroups.asScala.filter {
        x =>
          (1 == x.state) && (x.id != groupID)
      }.flatMap(_.ips.asScala).distinct.toList
    } else {
      List[String]()
    }

  }

  def domainIPByGroupID(appkey: String, env: Int, groupID: String): List[DomainIP] = {
    val group = getFileGroupInfo(appkey, env, groupID)
    if (null != group) {
      val ips = group.IPs
      val idcMap = ProcessInfoUtil.getIdcInfo(ips.get.asJava)
      if (null != ips) {
        ips.get.map {
          x =>
            try {
              DomainIP(OpsService.ipToHost(x), x, idcMap.get(x).idc)
            } catch {
              case e: Exception =>
                DomainIP(x, x)
            }
        }
      } else {
        List[DomainIP]()
      }
    } else {
      List[DomainIP]()
    }
  }

  private def getGroups(appkey: String, env: Int) = {
    val envDesc = Env(env).toString
    val response = mccSrv.getGroups(appkey, envDesc)
    val errCode = response.code
    if (com.sankuai.octo.config.model.Constants.SUCCESS != response.getCode) {
      throw new TApplicationException(TApplicationException.MISSING_RESULT, s"errorCode is $errCode while calling getGroups in mtconfig")
    }
    response
  }

  def checkMCC() = {
    monitorExe.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        checkMCCApiStatus()
        checkMCCThriftStatus()
      }
    }, 10, 60, TimeUnit.SECONDS)

    val job = JobBuilder.newJob(classOf[DailyStatisticJob]).build()
    //每天凌晨三点执行一次
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 0)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  private def getMCCProdNodes = {
    val mnsc = MnsCacheClient.getInstance
    try {
      val mnscRet = mnsc.getMNSCache(Appkeys.mcc.toString, "", Env.prod.toString)
      val errorCode = mnscRet.getCode
      if (com.sankuai.octo.mnsc.idl.thrift.model.Constants.SUCCESS == errorCode) {
        mnscRet.getDefaultMNSCache.asScala.filter(_.getStatus != com.sankuai.sgagent.thrift.model.fb_status.STOPPED.getValue)
      } else {
        List[SGService]()
      }
    } catch {
      case e: Exception =>
        List[SGService]()
    }
  }

  private def hitMCCValue(node: SGService) = {
    var transport: TTransport = null
    val ip = node.ip
    val port = node.port
    val ipPort = node.ip + ":" + node.port
    try {
      val timeout = 3000
      transport = new TFramedTransport(new TSocket(node.ip, node.port, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val mcc = new com.sankuai.octo.config.service.MtConfigService.Client(protocol)
      transport.open()
      val request = new GetMergeDataRequest(Appkeys.mcc.toString, Env.prod.toString, "/", 0, CommonHelper.getLocalIp)
      mcc.getMergeData(request)
    } catch {
      case e: Exception =>
        LOG.error(e.getMessage, e)
        val msg = s"MCC check ($onlineOrOfflineStr)\n IP: $ip \n Port: $port \n 备注: 无法获取配置"
        sendMCCCheckAlarm(msg)
    } finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception =>
            LOG.error(s"$ipPort fail ${e.getMessage}")
        }
      }
    }
  }

  private def checkMCCThriftStatus() = getMCCProdNodes.foreach(hitMCCValue)

  private def checkMCCApiStatus() = {
    val request: MtHttpRequest = MtHttpRequest.builder.host(hostUrl).path("/api/monitor/alive").method("GET").build
    val httpClient: HttpClient = new HttpClient

    val preMsgp = s"MCC check ($onlineOrOfflineStr)\nURL: $hostUrl/api/monitor/alive \n"
    try {
      val response: MtHttpResponse = httpClient.executeRaw(request)
      val errorCode = response.getStatusCode
      if (errorCode != HttpStatus.SC_OK) {
        val msg = s"$preMsgp errorCode: $errorCode"
        sendMCCCheckAlarm(msg)
      }
    } catch {
      case e: Exception =>
        LOG.error(e.getMessage, e)
        val msg = s"$preMsgp 备注: 无法连接"
        sendMCCCheckAlarm(msg)
    }
  }

  private def sendMCCCheckAlarm(msg: String) = {
    val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
    xm.send(alarmUserList, s"$now \n $msg")
  }

  private def getStatisticFile() = {
    val timeout = Duration.create(300L, duration.SECONDS)
    val url = s"$hostUrl/api/statistics/filecfg"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  private def getStatisticDynamic() = {
    val timeout = Duration.create(300L, duration.SECONDS)
    val url = s"$hostUrl/api/statistics/dynamiccfg"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    content
  }

  def getStatisticData(isFile: Boolean) = {
    val tairData = TairClient.get(getMCCcheckKey(isFile)).getOrElse("")
    val json = if (StringUtils.isEmpty(tairData)) {
      val mccStatisticData = generateMCCStatisticData(isFile)
      if (StringUtils.isEmpty(mccStatisticData)) {
        JsonHelper.dataJson(List())
      } else {
        mccStatisticData
      }
    } else {
      tairData
    }
    val list = (Json.parse(json) \ "data").validate[Map[String, String]].fold({ error =>
      LOG.error(s"Failed to parse MCC statistic data $error")
      List[mccStatisticData]()
    }, {
      data =>
        data.map {
          item =>
            val appkey = item._1
            val businessLine = if (ZkClient.exist(s"${ServiceCommon.prodPath}/${appkey}/${Path.desc}")) {
              Business.getBusinessNameById(ServiceCommon.desc(appkey).business.get)
            } else {
              "N/A"
            }
            mccStatisticData(appkey, item._2, businessLine)
        }.toList
    })
    JsonHelper.dataJson(list)
  }

  private def getMCCcheckKey(isFile: Boolean) = if (isFile) {
    "MCCStatisticDataFile"
  } else {
    "MCCStatisticDataDynamic"
  }

  private def generateMCCStatisticData(isFile: Boolean) = {
    val key = getMCCcheckKey(isFile)
    val data = if (isFile) {
      getStatisticFile()
    } else {
      getStatisticDynamic()
    }
    TairClient.put(key, data)
    data
  }


  @DisallowConcurrentExecution
  class DailyStatisticJob extends Job {
    @throws(classOf[JobExecutionException])
    override def execute(ctx: JobExecutionContext): Unit = {
      generateMCCStatisticData(true)
      generateMCCStatisticData(false)
    }
  }


  private def getWaimainSgconfigZK(env: Int, isWaimai: Boolean) = if (CommonHelper.isOffline) {
    //offline
    if (isWaimai) {
      env match {
        case 3 => if (waimaiProdSgZk.isEmpty) {
          waimaiProdSgZk = getZkClient(waimaiProdZkUrl)
        }
          waimaiProdSgZk.get
        case 2 =>
          if (waimaiStageSgZk.isEmpty) {
            waimaiStageSgZk = getZkClient(waimaiStageZkUrl)
          }
          waimaiStageSgZk.get
        case 1 =>
          if (waimaiTestSgZk.isEmpty) {
            waimaiTestSgZk = getZkClient(waimaiTestZkUrl)
          }
          waimaiTestSgZk.get
      }
    } else {
      getMccZk()
    }

  } else {
    //online
    if (sgZk.isEmpty) {
      sgZk = getZkClient(sgZkUrl)
    }
    sgZk.get
  }


  private def getMccZk() = if (CommonHelper.isOffline) {
    if (offlineMccZk.isEmpty) {
      offlineMccZk = getZkClient(offlineMccZkUrl)
    }
    offlineMccZk.get
  } else {
    if (onlineMccZk.isEmpty) {
      onlineMccZk = getZkClient(onlineMccZkUrl)
    }
    onlineMccZk.get
  }

  private def getZkClient(url: String) = {
    val superClient = CuratorFrameworkFactory.builder.connectString(url).retryPolicy(new RetryUntilElapsed(3000, 2000)).build()
    superClient.start()
    Some(superClient)
  }

  private def pathExist(env: Int, path: String, isWaimai: Boolean): Boolean = {
    getWaimainSgconfigZK(env, isWaimai).checkExists().forPath(path) != null
  }

  private def getZkData(env: Int, path: String, isWaimai: Boolean) = {
    val zk = getWaimainSgconfigZK(env, isWaimai)
    if (pathExist(env, path, isWaimai)) {
      val data = zk.getData.forPath(path)
      if (data == null) "" else new String(data, "utf-8")
    } else {
      ""
    }
  }

  private def getSgconfigNewData(env: Int, path: String, isWaimai: Boolean) = {
    val zk = getWaimainSgconfigZK(env, isWaimai)
    val children = zk.getChildren.forPath(path)
    children.asScala.map {
      key =>
        val value = getZkData(env, s"$path/$key", isWaimai)
        (key.trim -> value.trim)
    }.toMap
  }

  private def sgconfigData2Map(data: String) = {
    val lines = data.split("\n")
    lines.filter(x => (x.split("=", 2).length == 2) && (!x.startsWith("#"))).map {
      line =>
        val keyValue = line.split("=", 2)
        (keyValue(0).trim -> keyValue(1).trim)
    }.toMap
  }

  private def getSgconfigData(env: Int, app: String, isWaimai: Boolean) = {
    val myNewAppPath = s"$new_data_base_path/$app/config"
    val oldGlobalDataStr = getZkData(env, old_global_data_path, isWaimai)
    val oldGLobalDataMap = sgconfigData2Map(oldGlobalDataStr)
    val newGlobalDataMap = getSgconfigNewData(env, new_global_data_path, isWaimai)

    val oldAppDataStr = getZkData(env, s"$old_data_base_path/$app/cfgs", isWaimai)
    val oldAppDataMap = sgconfigData2Map(oldAppDataStr)
    val newAppDataMap = getSgconfigNewData(env, myNewAppPath, isWaimai)


    var globalMap = oldGLobalDataMap
    newGlobalDataMap.foreach(globalMap += _)


    var appMap = oldAppDataMap
    newAppDataMap.foreach(appMap += _)

    appMap.foreach(globalMap += _)
    globalMap.filter(_._1.matches("[a-zA-Z0-9_.-]*")).map(x => s"${x._1} = ${x._2}").mkString("\n")
  }

  def getWaimaiSgconfig(app: String, env: Int, isWaimai: Boolean) = {
    val myNewAppPath = s"$new_data_base_path/$app"
    if (pathExist(env, myNewAppPath, isWaimai)) {
      val appStr = getSgconfigData(env, app, isWaimai)
      JsonHelper.dataJson(appStr)
    } else {
      JsonHelper.errorJson(s"$app 不存在")
    }
  }

  private def handleSgconfigMigration(appkey: String, env: Int, data: String, app: String) = {
    val envStr = Env(env).toString
    val mccPath = s"/config/$appkey/$envStr"
    val mccZk = getMccZk()
    try {
      mccZk.setData().forPath(mccPath, data.getBytes("utf-8"))
      BorpClient.saveOpt(actionType = ActionType.UPDATE.getIndex, entityId = appkey, entityType = EntityType.sgconfigMigration, newValue = s"sgconfig app: $app\n MCC env: $envStr\n")
      JsonHelper.dataJson(data)
    } catch {
      case e: Exception =>
        JsonHelper.errorJson("导入失败")
    }
  }

  def waimaiSgconfigMigration(appkey: String, app: String, env: Int, isWaimai: Boolean) = {
    val myNewAppPath = s"$new_data_base_path/$app"
    if (pathExist(env, myNewAppPath, isWaimai)) {
      val appStr = getSgconfigData(env, app, isWaimai)
      handleSgconfigMigration(appkey, env, appStr, app)
    } else {
      JsonHelper.errorJson(s"$app 不存在")
    }
  }

  def createPR(json: String) = {
    Json.parse(json).validate[MCCPullRequest].fold({ error =>
      JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { pr =>

      val detailList = pr.data.map {
        entry =>
          val prItem = new PRDetail()
          prItem.setKey(entry.key)
          prItem.setNewValue(entry.value)
          prItem.setNewComment(entry.comment)
          prItem.setIsDeleted(entry.isDeleted)
          prItem
      }
      val prReq = new PullRequest()
      prReq.setAppkey(pr.nodename).setEnv(pr.env).setNote(pr.note).setPrMisID(UserUtils.getUser.getLogin)
      try {
        val ret = mccSrv.createPR(prReq, detailList.asJava)
        if (ret) JsonHelper.dataJson("PR提交成功") else JsonHelper.errorJson("PR提交失败")
      } catch {
        case e: Exception =>
          JsonHelper.errorJson("PR提交失败")
      }
    })
  }

  case class MCCUpdate(PrID: Long, data: List[MCCUpdateEntry])

  case class MCCUpdateEntry(PrDetailID: Long, key: String,NewValue: String, NewComment: String,isDeleted: Boolean)

  implicit val jsonMCCUpdateEntryReads = Json.reads[MCCUpdateEntry]
  implicit val jsonMCCUpdatetEntryWrites = Json.writes[MCCUpdateEntry]

  implicit val jsonMCCUpdateReads = Json.reads[MCCUpdate]
  implicit val jsonMCCUpdateWrites = Json.writes[MCCUpdate]


  def updatePRDetail(json: String,appkey: String) = {

    Json.parse(json).validate[MCCUpdate].fold({ error =>
      JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { pr =>

      val detailList = pr.data.map {
        entry =>
          val prItem = new PRDetail()
          prItem.setPrID(pr.PrID)
          prItem.setKey(entry.key)
          prItem.setPrDetailID(entry.PrDetailID)
          prItem.setNewValue(entry.NewValue)
          prItem.setNewComment(entry.NewComment)
          prItem.setIsDeleted(entry.isDeleted)
          prItem
      }
      try {
        if(detailList.isEmpty) {
          JsonHelper.errorJson("已经是最后一条记录，无法删除。如需继续删除，请点击decline撤销本次PR。")
        }else{
          val list = mccSrv.getPRDetail(pr.PrID)
          val ret = mccSrv.updatePRDetail(pr.PrID,detailList.asJava)
          if (ret) {
            XMUtil.sendMessage(pr.PrID + "",appkey,detailList.asJava,list)
            JsonHelper.dataJson("PR修改成功")
          } else {
            JsonHelper.errorJson("PR修改失败")
          }
        }
      } catch {
        case e: Exception =>
          LOG.error("PR修改失败:" + e.getStackTrace)
          JsonHelper.errorJson("PR修改失败")
      }})
  }

  def getPR(appkey: String, env: Int, status: Int, page: Page) = {
    val list = mccSrv.getPullRequest(appkey, env, status).asScala.sortBy(-_.prTime)
    val ret = if (isServiceOwner(appkey)) {
      list
    }
    else {
      val misID = UserUtils.getUser.getLogin
      list.filter(_.prMisID == misID)
    }
    page.setTotalCount(ret.length)
    ret.slice(page.getStart, page.getStart + page.getPageSize)
  }

  private def isServiceOwner(appkey: String) = {
    val misID = UserUtils.getUser.getLogin
    val desc = ServiceCommon.desc(appkey)
    desc.owners.foldLeft(true) {
      (result, item) =>
        result || item.login.equals(misID)
    }
  }

  private def getPRDetail(prID: Int) = {
    mccSrv.getPRDetail(prID)
  }

  private def getReview(prID: Int) = mccSrv.getReview(prID)

  def getPRDetailAndReview(prID: Int) = {

    val review = getReview(prID)
    val pr = mccSrv.getPR(prID)
    val canMerge = review.asScala.foldLeft(false) { (ret, item) => ret || (item.approve == 1) }
    Map("detail" -> getPRDetail(prID), "review" -> review, "pr" -> pr, "canMerge" -> canMerge)
  }

  def isPrAuthor(prID: Int) = {
    val pr = mccSrv.getPR(prID)
    pr.getPrMisID == getLoginName()
  }

  def createReview(json: String): String  = {
    Json.parse(json).validate[ReviewRequest].fold({ error =>
      JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { review =>
      val reviewer = UserUtils.getUser.getLogin
      //是否是ap操作
      if (1 == review.approve) {
        val pr = mccSrv.getPR(review.prID)
        if (null == pr || null == pr.getPrMisID) {
          JsonHelper.errorJson("PR获取失败")
        } else if (reviewer.equals(pr.getPrMisID)) {
          JsonHelper.errorJson("不能approve自己发起的PR，请联系其他服务负责人review!")
        } else {
          handleCreateReview(review)
        }
      } else {
        handleCreateReview(review)
      }
    })
  }

  def handleCreateReview(review: ReviewRequest): String = {
    val rRq = new Review()
    rRq.setNote(review.note)
      .setPrID(review.prID)
      .setReviewerMisID(UserUtils.getUser.getLogin)
      .setApprove(review.approve)
    val ret = mccSrv.createReview(rRq)
    if (ret) JsonHelper.dataJson("PR提交成功") else JsonHelper.errorJson("PR提交失败")
  }

  def delPR(prID: Int) = mccSrv.detelePR(prID)

  def merge(prID: Int) = {
    val req = new MergeRequest()
    req.setUsername(getLoginName())
      .setPrID(prID)
    mccSrv.mergePullRequest(req)
  }

  def reopenPR(appkey: String, env: Int, prID: Int) = {
    val list = mccSrv.getPullRequest(appkey, env, -1)
    val item = list.asScala.filter(_.prID == prID).head
    item.setStatus(0)
    mccSrv.updatePR(item)
    true
  }

  def syncDynamicCfg2Prod(appkey: String, nodeName: String, cookies: Array[Cookie]): String = {
    val prodNodeName = appkey + ".prod"
    val stageNodeDataResult = getNodeData(appkey, nodeName, cookies)
    val stageNodeDataJsonResult = JSON.parseObject(stageNodeDataResult)
    val prodNodeDataResult = getNodeData(appkey, prodNodeName, cookies)
    val prodNodeDataJsonResult = JSON.parseObject(prodNodeDataResult)

    if (!"success".equals(stageNodeDataJsonResult.getString("status"))) {
      return JsonHelper.errorJson("获取Stage动态配置失败, " + stageNodeDataJsonResult.getString("msg"))
    }
    if (!"success".equals(prodNodeDataJsonResult.getString("status"))) {
      return JsonHelper.errorJson("获取Prod动态配置失败, " + prodNodeDataJsonResult.getString("msg"))
    }
    // 同步至Prod操作
    val updateNodeDataUrl = s"$hostUrl/config2/temp/space/$appkey/node/update"

    val stageNodeDataArray = stageNodeDataJsonResult.getJSONObject("data").getJSONArray("data")
    val prodNodeDataArray = prodNodeDataJsonResult.getJSONObject("data").getJSONArray("data")
    val newProdNodeDataArray = mergeNodeData(stageNodeDataArray, prodNodeDataArray)

    val prodConfigItem = ConfigItemEdit(prodNodeName, newProdNodeDataArray.toString, appkey,
      prodNodeDataJsonResult.getJSONObject("data").getString("version"))

    val jsonParam = JsonHelper.jsonStr(prodConfigItem)
    val postReq = addHeaderAndCookie(updateNodeDataUrl, cookies).POST << jsonParam
    val feature = Http(postReq > as.String)
    val content = Await.result(feature, timeout)
    return content
  }

  private def mergeNodeData(stageNodeArray: JSONArray, prodNodeArray: JSONArray): JSONArray = {
    val newJSONArray = new JSONArray()
    val stageNodeKeyObjMap = getNodeKeyObjMap(stageNodeArray)
    val prodNodeKeyObjMap = getNodeKeyObjMap(prodNodeArray)
    val mergeMap = prodNodeKeyObjMap ++ stageNodeKeyObjMap // stage覆盖prod同key
    mergeMap.keys.foreach { i =>
      newJSONArray.add(mergeMap(i))
    }
    newJSONArray
  }

  private def getNodeKeyObjMap(jsonArray: JSONArray): Map[String, JSONObject] = {
    (0 to jsonArray.size() - 1).map {
      i =>
        val jsonObj = jsonArray.getJSONObject(i)
        val key = jsonObj.getString("key")
        (key -> jsonObj)
    }.toMap
  }

  def getEnvByNodename(nodename: String, appkey: String): String = {
    if (nodename.length < appkey.length + 1) {
      null
    } else {
      val path = nodename.substring(appkey.length + 1)
      if (StringUtils.isNotEmpty(path)) {
        path.split("\\.")(0)
      } else {
        null
      }
    }


  }

}

