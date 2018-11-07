package com.sankuai.octo.msgp.serivce.service

import java.text.SimpleDateFormat
import java.util
import java.util.{Date, TimeZone}
import java.util.concurrent.TimeUnit

import com.sankuai.meituan.auth.vo.User
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.HttpUtil
import com.sankuai.msgp.common.utils.helper.{CommonHelper, JsonHelper}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

import scala.concurrent.duration.Duration


/**
  * Created by xintao on 2017/3/10.
  */
case class SGConfig(id: Long, appkey: String, idc: String, env: Int, userLogin: String, status: Int,
                    maximumInstance: Int, minimumInstance: Int, desireInstance: Int, cooldown: Int,
                    created: Long, runtime: Long, cpu: Int, mem: Int, hd: Int, followers: Seq[Map[String, String]],
                    defaultImageId: Option[Long], healthCheckFlag: Option[Int], imagetags: Seq[Map[String, String]], fakeFlag: Option[Int]) {
}

object SGConfig {
  implicit val reader = Json.reads[SGConfig]
  implicit val writer = Json.writes[SGConfig]
}

case class MapMeta(key: String, value: String)

object MapMeta {
  implicit val reader = Json.reads[MapMeta]
  implicit val writer = Json.writes[MapMeta]
}

case class ServiceData(intro: String, owt: String, pdl: String, owners: List[String], observers: Option[List[String]], tags: Option[String])

object ServiceData {
  implicit val reader = Json.reads[ServiceData]
  implicit val writer = Json.writes[ServiceData]
}

case class ScalingGroupData(appkey: String, env: String, zone: String, serviceType: Int, serviceData: Option[ServiceData], maxInstanceNum: Int, userLogin: String,
                            cpu: Int, mem: Int, hd: Int, healthCheckFlag: Option[Int], cooldown: Option[Int], minInstanceNum: Option[Int])

object ScalingGroupData {
  implicit val reader = Json.reads[ScalingGroupData]
  implicit val writer = Json.writes[ScalingGroupData]
}

case class ScalingGroupResponse(sgId: Long, errorInfo: Option[String])

object ScalingGroupResponse {
  implicit val reader = Json.reads[ScalingGroupResponse]
  implicit val writer = Json.writes[ScalingGroupResponse]
}

case class ScaleOutResponse(rescode: Option[Long] = None, err: Option[String] = None)

object ScaleOutResponse {
  implicit val reader = Json.reads[ScaleOutResponse]
  implicit val writer = Json.writes[ScaleOutResponse]
}

case class ScaleInResponse(ok: Option[String] = None, err: Option[String] = None)

object ScaleInResponse {
  implicit val reader = Json.reads[ScaleInResponse]
  implicit val writer = Json.writes[ScaleInResponse]
}

case class HULKScheduleConfig(startDay: Int, endDay: Int, startTime: Int, endTime: Int)

object HULKScheduleConfig {
  implicit val writer = Json.writes[HULKScheduleConfig]
  implicit val reader = Json.reads[HULKScheduleConfig]
}

case class HULKMonitorConfig(gteType: Int, mType: Int, value: Double, spanName: String, monitorValueLower: Double, enableNonScalein: Int, startTime: Int, endTime: Int)

object HULKMonitorConfig {
  implicit val writer = Json.writes[HULKMonitorConfig]
  implicit val reader = Json.reads[HULKMonitorConfig]
}

case class SPConfig(id: Long, appkey: String, env: Int, idcs: List[String], pType: Int, schedule: HULKScheduleConfig,
                    monitor: HULKMonitorConfig, state: Int, esgNum: Int, esgNumScaleIn: Int, fakeFlag: Option[Int])

object SPConfig {
  implicit val writer = Json.writes[SPConfig]
  implicit val reader = Json.reads[SPConfig]
}

case class ScaleOutConfig(appkey: String, env: Seq[String], zone: Seq[String], num: Int, user: Option[String])

object ScaleOutConfig {
  implicit val writer = Json.writes[ScaleOutConfig]
  implicit val reader = Json.reads[ScaleOutConfig]
}

case class ScaleInConfig(appkey: String, instanceIps: Seq[String], user: Option[String])

object ScaleInConfig {
  implicit val writer = Json.writes[ScaleInConfig]
  implicit val reader = Json.reads[ScaleInConfig]
}

