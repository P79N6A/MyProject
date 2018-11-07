package com.sankuai.octo.mnsc.test

import com.sankuai.octo.mnsc.dataCache.appProviderDataCache
import com.sankuai.octo.mnsc.model.{Env, Path}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.junit.Assert.assertEquals
import org.slf4j.{Logger, LoggerFactory};

@RunWith(classOf[JUnitRunner])
class ProvidersSuite extends FunSuite with BeforeAndAfter {
  test("envInvalid") {
    assertEquals(false, Env.isValid(4));
    assertEquals(true, Env.isValid(1));
    assertEquals(true, Env.isValid(2));
    assertEquals(true, Env.isValid(3));
  }

  test("test getProviderPar") {
    val startTime = System.currentTimeMillis()
    val endTime = System.currentTimeMillis()
    println(endTime- startTime)


  }



}

