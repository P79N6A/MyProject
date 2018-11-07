package com.sankuai.octo.mnsc.utils

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.fasterxml.jackson.databind.{ObjectWriter, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.apache.commons.codec.binary.Base64
import play.api.libs.json.Json

object api {
  /** ObjectMapper is thread safe */
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  def errorJsonArgInvalid(msg: AnyRef):String ={
    errorJson(400,msg)
  }

  def dataJson200(data: AnyRef):String={
    dataJson(200,data)
  }

  def dataJson(errorCode:Int,data: AnyRef):String={
    val map = Map("ret" -> errorCode, "data" -> data)
    jsonStr(map)
  }

  def dataJson(errorCode:Int, errorMsg:String, data: AnyRef):String={
    val map = Map("ret" -> errorCode, "msg" -> errorMsg, "data" -> data)
    jsonStr(map)
  }

  def errorJson(errorCode: Int, msg: AnyRef): String = {
    val map = Map("ret" -> errorCode, "msg" -> msg)
    jsonStr(map)
  }

  def dataJson(data: AnyRef): String = {
    val map = Map("data" -> data, "isSuccess" -> true)
    jsonStr(map)
  }

  def jsonStr(data: AnyRef): String = {
    new String(jsonBytes(data), "utf-8")
  }

  def jsonBytes(data: AnyRef): Array[Byte] = {
    mapper.writeValueAsBytes(data)
  }

  def toObject[T](bytes: Array[Byte], valueType: Class[T]) = {
    mapper.readValue(bytes, valueType)
  }

  def toObject[T](str: String, valueType: Class[T]) = {
    mapper.readValue(str, valueType)
  }

  def authorization(uri: String, method: String, date: String, clientId: String, secret: String): String = {
    val signKey = new SecretKeySpec(secret.getBytes("utf-8"), "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(signKey)
    val text = method + " " + uri + "\n" + date
    "MWS " + clientId + ":" + Base64.encodeBase64String(mac.doFinal(text.getBytes("utf-8")))
  }
}