case class ScalingPolicyResponse(spId: Long, errorInfo: Option[String])

object ScalingPolicyResponse {
  implicit val reader = Json.reads[ScalingPolicyResponse]
  implicit val writer = Json.writes[ScalingPolicyResponse]
}

case class HULKScalingRecord(id: Long, appkey: String, env: Int, idc: String, eventType: Int, scaleNum: Int, detail: String, time: Long)

object HULKScalingRecord {
  implicit val reader = Json.reads[HULKScalingRecord]
  implicit val writer = Json.writes[HULKScalingRecord]
}

case class SpAndSgConfigInfo(policyRow: SPConfig, groupRow: SGConfig)

object SpAndSgConfigInfo {
  implicit val reader = Json.reads[SpAndSgConfigInfo]
  implicit val writer = Json.writes[SpAndSgConfigInfo]
}

case class MaxQpsInfo(rescode: Int, msg: String, qpsMax: Double)

object MaxQpsInfo {
  implicit val reader = Json.reads[MaxQpsInfo]
  implicit val writer = Json.writes[MaxQpsInfo]
}

case class ScalingGroupAndRunningSetInfo(sgRowId: Long, appkey: String, idc: String, env: Int, setId: Long, namePrefix: String = "", nameId: Long = 0L, status: Int = 0, ip: Option[String] = None)

object ScalingGroupAndRunningSetInfo {
  implicit val reader = Json.reads[ScalingGroupAndRunningSetInfo]
  implicit val writer = Json.writes[ScalingGroupAndRunningSetInfo]
}

case class ImageInfo(id: Long, compileId: Long, imageName: String, commitHash: String, compileTime: Long, userLogin: String, tags: Option[Seq[String]]) {
}

object ImageInfo {
  implicit val writer = Json.writes[ImageInfo]
  implicit val reader = Json.reads[ImageInfo]
}


object serviceHulkPolicy {
  private implicit val timeout = Duration.create(30L, TimeUnit.SECONDS)

  val idcEngToChaMap = Map("mos" -> "办公云", "dx" -> "大兴", "yf" -> "永丰", "gh" -> "光环", "cq" -> "次渠", "gq" -> "上海桂桥", "yp" -> "月浦","xh" -> "上海徐汇")
  val idcChaToEngMap = Map("办公云" -> "mos", "大兴" -> "dx", "永丰" -> "yf", "光环" -> "gh", "次渠" -> "cq", "上海桂桥" -> "gq", "月浦" -> "yp","上海徐汇" -> "xh")
  val envIntToChaMap = if (CommonHelper.isOffline) {
    Map("3" -> "dev", "2" -> "beta", "1" -> "test")
  } else {
    Map("3" -> "prod", "2" -> "stage", "1" -> "test")
  }

  val LOG: Logger = LoggerFactory.getLogger(serviceHulkPolicy.getClass)

  val hulkURL = if (CommonHelper.isOffline) {
    MsgpConfig.get("hulk-url", "http://hulk.test.sankuai.com/")
    //    MsgpConfig.get("hulk-url", "http://10.21.200.162:8080/")
  } else {
    MsgpConfig.get("hulk-url", "http://hulk.sankuai.com/")
  }

  val idcStr = if (CommonHelper.isOffline) {
    MsgpConfig.get("hulk-idc-str", "mos")
  } else {
    MsgpConfig.get("hulk-idc-str", "dx,yf,cq,gh,gq,yp")
  }

  val fakeIdcList = if (CommonHelper.isOffline) {
    MsgpConfig.get("hulk-fake-idc-str", "mos").split(",").map(_.trim).filter(_.length > 0).toList
  } else {
    MsgpConfig.get("hulk-fake-idc-str", "dx,yf,gh").split(",").map(_.trim).filter(_.length > 0).toList
  }

  def getIDCInfo = {
    if (CommonHelper.isOffline) {
      List(MapMeta("mos", "办公云"))
    } else {
      List(MapMeta("dx", "大兴"), MapMeta("yf", "永丰"), MapMeta("gh", "光环"), MapMeta("cq", "次渠"), MapMeta("gq", "上海桂桥"), MapMeta("yp", "月浦"))
    }
  }

