package com.sankuai.octo.spark

import com.sankuai.octo.spark.Helper._
import com.sankuai.octo.spark.domain.{HiveModuleData, ModuleInvokeAllKey, ModuleInvokeKey}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.model._
import com.sankuai.sgagent.thrift.model.PerfCostDataList
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable

object OctoDataCenterHour {

  private val role2TableNameMap = Map(0 -> "octolog_cli_perf_hour_index", 1 -> "octolog_serv_perf_hour_index")

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("OctoDataCenterPerfDay")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses {
      Array(classOf[ModuleInvokeAllKey], classOf[ModuleInvokeKey], classOf[HiveModuleData],
        classOf[StatSource], classOf[StatEnv], classOf[StatGroup], classOf[StatData], classOf[StatRange],
        classOf[PerfData], classOf[PerfProtocolType], classOf[PerfCostDataList], classOf[PerfRole], classOf[PerfDataType],
        classOf[Map[_, _]], classOf[(_, _)], classOf[(_, _, _)])
    }
    val sc = new SparkContext(conf)
    val sqlContext = new HiveContext(sc)
    // 读取hive，来自 http://git.sankuai.com/projects/DATA/repos/mthdp-sample/browse/spark
    // 写入hbase，可参考 http://git.sankuai.com/projects/DATA/repos/mthdp-sample/browse/spark/spark-hbase-demo

