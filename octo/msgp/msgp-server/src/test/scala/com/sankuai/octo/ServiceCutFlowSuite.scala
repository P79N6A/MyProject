package com.sankuai.octo

import com.sankuai.msgp.common.config.db.msgp.Tables
import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO
import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO.{CutFlowRatio, QuotaWarning}
//import com.sankuai.octo.msgp.serivce.service.ServiceCutFlow
import org.scalatest.{BeforeAndAfter, FunSuite}
import scala.collection.JavaConverters._


/**
  * Created by yves on 17/1/19.
  */
class ServiceCutFlowSuite extends FunSuite with BeforeAndAfter {

  test("testCutFlow") {
    /**
      *  注意, 测试的服务appkey 为 com.sankuai.inf.logCollector.cutFlow, 需要将
      *  getAllQuotaConfigs中appQuotas限定为 com.sankuai.inf.logCollector.cutFlow
      */
    //ServiceCutFlow.monitorCutFlow
  }

  test("getAllQuotaConfigs"){
    val allConfigs = ServiceCutFlowDAO.getAllQuotaConfigs
    println(allConfigs)
  }

  test("hasEnoughQuota"){
//    ServiceCutFlowDAO.getAllQuotaConfigs.foreach { item =>
//      val appQuota = item._1
//      val consumerQuotaConfigs = item._2
//      val records = ServiceCutFlow.getQps(appQuota)
//      val quotaWarning = ServiceCutFlow.hasEnoughQuota(consumerQuotaConfigs, records)
//      println(quotaWarning)
//    }
  }

  test("delCutFlowAppQuota"){
    ServiceCutFlowDAO.delCutFlowAppQuota(524,"strategy");
    ServiceCutFlowDAO.delCutFlowAppQuota(523,"simple");
  }

  test("getProQuotaInf"){
    val inf1 = ServiceCutFlowDAO.getProQuotaInf(523L)
    println(inf1)
    val inf2 = ServiceCutFlowDAO.getProQuotaInf(5233L)
    println(inf2)
  }

  test("getAlarmMessage") {
    val appQuota = Tables.AppQuotaRow(535, "", "com.sankuai.inf.msgp", 3, "ApiController.scannerReport",
      0, 1, 1, 0, 0, 1525241829, 1525932581, 0, 0, 500, 2000, 0, 0, "wuxinyu", 1525919147)
  }

  def getAppQuotaRow() = {
    Tables.AppQuotaRow(535, "", "com.sankuai.inf.msgp", 3, "ApiController.scannerReport", 0, 1, 1, 0, 0,
      1525241829, 1525932581, 0, 0, 500, 2000, 0, 0, "wuxinyu", 1525919147)
  }

  def getJsonConsumerRatioHistoryRowList() = {
    List(CutFlowRatio(0,535,"unknownService",1.0,0,1525241829,1525932581,0,"110.1,110.2,110.3,110.5",500,2000,1200)).asJava
  }

  def getQuotaWarning() = {
    QuotaWarning("ApiController.scannerReport", 200, 250, 0)
  }

  def getConsumerQuotaConfigs() =  {
    List(Tables.ConsumerQuotaConfigRow(0, 535, "unknownService", 1300, 0, 0, None, "", 0, 1525327497),
      Tables.ConsumerQuotaConfigRow(0, 535, "others", 700, 0, 0, None, "", 0, 1525327497)).asJava
  }
}
