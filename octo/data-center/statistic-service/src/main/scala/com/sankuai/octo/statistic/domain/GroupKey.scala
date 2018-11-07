package com.sankuai.octo.statistic.domain

import com.sankuai.octo.statistic.model.{StatGroup, StatRange}

case class GroupKey(ts: Int, range: StatRange, group: StatGroup, statTag: StatTag)