package com.sankuai.octo.service

import com.sankuai.octo.service.messager.MODE.MODE
import com.sankuai.xm.api.push.PushUtil
import dispatch.Defaults._
import dispatch._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

object messager {
  val LOG: Logger = LoggerFactory.getLogger(messager.getClass)

  object MODE extends Enumeration {
    type MODE = Value
    val SMS = Value(0, "短信")
    val MAIL = Value(1, "邮件")
    val XM = Value(2, "大象")
  }

  case class Alarm(subject: String, content: String, ackUrl: String)

  /**
   * 发送报警信息
   * @param to 用户ID
   * @param alarm 信息内容
   * @param modes 通知方式
   */
  def sendAlarm(to: Seq[Int], alarm: Alarm, modes: Seq[MODE]) {
    val employees = to.map(self => orgapi.employee(self))
    val emailsAndXMs = employees.map(_.getEmail)
    val mobiles = employees.map(_.getMobile)
    modes.foreach {
      case MODE.SMS =>
        sms.send(mobiles, alarm.content)
      case MODE.MAIL =>
        mail.send(emailsAndXMs, alarm.subject, s"${alarm.content}\n<a href='${alarm.ackUrl}'>ACK</a>")
      case MODE.XM =>
        if (alarm.ackUrl != null && alarm.ackUrl != "") {
          xm.send(emailsAndXMs, s"${alarm.content}\n[ACK|${alarm.ackUrl}]")
        } else {
          xm.send(emailsAndXMs, s"${alarm.content}")
        }
    }
  }

  object sms {
    private val SMS_KEY = "inf"
    private val SMS_TOKEN = "74fe60310dc35efb359e52231262e770"
    private val SMS_HOST = "http://open-in.meituan.com"
    private val SMS_URI = "/sms/send"
    implicit val timeout = Duration.create(10, duration.SECONDS)

    def send(phones: Seq[String], message: String): Option[Int] = {
      try {
        val headers = api.authHeaders(SMS_URI, "POST", SMS_KEY, SMS_TOKEN)
        val params = Map("mobile" -> phones.mkString(","), "msg" -> message, "pri" -> "1")
        val request = url(SMS_HOST + SMS_URI) <:< headers << params
        val feature = Http(request.POST OK as.String)
        val content = Await.result(feature, timeout)
        Json.parse(content).\("ok").asOpt[Int]
      } catch {
        case e: Exception => LOG.error(s"send sms to $phones fail : msg = $message, ex = $e"); None
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
        case e: Exception => LOG.error(s"send mail to $emails fail : msg = $message, ex = $e"); None
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
        case MultiPart => {
          val attachment = new EmailAttachment()
          attachment.setPath(mail.attachment.get.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(mail.attachment.get.getName)
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
        }
      }

      commonsMail.setHostName(SMTP_HOST)
      commonsMail.setSmtpPort(SMTP_PORT)
      commonsMail.setAuthentication(SMTP_USER, SMTP_PASSWORD)

      mail.to.foreach(commonsMail.addTo(_))
      mail.cc.foreach(commonsMail.addCc(_))
      mail.bcc.foreach(commonsMail.addBcc(_))
      commonsMail.setFrom(mail.from._1, mail.from._2).setSubject(mail.subject).send()
    }
  }

  object xm {
    private val XM_KEY = "octo_notice"
    private val XM_SECRET = "0c8ecbc5d8b826124cb2e975e8309319"
    private val XM_SENDER = "octo_subscribe@meituan.com"
    private val XM_API = "http://xm-in.sankuai.com/api"

    PushUtil.init(XM_KEY, XM_SECRET, XM_SENDER, XM_API);

    def send(emails: Seq[String], message: String) {
      PushUtil.push(message, emails: _*)
    }
  }

  def main(args: Array[String]): Unit = {
    mail.send(Seq("zhangxi@meituan.com"), "test", "test")
  }
}
