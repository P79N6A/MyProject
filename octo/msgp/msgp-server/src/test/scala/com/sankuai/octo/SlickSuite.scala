package com.sankuai.octo

import java.util.Date

import com.sankuai.msgp.common.config.db.msgp.Tables._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.MySQLDriver.simple._

@RunWith(classOf[JUnitRunner])
class SlickSuite extends FunSuite with BeforeAndAfter {

  test("gen") {
    scala.slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.20.116.73:5002/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/nero/Desktop",
        "com.sankuai.octo.msgp.db")
    )
  }

  test("wyzgen") {
    scala.slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://192.168.12.176:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/wangyanzhao/project/project-web/octo/msgp/msgp-server/src/main/scala",
        "com.sankuai.octo.msgp.db")
    )
  }

  test("xintaogen") {
    scala.slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://192.168.12.176:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/dreamblossom/project/overloadHotfix/msgp/msgp-server/src/main/scala",
        "com.sankuai.octo.msgp.db")
    )
  }

  test("event") {
    val db = Database.forURL(url = "jdbc:mysql://192.168.12.176:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8", driver = "com.mysql.jdbc.Driver")
    db withSession {
      implicit session: Session =>
        // select all
        val q = Event.list
        val list = ((Event.sortBy(_.item)).sortBy(_.createTime)).list
        println("list+++++++");
        println(list);

        val len = Event.filter(x => x.appkey === "test").length.run
        println(len)

        val c = Event.filter(x => x.appkey === "test").length.run
        println(c)
        println(Event.length.run)

        val now = System.currentTimeMillis()
        // insert
        Event.map(p => (p.appkey, p.side, p.item, p.status, p.createTime, p.ackTime, p.message)) +=
          ("com.meituan.mtthrift.demo.server", "server", "testest", 1, now, now, "")
        // select
        println(Event.list)
        val b = Event.filter(x => x.appkey === "test")
        println(b.selectStatement)
        println(b.first)
        // update
        val a = Event.filter(x => x.appkey === "test").map(r => r.status)
        println(a.selectStatement)
        println(a.updateStatement)
        a.update(2)
    }
  }

  test("slickStatement"){
    val db = Database.forURL(url = "jdbc:mysql://192.168.12.176:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8", driver = "com.mysql.jdbc.Driver")
    db withSession {
      implicit session: Session =>
        val start = 1419328237636L
        val end = start + 20 * 24 * 3600 * 1000
        val xNum = 20 * 24
        val format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:00:00")
        val xList = ( 0 to xNum).map( x => format.format( new Date(start + x * 3600 * 1000)) ).toList
        println(xList)
        println(xList.length)
        case class serie(name:String, data:List[Any])
        val allEvent = Event.map(x => (x.item, x.createTime)).list.map(x => (x._1, format.format(new Date(x._2))))
        val legend = allEvent.groupBy(_._1).map(_._1)
        println(legend)
        val xyMap = allEvent.groupBy(_._1).map(x => (x._1,x._2.groupBy(_._2).map(x => (x._1,x._2.length))))
        println(xyMap)
        val xAxisList = xyMap.values.flatMap(_.keySet).toSet.toList.sortWith((x,nextX) => x.compareTo(nextX) < 0)
        val yAxisList = legend.foldLeft(List[Any]()){
          (result,self) => {
            val valueMap = xyMap(self)
            val selfResult = xAxisList.foldLeft(List[Any]()){
              (result,self) => {
                valueMap.get(self).fold{
                  result :+ 0
                }{
                  self => result :+ self
                }
              }
            }
            result :+ serie(self, selfResult)
          }
        }
        println(xAxisList)
        println(xAxisList.length)
        println(yAxisList)
    }
  }

  test("appkeytrigger") {
    val db = Database.forURL(url = "jdbc:mysql://192.168.12.176:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8", driver = "com.mysql.jdbc.Driver")
    val appkey = "com.sankuai.inf.optlog10"
    val side = "server"
    db withSession{
      implicit session: Session =>
        val triggers = AppkeyTrigger.filter(x => x.appkey === appkey && x.side === side).list
        //val trigger = triggers(0)
        println(triggers)
    }
  }
}