package com.sankuai.octo.spark

import com.sankuai.octo.spark.domain.{HiveModuleData, ModuleInvokeKey}
import com.sankuai.octo.statistic.constant.Constants
import com.sankuai.octo.statistic.helper.HBaseHelper._
import com.sankuai.octo.statistic.helper.{HBaseHelper, PerfHelper, Serializer}
import com.sankuai.octo.statistic.model.{PerfRole, StatData, StatGroup, StatRange}
import org.apache.commons.lang3.ArrayUtils
import org.apache.hadoop.hbase.client.{Durability, HConnectionManager, HTable, Put}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{HFileOutputFormat2, TableOutputFormat}
import org.apache.hadoop.hbase.security.UserProvider
import org.apache.hadoop.hbase.security.token.TokenUtil
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, KeyValue}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.security.UserGroupInformation
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, Days}
import org.springframework.util.CollectionUtils

import scala.collection.mutable.ArrayBuffer

object Helper {

  def parseDate(args: Array[String]) = {
    require(args.length >= 1)
    val startDate = args(0)
    if (args.length == 1) {
      Seq(getDateTuple(startDate))
    } else {
      val dtf = DateTimeFormat.forPattern("yyyyMMdd")
      val endDate = args(1)
      val start = dtf.parseDateTime(startDate)
      val end = dtf.parseDateTime(endDate)

      val dayCount = Days.daysBetween(start, end).getDays
      val res = (0 to dayCount).map { i =>
        val dateTimeStr = start.plusDays(i).toString(dtf)
        getDateTuple(dateTimeStr)
      }
      res
    }

  }

  private def getDateTuple(dateTimeStr: String) = {
    val dtf = DateTimeFormat.forPattern("yyyyMMdd")
    val dateTime = dtf.parseDateTime(dateTimeStr)

    val startOfDay = dateTime.withTimeAtStartOfDay()
    val startMs = startOfDay.getMillis
    val endMs = startMs + 86400000L

    (dateTimeStr, startMs, endMs)
  }

  def getDayStart(ts: Long) = {
    val dateTime = new DateTime(ts, DateTimeZone.forID("Asia/Shanghai"))
    val startOfDay = dateTime.withTimeAtStartOfDay()
    (startOfDay.getMillis / 1000L).toInt
  }


  def getHourStart(ts: Long = System.currentTimeMillis()) = {
    val dateTime = new DateTime(ts, DateTimeZone.forID("Asia/Shanghai"))
    val res = (dateTime.withMinuteOfHour(0).withSecondOfMinute(0).getMillis / 1000L).toInt
    res
  }

  // 获取一个时间戳开始分钟的时间戳，输入毫秒，输出秒(falcon是秒)
  def getMinuteStart(ts: Long) = {
    (ts / 1000 / 60 * 60).toInt
  }

  def rddToHiveData(rdd: RDD[(ModuleInvokeKey, Map[(String, String), Map[Int, Long]])], statGroup: StatGroup)
                   (range: StatRange) = {
    rdd.flatMap { case (key, resultMap) =>
      resultMap.toSeq.map { case (tuple, costToCount) =>
        val tags = statGroup match {
          case StatGroup.SpanLocalHost => getTags(spanname = tuple._1, localHost = tuple._2)
          case StatGroup.SpanRemoteApp => getTags(spanname = tuple._1, remoteAppKey = tuple._2)
          case StatGroup.Span => getTags(spanname = tuple._1)
          case StatGroup.SpanRemoteHost => getTags(spanname = tuple._1, remoteHost = tuple._2)
          case StatGroup.LocalHostRemoteHost => getTags(localHost = tuple._1, remoteHost = tuple._2)
          case StatGroup.LocalHostRemoteApp => getTags(localHost = tuple._1, remoteAppKey = tuple._2)
        }
        val statData = Calculator.calculate(key, statGroup, tags, costToCount)(range)
        transformStatDataToHiveData(statData)
      }
    }

  }

