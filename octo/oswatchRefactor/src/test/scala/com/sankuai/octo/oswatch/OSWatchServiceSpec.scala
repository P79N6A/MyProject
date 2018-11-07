package com.sankuai.octo.oswatch

import com.sankuai.octo.oswatch.server.MTThriftServer
import com.sankuai.octo.oswatch.thrift.data.{MonitorPolicy, EnvType}
import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by dreamblossom on 15/10/3.
 */
class OSWatchServiceSpec  extends FlatSpec with Matchers{
  {
    val oswatchServiceImpl = MTThriftServer.oswatchServiceImpl
    // addMonitorPolicy
   // val oswatchMonitorPolicy = new MonitorPolicy(1L,"com.sankuai.chenxi.test_provider_a",EnvType.findByValue(1),true,false,30).setIdc("dx").setQps(20)
   // val response = oswatchServiceImpl.addMonitorPolicy(oswatchMonitorPolicy,"responseUrl")
   // println("Add MonitorPolicy: ", response)

    //delMonitorPolicy
   // oswatchServiceImpl.delMonitorPolicy(response.getOswatchId)
  }
}
