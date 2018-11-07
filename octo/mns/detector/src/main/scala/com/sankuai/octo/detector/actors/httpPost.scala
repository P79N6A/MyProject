package com.sankuai.octo.detector.actors

import com.alibaba.fastjson.JSONObject
import dispatch.{url, Http}

object httpPost {

  private implicit val ec = ExecutionContextFactory.build(2)

  def post(targetUrl: String, jsonObject: JSONObject, charset: String){
    val postReq = url(targetUrl).POST.setContentType("application/json", charset)
    Http(postReq.setBody(jsonObject.toJSONString) )
  }

}
