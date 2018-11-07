package com.sankuai.octo.msgp.serivce.doc

import com.alibaba.fastjson.{JSON, TypeReference}
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.octo.doclet.doc.{OctoMethodDoc, OctoTypeDoc}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._


object DocParser {
  val LOG: Logger = LoggerFactory.getLogger(DocParser.getClass)

  def parseDocs(text: String) = {
    val docsMap = JSON.parseObject(text, new TypeReference[java.util.Map[java.lang.String, java.util.List[OctoMethodDoc]]]() {})
    val result = docsMap.asScala.map {
      case (appkey, docs) =>
        (appkey, handleDocs(appkey, docs.asScala.toList))
    }.toMap
    JsonHelper.dataJson(result)
  }

  def handleDocs(appkey: String, docs: List[OctoMethodDoc]): Map[String, Long] = {
    if (docs == null || docs.isEmpty) {
      LOG.info(s"handle $appkey docs is empty")
      Map()
    } else {
      val gsign = DocDao.getSign(docs)
      if (DocDao.existMethodSign(appkey, gsign)) {
        LOG.info(s"handle $appkey docs $gsign already exist, docs = $docs")
        Map()
      } else {
        DocDao.saveDocs(appkey, gsign, docs)
      }
    }
  }

  def parseTypes(text: String) = {
    val typesMap = JSON.parseObject(text, new TypeReference[java.util.Map[java.lang.String, java.util.List[OctoTypeDoc]]]() {})
    val result = typesMap.asScala.map {
      case (appkey, types) => {
        (appkey, handleTypes(appkey, types.asScala.toList))
      }
    }
    JsonHelper.dataJson(result)
  }

  def handleTypes(appkey: String, types: List[OctoTypeDoc]): Map[String, Long] = {
    if (types == null || types.isEmpty) {
      LOG.info(s"handle $appkey types is empty")
      Map()
    } else {
      val gsign = DocDao.getTypesSign(types)
      if (DocDao.existTypeSign(appkey, gsign)) {
        LOG.info(s"handle $appkey types $gsign already exist, types = $types")
        Map()
      } else {
        DocDao.saveTypes(appkey, gsign, types)
      }
    }
  }
}
