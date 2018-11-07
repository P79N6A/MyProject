package com.sankuai.octo.spark

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HConnectionManager, Put, Result}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.security.UserProvider
import org.apache.hadoop.hbase.security.token.TokenUtil
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.security.UserGroupInformation
import org.apache.spark.{SparkConf, SparkContext}
import com.sankuai.octo.spark.Helper._

object App {
  val TABLE_NAME = "test"
  val CF_NAME = "cf"

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("SparkHBaseDemo")

    // IMPORTANT: obtain token to access hbase
    val hconf = HBaseConfiguration.create()
    TokenUtil.obtainAndCacheToken(HConnectionManager.createConnection(hconf),
      UserProvider.instantiate(hconf).create(UserGroupInformation.getCurrentUser))

    val sc = new SparkContext(conf)
    val hsc = new org.apache.spark.sql.hive.HiveContext(sc)
    val dayTuples = parseDate(args)
    dayTuples.foreach { dayTuple =>
      val dataFrame = hsc.sql(s"SELECT count(*) FROM log.mtrace  WHERE dt = ${dayTuple._1} AND start >= ${dayTuple._2} AND start < ${dayTuple._3} AND localappkey = 'com.sankuai.inf.logCollector'")
      // read from hbase
      hconf.set(TableInputFormat.INPUT_TABLE, TABLE_NAME)
      val usersRDD = sc.newAPIHadoopRDD(hconf, classOf[TableInputFormat],
        classOf[ImmutableBytesWritable], classOf[Result])

      val count = usersRDD.count()
      println("Users RDD Count:" + count)
      usersRDD.cache()

      usersRDD.collect().foreach{ case (_, result) =>
        val key = Bytes.toStringBinary(result.getRow)
        println("Row key:" + key)
      }

      // write to hbase
      val jobConf = new JobConf(hconf, this.getClass)
      jobConf.setOutputFormat(classOf[TableOutputFormat])
      jobConf.set(TableOutputFormat.OUTPUT_TABLE, TABLE_NAME)
      val rawData = List(("aaa", 14, "bbb"), ("ccc", 18, "ddd"), ("eee", 38, "fff"))
      val localData = sc.parallelize(rawData).map(convert)
      localData.saveAsHadoopDataset(jobConf)

      sc.stop()
    }

  }

  def convert(triple: (String, Int, String)) = {
    val p = new Put(Bytes.toBytes(triple._1))
    p.add(Bytes.toBytes(CF_NAME), Bytes.toBytes("c1"), Bytes.toBytes(triple._2))
    p.add(Bytes.toBytes(CF_NAME), Bytes.toBytes("c2"), Bytes.toBytes(triple._3))
    (new ImmutableBytesWritable, p)
  }
}
