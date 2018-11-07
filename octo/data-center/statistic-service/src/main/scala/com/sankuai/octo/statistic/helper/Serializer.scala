package com.sankuai.octo.statistic.helper

import org.apache.thrift.{TBase, TDeserializer, TFieldIdEnum, TSerializer}
import org.slf4j.LoggerFactory

/**
  * Created by wujinwu on 15/11/2.
  */
object Serializer {

  private val logger = LoggerFactory.getLogger(this.getClass)

  /** TSerializer && TDeserializer not thread safe !
    * Setup ThreadLocal of TSerializer instances */
  private val serializer = new ThreadLocal[TSerializer]() {
    override def initialValue() = {
      new TSerializer()
    }

  }
  /** TSerializer && TDeserializer not thread safe !
    * Setup ThreadLocal of TDeserializer instances */
  private val deserializer = new ThreadLocal[TDeserializer]() {
    override def initialValue() = {
      new TDeserializer()
    }
  }


  def toBytes[T <: TBase[_ <: TBase[_, _], _ <: TFieldIdEnum]](obj: T): Array[Byte] = {
    logger.debug("obj class:{}", obj.getClass.getCanonicalName)
    serializer.get().serialize(obj)
  }

  def toObject[T <: TBase[_ <: TBase[_, _], _ <: TFieldIdEnum]](bytes: Array[Byte], clazz: Class[T]): T = {
    logger.debug("clazz:{}", clazz.getCanonicalName)
    val obj = clazz.newInstance()
    deserializer.get().deserialize(obj, bytes)
    obj
  }
}
