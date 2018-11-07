package com.sankuai.octo

import java.security.MessageDigest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.config.model.{ConfigFile, file_param_t}
import com.sankuai.octo.config.service.MtConfigService
import com.sankuai.octo.msgp.serivce.service.{ServiceCommon, ServiceConfig}
import com.sankuai.msgp.common.utils.client.Messager
import com.sankuai.msgp.common.utils.client.Messager.{Alarm, MODE}
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.{TFramedTransport, TSocket}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class ApiSuite extends FunSuite with BeforeAndAfter {
  private val log = LoggerFactory.getLogger(this.getClass)

  test("test") {
    val env = ServiceConfig.getEnvByNodename("com.sankuai.octo.tmy.test.cell02[cell]", "com.sankuai.octo.tmy")
    println(env)
    val env1 = ServiceConfig.getEnvByNodename("com.sankuai.octo.tmy.test", "com.sankuai.octo.tmy")
    println(env1)
  }

  test("data") {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    val a = mapper.writeValueAsString(ServiceCommon.listService)
    println(a)

    println(JsonHelper.dataJson(ServiceCommon.listService))
  }

  test("messager") {
    val modes = Seq(MODE.XM, MODE.SMS, MODE.MAIL)
    val ownerIdList = Seq(41081)
    val alarm = Alarm(s"octo报警：test", "线下test", "testurl")
    Messager.sendAlarm(ownerIdList, alarm, modes)
  }

  test("sendmessage"){
   val triggerSubs = (List(24544, 54379, 2052137),List(),List())
    val  alarm = Alarm("OCTO报警：业务指标","业务指标 5分钟分账错误数(minute.metric/business=order,category=orderCountFail,type=status) 当前值:84.0 基线值:0.0 上升百分比:无穷大 大于 20%",null, null)
    Messager.sendAlarm(triggerSubs._1, alarm, Seq(MODE.XM))
    Messager.sendAlarm(triggerSubs._2, alarm, Seq(MODE.SMS))
    Messager.sendAlarm(triggerSubs._3, alarm, Seq(MODE.MAIL))
  }
  test("mtconfig-set") {
    val ip = "192.168.12.207"
    val transport = new TFramedTransport(new TSocket(ip, 9002, 500), 16384000)
    val protocol = new TBinaryProtocol(transport)
    val client = new MtConfigService.Client(protocol)
    transport.open()

    val fileContent = "这是一个测试文件！".getBytes()
    val md5 = MessageDigest.getInstance("MD5")
    val tmp = md5.digest(fileContent)
    val md5String = new java.math.BigInteger(1, tmp).toString(16)

    val configFiles = new ConfigFile()
    configFiles.setFilename("msgp.conf")
    configFiles.setFilepath("/opt/meituan/apps/mcc/com.sankuai.inf.msgp/")
    configFiles.setMd5(md5String)
    configFiles.setFilecontent(fileContent)
    println(md5String)

    val files = new file_param_t()
    files.setAppkey("com.sankuai.inf.msgp")
    files.setEnv("stage")
    files.setPath("")
    files.setConfigFiles(List(configFiles).asJava)

    println(client.setFileConfig(files))
  }

  test("mtconfig-get") {
    val ip = "192.168.12.207"
    val transport = new TFramedTransport(new TSocket(ip, 9002, 500), 16384000)
    val protocol = new TBinaryProtocol(transport)
    val client = new MtConfigService.Client(protocol)
    transport.open()

    val configFiles = new ConfigFile()
    configFiles.setFilename("msgp.conf2")

    val files = new file_param_t()
    files.setAppkey("com.sankuai.inf.msgp")
    files.setEnv("prod")
    files.setPath("")
    files.setConfigFiles(List(configFiles).asJava)
    val ret = client.getFileConfig(files)
    val retFile = ret.getConfigFiles
    println(ret)
    println(retFile)
    println(new String(retFile.asScala(0).getFilecontent))
  }

  test("mtconfig-getfilenamelist") {
    val ip = "192.168.12.207"
    val transport = new TFramedTransport(new TSocket(ip, 9002, 500), 16384000)
    val protocol = new TBinaryProtocol(transport)
    val client = new MtConfigService.Client(protocol)
    transport.open()

    val files = new file_param_t()
    files.setAppkey("com.sankuai.inf.msgp")
    files.setEnv("prod")
    files.setPath("")
    val ret = client.getFileList(files)
    val retFile = ret.getConfigFiles
    println(ret)
    println(retFile)
  }

  test("test error") {
    log.error("test error")
  }
}
