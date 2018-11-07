package com.sankuai.octo.query.scala

import com.sankuai.octo.query.helper.DailyQueryHelper
import com.sankuai.octo.statistic.helper.DailyMetricHelper
import com.sankuai.octo.statistic.model.StatData
import com.sankuai.octo.statistic.util.tair
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.LoggerFactory
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

import scala.collection.JavaConversions._
/**
 * Created by zava on 15/9/15.
  *
 */
@ContextConfiguration(locations = Array("classpath:applicationContext.xml"))
class DailyQueryHandlerSuite extends FunSuite with BeforeAndAfter {

  private val logger = LoggerFactory.getLogger(this.getClass)

  /**
    * Use the TestContextManager, as this caches the contexts so that they aren't rebuilt every test. It is configured from the class annotations.
    */
  new TestContextManager(this.getClass).prepareTestInstance(this)

  //如果 all的max时间小于其他时间为 异常
  test("dailyMetrics") {
    var appkey = "com.sankuai.inf.test3.logCollector"
    appkey = "com.sankuai.banma.monitor"
    //    val appkey = "com.sankuai.inf.logCollector"
    val ts = DailyMetricHelper.dayStart(System.currentTimeMillis)
    val env = "prod"
    val statDatas = DailyQueryHelper.dailyMetrics(appkey, ts, env, "server")
    logger.info(s"size:${statDatas.size}")
    var costMax = 0.0
    var allCostMax = 0.0
    statDatas.foreach { statData =>
      if (statData.getCostMax > costMax)
        costMax = statData.getCostMax
      if (statData.getTags.spanname == "all") {
        allCostMax = statData.getCostMax
      }
      logger.info("log:{}", statData)
    }
    logger.info(s"max $costMax allCostMax $allCostMax")
    if (costMax > allCostMax) {
      logger.error("all-costMax not max time")
    }
  }
  //删除统计数据
  test("deletedailyMetrics") {
    //    val appkey = "com.sankuai.inf.logCollector"
    val appkeys = Array(
//    "com.sankuai.inf.test3.logCollector"
      "com.sankuai.inf.logCollector", "com.meituan.banma.api", "com.sankuai.inf.mnsc",
      "com.sankuai.waimai.poiquery", "com.sankuai.waimai.poi",
      "waimai_api","com.sankuai.waimai.money"

    )
    //"com.sankuai.inf.test3.logCollector"
    val ts = DailyMetricHelper.dayStart(System.currentTimeMillis)
    val env = "prod"
    appkeys.foreach {
      appkey =>
        val tagsKey = DailyMetricHelper.dailyTagsTairKey(appkey, ts, env)
        val tagsValue = tair.getValue(tagsKey)
        logger.info("tagsKey:{}", tagsKey)
        tagsValue match {
          case Some(tagsBytes) =>
            val tags = DailyMetricHelper.asDailyTags(tagsBytes)
            logger.info("spanname:{}", tags.spanNames)
            tags.spanNames.foreach {
              spanName =>
                val key = DailyMetricHelper.dailyMetricTairKey(appkey, spanName, ts, env, "server")
                tair.del(key)
                logger.info("key:{}", key)
                val histogramKey = getTairHistogramKey(env, s"$appkey|$spanName|$ts")
                //prod|daily|stat|histogram|com.sankuai.inf.test8.logCollector|testapi.span2|1442419200
                logger.info("histogramKey:{}", histogramKey)
                tair.del(histogramKey);

            }
          case None =>
            List[StatData]()
        }
        tair.del(tagsKey)
    }


  }

  private def getTairHistogramKey(env: String, name: String) = s"$env|daily|stat|histogram|$name"

}
