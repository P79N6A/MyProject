package com.sankuai.octo.msgp.serivce.overload

import java.util.concurrent.TimeUnit

import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.octo.oswatch.thrift.data._
import com.sankuai.octo.oswatch.thrift.service.OSWatchService
import org.slf4j.{Logger, LoggerFactory}

import scala.util.control.Breaks._
/**
 * Created by dreamblossom on 15/10/5.
 */
object OswatchService {
  val LOG: Logger = LoggerFactory.getLogger(OswatchService.getClass)
  private val oswatchClient = OswatchThriftClient.getClient[OSWatchService, OSWatchService.Iface]("com.sankuai.inf.octo.oswatch", new OSWatchService)

  def addMonitorQuota(monitorPolicy: MonitorPolicy) = {
    //common.isOffline 目前该方法有局限
    val responseUrl = CommonHelper.isOffline match {
      case true => "http://octo.test.sankuai.com/api/oswatchService/MonitorPolicyPost"
      //case true => "http://172.30.6.197:8080//api/oswatchService/MonitorPolicyPost"  //本地测试
      case false => "http://octo.sankuai.com/api/oswatchService/MonitorPolicyPost"
    }
   LOG.info("addMonitorQuota is called")
    var oswatchId= 0L
      breakable {
        for(i<- 0 to 2){
          oswatchId = OswatchThriftClient.tryThenCatch({oswatchClient.fold(0L)(x => x.addMonitorPolicy(monitorPolicy, responseUrl).getOswatchId)}) (e => {println(e); 0L})
          print("oswatchId",oswatchId)
          if(oswatchId!=0L) break
          TimeUnit.SECONDS.sleep(2)
        }
      }
    oswatchId
  }

    def delMonitorQuota(oswatchId: Long): Unit = {
      OswatchThriftClient.tryThenCatch({
        oswatchClient.foreach(_.delMonitorPolicy(oswatchId));
        println("delMonitorQuota OK")
      })(e => println(e))
    }
}