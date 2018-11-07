package com.sankuai.octo.queue;

import com.meituan.mafka.client.MafkaClient;
import com.meituan.mafka.client.consumer.ConsumerConstants;
import com.meituan.mafka.client.producer.AsyncProducerResult;
import com.meituan.mafka.client.producer.FutureCallback;
import com.meituan.mafka.client.producer.IProducerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by yves on 17/4/23.
 *
 */
public class MetricProducer {

    private static Logger LOG= LoggerFactory.getLogger(MetricProducer.class);

    private IProducerProcessor producer;

    public MetricProducer(String topic){
        Properties properties = new Properties();
        // 设置业务所在BG的namespace
        properties.setProperty(ConsumerConstants.MafkaBGNamespace, "octo");
        // 设置生产者appkey
        properties.setProperty(ConsumerConstants.MafkaClientAppkey, "com.sankuai.inf.logCollector");
        try {
            // 创建topic对应的producer对象（注意每次build调用会产生一个新的实例）
            producer = MafkaClient.buildProduceFactory(properties, topic);
        }catch (Exception e){
            LOG.error("init mafka client failed. topic: " + topic, e);
        }
    }

    public void sendAsyncMessage(byte[] message) {
        try {
            // 异步发送消息
            LOG.debug("message: " + message.toString());
            producer.sendAsyncMessage(message, new FutureCallback() {
                @Override public void onSuccess(AsyncProducerResult result) {
                    LOG.debug("sendAsyncMessage successful.");
                }

                @Override public void onFailure(Throwable t) {
                    LOG.error("sendAsyncMessage failed.");
                }
            });
        } catch (Exception e) {
            LOG.error("mafka producer sendAsyncMessage failed", e);
        }
    }

}
