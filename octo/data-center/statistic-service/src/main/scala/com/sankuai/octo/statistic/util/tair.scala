package com.sankuai.octo.statistic.util

import java.nio.charset.StandardCharsets

import com.sankuai.octo.statistic.helper.api
import com.taobao.tair3.client.TairClient.TairOption
import com.taobao.tair3.client.error.TairException
import com.taobao.tair3.client.impl.DefaultTairClient
import com.taobao.tair3.client.{Result, TairClient}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

object tair {

  private val logger = LoggerFactory.getLogger(tair.getClass)

  private val defaultOpt = new TairClient.TairOption(3000)

  private val MAX_LENGTH_OF_TAIR_KEY = 1024

  private val client: DefaultTairClient = new DefaultTairClient()
  private var area: Short = 0

  def put(key: String, data: AnyRef, expire: Int) {
    val opt = new TairOption(3000)
    opt.setExpireTime(expire)
    put(key.getBytes(StandardCharsets.UTF_8), data, opt)
  }

  def put(key: Array[Byte], data: Array[Byte], expire: Int) {
    val opt = new TairOption(3000)
    opt.setExpireTime(expire)
    try {
      client.putAsync(area, key, data, opt)
    } catch {
      case e: Exception => logger.error(s"put Fail,key:${new String(key, StandardCharsets.UTF_8)}", e)
    }
  }

  def put(key: String, data: Array[Byte], expire: Int) {
    val opt = new TairOption(3000)
    opt.setExpireTime(expire)
    try {
      client.putAsync(area, key.getBytes(StandardCharsets.UTF_8), data, opt)
    } catch {
      case e: Exception => logger.error(s"put Fail,key:$key", e)
    }
  }


  def incr(key: String, value: Int, expire: Int) = {
    try {
      client.incr(area, key.getBytes(StandardCharsets.UTF_8), value, 0, expireOpt(expire)).getResult
    } catch {
      case e: Exception => logger.error(s"incr Fail,key:$key,value:$value", e)
        0
    }
  }

  def expireOpt(seconds: Int) = {
    val opt = new TairOption(3000)
    opt.setExpireTime(seconds)
    opt
  }

  def put(key: String, data: AnyRef) {
    if (isValidKeyLength(key)) {
      put(key.getBytes(StandardCharsets.UTF_8), data)
    } else {
      logger.warn(s"tair put fail, key is too long, key: $key")
    }
  }

  def putAsync(key: String, data: AnyRef) {
    if (isValidKeyLength(key)) {
      try {
        val tairKey = key.getBytes(StandardCharsets.UTF_8)
        data match {
          case s: String =>
            client.putAsync(area, tairKey, s.getBytes(StandardCharsets.UTF_8), defaultOpt)
          case bytes: Array[Byte] =>
            client.putAsync(area, tairKey, bytes, defaultOpt)
          case _ =>
            client.putAsync(area, tairKey, api.jsonBytes(data), defaultOpt)
        }
      } catch {
        case e: Exception => logger.error(s"tair put fail,key:$key", e)
      }
    } else {
      logger.warn(s"tair put fail, key is too long, key: $key")
    }
  }

  def putAsync(key: String, data: Array[Byte], expire: Int) {
    if (isValidKeyLength(key)) {
      val opt = new TairOption(3000)
      opt.setExpireTime(expire)
      try {
        client.putAsync(area, key.getBytes(StandardCharsets.UTF_8), data, opt)
      } catch {
        case e: Exception => logger.error(s"put Fail,key:$key", e)
      }
    }else{
      logger.warn(s"tair put fail, key is too long, key: $key")
    }
  }

  def put(key: Array[Byte], data: AnyRef) {
    put(key, data, defaultOpt)
  }

  def put(key: Array[Byte], data: AnyRef, opt: TairOption) {
    try {
      data match {
        case s: String =>
          client.putAsync(area, key, s.getBytes(StandardCharsets.UTF_8), opt)
        case bytes: Array[Byte] =>
          client.putAsync(area, key, bytes, opt)
        case _ =>
          client.putAsync(area, key, api.jsonBytes(data), opt)
      }
    } catch {
      case e: Exception => logger.error(s"tair put fail,key:${new String(key, StandardCharsets.UTF_8)}", e)
    }
  }

