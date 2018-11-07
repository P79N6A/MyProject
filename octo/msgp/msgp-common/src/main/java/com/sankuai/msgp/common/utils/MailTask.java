package com.sankuai.msgp.common.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nero on 2018/5/29
 */
public class MailTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MailTask.class);

    // 默认的编码方式
    private static final String MAIL_DEFAULT_CHARSET = "UTF-8";
    // 默认的连接等待时间（ms）
    private static final int MAIL_DEFAULT_CONNECTION_TIMEOUT = 5000;
    // 默认的数据等待时间（ms）
    private static final int MAIL_DEFAULT_SOCKET_TIMEOUT = 30000;

    // host="smtp.meituan.com"
    private String host = "smtpin.meituan.com";
    // port="587"
    private int port = 25;
    // 发件人邮箱 sender 必须与 senderId 对应
    private String sender = "octo@meituan.com";
    // 发件人姓名
    private String senderName = "OCTO服务治理平台";
    // MIS 帐号，例如：guoning02
    private String senderId = "octo@meituan.com";
    // 收件人
    private Set<String> recipients;

    private String title;
    private String message;

    public MailTask(String title, String message, Set<String> recipients) {
        this.title = title;
        this.message = message;
        this.recipients = recipients;
    }


    public void run() {
        send();
    }

    private void send() {
        if (StringUtils.isBlank(title) || StringUtils.isBlank(message) || recipients.isEmpty()) {
            LOG.error("title: {},message: {},recipients: {},not complete mail skip send!", title, message, recipients);
            return;
        }
        HtmlEmail email = new HtmlEmail();
        email.setCharset(MAIL_DEFAULT_CHARSET);
        email.setHostName(host);
        email.setSmtpPort(port);
        email.setSocketConnectionTimeout(MAIL_DEFAULT_CONNECTION_TIMEOUT);
        email.setSocketTimeout(MAIL_DEFAULT_SOCKET_TIMEOUT);

        try {
            for (String recipient : recipients) {
                email.addTo(recipient);
            }
            email.setFrom(sender, senderName);
            email.setSubject(title);
//            email.setMsg(StringUtils.trimToEmpty(message));
            email.setHtmlMsg(StringUtils.trimToEmpty(message));
            email.send();
            LOG.info("###send complete recipient : {}",recipients);
        } catch (Exception e) {
            LOG.error(String.format("邮件发送失败，邮件主题：%s，发件人：%s，发件人姓名：%s，收件人：%s，部分邮件内容：%s，异常信息：%s, %s, %s", title, sender, senderName, recipients, message, e.getMessage(), e.getCause(), e.getCause().getCause()));
        }
    }

    public static void main(String[] args) {
        Set<String> recipients = new HashSet<>();
        recipients.add("zhangyun16@meituan.com");
        MailTask task = new MailTask("测试发送123","测试数据test 1234<h1>html</h1>",recipients);
        task.send();
    }

}
