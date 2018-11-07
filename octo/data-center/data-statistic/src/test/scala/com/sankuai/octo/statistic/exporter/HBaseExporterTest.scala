package com.sankuai.octo.statistic.exporter

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

import com.google.common.base.Stopwatch
import com.sankuai.octo.FalconSuite
import com.sankuai.octo.statistic.model.StatData
import com.sankuai.octo.statistic.util.HessianSerializer
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.util.SerializationUtils

import scala.util.Random

/**
  * Created by wujinwu on 16/1/22.
  */
@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:applicationContext.xml"))
class HBaseExporterTest extends FunSuite {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  /*
    test("testExport") {
      val suite = new FalconSuite()
      while (true) {
        val stat = suite.randStat((System.currentTimeMillis() / 1000).toInt, Random.nextString(10))
        DefaultExporterProxy.export(stat)
        Thread.sleep(10)
      }
    }
  */
  //  性能测试
  /* test("testSerial") {
     val suite = new FalconSuite()
     val stat = suite.randStat((System.currentTimeMillis() / 1000).toInt)
     val perf = HBaseExporter.statDataToPerfData(stat)
     val put = PerfTransformer.perfDataToSerialPut(perf)
     val sw = Stopwatch.createStarted()
     (1 to 1000000).foreach { _ =>
       PerfTransformer.serialPutToBytes(put)
     }
     println("test ms " + sw.elapsed(TimeUnit.MILLISECONDS))
     sw.reset().start()
     (1 to 1000000).foreach { _ =>
       PerfTransformer.serialPutToBytes(put)
     }
     println("test ms " + sw.elapsed(TimeUnit.MILLISECONDS))
   }*/
  test("testHessian") {
    val suite = new FalconSuite()
    val stat = suite.randStat((System.currentTimeMillis() / 1000).toInt)
    val bytes1 = SerializationUtils.serialize(stat)
    val bytes2 = HessianSerializer.serialize(stat)

    println(s"java serialization:bytes length:${bytes1.length}")
    println(s"hessian serialization:bytes length:${bytes2.length}")

    val stat2 = HessianSerializer.deserialize(bytes2, classOf[StatData])
    println(s"equal:${stat2 == stat}")
    val sw = Stopwatch.createStarted()
    val loopCount = 1000000
    val statList = (1 to loopCount).map { _ => suite.randStat((System.currentTimeMillis() / 1000).toInt, Random.nextString(10)) }
    //  java 与 hessian 序列化对比
    statList.foreach { stat =>
      SerializationUtils.serialize(stat)
    }
    sw.stop()
    println(s"java serialization:loop count:$loopCount,time:${sw.elapsed(MILLISECONDS)}")
    sw.reset().start()
    statList.foreach { stat =>
      HessianSerializer.serialize(stat)
    }
    sw.stop()
    println(s"Hessian serialization:loop count:$loopCount,time:${sw.elapsed(MILLISECONDS)}")

    //  单线程测试
    sw.reset().start()
    statList.foreach { stat =>
      //      SerializationUtils.serialize(stat)
      HessianSerializer.serialize(stat)
    }

    sw.stop()
    println(s"single thread time:${sw.elapsed(MILLISECONDS)}")
    val es = Executors.newCachedThreadPool()
    val count = new AtomicInteger(statList.size)
    sw.reset().start()
    statList.foreach { stat =>
      es.submit(new Runnable {
        override def run(): Unit = {
          HessianSerializer.serialize(stat)
          if (count.decrementAndGet() == 0) {
            sw.stop()
            println(s"multi thread time:${sw.elapsed(MILLISECONDS)}")
          }
        }
      })
    }

  }
}
