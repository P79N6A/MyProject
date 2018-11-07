package com.sankuai.msgp.common.config;


/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-27
 */
public class MtConfigBean {
    public static final String nodeName = "mtsg";

    // appkey
    public static String appkey = "mtsg";

    // rabbit mq
    public static String rabbitmqUser = MtsgConfig.get("rabbitmq.user", "cosguest");

    public static String rabbitmqPassword = MtsgConfig.get("rabbitmq.passport", "cosguest");

    public static String rabbitmqCluster = MtsgConfig.get("rabbitmq.cluster", "cos22,cos23");

    public static String rabbitmqVhost = MtsgConfig.get("rabbitmq.vhost", "cos");

    public static String rabbitmqTransacted = MtsgConfig.get("rabbitmq.transacted", "true");

    public static String rabbitmqLogCluster = MtsgConfig.get("rabbitmq.log.cluster", "10.64.250.111");

    public static String rabbitmqLogVhost = MtsgConfig.get("rabbitmq.log.vhost", "cos");

    public static String rabbitmqMtraceCluster = MtsgConfig.get("rabbitmq.mtrace.cluster", "10.64.16.252,10.64.16.251");

    public static String rabbitmqMtraceVhost = MtsgConfig.get("rabbitmq.mtrace.vhost", "cos");

    public static String rabbitmqMtraceUser = MtsgConfig.get("rabbitmq.mtrace.user", "mtrace");

    public static String rabbitmqMtracePassword = MtsgConfig.get("rabbitmq.mtrace.password", "mtrace");

    public static String mtsgSecret = MtsgConfig.get("mtsg_secret", "73534ff81eaee5bf4f2d9b6edbe0fa66");

    public static String mtmqHost = MtsgConfig.get("mtmq_host", "http://api.mtmq.sankuai.com");

}