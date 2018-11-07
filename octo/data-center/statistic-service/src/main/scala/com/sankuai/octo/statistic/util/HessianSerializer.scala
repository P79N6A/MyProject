package com.sankuai.octo.statistic.util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.caucho.hessian.io.HessianFactory

/**
  * Created by wujinwu on 16/1/26.
  */
object HessianSerializer {

  private val factory = new HessianFactory

  def serialize(obj: AnyRef) = {
    val outputStream = new ByteArrayOutputStream()
    val output = factory.createHessian2Output(outputStream)
    output.startMessage()
    output.writeObject(obj)
    output.completeMessage()
    output.close()
    outputStream.toByteArray

  }

  def deserialize[T](bytes: Array[Byte], clazz: Class[T]) = {
    val inputStream = new ByteArrayInputStream(bytes)
    val in = factory.createHessian2Input(inputStream)
    in.startMessage()
    val value: T = in.readObject(clazz).asInstanceOf[T]
    in.completeMessage()
    in.close()
    value
  }
}
