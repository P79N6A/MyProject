package com.sankuai.octo.oswatch.task

import akka.actor.{ActorLogging, Actor}
import com.sankuai.octo.oswatch.model._
import com.sankuai.octo.oswatch.service.HttpService

/**
 * Created by dreamblossom on 15/10/3.
 */
class MonitorPolicyExecutorActor extends  Actor with  ActorLogging{
  def receive = {
    case ExecutorMail.TellRegister(oswatchMonitorPolicy,monitorTypeValue)=>
         HttpService.tellRegister(oswatchMonitorPolicy.responseurl,oswatchMonitorPolicy.id,monitorTypeValue )
    case _ =>
  }
}
