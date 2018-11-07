package com.sankuai.octo

import java.security.MessageDigest

import com.sankuai.octo.config.model.{ConfigNode, file_param_t, ConfigFile, ConfigFileRequest}
import com.sankuai.octo.sgnotify.NotifyImpl
import com.sankuai.octo.sgnotify.comm.FileConfigCmdType
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConverters._
import java.util
import com.sankuai.octo.sgnotify.model.ConfigUpdateEvent

@RunWith(classOf[JUnitRunner])
class ApiSuite extends FunSuite with BeforeAndAfter {

  test("fileConfig DISTRIBUTE") {
    val request = new ConfigFileRequest()
    request.setHosts(List("10.4.244.156", "172.30.8.236").asJava) //用于测试新老云主机

    val fileContent = "test new and old cloud machines".getBytes()
    val md5 = MessageDigest.getInstance("MD5")
    val tmp = md5.digest(fileContent)
    val md5String = new java.math.BigInteger(1, tmp).toString(16)

    val configFiles = new ConfigFile()
    configFiles.setFilename("sgnotify.conf")
    configFiles.setFilepath("/opt/meituan/apps/mcc/com.sankuai.inf.sgnotify/")
    configFiles.setMd5(md5String)
    configFiles.setFilecontent(fileContent)
    println(md5String)

    val files = new file_param_t()
    files.setAppkey("com.sankuai.inf.sgnotify")
    files.setEnv("prod")
    files.setPath("/opt/meituan/apps/mcc/com.sankuai.inf.sgnotify/")
    files.setConfigFiles(List(configFiles).asJava)

    request.setFiles(files)
    println(NotifyImpl.distributeOrEnableFileConfig(request, FileConfigCmdType.DISTRIBUTE))
  }

  test("fileConfig ENABLE") {
    val request = new ConfigFileRequest()
    //    request.setHosts(List("172.30.8.236").asJava)//用于测试老云主机
    //    request.setHosts(List("10.4.244.156").asJava)//用于测试新云主机
    request.setHosts(List("10.4.244.156", "172.30.8.236").asJava) //用于测试新老云主机

    val fileContent = "test new and old cloud machines".getBytes()
    val md5 = MessageDigest.getInstance("MD5")
    val tmp = md5.digest(fileContent)
    val md5String = new java.math.BigInteger(1, tmp).toString(16)

    val configFiles = new ConfigFile()
    configFiles.setFilename("sgnotify.conf")
    configFiles.setFilepath("/opt/meituan/apps/mcc/com.sankuai.inf.sgnotify/")
    configFiles.setMd5(md5String)
    configFiles.setFilecontent(fileContent)

    val files = new file_param_t()
    files.setAppkey("com.sankuai.inf.msgp")
    files.setEnv("prod")
    files.setPath("/opt/meituan/apps/mcc/com.sankuai.inf.sgnotify/")
    files.setConfigFiles(List(configFiles).asJava)

    request.setFiles(files)
    println(NotifyImpl.distributeOrEnableFileConfig(request, FileConfigCmdType.ENABLE))
  }

//  test("batch notify") {
//    val map = new util.HashMap[String, java.util.List[ConfigNode]]()
//    val nodes = new java.util.ArrayList[ConfigNode]()
//    nodes.add(new ConfigNode("com.sankuai.inf.msgp", "", "/"))
//    map.put("10.4.241.125", nodes)
//    map.put("10.4.241.165", nodes)
//    NotifyImpl.batchNotify(map)
//
//    val parRet = map.asScala.par.map {
//      case (ip, nodes) =>
//        (ip, NotifyImpl.notifyAgent(ip, nodes).asInstanceOf[Integer])
//    }.toMap
//    println(parRet)
//  }
//
//  test("onConfigUpdate") {
//    val map = new util.HashMap[String, java.util.List[ConfigNode]]()
//    val nodes = new java.util.ArrayList[ConfigNode]()
//    nodes.add(new ConfigNode("com.sankuai.inf.msgp", "", "/"))
//    map.put("10.4.241.125", nodes)
//    map.put("10.4.241.165", nodes)
//    val event = new ConfigUpdateEvent()
//    event.setChanged(map)
//    NotifyImpl.onConfigUpdate(event)
//  }
}
