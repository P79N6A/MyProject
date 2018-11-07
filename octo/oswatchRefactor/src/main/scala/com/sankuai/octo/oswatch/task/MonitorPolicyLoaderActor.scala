package com.sankuai.octo.oswatch.task

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorLogging, ReceiveTimeout, Actor}
import com.sankuai.octo.oswatch.dao.MonitorPolicyDAO
import com.sankuai.octo.oswatch.model.{WatchActorMail, WatcherMail, LoaderMail}
import com.sankuai.octo.oswatch.utils.Common
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.Set
import scala.concurrent.duration._
import scala.util._
import akka.util._
/**
 * Created by dreamblossom on 15/9/30.
 */
class MonitorPolicyLoaderActor extends Actor with  ActorLogging {
   context.setReceiveTimeout(Duration(Common.checkInterval,SECONDS))
   var loop = false
   var monitorPolicyWatchingSet = Set[Long]()

   def receive = {
      case LoaderMail.Start =>
         log.info("Loader start.")
         loop = true
         loadMonitorPolicy()

      case LoaderMail.Stop =>
         log.info("Loader stop.")
         loop = false

      case WatchActorMail.quit(actorId) =>
        log.info(s"WatchActorMail.quit($actorId).")
        monitorPolicyWatchingSet -= actorId
        if (loop) loadMonitorPolicy()

      case ReceiveTimeout =>
         if (loop) loadMonitorPolicy()
   }

  def loadMonitorPolicy(): Unit ={
    val monitorPolicyList =  MonitorPolicyDAO.getAll
    log.info("monitorPolicyList:"+monitorPolicyList)
    if(monitorPolicyList!=null){

      monitorPolicyWatchingSet --= monitorPolicyWatchingSet.diff(monitorPolicyList.map { mp =>
        monitorPolicyWatchingSet.contains(mp.id) match {
         case false =>
           log.info(s"create new MonitorPolicyWatcherActor /user/${mp.id}")
           context.system.actorOf(Props[MonitorPolicyWatcherActor](new MonitorPolicyWatcherActor(mp)), mp.id.toString) ! WatcherMail.Start
           monitorPolicyWatchingSet += mp.id
           log.info("monitorPolicySet:"+monitorPolicyWatchingSet)
         case true =>
           log.info(s"use old MonitorPolicyWatcherActor /user/${mp.id}")
           //context.system.actorSelection("/user/" + mp.id)! WatcherMail.Update(mp)
           implicit val timeout = Timeout(FiniteDuration(Common.actorSelectionTimeout, TimeUnit.SECONDS))
           context.system.actorSelection("/user/" + mp.id).resolveOne().onComplete{
             case Success(ref) =>
               log.info("monitorPolicySet:"+monitorPolicyWatchingSet)
               ref ! WatcherMail.Update(mp)
             case Failure(ex) =>
               log.info(s"not found actor /user/${mp.id},so create new MonitorPolicyWatcherActor")
               context.system.actorOf(Props[MonitorPolicyWatcherActor](new MonitorPolicyWatcherActor(mp)), mp.id.toString) ! WatcherMail.Start
               monitorPolicyWatchingSet += mp.id
               log.info("monitorPolicySet:"+monitorPolicyWatchingSet)
           }
       }
     mp.id
    }.toSet)
      log.info("monitorPolicySet:"+monitorPolicyWatchingSet)
    }
  }
}
