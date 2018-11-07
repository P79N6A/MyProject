package com.sankuai.octo.log.codec

import akka.actor.ActorRef
import com.sankuai.octo.log.actor.Connector.LogReply
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import org.slf4j.LoggerFactory

/**
  * Created by wujinwu on 16/4/21.
  */
class ForwardHandler(private val actorRef: ActorRef) extends SimpleChannelInboundHandler[LogReply] {

  import ForwardHandler._

  @throws(classOf[Exception])
  override def channelRead0(ctx: ChannelHandlerContext, msg: LogReply): Unit = {
    actorRef ! msg
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.error("ForwardHandler fail", cause)
    ctx.close()
    //连接断开,发送给 watcher,由其新建连接
    actorRef ! cause
  }

}

object ForwardHandler {
  private val logger = LoggerFactory.getLogger(this.getClass)
}