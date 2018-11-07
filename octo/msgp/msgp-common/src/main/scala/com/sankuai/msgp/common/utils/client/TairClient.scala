package com.sankuai.msgp.common.utils.client

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.taobao.tair3.client.TairClient.TairOption
import com.taobao.tair3.client.config.impl.SimpleTairConfig
import com.taobao.tair3.client.error.TairException
import com.taobao.tair3.client.impl.MultiTairClient
import com.taobao.tair3.client.{Result => TairResult, TairClient => RealTairClient}
import org.slf4j.{Logger, LoggerFactory}

object TairClient {
  private val LOG: Logger = LoggerFactory.getLogger(TairClient.getClass)

  val localAppKey = "com.sankuai.inf.msgp"

  private def master: String = {
    MsgpConfig.get("tair.master", "m-yf.qadev.tair.vip.sankuai.com:5198")
  }

  private def slave: String = {
    MsgpConfig.get("tair.slave", "s-yf.qadev.tair.vip.sankuai.com:5198")

  }

  private def group: String = {
    MsgpConfig.get("tair.group", "group_inf_qadevfunction")
  }

  private def area: Short = {
    MsgpConfig.get("tair.area", "5").toShort
  }

  private def remoteAppKey: String = {
    MsgpConfig.get("tair.remote.appkey", "com.sankuai.tair.qa.function")
  }

  private val defaultOpt = new RealTairClient.TairOption(3000)

  private var tairClient: Option[MultiTairClient] = None

  def expireOpt(seconds: Int) = {
    val opt = new TairOption(3000)
    opt.setExpireTime(seconds)
    opt
  }

  def client = {
    if (tairClient.isEmpty) {
      synchronized {
        try {
          val tairConfig = new SimpleTairConfig(localAppKey, remoteAppKey)
          val instance = new MultiTairClient(tairConfig)
          instance.init()
          tairClient = Some(instance)
          Runtime.getRuntime.addShutdownHook(new Thread() {
            override def run() {
              LOG.info("shutdown tair...")
              tairClient.get.close()
            }
          })
        } catch {
          case e: Exception => LOG.error("int tair exception",e)
        }
      }
    }
    tairClient.get
  }

  def put(key: String, data: AnyRef, expire: Int) {
    val opt = new TairOption(3000)
    opt.setExpireTime(expire)
    put(key.getBytes("utf-8"), data, opt)
  }

  def incr(key: String, value: Int, expire: Int) = {
    client.incr(area, key.getBytes("utf-8"), value, 0, expireOpt(expire)).getResult
  }

  def put(key: String, data: AnyRef) {
    put(key.getBytes("utf-8"), data)
  }

  def put(key: Array[Byte], data: AnyRef) {
    put(key, data, defaultOpt)
  }

  def put(key: Array[Byte], data: AnyRef, opt: TairOption) {
    try {
      if (data.isInstanceOf[String]) {
        client.put(area, key, data.asInstanceOf[String].getBytes("utf-8"), opt)
      } else if (data.isInstanceOf[Array[Byte]]) {
        client.put(area, key, data.asInstanceOf[Array[Byte]], opt)
      } else {
        client.put(area, key, JsonHelper.jsonBytes(data), opt)
      }
    }
    catch {
      case e: Exception => LOG.error(s"put error key ${new String(key)},data:${data}", e)
    }
  }

  def prefixPut(pkey: Array[Byte], skey: Array[Byte], data: AnyRef) {
    if (data.isInstanceOf[String]) {
      client.prefixPut(area, pkey, skey, data.asInstanceOf[String].getBytes("utf-8"), defaultOpt)
    } else if (data.isInstanceOf[Array[Byte]]) {
      client.prefixPut(area, pkey, skey, data.asInstanceOf[Array[Byte]], defaultOpt)
    } else {
      client.prefixPut(area, pkey, skey, JsonHelper.jsonBytes(data), defaultOpt)
    }
  }

  def getValue(key: String): Option[Array[Byte]] = {
    try {
      val result = client.get(area, key.getBytes("utf-8"), defaultOpt)
      if (result.isSuccess && result.getCode == TairResult.ResultCode.OK) {
        return Some(result.getResult)
      } else {
        //LOG.warn(s"get failed by $key, error code $result")
      }
    } catch {
      case e: TairException => LOG.error(s"tair exception $e")
    }
    None
  }

  def del(key: String) {
    client.removeItems(area, key.getBytes("utf-8"), 0, 100, defaultOpt)
  }

  def getCount(key: String): Option[Integer] = {
    try {
      val result = client.getCount(area, key.getBytes("utf-8"), defaultOpt)
      if (result.isSuccess && result.getCode == TairResult.ResultCode.OK) {
        return Some(result.getResult)
      }
    } catch {
      case e: TairException => LOG.error(s"tair exception $e")
    }
    None
  }

  def get(key: String): Option[String] = {
    getValue(key).map {
      x => new String(x, "utf-8")
    }
  }


}
