package com.sankuai.octo.msgp.serivce.service

import java.text.DecimalFormat

import com.sankuai.msgp.common.config.{DbConnection, MsgpConfig}
import com.sankuai.msgp.common.model.{Page, Status}
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.octo.msgp.serivce.overload.{OswatchService, OverloadDegrade}
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import com.sankuai.octo.oswatch.thrift.data.{ConsumerQuota => _, _}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsArray, JsError, JsValue, Json}

import scala.collection.JavaConverters._
import scala.slick.driver.MySQLDriver.simple._

object ServiceQuota {
  private val LOG: Logger = LoggerFactory.getLogger(ServiceQuota.getClass)
  private val dbCP = DbConnection.getPool()
  val IsAlarmToOCTO = MsgpConfig.get("overload-cutFlow-quota-alarm-switch", "1").trim.toInt

  case class JsonAppQuotaRow(id: Long, name: String, appkey: String, env: Int = 1, method: String = "all",
                             qpsCapacity: Long = 0L, providerCountSwitch: Int = 0, alarmStatus: Int = 1, degradeStatus: Int = 1,
                             degradeend: Int = 1, watchPeriod: Int = 10, ctime: Long = 0L, utime: Long = 0L)

  implicit val f1 = Json.reads[JsonAppQuotaRow]
  implicit val f2 = Json.writes[JsonAppQuotaRow]

  case class JsonConsumerQuotaRow(id: Long, appQuotaId: Long, consumerAppkey: String, qpsRatio: Double,
                                  strategy: Int = 0, redirect: Option[String] = Some(""))

  implicit val g1 = Json.reads[JsonConsumerQuotaRow]
  implicit val g2 = Json.writes[JsonConsumerQuotaRow]

  //providerCountSwitch: 计算appkey Proivder有效节点开关, 0 表示by ip＋port, 1 表示by ip
  case class JsonQuotaWithConsumer(id: Long, name: String, appkey: String, env: Int, method: String, providerCountSwitch: Int = 0,
                                   qpsCapacity: Long = 0L, aliveNode: Int = 0, alarmStatus: Int = 1, degradeStatus: Int = 1, degradeend: Int = 1,
                                   watchPeriod: Int = 10, ctime: Long = 0L, utime: Long = 0L, consumers: List[String])

  implicit val h1 = Json.reads[JsonQuotaWithConsumer]
  implicit val h2 = Json.writes[JsonQuotaWithConsumer]

  /**
   * id:consumerAppkey + method + providerQuota.id
   * degradeRatio: from 0 to 1
   * degradeStrategy:DROP = 0, URL = 1, PROVIDER = 2
   **/
  case class JsonAlarm(id: String, env: Int, providerAppkey: String, consumerAppkey: String, providerQPSCapacity: Int,
                       consumerCurrentQPS: Int, consumerQuotaQPS: Int, degradeRatio: Double, method: String,
                       degradeStrategy: Int, degradeEnd: Int, status: Int, timestamp: Long)

  implicit val jsonAlarmReads = Json.reads[JsonAlarm]
  implicit val jsonAlarmWrites = Json.writes[JsonAlarm]

  def getQuotaWithConsumer(appkey: String, env: Int, page: Page) = {
    val df = new DecimalFormat("0.0")
    val quotaList = env match {
      case 0 =>
        dbCP withSession {
          implicit session: Session =>
            val statement = AppQuota.filter(x => (x.appkey === appkey && x.qpsCapacity =!= 0L))
            statement.list.drop(page.getStart).take(page.getPageSize)
        }
      case _ =>
        dbCP withSession {
          implicit session: Session =>
            val statement = AppQuota.filter(x => (x.appkey === appkey && x.env === env && x.qpsCapacity =!= 0L))
            statement.list.drop(page.getStart).take(page.getPageSize)

        }
    }

    val rv = quotaList.map { quota =>
      val consumers = dbCP withSession {
        implicit session: Session =>
          val statement = ConsumerQuota.filter(x => x.appQuotaId === quota.id)
          statement.list
      }

      JsonQuotaWithConsumer(
        id = quota.id,
        name = quota.name,
        appkey = quota.appkey,
        method = quota.method,
        env = quota.env,
        providerCountSwitch = quota.providerCountSwitch,
        qpsCapacity = quota.qpsCapacity,
        aliveNode = countAliveProviderNode(quota.appkey, quota.env, quota.providerCountSwitch),
        alarmStatus = quota.alarmStatus,
        degradeStatus = quota.degradeStatus,
        degradeend = quota.degradeend,
        watchPeriod = quota.watchPeriod,
        ctime = quota.ctime,
        utime = quota.utime,
        consumers = consumers.map(c =>
          s"${c.consumerAppkey}; ${df.format(c.qpsRatio.toDouble * 100) + "%"}; ${c.strategy}; ${c.redirect.getOrElse("")}"
        ))
    }
    page.setTotalCount(rv.length)
    JsonHelper.dataJson(rv, page)
  }

