package com.sankuai.msgp.common.utils.client

import com.sankuai.meituan.auth.vo.User
import com.sankuai.meituan.borp.vo.ActionType
import com.sankuai.meituan.notify.thrift.model.SendEmailRequest
import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.EntityType
import com.sankuai.msgp.common.service.appkey.AppkeyDescService
import com.sankuai.msgp.common.service.org.OrgSerivce
import com.sankuai.msgp.common.utils.helper.JsonHelper
import com.sankuai.msgp.common.utils.{ExecutionContextFactory, StringUtil}
import com.sankuai.xm.api.push.PushUtil
import dispatch.{as, url, _}
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}


object Messager {
  val LOG: Logger = LoggerFactory.getLogger(Messager.getClass)

  private implicit val ec = ExecutionContextFactory.build(Runtime.getRuntime.availableProcessors)
  private val errorLogAdminList = List("yangrui08", "zhanghui24")

  object MODE extends Enumeration {
    val SMS = Value(0, "短信")
    val MAIL = Value(1, "邮件")
    val XM = Value(2, "大象")
  }

  case class Alarm(subject: String, content: String, ackUrl: String, detailUrl: String)

  object Alarm {
    def apply(subject: String, content: String): Alarm = {
      new Alarm(subject, content, null, null)
    }

    def apply(subject: String, content: String, ackUrl: String): Alarm = {
      new Alarm(subject, content, ackUrl, null)
    }
  }

  def sendSingleMessage(login: String, alarm: Alarm, modes: Seq[MODE.Value]): Unit = {
    if (modes.nonEmpty) {
      val employeeOpt = OrgSerivce.employee(login)
      if (employeeOpt.isDefined) {
        val employee = employeeOpt.get
        val emailsAndXM = employee.getEmail
        val mobile = employee.getMobile
        modes.foreach {
          case MODE.SMS =>
            sms.send(Seq(mobile), s"${alarm.content}")
          case MODE.MAIL =>
            if (alarm.ackUrl != null && alarm.ackUrl != "") {
              mail.send(Seq(emailsAndXM), alarm.subject, s"${alarm.content}\n<a href='${alarm.ackUrl}'>ACK</a>\n<a href='${alarm.detailUrl}'>详情</a>")
            } else {
              mail.send(Seq(emailsAndXM), alarm.subject, s"${alarm.content}\n<a href='${alarm.detailUrl}'>详情</a>")
            }
          case MODE.XM =>
            if (alarm.ackUrl != null && alarm.ackUrl != "") {
              xm.send(Seq(emailsAndXM), s"${alarm.content}\n[ACK|${alarm.ackUrl}] [详情|${alarm.detailUrl}]")
            } else {
              xm.send(Seq(emailsAndXM), s"${alarm.content}")
            }
        }
      }
    }
  }

  /**
    * 发送报警信息
    *
    * @param to    用户ID
    * @param alarm 信息内容
    * @param modes 通知方式
    */
  def sendAlarm(to: Seq[Int], alarm: Alarm, modes: Seq[MODE.Value]) {
    if (to.nonEmpty && modes.nonEmpty) {
      val employees = to.map(self => OrgSerivce.employee(self)).filter(_ != null)
      //      val employees = OrgSerivce.filterLeftEmployeeById(to)
      if (employees.nonEmpty) {
        val emailsAndXMs = employees.map(_.getEmail)
        val mobiles = employees.map(_.getMobile)
        val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
        modes.foreach {
          case MODE.SMS =>
            sms.send(mobiles, s"$now ${alarm.content}")
          case MODE.MAIL =>
            if (alarm.ackUrl != null && alarm.ackUrl != "") {
              mail.send(emailsAndXMs, alarm.subject, s"$now\n${alarm.content}\n<a href='${alarm.ackUrl}'>ACK</a>\n<a href='${alarm.detailUrl}'>详情</a>")
            } else {
              mail.send(emailsAndXMs, alarm.subject, s"$now\n${alarm.content}\n<a href='${alarm.detailUrl}'>详情</a>")
            }
          case MODE.XM =>
            if (alarm.ackUrl != null && alarm.ackUrl != "") {
              xm.send(emailsAndXMs, s"$now\n${alarm.content}\n[ACK|${alarm.ackUrl}] [详情|${alarm.detailUrl}]")
            } else {
              xm.send(emailsAndXMs, s"$now\n${alarm.content}")
            }
        }
      } else {
        LOG.info(s"${to}邮件目标为空")
      }
    }
  }

