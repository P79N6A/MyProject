package com.sankuai.octo.msgp.dao.service

import com.sankuai.meituan.auth.util.UserUtils
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.config.{DbConnection, MsgpConfig}
import com.sankuai.msgp.common.model.Env
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.serivce.overload.OverloadDegrade
import com.sankuai.octo.msgp.serivce.service.ServiceQuota
import com.sankuai.octo.msgp.service.cutFlow.CutFlowService
import com.sankuai.octo.oswatch.thrift.data.{DegradeAction, DegradeEnd, DegradeStrategy}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}

import scala.collection.JavaConverters._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}

object ServiceCutFlowDAO {
  private val LOG: Logger = LoggerFactory.getLogger(ServiceCutFlowDAO.getClass)
  private val dbCP = DbConnection.getPool()
  private val ALL = "all"
  private val OTHERS = "others"
  private val link = "/data/tabNav"

  val IsAlarmToOCTO = MsgpConfig.get("overload-cutFlow-quota-alarm-switch", "1").trim.toInt

  case class JsonAppCutFlowQuotaRow(id: Long, name: String, appkey: String, env: Int = 1, method: String = "all", qpsCapacity: Long = 0L,
                                    alarmStatus: Int = 1, degradeStatus: Int = 1, degradeend: Int = 1, watchPeriod: Int = 10,
                                    ctime: Long = 0L, utime: Long = 0L, hostQpsCapacity: Long = 0L, clusterQpsCapacity: Long = 0L, testStatus: Int = 0)

  implicit val cutFLowRead = Json.reads[JsonAppCutFlowQuotaRow]
  implicit val cutFlowWrite = Json.writes[JsonAppCutFlowQuotaRow]

  case class JsonConsumerRatioRow(id: Long, appQuotaId: Long, consumerAppkey: String, qpsRatio: Double, strategy: Int = 0, redirect: Option[String] = Some(""))

  implicit val consumerRatioRead = Json.reads[JsonConsumerRatioRow]
  implicit val consumerRatioWrite = Json.writes[JsonConsumerRatioRow]

  case class ConfigWithQuota(config: String, qpsRatio: Option[Double], cutFlowLink: Option[String])

  implicit val configWithQuotaRead = Json.reads[ConfigWithQuota]
  implicit val configWithQuotaWrite = Json.writes[ConfigWithQuota]

  case class ConsumerCutFlowConfigWithQuota(id: Long, name: String, appkey: String, env: Int, method: String, qpsCapacity: Long = 0L, alarmStatus: Int = 1,
                                            degradeStatus: Int = 1, degradeend: Int = 1, watchPeriod: Int = 10, ctime: Long = 0L, utime: Long = 0L,
                                            hostQpsCapacity: Long = 0L, clusterQpsCapacity: Long = 0L, testStatus: Int = 0, consumers: List[ConfigWithQuota])

  implicit val cutFlowWithConsumerRead = Json.reads[ConsumerCutFlowConfigWithQuota]
  implicit val cutFlowWithConsumerWrite = Json.writes[ConsumerCutFlowConfigWithQuota]

  case class JsonConsumerQuotaConfigRow(id: Long, appQuotaId: Long, consumerAppkey: String, clusterQuota: Int, hostQuota: Int, strategy: Int = 0, redirect: Option[String] = Some(""))

  implicit val consumerConfigRead = Json.reads[JsonConsumerQuotaConfigRow]
  implicit val consumerConfigWrite = Json.writes[JsonConsumerQuotaConfigRow]

  case class JsonDegradeAction(id: String, env: Int, providerAppkey: String, consumerAppkey: String, method: String,
                               degradeRatio: Double, degradeStrategy: Int, timestamp: Long, degradeRedirect: Option[String],
                               degradeEnd: Int, extend: String)

  implicit val degradeActionRead = Json.reads[JsonDegradeAction]
  implicit val degradeActionWrite = Json.writes[JsonDegradeAction]

  case class ConsumerQuotaDomain(consumerAppkey: String, hostQuota: Int, clusterQuota: Int, strategy: Int, qpsRatio: Option[Double])

  implicit val getConsumerQuotaDomainResult = GetResult(r => ConsumerQuotaDomain(r.<<, r.<<, r.<<, r.<<, r.<<))

  case class QuotaWarning(consumer: String, realQps: Double, quotaThreshold: Double, ackStatus: Int)

