package com.sankuai.octo.msgp.serivce.doc

import com.sankuai.msgp.common.config.DbConnection
import com.sankuai.octo.doclet.doc.{OctoMethodDoc, OctoTypeDoc}
import com.sankuai.octo.doclet.util.JsonUtil
import com.sankuai.msgp.common.config.db.msgp.Tables._
import com.sankuai.msgp.common.utils.helper.Md5Helper
import org.slf4j.{Logger, LoggerFactory}

import scala.slick.driver.MySQLDriver.simple._

object DocDao {
  val LOG: Logger = LoggerFactory.getLogger(DocParser.getClass)
  private val db = DbConnection.getPool()

  def getlatestDocs(appkey: String) = {
    val gsignOption = getLatestGSgin(appkey)
    gsignOption.map {
      gsign =>
        getDocs(appkey, gsign)
    }
  }

  def getDocs(appkey: String, gsign: String) = {
    db withSession {
      implicit session: Session =>
        AppMethodDoc.filter(x => x.appkey === appkey && x.gsign === gsign).run
    }
  }

  def getLatestGSgin(appkey: String) = {
    db withSession {
      implicit session: Session =>
        val a = AppMethodDoc.filter(x => x.appkey === appkey).sortBy(_.ctime.desc).take(1).map(_.gsign).run
        a.headOption
    }
  }

  def existMethodSign(appkey: String, gsign: String) = {
    db withSession {
      implicit session: Session =>
        AppMethodDoc.filter(x => x.appkey === appkey && x.gsign === gsign).exists.run
    }
  }

  def saveDocs(appkey: String, gsign: String, docs: List[OctoMethodDoc]): Map[String, Long] = {
    val ctime = System.currentTimeMillis()
    db withSession {
      implicit session: Session =>
        val ids = docs.map {
          doc =>
            if (doc.getAppkey != appkey) {
              LOG.info(s"ignore $appkey $gsign $doc")
              (doc.getApi, -1L)
            } else {
              val id = (AppMethodDoc returning AppMethodDoc.map(_.id)) += AppMethodDocRow(0, doc.getAppkey, Option(doc.getGroup),
                doc.getApi, Option(doc.getName), Option(doc.getDesc), Option(JsonUtil.toJson(doc.getParams)),
                Option(JsonUtil.toJson(doc.getResult)), Option(JsonUtil.toJson(doc.getExceptions)),
                Option(doc.getPermission), Option(doc.getStatus), Option(doc.getVersion), Option(doc.getLink),
                Option(doc.getAuthor), gsign, signDoc(doc), ctime)
              (doc.getApi, id)
            }
        }.toMap
        LOG.info(s"insert result $ids for $appkey $gsign ${docs.size}")
        ids
    }
  }

  def getSign(docs: List[OctoMethodDoc]) = {
    Md5Helper.getMD5(docs.map(signDoc).sorted.mkString(","))
  }

  def signDoc(doc: OctoMethodDoc) = {
    Md5Helper.getMD5(doc.toString)
  }

  def existTypeSign(appkey: String, gsign: String) = {
    db withSession {
      implicit session: Session =>
        AppTypeDoc.filter(x => x.appkey === appkey && x.gsign === gsign).exists.run
    }
  }

  def saveTypes(appkey: String, gsign: String, types: List[OctoTypeDoc]): Map[String, Long] = {
    val ctime = System.currentTimeMillis()
    db withSession {
      implicit session: Session =>
        val ids = types.map {
          doc =>
            val id = (AppTypeDoc returning AppTypeDoc.map(_.id)) += AppTypeDocRow(0, appkey, doc.getType,
              doc.getSimpleType, Option(JsonUtil.toJson(doc.getParamTypes)), Option(doc.getCommentText),
              Option(JsonUtil.toJson(doc.getFields)), gsign, signTypeDoc(doc), ctime)
            (doc.getType, id)
        }.toMap
        LOG.info(s"insert result $ids for $appkey $gsign ${types.size}")
        ids
    }
  }

  def getTypesSign(docs: List[OctoTypeDoc]) = {
    Md5Helper.getMD5(docs.map(signTypeDoc).sorted.mkString(","))
  }

  def signTypeDoc(typeDoc: OctoTypeDoc) = {
    Md5Helper.getMD5(typeDoc.toString)
  }

  def getApiName(appkey:String,api:String) ={
    db withSession {
      implicit session: Session =>
        val records=AppMethodDoc.filter(x => x.appkey === appkey && x.api === api).sortBy(_.ctime.desc).run
        if(records.nonEmpty){
          records.head.name
        }else{
          None
        }
    }
  }
}
