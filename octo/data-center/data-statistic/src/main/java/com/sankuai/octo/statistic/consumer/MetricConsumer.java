package com.sankuai.octo.statistic.consumer;

import com.meituan.mafka.client.MafkaClient;
import com.meituan.mafka.client.consumer.ConsumeStatus;
import com.meituan.mafka.client.consumer.ConsumerConstants;
import com.meituan.mafka.client.consumer.IConsumerProcessor;
import com.meituan.mafka.client.consumer.IMessageListener;
import com.meituan.mafka.client.message.MafkaMessage;
import com.meituan.mafka.client.message.MessagetContext;
import com.sankuai.octo.statistic.MafkaConfig;
import com.sankuai.octo.statistic.StatConstants;
import com.sankuai.octo.statistic.impl.LogStatisticServiceImpl;
import com.sankuai.octo.statistic.model.Metric;
import com.sankuai.octo.statistic.model.MetricList;
import com.sankuai.octo.statistic.model.TimeCount;
import com.sankuai.octo.statistic.service.LogStatisticService;
import com.sankuai.octo.statistic.util.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MetricConsumer.class);
    @Autowired
    private LogStatisticService logStatisticService;

    private TimeCount messageCount = new TimeCount();
    private AtomicInteger messageCountInfoCount = new AtomicInteger();

    @Bean
    public IConsumerProcessor metricDataConsumer() throws Exception {
        Boolean isTaskHost = MafkaConfig.isTaskHost();
        logger.info("localhost is metric consumer task host : " + isTaskHost);
        if (!isTaskHost) {
            return null;
        }
        final LogStatisticServiceImpl statisticServiceImpl = (LogStatisticServiceImpl) logStatisticService;

        // 创建topic对应的consumer对象（注意每次build调用会产生一个新的实例）
        IConsumerProcessor consumer = getIConsumerProcessor();
        // 绑定了partition list, 因此此消费组只会消费list内的消息
        consumer.recvMessageWithParallel(byte[].class, new MetricListListener(statisticServiceImpl));
        return consumer;
    }

    private IConsumerProcessor getIConsumerProcessor() throws Exception {
        Properties properties = new Properties();
        // 设置业务所在BG的namespace
        properties.setProperty(ConsumerConstants.MafkaBGNamespace, "octo");
        // 设置消费者appkey
        properties.setProperty(ConsumerConstants.MafkaClientAppkey, "com.sankuai.inf.data.statistic");
        // 设置消费组
        String groupName = MafkaConfig.getGroupName();
        logger.info("consumer is " + groupName);
        String topicName = MafkaConfig.getTopicName();
        logger.info("topicName is " + topicName);

        properties.setProperty(ConsumerConstants.SubscribeGroup, groupName);
        properties.setProperty(ConsumerConstants.MAX_CONSUME_THREAD_COUNT, "16");
        IConsumerProcessor consumer = MafkaClient.buildConsumerFactory(properties, topicName);
        logger.info("Mafka client init success");
        return consumer;
    }

    class MetricListListener implements IMessageListener {
        private LogStatisticServiceImpl statisticServiceImpl;

        public MetricListListener(LogStatisticServiceImpl statisticServiceImpl) {
            this.statisticServiceImpl = statisticServiceImpl;
        }

        @Override
        public ConsumeStatus recvMessage(MafkaMessage message, MessagetContext messagetContext) {
            try {
                Object obj = message.getBody();
                if (null != obj) {
                    byte[] body = (byte[]) obj;
                    MetricList metricList = HessianSerializer.deserialize(body, MetricList.class);
                    statisticServiceImpl.sendMetricList(metricList);

                    long currentCount = messageCount.addAndGet(metricList.getData().size());
                    if (currentCount % 1000 == 0) {
                        logger.info("Mafka message count: " + currentCount);
                    }
                    limitConsumer(currentCount);
                }
            } catch (Exception e) {
                logger.error("Mafka client recvMessage failed", e);
                return ConsumeStatus.CONSUME_FAILURE;
            }
            return ConsumeStatus.CONSUME_SUCCESS;
        }
    }

    private void limitConsumer(long currentCount) throws InterruptedException {
        if (currentCount > StatConstants.metricLimit()) {
            //发送超限, sleep一段时间
            messageCountInfoCount.incrementAndGet();
            if (messageCountInfoCount.get() > 10) {
                logger.info("limitConsumer,Mafka message count: " + currentCount);
                messageCountInfoCount.set(0);
            }
            Thread.sleep(StatConstants.mafkaMessageSleep());
        }
    }
}
