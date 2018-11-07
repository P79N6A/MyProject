package com.sankuai.octo.msgp

import com.ning.http.client.cookie.Cookie
import com.sankuai.msgp.common.utils.helper.CommonHelper
import com.sankuai.msgp.common.model.ServiceModels.ProviderSimple
import dispatch.url
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import play.api.libs.json.Json

import scala.concurrent.duration
import scala.concurrent.duration.Duration

/**
 * Created by zava on 16/8/11.
 */
@RunWith(classOf[JUnitRunner])
class CommonSuite extends FunSuite with BeforeAndAfter {

  implicit val timeout = Duration.create(30, duration.SECONDS)

  test("idc") {
    val ips = List("10.5.238.230", "10.32.120.156")
    ips.map{
      ip=>
      val idc = CommonHelper.ip2IDC(ip)
      println(s"$ip:$idc")
    }
  }
  implicit val providerSimpleReads = Json.reads[ProviderSimple]
  implicit val providerSimpleWrites = Json.writes[ProviderSimple]
  val hostname = "octo.st.sankuai.com"
  def getAppkeyProvider(appkey:String) = {
    /*val url = s"http://$hostname/api/provider/${appkey}?type=1&pageSize=10000"
    val getReq = addHeaderAndCookie(url)
    val feature = Http(getReq > as.String)
    val content = Await.result(feature, timeout)
    val providers = (Json.parse(content) \ "data").validate[List[ProviderSimple]].asOpt.getOrElse(List[ProviderSimple]())
    providers*/
  }

  def addHeaderAndCookie(urlString: String) = {
    var result = url(urlString)
    result = result.addCookie(new Cookie("skmtutc", "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=",
      "anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE=", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("SID", "570th7p1bt26ksolu59n401164", "570th7p1bt26ksolu59n401164", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("misId", "hanjiancheng", "hanjiancheng", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("misId.sig", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "R6t6zhbvKEhWCv0U1TVz6SuDRQk", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userId", "64137", "64137", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userId.sig", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "rTbO-Kqsm5A1-tTXf40dHyv7PiY", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userName", "%E9%9F%A9%E5%BB%BA%E6%88%90", "%E9%9F%A9%E5%BB%BA%E6%88%90", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("userName.sig", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oDdXTbNSdFWkL8A51ojyIOMsgmM", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("__mta", "149770889.1445523886869.1445523886869.1452239177404.2", "149770889.1445523886869.1445523886869.1452239177404.2", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("_ga", "GA1.2.1016736437.1445859649", "GA1.2.1016736437.1445859649", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("ssoid", "d722c5a90bfa401f8aeb5b7c1ec115fc", "d722c5a90bfa401f8aeb5b7c1ec115fc", "oct.sankuai.com", "/", -1, 2000, false, true))
      .addCookie(new Cookie("JSESSIONID", "15hytutkzkqyswfrme8hmssj8", "15hytutkzkqyswfrme8hmssj8", "oct.sankuai.com", "/", -1, 2000, false, true))
    result
  }

}
