package com.sankuai.octo.statistic.helper

import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonParser.Feature
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object api {

  /** ObjectMapper is thread safe */
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.setSerializationInclusion(Include.NON_NULL)
  mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)


  def dataJson(data: AnyRef): String = {
    val map = Map("data" -> data, "isSuccess" -> true)
    jsonStr(map)
  }

  def jsonStr(data: AnyRef): String = {
    new String(jsonBytes(data), StandardCharsets.UTF_8)
  }

  def jsonBytes(data: AnyRef): Array[Byte] = {
    mapper.writeValueAsBytes(data)
  }

  def bytesToObject[T](bytes: Array[Byte], valueType: Class[T]) = {
    mapper.readValue(bytes, valueType)
  }

  def toObject[T](str: String, valueType: Class[T]) = {
    mapper.readValue(str, valueType)
  }

  def toComplexList[T](str: String, valueType: TypeReference[T]) = {
    mapper.readValue(str, valueType)
  }

  def toComplexList[T](str: Array[Byte], valueType: TypeReference[T]) = {
    mapper.readValue(str, valueType)
  }

}