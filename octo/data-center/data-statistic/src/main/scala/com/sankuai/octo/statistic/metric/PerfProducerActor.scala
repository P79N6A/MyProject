/*
package com.sankuai.octo.statistic.metric

import akka.actor.{Actor, ActorLogging}
import com.meituan.mafka.client.MafkaClient
import com.meituan.mafka.client.producer.{AsyncProducerResult, FutureCallback, IProducerProcessor}
import com.sankuai.octo.statistic.mafka.constant.MqConstants
import com.sankuai.octo.statistic.model.StatData
import com.sankuai.octo.statistic.util.{ExecutionContextFactory, HessianSerializer}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future, blocking}

class PerfProducerActor extends Actor with ActorLogging {

  import PerfProducerActor._

  private val producer = createProducer()

  override def receive: Receive = {
    case statData: StatData => sendToMq(statData)
  }

  def sendToMq(statData: StatData): Unit = {
    Future {
      blocking {
        val bytes = HessianSerializer.serialize(statData)
        produce(bytes)
      }
    }
  }

  private def produce(bytes: Array[Byte]): Unit = {
    producer.sendAsyncMessage(bytes, producerCallback)
  }

  @throws(classOf[Exception])
  override def postStop(): Unit = {
    producer.close()
  }
}

object PerfProducerActor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private implicit lazy val ec: ExecutionContextExecutor = ExecutionContextFactory.build(2)

  //  生产者的回调对象,避免重复创建
  private val producerCallback = new FutureCallback {
    override def onFailure(t: Throwable): Unit = logger.error("async send fail", t)

    override def onSuccess(result: AsyncProducerResult): Unit = ()
  }


  private def createProducer() = {
    try {
      val topic = MqConstants.perfDataTopic()
      val producer = MafkaClient.buildProduceFactory(topic)
      logger.info("perf producer created")
      producer.asInstanceOf[IProducerProcessor[String, Array[Byte]]]
    } catch {
      case e: Exception =>
        logger.error("createProducer fail", e)
        throw new RuntimeException("createProducer fail")
    }
  }


}
*/
