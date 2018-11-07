package com.sankuai.octo.msgp.model

/**
  * Created by lhmily on 01/05/2016.
  */
object Appkeys extends Enumeration{
  type Appkeys=Value
  val sgagent=Value("com.sankuai.inf.sg_agent")
  val kmsagent=Value("com.sankuai.inf.kms_agent")
  val mcc= Value("com.sankuai.cos.mtconfig")
  val sgsentinel = Value("com.sankuai.inf.sg_sentinel")
  val msgp = Value("com.sankuai.inf.msgp")
}
