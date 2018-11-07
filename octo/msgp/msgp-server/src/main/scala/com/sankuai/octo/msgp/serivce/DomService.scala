package com.sankuai.octo.msgp.serivce

import com.sankuai.msgp.common.utils.HttpUtil
import com.sankuai.octo.msgp.domain.report.NonstandardAppkey
import com.sankuai.octo.mworth.utils.CronScheduler
import org.apache.commons.lang3.StringUtils
import org.quartz._
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.Set

object DomService {
  private val LOG = LoggerFactory.getLogger(this.getClass)

  private val domHost = "http://dom.dp"

  case class OwnerBg(owners: String, bg: String, except_type: List[Int], owt: String)

  implicit val ownerBgReads = Json.reads[OwnerBg]
  implicit val ownerBgWrites = Json.writes[OwnerBg]

  val userNonstandardAppkey = TrieMap[String, Set[NonstandardAppkey]]()

  def init(): Unit = {
    val job = JobBuilder.newJob(classOf[DomJob]).build()
    val trigger: Trigger = TriggerBuilder.newTrigger().startNow()
      .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(4, 0)).build()
    CronScheduler.scheduleJob(job, trigger)
  }

  @DisallowConcurrentExecution
  class DomJob extends Job {

    @throws(classOf[JobExecutionException])
    override def execute(ctx: JobExecutionContext): Unit = {
      calculate()
    }

    def calculate(timeMillis: Long = System.currentTimeMillis()): Unit = {
      LOG.info(s"Dom begin refresh userNonstandardAppkey")
      refreshNonstandardAppkeys
    }
  }


  def clearData(): Unit = {
    userNonstandardAppkey.clear()
  }


  def refreshNonstandardAppkeys = {
    userNonstandardAppkey.clear()
    try {
      val request = s"$domHost/bj/appkey_stree_mail.json"
      val result = HttpUtil.getResult(request)
      val data = Json.parse(result).asOpt[Map[String, OwnerBg]]
      data.getOrElse(Map[String, OwnerBg]()).foreach(e => {
        val (appkey, ownerBg) = e
        val users = ownerBg.owners
        val user_arr = users.split(",")
        val bg = ownerBg.bg
        val abnormityCodes = ownerBg.except_type.toArray
        val abnormityDescption = abnormityCodes.mkString(", ")
        user_arr.foreach {
          user =>
            if (StringUtils.isNotBlank(user)) {
              val setAppkey = userNonstandardAppkey.getOrElse(user, Set[NonstandardAppkey]())
              val nonstandardAppkey = new NonstandardAppkey(bg, appkey, abnormityCodes, abnormityDescption)
              if (setAppkey.isEmpty) {
                userNonstandardAppkey.put(user, setAppkey)
              }
              setAppkey += nonstandardAppkey
            }
        }
      }
      )
      userNonstandardAppkey
    } catch {
      case e: Exception => LOG.error(s"get nonstandard appkey fail ", e)
        userNonstandardAppkey
    }
  }


  def getNonstandardAppkey(username: String) = {
    if (userNonstandardAppkey.isEmpty) {
      refreshNonstandardAppkeys
    }
    val list = userNonstandardAppkey.getOrElse(username, Set[NonstandardAppkey]())
    list
  }
}
