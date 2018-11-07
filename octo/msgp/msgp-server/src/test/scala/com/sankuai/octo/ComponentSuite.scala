package com.sankuai.octo

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.dao.component.ComponentDAO
import com.sankuai.octo.msgp.dao.component.ComponentDAO.{AppDescDomain, SimpleArtifact}
import com.sankuai.octo.msgp.domain.Dependency
import com.sankuai.octo.msgp.serivce.component._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.collection.JavaConverters._


/**
  * Created by yves on 16/8/8.
  */
@RunWith(classOf[JUnitRunner])
class ComponentSuite extends FunSuite with BeforeAndAfter {

  /*test("queryGroupIdByKeyword"){
    val start = new DateTime("2016-08-01")
    val end = new DateTime("2016-08-11")
   ComponentService.queryComponentChecklist("","","com.sankuai.octo", "idl-oswatch","")
  }*/

  test("getCategory") {
    val dependency = ComponentDAO.SimpleArtifact("com.alibaba", "fastjson", "")
    val result = ComponentHelper.getCategoryOfComponent(dependency)
    println(result)
  }

  test("activeness update") {
    val rawResult = ComponentDAO.getDetails("", "技术工程及基础数据平台", "inf", "octo", "", "", "").groupBy(x => (x.appGroupId, x.appArtifactId))
    val dependencyUploads = List[ComponentDAO.DependencyUpload]()
    val dependences = dependencyUploads.map {
      uploads =>
        val jsonValue = Json.toJson(uploads)
        Json.stringify(jsonValue)
    }
    dependences.zipWithIndex.map { case (element, index) =>
      ComponentHelper.uploadDependency(element)
    }
    Thread.sleep(1000000)
  }

  test("activeness") {
    ActivenessService.getAppActiveness("all", "技术工程及基础数据平台", "inf", "")
  }

  test("uploadBomInformation") {
    val testStr = "{\"appDesc\":{\"business\":\"\",\"owt\":\"inf\",\"pdl\":\"octo\",\"app\":\"msgp\",\"appkey\":\"true\",\"base\":\"beijing\",\"groupId\":\"com.sankuai.octo\",\"artifactId\":\"msgp\",\"version\":\"1.1.23\"},\"infBomUsed\":1,\"infBomVersion\":\"1.1.1\",\"xmdBomUsed\":1,\"xmdBomVersion\":\"1.1.0\"}"
    ComponentHelper.uploadBomInformation(testStr)
    Thread.sleep(60000)
  }

  test("uploadDailyTrend") {
    val date = DateTime.parse("2016-11-24").toDate
    TrendService.uploadDailyTrend(date)
  }

  test("sendMessage") {
    val dependencise = List(new Dependency("com.meituan.service.mobile", "mtthrift", "(,1.7.4)")).asJava
    val recommend_dependencies = List(new Dependency("com.meituan.service.mobile", "mtthrift", "1.8.0")).asJava
    ComponentService.sendMessage(0, "组件提醒", "", List("1").asJava, dependencise, recommend_dependencies, List("http://octo.sankuai.com/repservice/daily").asJava)
    Thread.sleep(10000000)
  }

  test("getAppVersionInRange"){
    val list = ComponentService.getAppVersionInRange("com.meituan.service.mobile", "mtthrift", "1.7.4").map(_.version).distinct.sortWith(_ > _)
    println(list)
  }

  test("checkIsDependency") {
    val artifact = SimpleArtifact("com.meituan.mobile", "trainapi-server", "")
    println(ComponentHelper.checkIsDependency(artifact))
    Thread.sleep(100000)
  }

  test("getOwtPdlList") {
    OpsService.refreshOwt
    OpsService.refreshPdls
    val result = AppConfigService.getOwtPdlList("", "餐饮生态", "", "")
    println(result.size)
    println(result)
    Thread.sleep(100000)
  }

