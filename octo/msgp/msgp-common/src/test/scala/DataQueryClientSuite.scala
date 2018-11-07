import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by yves on 17/6/20.
  */
class DataQueryClientSuite extends FunSuite with BeforeAndAfter {

  /*private implicit val timeout = Duration.create(20000, TimeUnit.MILLISECONDS)

  test("queryTag") {
    val start = System.currentTimeMillis()

    val testCount = 10
    val result = (0 to testCount).map {
      x =>
        DataQueryClient.queryTag("com.sankuai.inf.msgp", 1497283200, 1497369600, "prod", "client")
    }
    val end = System.currentTimeMillis()
    println((end - start) / testCount)
  }

  test("queryHistoryData") {
    val start: Int = 1498723200
    val end: Int = 1498723500
    val env: String = "prod"
    val source: String = "server"
    val appkey: String = "com.sankuai.inf.msgp"
    val testCount = 500

    System.out.println(DataQueryClient.queryHistoryData("com.sankuai.inf.msgp", start, end, "", source, "", env, "minute", "all", "all", "", "", "", "hbase"))


    var testStart = System.currentTimeMillis()
    (0 to testCount).map {
      x =>
        DataQuery.getHistoryStatistic("com.sankuai.inf.msgp", start, end, "", source, "", env,
          "hour", "", "*", "all", "all", "all", "hbase")(timeout)
    }
    var testEnd = System.currentTimeMillis()
    println(s"queryHistoryData with Http interface cost ${(testEnd - testStart) / testCount}(mean)\n")

    testStart = System.currentTimeMillis()

    (0 to testCount).map {
      x =>
        DataQueryClient.queryHistoryData("com.sankuai.inf.msgp", start, end, "", source, "", env,
          "hour", "", "*", "all", "all", "all", "hbase")
    }
    testEnd = System.currentTimeMillis()
    println(s"queryHistoryData with Thrift interface cost ${(testEnd - testStart) / testCount}(mean)\n")

    /*testStart = System.currentTimeMillis()
    (0 to testCount).map {
      x =>
        DataQueryClient.queryHistoryDataMerged("com.sankuai.inf.msgp", start, end, "", source, "", env, "hour", "*", "all", "all", "all", "", "hbase")
    }

    testEnd = System.currentTimeMillis()
    println(s"queryHistoryDataMerged with thrift interface cost ${(testEnd - testStart) / testCount}(mean)")

    testStart = System.currentTimeMillis()
    (0 to testCount).map {
      x =>
        DataQuery.getHistoryStatisticMerged("com.sankuai.inf.msgp", start, end, "", source, "", env,
          "hour", "", "*", "all", "all", "all", "hbase", true)(timeout)
    }
    testEnd = System.currentTimeMillis()
    println(s"queryHistoryDataMerged with thrift interface cost ${(testEnd - testStart) / testCount}(mean)\n")*/
  }

  test("query daily data") {
    val env: String = "prod"
    val source: String = "server"
    val testCount = 50
    val appkey: String = "octo.sankuai.inf.mnsc"

    val dateTime = new DateTime().withTimeAtStartOfDay()
    val ts = dateTime.getMillis.toInt

    var testStart = System.currentTimeMillis()
    (0 to testCount).map {
      x =>
        DataQueryClient.queryDailyData(appkey, ts, env, source)
    }
    var testEnd = System.currentTimeMillis()
    println(s"queryDailyData with thrift interface cost ${(testEnd - testStart) / testCount}(mean)")

    testStart = System.currentTimeMillis()
    (0 to testCount).map {
      x =>
        val dateTime = new DateTime().withTimeAtStartOfDay()
        val result = DataQuery.getDailyStatistic(appkey, env, dateTime, source)
        Json.parse(result).validate[List[StatisticWithHttpCode]]
    }
    testEnd = System.currentTimeMillis()
    println(s"queryDailyData with http interface cost ${(testEnd - testStart) / testCount}(mean)")
  }*/
}
