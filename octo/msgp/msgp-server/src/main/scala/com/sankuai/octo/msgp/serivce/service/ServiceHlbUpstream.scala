package com.sankuai.octo.msgp.serivce.service

import java.util.Date

import com.sankuai.meituan.auth.vo.User
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.model.EntityType
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.utils.client.BorpClient.operationDisplay
import com.sankuai.octo.msgp.utils.client.ZkClient
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.collection.JavaConverters._


/**
 * Created by zhoufeng on 15/11/18.
 */
object ServiceHlbUpstream {
  val LOG: Logger = LoggerFactory.getLogger(ServiceHlbUpstream.getClass)
  val idcCq : String = "idc-cq"
  val idcDx : String = "idc-dx"
  val idcLf : String = "idc-lf"
  val idcYf : String = "idc-yf"

  //显式的指定操作用户
  val user = new User()
  user.setId(1000000)
  user.setName("周峰")

  //默认的page
  val defaultPage = new Page


  case class scheduleStartegy(least_conn: Option[String], consistent_hash: Option[String])
  case class serverNode(ip: String, port: Int, fail_timeout: Option[Int], max_fails: Option[Int], slow_start: Option[Int], weight: Option[Int])
  case class checkNode(fall: Option[Int], interval: Option[Int], rise: Option[Int], timeout: Option[Int], ctype: Option[String])
  case class checkStrategy(check: Option[checkNode], check_http_expect_alive: Option[String], check_http_send: Option[String])
  case class upstreamData(upstream_name: String, environment: String, nginx: String, is_grey: Int, idc: String, schedule_strategy: Option[scheduleStartegy],
                         server: Option[List[serverNode]], check_strategy: Option[checkStrategy])

  case class ipAndPort(ip: String, port: Int)
  case class serverToDelete(upstream_name: String, environment: String, nginx: String, idc: String, servers: List[ipAndPort])

  case class createResult(code: String, msg: String)
  case class createSuccess(data:upstreamData, code: String, msg: String)
  case class deleteResult(code: String, msg: String)
  case class searchResult(code: String, msg: String)
  case class deleteServerResult(code: String, msg: String, modifiedUpstream: upstreamData)
  case class updateUpstreamResult(code: String, msg: String, updatedUpstream: upstreamData)
  case class exchangeServerResult(code: String, msg: String, exchangedUpstream: upstreamData)

  implicit val checkNodeReads:Reads[checkNode] = ((JsPath \ "fall").read[Option[Int]] and
                                                 (JsPath \ "interval").read[Option[Int]] and
                                                 (JsPath \ "rise").read[Option[Int]] and
                                                 (JsPath \ "timeout").read[Option[Int]] and
                                                 (JsPath \ "type").read[Option[String]])(checkNode.apply _)
  implicit val checkNodeWrites: Writes[checkNode] = ((JsPath \ "fall").write[Option[Int]] and
                                                    (JsPath \ "interval").write[Option[Int]] and
                                                    (JsPath \ "rise").write[Option[Int]] and
                                                    (JsPath \ "timeout").write[Option[Int]] and
                                                    (JsPath \ "type").write[Option[String]])(unlift(checkNode.unapply))

  implicit val serverNodeReads = Json.reads[serverNode]
  implicit val serverNodeWrites = Json.writes[serverNode]

  implicit val scheduleStrategyReads = Json.reads[scheduleStartegy]
  implicit val scheduleStrategyWrites = Json.writes[scheduleStartegy]

  implicit val checkStrategyReads = Json.reads[checkStrategy]
  implicit val checkStrategyWrites = Json.writes[checkStrategy]

  implicit val upstreamDataReads = Json.reads[upstreamData]
  implicit val upstreamDataWrites = Json.writes[upstreamData]

  implicit val ipAndPortReads = Json.reads[ipAndPort]
  implicit val ipAndPortWrites = Json.writes[ipAndPort]

  implicit val serverToDeleteReads = Json.reads[serverToDelete]
  implicit val serverToDeleteWrites = Json.writes[serverToDelete]

  implicit val createResultReads = Json.reads[createResult]
  implicit val createResultWrites = Json.writes[createResult]

  implicit val createSuccessReads = Json.reads[createSuccess]
  implicit val createSuccessWrites = Json.writes[createSuccess]

