package com.sankuai.msgp.common.utils.helper

import java.nio.charset.Charset
import java.security.MessageDigest

import org.slf4j.LoggerFactory

object Md5Helper {
  val LOG = LoggerFactory.getLogger(Md5Helper.getClass)

  val md5 = MessageDigest.getInstance("MD5")

  def getMD5(content : String) = {
    getMD532(content.getBytes(Charset.forName("utf-8")))
  }

  def getMD532(content: Array[Byte]) = {
    val tmp = md5.digest(content)
    val buf = new StringBuffer("")
    tmp.foreach{
      x =>
        var i = x.toInt
        if(i < 0) {
          i = i + 256
        }
        if(i < 16) {
          buf.append("0")
        }
        buf.append(Integer.toHexString(i))

    }
    buf.toString
  }
}
