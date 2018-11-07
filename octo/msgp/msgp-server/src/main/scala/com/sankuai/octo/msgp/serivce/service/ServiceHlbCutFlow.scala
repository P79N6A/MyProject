package com.sankuai.octo.msgp.serivce.service

import com.sankuai.msgp.common.utils.HttpUtil
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

import scala.collection.JavaConverters._

/**
 * Created by zhoufeng on 17/2/24.
 */
object serviceHlbCutFlow {
  val LOG: Logger = LoggerFactory.getLogger(ServiceHlbUpstream.getClass)
  case class OneHttpCutFlowRecord(serverName: String, methodUrl: String, closurePercent: String, closureStatus: String)
  implicit val OneHttpCutFlowRecordRead = Json.reads[OneHttpCutFlowRecord]
  implicit val OneHttpCutFlowRecordWrite = Json.writes[OneHttpCutFlowRecord]

  case class HttpCutFlowConf(closureList: List[OneHttpCutFlowRecord])
  implicit val HttpCutFlowConfRead = Json.reads[HttpCutFlowConf]
  implicit val HttpCutFlowConfWrite = Json.writes[HttpCutFlowConf]

  case class HttpUrlRecord(serverName: String, methodUrl: String)
  implicit val HttpUrlRecordRead = Json.reads[HttpUrlRecord]
  implicit val HttpUrlRecordWrite = Json.writes[HttpUrlRecord]

  case class HttpUrlForCut(urlList: List[HttpUrlRecord])
  implicit val HttpUrlForCutRead = Json.reads[HttpUrlForCut]
  implicit val HttpUrlForCutWrite = Json.writes[HttpUrlForCut]


  def getPath(appkey: String, env: String) = "/mns/sankuai/" + env + "/" + appkey + "/http/http-cutflow"
  def getUrlPath(appkey:String, env: String) = "/mns/sankuai/" + env + "/" + appkey + "/http/http-url"

