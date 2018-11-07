package com.sankuai.octo.msgp.serivce.sgagent

import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.sgagent.thrift.model.{SGAgent, Switch, SwitchRequest}
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsError, Json}



/**
  * Created by lhmily on 06/08/2016.
  */
object SgAgentSwitch {
  private val LOG: Logger = LoggerFactory.getLogger(SgAgentSwitch.getClass)

  case class SwitchSGAgent(isOpen: Boolean, switchName: String, ips: List[String])

  case class SwitchSGAgentItem(ip: String, errcode: Int, msg: String)

  case class SwitchSGAgentReponse(successList: List[SwitchSGAgentItem], failureList: List[SwitchSGAgentItem])

  implicit val SwitchSGAgentReads = Json.reads[SwitchSGAgent]
  implicit val SwitchSGAgentWrites = Json.writes[SwitchSGAgent]





  def switchSGAgent(json: String) = {
    Json.parse(json).validate[SwitchSGAgent].fold({ error =>
      LOG.info(error.toString)
      JsonHelper.errorJson("内部异常: " + JsError.toFlatJson(error).toString())
    }, {
      switchItem =>
        val list = handleSwitchAgent(switchItem)
        val (successList, failureList) = list.partition(0 == _.errcode)
        JsonHelper.dataJson(SwitchSGAgentReponse(successList, failureList))
    })
  }

  private def handleSwitchAgent(switchItem: SwitchSGAgent) = {
    var transport: TTransport = null
    val timeout = 3000
    val switchType = Switch.valueOf(switchItem.switchName).getValue
    switchItem.ips.map { ip =>
      var ret = new SwitchSGAgentItem(ip, -1, "")
      try {
        transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
        val protocol: TProtocol = new TBinaryProtocol(transport)
        val agent = new SGAgent.Client(protocol)
        transport.open
        val req = new SwitchRequest()
        req.setKey(switchType).setSwitchName(switchItem.switchName)
          .setValue(switchItem.isOpen).setVerifyCode("agent.octo.sankuai.com")
        val rep = agent.setRemoteSwitch(req)
        ret = ret.copy(errcode = rep.errcode, msg = rep.msg)
      } catch {
        case e: Exception =>
          ret = ret.copy(errcode = -1, msg = "cannot connect to sg_agent")
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

  def getSwitchTypes() = {
    Switch.values().sortBy(_.toString)
  }
}
