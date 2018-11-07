package com.sankuai.octo

import com.sankuai.octo.doclet.doc.{OctoClassDoc, OctoMethodDoc, OctoTypeDoc}
import com.sankuai.octo.doclet.util.JsonUtil
import com.sankuai.octo.msgp.serivce.doc.{DocDao, DocParser, DocQuery}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class DocSuite extends FunSuite with BeforeAndAfter {

  test("exist") {
    val ret = DocDao.existMethodSign("com.sankuai.octo.demo", "rewrw")
    println(ret)
  }

  test("gsign") {
    val ret = DocDao.getLatestGSgin("com.sankuai.octo.demo")
    println(ret)
  }

  test("getDocs") {
    val ret = DocDao.getlatestDocs("com.sankuai.octo.demo")
    println(ret)
  }

  test("group docs") {
    val ret = DocQuery.groupDocs("com.sankuai.octo.demo")
    println(ret)
    val ret2 = DocQuery.groupDocs("com.sankuai.octo.demo2")
    println(ret2)
  }

  test("docs") {
    val docs = mockDocs()
    val text = JsonUtil.toJson(Map("com.sankuai.octo.demo" -> docs).asJava)
    println(text)
    val ret = DocParser.parseDocs(text)
    println(ret)
  }

  def mockDocs() = {
    val classDoc = new OctoClassDoc("com.sankuai.octo.demo")
    (1 to 2).map {
      x =>
        val doc = new OctoMethodDoc(classDoc, s"api${x}")
        doc.setName(s"api${x}")
        doc
    }.toList.asJava
  }

  test("types") {
    val types = mockTypes()
    val text = JsonUtil.toJson(Map("com.sankuai.octo.demo" -> types).asJava)
    println(text)
    val ret = DocParser.parseTypes(text)
    println(ret)
  }

  def mockTypes() = {
    (1 to 2).map {
      x =>
        val doc = new OctoTypeDoc()
        doc.setType(s"User${x}")
        doc.setSimpleType(s"User${x}")
        doc
    }.toList.asJava
  }
}