  def addQuota(json: String) = {
    Json.parse(json).validate[JsonAppQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => val quota = AppQuotaRow(x.id, x.name, x.appkey, x.env, x.method, x.qpsCapacity, x.alarmStatus, x.degradeStatus,
        x.degradeend, x.watchPeriod, x.ctime, x.utime, 0L, x.providerCountSwitch)
        dbCP withSession {
          implicit session: Session =>
            val statement = x.method match {
              case "all" =>
                AppQuota.filter(x => (x.appkey === quota.appkey && x.env === quota.env))
              case _ =>
                AppQuota.filter(x => (x.appkey === quota.appkey && x.env === quota.env && (x.method === quota.method || x.method === "all")))
            }
            if (!statement.exists.run) {
              val id = (AppQuota returning AppQuota.map(_.id)) += quota
              if (IsAlarmToOCTO == 1) message(x.appkey)
              //向oswatch注册监控,新增
              val monitorPolicy = new MonitorPolicy(0L, x.appkey, EnvType.findByValue(x.env), GteType.GTE, x.watchPeriod, MonitorType.QPS, x.qpsCapacity).setSpanName(x.method).setProviderCountSwitch(x.providerCountSwitch)
              val oswatchId = OswatchService.addMonitorQuota(monitorPolicy)
              // 将oswatch返回的注册Id 写入数据库oswatchId，oswatchId!=0表明已注册
              val tmpOswatchId = for {c <- AppQuota if c.id === id} yield c.oswatchid
              tmpOswatchId.update(oswatchId)
              JsonHelper.dataJson(s"insert success ${id}")
            } else {
              LOG.info("can't insert, the quota is conflict with existed quota in overloadProtection or cutFlow function")
              JsonHelper.errorJson("can't insert, the quota is conflict with existed quota in overloadProtection or cutFlow function ")
            }
        }
    })
  }

  def addUpdateQuota(json: String) = {
    Json.parse(json).validate[JsonAppQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => val quota = AppQuotaRow(x.id, x.name, x.appkey, x.env, x.method, x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend, x.watchPeriod, x.ctime, x.utime, 0L, x.providerCountSwitch)
        dbCP withSession {
          implicit session: Session =>
            val statement = AppQuota.filter(x => (x.appkey === quota.appkey && x.env === quota.env && x.method === quota.method)).map(x => (x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend, x.watchPeriod, x.utime, x.providerCountSwitch))
            if (!statement.exists.run) {
              val id = (AppQuota returning AppQuota.map(_.id)) += quota
              //当RD配置了overload 向相关人员发送提醒信息，方便跟踪统计
              if (IsAlarmToOCTO == 1) message(x.appkey)
              //向oswatch注册监控，新增
              val monitorPolicy = new MonitorPolicy(0L, x.appkey, EnvType.findByValue(x.env), GteType.GTE, x.watchPeriod, MonitorType.QPS, x.qpsCapacity).setSpanName(x.method).setProviderCountSwitch(x.providerCountSwitch)
              val oswatchId = OswatchService.addMonitorQuota(monitorPolicy)
              // 将oswatch返回的注册Id 写入数据库oswatchId，oswatchId!=0表明已注册
              val tmpOswatchId = for {c <- AppQuota if c.id === id} yield c.oswatchid
              tmpOswatchId.update(oswatchId)
              JsonHelper.dataJson(s"insert success ${id}")
            } else {
              val now = new DateTime().getMillis
              statement.update(quota.qpsCapacity, quota.alarmStatus, quota.degradeStatus, quota.degradeend, quota.watchPeriod, now, quota.providerCountSwitch)
              //更新oswatch已注册的监控项，主要是qps大小的设置
              LOG.info("getOswatchId(x.appkey, x.env, x.method):" + getOswatchId(x.appkey, x.env, x.method))
              val monitorPolicy = new MonitorPolicy(getOswatchId(x.appkey, x.env, x.method), x.appkey, EnvType.findByValue(x.env), GteType.GTE, x.watchPeriod, MonitorType.QPS, x.qpsCapacity).setSpanName(x.method).setProviderCountSwitch(x.providerCountSwitch)
              OswatchService.addMonitorQuota(monitorPolicy)
              JsonHelper.dataJson(quota.id)
            }
        }
    })
  }

  def delQuotaWithConsumer(quotaId: Long) = {
    dbCP withSession {
      implicit session: Session =>
        val baseInf = getProQuotaBaseInf(quotaId)
        //删除oswatch监控项
        OswatchService.delMonitorQuota(baseInf._1)
        //若zk上存有降级决策，删除
        OverloadDegrade.removeDegradeNode(baseInf._3, baseInf._2, baseInf._4)
        AppQuota.filter(x => x.id === quotaId).delete
        ConsumerQuota.filter(x => x.appQuotaId === quotaId).delete
        JsonHelper.dataJson(s"delete success ${quotaId}")
    }
  }

  def updateQuota(json: String) = {
    Json.parse(json).validate[JsonAppQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => val quota = AppQuotaRow(x.id, x.name, x.appkey, x.env, x.method, x.qpsCapacity, x.alarmStatus,
        x.degradeStatus, x.degradeend, x.watchPeriod, x.ctime, x.utime, 0L, x.providerCountSwitch)
        dbCP withSession {
          implicit session: Session =>
            val statement = AppQuota.filter(x => x.id === quota.id).map(x => (x.qpsCapacity, x.alarmStatus, x.degradeStatus, x.degradeend, x.watchPeriod, x.utime, x.providerCountSwitch))
            if (!statement.exists.run) {
              LOG.info(s"can't update quota,${quota} didn't exist")
              JsonHelper.errorJson(s"can't update quota,${quota} didn't exist")
            } else {
              val now = new DateTime().getMillis
              statement.update(quota.qpsCapacity, quota.alarmStatus, quota.degradeStatus, quota.degradeend, quota.watchPeriod, now, quota.providerCountSwitch)
              LOG.info("getOswatchId(x.appkey, x.env, x.method):" + getOswatchId(x.appkey, x.env, x.method))
              val monitorPolicy = new MonitorPolicy(getOswatchId(x.appkey, x.env, x.method), x.appkey, EnvType.findByValue(x.env), GteType.GTE, x.watchPeriod, MonitorType.QPS, x.qpsCapacity).setSpanName(x.method).setProviderCountSwitch(x.providerCountSwitch)
              OswatchService.addMonitorQuota(monitorPolicy)
              JsonHelper.dataJson(quota.id)
            }
        }
    })
  }

  def delQuota(quotaId: Long) = {
    dbCP withSession {
      implicit session: Session =>
        val baseInf = getProQuotaBaseInf(quotaId)
        //删除oswatch监控项
        OswatchService.delMonitorQuota(baseInf._1)
        //若zk上存有降级决策，删除
        OverloadDegrade.removeDegradeNode(baseInf._3, baseInf._2, baseInf._4)
        AppQuota.filter(x => x.id === quotaId).delete
        JsonHelper.dataJson(quotaId)
    }
  }

  def getQuota(quotaId: Long) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = AppQuota.filter(x => x.id === quotaId)
        JsonHelper.dataJson(statement.list)
    }
  }

  def getQuota(appkey: String, method: String) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = AppQuota.filter(x => (x.appkey === appkey && x.method === method && x.qpsCapacity =!= 0L))
        JsonHelper.dataJson(statement.list)
    }
  }

  def addBatchConsumer(json: String) = {
    val arr = Json.parse(json).\("consumers").asInstanceOf[JsArray].value.toSeq
    val xx = arr.map(addUpdateConsumer)

    xx.last
  }

  def addSingleConsumer(json: String) = {
    addUpdateConsumer(Json.parse(json))
  }

  def addConsumer(json: JsValue): String = {
    json.validate[JsonConsumerQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => val consumer = ConsumerQuotaRow(x.id, x.appQuotaId, x.consumerAppkey, x.qpsRatio, x.strategy, x.redirect)
        dbCP withSession {
          implicit session: Session =>
            val statement = ConsumerQuota.filter(x => x.appQuotaId === consumer.appQuotaId && x.consumerAppkey === consumer.consumerAppkey)
            if (!statement.exists.run) {
              val id = (ConsumerQuota returning ConsumerQuota.map(_.id)) += consumer
              JsonHelper.dataJson(id)
            } else {
              LOG.info(s"can't insert consumer,${consumer} exist")
              JsonHelper.errorJson(s"can't insert consumer,${consumer} exist")
            }
        }
    })
  }

  def addUpdateConsumer(json: JsValue) = {
    json.validate[JsonConsumerQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => val consumer = ConsumerQuotaRow(x.id, x.appQuotaId, x.consumerAppkey, x.qpsRatio, x.strategy, x.redirect)
        dbCP withSession {
          implicit session: Session =>
            val statement = ConsumerQuota.filter(x => x.appQuotaId === consumer.appQuotaId && x.consumerAppkey === consumer.consumerAppkey).map(x => (x.consumerAppkey, x.qpsRatio, x.strategy, x.redirect))
            if (!statement.exists.run) {
              val id = (ConsumerQuota returning ConsumerQuota.map(_.id)) += consumer
              JsonHelper.dataJson(id)
            } else {
              statement.update(consumer.consumerAppkey, consumer.qpsRatio, consumer.strategy, consumer.redirect)
              JsonHelper.dataJson(consumer.id)
            }
        }
    })
  }

  def updateConsumer(json: String) = {
    Json.parse(json).validate[JsonConsumerQuotaRow].fold({
      error => LOG.info(error.toString)
        JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      x => val consumer = ConsumerQuotaRow(x.id, x.appQuotaId, x.consumerAppkey, x.qpsRatio, x.strategy, x.redirect)
        dbCP withSession {
          implicit session: Session =>
            val statement = ConsumerQuota.filter(x => x.appQuotaId === consumer.appQuotaId && x.consumerAppkey === consumer.consumerAppkey).map(x => (x.consumerAppkey, x.qpsRatio, x.strategy, x.redirect))
            if (!statement.exists.run) {
              LOG.info(s"can't update quota,$consumer didn't exist")
              JsonHelper.errorJson(s"can't update quota,$consumer didn't exist")
            } else {
              statement.update(consumer.consumerAppkey, consumer.qpsRatio, consumer.strategy, consumer.redirect)
              JsonHelper.dataJson(consumer.id)
            }
        }
    })
  }

  def delConsumer(id: Long) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = ConsumerQuota.filter(x => x.id === id)
        statement.delete
        JsonHelper.dataJson(id)
    }
  }

  def getConsumer(appQuotaId: Int) = {
    dbCP withSession {
      implicit session: Session =>
        val statement = ConsumerQuota.filter(x => x.appQuotaId === appQuotaId.toLong)
        JsonHelper.dataJson(statement.list)
    }
  }

  def getQuotaByoswatchId(oswatchId: Long) = {
    dbCP withSession {
      implicit session: Session =>
        val list = AppQuota.filter(x => (x.qpsCapacity =!= 0L && x.oswatchid === oswatchId)).list
        list.headOption match {
          case None =>
            //app_quota 数据库中不存在该配置项 删除oswatch中该注册项
            OswatchService.delMonitorQuota(oswatchId)
            None
          case Some(quota) =>
            val consumerList = ConsumerQuota.filter(x => x.appQuotaId === quota.id).list.map { x =>
              val consumer = new com.sankuai.octo.oswatch.thrift.data.ConsumerQuota()
              consumer.setConsumerAppkey(x.consumerAppkey)
              consumer.setQPSRatio(x.qpsRatio.toDouble)
              consumer.setDegradeStrategy(DegradeStrategy.findByValue(x.strategy))
              consumer.setDegradeRedirect(x.redirect.getOrElse(null))
              consumer
            }
            val result = new ProviderQuota()
            result.setId(quota.id.toString)
            result.setName(quota.name)
            result.setProviderAppkey(quota.appkey)
            result.setEnv(quota.env)
            result.setMethod(quota.method)
            result.setQPSCapacity(quota.qpsCapacity.toInt)
            result.setProNumCntSwitch(ProviderNumCountSwitch.findByValue(quota.providerCountSwitch))
            result.setConsumerList(consumerList.asJava)
            result.setStatus(DegradeStatus.findByValue(quota.degradeStatus))
            result.setAlarm(AlarmStatus.findByValue(quota.alarmStatus))
            result.setDegradeEnd(DegradeEnd.findByValue(quota.degradeend))
            result.setWatchPeriodInSeconds(quota.watchPeriod)
            result.setCreateTime(quota.ctime)
            result.setUpdateTime(quota.utime)
            Some(result)
        }
    }
  }

  def getQuotaList(limit: Int, offset: Int): Seq[ProviderQuota] = {
    dbCP withSession {
      implicit session: Session =>
        val list = AppQuota.drop(offset).take(limit).list
        list.map { quota =>
          val consumerList = ConsumerQuota.filter(x => x.appQuotaId === quota.id).list.map { x =>
            val consumer = new com.sankuai.octo.oswatch.thrift.data.ConsumerQuota()
            consumer.setConsumerAppkey(x.consumerAppkey)

            consumer.setQPSRatio(x.qpsRatio.toDouble)
            consumer.setDegradeStrategy(DegradeStrategy.findByValue(x.strategy))
            consumer.setDegradeRedirect(x.redirect.getOrElse(null))
            consumer
          }
          val result = new ProviderQuota()
          result.setId(quota.id.toString)
          result.setName(quota.name)
          result.setProviderAppkey(quota.appkey)
          result.setEnv(quota.env)
          result.setMethod(quota.method)
          result.setQPSCapacity(quota.qpsCapacity.toInt)
          result.setProNumCntSwitch(ProviderNumCountSwitch.findByValue(quota.providerCountSwitch))
          result.setConsumerList(consumerList.asJava)
          result.setStatus(DegradeStatus.findByValue(quota.degradeStatus))
          result.setAlarm(AlarmStatus.findByValue(quota.alarmStatus))
          result.setDegradeEnd(DegradeEnd.findByValue(quota.degradeend))
          result.setWatchPeriodInSeconds(quota.watchPeriod)
          result.setCreateTime(quota.ctime)
          result.setUpdateTime(quota.utime)
          result
        }
    }
  }

  def countQuotaWithoutRegister(): Int = {
    dbCP withSession {
      implicit session: Session =>
        AppQuota.filter(x => (x.qpsCapacity =!= 0L && x.oswatchid =!= 0L)).length.run
    }
  }

  private def getOswatchId(appkey: String, env: Int, method: String) = {
    val quotaList =
      dbCP withSession {
        implicit session: Session =>
          LOG.info("info: " + appkey + " " + env + " " + method)
          // AppQuota.filter(c => (c.appkey === appkey && c.env === env && c.method === method)).map(_.id).sum.run
          AppQuota.filter(x => (x.appkey === appkey && x.env === env && x.method === method && x.qpsCapacity =!= 0L)).list
      }
    quotaList.headOption match {
      case None => 0L
      case Some(quota) => quota.oswatchid
    }
  }

  private def getOswatchId(id: Long) = {
    val quotaList =
      dbCP withSession {
        implicit session: Session =>
          AppQuota.filter(x => (x.id === id && x.qpsCapacity =!= 0L)).list
      }
    quotaList.headOption match {
      case None => 0L
      case Some(quota) => quota.oswatchid
    }
  }

  //通过id 查询其oswatchId、 providerappkey、env 、method
  private def getProQuotaBaseInf(quotaId: Long) = {
    val quotaList =
      dbCP withSession {
        implicit session: Session =>
          AppQuota.filter(x => x.id === quotaId).list
      }
    quotaList.headOption match {
      case None =>
        (0L, "", 1, "all")
      case Some(quota) =>
        (quota.oswatchid, quota.appkey, quota.env, quota.method)
    }
  }

  /**
   * 计算appkey的有效节点开关：countByIP = 0 表示by ip＋port
   * countByIP = 1 表示by ip
   **/
  def countAliveProviderNode(appkey: String, envId: Int, countByIP: Int = 0): Int = {
    val nodeList = try {
      AppkeyProviderService.provider(appkey, envId)
    } catch {
      case e: Exception => Nil
    }

    if (nodeList != null && nodeList.nonEmpty) {
      countByIP match {
        case 0 =>
          nodeList.count(n => n.status == Status.ALIVE.id)
        case 1 =>
          nodeList.filter(x => x.status == Status.ALIVE.id).map(_.ip).distinct.length
      }
    }
    else 0
  }

  /* *
      当RD配置过载保护或者一键截流时 发送提醒
   * */
  def message(appkey: String) {
    val alarmMessage = s"${appkey}配置新截流"
    val userIdList = List(64137)
    val overloadQuotaAlarm = Alarm(s"octo提醒：${alarmMessage}", alarmMessage, null)
    val modes = Seq(MODE.XM)
    Messager.sendAlarm(userIdList, overloadQuotaAlarm, modes)
  }
}
