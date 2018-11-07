package com.sankuai.octo.service

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import akka.actor.{ActorRef, ActorSystem, Props}
import com.sankuai.octo.service.actors.agentCheckActor
import com.sankuai.octo.service.messager.{MODE, Alarm}
import com.sankuai.octo.scanner.Common
import com.sankuai.octo.scanner.falcon.{FalconItem, ReportUtils}
import com.sankuai.octo.scanner.model.Provider
import com.sankuai.octo.scanner.service.{ScannerTasks, LeaderElection}
import com.sankuai.octo.scanner.service.impl.ScanServiceImpl
import com.sankuai.octo.scanner.util.ScanUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._


object agentCheckerAKKA {
  private val LOG: Logger = LoggerFactory.getLogger(agentCheckerAKKA.getClass)
  private val system = ActorSystem("scannerActorSystem");
  val actorMap = scala.collection.mutable.Map[String, ActorRef]()

  def checkSleep = {
    //默认5分钟扫描一次（线上：5分钟，线下：10分钟）
    config.get("agent.check.sleep", "300000").toLong
  }

  val scheduler = Executors.newScheduledThreadPool(2)

  def start() {
    init()
    scheduler.scheduleWithFixedDelay(new Runnable {
      def run(): Unit = {
        try {
          if (LeaderElection.isMaster) {
            reportFalcon();
            if (ScanServiceImpl.scanSgAgent)
              checkAll()
          }
        } catch {
          case e: Exception => LOG.error(s"checkAll $e")
        }
      }
    }, 10000, checkSleep, TimeUnit.MILLISECONDS)
  }

  val whitelist = scala.collection.mutable.Map[String, Int]()

  def init() {
    config.get("agent.whitelist", "inf-").split(",").foreach(whitelist.put(_, 0))
    LOG.info(s"after init $whitelist")
    FalconItem.failNum = new AtomicInteger();
    FalconItem.pingFailNum = new AtomicInteger();
    FalconItem.totalNum = new AtomicInteger(1);
  }

  def checkAll() {
    val ipPortMap = new java.util.HashMap[String, java.util.HashSet[String]]()
    val envs = List("prod", "stage", "test")
    val list = envs.foreach {
      env =>
        val providerListPath = s"/mns/sankuai/$env/${Common.agentAppkey}/provider"
        val nodes = zk.children(providerListPath).asScala.toArray

        var newlists: Array[String] = null
        if (Common.isOnline) {
          if (ScanUtils.hostIpPrefix.startsWith(Common.cqIpPrefix)) {
            newlists = nodes.filterNot(x => !Common.beijingIpPrefixSet.contains(ScanUtils.getProviderIpPrefix(x)) ||
              x.startsWith(Common.yfIpPrefix) || x.startsWith(Common.dxIpPrefix))
          }
          else if (ScanUtils.hostIpPrefix.startsWith(Common.gqIpPrefix)) {
            newlists = nodes.filter(x => !Common.beijingIpPrefixSet.contains(ScanUtils.getProviderIpPrefix(x)))
          } else {
            newlists = nodes.filter(x => ScanUtils.isSameDC(x))
          }
        }
        newlists.foreach(
          node => {
            val provider: Provider = getProvider(env, node)
            val providerZKPath = providerListPath + "/" + node
            provider.setServerPath(providerZKPath)
            //配置newCloudCheck=false，排除新办公云agent扫描
            if (!Common.isOnline && !ScanServiceImpl.newCloudCheck && provider.getIp.startsWith("10.")) {
              LOG.info("skip agent:" + provider.getIdentifierString)
            } else {
              if (!actorMap.contains(providerZKPath)) {
                val agentActor = system.actorOf(Props(new agentCheckActor(provider)))
                actorMap.put(providerZKPath, agentActor)
                agentActor ! Common.checkStatus
                LOG.info("actorMap size:" + actorMap.size, " agent:" + provider.getIdentifierString)
              } else {
                val agentActor = actorMap.apply(providerZKPath)
                agentActor ! Common.checkStatus
              }
            }
            checkDuplicateAgent(ipPortMap, getProvider(env, node))
          }
        )
    }
  }

  def checkDuplicateAgent(ipPortMap: java.util.HashMap[String, java.util.HashSet[String]], provider: Provider) =
    ScannerTasks.checkDuplicateRegistry(ipPortMap, provider.getIdentifierString, provider.getIpPort)

  // TODO: 优化 函数返回方式
  def getProvider(env: String, node: String): Provider = {
    val provider = new Provider()
    provider.setIp(node.split(":").apply(0))
    provider.setPort(node.split(":").apply(1).toInt)
    provider.setEnv(envFormat(env))
    provider.setAppkey(Common.agentAppkey)
    return provider
  }

  def envFormat(env: String) = {
    env match {
      case "prod" => 3
      case "stage" => 2
      case "test" => 1
    }
  }

  def reportFalcon(): Unit = {

    val totalNum = FalconItem.totalNum.get();
    val failNum = FalconItem.failNum.get();
    val pingFailNum = FalconItem.pingFailNum.get();
    val successNum = totalNum - failNum;
    val successRate = successNum * 100.0f / totalNum;

    //线上才上报falcon
    if (Common.isOnline) {
      ReportUtils.addItem("scanner.sg_agent.totalNum", totalNum.toString)
      ReportUtils.addItem("scanner.sg_agent.failNum", failNum.toString)
      ReportUtils.addItem("scanner.sg_agent.pingFailNum", pingFailNum.toString)
      ReportUtils.addItem("scanner.sg_agent.successNum", successNum.toString)
      ReportUtils.addItem("scanner.sg_agent.successRate", successRate.toString)
      ReportUtils.report()
    }

    FalconItem.failNum = new AtomicInteger();
    FalconItem.pingFailNum = new AtomicInteger();
    FalconItem.totalNum = new AtomicInteger(1);
  }


  def main(args: Array[String]) {
    start
  }

}