  def getCutFlowQuota(quotaId: Long) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = AppQuota.filter(x => x.id === quotaId)
        JsonHelper.dataJson(statement.list)
    }
  }

  def getCutFlowQuota(appkey: String, method: String) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = AppQuota.filter(x => x.appkey === appkey && x.method === method && x.qpsCapacity === 0L)
        JsonHelper.dataJson(statement.list)
    }
  }

  def getCutFlowQuotaWithConsumer(appkey: String, env: Int, name: String) = {
    val cutFlowQuotaList = dbCP withSession {
      implicit session: Session =>
        AppQuota.filter(x => x.appkey === appkey && x.env === env && x.name === name).list
    }

    val rv = cutFlowQuotaList.map { quota =>
      val consumers = dbCP withSession {
        implicit session: Session =>
          val configs = ConsumerQuotaConfig.filter(x => x.appQuotaId === quota.id).list
          val consumerAppkeys = configs.map(_.consumerAppkey)

          if (consumerAppkeys.contains(OTHERS)) {
            val othersConfig = configs.filter(_.consumerAppkey == OTHERS).head
            val notOthersConfigs = configs.filterNot(_.consumerAppkey == OTHERS)
            val notOthersConsumers = notOthersConfigs.map { item =>
              val firstOpt = ConsumerQuota.filter(x => x.appQuotaId === quota.id && x.consumerAppkey === item.consumerAppkey).firstOption
              val qpsRatio = if (firstOpt.nonEmpty) Some(firstOpt.get.qpsRatio.toDouble) else None
              ConsumerQuotaDomain(item.consumerAppkey, item.hostQuota, item.clusterQuota, item.strategy, qpsRatio)
            }
            val firstOpt = ConsumerQuota.filter(x => x.appQuotaId === quota.id && !(x.consumerAppkey inSet notOthersConfigs.map(_.consumerAppkey))).firstOption
            val qpsRatio = if (firstOpt.nonEmpty) Some(firstOpt.get.qpsRatio.toDouble) else None
            notOthersConsumers ++ List(ConsumerQuotaDomain(othersConfig.consumerAppkey, othersConfig.hostQuota, othersConfig.clusterQuota, othersConfig.strategy, qpsRatio))
          } else {
            val consumerQuota = ConsumerQuota.filter(x => x.appQuotaId === quota.id).list
            val disableConfigs = configs.filterNot(x => consumerQuota.map(_.consumerAppkey).contains(x.consumerAppkey))
            val disableConfigsConsumerQuota = disableConfigs.map { item =>
              ConsumerQuotaDomain(item.consumerAppkey, item.hostQuota, item.clusterQuota, item.strategy, None)
            }
            val otherConsumerQuotaWithoutConfig = consumerQuota.map { item =>
              val configOpt = configs.find(_.consumerAppkey == item.consumerAppkey)
              if (configOpt.nonEmpty) {
                val config = configOpt.get
                ConsumerQuotaDomain(config.consumerAppkey, config.hostQuota, config.clusterQuota, config.strategy, Some(item.qpsRatio.toDouble))
              } else {
                ConsumerQuotaDomain(item.consumerAppkey, -1, -1, item.strategy, Some(item.qpsRatio.toDouble))
              }
            }
            disableConfigsConsumerQuota ++ otherConsumerQuotaWithoutConfig
          }
      }

      ConsumerCutFlowConfigWithQuota(
        id = quota.id,
        name = quota.name,
        appkey = quota.appkey,
        method = quota.method,
        env = quota.env,
        qpsCapacity = quota.qpsCapacity,
        alarmStatus = quota.alarmStatus,
        degradeStatus = quota.degradeStatus,
        degradeend = quota.degradeend,
        watchPeriod = quota.watchPeriod,
        ctime = quota.ctime,
        utime = quota.utime,
        hostQpsCapacity = quota.hostQpsCapacity,
        clusterQpsCapacity = quota.clusterQpsCapacity,
        testStatus = quota.testStatus,
        consumers = consumers.map { c =>
          val cutFlowLink = if (c.qpsRatio.nonEmpty) {
            val consumerAppkey = if (c.consumerAppkey == "others") "*" else c.consumerAppkey
            Some(s"$link?appkey=${quota.appkey}&env=${Env.apply(env)}&remoteApp=$consumerAppkey&spanname=${quota.method}&merge=false#source")
          } else {
            None
          }
          if (c.clusterQuota < 0) {
            ConfigWithQuota(s"${c.consumerAppkey};-;-;0", c.qpsRatio, cutFlowLink)
          } else {
            ConfigWithQuota(s"${c.consumerAppkey};${c.hostQuota};${c.clusterQuota};${c.strategy}", c.qpsRatio, cutFlowLink)
          }
        })
    }

    JsonHelper.dataJson(rv)
  }

  def addCutFlowQuota(json: String) = {
    Json.parse(json).validate[JsonAppCutFlowQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        val quota = AppQuotaRow(x.id, x.name, x.appkey, x.env, x.method, x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend,
          x.watchPeriod, x.ctime, x.utime, 0L, 0, x.hostQpsCapacity, x.clusterQpsCapacity, x.testStatus)
        dbCP withSession {
          implicit session: Session =>
            val statement = x.method match {
              case "all" => AppQuota.filter(x => x.appkey === quota.appkey && x.env === quota.env)
              case _ => AppQuota.filter(x => x.appkey === quota.appkey && x.env === quota.env && (x.method === quota.method || x.method === "all"))
            }

            if (!statement.exists.run) {
              val id = (AppQuota returning AppQuota.map(_.id)) += quota
              if (IsAlarmToOCTO == 1)
                ServiceQuota.message(x.appkey)
              JsonHelper.dataJson(id)
            } else {
              LOG.info(s"插入失败，方法名${x.method}配额已经存在，注：all 不能与其他方法同在")
              JsonHelper.errorJson(s"插入失败，方法名${x.method}配额已经存在，注：all 不能与其他方法同在")
            }
        }
    })
  }

  /**
    * 根据截流类型删除截流配置:
    * 1, 删除Provider侧配置
    * 2, 删除Consumer侧配置
    * 3, 删除Consumer侧策略
    * 4, 删除ZK中的信息
    *
    */
  def delCutFlowAppQuota(id: Long, quotaType: String) = {
    dbCP withSession {
      implicit session: Session =>
        quotaType match {
          case "simple" =>    //一键截流
            val appQuota = AppQuota.filter(x => x.id === id).list
            if(!appQuota.isEmpty && appQuota.head.degradeStatus == 0)
              delCutFlowMsgInZK(id)
          case "strategy" =>  //thrift截流
            deleteConsumerQuota(id)
            delCutFlowMsgInZK(id)
        }
        delConsumerConfig(id)
        AppQuota.filter(x => x.id === id).delete   // 再删除AppQuota信息
        JsonHelper.dataJson(id)
    }
  }

  def updateCutFlowQuotas(json: String) = {
    Json.parse(json).validate[JsonAppCutFlowQuotaRow].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        val quota = AppQuotaRow(x.id, x.name, x.appkey, x.env, x.method, x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend,
          x.watchPeriod, x.ctime, x.utime, 0L, 0, x.hostQpsCapacity, x.clusterQpsCapacity, x.testStatus)

        val updateResult = ServiceCutFlowDAO.updateCutFlowQuota(quota)
        updateResult match {
          case None =>
            LOG.info(s"更新配额失败,$quota 不存在")
            JsonHelper.errorJson(s"更新配额失败,$quota 不存在")
          case Some(id) =>
            // 测试模式
            if (x.testStatus == 0) {
              // 删除ZK数据
              ServiceCutFlowDAO.delCutFlowMsgInZK(x.id)
            }
            // disable
            if (x.degradeStatus == 1) {
              // 删除之前所有的策略
              ServiceCutFlowDAO.delCutFlowMsgInZK(x.id)
              ServiceCutFlowDAO.deleteConsumerQuota(x.id)
            }
            // 非测试模式
            if (x.testStatus == 1) {
              ServiceCutFlowDAO.wrtCutFlowMsgToZK(quota.id)
            }

            val msg = CutFlowService.getNotifyMessage(quota, "策略")
            OverloadDegrade.cutFlowAlarm(List(quota.appkey), msg)
            JsonHelper.dataJson(quota.id)
        }
    })
  }

  def updateCutFlowQuota(quota: AppQuotaRow) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = AppQuota.filter(x => x.id === quota.id).map(x => (x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend, x.watchPeriod, x.utime, x.hostQpsCapacity, x.clusterQpsCapacity, x.testStatus, x.ackStatus))
        if (!statement.exists.run) {
          None
        } else {
          val now = new DateTime().getMillis / 1000
          statement.update(quota.qpsCapacity, quota.alarmStatus, quota.degradeStatus, quota.degradeend, quota.watchPeriod, now, quota.hostQpsCapacity, quota.clusterQpsCapacity, quota.testStatus, quota.ackStatus)
          Some(quota.id)
        }
    }
  }

  def addUpdateCutFlowQuota(json: String) = {
    Json.parse(json).validate[JsonAppCutFlowQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => val quota = AppQuotaRow(x.id, x.name, x.appkey, x.env, x.method, x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend, x.watchPeriod, x.ctime, x.utime, 0L)
        dbCP withSession {
          implicit session: Session =>
            val statement = AppQuota.filter(x => x.appkey === quota.appkey && x.env === quota.env && x.method === quota.method).map(x => (x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend, x.watchPeriod, x.utime))
            if (!statement.exists.run) {
              val id = (AppQuota returning AppQuota.map(_.id)) += quota
              if (IsAlarmToOCTO == 1) ServiceQuota.message(x.appkey)
              wrtCutFlowMsgToZK(id)
              JsonHelper.dataJson(id)
            } else {
              val now = new DateTime().getMillis
              statement.update(quota.qpsCapacity, quota.alarmStatus, quota.degradeStatus, quota.degradeend, quota.watchPeriod, now)
              wrtCutFlowMsgToZK(x.appkey, x.env, x.method)
              JsonHelper.dataJson(quota.id)
            }
        }
    })
  }

  def delCutFlowQuotaWithConsumer(quotaId: Long) = {
    LOG.info("delCutFlowQuotaWithConsumer is called!")
    dbCP withSession {
      implicit session: Session =>
        delCutFlowMsgInZK(quotaId)
        AppQuota.filter(x => x.id === quotaId).delete
        ConsumerQuota.filter(x => x.appQuotaId === quotaId).delete
        JsonHelper.dataJson(s"Delete success $quotaId")
    }
  }

  def getConsumer(appQuotaId: Int) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = ConsumerQuotaConfig.filter(x => x.appQuotaId === appQuotaId.toLong)
        JsonHelper.dataJson(statement.list)
    }
  }

  def addBatchConsumerConfig(json: String) = {
    Json.parse(json).validate[List[JsonConsumerQuotaConfigRow]].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { x =>
      dbCP withSession {
        implicit session: Session =>
          x.map { item =>
            val consumerQuotaConfig = ConsumerQuotaConfigRow(item.id, item.appQuotaId, item.consumerAppkey, item.clusterQuota, item.hostQuota, item.strategy, item.redirect)
            val statement = ConsumerQuotaConfig.filter(x => x.appQuotaId === consumerQuotaConfig.appQuotaId && x.consumerAppkey === consumerQuotaConfig.consumerAppkey)
              .map(x => (x.consumerAppkey, x.clusterQuota, x.hostQuota, x.strategy, x.redirect))

            if (!statement.exists.run)
              ConsumerQuotaConfig += consumerQuotaConfig
            else
              statement.update(consumerQuotaConfig.consumerAppkey, consumerQuotaConfig.clusterQuota, consumerQuotaConfig.hostQuota, consumerQuotaConfig.strategy, consumerQuotaConfig.redirect)
          }
      }
      JsonHelper.dataJson("ok")
    })
  }

  def doAddUpdateConsumer(x: JsonConsumerRatioRow) = {
    val consumer = ConsumerQuotaRow(x.id, x.appQuotaId, x.consumerAppkey, x.qpsRatio, x.strategy, x.redirect)
    dbCP withSession {
      implicit session: Session =>
        val statement = ConsumerQuota.filter(x => x.appQuotaId === consumer.appQuotaId && x.consumerAppkey === consumer.consumerAppkey)
          .map(x => (x.consumerAppkey, x.qpsRatio, x.strategy, x.redirect))
        if (!statement.exists.run) {
          val id = (ConsumerQuota returning ConsumerQuota.map(_.id)) += consumer
          JsonHelper.dataJson(id)
        } else {
          statement.update(consumer.consumerAppkey, consumer.qpsRatio, consumer.strategy, consumer.redirect)
          JsonHelper.dataJson(consumer.id)
        }
    }
  }

  def delConsumerConfig(id: Long) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = ConsumerQuotaConfig.filter(x => x.id === id)
        statement.list.headOption match {
          case None =>
            JsonHelper.dataJson(id)
          case Some(consumerQuotaConfig) =>
            statement.delete
            val list = ConsumerQuotaConfig.filter(x => x.appQuotaId === consumerQuotaConfig.appQuotaId).list
            if (list.isEmpty) {
              delCutFlowMsgInZK(consumerQuotaConfig.appQuotaId)
              deleteConsumerQuota(consumerQuotaConfig.appQuotaId)
            }
            JsonHelper.dataJson(id)
        }
    }
  }

  def genDegradeAction(id: String, env: Int, providerAppkey: String, consumerAppkey: String, method: String,
                       degradeRatio: Double, degradeStrategy: DegradeStrategy, degradeEnd: DegradeEnd, timestamp: Long) = {
    val action: DegradeAction = new DegradeAction
    action.setId(id)
    action.setConsumerAppkey(consumerAppkey)
    action.setTimestamp(timestamp)
    action.setDegradeRatio(degradeRatio)
    action.setDegradeStrategy(degradeStrategy)
    action.setMethod(method)
    action.setProviderAppkey(providerAppkey)
    action.setEnv(env)
    action.setConsumerQPS(0)
    action.setDegradeEnd(degradeEnd)
    action.setDegradeRedirect("")
    action
  }

  def genDegradeActions(providerAppkey: String, env: Int, method: String) = {
    val quotaList =
      dbCP withSession {
        implicit session: Session =>
          AppQuota.filter(x => x.appkey === providerAppkey && x.env === env && x.method === method && x.qpsCapacity === 0L).list
      }

    quotaList.headOption match {
      case None =>
        List()
      case Some(quota) =>
        if (quota.degradeStatus == 1) {
          // 截流开关为停用状态
          List()
        } else {
          val consumers =
            dbCP withSession {
              implicit session: Session =>
                ConsumerQuota.filter(x => x.appQuotaId === quota.id).list
            }
          consumers.isEmpty match {
            case true =>
              List()
            case false =>
              consumers.map(c =>
                Some(genDegradeAction("", quota.env, quota.appkey, c.consumerAppkey, quota.method, c.qpsRatio.toDouble,
                  DegradeStrategy.findByValue(c.strategy), DegradeEnd.findByValue(quota.degradeend), System.currentTimeMillis))
              )
          }
        }
    }
  }

  def getProQuotaBaseInf(quotaId: Long) = {
    val quotaList =
      dbCP withSession {
        implicit session: Session =>
          AppQuota.filter(x => x.id === quotaId).list
      }
    quotaList.headOption match {
      case None =>
        ("", 1, "all")
      case Some(quota) =>
        (quota.appkey, quota.env, quota.method)
    }
  }

  private def wrtCutFlowMsgToZK(providerAppkey: String, env: Int, method: String): Unit = {
    val actions = genDegradeActions(providerAppkey, env, method)
    actions.isEmpty match {
      case true =>
        OverloadDegrade.removeDegradeNode(env, providerAppkey, method)
      case false =>
        OverloadDegrade.updateDegradeAction(env, providerAppkey, method, actions)
    }
  }

  def wrtCutFlowMsgToZK(id: Long): Unit = {
    val baseInf = getProQuotaBaseInf(id)
    wrtCutFlowMsgToZK(baseInf._1, baseInf._2, baseInf._3)
  }

  def delCutFlowMsgInZK(quotaId: Long): Unit = {
    LOG.info("delCutFlowMsgInZK is called!")
    val baseInf = getProQuotaBaseInf(quotaId)
    OverloadDegrade.removeDegradeNode(baseInf._2, baseInf._1, baseInf._3)
  }

  def getAllQuotaConfigs = {
    dbCP withSession {
      implicit session: Session =>
        //for test suite
        //val appQuotas = AppQuota.filter(x => x.appkey === "com.sankuai.inf.msgp" && x.name === "").list
        val appQuotas = AppQuota.filter(x => x.degradeStatus === 0 && x.name === "").list
        appQuotas.flatMap {
          quota =>
            val list = ConsumerQuotaConfig.filter(_.appQuotaId === quota.id).list
            if (list.nonEmpty) {
              Some((quota, list.asJava))
            } else {
              None
            }
        }.toMap.asJava
    }
  }

  def renewCutAck(appQuotaId: Long) = {
    dbCP withSession {
      implicit session: Session =>
        AppQuota.filter(_.id === appQuotaId).map(_.ackStatus).update(0)
    }
  }

  def renewWarnAck(appQuotaId: Long, consumerAppkeys: java.util.List[String]) = {
    dbCP withSession {
      implicit session: Session =>
        ConsumerQuotaConfig.filter(x => x.appQuotaId === appQuotaId && !(x.consumerAppkey inSet consumerAppkeys.asScala.toList)).map(_.ackStatus).update(0)
    }
  }

  def doCutAck(appQuotaId: Long) = {
    try {
      val user = UserUtils.getUser
      dbCP withSession {
        implicit session: Session =>
          AppQuota.filter(_.id === appQuotaId).map(x => (x.ackStatus, x.ackUser, x.ackTime)).update(1, user.getLogin, (new DateTime().getMillis / 1000).toInt)
      }
      JsonHelper.dataJson("ack success")
    } catch {
      case e: Exception => LOG.error("cut flow do ack failed", e)
        JsonHelper.errorJson("ack failed")
    }
  }

  def doWarnAck(appQuotaId: Long, consumer: String) = {
    try {
      val user = UserUtils.getUser
      dbCP withSession {
        implicit session: Session =>
          val statement = ConsumerQuotaConfig.filter {
            x =>
              x.appQuotaId === appQuotaId &&
                x.consumerAppkey === consumer
          }
          if (statement.exists.run) {
            statement.map(x => (x.ackStatus, x.ackUser, x.ackTime)).update(1, user.getLogin, (new DateTime().getMillis / 1000).toInt)
          }
      }
      JsonHelper.dataJson("ack success")
    } catch {
      case e: Exception => LOG.error("quota warning do ack failed", e)
        JsonHelper.errorJson("ack failed")
    }
  }

  def deleteConsumerQuota(appQuotaId: Long) = {
    dbCP withSession {
      implicit session: Session =>
        ConsumerQuota.filter(x => x.appQuotaId === appQuotaId).delete
    }
  }


  //一键截流
  case class ConsumerQuotas(consumerAppkey: String, qpsRatio: Double)

  implicit val consumerQuotasRead = Json.reads[ConsumerQuotas]
  implicit val consumerQuotasWrite = Json.writes[ConsumerQuotas]

  case class QuotaWithConsumers(id: Long, name: String, appkey: String, env: Int, method: String, qpsCapacity: Long = 0L, alarmStatus: Int = 1,
                                degradeStatus: Int = 1, degradeend: Int = 1, watchPeriod: Int = 10, ctime: Long = 0L, utime: Long = 0L,
                                hostQpsCapacity: Long = 0L, clusterQpsCapacity: Long = 0L, testStatus: Int = 0, consumers: List[ConsumerQuotas])

  implicit val quotaWithConsumersRead = Json.reads[QuotaWithConsumers]
  implicit val quotaWithConsumersWrite = Json.writes[QuotaWithConsumers]

  def getQuotaWithConsumers(appkey: String, env: Int, name: String) = {
    val cutFlowQuotaList = dbCP withSession {
      implicit session: Session =>
        AppQuota.filter(x => x.appkey === appkey && x.env === env && x.name === name).list
    }
    getConsumersFromDB(cutFlowQuotaList, appkey, env)
  }

  def getConsumersFromZK(cutFlowQuotaList: List[AppQuota#TableElementType], appkey: String, env: Int) = {
    val result = cutFlowQuotaList.map { quota =>
      val cutFlowJson = OverloadDegrade.getDegradeAction(env, appkey, quota.method)
      cutFlowJson.equals("") match {
        case false =>
          Json.parse(cutFlowJson).validate[List[JsonDegradeAction]].fold({
            error =>
              LOG.info(error.toString)
          }, { x =>
            val consumers = x.map { item =>
              ConsumerQuotas(item.consumerAppkey, item.degradeRatio)
            }
            QuotaWithConsumers(quota.id, quota.name, quota.appkey, quota.env, quota.method,
              quota.qpsCapacity, quota.alarmStatus, quota.degradeStatus, quota.degradeend, quota.watchPeriod,
              quota.ctime, quota.utime, quota.hostQpsCapacity, quota.clusterQpsCapacity, quota.testStatus, consumers)
          })
        case true =>
          QuotaWithConsumers(quota.id, quota.name, quota.appkey, quota.env, quota.method,
            quota.qpsCapacity, quota.alarmStatus, quota.degradeStatus, quota.degradeend, quota.watchPeriod,
            quota.ctime, quota.utime, quota.hostQpsCapacity, quota.clusterQpsCapacity, quota.testStatus, List())
      }
    }
    JsonHelper.dataJson(result)
  }

  def getConsumersFromDB(cutFlowQuotaList: List[AppQuota#TableElementType], appkey: String, env: Int) = {
    val result = cutFlowQuotaList.map { quota =>
      val consumerConfig = dbCP withSession {
        implicit session: Session =>
          val statement = ConsumerQuotaConfig.filter(x => x.appQuotaId === quota.id)
          statement.list
      }
      val consumers = consumerConfig.map { item =>
        ConsumerQuotas(item.consumerAppkey, item.hostQuota / 100.0)
      }
      QuotaWithConsumers(quota.id, quota.name, quota.appkey, quota.env, quota.method,
        quota.qpsCapacity, quota.alarmStatus, quota.degradeStatus, quota.degradeend, quota.watchPeriod,
        quota.ctime, quota.utime, quota.hostQpsCapacity, quota.clusterQpsCapacity, quota.testStatus, consumers)
    }
    JsonHelper.dataJson(result)
  }

  def updateQuota(json: String, attr: String) = {
    Json.parse(json).validate[QuotaWithConsumers].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x =>
        val quota = AppQuotaRow(x.id, x.name, x.appkey, x.env, x.method, x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend,
          x.watchPeriod, x.ctime, x.utime, 0L, 0, x.hostQpsCapacity, x.clusterQpsCapacity, x.testStatus)
        val updateResult = ServiceCutFlowDAO.updateCutFlowQuota(quota)
        updateResult match {
          case None =>
            LOG.info(s"更新配置失败,$quota 不存在")
            JsonHelper.errorJson(s"更新配置失败,$quota 不存在")
          case Some(id) => // 启用  禁用  截流比例
            attr match {
              case "open" =>
                // db数据写入zk
                val consumerConfig = dbCP withSession {
                  implicit session: Session =>
                    val statement = ConsumerQuotaConfig.filter(x => x.appQuotaId === quota.id)
                    statement.list
                }
                val jsonArray = consumerConfig.map({ item =>
                  JsonDegradeAction("", x.env, x.appkey, item.consumerAppkey, x.method, item.hostQuota / 100.0, DegradeStrategy.DROP.getValue(),
                    System.currentTimeMillis, Some(""), DegradeEnd.SERVER.getValue(), "")
                })
                OverloadDegrade.updateDegradeAction(Json.toJson(jsonArray).toString, x.env, x.appkey, x.method)
              case "close" =>
                // 删除zk数据
                delCutFlowMsgInZK(quota.id)
              case "ratio" =>
                // 修改截流配置
                val consumerConfig = JsonConsumerRatioRow(0, quota.id, OTHERS, quota.hostQpsCapacity / 100.0, 0, Some(""))
                doAddUpdateConsumer(consumerConfig)
                // 判断是否处于启用状 更新zk数据
                if (quota.degradeStatus == 0) {
                  val cutFlowJson = OverloadDegrade.getDegradeAction(quota.env, quota.appkey, quota.method)
                  cutFlowJson.equals("") match {
                    case false =>
                      Json.parse(cutFlowJson).validate[List[JsonDegradeAction]].fold({
                        error =>
                          LOG.info(error.toString)
                          None
                      }, { x =>
                        val notOthers = x.filterNot(item => OTHERS.equals(item.consumerAppkey))
                        val others = x.filter(item => OTHERS.equals(item.consumerAppkey)).map({ item =>
                          JsonDegradeAction("", item.env, item.providerAppkey, item.consumerAppkey, item.method, quota.hostQpsCapacity / 100.0, DegradeStrategy.DROP.getValue(),
                            System.currentTimeMillis, Some(""), DegradeEnd.SERVER.getValue(), item.extend)
                        })
                        LOG.info(others.toString)
                        val jsonArray = notOthers ++: others
                        OverloadDegrade.updateDegradeAction(Json.toJson(jsonArray).toString, quota.env, quota.appkey, quota.method)
                      })
                    case true => None
                  }
                }
            }
            val msg = CutFlowService.getNotifyMessage(quota, "一键")
            OverloadDegrade.cutFlowAlarm(List(quota.appkey), msg)
            JsonHelper.dataJson(quota.id)
        }
    })
  }

  def addConsumerQuota(json: String, status: Boolean) = {
    Json.parse(json).validate[List[JsonConsumerQuotaConfigRow]].fold({
      error =>
        LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, { x =>
      //所有consumerQuota写入db
      dbCP withSession {
        implicit session: Session =>
          x.map { item =>
            val consumerQuotaConfig = ConsumerQuotaConfigRow(item.id, item.appQuotaId, item.consumerAppkey, item.clusterQuota, item.hostQuota, item.strategy, item.redirect)
            val statement = ConsumerQuotaConfig.filter(x => x.appQuotaId === consumerQuotaConfig.appQuotaId && x.consumerAppkey === consumerQuotaConfig.consumerAppkey)
              .map(x => (x.consumerAppkey, x.clusterQuota, x.hostQuota, x.strategy, x.redirect))
            if (!statement.exists.run)
              ConsumerQuotaConfig += consumerQuotaConfig
            else
              statement.update(consumerQuotaConfig.consumerAppkey, consumerQuotaConfig.clusterQuota, consumerQuotaConfig.hostQuota, consumerQuotaConfig.strategy, consumerQuotaConfig.redirect)
          }
      }
      if (status) {
        //启用状态写入zk
        val baseInf = getProQuotaInf(x.head.appQuotaId)
        val actions = x.map { item =>
          Some(genDegradeAction("", baseInf.env, baseInf.appkey, item.consumerAppkey, baseInf.method, (item.hostQuota / 100.0),
            DegradeStrategy.findByValue(item.strategy), DegradeEnd.findByValue(0), System.currentTimeMillis))
        }
        actions.isEmpty match {
          case true =>
            OverloadDegrade.removeDegradeNode(baseInf.env, baseInf.appkey, baseInf.method)
          case false =>
            OverloadDegrade.updateDegradeAction(baseInf.env, baseInf.appkey, baseInf.method, actions)
        }
      }
      JsonHelper.dataJson("ok")
    })
  }

  def getProQuotaInf(quotaId: Long) = {
    val quotaList =
      dbCP withSession {
        implicit session: Session =>
          AppQuota.filter(x => x.id === quotaId).list
      }
    quotaList.head
  }

  def deleteConsumerQuotas(appQuotaId: Long, consumerAppkey: String, degradeStatus: Int) = {
    val baseInf = getProQuotaInf(appQuotaId)
    //删除db
    deleteConsumerQuotaWithKey(baseInf.id, consumerAppkey)
    // 删除zk
    if (baseInf.degradeStatus == 0) {
      val cutFlowJson = OverloadDegrade.getDegradeAction(baseInf.env, baseInf.appkey, baseInf.method)
      val result = cutFlowJson.equals("") match {
        case false =>
          Json.parse(cutFlowJson).validate[List[JsonDegradeAction]].fold({
            error =>
              LOG.info(error.toString)
              None
          }, { x =>
            val json = x.filterNot(item => consumerAppkey.equals(item.consumerAppkey))
            OverloadDegrade.updateDegradeAction(Json.toJson(json).toString, baseInf.env, baseInf.appkey, baseInf.method)
          })
        case true => None
      }
      JsonHelper.dataJson(result)
    } else {
      JsonHelper.dataJson("ok")
    }
  }

  def deleteConsumerQuotaWithKey(appQuotaId: Long,consumerAppkey : String) = {
    LOG.info(s"delete consumer Quotas.$consumerAppkey")
    dbCP withSession {
      implicit session: Session =>
        ConsumerQuotaConfig.filter(x => x.appQuotaId === appQuotaId && x.consumerAppkey === consumerAppkey).delete
    }
  }

  def genJsonConsumerRatioRow(ratio : CutFlowRatio) = {
    JsonConsumerRatioRow(ratio.id, ratio.appQuotaId, ratio.consumerAppkey, ratio.qpsRatio, ratio.strategy,Some(""))
  }

  case class CutFlowRatio(id: Long, appQuotaId: Long, consumerAppkey: String, qpsRatio: Double,
                                         qpsRatioAll: Double, startTime: Long, endTime: Long, strategy: Int = 0,
                                         qpsList: String, hostQuota: Long, clusterQuota: Long, consumerQuota : Long)

  implicit val cutFlowRatioRead = Json.reads[CutFlowRatio]
  implicit val cutFlowRatioWrite = Json.writes[CutFlowRatio]

}
