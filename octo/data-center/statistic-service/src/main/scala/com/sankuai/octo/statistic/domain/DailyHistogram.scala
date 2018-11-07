package com.sankuai.octo.statistic.domain

import com.sankuai.octo.statistic.metrics.SimpleCountHistogram2

case class DailyHistogram(appkey: String, name: String, histogram: SimpleCountHistogram2)