  def transformStatDataToHiveData(statData: StatData) = {
    val perf = PerfHelper.statDataToPerfData(statData)
    val rowKey = HBaseHelper.generateRowKey(perf)
    val costData = if (perf.getCostData != null && !CollectionUtils.isEmpty(perf.getCostData.getCostDataList)) {
      Serializer.toBytes(perf.getCostData)
    } else {
      Array[Byte]()
    }
    val role = perf.getRole match {
      case PerfRole.CLIENT => 0
      case PerfRole.SERVER => 1
    }
    (role, HiveModuleData(rowKey, perf.getCount, perf.getQps,
      perf.getCost50, perf.getCost90, perf.getCost95, perf.getCost99, perf.getCostMax, costData))
  }

  def getTags(spanname: String = Constants.ALL, localHost: String = Constants.ALL,
              remoteAppKey: String = Constants.ALL, remoteHost: String = Constants.ALL) = {
    Map(Constants.SPAN_NAME -> spanname,
      Constants.LOCAL_HOST -> localHost,
      Constants.REMOTE_APPKEY -> remoteAppKey,
      Constants.REMOTE_HOST -> remoteHost)
  }

  def insertToHive(rdd: RDD[(Int, HiveModuleData)], role2TableNameMap: Map[Int, String], sqlContext: HiveContext, dt: String) = {
    import sqlContext.implicits._
    role2TableNameMap.foreach { case (role, tableName) =>
      val filterRdd = rdd.filter(entry => entry._1 == role).map(_._2)
      filterRdd.toDF().registerTempTable(tableName)
      sqlContext.sql(s"INSERT OVERWRITE TABLE mart_inf.$tableName PARTITION(dt=$dt) " +
        s" SELECT rowkey,count,qps,tp50,tp90,tp95,tp99,cost_max,cost_data FROM $tableName")
    }

  }

  def insertToHBase(rdd: RDD[(Int, HiveModuleData)], role2TableNameMap: Map[Int, String]) = {

    role2TableNameMap.foreach { case (role, tableName) =>
      val filterRdd = rdd.filter(_._1 == role).map(_._2)

      val resRdd = filterRdd.map(convertToPut)
      val conf = HBaseConfiguration.create()
      // IMPORTANT: obtain token to access hbase
      TokenUtil.obtainAndCacheToken(HConnectionManager.createConnection(conf),
        UserProvider.instantiate(conf).create(UserGroupInformation.getCurrentUser))

      val table = new HTable(conf, tableName)

      conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
      val job = Job.getInstance(conf)
      job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
      job.setMapOutputValueClass(classOf[Put])
      HFileOutputFormat2.configureIncrementalLoad(job, table)
//      job.setReducerClass(classOf[Comparator])

      // Directly bulk load to Hbase/MapRDB tables.
      resRdd.repartition(1).saveAsNewAPIHadoopFile(s"/user/hadoop-inf/$tableName", classOf[ImmutableBytesWritable], classOf[Put], classOf[HFileOutputFormat2], job.getConfiguration)

    }
  }

  /*  // new Hadoop API configuration
    val newAPIJobConfig: Job = Job.getInstance(hconf)
    newAPIJobConfig.getConfiguration.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    newAPIJobConfig.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])
    filterRdd.sortBy(_.rowkey)
    val localData = filterRdd.map(convert)
    localData.saveAsNewAPIHadoopDataset(newAPIJobConfig.getConfiguration)






    val conf = HBaseConfiguration.create()
    // IMPORTANT: obtain token to access hbase
    TokenUtil.obtainAndCacheToken(HConnectionManager.createConnection(conf),
      UserProvider.instantiate(conf).create(UserGroupInformation.getCurrentUser))


    val conf = HBaseConfiguration.create()
    val tableName = "hao"
    val table = new HTable(conf, tableName)

    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    val job = Job.getInstance(conf)
    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setMapOutputValueClass(classOf[KeyValue])
    HFileOutputFormat.configureIncrementalLoad(job, table)

    // Generate 10 sample data:

    // Directly bulk load to Hbase/MapRDB tables.
    rdd.saveAsNewAPIHadoopFile("/tmp/xxxx19", classOf[ImmutableBytesWritable], classOf[KeyValue], classOf[HFileOutputFormat], job.getConfiguration())

*/


