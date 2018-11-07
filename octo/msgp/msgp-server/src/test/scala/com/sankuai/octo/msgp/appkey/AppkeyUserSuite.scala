package com.sankuai.octo.msgp.appkey

import java.net.URI

import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.msgp.common.model.ServiceModels.Desc
import org.apache.http.HttpEntity
import org.apache.http.client.CookieStore
import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{BasicCookieStore, CloseableHttpClient, HttpClients}
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.util.EntityUtils
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsArray, Json}

import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.duration
import scala.concurrent.duration.Duration

/**
 * 遍历所有的appkey
 * 根据服务树获取所有的服务负责人
 * 把现有的服务负责人和服务树负责人合并
 */
@RunWith(classOf[JUnitRunner])
class AppkeyUserSuite extends FunSuite with BeforeAndAfter {
  val LOG: Logger = LoggerFactory.getLogger("AppkeyUserSuite")
  private val taskSuppertPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))
  val ssoid = "4282a648dc*b456f933134d899613d1d"
  implicit val timeout = Duration.create(60L, duration.SECONDS)
  //  val hostname = "octo.test.sankuai.info"
  //  val hostname = "localhost:8080"
  val hostname = "octo.sankuai.com"


  test("owners") {
    val tag = getTag("com.sankuai.inf.msgp")
    if(tag.isDefined){
      val admin = OpsService.getRDAdmin(tag.get)
      println(admin)
    }
  }


  def getTag(appkey:String)={
    OpsService.getAppkeyTag(appkey)
  }
  def getAppkey(): List[Desc] = {
    val url = s"http://$hostname/service/filter?business=-1&type=4&pageNo=1&pageSize=20000"
    //    val url = s"http://octo.sankuai.com/service/filter?business=-1&type=4&pageNo=1&pageSize=20000"
    val content = httpGet(url)
    val appkeyDescs = (Json.parse(content) \ "data").asInstanceOf[JsArray].value.toSeq
    val list = ListBuffer[Desc]()
    appkeyDescs.foreach {
      appkeyDesc =>
        Json.parse(appkeyDesc.toString()).validate[Desc].fold({ error =>
          println(error)
          None
        }, {
          value => list.append(value)
        })
    }
    list.toList
  }


  def httpGet(urlStr: String) = {
    var result: String = ""
    try {
      val httpget: HttpGet = new HttpGet(urlStr)
      val cookieStore: CookieStore = new BasicCookieStore
      val cookie: BasicClientCookie = new BasicClientCookie("ssoid", ssoid)
      cookie.setVersion(0)
      val uri: URI = httpget.getURI
      cookie.setDomain(uri.getHost)
      cookie.setPath("/")
      cookieStore.addCookie(cookie)
      val httpclient: CloseableHttpClient = HttpClients.custom.setDefaultCookieStore(cookieStore).build
      val requestConfig: RequestConfig = RequestConfig.custom.setSocketTimeout(20000).setConnectTimeout(20000).setCookieSpec(CookieSpecs.STANDARD).build
      System.out.println("Executing request " + httpget.getRequestLine)
      httpget.setConfig(requestConfig)
      val response: CloseableHttpResponse = httpclient.execute(httpget)
      try {
        val entity: HttpEntity = response.getEntity
        if (entity != null) {
          result = EntityUtils.toString(response.getEntity)
        }
      } finally {
        response.close
        httpclient.close
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
    result
  }
}