  def getHttpCutFlow(appkey: String, env: String) = {
    val path = getPath(appkey, env)
    var cutList: List[OneHttpCutFlowRecord] = List()
    if(ZkClient.exist(path)) {
      Json.parse(ZkClient.getData(path)).validate[HttpCutFlowConf].fold({
        er => LOG.info(er.toString)
          JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
      }, {
        cutFlowList =>
          cutList = cutFlowList.closureList
      })
    }
    cutList.asJava
  }
  def addHttpCutFlow(appkey: String, env: String, json: String) = {
    val path = getPath(appkey, env)
    var newConfList: List[OneHttpCutFlowRecord] = List()
    var resultCode = 200
    Json.parse(json).validate[OneHttpCutFlowRecord].fold({
      er => LOG.info(er.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
    }, {
      cutFlowNode =>
        if(cutFlowNode.closureStatus.equals("0")) {
          var cutData: Map[String, String] = Map()
          var postUrl = "http://admin.hlb.inf.test.sankuai.com/degrade/admin/?action=set"
          if(!CommonHelper.isOffline) {
            if(env.equals("prod")) {
              postUrl = "http://admin.hlb.inf.sankuai.com/degrade/admin/?action=set"
            } else {
              postUrl = "http://admin.hlb.inf.st.sankuai.com/degrade/admin/?action=set"
            }
          }

          cutData += ("deg_servername" -> cutFlowNode.serverName)
          cutData += ("deg_location" -> cutFlowNode.methodUrl)
          cutData += ("deg_percent" -> cutFlowNode.closurePercent)
          resultCode = HttpUtil.httpPostRequestByBody(postUrl, JsonHelper.jsonStr(cutData))
          if(200 != resultCode) {
            LOG.error("[addHttpCutFlowServer] add failed")
          }
        }
        if(200 == resultCode) {
          if(ZkClient.exist(path)){
            Json.parse(ZkClient.getData(path)).validate[HttpCutFlowConf].fold({
              er => LOG.info(er.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
            }, {
              cutFlowConf =>
                newConfList = cutFlowNode :: cutFlowConf.closureList
                val newConf = new HttpCutFlowConf(newConfList)
                ZkClient.setData(path, JsonHelper.jsonStr(newConf))
            })
          } else {
            newConfList = cutFlowNode :: newConfList
            val newConf = new HttpCutFlowConf(newConfList)
            ZkClient.create(path, JsonHelper.jsonStr(newConf))
          }
        }
    })

    getHttpCutFlow(appkey, env)
  }
  def delHttpCutFlow(appkey: String, env: String, json: String) = {
    val path = getPath(appkey, env)
    var cutList: List[OneHttpCutFlowRecord] = List()
    var resultCode = 200
    if(ZkClient.exist(path)) {
      Json.parse(json).validate[OneHttpCutFlowRecord].fold({
        er => LOG.info(er.toString)
          JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
      }, {
        toBeDeleted =>
          if(toBeDeleted.closureStatus.equals("0")) {
            var cutData: Map[String, String] = Map()
            var postUrl = "http://admin.hlb.inf.test.sankuai.com/degrade/admin/?action=del"
            if(!CommonHelper.isOffline) {
              if(env.equals("prod")) {
                postUrl = "http://admin.hlb.inf.sankuai.com/degrade/admin/?action=del"
              } else {
                postUrl = "http://admin.hlb.inf.st.sankuai.com/degrade/admin/?action=del"
              }
            }
            cutData += ("deg_servername" -> toBeDeleted.serverName)
            cutData += ("deg_location" -> toBeDeleted.methodUrl)
            resultCode = HttpUtil.httpPostRequestByBody(postUrl, JsonHelper.jsonStr(cutData))
            if(200 != resultCode) {
              LOG.error("[delHttpCutFlowServer] delete failed")
            }
          }
          if(200 == resultCode) {
            Json.parse(ZkClient.getData(path)).validate[HttpCutFlowConf].fold({
              er => LOG.info(er.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
            }, {
              cutFlowList =>
                cutList = cutFlowList.closureList
                cutList = cutList.filter(x=> !(x.serverName.equals(toBeDeleted.serverName) && x.methodUrl.equals(toBeDeleted.methodUrl)))
            })
            val newConf = new HttpCutFlowConf(cutList)
            ZkClient.setData(path, JsonHelper.jsonStr(newConf))
          }
      })
    }
    getHttpCutFlow(appkey, env)
  }
  def upsHttpCutFlow(appkey: String, env: String, json: String) = {
    val path = getPath(appkey, env)
    var cutList: List[OneHttpCutFlowRecord] = List()
    var resultCode = 200
    if(ZkClient.exist(path)) {
      Json.parse(json).validate[OneHttpCutFlowRecord].fold({
        er => LOG.info(er.toString)
          JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
      }, {
        toBeUpdated =>
          if(toBeUpdated.closureStatus.equals("0")) {
            var cutData: Map[String, String] = Map()
            var postUrl = "http://admin.hlb.inf.test.sankuai.com/degrade/admin/?action=set"
            if(!CommonHelper.isOffline) {
              if(env.equals("prod")) {
                postUrl = "http://admin.hlb.inf.sankuai.com/degrade/admin/?action=set"
              } else {
                postUrl = "http://admin.hlb.inf.st.sankuai.com/degrade/admin/?action=set"
              }
            }
            cutData += ("deg_servername" -> toBeUpdated.serverName)
            cutData += ("deg_location" -> toBeUpdated.methodUrl)
            cutData += ("deg_percent" -> toBeUpdated.closurePercent)
            resultCode = HttpUtil.httpPostRequestByBody(postUrl, JsonHelper.jsonStr(cutData))
            if(200 != resultCode) {
              LOG.error("[upsHttpCutFlowServer] update failed")
            }
          }
          if(200 == resultCode) {
            Json.parse(ZkClient.getData(path)).validate[HttpCutFlowConf].fold({
              er => LOG.info(er.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
            }, {
              cutFlowList =>
                cutList = cutFlowList.closureList
                cutList.find(x => (x.serverName.equals(toBeUpdated.serverName) && x.methodUrl.equals(toBeUpdated.methodUrl))) match {
                  case Some(toBeShutCut) =>
                    if(toBeUpdated.closureStatus.equals("1") && toBeShutCut.closureStatus.equals("0")) {
                      var delData: Map[String, String] = Map()
                      var postUrl = "http://admin.hlb.inf.test.sankuai.com/degrade/admin/?action=del"
                      if(!CommonHelper.isOffline) {
                        if(env.equals("prod")) {
                          postUrl = "http://admin.hlb.inf.sankuai.com/degrade/admin/?action=del"
                        } else {
                          postUrl = "http://admin.hlb.inf.st.sankuai.com/degrade/admin/?action=del"
                        }
                      }
                      delData += ("deg_servername" -> toBeUpdated.serverName)
                      delData += ("deg_location" -> toBeUpdated.methodUrl)
                      resultCode = HttpUtil.httpPostRequestByBody(postUrl, JsonHelper.jsonStr(delData))
                      if(200 != resultCode) {
                        LOG.error("[delHttpCutFlowServer] delete failed")
                      }
                    }
                  case None =>
                }
                if(200 == resultCode) {
                  cutList = cutList.filter(x=> !(x.serverName.equals(toBeUpdated.serverName) && x.methodUrl.equals(toBeUpdated.methodUrl)))
                  cutList = toBeUpdated :: cutList
                }
            })
            val newConf = new HttpCutFlowConf(cutList)
            ZkClient.setData(path, JsonHelper.jsonStr(newConf))
          }
      })
    }
    getHttpCutFlow(appkey, env)
  }


  def getHttpUrl(appkey: String, env: String) = {
    val path = getUrlPath(appkey, env)
    var urlList: List[HttpUrlRecord] = List()
    if(ZkClient.exist(path)) {
      Json.parse(ZkClient.getData(path)).validate[HttpUrlForCut].fold({
        er => LOG.info(er.toString)
          JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
      }, {
        httpUrlList =>
          urlList = httpUrlList.urlList
      })
    }
    urlList.asJava
  }

  def addHttpUrl(appkey: String, env: String, json: String) = {
    val path = getUrlPath(appkey, env)
    var newUrlList: List[HttpUrlRecord] = List()
    Json.parse(json).validate[HttpUrlRecord].fold({
      er => LOG.info(er.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
    }, {
      urlNode =>
        if(ZkClient.exist(path)){
          Json.parse(ZkClient.getData(path)).validate[HttpUrlForCut].fold({
            er => LOG.info(er.toString)
              JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
          }, {
            curHttpUrl =>
              newUrlList = urlNode :: curHttpUrl.urlList
              val newUrlConf = new HttpUrlForCut(newUrlList)
              ZkClient.setData(path, JsonHelper.jsonStr(newUrlConf))
          })
        } else {
          newUrlList = urlNode :: newUrlList
          val newUrlConf = new HttpUrlForCut(newUrlList)
          ZkClient.create(path, JsonHelper.jsonStr(newUrlConf))
        }
    })
    getHttpUrl(appkey, env)
  }

  def delHttpUrl(appkey: String, env: String, json: String) = {
    val path = getUrlPath(appkey, env)
    var newUrlConf: List[HttpUrlRecord] = List()
    if(ZkClient.exist(path)) {
      Json.parse(json).validate[HttpUrlRecord].fold({
        er => LOG.info(er.toString)
          JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
      }, {
        toBeDeleted =>
          Json.parse(ZkClient.getData(path)).validate[HttpUrlForCut].fold({
            er => LOG.info(er.toString)
              JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
          }, {
            curUrlConf =>
              newUrlConf = curUrlConf.urlList
              newUrlConf = newUrlConf.filter(x=> !(x.serverName.equals(toBeDeleted.serverName) && x.methodUrl.equals(toBeDeleted.methodUrl)))

          })
          val newConf = new HttpUrlForCut(newUrlConf)
          ZkClient.setData(path, JsonHelper.jsonStr(newConf))
      })
    }
    getHttpUrl(appkey, env)
  }

  def getHttpApkServerNameList(appkey: String, env: String) = {
    val path = getUrlPath(appkey, env)
    var serNameList: List[String] = List()
    if(ZkClient.exist(path)) {
      Json.parse(ZkClient.getData(path)).validate[HttpUrlForCut].fold({
        er => LOG.info(er.toString)
          JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
      }, {
        httpUrlConf =>
            httpUrlConf.urlList.foreach {
              x =>
                serNameList = x.serverName :: serNameList
            }
      })
    }
    serNameList.distinct.asJava
  }

  def getHttpUrlByServerName(appkey: String, env: String, serverName: String) = {
    val path = getUrlPath(appkey, env)
    var urlList: List[String] = List()
    if(ZkClient.exist(path)) {
      Json.parse(ZkClient.getData(path)).validate[HttpUrlForCut].fold({
        er => LOG.info(er.toString)
          JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
      }, {
        httpUrlConf =>
          httpUrlConf.urlList.foreach {
            x =>
              if(x.serverName.equals(serverName)) {
                urlList = x.methodUrl :: urlList
              }
          }
      })
    }
    urlList.asJava
  }

}
