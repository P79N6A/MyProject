package com.sankuai.octo.msgp.utils.client

import com.dianping.squirrel.client.StoreKey
import com.dianping.squirrel.client.impl.redis.{RedisClientConfig, RedisDefaultClient, RedisStoreClient}
import com.sankuai.msgp.common.config.MsgpConfig
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object SquirrelClient {
  private val LOGGER = LoggerFactory.getLogger(this.getClass)


  /*
    String operations
   */
  def get(category: String, keys: String*): String = {
    val key =  new StoreKey(category, keys: _*)
    client.get(key)
  }

  def hgetAll[T](category: String, keys: String*) = {
    val key = new StoreKey(category, keys: _*)
    val m = client.hgetAll[String](key)
    m.asScala.toMap
  }

  def hmget[T](category: String, fields:List[String], keys: String*): java.util.List[String] = {
    val key = new StoreKey(category, keys: _*)
    client.hmget[String](key, fields: _*)
  }

  def multiGet(storeKeys: Seq[StoreKey]): Map[StoreKey, String] = {
    val m: java.util.Map[StoreKey, String] = client.multiGet(storeKeys.asJava)
    m.asScala.toMap
  }

  def set(category: String, value: String, keys: String*): Boolean = {
    val key = new StoreKey(category, keys: _*)
    client.set(key, value)
  }

  def multiSet(storeKeys: Seq[StoreKey], values: Seq[String]): Boolean = {
    client.multiSet(storeKeys.asJava, values.asJava)
  }

  /*
    List operations
   */
  def push(category: String, value: AnyRef, keys: String*): Long = {
    val key = new StoreKey(category, keys: _*)
    client.rpush(key, value)
  }

  def pop[T](category: String, keys: String*): T = {
    val key = new StoreKey(category, keys: _*)
    client.lpop(key)
  }

  /*
    Hash operations
   */
  def hget[T](category: String, field: String, keys: String*): String = {
    val key = new StoreKey(category, keys: _*)
    client.hget(key, field)
  }

  def hset(category: String, field: String, value: AnyRef, keys: String*): Long = {
    val key = new StoreKey(category, keys: _*)
    client.hset(key, field, value)
  }

  def hmset(category: String, fields: Map[String, AnyRef], keys: String*): Unit = {
    val key = new StoreKey(category, keys: _*)
    client.hmset(key, fields.asJava)
  }

  def hsetnx(category: String, field: String, value: AnyRef, keys: String*): Boolean = {
    val key = new StoreKey(category, keys: _*)
    client.hsetnx(key, field, value)
  }

  def hdel(category: String, field: String, keys: String*): Long = {
    val key = new StoreKey(category, keys: _*)
    client.hdel(key, field)
  }

  def llen(category: String, keys: String*): Long = {
    val key = new StoreKey(category, keys: _*)
    client.llen(key)
  }

  def getPipelined = {
    client.pipelined()
  }

  /*
    All
   */
  def delete(category: String, keys: String*): Boolean = {
    val key = new StoreKey(category, keys: _*)
    client.delete(key)
  }

  private val client: RedisStoreClient = {
    val clientCommonConfig = new RedisClientConfig
    clientCommonConfig.setReadTimeout(3000)
    clientCommonConfig.setPoolMaxIdle(32)
    clientCommonConfig.setPoolMaxTotal(64)
    clientCommonConfig.setPoolWaitMillis(5000)
    clientCommonConfig.setPoolMinIdle(4)
    var clusterName = MsgpConfig.get("squirrel-cluster-name","redis-portrait_qa")
    new RedisDefaultClient(clusterName, clientCommonConfig)
  }

  def main(args: Array[String]): Unit = {
    hset("portrait-qps", "qpsMax", 147.toString, "com.sankuai.inf.logCollector", null, null)
    val ret = hget("portrait-qps", "qpsMax", "com.sankuai.inf.logCollector", null, null)
    println(ret)
  }
}
