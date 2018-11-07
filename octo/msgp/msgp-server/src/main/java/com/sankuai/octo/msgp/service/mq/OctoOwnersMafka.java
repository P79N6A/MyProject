package com.sankuai.octo.msgp.service.mq;

import com.meituan.mafka.client.MafkaClient;
import com.meituan.mafka.client.consumer.ConsumeStatus;
import com.meituan.mafka.client.consumer.ConsumerConstants;
import com.meituan.mafka.client.consumer.IConsumerProcessor;
import com.meituan.mafka.client.consumer.IMessageListener;
import com.meituan.mafka.client.message.MafkaMessage;
import com.meituan.mafka.client.message.MessagetContext;
import com.meituan.mafka.client.producer.AsyncProducerResult;
import com.meituan.mafka.client.producer.FutureCallback;
import com.meituan.mafka.client.producer.IProducerProcessor;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by nero on 2018/6/25
 */
public class OctoOwnersMafka {

    private static OctoOwnersMafka instance = new OctoOwnersMafka();

    private static Logger LOG = LoggerFactory.getLogger(OctoOwnersMafka.class);

    private static IProducerProcessor producer;

    private static final String OWNERS_MQ_SWITCH = "owner.send.mq.switch";

    private static boolean isOnline = !CommonHelper.isOffline();

    private static boolean isSendMQ = true;

    private OctoOwnersMafka() {
    }

    static {
        if (isOnline) {
            try {
                Properties properties = new Properties();
                properties.setProperty(ConsumerConstants.MafkaBGNamespace, "common");
                properties.setProperty(ConsumerConstants.MafkaClientAppkey, "com.sankuai.inf.msgp");
                producer = MafkaClient.buildProduceFactory(properties, "octo_owners_change_online");
                LOG.info("init mafka producer success");
            } catch (Exception e) {
                LOG.error("init mafka producer error, please check the code", e);
            }
        }

        MsgpConfig.addListener(OWNERS_MQ_SWITCH, new IConfigChangeListener() {
            @Override
            public void changed(String s, String oldValue, String newValue) {
                LOG.info("config[{}] changed from {} to {}", OWNERS_MQ_SWITCH, oldValue, newValue);
                if (StringUtil.isNotBlank(newValue)) {
                    isSendMQ = Boolean.parseBoolean(newValue);
                }
            }
        });
    }

    public static OctoOwnersMafka getInstance() {
        return instance;
    }

    public void sendAsyncMessage(String message) {
        if (isOnline && isSendMQ) {
            try {
                producer.sendAsyncMessage(message, new FutureCallback() {
                    @Override
                    public void onSuccess(AsyncProducerResult result) {
                        LOG.info("send async message success!");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.error("send async message error!", t);
                    }
                });
            } catch (Exception e) {
                LOG.error("send async message error!", e);
            }
        }
    }

    public void sendSyncMessage(String message) {
        if (isOnline && isSendMQ) {
            try {
                producer.sendMessage(message);
            } catch (Exception e) {
                LOG.error("send sync message error!", e);
            }
        }
    }
}
