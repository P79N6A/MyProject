package com.sankuai.octo
import java.util.concurrent.atomic.AtomicInteger

import com.sankuai.msgp.common.model.ServiceModels
import dispatch.{Http, as, url}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

@RunWith(classOf[JUnitRunner])
class AppkeyProviderSuite extends FunSuite with BeforeAndAfter {

  val hostname = "octo.sankuai.com"

  implicit val timeout = Duration.create(30, duration.SECONDS)

  test("get provider") {
    val counter = new AtomicInteger()
    val errorCounter = new AtomicInteger()
    val start  = System.currentTimeMillis()
    while (true){
      try {
        val httpUrl = s"http://$hostname/api/provider?appkey=com.sankuai.waimai.service.poisearch&env=3&type=1"
        val getReq = url(httpUrl)
        val feature = Http(getReq > as.String)
        val text = Await.result(feature, timeout)
        val data = (Json.parse(text) \ "data").asOpt[List[ServiceModels.ProviderSimple]]
        val size = data.getOrElse(List()).size;
        counter.incrementAndGet()
        if (size < 1) {
          errorCounter.incrementAndGet()
        }
      }
      catch {
        case e: Exception  =>
          errorCounter.incrementAndGet()
//          println(e)
      }
      if (counter.get() % 100 == 0) {
        println(s"累积耗时 ${System.currentTimeMillis() - start},累积总数：${counter.get()},累积失败次数${errorCounter.get()}")
      }
    }

  }
}