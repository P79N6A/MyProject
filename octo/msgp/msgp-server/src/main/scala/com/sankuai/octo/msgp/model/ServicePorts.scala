package com.sankuai.octo.msgp.model

/**
 * Created by lhmily on 01/05/2016.
 */
object ServicePorts extends Enumeration {

  type Ports = Value
  val sgagent = Value(5266, "5266")
  val kmsagent = Value(5269, "5269")
  val AgentMap = Map("com.sankuai.inf.sg_agent" -> 5266, "com.sankuai.inf.kms_agent" -> 5269)

  def getPort(agent_appkey: String) = {
    AgentMap.get(agent_appkey).getOrElse(5266)
  }

}

