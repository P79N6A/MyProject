package com.sankuai.octo

import java.util.Date

import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.client.BorpClient
import com.sankuai.msgp.common.model.MonitorModels.Trigger
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

//"添加", 1
//"更新", 2
//"删除", 3

@RunWith(classOf[JUnitRunner])
class borpClientSuite extends FunSuite with BeforeAndAfter {
  val appkey = "com.sankuai.inf.testRegistry11122"
  val page = new Page
  page.setPageSize(30)
  val startTime = new Date(new DateTime().minusDays(7).getMillis)
  val endTime = new Date
  test("borp_getOperation"){
    val operationLogs = BorpClient.getOptLogById(appkey, startTime, endTime, page)
    println(page.getTotalCount)
    println(operationLogs)
    val count = operationLogs.foldLeft(0){
      (count,x) =>
        println("====")
        println(x.actionType)
        println(x.entityType)
        println(x.operatorName)
        println(x.time)
        println(x.oldValue)
        println(x.newValue)
        println("====")
        count+ 1
    }
    println(count)
    println( BorpClient.getOptLogByOperatorId("-1024", startTime, endTime, page) )
  }

  test("date") {
    val opt = Json.parse("").validate[Trigger].asOpt
    if(opt.isEmpty)
      println("dd")
    else
      println("xxx")
    val a = new DateTime().getMillis
    println(a)
    val b = new Date(a)
    println(b)
    println(new DateTime().minusDays(7))
  }
}
