package com.sankuai.octo.query.helper

import com.sankuai.octo.statistic.constant.Constants._
import com.sankuai.octo.statistic.model.StatGroup

object QueryHelper {

  def transformQueryCondition(spanname: String, localhost: String, remoteHost: String, remoteAppkey: String) = {
    if (spanname == ALL && localhost == ALL && remoteHost == ALL && remoteAppkey == ALL) {
      QueryCondition(StatGroup.Span, ALL, ALL, ALL, ALL)
    } else if (localhost == ALL && remoteHost == ALL && remoteAppkey == ALL) {
      QueryCondition(StatGroup.Span, spanname, ALL, ALL, ALL)
    } else if (spanname == ALL && remoteHost == ALL && remoteAppkey == ALL) {
      QueryCondition(StatGroup.SpanLocalHost, ALL, localhost, ALL, ALL)
    } else if (spanname == ALL && localhost == ALL && remoteAppkey == ALL) {
      QueryCondition(StatGroup.SpanRemoteHost, ALL, ALL, remoteHost, ALL)
    } else if (spanname == ALL && localhost == ALL && remoteHost == ALL) {
      QueryCondition(StatGroup.SpanRemoteApp, ALL, ALL, ALL, remoteAppkey)
    } else if (spanname == ALL && localhost == ALL) {
      QueryCondition(StatGroup.RemoteAppRemoteHost, ALL, ALL, remoteHost, remoteAppkey)
    } else if (spanname == ALL && remoteHost == ALL) {
      QueryCondition(StatGroup.LocalHostRemoteApp, ALL, localhost, ALL, remoteAppkey)
    } else if (spanname == ALL && remoteAppkey == ALL) {
      QueryCondition(StatGroup.LocalHostRemoteHost, ALL, localhost, remoteHost, ALL)
    } else if (localhost == ALL && remoteHost == ALL) {
      QueryCondition(StatGroup.SpanRemoteApp, spanname, ALL, ALL, remoteAppkey)
    } else if (localhost == ALL && remoteAppkey == ALL) {
      QueryCondition(StatGroup.SpanRemoteHost, spanname, ALL, remoteHost, ALL)
    } else if (remoteHost == ALL && remoteAppkey == ALL) {
      QueryCondition(StatGroup.SpanLocalHost, spanname, localhost, ALL, ALL)
    } else {
      //  default
      QueryCondition(StatGroup.Span, ALL, ALL, ALL, ALL)
    }
  }

}

case class QueryCondition(statGroup: StatGroup, spanname: String, localhost: String, remoteHost: String, remoteAppkey: String)
