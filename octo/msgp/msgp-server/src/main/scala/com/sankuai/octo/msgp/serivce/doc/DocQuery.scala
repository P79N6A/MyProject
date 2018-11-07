package com.sankuai.octo.msgp.serivce.doc

import com.sankuai.msgp.common.utils.helper.JsonHelper
import org.slf4j.{Logger, LoggerFactory}

object DocQuery {
  val LOG: Logger = LoggerFactory.getLogger(DocQuery.getClass)

  def groupDocs(appkey: String) = {
    val docRowsOption = DocDao.getlatestDocs(appkey)
    val groupDocs = docRowsOption.map {
      docRows =>
        docRows.groupBy(_.group.getOrElse("Default")).toMap
    }
    JsonHelper.dataJson(groupDocs)
  }
}
