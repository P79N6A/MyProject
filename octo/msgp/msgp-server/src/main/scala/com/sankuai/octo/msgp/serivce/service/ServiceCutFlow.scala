//package com.sankuai.octo.msgp.serivce.service
//
//import java.util.concurrent.{Executors, TimeUnit}
//
//import com.meituan.jmonitor.JMonitor
//import com.sankuai.meituan.auth.util.UserUtils
//import com.sankuai.msgp.common.config.db.msgp.Tables._
//import com.sankuai.msgp.common.model.{Env, Page}
//import com.sankuai.msgp.common.utils.helper.CommonHelper
//import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO
//import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO.JsonConsumerRatioRow
//import com.sankuai.octo.msgp.model.EnvMap
//import com.sankuai.octo.msgp.serivce.data.DataQuery
//import com.sankuai.octo.msgp.serivce.overload.OverloadDegrade
//import org.joda.time.DateTime
//import org.slf4j.LoggerFactory
//
//import scala.concurrent.duration.Duration
//
///*
//  只支持thrift服务
// */
//object ServiceCutFlow {
//  private val LOGGER = LoggerFactory.getLogger(this.getClass)
//  private implicit val timeout = Duration.create(10000, TimeUnit.MILLISECONDS)
//  private val ALL = "all"
//  private val OTHERS = "others"
//  private val DEFAULT_COUNT = List(0.0, 0.0, 0.0, 0.0, 0.0)
//  private val scheduler = Executors.newScheduledThreadPool(1)
//  private val WARNING_PERCENT = 0.8
//  private val QPS_RECORDS_NUMBER = 5
//
//  case class StrategyResponse(ratio: Double, reducedFlow: Double)
//
//  case class QuotaWarning(consumer: String, realQps: Double, quotaThreshold: Double, ackStatus: Int)
//
//  // 截流阈值 = Min(集群配额, 单机配额/Max(权重)*Sum(权重))
//  def getQuota(appkey: String, envId: Int, clusterQuota: Long, hostQuota: Long) = {
//    val page = new Page()
//    page.setPageSize(-1)
//    val providers = AppkeyProviderService.getProviderByType(appkey, 1, Env.apply(envId).toString, "", -1, page)
//    val weights = providers.map(_.fweight.getOrElse(0.0).toInt)
//    val maxWeight = if (weights.isEmpty) {
//      0
//    } else {
//      weights.max
//    }
//
//    val ret = if (maxWeight == 0) {
//      clusterQuota
//    } else {
//      List(hostQuota / maxWeight * weights.sum, clusterQuota).min
//    }
//    ret
//  }
//
//  // ratio为0不降级
//  def getStrategy(quotaThreshold: Long, qpsList: List[Double]) = {
//    val qpsListReverse = qpsList.reverse
//    var ratio = 0.0
//    var reducedFlow = 0.0
//    //取得的 分钟数据条数需要大于5
//    if (qpsListReverse.length >= QPS_RECORDS_NUMBER) {
//      val greaterQpsList = qpsListReverse.filter(_ > quotaThreshold)
//      //如果最近的一次QPS大于阈值
//      if (qpsListReverse.head > quotaThreshold) {
//        val average = greaterQpsList.sum / greaterQpsList.length
//        ratio = ((average - quotaThreshold) / average).formatted("%.2f").toDouble
//        reducedFlow = average - quotaThreshold
//      } else {
//        //否则如果比阈值大的qps数大于4
//        if (greaterQpsList.length >= QPS_RECORDS_NUMBER - 1) {
//          val average = greaterQpsList.sum / greaterQpsList.length
//          ratio = ((average - quotaThreshold) / average).formatted("%.2f").toDouble
//          reducedFlow = average - quotaThreshold
//        }
//      }
//    }
//    StrategyResponse(ratio, reducedFlow)
//  }
//
//  /**
//    * 获取当前时间到6分钟之前间的QPS
//    *
//    * @param appQuota 服务提供方配额
//    * @return 包含QPS的DataRecord
//    */
//  def getQps(appQuota: AppQuotaRow) = {
//    val end = (new DateTime().getMillis / 1000).toInt
//    val start = end - (QPS_RECORDS_NUMBER + 1) * 60
//    val data = DataQuery.getDataRecord(appQuota.appkey, start, end, "thrift", "server", "", Env.apply(appQuota.env).toString,
//      "", "SpanRemoteApp", appQuota.method, ALL, "*", ALL, "hbase")
//    val allData =DataQuery.getDataRecord(appQuota.appkey, start, end, "thrift", "server", "", Env.apply(appQuota.env).toString,
//      "", "SpanRemoteApp", appQuota.method, ALL, ALL, ALL, "hbase")
//    data.getOrElse(List()) ++ allData.getOrElse(List())
//  }
//
//  /**
//    * 根据qps生成降级策略
//    * 先判断单个appkey配额
//    * 再判断others配额
//    * 最后判断集群容量
//    *
//    * @param appQuota             服务提供方配额
//    * @param consumerQuotaConfigs 接入方配额
//    * @param consumerAppkeys      接入方的appkey
//    * @param data                 qps数据
//    * @return 降级策略
//    */
//  def getDegrees(appQuota: AppQuotaRow, consumerQuotaConfigs: List[ConsumerQuotaConfigRow], consumerAppkeys: List[String], data: List[DataQuery.DataRecord]) = {
//    try {
//      // consumerAppkey包括appkey和其他
//      val notOthersAppkeys = consumerAppkeys.filterNot(_.equals(OTHERS))
//      val notOthersConfigs = consumerQuotaConfigs.filterNot(_.consumerAppkey == OTHERS)
//      val othersConfigOpt = consumerQuotaConfigs.find(_.consumerAppkey == OTHERS)
//      val dataWithoutAll = data.filter(x => x.tags.remoteApp.getOrElse("") != ALL)
//
//      // 针对单个appkey检测是否降级
//      val notOthersDegree = notOthersConfigs.map { config =>
//        val tmp = dataWithoutAll.find(x => config.consumerAppkey == x.tags.remoteApp.getOrElse(""))
//        if (tmp.nonEmpty) {
//          val dataRecord = tmp.get
//          val qpsList = dataRecord.qps.map(_.y.getOrElse(0.0))
//          val ret = getStrategy(config.clusterQuota, qpsList)
//          val ratio = ret.ratio
//          if (ratio != 0) {
//            Some((JsonConsumerRatioRow(0, appQuota.id, config.consumerAppkey, ratio, config.strategy, Some("")), ret))
//          } else {
//            None
//          }
//        } else {
//          None
//        }
//      }.flatMap(x => x)
//
//      // 针对others检测是否降级
//      val othersData = dataWithoutAll.filterNot(x => notOthersAppkeys.contains(x.tags.remoteApp.getOrElse("")))
//      val othersCountList = othersData.map(_.qps.map(_.y.getOrElse(0.0)))
//      val qpsList = (0 until QPS_RECORDS_NUMBER).map { i =>
//        othersCountList.map(_.applyOrElse(i, DEFAULT_COUNT)).sum
//      }.toList
//      val othersDegreeOpt = if (othersConfigOpt.nonEmpty) {
//        val othersConfig = othersConfigOpt.get
//        val othersStrategy = getStrategy(othersConfig.clusterQuota, qpsList)
//        val ratio = othersStrategy.ratio
//        if (ratio != 0) {
//          // 降级
//          othersData.map { item =>
//            val remoteApp = item.tags.remoteApp
//            if (remoteApp.nonEmpty) {
//              Some((JsonConsumerRatioRow(0, appQuota.id, remoteApp.get, ratio, othersConfig.strategy, Some("")), othersStrategy))
//            } else {
//              None
//            }
//          }
//        } else {
//          List(None)
//        }
//      } else {
//        List(None)
//      }
//      val othersDegree = othersDegreeOpt.flatMap(x => x)
//      val othersDegreeReducedFlow = if (othersDegree.nonEmpty) {
//        othersDegree.head._2.reducedFlow
//      } else {
//        0
//      }
//
//      // 判断集群容量是否过载
//      val degrees = notOthersDegree ++ othersDegree
//      val reducedFlowSum = notOthersDegree.map {
//        item =>
//          item._2.reducedFlow
//      }.sum + othersDegreeReducedFlow
//
//      val allDataOpt = data.find(_.tags.remoteApp.getOrElse("").equalsIgnoreCase(ALL))
//
//      val qpsListWithoutReducedFlow = if (allDataOpt.isDefined) {
//        allDataOpt.get.qps.map(_.y.getOrElse(0.0) - reducedFlowSum)
//      } else {
//        List(0.0)
//      }
//      val allQuotaThreshold = getQuota(appQuota.appkey, appQuota.env, appQuota.clusterQpsCapacity, appQuota.hostQpsCapacity)
//      val allStrategy = getStrategy(allQuotaThreshold, qpsListWithoutReducedFlow)
//      val allRatio = allStrategy.ratio
//      val finalDegrees = if (allRatio != 0) {
//        val newDegrees = degrees.map { item =>
//          val row = item._1
//          val finalRatio = (row.qpsRatio + allRatio * (1 - row.qpsRatio)).formatted("%.2f").toDouble
//          row.copy(qpsRatio = finalRatio)
//        }
//        val othersDegrees = if (othersConfigOpt.isEmpty) {
//          othersData.map { item =>
//            val remoteApp = item.tags.remoteApp
//            if (remoteApp.nonEmpty) {
//              Some(JsonConsumerRatioRow(0, appQuota.id, remoteApp.get, allRatio, 0, Some("")))
//            } else {
//              None
//            }
//          }
//        } else {
//          List(None)
//        }
//        newDegrees ++ othersDegrees.flatMap(x => x)
//      } else {
//        degrees.map { item =>
//          val row = item._1
//          row
//        }
//      }
//      finalDegrees
//    } catch {
//      case e: Exception => LOGGER.error(s"getDegrees error appkey is ${appQuota.appkey} spanname is ${appQuota.method}", e)
//        List()
//    }
//  }
//
//
//  def getEnvDesc(env: Int) = {
//    val onlineOrOfflineStr = if (CommonHelper.isOffline) {
//      "线下"
//    } else {
//      "线上"
//    }
//    s"$onlineOrOfflineStr${EnvMap.getAliasEnv(env)}环境"
//  }
//
//  def getWarningMessage(appQuota: AppQuotaRow, quotaWarning: QuotaWarning) = {
//    val env = appQuota.env
//    val envDes = getEnvDesc(env)
//
//    val domain = ServiceCommon.OCTO_URL
//    val detailUrl = s"$domain/serverOpt/operation?appkey=${appQuota.appkey}#thriftCutFlow"
//    val ackUrl = s"$domain/service/cutFlow/${appQuota.id}/${quotaWarning.consumer}/ack/warn"
//
//    s"一键截流提醒($envDes)\n客户端服务(${quotaWarning.consumer})实际流量已超过配额的${WARNING_PERCENT * 100}%: \n【服务名称】: ${appQuota.appkey} \n【方法名称】: ${appQuota.method}" +
//      s"\n【流量配额】: ${quotaWarning.quotaThreshold.toLong}\n【流量均值】: ${quotaWarning.realQps.toLong}(最近5分钟) \n请联系服务提供方及时修改配额, 以免出现流量被截断影响服务使用。\n" +
//      s"[详情|$detailUrl] [ACK|$ackUrl]"
//  }
//
//  /**
//    * 生成一键截流报警消息内容
//    *
//    * @param appQuota
//    * @return
//    */
//  def getAlarmMessage(appQuota: AppQuotaRow, degrees: List[JsonConsumerRatioRow]) = {
//    val env = appQuota.env
//    val envDes = getEnvDesc(env)
//
//    val domain = ServiceCommon.OCTO_URL
//    val ackUrl = s"$domain/service/cutFlow/${appQuota.id}/ack/cut"
//    val detailUrl = s"$domain/serverOpt/operation?appkey=${appQuota.appkey}#thriftCutFlow"
//    val relevantConsumer = degrees.zipWithIndex.map {
//      case (degree, index) =>
//        s"${index + 1}, ${degree.consumerAppkey} (截流比例: ${degree.qpsRatio})\n"
//    }.mkString
//
//    s"一键截流报警($envDes)\n服务(${appQuota.appkey})的方法(${appQuota.method})发生过载，将会被降级。" +
//      s"\n服务提供者: ${appQuota.appkey}\n相关消费者: \n$relevantConsumer[详情|$detailUrl] [ACK|$ackUrl]"
//  }
//
//
//  /**
//    * 更新截流配置时, 提醒相应负责人
//    *
//    */
//  def getNotifyMessage(appQuota: AppQuotaRow) = {
//    val operatorDesc = try {
//      val user = UserUtils.getUser
//      user.getName
//    } catch {
//      case e: Exception =>
//        LOGGER.error("get user failed", e)
//        ""
//    }
//
//    val env = appQuota.env
//    val envDes = getEnvDesc(env)
//
//    val domain = ServiceCommon.OCTO_URL
//    val detailUrl = s"$domain/serverOpt/operation?appkey=${appQuota.appkey}#thriftCutFlow"
//    val testDesc = if (appQuota.testStatus == 0) "测试模式" else "非测试模式"
//    val enableDesc = if (appQuota.degradeStatus == 0) "启用" else "停用"
//    s"一键截流通知($envDes)\n【通知内容】：编辑一键截流配置成功 \n【操作用户】：$operatorDesc\n【服务名称】：${appQuota.appkey}\n【方法名称】：${appQuota.method}\n【单机容量】：${appQuota.hostQpsCapacity}\n" +
//      s"【集群容量】：${appQuota.clusterQpsCapacity}\n【测试模式】：$testDesc\n【是否启用】：$enableDesc\n[详情|$detailUrl]"
//  }
//
//  /**
//    * 判断客户端配额是否存在不够用的情况
//    *
//    * @param consumerQuotaConfigs 客户端配额
//    * @param records
//    */
//  def hasEnoughQuota(consumerQuotaConfigs: List[ConsumerQuotaConfigRow], records: List[DataQuery.DataRecord]) = {
//    val consumerConfigs = consumerQuotaConfigs.filterNot(_.consumerAppkey == OTHERS)
//    val qpsList = records.filter(x => x.tags.remoteApp.getOrElse("") != ALL)
//
//    consumerConfigs.map {
//      config =>
//        val relevantRecordsOpt = qpsList.find(x => config.consumerAppkey == x.tags.remoteApp.getOrElse(""))
//        relevantRecordsOpt match {
//          case Some(relevantQpsList) =>
//            val relevantQpsList = relevantRecordsOpt.get.qps.map(_.y.getOrElse(0.0)).reverse.drop(1)
//            val quotaThreshold = config.clusterQuota
//            val averageQps = relevantQpsList.sum / relevantQpsList.length
//            if (averageQps > quotaThreshold * WARNING_PERCENT && averageQps < quotaThreshold) {
//              Some(QuotaWarning(config.consumerAppkey, averageQps, quotaThreshold, config.ackStatus))
//            } else {
//              None
//            }
//          case None => None
//        }
//    }.filter(_.isDefined).map(_.get)
//  }
//
//  def monitorCutFlow(): Unit = {
//    ServiceCutFlowDAO.getAllQuotaConfigs.foreach { item =>
//      val appQuota = item._1
//      val consumerQuotaConfigs = item._2
//      val consumerAppkeys = consumerQuotaConfigs.map(_.consumerAppkey)
//      val records = getQps(appQuota)
//      val degrees = getDegrees(appQuota, consumerQuotaConfigs, consumerAppkeys, records)
//
//      // 删除之前所有的consumer quota策略
//      ServiceCutFlowDAO.deleteConsumerQuota(appQuota.id)
//      // 写入consumer quota策略
//      degrees.foreach { degree =>
//        ServiceCutFlowDAO.doAddUpdateConsumer(degree)
//      }
//
//      if (appQuota.testStatus == 1) {
//        if (!degrees.isEmpty) {
//          LOGGER.warn(s"${appQuota.appkey}-${appQuota.env}-${appQuota.method} degrees are $degrees")
//          records.foreach { record =>
//            LOGGER.warn(s"${appQuota.appkey}-${appQuota.env}-${appQuota.method} tags are ${record.tags}, qps are ${record.qps}")
//          }
//          JMonitor.add("octo.msgp.cutflow")
//        }
//        // 写入ZK
//        ServiceCutFlowDAO.wrtCutFlowMsgToZK(appQuota.id)
//      }
//
//      // 检查是否报警
//      if (OverloadDegrade.checkAlarm(appQuota.env, appQuota.appkey, appQuota.method)) {
//        if (appQuota.ackStatus == 0) {
//          // alarm
//          val msg = getAlarmMessage(appQuota, degrees)
//          OverloadDegrade.cutFlowAlarm(List(appQuota.appkey), msg)
//        }
//      } else {
//        // 重置ack
//        ServiceCutFlowDAO.renewCutAck(appQuota.id)
//        //没有报警且非测试模式下下才提醒
//        if (appQuota.testStatus == 1) {
//          val quotaWarnings = hasEnoughQuota(consumerQuotaConfigs, records)
//          quotaWarnings.foreach {
//            quotaWarning =>
//              //如果可以接受报警
//              if (quotaWarning.ackStatus == 0) {
//                val msg = getWarningMessage(appQuota, quotaWarning)
//                OverloadDegrade.cutFlowAlarm(List(appQuota.appkey, quotaWarning.consumer), msg)
//              }
//          }
//          val warnConsumerAppkeys = quotaWarnings.map(_.consumer)
//          //更新所有没有超额的consumer的askStatus为可接受报警的状态
//          ServiceCutFlowDAO.renewWarnAck(appQuota.id, warnConsumerAppkeys)
//        }
//      }
//    }
//  }
//
//  def start() {
//    scheduler.scheduleAtFixedRate(new Runnable {
//      def run(): Unit = {
//        try {
//          val checkBegin = System.currentTimeMillis()
//          monitorCutFlow()
//          val checkEnd = System.currentTimeMillis()
//          LOGGER.info(s"It costs ${(checkEnd - checkBegin) / 1000L}s in one round of cutflow")
//          JMonitor.add("octo.msgp.cutflow.roundtime", (checkEnd - checkBegin))
//        } catch {
//          case e: Exception => LOGGER.error(s"CutFlow checkAll failed", e)
//        }
//      }
//    }, 0, 20, TimeUnit.SECONDS)
//  }
//}
