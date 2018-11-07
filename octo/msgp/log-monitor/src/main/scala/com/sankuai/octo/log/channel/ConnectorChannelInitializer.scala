package com.sankuai.octo.log.channel

import akka.actor.ActorRef
import com.sankuai.octo.log.codec.{ForwardHandler, IntLengthFieldPrepender, JsonDecoder, JsonEncoder}
import com.sankuai.octo.log.constant.RTLogConstant
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder

/**
  * Created by wujinwu on 16/5/4.
  */
class ConnectorChannelInitializer(connector: ActorRef) extends ChannelInitializer[SocketChannel] {

  @throws(classOf[Exception])
  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()

    pipeline.addLast(new LengthFieldBasedFrameDecoder(RTLogConstant.MAX_FRAME_LENGTH, 0, 4, 0, 4))
    pipeline.addLast(new JsonDecoder())

    pipeline.addLast(IntLengthFieldPrepender)
    pipeline.addLast(JsonEncoder)

    pipeline.addLast(new ForwardHandler(connector))
  }
}
