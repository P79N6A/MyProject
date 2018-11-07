package com.sankuai.octo.mworth

import com.sankuai.msgp.common.service.org.OpsService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.test.context.ContextConfiguration

/**
  * Created by yves on 16/7/7.
  */

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath*:applicationContext.xml",
  "classpath*:webmvc-config.xml",
  "classpath*:applicationContext-*.xml",
  "classpath*:mybatis*.xml"))
class worthEventCountDailySuite extends FunSuite with BeforeAndAfter {

//  test("queryFree") {
//    val start = new DateTime("2016-07-05")
//    mWorthDailyService.queryFree(new Date(start.withTimeAtStartOfDay().getMillis),-1,"hanjiancheng",0,"选择全部")
//  }

  /*test("queryTotal") {
    val start = new DateTime("2016-06-19")
    val end = new DateTime("2016-07-19")
    val data = worthEventCountDaily.queryUsersTotal(new Date(start.withTimeAtStartOfDay().getMillis),new Date(end.withTimeAtStartOfDay().getMillis))
    println(data)
  }*/


  /*test("queryModule") {
    val start = new DateTime("2016-06-19")
    val end = new DateTime("2016-07-19")
    val result = borp.getAllEntityType("com.sankuai.waimai.poiquery",new Date(start.withTimeAtStartOfDay().getMillis),new Date(end.withTimeAtStartOfDay().getMillis))
    val size = result.size()
    if(size != 0){
      result.subList(0,12)
    }
  }*/


  /*test("queryBusiness") {
    val start = new DateTime("2016-06-20")
    val end = new DateTime("2016-07-20")
    mWorthDailyService.queryBusiness(new Date(start.withTimeAtStartOfDay().getMillis),new Date(end.withTimeAtStartOfDay().getMillis), 1,1)
  }*/

  /*test("orgAPI"){
    val result = orgapi.getAllOrgIdOfEmployee(2)
    val abc = result.slice(12,14)
    print(result)
  }*/


  /*test("queryCoverage") {
    val start = new DateTime("2016-06-19")
    val end = new DateTime("2016-07-19")
    val org = orgapi.getOrgByOrgId(100046)
    val emp = orgapi.employee("guochen06")
    mWorthDailyService.queryCoverage(new Date(start.withTimeAtStartOfDay().getMillis),new Date(end.withTimeAtStartOfDay().getMillis), 100046)
<<<<<<< HEAD
  }
*/

  /*test("employee"){
    //var aaa = orgapi.getSingelEmployeeByKeyWord("zining.wang")
    //var posname = orgapi.getEmployeePosName("zining.wang")
    //var bbb = orgapi.getEmployeeOrgId("zining.wang")
    var top = orgapi.getTopOrgName(2003454)
    println(top)
    //mWorthDailyService.updateBusinessInfo()
  }*/

  /*test("employee"){
   val start = new DateTime("2016-07-05")
   val emp = mWorthDailyService.updateOrgInfo()
  }*/

  /*test("provider"){
    val page = new Page()
    page.setPageSize(-1)
   val emp = serviceProvider.getOutlineofProvider("com.sankuai.inf.sg_agent",1,3,"",-1,page,-8)
    val idcList = emp.idcList
  }*/

  /*test("getOwtsbyUsername"){
    val result = ops.getOwtsbyUsername("tangye03")
    result.asScala.foreach(print(_))
  }*/


  /*test("getKeyRootOrg") {
    val list = orgapi.getOrgByOrgId(1)
    println(mWorthDailyService.getKeyRootOrg())
  }*/

  /*test("employee") {
    val result = orgapi.employee("gaoxiukun")
    println(result)
  }*/

  test("getOwtsbyUsername") {
    val result = OpsService.getOwtsbyUsername("yangdecheng")
    println(result)
  }
}
