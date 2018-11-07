package com.sankuai.octo.msgp.serivce.sgagent

import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.msgp.domain.AppkeyIps
import com.sankuai.sgagent.thrift.model.{SGAgent, SGAgentWorker}
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
/**
 * Created by lhmily on 07/15/2016.
 */
object SgAgentShutdown {
  private val LOG: Logger = LoggerFactory.getLogger(SgAgentShutdown.getClass)

  case class ShutdownSGAgentItem(ip: String, errcode: Int, msg: String)

  case class ShutdownSGAgentReponse(successList: List[ShutdownSGAgentItem], failureList: List[ShutdownSGAgentItem])


  def shutdownSgAgent(isWorker: Boolean, appkeyIps: AppkeyIps) = {
    val ips = appkeyIps.getIps.asScala.toList
    val list = handleShutdownAgent(isWorker, ips)
    val (successList, failureList) = list.partition(0 == _.errcode)
    JsonHelper.dataJson(ShutdownSGAgentReponse(successList, failureList))

  }

  private def handleShutdownAgent(isWorker: Boolean, ips: List[String]) = {
    var transport: TTransport = null
    val port = if (isWorker) 5267 else 5266
    val timeout = 3000
    val scre = "agent.octo.sankuai.com"

    ips.map { ip =>
      var ret = new ShutdownSGAgentItem(ip, -1, "")
      try {
        transport = new TFramedTransport(new TSocket(ip, port, timeout), 16384000)
        val protocol: TProtocol = new TBinaryProtocol(transport)

        ret = if (isWorker) {
          val agentWorker = new SGAgentWorker.Client(protocol)
          transport.open
          try {
            agentWorker.shutdown(scre)
            ret.copy(errcode = -1, msg = "connecting to sgagent_worker is success, but fail to shutdown.")
          } catch {
            case e: Exception =>
              e.printStackTrace()
              ret.copy(errcode = 0, msg = "success")
          }

        } else {
          val agent = new SGAgent.Client(protocol)
          transport.open
          try {
            agent.shutdown(scre)
            ret.copy(errcode = -1, msg = "connecting to sgagent is success, but fail to shutdown.")
          } catch {
            case e: Exception =>
              ret.copy(errcode = 0, msg = "success")
          }
        }
      } catch {
        case e: Exception =>
          ret = ret.copy(errcode = -1, msg = "connection is failure.")
      } finally {
        if (null != transport) {
          try {
            transport.close()
          } catch {
            case e: Exception =>
          }
        }
      }
      ret
    }
  }

}
