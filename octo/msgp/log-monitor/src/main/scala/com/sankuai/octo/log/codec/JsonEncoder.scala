package com.sankuai.octo.log.codec

import com.sankuai.octo.log.actor.Connector.LogQuery
import com.sankuai.octo.statistic.helper.api
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.slf4j.LoggerFactory

/**
  * Created by wujinwu on 16/4/20.
  */
@Sharable
object JsonEncoder extends MessageToByteEncoder[LogQuery] {
  private val logger = LoggerFactory.getLogger(this.getClass)

  @throws(classOf[Exception])
  override def encode(ctx: ChannelHandlerContext, msg: LogQuery, out: ByteBuf): Unit = {
    val bytes = api.jsonBytes(msg)
    out.writeBytes(bytes)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    ctx.close()
  }
}
