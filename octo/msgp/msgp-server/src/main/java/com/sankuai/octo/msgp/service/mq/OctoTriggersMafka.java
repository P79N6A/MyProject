package com.sankuai.octo.msgp.service.mq;

import com.meituan.mafka.client.MafkaClient;
import com.meituan.mafka.client.consumer.ConsumerConstants;
import com.meituan.mafka.client.producer.AsyncProducerResult;
import com.meituan.mafka.client.producer.FutureCallback;
import com.meituan.mafka.client.producer.IProducerProcessor;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * octo的trigger订阅mq producer
 * Created by nero on 2018/6/25
 */
public class OctoTriggersMafka {

    private static OctoTriggersMafka instance = new OctoTriggersMafka();

    private static Logger LOG = LoggerFactory.getLogger(OctoTriggersMafka.class);

    private static IProducerProcessor producer;

    private static boolean isOnline = !CommonHelper.isOffline();

    private OctoTriggersMafka() {
    }

    static {
        if (isOnline) {
            try {
                Properties properties = new Properties();
                properties.setProperty(ConsumerConstants.MafkaBGNamespace, "common");
                properties.setProperty(ConsumerConstants.MafkaClientAppkey, "com.sankuai.inf.msgp");
                producer = MafkaClient.buildProduceFactory(properties, "octo_triggers_event_online");
                LOG.info("init mafka producer success");
            } catch (Exception e) {
                LOG.error("init mafka producer error, please check the code", e);
            }
        }
    }

    public static OctoTriggersMafka getInstance() {
        return instance;
    }

    public void sendAsyncMessage(String message) {
        if (isOnline) {
            try {
                producer.sendAsyncMessage(message, new FutureCallback() {
                    @Override
                    public void onSuccess(AsyncProducerResult result) {
                        LOG.info("send trigger async message success!");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.error("send trigger async message error!", t);
                    }
                });
            } catch (Exception e) {
                LOG.error("send trigger async message error!", e);
            }
        }
    }

    public void sendSyncMessage(String message) {
        if (isOnline) {
            try {
                producer.sendMessage(message);
            } catch (Exception e) {
                LOG.error("send trigger sync message error!", e);
            }
        }
    }
}