  implicit val deleteResultReads = Json.reads[deleteResult]
  implicit val deleteResultWrites = Json.writes[deleteResult]

  implicit val searchResultReads = Json.reads[searchResult]
  implicit val searchResultWrites = Json.writes[searchResult]

  implicit val deleteServerResultReads = Json.reads[deleteServerResult]
  implicit val deleteServerResultWrites = Json.writes[deleteServerResult]

  implicit val updateUpstreamResultReads = Json.reads[updateUpstreamResult]
  implicit val updateUpstreamResultWrites = Json.writes[updateUpstreamResult]


  implicit val exchangeServerResultReads = Json.reads[exchangeServerResult]
  implicit val exchangeServerResultWrites = Json.writes[exchangeServerResult]

  //辅助函数
  def getPath(env: String, nginx: String, idc: String, upstream: String) = "/dyups/" + env + "/" + nginx +"/" + idc + "/" + upstream

  //添加upstream数据
  def addUpstreamData(json: String) = {
    Json.parse(json).validate[upstreamData].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      toCreateUpstream =>
        try{
          if(toCreateUpstream.idc != "shared"){
            val path = getPath(toCreateUpstream.environment, toCreateUpstream.nginx, toCreateUpstream.idc, toCreateUpstream.upstream_name)
            val result = if (ZkClient.exist(path)){
              Json.prettyPrint(Json.toJson(createResult("201", "upstream has been created")))
            } else {
              ZkClient.createWithParent(path)
              ZkClient.setData(path, Json.prettyPrint(Json.toJson(toCreateUpstream)))
              LOG.info("添加upstream: " + path)
              BorpClient.saveOpt(user, actionType = ActionType.INSERT.getIndex, entityId = toCreateUpstream.upstream_name, entityType = EntityType.registerServer, newValue = path)
              Json.prettyPrint(Json.toJson(createSuccess(toCreateUpstream, "200", "create upstream succeed")))
            }
            result
          }else{
            val cqPath = getPath(toCreateUpstream.environment, toCreateUpstream.nginx, idcCq, toCreateUpstream.upstream_name)
            val result = if (ZkClient.exist(cqPath)) {
              Json.prettyPrint(Json.toJson(createResult("201", "upstream has been created")))
            } else {
              Json.prettyPrint(Json.toJson(createSuccess(toCreateUpstream, "200", "create upstream succeed")))
            }
            if (!ZkClient.exist(cqPath)) ZkClient.createWithParent(cqPath)
            ZkClient.setData(cqPath, Json.prettyPrint(Json.toJson(toCreateUpstream)))
            BorpClient.saveOpt(user, actionType = ActionType.INSERT.getIndex, entityId = toCreateUpstream.upstream_name, entityType = EntityType.registerServer, newValue = cqPath)

            val dxPath = getPath(toCreateUpstream.environment, toCreateUpstream.nginx, idcDx, toCreateUpstream.upstream_name)
            if (!ZkClient.exist(dxPath)) ZkClient.createWithParent(dxPath)
            ZkClient.setData(dxPath, Json.prettyPrint(Json.toJson(toCreateUpstream)))
            BorpClient.saveOpt(user, actionType = ActionType.INSERT.getIndex, entityId = toCreateUpstream.upstream_name, entityType = EntityType.registerServer, newValue = dxPath)

            val lfPath = getPath(toCreateUpstream.environment, toCreateUpstream.nginx, idcLf, toCreateUpstream.upstream_name)
            if (!ZkClient.exist(lfPath)) ZkClient.createWithParent(lfPath)
            ZkClient.setData(lfPath, Json.prettyPrint(Json.toJson(toCreateUpstream)))
            BorpClient.saveOpt(user, actionType = ActionType.INSERT.getIndex, entityId = toCreateUpstream.upstream_name, entityType = EntityType.registerServer, newValue = lfPath)

            val yfPath = getPath(toCreateUpstream.environment, toCreateUpstream.nginx, idcYf, toCreateUpstream.upstream_name)
            if (!ZkClient.exist(yfPath)) ZkClient.createWithParent(yfPath)
            ZkClient.setData(yfPath, Json.prettyPrint(Json.toJson(toCreateUpstream)))
            BorpClient.saveOpt(user, actionType = ActionType.INSERT.getIndex, entityId = toCreateUpstream.upstream_name, entityType = EntityType.registerServer, newValue = yfPath)

            LOG.info("添加 shared upstream: " + toCreateUpstream.upstream_name)
            result
          }
        } catch {
          case e: Exception => LOG.error(f"$e")
            JsonHelper.errorJson(e.getMessage)
        }
    })
  }

  //删除upstream数据
  def deleteUpstreamData(json: String) = {
    Json.parse(json).validate[upstreamData].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      toDeleteUpstream =>
        try{
          if(toDeleteUpstream.idc != "shared"){
            val path = getPath(toDeleteUpstream.environment, toDeleteUpstream.nginx, toDeleteUpstream.idc, toDeleteUpstream.upstream_name)
            ZkClient.deleteWithChildren(path)
            BorpClient.saveOpt(user, actionType = ActionType.DELETE.getIndex, entityId = toDeleteUpstream.upstream_name, entityType = EntityType.deleteServer, oldValue = path)
            LOG.info("删除upstream: "+ path)
          }else{
            val path_cq = getPath(toDeleteUpstream.environment, toDeleteUpstream.nginx, idcCq, toDeleteUpstream.upstream_name)
            ZkClient.deleteWithChildren(path_cq)
            BorpClient.saveOpt(user, actionType = ActionType.DELETE.getIndex, entityId = toDeleteUpstream.upstream_name, entityType = EntityType.deleteServer, oldValue = path_cq)

            val path_dx = getPath(toDeleteUpstream.environment, toDeleteUpstream.nginx, idcDx, toDeleteUpstream.upstream_name)
            ZkClient.deleteWithChildren(path_dx)
            BorpClient.saveOpt(user, actionType = ActionType.DELETE.getIndex, entityId = toDeleteUpstream.upstream_name, entityType = EntityType.deleteServer, oldValue = path_dx)

            val path_lf = getPath(toDeleteUpstream.environment, toDeleteUpstream.nginx, idcLf, toDeleteUpstream.upstream_name)
            ZkClient.deleteWithChildren(path_lf)
            BorpClient.saveOpt(user, actionType = ActionType.DELETE.getIndex, entityId = toDeleteUpstream.upstream_name, entityType = EntityType.deleteServer, oldValue = path_lf)

            val path_yf = getPath(toDeleteUpstream.environment, toDeleteUpstream.nginx, idcYf, toDeleteUpstream.upstream_name)
            ZkClient.deleteWithChildren(path_yf)
            BorpClient.saveOpt(user, actionType = ActionType.DELETE.getIndex, entityId = toDeleteUpstream.upstream_name, entityType = EntityType.deleteServer, oldValue = path_yf)

            LOG.info("删除shared upstream: "+ toDeleteUpstream.upstream_name)
          }
        } catch {
          case e: Exception => LOG.error(f"$e")
            JsonHelper.errorJson(e.getMessage)
        }
    })
    Json.prettyPrint(Json.toJson(deleteResult("200", "delete upstream succeed")))
  }

  //查询upstream数据
  def findUpstreamData(path: String) = {
     if(ZkClient.exist(path.toString)) {
       ZkClient.getData(path)
     } else {
       Json.prettyPrint(Json.toJson(deleteResult("200", "can't find the upstream")))
     }
  }

  def findUpstreamList(path: String) = {
    if(ZkClient.exist(path.toString)) {
      Json.prettyPrint(Json.toJson(ZkClient.children(path).asScala))
    } else {
      Json.prettyPrint(Json.toJson(deleteResult("200", "can't find the upstream")))
    }
  }
  def deleteServer(json: String) = {
    Json.parse(json).validate[serverToDelete].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      toDeleteServer =>
        try{
          if(toDeleteServer.idc != "shared"){
            val path = getPath(toDeleteServer.environment, toDeleteServer.nginx, toDeleteServer.idc, toDeleteServer.upstream_name)
            val result = findUpstreamData(path)
            Json.parse(result).validate[upstreamData].fold({
              er => LOG.info(er.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
            }, {
              toModfiyUpstream =>
                try{
                  val nonDeletedServer = toModfiyUpstream.server.get.filter{i =>
                    !toDeleteServer.servers.contains(ipAndPort(i.ip, i.port))
                  }
                  ZkClient.setData(path, Json.prettyPrint(Json.toJson(toModfiyUpstream.copy(server = Some(nonDeletedServer)))))
                  BorpClient.saveOpt(user, actionType = ActionType.DELETE.getIndex, entityId = toModfiyUpstream.upstream_name, entityType = EntityType.deleteProvider, oldValue = Json.toJson(toModfiyUpstream).toString(), newValue = Json.toJson(toModfiyUpstream.copy(server = Some(nonDeletedServer))).toString())

                  LOG.info("删除upstream(" + toDeleteServer.upstream_name +")的server节点")
                  //返回结果
                  Json.prettyPrint(Json.toJson(deleteServerResult("200", "delete server succeed", toModfiyUpstream.copy(server = Some(nonDeletedServer)))))
                } catch {
                  case ex: Exception => LOG.error(f"$ex")
                    JsonHelper.errorJson(ex.getMessage)
                }
            })
          }else{
            //删除idc-cq中upstream的server节点
            val path_cq = getPath(toDeleteServer.environment, toDeleteServer.nginx, idcCq, toDeleteServer.upstream_name)
            val cqResult = findUpstreamData(path_cq)
            Json.parse(cqResult).validate[upstreamData].fold({
              cqEr => LOG.info(cqEr.toString())
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(cqEr).toString())
            }, {
              cqUpstream =>
                try{
                  val cqNonDeletedServer = cqUpstream.server.get.filter{i =>
                    !toDeleteServer.servers.contains(ipAndPort(i.ip, i.port))
                  }
                  ZkClient.setData(path_cq, Json.prettyPrint(Json.toJson(cqUpstream.copy(server = Some(cqNonDeletedServer)))))
                } catch {
                  case cqEx: Exception => LOG.error(f"$cqEx")
                    JsonHelper.errorJson(cqEx.getMessage)
                }
            })

            //删除idc-dx中upstream的server节点
            val path_dx = getPath(toDeleteServer.environment, toDeleteServer.nginx, idcDx, toDeleteServer.upstream_name)
            val dxResult = findUpstreamData(path_dx)
            Json.parse(dxResult).validate[upstreamData].fold({
              dxEr => LOG.info(dxEr.toString())
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(dxEr).toString())
            }, {
              dxUpstream =>
                try{
                  val dxNonDeletedServer = dxUpstream.server.get.filter{j =>
                    !toDeleteServer.servers.contains(ipAndPort(j.ip, j.port))
                  }
                  ZkClient.setData(path_dx, Json.prettyPrint(Json.toJson(dxUpstream.copy(server = Some(dxNonDeletedServer)))))
                } catch {
                  case dxEx: Exception => LOG.error(f"$dxEx")
                    JsonHelper.errorJson(dxEx.getMessage)
                }
            })

            //删除idc-lf中upstream的server节点
            val path_lf = getPath(toDeleteServer.environment, toDeleteServer.nginx, idcLf, toDeleteServer.upstream_name)
            val lfResult = findUpstreamData(path_lf)
            Json.parse(lfResult).validate[upstreamData].fold({
              lfEr => LOG.info(lfEr.toString())
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(lfEr).toString())
            }, {
              lfUpstream =>
                try{
                  val lfNonDeletedServer = lfUpstream.server.get.filter{i =>
                    !toDeleteServer.servers.contains(ipAndPort(i.ip, i.port))
                  }
                  ZkClient.setData(path_lf, Json.prettyPrint(Json.toJson(lfUpstream.copy(server = Some(lfNonDeletedServer)))))
                } catch {
                  case lfEx: Exception => LOG.error(f"$lfEx")
                    JsonHelper.errorJson(lfEx.getMessage)
                }
            })

            //删除idc-yf中upstream的server节点
            val path_yf = getPath(toDeleteServer.environment, toDeleteServer.nginx, idcYf, toDeleteServer.upstream_name)
            val yfResult = findUpstreamData(path_yf)
            Json.parse(yfResult).validate[upstreamData].fold({
              yfEr => LOG.info(yfEr.toString());
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(yfEr).toString())
            }, {
              yfUpstream =>
                try{
                  val yfNonDeletedServer = yfUpstream.server.get.filter{i =>
                    !toDeleteServer.servers.contains(ipAndPort(i.ip, i.port))
                  }
                  ZkClient.setData(path_yf, Json.prettyPrint(Json.toJson(yfUpstream.copy(server = Some(yfNonDeletedServer)))))
                  BorpClient.saveOpt(user, actionType = ActionType.DELETE.getIndex, entityId = yfUpstream.upstream_name, entityType = EntityType.deleteProvider, oldValue = Json.toJson(yfUpstream).toString(), newValue = Json.toJson(yfUpstream.copy(server = Some(yfNonDeletedServer))).toString())

                  LOG.info("删除upstream(" + toDeleteServer.upstream_name +")的server节点")

                  //返回结果
                  Json.prettyPrint(Json.toJson(deleteServerResult("200", "delete server succeed", yfUpstream.copy(server = Some(yfNonDeletedServer)))))
                } catch {
                  case yfEx: Exception => LOG.error(f"$yfEx")
                    JsonHelper.errorJson(yfEx.getMessage)
                }
            })
          }
        } catch {
          case e: Exception => LOG.error(f"$e")
            JsonHelper.errorJson(e.getMessage)
        }
    })
  }

  //修改upstream数据
  def updateUpstreamData(json: String) = {
    Json.parse(json).validate[upstreamData].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      inputUpstream =>
        try{
          if(inputUpstream.idc != "shared"){
            val path = getPath(inputUpstream.environment, inputUpstream.nginx, inputUpstream.idc, inputUpstream.upstream_name)
            val result = findUpstreamData(path)
            Json.parse(result).validate[upstreamData].fold({
              er => LOG.info(er.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
            }, {
              toModifiedUpstream =>
                try{
                  val tmpUpstream = toModifiedUpstream.copy(schedule_strategy = inputUpstream.schedule_strategy match {
                    case Some (tmpscheduleStrategy) => Some(tmpscheduleStrategy)
                    case None => toModifiedUpstream.schedule_strategy
                  }, check_strategy = inputUpstream.check_strategy match {
                    case Some (tmpcheckStrategy) => Some(tmpcheckStrategy)
                    case None => toModifiedUpstream.check_strategy
                  })

                  val resultUpstream = inputUpstream.server match {
                    case Some(inputServers) =>
                      tmpUpstream.server.get.map{i =>
                        inputServers.find(j => j.ip == i.ip && j.port == i.port) match {
                          case Some(j) => i.copy(fail_timeout = j.fail_timeout, max_fails = j.max_fails, slow_start = j.slow_start, weight = j.weight)
                          case None => i
                        }
                      } ++ inputServers
                    case None => tmpUpstream.server.get
                  }
                  val distinctUpstream = resultUpstream.distinct
                  ZkClient.setData(path, Json.prettyPrint(Json.toJson(tmpUpstream.copy(server = Some(distinctUpstream)))))
                  BorpClient.saveOpt(user, actionType = ActionType.UPDATE.getIndex, entityId = inputUpstream.upstream_name, entityType = EntityType.updateServer, oldValue = Json.toJson(toModifiedUpstream).toString(), newValue = Json.toJson(tmpUpstream.copy(server = Some(distinctUpstream))).toString())

                  LOG.info("修改upstream: " + inputUpstream.upstream_name)
                  //返回结果
                  Json.prettyPrint(Json.toJson(updateUpstreamResult("200", "update upstream succeed", tmpUpstream.copy(server = Some(distinctUpstream)))))
                } catch {
                  case ex: Exception => LOG.error(f"$ex")
                    JsonHelper.errorJson(ex.getMessage)
                }
            })

          }else{
            //修改idc-cq环境的节点
            val path_cq = getPath(inputUpstream.environment, inputUpstream.nginx, idcCq, inputUpstream.upstream_name)
            val cqResult = findUpstreamData(path_cq)
            Json.parse(cqResult).validate[upstreamData].fold({
              cqEr => LOG.info(cqEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(cqEr).toString())
            }, {
              cqUpstream =>
                try{
                  val cq = cqUpstream.copy(schedule_strategy = inputUpstream.schedule_strategy match {
                    case Some (tmpscheduleStrategy) => Some(tmpscheduleStrategy)
                    case None => cqUpstream.schedule_strategy
                  }, check_strategy = inputUpstream.check_strategy match {
                    case Some (tmpcheckStrategy) => Some(tmpcheckStrategy)
                    case None => cqUpstream.check_strategy
                  })
                  val cqResultUpstream = inputUpstream.server match {
                    case Some(inputServers) =>
                      cq.server.get.map{i =>
                        inputServers.find(j => j.ip == i.ip && j.port == i.port) match {
                          case Some(j) => i.copy(fail_timeout = j.fail_timeout, max_fails = j.max_fails, slow_start = j.slow_start, weight = j.weight)
                          case None => i
                        }
                      } ++ inputServers
                    case None => cq.server.get
                  }
                  val cqDistinctUpstream = cqResultUpstream.distinct
                  ZkClient.setData(path_cq, Json.prettyPrint(Json.toJson(cq.copy(server = Some(cqDistinctUpstream)))))

                } catch {
                  case cqEx: Exception => LOG.error(f"$cqEx")
                    JsonHelper.errorJson(cqEx.getMessage)
                }
            })

            //修改idc-dx环境的节点
            val path_dx = getPath(inputUpstream.environment, inputUpstream.nginx, idcDx, inputUpstream.upstream_name)
            val dxResult = findUpstreamData(path_dx)
            Json.parse(dxResult).validate[upstreamData].fold({
              dxEr => LOG.info(dxEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(dxEr).toString())
            }, {
              dxUpstream =>
                try{
                  val dx = dxUpstream.copy(schedule_strategy = inputUpstream.schedule_strategy match {
                    case Some (tmpscheduleStrategy) => Some(tmpscheduleStrategy)
                    case None => dxUpstream.schedule_strategy
                  }, check_strategy = inputUpstream.check_strategy match {
                    case Some (tmpcheckStrategy) => Some(tmpcheckStrategy)
                    case None => dxUpstream.check_strategy
                  })
                  val dxResultUpstream = inputUpstream.server match {
                    case Some(inputServers) =>
                      dx.server.get.map{i =>
                        inputServers.find(j => j.ip == i.ip && j.port == i.port) match {
                          case Some(j) => i.copy(fail_timeout = j.fail_timeout, max_fails = j.max_fails, slow_start = j.slow_start, weight = j.weight)
                          case None => i
                        }
                      } ++ inputServers
                    case None => dx.server.get
                  }
                  val dxDistinctUpstream = dxResultUpstream.distinct
                  ZkClient.setData(path_dx, Json.prettyPrint(Json.toJson(dx.copy(server = Some(dxDistinctUpstream)))))
                } catch {
                  case dxEx: Exception => LOG.error(f"$dxEx")
                    JsonHelper.errorJson(dxEx.getMessage)
                }
            })

            //修改idc-lf环境的节点
            val path_lf = getPath(inputUpstream.environment, inputUpstream.nginx, idcLf, inputUpstream.upstream_name)
            val lfResult = findUpstreamData(path_lf)
            Json.parse(lfResult).validate[upstreamData].fold({
              lfEr => LOG.info(lfEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(lfEr).toString())
            }, {
              lfUpstream =>
                try{
                  val lf = lfUpstream.copy(schedule_strategy = inputUpstream.schedule_strategy match {
                    case Some (tmpscheduleStrategy) => Some(tmpscheduleStrategy)
                    case None => lfUpstream.schedule_strategy
                  }, check_strategy = inputUpstream.check_strategy match {
                    case Some (tmpcheckStrategy) => Some(tmpcheckStrategy)
                    case None => lfUpstream.check_strategy
                  })
                  val lfResultUpstream = inputUpstream.server match {
                    case Some(inputServers) =>
                      lf.server.get.map{i =>
                        inputServers.find(j => j.ip == i.ip && j.port == i.port) match {
                          case Some(j) => i.copy(fail_timeout = j.fail_timeout, max_fails = j.max_fails, slow_start = j.slow_start, weight = j.weight)
                          case None => i
                        }
                      } ++ inputServers
                    case None => lf.server.get
                  }
                  val lfDistinctUpstream = lfResultUpstream.distinct
                  ZkClient.setData(path_lf, Json.prettyPrint(Json.toJson(lf.copy(server = Some(lfDistinctUpstream)))))
                } catch {
                  case lfEx: Exception => LOG.error(f"$lfEx")
                    JsonHelper.errorJson(lfEx.getMessage)
                }
            })

            //修改idc-yf环境的节点
            val path_yf = getPath(inputUpstream.environment, inputUpstream.nginx, idcYf, inputUpstream.upstream_name)
            val yfResult = findUpstreamData(path_yf)
            Json.parse(yfResult).validate[upstreamData].fold({
              yfEr => LOG.info(yfEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(yfEr).toString())
            }, {
              yfUpstream =>
                try{
                  val yf = yfUpstream.copy(schedule_strategy = inputUpstream.schedule_strategy match {
                    case Some (tmpscheduleStrategy) => Some(tmpscheduleStrategy)
                    case None => yfUpstream.schedule_strategy
                  }, check_strategy = inputUpstream.check_strategy match {
                    case Some (tmpcheckStrategy) => Some(tmpcheckStrategy)
                    case None => yfUpstream.check_strategy
                  })
                  val yfResultUpstream = inputUpstream.server match {
                    case Some(inputServers) =>
                      yf.server.get.map{i =>
                        inputServers.find(j => j.ip == i.ip && j.port == i.port) match {
                          case Some(j) => i.copy(fail_timeout = j.fail_timeout, max_fails = j.max_fails, slow_start = j.slow_start, weight = j.weight)
                          case None => i
                        }
                      } ++ inputServers
                    case None => yf.server.get
                  }
                  val yfDistinctUpstream = yfResultUpstream.distinct
                  ZkClient.setData(path_yf, Json.prettyPrint(Json.toJson(yf.copy(server = Some(yfDistinctUpstream)))))
                  BorpClient.saveOpt(user, actionType = ActionType.UPDATE.getIndex, entityId = inputUpstream.upstream_name, entityType = EntityType.updateServer, oldValue = Json.toJson(yfUpstream).toString(), newValue = Json.toJson(yf.copy(server = Some(yfDistinctUpstream))).toString())


                  LOG.info("修改upstream: " + inputUpstream.upstream_name)
                  //返回结果
                  Json.prettyPrint(Json.toJson(updateUpstreamResult("200", "update upstream succeed", yf.copy(server = Some(yfDistinctUpstream)))))
                } catch {
                  case yfEx: Exception => LOG.error(f"$yfEx")
                    JsonHelper.errorJson(yfEx.getMessage)
                }
            })
          }
        } catch {
          case e: Exception => LOG.error(f"$e")
            JsonHelper.errorJson(e.getMessage)
        }
    })
  }

  //全量替换server
  def exchangeServer(json: String) = {
    Json.parse(json).validate[upstreamData].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      inputServer =>
        try{
          if(inputServer.idc != "shared"){
            val path = getPath(inputServer.environment, inputServer.nginx, inputServer.idc, inputServer.upstream_name)
            val result = findUpstreamData(path)
            Json.parse(result).validate[upstreamData].fold({
              er => LOG.info(er.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(er).toString())
            }, {
              toExchagenUpstream =>
                try{
                  val exchangedUpstream = toExchagenUpstream.copy(server = inputServer.server match {
                    case Some (tmpServer) => Some (tmpServer)
                    case None => toExchagenUpstream.server
                  })
                  ZkClient.setData(path, Json.prettyPrint(Json.toJson(exchangedUpstream)))
                  BorpClient.saveOpt(user, actionType = ActionType.UPDATE.getIndex, entityId = inputServer.upstream_name, entityType = EntityType.updateProvider, oldValue = Json.toJson(toExchagenUpstream).toString(), newValue = Json.toJson(exchangedUpstream).toString())

                  LOG.info("全量替换upstream(" + inputServer.upstream_name +")的server节点")

                  //返回结果
                  Json.prettyPrint(Json.toJson(exchangeServerResult("200", "exchange server succeed", exchangedUpstream)))
                } catch {
                  case ex: Exception => LOG.error(f"$ex")
                    JsonHelper.errorJson(ex.getMessage)
                }
            })
          }else{
            val path_cq = getPath(inputServer.environment, inputServer.nginx, idcCq, inputServer.upstream_name)
            val cqResult = findUpstreamData(path_cq)
            Json.parse(cqResult).validate[upstreamData].fold({
              cqEr => LOG.info(cqEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(cqEr).toString())
            }, {
              cqUpstream =>
                try{
                  val cqExchangedUpstream = cqUpstream.copy(server = inputServer.server match {
                    case Some (tmpServer) => Some(tmpServer)
                    case None => cqUpstream.server
                  })

                  ZkClient.setData(path_cq, Json.prettyPrint(Json.toJson(cqExchangedUpstream)))
                } catch {
                  case cqEx: Exception => LOG.error(f"$cqEx")
                    JsonHelper.errorJson(cqEx.getMessage)
                }
            })

            val path_dx = getPath(inputServer.environment, inputServer.nginx, idcDx, inputServer.upstream_name)
            val dxResult = findUpstreamData(path_dx)
            Json.parse(dxResult).validate[upstreamData].fold({
              dxEr => LOG.info(dxEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(dxEr).toString())
            }, {
              dxUpstream =>
                try{
                  val dxExchangedUpstream = dxUpstream.copy(server = inputServer.server match {
                    case Some (tmpServer) => Some(tmpServer)
                    case None => dxUpstream.server
                  })

                  ZkClient.setData(path_dx, Json.prettyPrint(Json.toJson(dxExchangedUpstream)))
                } catch {
                  case dxEx: Exception => LOG.error(f"$dxEx")
                    JsonHelper.errorJson(dxEx.getMessage)
                }
            })

            val path_lf = getPath(inputServer.environment, inputServer.nginx, idcLf, inputServer.upstream_name)
            val lfResult = findUpstreamData(path_lf)
            Json.parse(lfResult).validate[upstreamData].fold({
              lfEr => LOG.info(lfEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(lfEr).toString())
            }, {
              lfUpstream =>
                try{
                  val lfExchangedUpstream = lfUpstream.copy(server = inputServer.server match {
                    case Some (tmpServer) => Some(tmpServer)
                    case None => lfUpstream.server
                  })

                  ZkClient.setData(path_lf, Json.prettyPrint(Json.toJson(lfExchangedUpstream)))
                } catch {
                  case lfEx: Exception => LOG.error(f"$lfEx")
                    JsonHelper.errorJson(lfEx.getMessage)
                }
            })

            val path_yf = getPath(inputServer.environment, inputServer.nginx, idcYf, inputServer.upstream_name)
            val yfResult = findUpstreamData(path_yf)
            Json.parse(yfResult).validate[upstreamData].fold({
              yfEr => LOG.info(yfEr.toString)
                JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(yfEr).toString())
            }, {
              yfUpstream =>
                try{
                  val yfExchangedUpstream = yfUpstream.copy(server = inputServer.server match {
                    case Some (tmpServer) => Some(tmpServer)
                    case None => yfUpstream.server
                  })

                  ZkClient.setData(path_yf, Json.prettyPrint(Json.toJson(yfExchangedUpstream)))
                  BorpClient.saveOpt(user, actionType = ActionType.UPDATE.getIndex, entityId = inputServer.upstream_name, entityType = EntityType.updateProvider, oldValue = Json.toJson(yfUpstream).toString(), newValue = Json.toJson(yfExchangedUpstream).toString())

                  LOG.info("全量替换upstream(" + inputServer.upstream_name +")的server节点")

                  //返回结果
                  Json.prettyPrint(Json.toJson(exchangeServerResult("200", "exchange server succeed", yfExchangedUpstream)))
                } catch {
                  case yfEx: Exception => LOG.error(f"$yfEx")
                    JsonHelper.errorJson(yfEx.getMessage)
                }
            })
          }
        } catch {
          case e: Exception => LOG.error(f"$e")
            JsonHelper.errorJson(e.getMessage)
        }
    })
  }

  def searchOperationLog(upstreamName: String, startTime: Date, endTime:Date): List[operationDisplay] = {
    return BorpClient.getOptLogById(upstreamName, startTime, endTime, defaultPage)
  }

}
