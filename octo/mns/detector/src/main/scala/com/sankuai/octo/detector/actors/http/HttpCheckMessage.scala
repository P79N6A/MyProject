
package com.sankuai.octo.detector.actors.http

/**
 * Copyright (C) 2015 Meituan* All rights reserved
 * User: gaosheng
 * Date: 16-7-4
 * Time: 下午5:55
 */
class HttpCheckMessage(r: Int, url: String) {
  private val round = r
  private val checkUrl = url

  def getRound() = round

  def getCheckUrl = checkUrl
}
