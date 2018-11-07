package com.sankuai.octo.log.actor

import akka.actor.FSM
import com.fasterxml.jackson.annotation.JsonProperty
import com.sankuai.octo.log.Protocol.WatchInfo
import com.sankuai.octo.log.actor.Connector.ConnectorError.ConnectorError
import com.sankuai.octo.log.actor.Connector.{ConnectorData, ConnectorState}
import com.sankuai.octo.log.channel.ConnectorChannelInitializer
import com.sankuai.octo.log.constant.RTLogConstant
import com.sankuai.octo.log.constant.RTLogConstant.TIMEOUT
import com.sankuai.octo.log.utils.EventLoopUtil
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{Channel, ChannelFuture, ChannelFutureListener, ChannelOption}
import org.springframework.util.StringUtils

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

/**
  * Created by wujinwu on 16/5/16.
  */
class Connector(watchInfo: WatchInfo) extends FSM[ConnectorState, ConnectorData] {

  import Connector._

  private val watcher = context.parent

  private var selfCheckList = ListBuffer[Boolean]()

  private var channel: Channel = _

  {
    startWith(Idle, Buffer())

    when(Idle, stateTimeout = TIMEOUT) {

      case Event(LogReply(result, content), buffer@Buffer(q)) =>
        if (!StringUtils.hasText(result) || result != "success") {
          log.info(s"result invalid,content:$content")
          throw ConnectorException(ConnectorError.ResultInvalid, content)
        } else {
          goto(Active) using buffer.copy(queue = q += content)
        }
      case Event(StateTimeout, _) =>
        log.info("Connector Timeout close,watchInfo:{}", watchInfo)
        throw ConnectorException(ConnectorError.NoData, NO_DATA_INFO)
    }

    when(Active) {
      case Event(LogReply(result, content), buffer@Buffer(q)) =>
        stay() using buffer.copy(queue = q += content)
      case Event(Flush, buffer@Buffer(q)) =>
        if (q.isEmpty) {
          selfCheckList += false
          if (selfCheckList.size > SELF_CHECK_COUNT) {
            selfCheckList = selfCheckList.tail
            if (selfCheckList.forall(_ == false)) {
              throw ConnectorException(ConnectorError.NoData, NO_DATA_INFO)
            }
          }
          stay()
        } else {
          selfCheckList += true
          sendBuffer(buffer)
          stay() using Buffer()
        }
    }

    whenUnhandled {
      // common code for both states
      case Event(e: Throwable, _) =>
        log.info("Exception", e)
        throw ConnectorException(ConnectorError.Network, NETWORK_INFO)
    }

    onTransition {
      case _ -> Active => setTimer("flush", Flush, SELF_CHECK_INTERVAL, repeat = true)
      case Active -> _ => cancelTimer("flush")
    }

    initialize()
  }

  @throws(classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    val group = EventLoopUtil.nextEventLoopGroup
    // Configure the client.
    val b = new Bootstrap()
    b.group(group)
      .channel(classOf[NioSocketChannel])
      .option(ChannelOption.TCP_NODELAY, java.lang.Boolean.TRUE)
      .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
      .handler(new ConnectorChannelInitializer(self))

    connectAndSend(b)

  }

  private def connectAndSend(bootstrap: Bootstrap) = {

    // Start the client.
    val f: ChannelFuture = bootstrap.connect(watchInfo.host, RTLogConstant.LOG_AGENT_PORT)

    f.addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture): Unit = {
        if (!future.isSuccess) {
          log.error("fail", future.cause())
          future.channel.close
          Connector.this.channel = null
          throw ConnectorException(ConnectorError.Network, future.cause().getMessage)
        } else {
          val channel = future.channel()
          val query = LogQuery("start", watchInfo.filePath, watchInfo.filterWords)
          channel.writeAndFlush(query)
          Connector.this.channel = channel
        }

      }
    })
  }

  @throws(classOf[Exception])
  override def postStop(): Unit = {
    if (channel != null) {
      channel.close()
      channel = null
    }
    cancelTimer("flush")
  }

  private def sendBuffer(buffer: Buffer): Unit = {
    watcher ! buffer
  }
}


object Connector {

  private val SELF_CHECK_INTERVAL = Duration(2, SECONDS)

  private val SELF_CHECK_COUNT = (TIMEOUT / SELF_CHECK_INTERVAL).toInt
  private val NO_DATA_INFO = s"No data in $TIMEOUT"
  private val NETWORK_INFO = "Network Error"

  sealed trait ConnectorState

  sealed trait ConnectorData

  final case class Buffer(queue: ListBuffer[String] = ListBuffer()) extends ConnectorData

  case class LogQuery(@JsonProperty("cmd") cmd: String, @JsonProperty("filePath") filePath: String, @JsonProperty("filterWords") filterWords: Set[String])

  case class LogReply(@JsonProperty("result") result: String, @JsonProperty("content") content: String)

  case class ConnectorException(error: ConnectorError, msg: String) extends RuntimeException

  case object Idle extends ConnectorState

  case object Active extends ConnectorState

  case object Flush

  object ConnectorError extends Enumeration {
    type ConnectorError = Value
    val ResultInvalid, NoData, Network = Value
  }


}
