/*
package com.sankuai.octo.statistic.metric

import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.google.common.base.Stopwatch
import com.meituan.mafka.client.MafkaClient
import com.meituan.mafka.client.consumer.{ConsumeStatus, ConsumerConstants, IConsumerProcessor, IMessageListener}
import com.meituan.mafka.client.message.{MafkaMessage, MessagetContext}
import com.sankuai.octo.statistic.mafka.constant.MqConstants
import com.sankuai.octo.statistic.model.{StatData, StatRange}
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, HessianSerializer, config}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Created by wujinwu on 16/5/28.
  */
class PerfConsumerActor extends Actor with ActorLogging {

  import PerfConsumerActor._

  private val hBaseExporter = context.actorOf(Props[HBaseExporterActor](), "HBaseExporterActor")

  private val consumer = {
    val c = createConsumer()
    init(c, hBaseExporter)
    c
  }

  override def receive: Receive = {
    case _ =>
  }

  @throws(classOf[Exception])
  override def postStop(): Unit = {
    consumer.close()
  }
}

object PerfConsumerActor {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /**
    * 触发器,激活消费者,持续消费数据
    * 调用一次即可
    */
  private def init(consumer: IConsumerProcessor, hBaseExporter: ActorRef): Unit = {
    //  所有消费者线程共享的监听器
    val listener = new PerfMessageListener(hBaseExporter)
    //  方法内部动态创建消费者线程
    consumer.recvMessageWithParallel(classOf[Array[Byte]], listener)
  }

  private def createConsumer() = {
    try {
      val consumerProps = new Properties()
      //  指定订阅组
      consumerProps.put(ConsumerConstants.SubscribeGroup, MqConstants.perfConsumerGroup())
      //  新订阅组从最新的offset开始pull
      consumerProps.put(ConsumerConstants.OffsetTypeOnNewSubscribeGroup, ConsumerConstants.LatestOffsetOnNewSubscribeGroup)
      val consumer: IConsumerProcessor = MafkaClient.buildConsumerFactory(consumerProps, MqConstants.perfDataTopic())
      logger.info("perf consumer created")
      consumer
    } catch {
      case e: Exception =>
        logger.error("createConsumer fail", e)
        throw new RuntimeException("createConsumer fail")
    }
  }

}

/**
  * perf 性能数据的消费者线程的监听器,内部实现了流控
  */
private class PerfMessageListener(hBaseExporter: ActorRef) extends IMessageListener[Array[Byte]] {

  private implicit lazy val ec = ExecutionContextFactory.build(2)
  private val logger = LoggerFactory.getLogger(this.getClass)

  //  计时器
  private val sw: ThreadLocal[Stopwatch] = new ThreadLocal[Stopwatch] {
    override def initialValue(): Stopwatch = Stopwatch.createStarted()
  }
  //  在时间周期内允许消费的消息数目
  private val counter = new ThreadLocal[AtomicInteger] {
    override def initialValue(): AtomicInteger = {
      val count = MqConstants.perfPullNumber()
      new AtomicInteger(count)
    }
  }
  //  每次流控的时间周期
  private val period = 100

  override def recvMessage(message: MafkaMessage[_], context: MessagetContext): ConsumeStatus = {
    try {
      val bytes = message.getBody.asInstanceOf[Array[Byte]]
      processMsg(bytes)
      val remainCount = counter.get().decrementAndGet()
      val elapsedMillis = sw.get.elapsed(TimeUnit.MILLISECONDS)
      if (isFlowControl(remainCount, elapsedMillis)) {
        //  已经在这个周期内消费了 N 条数据
        Thread.sleep(period - elapsedMillis)
        resetCounter()
      } else {
        //  继续消费
        ()
      }
      ConsumeStatus.CONSUME_SUCCESS
    } catch {
      case e: Exception =>
        logger.error("process msg fail", e)
        ConsumeStatus.RECONSUME_LATER
    }
  }

  private def processMsg(bytes: Array[Byte]) = {
    Future {
      val statData = HessianSerializer.deserialize(bytes, classOf[StatData])
      //  导出数据到HBase
      statData.getRange match {
        case StatRange.Day =>
          if (config.get("HBaseDaySwitch", "false").toBoolean)
            hBaseExporter ! statData
        case StatRange.Hour =>
          if (config.get("HBaseHourSwitch", "false").toBoolean)
            hBaseExporter ! statData
        case StatRange.Minute =>
          if (config.get("HBaseMinuteSwitch", "false").toBoolean)
            hBaseExporter ! statData
      }
    }

  }

  /**
    * 是否触发流控
    *
    * @param remainCount   本周期内,剩余未消费的消息个数
    * @param elapsedMillis 本周期已经流逝的毫秒数
    */
  private def isFlowControl(remainCount: Int, elapsedMillis: Long) = {
    //  在指定时间周期内消费了 N 条数据
    remainCount <= 0 && elapsedMillis < period
  }

  private def resetCounter() = {
    counter.get.set(MqConstants.perfPullNumber())
    sw.get.reset().start()
  }
}*/