  /*
    def client = {
      if (tairClient.isEmpty) {

      }
      tairClient.get
    }
  */

  def init(master: String, slave: String, group: String, localAppKey: String,
           remoteAppKey: String, area: Short) = {
    client.setMaster(master)
    if (slave != null && !slave.isEmpty) {
      client.setSlave(slave)
    }
    client.setGroup(group)
    client.setLocalAppKey(localAppKey)
    client.setRemoteAppKey(config.get("tair.remoteAppKey", ""))
    this.area = area
    logger.info("init tair master:{}, slave:{}, group:{}, area:{},remoteAppKey:{}", Seq[AnyRef](master, slave, group, area: java.lang.Short, config.get("tair.remoteAppKey", "")): _*)
    try {
      client.init()
      logger.info("init tair done...")
      //  在机器关闭时释放资源
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run() {
          logger.info("shutdown tair...")
          client.close()
        }
      })
    } catch {
      case e: Exception =>
        logger.error("init tair exception", e)
        //  初始化不成功,快速失败
        throw new RuntimeException(e)
    }

  }

  def prefixPut(pkey: Array[Byte], skey: Array[Byte], data: AnyRef) {
    data match {
      case s: String =>
        client.prefixPut(area, pkey, skey, s.getBytes(StandardCharsets.UTF_8), defaultOpt)
      case bytes: Array[Byte] =>
        client.prefixPut(area, pkey, skey, bytes, defaultOpt)
      case _ =>
        client.prefixPut(area, pkey, skey, api.jsonBytes(data), defaultOpt)
    }
  }

  def getRangeKey(pkey: String, begin: Array[Byte], end: Array[Byte]) = {
    val result = client.getRangeKey(area, pkey.getBytes(StandardCharsets.UTF_8), begin, end, 0, 10, defaultOpt)
    if (result.isSuccess && result.getCode == Result.ResultCode.OK) {
      result.getResult.foreach {
        x => if (x.isSuccess && x.getCode == Result.ResultCode.OK) {
          println(x.getResult)
        }
      }
    } else {
      logger.warn(s"get failed by $pkey, $begin, $end error code $result")
    }
  }

  def del(key: String) {
    try {
      client.delete(area, key.getBytes(StandardCharsets.UTF_8), defaultOpt)
    } catch {
      case e: Exception => logger.error(s"delete Fail,key:$key", e)
    }
  }

  def get(key: String): Option[String] = {
    getValue(key).map {
      x => new String(x, StandardCharsets.UTF_8)
    }
  }

  def getValue(key: String): Option[Array[Byte]] = {
    if(isValidKeyLength(key)){
      try {
        val result = client.get(area, key.getBytes(StandardCharsets.UTF_8), defaultOpt)
        if (result.isSuccess && result.getCode == Result.ResultCode.OK) {
          Some(result.getResult)
        }else{
          None
        }
      } catch {
        case e: TairException => 
          logger.error(s"tair exception,key:$key", e)
          None
        case e: Exception =>
          logger.error(s"exception,key:$key", e)
          None
      }
    }else{
      logger.warn(s"tair get fail, key is too long, key: $key")
      None
    }
  }

  def isValidKeyLength(key: String) = if (key.length >= MAX_LENGTH_OF_TAIR_KEY) false else true

  /*
    def setAdd(key: String, values: Set[String], expire: Int) = {
      try {
        client.setAdd(area, key.getBytes(StandardCharsets.UTF_8), values.map(_.getBytes(StandardCharsets.UTF_8)), expireOpt(expire)).getResult
      } catch {
        case e: Exception => logger.warn("setAdd fail,key:{},values:{}", Array[AnyRef](key, values, e): _*)
      }
    }
  */

  /*
    def setMembers(key: String): Option[Set[String]] = {
      try {
        val result = client.setMembers(area, key.getBytes(StandardCharsets.UTF_8), defaultOpt)
        if (result != null && result.getCode == ResultCode.OK && result.getResult.nonEmpty) {
          Some(result.getResult.map(new String(_, StandardCharsets.UTF_8)).toSet)
        } else {
          None
        }
      } catch {
        case e: Exception => None
      }
    }
  */


}