  /**
    * 大象发送建议直接走发送用户名
    *
    * @param to
    * @param alarm
    */
  def sendXMAlarm(to: Seq[String], alarm: Alarm): Unit = {
    if (to.nonEmpty) {
      // 虚拟账号会抛异常
      //      val legalTo = OrgSerivce.filterLeftEmployeeByName(to)
      LOG.info(s"send msg to $to,and alram is $alarm")
      val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
      if (alarm.ackUrl != null && alarm.ackUrl != "") {
        xm.send(to, s"$now\n${alarm.content}\n[ACK|${alarm.ackUrl}] [详情|${alarm.detailUrl}]")
      } else {
        xm.send(to, s"$now\n${alarm.content}")
      }
    }
  }

  def sendXMAlarmToErrorLogAdmin(message: String): Unit = {
    Messager.xm.send(errorLogAdminList, message)
  }

  def sendXMAlarmByAppkey(appkey: String, message: String): Unit = {
    val users = AppkeyDescService.getOwners(appkey)
    val userLoginList = users.map(_.login)
    val now = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
    //    val legalList = OrgSerivce.filterLeftEmployeeByName(userLoginList)
    xm.send(userLoginList, s"$now\n${message}")
  }

  def sendAlarmByAppkey(appkey: String, subject: String, message: String): Unit = {
    val users = AppkeyDescService.getOwners(appkey)
    val userIds = users.map(_.id)
    val modes = List(Messager.MODE.XM, Messager.MODE.SMS, Messager.MODE.MAIL)
    val alarm = new Messager.Alarm(subject, message, null, null)
    Messager.sendAlarm(userIds, alarm, modes)
  }

  /**
    * 发送报警信息
    *
    * @param to    用户login
    * @param alarm 信息内容
    * @param modes 通知方式
    */
  def sendAlarmByLogin(to: Seq[String], alarm: Alarm, modes: Seq[MODE.Value], time: Option[String]) {
    if (to.nonEmpty && modes.nonEmpty) {
      val employees = to.flatMap(self => OrgSerivce.employee(self)).filter(_.getStatus.equals(0))
      val emailsAndXMs = employees.map(_.getEmail)
      val mobiles = employees.map(_.getMobile)
      val now = if (time.nonEmpty) time.get else DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
      modes.foreach {
        case MODE.SMS =>
          sms.send(mobiles, s"$now ${alarm.content}")
        case MODE.MAIL =>
          mail.send(emailsAndXMs, alarm.subject, s"$now\n${alarm.content}\n<a href='${alarm.ackUrl}'>ACK</a>\n<a href='${alarm.detailUrl}'>详情</a>")
        case MODE.XM =>
          if (alarm.ackUrl != null && alarm.ackUrl != "") {
            xm.send(emailsAndXMs, s"$now\n${alarm.content}\n[ACK|${alarm.ackUrl}] [详情|${alarm.detailUrl}]")
          } else {
            xm.send(emailsAndXMs, s"$now\n${alarm.content}")
          }
      }
    }
  }

  object sms {
    private val SMS_KEY = "com.sankuai.inf.msgp"
    private val SMS_TOKEN = "AE8ECE065466EA418B1EB21422B4C2DA"
    private val SMS_HOST = MsgpConfig.get("sms.url", "http://bjsms.dp")
    private val SMS_URI = "/send/industry"
    implicit val timeout = Duration.create(10L, duration.SECONDS)

    def send(phones: Seq[String], message: String, typeNo: String = "6656") = {
      try {

        phones.foreach {
          phone =>
            val headers = JsonHelper.authHeaders(SMS_URI, "POST", SMS_KEY, SMS_TOKEN)
            val params = Map(
              "type" -> typeNo,
              "mobiles" -> List(phone),
              "pairs" -> Map("message" -> message)
            )
            val json_param = JsonHelper.jsonStr(params)
            val request = url(SMS_HOST + SMS_URI) <:< headers << json_param
            val feature = Http(request.POST OK as.String)
            val content = Await.result(feature, timeout)
            val code = (Json.parse(content) \ "code").asOpt[String]
            if (!"200".equals(code.getOrElse("0"))) {
              LOG.error(s"短信发送失败,params:$params,content:$content")
            }
        }
      } catch {
        case e: Exception => LOG.error(s"send sms to $phones fail : msg = $message,", e); None
      }
    }
  }

  object mail {
    def send(emails: Seq[String], subject: String, message: String) {
      LOG.info(s"send mail $emails $subject $message")
      try {
        mail.send(Mail(from = ("octo@meituan.com", "Octo服务治理平台"), to = emails,
          subject = subject, message = message, richMessage = Some(message)))
      } catch {
        case e: Exception => LOG.error(s"send mail to $emails fail : msg = $message, ex = $e");
      }
    }

