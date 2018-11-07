package com.sankuai.octo

import com.sankuai.msgp.common.utils.StringUtil
import com.sankuai.octo.msgp.dao.appkey.AppkeyDescDao
import com.sankuai.octo.msgp.model.ServiceCategory
import com.sankuai.octo.msgp.serivce.data.Kpi
import com.sankuai.octo.msgp.serivce.service
import com.sankuai.octo.msgp.serivce.service.{AppkeyProviderService, ServiceDesc}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Created by zava on 16/8/30.
 */
@RunWith(classOf[JUnitRunner])
class KpiSuite extends FunSuite with BeforeAndAfter {

  test("refresh") {
    val now = DateTime.now()
    val startOfYesterday = now.minusDays(1).withTimeAtStartOfDay()
    Kpi.syncDay(ServiceDesc.appsName(), startOfYesterday)
    Thread.sleep(10000000)
  }

  test("category"){
    val serviceList = service.ServiceCommon.listService
    serviceList.par.foreach {
      x =>
        val thrift_provider = AppkeyProviderService.provider(x.appkey)
        val http_provider = AppkeyProviderService.httpProvider(x.appkey)
        val isThrift = if (thrift_provider.size > 0) true else false
        val isHttp = if (http_provider.size > 0) true else false
        val category = if (isHttp && isThrift) {
          ServiceCategory.BOTH.toString
        } else if (isHttp) {
          ServiceCategory.HTTP.toString
        } else if (isHttp) {
          ServiceCategory.THRIFT.toString
        } else {
          ServiceCategory.THRIFT.toString
        }
        if (StringUtil.isNotBlank(category)) {
          AppkeyDescDao.updateCategory(x.appkey, category)
          AppkeyDescDao.getCategory(x.appkey)
        }
    }
  }

}
