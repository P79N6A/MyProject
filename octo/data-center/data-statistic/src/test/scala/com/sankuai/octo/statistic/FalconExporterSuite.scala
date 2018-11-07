package com.sankuai.octo.statistic

//import com.sankuai.octo.statistic.exporter.DefaultExporterProxy
import com.sankuai.octo.statistic.helper.{DailyMetricHelper, api}
import com.sankuai.octo.statistic.model._
import com.sankuai.octo.statistic.util.tair
import org.scalatest.FunSuite

/**
 * Created by wujinwu on 15/9/24.
 */
class FalconExporterSuite extends FunSuite {

  /*
    test("testFalconExport") {
      val tags = Map(
        Constants.SPAN_NAME -> "spanname",
        Constants.LOCAL_HOST -> "localHost",
        Constants.REMOTE_APPKEY -> "remoteAppKey",
        Constants.REMOTE_HOST -> "remoteHost")
      val str = TreeMap(tags.toSeq: _*).map(x => s"${x._1}=${x._2}").mkString(",")
      println(str)


      val falcon = new FalconSuite()
      val start = new DateTime().minusDays(2).getMillis / 1000
      val end = new DateTime().getMillis / 1000
      val count = (end - start) / 60
      val appkey = "com.sankuai.inf.falcontest"

      (0 to 2000).foreach(
        x => {
          val ts = System.currentTimeMillis() / 1000L
          val data = falcon.randStat(ts.toInt, appkey = "com.sankuai.inf.falcontest", group = StatGroup.Span,
            source = StatSource.Server)
          DefaultExporterProxy.export(data)
          Thread.sleep(10)
        }
      )
    }
  */
  test("testTag") {
    def getTagTairkey(key: TagKey) = s"${key.env}|daily|tags|${key.appkey}|${key.ts}|${key.source}"
    val appkey = "com.sankuai.inf.falcontest"
    val tagkey = TagKey(appkey, DailyMetricHelper.dayStart(System.currentTimeMillis()), StatEnv.Prod, StatSource.Server)
    val tairKey = getTagTairkey(tagkey)
    println(s"tagkey:$tagkey tairKey :$tairKey")
    tair.get(tairKey) match {
      case Some(bytes) =>
        val tag = api.toObject(bytes, classOf[Tag])
        println(tag)
      case None =>
    }

  }

}
