package com.sankuai.octo.log.actor

import java.lang.Boolean

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.sankuai.octo.log.actor.Connector.LogQuery
import com.sankuai.octo.log.codec.{ForwardHandler, IntLengthFieldPrepender, JsonDecoder, JsonEncoder}
import com.sankuai.octo.log.constant.RTLogConstant
import com.sankuai.octo.log.utils.EventLoopUtil
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelInitializer, ChannelOption}
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.scalatest.FunSuite

/**
  * Created by wujinwu on 16/4/27.
  */
class WatcherTest extends FunSuite {
  test("watcher") {
    implicit lazy val system = ActorSystem()
    val testProbe = TestProbe()
    val b = new Bootstrap()
    val group = EventLoopUtil.nextEventLoopGroup
    // Configure the client.
    b.group(group)
      .channel(classOf[NioSocketChannel])
      .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
      .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
      .handler(new ChannelInitializer[SocketChannel]() {
        @throws(classOf[Exception])
        override def initChannel(ch: SocketChannel) = {
          val pipeline = ch.pipeline()

          pipeline.addLast(new LengthFieldBasedFrameDecoder(RTLogConstant.MAX_FRAME_LENGTH, 0, 4, 0, 4))
          pipeline.addLast(new JsonDecoder())

          pipeline.addLast(IntLengthFieldPrepender)
          pipeline.addLast(JsonEncoder)

          pipeline.addLast(new ForwardHandler(testProbe.ref))
        }
      })


    // Start the client.
    val f: ChannelFuture = b.connect("10.4.237.168", RTLogConstant.LOG_AGENT_PORT)

    f.addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture): Unit = {
        if (!future.isSuccess) {
          future.channel.close
        } else {
          val channel = future.channel()
          val query = new LogQuery("start", "/opt/logs/mobile/msgp/msgp.log.2016-04-28", Set("2016-04-28 15"))
          channel.writeAndFlush(query)
        }

      }
    })
    f.channel().closeFuture().sync()
  }

}