    val dayTuples = parseDate(args)
    dayTuples.foreach { dayTuple =>
      val dataFrame = sqlContext.sql(s"SELECT spanname,localappkey,`_mt_clientip`,remoteappkey,remotehost,cost,type,count,start,`_mt_servername` FROM log.mtrace " +
        s" WHERE dt = ${dayTuple._1} AND start >= ${dayTuple._2} AND start < ${dayTuple._3}")
      val rawInfos = dataFrame.map { row =>
        val spanName = row.getString(0)
        val localAppKey = row.getString(1)
        val localhost = row.getString(2)
        val remotAppKey = row.getString(3)
        val remoteHost = row.getString(4)
        val cost = row.getInt(5)
        val _type = row.getInt(6)
        val count = row.getInt(7)
        val start = row.getLong(8)
        val hourTs = Helper.getHourStart(start)
        val localhostName = row.getString(9)

        val envStr = localhostName match {
          case str if str.contains("stage") || str.contains("staging") => StatEnv.Stage.toString
          case str if str.contains("test") => StatEnv.Test.toString
          case _ => StatEnv.Prod.toString
        }
        (new ModuleInvokeAllKey(localAppKey, hourTs, _type, envStr, spanName, localhost, remotAppKey, remoteHost),
          Map[Int, Long](cost -> count.toLong))
      }
      val infos = rawInfos.reduceByKey(mergeCostToCountMap).persist(StorageLevel.MEMORY_ONLY_SER)

      //  计算相应的RDD
      val spanAllRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[String, Map[Int, Long]](Constants.ALL -> costToCount))
      }.reduceByKey(mergeSingleKeyMap)

      val spanRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[String, Map[Int, Long]](allKey.spanName -> costToCount))
      }.reduceByKey(mergeSingleKeyMap)

      val localhostRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[String, Map[Int, Long]](allKey.localHost -> costToCount))
      }.reduceByKey(mergeSingleKeyMap)

      val remoteAppRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[String, Map[Int, Long]](allKey.remoteAppKey -> costToCount))
      }.reduceByKey(mergeSingleKeyMap)

      val remoteHostRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[String, Map[Int, Long]](allKey.remoteHost -> costToCount))
      }.reduceByKey(mergeSingleKeyMap)

      val spanRemoteAppRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[(String, String), Map[Int, Long]]((allKey.spanName, allKey.remoteAppKey) -> costToCount))
      }.reduceByKey(mergeDualKeyMap)

      val spanLocalHostRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[(String, String), Map[Int, Long]]((allKey.spanName, allKey.localHost) -> costToCount))
      }.reduceByKey(mergeDualKeyMap)

      val spanRemoteHostRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[(String, String), Map[Int, Long]]((allKey.spanName, allKey.remoteHost) -> costToCount))
      }.reduceByKey(mergeDualKeyMap)

      val localHostRemoteAppRdd = infos.map { case (allKey, costToCount) =>
        (new ModuleInvokeKey(allKey.localAppKey, allKey.ts, allKey._type, allKey.envStr),
          Map[(String, String), Map[Int, Long]]((allKey.localHost, allKey.remoteAppKey) -> costToCount))
      }.reduceByKey(mergeDualKeyMap)

      //  计算statData,并且用于hive存储的格式
      val spanAllHive = spanAllRdd.flatMap { case (hourKey, resultMap) =>
        resultMap.toSeq.flatMap { case (tuple, costToCount) =>
          val tags = getTags(spanname = tuple)
          val statDataList = Calculator.calculate(hourKey, immutable.Seq(StatGroup.Span, StatGroup.SpanRemoteApp,
            StatGroup.SpanLocalHost, StatGroup.SpanRemoteHost, StatGroup.LocalHostRemoteApp),
            tags, costToCount)(StatRange.Hour)
          statDataList.map { statData =>
            transformStatDataToHiveData(statData)
          }
        }
      }
      val spanHive = spanRdd.flatMap { case (hourKey, resultMap) =>
        resultMap.toSeq.flatMap { case (tuple, costToCount) =>
          val tags = getTags(spanname = tuple)
          val statDataList = Calculator.calculate(hourKey, immutable.Seq(StatGroup.Span, StatGroup.SpanRemoteApp,
            StatGroup.SpanLocalHost, StatGroup.SpanRemoteHost), tags, costToCount)(StatRange.Hour)
          statDataList.map { statData =>
            transformStatDataToHiveData(statData)
          }
        }
      }

      val localhostHive = localhostRdd.flatMap { case (hourKey, resultMap) =>
        resultMap.toSeq.flatMap { case (tuple, costToCount) =>
          val tags = getTags(localHost = tuple)
          val statDataList = Calculator.calculate(hourKey, immutable.Seq(StatGroup.SpanLocalHost,
            StatGroup.LocalHostRemoteApp), tags, costToCount)(StatRange.Hour)
          statDataList.map { statData =>
            transformStatDataToHiveData(statData)
          }
        }
      }
      val remoteAppHive = remoteAppRdd.flatMap { case (hourKey, resultMap) =>
        resultMap.toSeq.flatMap { case (tuple, costToCount) =>
          val tags = getTags(remoteAppKey = tuple)
          val statDataList = Calculator.calculate(hourKey, immutable.Seq(StatGroup.SpanRemoteApp,
            StatGroup.LocalHostRemoteApp), tags, costToCount)(StatRange.Hour)
          statDataList.map { statData =>
            transformStatDataToHiveData(statData)
          }
        }
      }

      val remoteHostHive = remoteHostRdd.flatMap { case (hourKey, resultMap) =>
        resultMap.toSeq.flatMap { case (tuple, costToCount) =>
          val tags = getTags(remoteHost = tuple)
          val statDataList = Calculator.calculate(hourKey, immutable.Seq(StatGroup.SpanRemoteHost),
            tags, costToCount)(StatRange.Hour)
          statDataList.map { statData =>
            transformStatDataToHiveData(statData)
          }
        }
      }
      val spanRemoteAppHive = rddToHiveData(spanRemoteAppRdd, StatGroup.SpanRemoteApp)(StatRange.Hour)
      val spanLocalHostHive = rddToHiveData(spanLocalHostRdd, StatGroup.SpanLocalHost)(StatRange.Hour)
      val spanRemoteHostHive = rddToHiveData(spanRemoteHostRdd, StatGroup.SpanRemoteHost)(StatRange.Hour)
      val localHostRemoteAppHive = rddToHiveData(localHostRemoteAppRdd, StatGroup.LocalHostRemoteApp)(StatRange.Hour)


      //  将所有结果rdd union求得并集
      val allRdd = sc.union {
        Seq(spanAllHive, spanHive, localhostHive, remoteAppHive, remoteHostHive, spanRemoteAppHive, spanLocalHostHive,
          spanRemoteHostHive, localHostRemoteAppHive)
      }.persist(StorageLevel.MEMORY_ONLY_SER)

      //  配置hiveContext
      sqlContext.sql("set hive.exec.dynamic.partition=true")
      sqlContext.sql("set hive.exec.dynamic.partition.mode=nonstrict")

      // 插入数据至hive
      insertToHive(allRdd, role2TableNameMap, sqlContext, dayTuple._1)

      //  去持久化,释放磁盘与内存,为后续运算留出空间
      allRdd.unpersist()
      infos.unpersist()
    }

    sc.stop()
  }

}



