package com.sankuai.octo.oswatch.server

import akka.actor.{Props, ActorSystem}
import com.sankuai.octo.oswatch.task.{MonitorPolicyLoaderActor, MonitorPolicyExecutorActor}

/**
 * Created by xintao on 15/10/28.
 */
object ActorManager {
  val monitorSystem = ActorSystem("monitorSystem")
  val executor = monitorSystem.actorOf(Props[MonitorPolicyExecutorActor](new MonitorPolicyExecutorActor()), "executor")
  val loader = monitorSystem.actorOf(Props[MonitorPolicyLoaderActor](new MonitorPolicyLoaderActor()), "loader")
}