  private def convert(hiveModuleData: HiveModuleData) = {

    val array = ArrayBuffer[KeyValue]()
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, COUNT_COLUMN, Bytes.toBytes(hiveModuleData.count))
    //todo  离线运算暂时用总调用量作为可用性指标
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, SUCCESS_COUNT_COLUMN, Bytes.toBytes(hiveModuleData.count))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, EXCEPTION_COUNT_COLUMN, Bytes.toBytes(0L))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, TIMEOUT_COUNT_COLUMN, Bytes.toBytes(0L))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, DROP_COUNT_COLUMN, Bytes.toBytes(0L))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, QPS_COLUMN, Bytes.toBytes(hiveModuleData.qps))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, TP50_COLUMN, Bytes.toBytes(hiveModuleData.tp50))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, TP90_COLUMN, Bytes.toBytes(hiveModuleData.tp90))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, TP95_COLUMN, Bytes.toBytes(hiveModuleData.tp95))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, TP99_COLUMN, Bytes.toBytes(hiveModuleData.tp99))
    array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, COST_MAX_COLUMN_NEW, Bytes.toBytes(hiveModuleData.cost_max))
    if (!ArrayUtils.isEmpty(hiveModuleData.cost_data)) {
      array += new KeyValue(hiveModuleData.rowkey, COLUMN_FAMILY, COST_DATA_COLUMN_NEW, hiveModuleData.cost_data)
    }
    array.map { kv =>
      (new ImmutableBytesWritable(hiveModuleData.rowkey), kv)
    }

  }

  private def convertToPut(hiveModuleData: HiveModuleData) = {

    val put = new Put(hiveModuleData.rowkey)

    /**
      * Do not write the Mutation to the WAL
      */
    put.setDurability(Durability.SKIP_WAL)

    put.add(COLUMN_FAMILY, COUNT_COLUMN, Bytes.toBytes(hiveModuleData.count))
    //todo  离线运算暂时用总调用量作为可用性指标
    put.add(COLUMN_FAMILY, SUCCESS_COUNT_COLUMN, Bytes.toBytes(hiveModuleData.count))
    put.add(COLUMN_FAMILY, EXCEPTION_COUNT_COLUMN, Bytes.toBytes(0L))
    put.add(COLUMN_FAMILY, TIMEOUT_COUNT_COLUMN, Bytes.toBytes(0L))
    put.add(COLUMN_FAMILY, DROP_COUNT_COLUMN, Bytes.toBytes(0L))
    put.add(COLUMN_FAMILY, QPS_COLUMN, Bytes.toBytes(hiveModuleData.qps))
    put.add(COLUMN_FAMILY, TP50_COLUMN, Bytes.toBytes(hiveModuleData.tp50))
    put.add(COLUMN_FAMILY, TP90_COLUMN, Bytes.toBytes(hiveModuleData.tp90))
    put.add(COLUMN_FAMILY, TP95_COLUMN, Bytes.toBytes(hiveModuleData.tp95))
    put.add(COLUMN_FAMILY, TP99_COLUMN, Bytes.toBytes(hiveModuleData.tp99))
    put.add(COLUMN_FAMILY, COST_MAX_COLUMN_NEW, Bytes.toBytes(hiveModuleData.cost_max))
    if (!ArrayUtils.isEmpty(hiveModuleData.cost_data)) {
      put.add(COLUMN_FAMILY, COST_DATA_COLUMN_NEW, hiveModuleData.cost_data)
    }
    (new ImmutableBytesWritable, put)
  }


  private[spark] def mergeSingleKeyMap(map1: Map[String, Map[Int, Long]], map2: Map[String, Map[Int, Long]]) = {
    map1 ++ map2.map { case (key, costToCount) =>
      key -> mergeCostToCountMap(map1.getOrElse(key, Map[Int, Long]()), costToCount)
    }
  }

  def mergeCostToCountMap(map1: Map[Int, Long], map2: Map[Int, Long]) = {
    map1 ++ map2.map { case (k, v) => k -> (v + map1.getOrElse(k, 0L)) }
  }

  private[spark] def mergeDualKeyMap(map1: Map[(String, String), Map[Int, Long]], map2: Map[(String, String), Map[Int, Long]]) = {
    map1 ++ map2.map { case (key, costToCount) =>
      key -> mergeCostToCountMap(map1.getOrElse(key, Map[Int, Long]()), costToCount)
    }
  }


}