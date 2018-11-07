package com.sankuai.octo.log.codec

import java.nio.charset.StandardCharsets.UTF_8
import java.util

import com.sankuai.octo.log.actor.Connector.LogReply
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.slf4j.LoggerFactory

/**
  * Created by wujinwu on 16/4/20.
  */
class JsonDecoder extends MessageToMessageDecoder[ByteBuf] {

  import JsonDecoder.logger

  @throws(classOf[Exception])
  override def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
    val length = in.readableBytes()
    val buffer = new Array[Byte](length)
    in.readBytes(buffer)
    val str = new String(buffer, UTF_8)

    try {
      val resBeginIndex = str.indexOf("\"result\":\"") + "\"result\":\"".length
      val resEndIndex = str.indexOf("\"", resBeginIndex)
      val res = str.substring(resBeginIndex, resEndIndex)

      val contentBeginIndex = str.indexOf("\"content\":\"") + "\"content\":\"".length
      val contentEndIndex = str.lastIndexOf("\"}")
      val content = str.substring(contentBeginIndex, contentEndIndex)
      val logReply = LogReply(res, content)

      out.add(logReply)
    } catch {
      case e: Exception =>
        //  跳过解析失败的消息
        logger.error(s"decode fail,msg:$str", e)
    }
  }
}

object JsonDecoder {
  private val logger = LoggerFactory.getLogger(this.getClass)
}