  def getScalingGroup(appkey: String, env: Int) = {
    try {
      val body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/scalinggroup/get/$appkey", new util.HashMap())
      //   LOG.info(s"$body")
      (Json.parse(body) \ "scalingGroup").asOpt[List[SGConfig]] match {
        case Some(sgs) =>
          val sgsWithEnv = sgs.filter(_.env == env)
          val realSgs = sgsWithEnv.map(x => x.copy(idc = idcEngToChaMap.getOrElse(x.idc, x.idc), fakeFlag = Some(0)))
          val fakeSgs = fakeIdcList.filter(x => !sgsWithEnv.map(_.idc).contains(x)).map(idc => new SGConfig(0L, appkey, idcEngToChaMap.getOrElse(idc, "未知机房"), env, "HULK", 0, 3, 0, 0, 120,
            0L, 0L, 4, 8192, 200, Seq(), None, None, Seq(), Some(1)))
          fakeSgs ::: realSgs
        case None =>
          LOG.warn(s"getScalingGroup($appkey, $env) result None")
          fakeIdcList.map(idc => new SGConfig(0L, appkey, idcEngToChaMap.getOrElse(idc, "未知机房"), env, "HULK", 0, 3, 0, 0, 120,
            0L, 0L, 4, 8192, 200, Seq(), None, None, Seq(), Some(1)))
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getScalingGroup($appkey, $env) exception: $ex")
        fakeIdcList.map(idc => new SGConfig(0L, appkey, idcEngToChaMap.getOrElse(idc, "未知机房"), env, "HULK", 0, 3, 0, 0, 120,
          0L, 0L, 4, 8192, 200, Seq(), None, None, Seq(), Some(1)))
    }
  }

  def getIdcsByAppkeyAndEnv(appkey: String, env: Int) = {
    try {
      val body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/scalinggroup/get/$appkey", new util.HashMap())
      (Json.parse(body) \ "scalingGroup").asOpt[List[SGConfig]] match {
        case Some(sgs) =>
          val sgsWithEnv = sgs.filter(_.env == env)
          val realSgs = sgsWithEnv.map(x => x.copy(idc = idcEngToChaMap.getOrElse(x.idc, x.idc), fakeFlag = Some(0)))
          realSgs
        case None =>
          LOG.warn(s"getIdcsByAppkeyAndEnv($appkey, $env) result None")
          JsonHelper.errorJson(s"该服务未在hulk上面授权")
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getIdcsByAppkeyAndEnv($appkey, $env) exception: $ex")
        //获取伸缩组异常时会给出虚拟伸缩组作提示
        fakeIdcList.map(idc => new SGConfig(0L, appkey, idcEngToChaMap.getOrElse(idc, "未知机房"), env, "HULK", 0, 3, 0, 0, 120,
          0L, 0L, 4, 8192, 200, Seq(), None, None, Seq(), Some(1)))
    }
  }

  def updateScalingGroup(json: String, user: User) = {
    Json.parse(json).validate[SGConfig].fold({
      error =>
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      sgInfo =>
        if (ServiceCommon.isOwnerLogin(sgInfo.appkey, user.getLogin)) {
          try {
            val body = HttpUtil.httpPostRequest(hulkURL + "/api/hulk/scalinggroup/save", Json.toJson(sgInfo.copy(userLogin = user.getLogin,
              idc = idcChaToEngMap.getOrElse(sgInfo.idc, sgInfo.idc))).toString())
            Json.parse(body).asOpt[ScalingGroupResponse] match {
              case Some(response) =>
                if (response.sgId == 0) {
                  LOG.error(s"updateScalingGroup($sgInfo) failure: ${response.errorInfo}")
                  JsonHelper.errorJson(s"更新机房配置失败:${response.errorInfo.getOrElse("")}")
                } else {
                  JsonHelper.dataJson(response.sgId)
                }
              case None =>
                LOG.error(s"updateScalingGroup($sgInfo) error")
                JsonHelper.errorJson(s"更新机房配置失败")
            }
          } catch {
            case ex: Exception =>
              LOG.error(s"updateScalingGroup($sgInfo) exception: $ex")
              JsonHelper.errorJson(s"更新机房配置失败")
          }
        } else {
          JsonHelper.errorJson(s"更新机房配置失败：必须是服务负责人")
        }
    })
  }

  def createScalingGroup(json: String, user: User) = {
    Json.parse(json).validate[ScalingGroupData].fold({
      error =>
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      sgCreateInfo =>
        if (ServiceCommon.isOwnerLogin(sgCreateInfo.appkey, user.getLogin)) {
          try {
            val body = HttpUtil.httpPostRequest(hulkURL + "/api/hulk/scalinggroupinocto/create", Json.toJson(sgCreateInfo.copy(userLogin = user.getLogin,
              zone = idcChaToEngMap.getOrElse(sgCreateInfo.zone, sgCreateInfo.zone))).toString())
            Json.parse(body).asOpt[ScalingGroupResponse] match {
              case Some(response) =>
                if (response.sgId == 0) {
                  LOG.error(s"createScalingGroup($sgCreateInfo) failure: ${response.errorInfo}")
                  JsonHelper.errorJson(s"伸缩组已经存在".trim)
                } else if (response.sgId == -1) {
                  JsonHelper.errorJson(s"sgId: ${response.sgId}")
                } else {
                  JsonHelper.dataJson(response.sgId)
                }
              case None =>
                LOG.error(s"createScalingGroup($sgCreateInfo) error")
                JsonHelper.errorJson(s"机房配置创建失败")
            }

          } catch {
            case ex: Exception =>
              LOG.error(s"createScalingGroup(fo) exception: $ex")
              JsonHelper.errorJson(s"机房配置创建失败")
          }
        } else {
          JsonHelper.errorJson(s"创建机房配置失败：必须是服务负责人")
        }
    })
  }

  def getScalingPolicy(appkey: String, env: Int) = {
    try {
      val body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/scalingpolicy/get/$appkey/$env", new util.HashMap())
      (Json.parse(body) \ "scalingPolicy").asOpt[List[SPConfig]] match {
        case Some(sps) =>
          val realSps = sps.filter(x => x.pType == 1).map(x => x.copy(idcs = x.idcs.map(x => idcEngToChaMap.getOrElse(x, x)), fakeFlag = Some(0)))
          realSps
        case None =>
          LOG.warn(s"getScalingPolicy($appkey, $env) result None")
          List()
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getScalingPolicy($appkey, $env) exception: $ex")
        List()
    }
  }

  def saveScalingPolicy(json: String, user: User) = {
    Json.parse(json).validate[SPConfig].fold({
      error =>
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { spInfo =>
      if (ServiceCommon.isOwnerLogin(spInfo.appkey, user.getLogin)) {
        try {
          val body = HttpUtil.httpPostRequest(hulkURL + "/api/hulk/scalingpolicy/save", Json.toJson(spInfo.copy(idcs = spInfo.idcs.map(x => idcChaToEngMap.getOrElse(x, x)))).toString())
          Json.parse(body).asOpt[ScalingPolicyResponse] match {
            case Some(response) =>
              if (response.spId == 0) {
                LOG.error(s"saveScalingPolicy($spInfo) failure: ${response.errorInfo}")
                JsonHelper.errorJson(s"伸缩策略创建/更新失败:${response.errorInfo.getOrElse("")}")
              } else {
                JsonHelper.dataJson(response.spId)
              }
            case None =>
              LOG.error(s"saveScalingPolicy($spInfo) error")
              JsonHelper.errorJson(s"伸缩策略创建/更新失败")
          }
        } catch {
          case ex: Exception =>
            LOG.error(s"saveScalingPolicy($spInfo) exception: $ex")
            JsonHelper.errorJson(s"伸缩策略创建/更新失败")
        }
      } else {
        JsonHelper.errorJson(s"创建/更新伸缩策略失败：必须是服务负责人")
      }
    })
  }

  def deleteScalingPolicy(appkey: String, user: User, spId: Long) = {
    if (ServiceCommon.isOwnerLogin(appkey, user.getLogin)) {
      try {
        val body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/scalingpolicy/delete/$spId", new util.HashMap())
        Json.parse(body).asOpt[ScalingPolicyResponse] match {
          case Some(response) =>
            if (response.spId == 0) {
              LOG.error(s"deleteScalingPolicy($spId) failure: ${response.errorInfo}")
              JsonHelper.errorJson(s"伸缩策略删除失败:${response.errorInfo.getOrElse("")}")
            } else {
              JsonHelper.dataJson(response.spId)
            }
          case None =>
            LOG.error(s"deleteScalingPolicy($spId) error")
            JsonHelper.errorJson(s"伸缩策略删除失败")
        }
      } catch {
        case ex: Exception =>
          LOG.error(s"deleteScalingPolicy($spId) exception: $ex")
          JsonHelper.errorJson(s"伸缩策略删除异常")
      }
    } else {
      JsonHelper.errorJson(s"删除伸缩策略失败：必须是服务负责人")
    }
  }

  //operatorType：0 扩容, 1 缩容, 100 全部 idcType: all,mos...
  def getScalingRecord(appkey: String, env: Int, startTime: Long, endTime: Long, operatorType: Int, idcType: String, page: Page) = {
    try {
      val body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/scalinggrecord/get/$appkey/$env/$startTime/$endTime", new util.HashMap())
      (Json.parse(body) \ "scalingRecords").asOpt[List[HULKScalingRecord]] match {
        case Some(records) =>
          val result = records.filter(x => (if (operatorType == 0) x.eventType == 4 else if (operatorType == 1) x.eventType == 8 else true)
            && (if (idcType != "all") x.idc == idcType else true)).reverse
          page.setPageSize(20)
          page.setTotalCount(result.length)
          val resultWithFilter = result.slice((page.getPageNo - 1) * page.getPageSize, page.getPageNo * page.getPageSize)
          resultWithFilter.map(x => x.copy(idc = idcEngToChaMap.getOrElse(x.idc, x.idc)))
        case None =>
          LOG.warn(s"getScalingRecord($appkey, $env) result None")
          List()
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getScalingRecord($appkey, $env) exception: $ex")
        List()
    }
  }

  def getScalingRecordScaleOut(appkey: String, env: Int, startTime: Long, endTime: Long, operatorType: Int, idcType: String, page: Page) = {
    try {
      val body = HttpUtil.httpGetRequest(hulkURL + s"/api/octo/scalinggrecord/get/$appkey/$env/$startTime/$endTime", new util.HashMap())
      (Json.parse(body) \ "scalingRecords").asOpt[List[HULKScalingRecord]] match {
        case Some(records) =>
          val result = records.filter(x => (if (operatorType == 0) x.eventType == 4 else if (operatorType == 1) x.eventType == 8 else true)
            && (if (idcType != "all") x.idc == idcType else true)).reverse
          page.setPageSize(20)
          page.setTotalCount(result.length)
          val resultWithFilter = result.slice((page.getPageNo - 1) * page.getPageSize, page.getPageNo * page.getPageSize)
          resultWithFilter.map(x => x.copy(idc = idcEngToChaMap.getOrElse(x.idc, x.idc)))
        case None =>
          LOG.warn(s"getScalingRecord($appkey, $env) result None")
          List()
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getScalingRecord($appkey, $env) exception: $ex")
        List()
    }
  }


  def getScalingPolicyAndGroup(appkey: String, env: Int) = {
    try {

      var body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/scalingpolicyandgroup/get/$appkey/$env", new util.HashMap())
      (Json.parse(body) \ "scalingPolicyAndGroup").asOpt[List[SpAndSgConfigInfo]] match {
        case Some(spAndSgs) =>
          //var realSpAndSgs:List[SpAndSgConfigInfo] = spAndSgs.filter(x => x.policyRow.pType == 1 && x.groupRow.env == env).map(y=> {y.copy(y.policyRow.idcs = y.policyRow.idcs.map(z => idcEngToChaMap.getOrElse(z, z)), y.policyRow.fakeFlag = Some(0));y.copy(y.groupRow.idc = idcEngToChaMap.getOrElse(y.groupRow.idc, y.groupRow.idc), y.groupRow.fakeFlag = Some(0))})
          val realSpAndSgs: List[SpAndSgConfigInfo] = spAndSgs.filter(x => x.groupRow.env == env && x.policyRow.pType == 1)
          val SpAndSgsInfo = realSpAndSgs.map(y => new SpAndSgConfigInfo(y.policyRow.copy(idcs = y.policyRow.idcs.map(z => idcEngToChaMap.getOrElse(z, z)), fakeFlag = Some(0)), y.groupRow.copy(idc = idcEngToChaMap.getOrElse(y.groupRow.idc, y.groupRow.idc), fakeFlag = Some(0))))
          SpAndSgsInfo
        case None =>
          LOG.warn(s"getScalingPolicyAndGroup($appkey, $env) result None")
          List()
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getScalingPolicyAndGroup($appkey, $env) exception: $ex")
        List()
    }
  }

  def getQpsMax(appkey: String, env: String) = {
    try {
      var body = HttpUtil.httpGetRequest(hulkURL + s"/api/octo/query/qpsMax/$appkey", new util.HashMap())
      (Json.parse(body) \ "suggestQpsMax").asOpt[MaxQpsInfo] match {
        case Some(maxQps) =>
          maxQps
        case None =>
          LOG.warn(s"getQpsMax($appkey) result None")
          MaxQpsInfo(0, "none", 0)
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getQpsMax($appkey) exception: $ex")
        MaxQpsInfo(0, "erro", 0)
    }
  }

  def scaleOut(json: String, userIn: User) = {
    Json.parse(json).validate[ScaleOutConfig].fold({
      error =>
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { soInfo =>
      if (ServiceCommon.isOwnerLogin(soInfo.appkey, userIn.getLogin)) {
        try {
          val body = HttpUtil.httpPostRequestForScaleOut(hulkURL + "api/octo/scaleout", Json.toJson(soInfo.copy(env = soInfo.env.map(x => envIntToChaMap.getOrElse(x, x)), zone = soInfo.zone.map(x => idcChaToEngMap.getOrElse(x, x)), user = Some(userIn.getLogin))).toString())
          Json.parse(body).asOpt[ScaleOutResponse] match {
            case Some(response) =>
              if (response.err != None) {
                LOG.error(s"scaleOut($soInfo) failure: ${response.err}")
                if (response.err.getOrElse("").trim() == "scalinggroup not found") {
                  JsonHelper.errorJson(s"一键扩容失败：未找到伸缩组")
                }
                else if (response.err.getOrElse("").trim() == "the num of scale or the quota(cpu/mem/harddisk) of set exceed the scalingGroup limit") {
                  JsonHelper.errorJson(s"一键扩容失败：扩容数量超过伸缩组限制")
                }
                else if (response.err.getOrElse("").trim() == "image not found") {
                  JsonHelper.errorJson(s"一键扩容失败:未找到镜像，扩容需要镜像")
                }
                else if (response.err.getOrElse("").trim() == "cooling") {
                  JsonHelper.errorJson(s"当前已有操作正在扩容中，请稍后操作...")
                }
                else {
                  JsonHelper.errorJson(s"octo一键扩容失败")
                }
              } else {
                LOG.info(s"scalingout success ! $response")
                JsonHelper.dataJson(response.rescode)
              }
            case None =>
              LOG.error(s"scaleOut($soInfo) error")
              JsonHelper.errorJson(s"一键扩容失败")
          }
        } catch {
          case ex: Exception =>
            LOG.error(s"scaleOut($soInfo) exception: $ex")
            JsonHelper.errorJson(s"一键扩容失败")
        }
      } else {
        JsonHelper.errorJson(s"一键扩容失败：必须是服务负责人")
      }
    })
  }

  def getScalingGroupAndRunningSet(appkey: String, env: Int) = {
    try {
      var body = HttpUtil.httpGetRequest(hulkURL + s"/api/octo/query/scalingGroupAndSetInfo/$appkey/$env", new util.HashMap())
      (Json.parse(body) \ "ScalingGroupAndRunningSet").asOpt[List[ScalingGroupAndRunningSetInfo]] match {
        case Some(response) =>
          val SgAndRunningSetInfo = response.map(x => x.copy(idc = idcEngToChaMap.getOrElse(x.idc, x.idc)))
          SgAndRunningSetInfo
        case None =>
          LOG.warn(s"getScalingGroupAndRunningSet($appkey,$env) result None")
          List()
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getScalingGroupAndRunningSet($appkey,$env) exception: $ex")
        List()
    }
  }

  def scaleIn(json: String, userIn: User) = {
    Json.parse(json).validate[ScaleInConfig].fold({
      error =>
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { siInfo =>
      if (ServiceCommon.isOwnerLogin(siInfo.appkey, userIn.getLogin)) {
        try {
          val body = HttpUtil.httpPostRequestForScaleOut(hulkURL + "/api/octo/scalein", Json.toJson(siInfo.copy(user = Some(userIn.getLogin))).toString())
          // LOG.info(s"$body")
          Json.parse(body).asOpt[ScaleInResponse] match {
            case Some(response) =>
              if (response.err != None) {
                LOG.error(s"scaleIn($siInfo) failure: ${response.err}")
                if (response.err.getOrElse("").trim() == "env or zone not found") {
                  JsonHelper.errorJson(s"缩容失败：未找到环境或机房")
                }
                else if (response.err.getOrElse("").trim() == "Internal Error") {
                  JsonHelper.errorJson(s"缩容失败:网络错误")
                }
                else if (response.err.getOrElse("").trim() == "Operation Conflict, other task is running") {
                  JsonHelper.errorJson(s"当前已有操作正在缩容中，请稍后操作...")
                }
                else {
                  JsonHelper.errorJson(s"缩容失败")
                }
              } else {
                LOG.info(s"scalingin success ! $response")
                JsonHelper.dataJson(response)
              }
            case None =>
              LOG.error(s"scaleIn($siInfo) error")
              JsonHelper.errorJson(s"octo缩容失败")
          }
        } catch {
          case ex: Exception =>
            LOG.error(s"scaleIn($siInfo) exception: $ex")
            JsonHelper.errorJson(s"缩容失败")
        }
      } else {
        JsonHelper.errorJson(s"缩容失败：必须是服务负责人")
      }
    })
  }

  def checkIsImagineExist(appkey: String, env: Int, user: User): String = {
    if (ServiceCommon.isOwnerLogin(appkey, user.getLogin)) {
      try {
        val body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/isImageExist/$appkey/$env", new util.HashMap())
        (Json.parse(body) \ "isImageExist").asOpt[Int] match {
          case Some(1) =>
            JsonHelper.dataJson("imageExist")
          case Some(-1) =>
            JsonHelper.dataJson("imageNotExist")
          case _ =>
            JsonHelper.dataJson("error")
        }
      } catch {
        case ex: Exception =>
          LOG.error(s"checkIsImagineExist($appkey) exception: $ex")
          JsonHelper.dataJson("error")
      }
    } else {
      LOG.error(s"checkIsImagineExist erro :${user.getLogin} appkey is: $appkey" )
      JsonHelper.dataJson("notOwer")
    }
  }

  def getPeriodicPolicyAndGroup(appkey: String, env: Int) = {
    try {

      var body = HttpUtil.httpGetRequest(hulkURL + s"/api/hulk/scalingpolicyandgroup/get/$appkey/$env", new util.HashMap())
      (Json.parse(body) \ "scalingPolicyAndGroup").asOpt[List[SpAndSgConfigInfo]] match {
        case Some(spAndSgs) =>
          val realSpAndSgs: List[SpAndSgConfigInfo] = spAndSgs.filter(x => x.groupRow.env == env && x.policyRow.pType == 0)
          val SpAndSgsInfo = realSpAndSgs.map(y => new SpAndSgConfigInfo(y.policyRow.copy(idcs = y.policyRow.idcs.map(z => idcEngToChaMap.getOrElse(z, z)), fakeFlag = Some(0)), y.groupRow.copy(idc = idcEngToChaMap.getOrElse(y.groupRow.idc, y.groupRow.idc), fakeFlag = Some(0))))
          SpAndSgsInfo
        case None =>
          LOG.warn(s"getScalingPolicyAndGroup($appkey, $env) result None")
          List()
      }
    } catch {
      case ex: Exception =>
        LOG.error(s"getScalingPolicyAndGroup($appkey, $env) exception: $ex")
        List()
    }
  }

  def getLatestImageInfo(appkey: String, env: Int) = {
    try {
      var body = HttpUtil.httpGetRequest(hulkURL + s"/api/plus/$appkey/$env/getImageList", new util.HashMap())
      (Json.parse(body) \ "imageList").asOpt[ImageInfo] match {
        case Some(imageInfo) =>
          val myformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
          val time = new Date(imageInfo.compileTime)
          val result = myformat.format(time)
          result
        case None =>
          LOG.warn(s"getLatestImageInfo($appkey, $env) result None")
          None
      }
    }
    catch {
      case ex: Exception =>
        LOG.error(s"getLatestImageInfo($appkey, $env) exception: $ex")
        None
    }
  }

}
