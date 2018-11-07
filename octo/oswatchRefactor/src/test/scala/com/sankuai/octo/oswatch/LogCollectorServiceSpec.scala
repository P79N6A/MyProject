package com.sankuai.octo.oswatch

import com.sankuai.octo.oswatch.service.LogCollectorService
import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by dreamblossom on 15/10/4.
 */
class LogCollectorServiceSpec extends FlatSpec with Matchers{
  //val logCollectorService =new LogCollectorService()
  println("getCurrentQPS : "+LogCollectorService.getCurrentQPS("com.meituan.waimai","all","prod",1443922391,60))

}
