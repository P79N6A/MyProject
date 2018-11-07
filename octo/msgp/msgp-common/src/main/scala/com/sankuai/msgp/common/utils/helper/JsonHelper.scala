package com.sankuai.msgp.common.utils.helper

import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.sankuai.msgp.common.model.Page
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.libs.json.Json


object JsonHelper {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  val LOGGER = LoggerFactory.getLogger(this.getClass)

  {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    mapper.registerModule(DefaultScalaModule)
  }

  def jsonStr(data: Any): String = {
    new String(jsonBytes(data), "utf-8")
  }

  def jsonBytes(data: Any): Array[Byte] = {
    mapper.writeValueAsBytes(data)
  }

  def dataJson(data: Any): String = {
    val map = Map("data" -> data, "isSuccess" -> true)
    jsonStr(map)
  }

  def dataJson(data: Any, page: Page): String = {
    val map = Map("data" -> data, "page" -> page, "isSuccess" -> true)
    jsonStr(map)
  }

  def errorJson(message: String): String = {
    Json.obj("msg" -> message, "isSuccess" -> false).toString()
  }

  def errorDataJson(data: Any): String = {
    val map = Map("data" -> data, "isSuccess" -> false)
    jsonStr(map)
  }

  def errorDataJson(data: Any, exception: Exception): String = {
    val map = Map("data" -> data, "error" -> exception)
    jsonStr(map)
  }

  def errorCodeDataJson(data: Any, code: Integer): String = {
    val map = Map("data" -> data, "code" -> code, "isSuccess" -> false)
    jsonStr(map)
  }

  def toObject[T](bytes: Array[Byte], valueType: Class[T]) = {
    mapper.readValue(bytes, valueType)
  }

  def toObject[T](str: String, valueType: Class[T]) = {
    mapper.readValue(str, valueType)
  }

  def authDate(time: DateTime) = {
    time.toString("yy-MM-dd hh:mm:ss", Locale.ENGLISH)
  }

  def authorization(uri: String, method: String, date: String, clientId: String, secret: String): String = {
    val signKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(signKey)
    val text = method + " " + uri + "\n" + date
    "MWS " + clientId + ":" + Base64.encodeBase64String(mac.doFinal((text).getBytes("UTF-8")))
  }

  def authHeaders(uri: String, method: String, key: String, secret: String) = {
    val date = authDate(DateTime.now)
    Map("Date" -> date, "Authorization" -> authorization(uri, method, date, key, secret))
  }

  def isValidJson(data: String) ={
    val o = new ObjectMapper()
    try{
      o.readTree(data)
      true
    }catch {
      case e: Exception =>
        false
    }
  }
}