  test("doBrokenArtifactsNotification") {
    val text = "{\"appDesc\":{\"business\":\"\",\"owt\":\"inf\",\"pdl\":\"octo\",\"groupId\":\"com.sankuai.octo\",\"artifactId\":\"data-query\",\"version\":\"0.3.0\",\"appkey\":\"\",\"base\":\"meituan\",\"app\":\"com.sankuai.inf.data.query\",\"packaging\":\"war\"},\"requiredBrokenCheckResults\":{\"artifactConfigs\":[],\"artifacts\":[]},\"brokenCheckResults\":{\"artifactConfigs\":[{\"groupId\":\"com.sankuai.meituan\",\"artifactId\":\"mtconfig-client\",\"version\":\"[1.2.25,);1.1.31.sec01,1.1.33.sec01,1.1.34.sec01,1.1.41.sec01,1.1.46.sec01,1.2.13.sec01,1.2.14.sec01,1.2.16.sec01,1.2.2.sec01,1.2.4.sec01,1.2.7.sec01,1.2.8.sec01\",\"action\":\"broken\"},{\"groupId\":\"com.sankuai.meituan\",\"artifactId\":\"mtthrift-abc\",\"version\":\"1.8.1\",\"action\":\"broken\"}],\"artifacts\":[{\"groupId\":\"com.sankuai.meituan\",\"artifactId\":\"mtconfig-client\",\"version\":\"1.1.2-20151230.084348-6\"},{\"groupId\":\"com.sankuai.meituan\",\"artifactId\":\"mtthrift-abc\",\"version\":\"1.7.1\"}]}}"
    ComponentHelper.doNotificationAction(text)
  }

  test("getRecommendedComponent") {
    val a = ComponentHelper.getRecommendedComponent("com.meituan.service.mobile", "mtthrift")
    print(a)
  }

  test("updateBusiness") {
    ComponentHelper.updateBusiness("其他","")
    Thread.sleep(100000)
  }

  test("getUsernameByAppDesc") {
    //val appDesc = AppDescDomain("", "", "", "meituan.pay.paycashier.mis", "", "meituan", "", "", "", "")
    val appDesc = AppDescDomain("", "", "", "neocortex-4j-service", "overseas-deal-api-web", "dianping", "", "", "", "")
    val usernames = ComponentHelper.getUsernameByAppDesc(appDesc)
    println(usernames)
  }

  test("getBlackListConfig") {
    OpsService.refreshOwt
    AppConfigService.getBlackListConfig("meituan", "inf", "octo", "msgp")
  }

  test("getVersionRangeSpec") {
    val exp1: String = "(1.1,1.5)"
    println(exp1 + ": " + ComponentHelper.getVersionDesc(exp1))
    //(1.1,1.5): 1.1 < version < 1.5
    val exp2: String = "(1.1,)"
    println(exp2 + ": " + ComponentHelper.getVersionDesc(exp2))
    //(1.1,): version > 1.1
    val exp3: String = "(,1.5);1.4.Fixed"
    println(exp3 + ": " + ComponentHelper.getVersionDesc(exp3))
    //(,1.5): version < 1.5
    val exp4: String = "(1.1,1.5),(1.7,1.9];1.4.Fixed"
    println(exp4 + ": " + ComponentHelper.getVersionDesc(exp4))
    // (1.1,1.5),(1.7,1.9]: 1.1 < version < 1.5 or 1.7 < version <= 1.9
    val exp5: String = "(1.1,1.5),(1.7,1.9],(1.7,1.9]"
    println(exp5 + ": " + ComponentHelper.getVersionDesc(exp5))
    // (1.1,1.5),(1.7,1.9],(1.7,1.9]: 1.1 < version < 1.5 or 1.7 < version <= 1.9 or 1.7 < version <= 1.9
    val exp6: String = "(,]"
    println(exp6 + ": " + ComponentHelper.getVersionDesc(exp6))
  }

  test("initRecommendedComponent") {
    val result = ComponentHelper.initRecommendedComponent(MsgpConfig.get("cpmt.recommended", ""))
    println(result)
  }
}

