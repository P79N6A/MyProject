package com.sankuai.msgp.errorlog.consumer;

import com.dianping.zebra.util.StringUtils;
import com.meituan.mafka.client.MafkaClient;
import com.meituan.mafka.client.consumer.ConsumeStatus;
import com.meituan.mafka.client.consumer.ConsumerConstants;
import com.meituan.mafka.client.consumer.IConsumerProcessor;
import com.meituan.mafka.client.consumer.IMessageListener;
import com.meituan.mafka.client.message.MafkaMessage;
import com.meituan.mafka.client.message.MessagetContext;
import com.sankuai.msgp.common.utils.client.Messager;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.errorlog.service.LogParseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class XMDErrorLogConsumer {
    private static final Logger logger = LoggerFactory.getLogger(XMDErrorLogConsumer.class);

    private static final String MAFKA_BG_NAMESPACE = "octo";
    private static final String MAFKA_CONSUMER_APPKEY = "com.sankuai.inf.octo.errorlog";
    private String topicName = "";
    private String consumerGroupName = "";

    @Autowired
    private LogParseService logParseService;

    private IConsumerProcessor consumer = null;

    public void initMafkaClientAndConsumeErrorLog(String topic, String consumerGroup) {
        if (StringUtils.isBlank(topic) || StringUtils.isBlank(consumerGroup)) {
            logger.error("Current host is not the errorlog task node, ip={}, consumerGroup={}", CommonHelper.getLocalIp(), consumerGroup);
            return;
        }
        logger.info("Init mafka client, consumerGroup={}, topic={}", consumerGroup, topic);

        topicName = topic;
        consumerGroupName = consumerGroup;
        try {
            consumer = initMafkaConsumer(topic, consumerGroup);
            logger.info("Mafka client init success");
            consumer.recvMessageWithParallel(String.class, new IMessageListener() {
                @Override
                public ConsumeStatus recvMessage(MafkaMessage message, MessagetContext context) {
                    Object messageObj = message.getBody();
                    if (null != messageObj) {
                        String error = (String) message.getBody();
                        logParseService.handleMessage(error);
                    }
                    return ConsumeStatus.CONSUME_SUCCESS;
                }
            });
        } catch (Exception e) {
            logger.error("Mafka client init failed", e);
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]创建消费者实例异常, ip=" + CommonHelper.getLocalIp() + " topic=" + topic + ", consumer=" + consumerGroup);
        }
    }

    public void reinitMafkaConsumer(String topic, String consumerGroup) {
        if (consumer != null && topicName.equals(topic) && consumerGroupName.equals(consumerGroup)) {
            logger.info("Topic and consumerGroup no change, do not need reinit Mafka client.");
            // topic和消费组未变
            return;
        }
        destroyMafkaClient();
        initMafkaClientAndConsumeErrorLog(topic, consumerGroup);
    }

    public void destroyMafkaClient() {
        try {
            if (consumer == null) {
                return;
            }
            consumer.close();
            consumer = null;
            logger.warn("Mafka client destroyed");
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]销毁消费者实例, ip=" + CommonHelper.getLocalIp() + " topic=" + topicName + ", consumer=" + consumerGroupName);
        } catch (Exception e) {
            logger.error("Mafka client destroy failed", e);
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]销毁消费者实例异常! ip=" + CommonHelper.getLocalIp() + " topic=" + topicName + ", consumer=" + consumerGroupName);
        }
    }

    private IConsumerProcessor initMafkaConsumer(String topic, String consumerGroup) throws Exception {
        Properties properties = new Properties();
        // 设置业务所在BG的namespace
        properties.setProperty(ConsumerConstants.MafkaBGNamespace, MAFKA_BG_NAMESPACE);
        // 设置消费者appkey
        properties.setProperty(ConsumerConstants.MafkaClientAppkey, MAFKA_CONSUMER_APPKEY);
        // 设置消费组
        properties.setProperty(ConsumerConstants.SubscribeGroup, consumerGroup);
        // 创建topic对应的consumer对象（注意每次build调用会产生一个新的实例）
        IConsumerProcessor consumer = MafkaClient.buildConsumerFactory(properties, topic);
        return consumer;
    }
}