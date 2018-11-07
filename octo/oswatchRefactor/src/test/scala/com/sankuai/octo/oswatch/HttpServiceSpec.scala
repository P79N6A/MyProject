package com.sankuai.octo.oswatch

import com.sankuai.octo.oswatch.db.Tables.OswatchMonitorPolicyRow
import com.sankuai.octo.oswatch.service.HttpService
import com.sankuai.octo.oswatch.thrift.data.{EnvType, MonitorPolicy}
import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by dreamblossom on 15/10/4.
 */
class HttpServiceSpec extends FlatSpec with Matchers{
  //case class OswatchMonitorPolicyRow(id: Long, appkey: String = "", idc: Option[String] = None, env: Int = 0, gtetype: Int = 0, watchperiod: Int = 0, monitorType: Int, monitorvalue: Double, spanName: Option[String] = None, responseurl: String = "")
  val oswatchMonitorPolicy= new OswatchMonitorPolicyRow(0L,"com.sankuai.inf.octo.msgp",None,3,0,30,2,30,None,"")
  HttpService.getAliveNode(oswatchMonitorPolicy) should be (2)
}
