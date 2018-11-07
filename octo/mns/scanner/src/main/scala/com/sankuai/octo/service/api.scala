package com.sankuai.octo.service

import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime
import play.api.libs.json.Json

object api {

  def jsonStr(data: Any): String = {
    new String(jsonBytes(data), "utf-8")
  }

  def jsonBytes(data: Any): Array[Byte] = {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    mapper.registerModule(DefaultScalaModule)
    mapper.writeValueAsBytes(data)
  }

  def dataJson(data: Any): String = {
    val map = Map("data" -> data, "isSuccess" -> true)
    jsonStr(map)
  }

  def errorJson(message: String): String = {
    Json.obj("msg" -> message, "isSuccess" -> false).toString()
  }

  def authDate(time: DateTime) = {
    time.toString("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
  }

  def authorization(uri: String, method: String, date: String, clientId: String, secret: String): String = {
    val signKey = new SecretKeySpec(secret.getBytes("utf-8"), "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(signKey)
    val text = method + " " + uri + "\n" + date
    "MWS " + clientId + ":" + Base64.encodeBase64String(mac.doFinal((text).getBytes("utf-8")))
  }

  def authHeaders(uri: String, method: String, key: String, secret: String) = {
    val date = authDate(DateTime.now)
    Map("Date" -> date, "Authorization" -> authorization(uri, method, date, key, secret))
  }
}