    def notifySend(emails: Seq[String], subject: String, message: String) {
      LOG.info(s"send mail $emails $subject")
      try {
        val request = new SendEmailRequest()
        request.setApp("msgp")
          .setFrom("octo@meituan.com")
          .setFromName("Octo服务治理平台")
          .setTo(emails.mkString(","))
          .setTitle(subject)
          .setContent(message)
        try {
          val result = NotifyClient.getInstance.sendEmail(request)
          saveBorpLog(emails, message, "email")
          LOG.info(s"result $result")
        } catch {
          case e: Exception => e.printStackTrace();
        }
      } catch {
        case e: Exception => LOG.error(s"send mail to $emails fail : msg = $message, ex = $e");
      }
    }

    private val SMTP_HOST = "smtpin.meituan.com"
    private val SMTP_PORT = 25
    private val SMTP_USER = "app.inf.octo"
    private val SMTP_PASSWORD = "18xb9W8KC2#62"

    implicit def liftToOption[T](t: T): Option[T] = Some(t)

    sealed abstract class MailType

    case object Plain extends MailType

    case object Rich extends MailType

    case object MultiPart extends MailType

    case class Mail(from: (String, String),
                    to: Seq[String],
                    cc: Seq[String] = Seq.empty,
                    bcc: Seq[String] = Seq.empty,
                    subject: String,
                    message: String,
                    richMessage: Option[String] = None,
                    attachment: Option[(java.io.File)] = None)

    def send(mail: Mail) {
      import org.apache.commons.mail._

      val format = {
        if (mail.attachment.isDefined) MultiPart
        else if (mail.richMessage.isDefined) Rich
        else Plain
      }

      val commonsMail: Email = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
        case MultiPart =>
          val attachment = new EmailAttachment()
          attachment.setPath(mail.attachment.get.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(mail.attachment.get.getName)
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
      }

      commonsMail.setHostName(SMTP_HOST)
      commonsMail.setSmtpPort(SMTP_PORT)
      //      commonsMail.setAuthentication(SMTP_USER, SMTP_PASSWORD)

      mail.to.foreach(commonsMail.addTo)
      mail.cc.foreach(commonsMail.addCc)
      mail.bcc.foreach(commonsMail.addBcc)
      commonsMail.setFrom(mail.from._1, mail.from._2).setSubject(mail.subject).send()
    }
  }

  object xm {
    private val XM_KEY = "octo_notice"
    private val XM_SECRET = "0c8ecbc5d8b826124cb2e975e8309319"
    private val XM_SENDER = "octo_subscribe@meituan.com"
    private val XM_API = "http://xm-in.sankuai.com/api"

    PushUtil.init(XM_KEY, XM_SECRET, XM_SENDER, XM_API)

    def send(emails: Seq[String], message: String) {
      try {
        if (null != emails && emails.nonEmpty) {
          sliceMessage(message).foreach {
            m =>
              if (StringUtil.isNotBlank(m)) {
                PushUtil.push(message, emails: _*)
              }
          }
          saveBorpLog(emails, message, "xm")
        } else {
          LOG.error(s"xm send message failed,邮件列表为空。message:$message")
        }
      } catch {
        case e: Exception => LOG.error(s"xm send message failed emails: $emails message:$message", e)
      }
    }
  }

  def saveBorpLog(emails: Seq[String], message: String, model: String): Unit = {
    val user = new User()
    user.setId(-1)
    user.setLogin("msgp")
    user.setName("msgp")
    BorpClient.saveOpt(user, actionType = ActionType.INSERT.getIndex, entityId = "com.sankuai.inf.msgp", entityType = EntityType.MESSAGE, fieldName = model, newValue = message, oldValue = emails.mkString(","))
  }

  //切割大文本，大象消息发送
  def sliceMessage(message: String) = {
    val size = message.getBytes().length / 8192
    val mod = message.getBytes().length % 8192
    val slice = if (size > 0 && mod > 0) {
      size + 1
    } else if (size > 0 && mod == 0) {
      size
    } else {
      0
    }
    if (slice > 0) {
      val split_count = slice
      val split_step = message.length / split_count
      val list = (0 to split_count).map {
        x =>
          val str = subString(message, split_step * x, split_step * (x + 1))
          str
      }.toList
      list
    } else {
      List(message)
    }
  }

  def subString(message: String, f: Int, t: Int): String = {
    if (f < message.length) {
      if (t > message.length) {
        message.substring(f, message.length)
      } else {
        message.substring(f, t)
      }
    } else {
      ""
    }
  }

  def main(args: Array[String]): Unit = {
    val str = "dd"
    val length = str.length
    val list = sliceMessage(str);
    list.foreach {
      x =>
        println(x.length)
        if (StringUtil.isNotBlank(x)) {
          xm.send(Seq("zhangyun16@meituan.com"), x)
        }
    }
    list
    //    xm.send(Seq("zhangyun16@meituan.com"), str)
    //    mail.send(Seq("yangrui08@meituan.com"), "test", "test")
    //    messager.sendRegistry("com.sankuai.inf.testRegistry111")
    //    sms.send(Seq("13426429178"), "你好，message4")
  }
}
