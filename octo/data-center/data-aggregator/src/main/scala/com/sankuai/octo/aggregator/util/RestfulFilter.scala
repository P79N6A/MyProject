package com.sankuai.octo.aggregator.util

import java.util.regex.Pattern

/**
  * Created by wujinwu on 15/12/17.
  */
object RestfulFilter {

  private val pattern = Pattern.compile("([/\\-\\.,_])(\\d+)([/\\-\\.,_])")
  private val patternForTail = Pattern.compile("([/\\-\\.,_])(\\d+)($)")

  private val patternForJessionId = Pattern.compile("(jsessionid=)(.*+)")

  def cleanSpanName(spanName: String) = {
    var input = spanName
    var matcher = pattern.matcher(spanName)
    while (matcher.find()) {
      input = matcher.replaceAll("$1*$3")
      matcher = pattern.matcher(input)
    }
    //    println(input)
    val m2 = patternForTail.matcher(input)
    if (m2.find()) {
      input = m2.replaceAll("$1*$3")
    }

    val m3 = patternForJessionId.matcher(input)
    if (m3.find()) {
      input = m3.replaceAll("$1")
    }
    //  连续的多个通过','分隔的'*'去除
    input = input.replaceAll("(,\\*)+", "")
    input = input.replaceAll("\\t|\\n","")
    input
  }
}
