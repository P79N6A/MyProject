package com.sankuai.octo.statistic.constant

import java.nio.charset.StandardCharsets._

import com.sankuai.octo.statistic.model.{PerfRole, StatRange}

/**
  * Created by wujinwu on 16/1/22.
  */
object HBaseConstants {

  // hbase表名的映射关系
  val PERF_TABLE_NAME_MAP = Map(
    PerfRole.SERVER ->
      Map(StatRange.Day -> "octolog.serv.perf.day.index",
        StatRange.Hour -> "octolog.serv.perf.hour.index",
        StatRange.Minute -> "octolog.serv.perf.min.index"),
    PerfRole.CLIENT ->
      Map(StatRange.Day -> "octolog.cli.perf.day.index",
        StatRange.Hour -> "octolog.cli.perf.hour.index",
        StatRange.Minute -> "octolog.cli.perf.min.index"))

  val PERF_COLUMN_FAMILY = "D".getBytes(UTF_8)

  val PERF_COUNT_COLUMN = "count".getBytes(UTF_8)
  val PERF_QPS_COLUMN = "qps".getBytes(UTF_8)
  val PERF_TP50_COLUMN = "tp50".getBytes(UTF_8)
  val PERF_TP90_COLUMN = "tp90".getBytes(UTF_8)
  val PERF_TP95_COLUMN = "tp95".getBytes(UTF_8)
  val PERF_TP99_COLUMN = "tp99".getBytes(UTF_8)
  val PERF_COST_MAX_COLUMN = "costMax".getBytes(UTF_8)
  val PERF_COST_DATA_COLUMN = "costData".getBytes(UTF_8)
}
