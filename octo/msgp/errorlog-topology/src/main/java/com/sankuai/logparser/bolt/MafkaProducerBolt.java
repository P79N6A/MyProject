package com.sankuai.logparser.bolt;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.meituan.mafka.client.MafkaClient;
import com.meituan.mafka.client.consumer.ConsumerConstants;
import com.meituan.mafka.client.producer.AsyncProducerResult;
import com.meituan.mafka.client.producer.FutureCallback;
import com.meituan.mafka.client.producer.IProducerProcessor;
import com.sankuai.logparser.service.RouteCfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 定制化写入Mafka
 */
public class MafkaProducerBolt extends BaseBasicBolt {
    private static final Logger logger = LoggerFactory.getLogger(MafkaProducerBolt.class);

    private static final String MAFKA_BG_NAMESPACE = "octo";
    private static final String MAFKA_PRODUSER_APPKEY = "com.sankuai.inf.octo.errorlog.topology";

    private static RouteCfService routeCfService = RouteCfService.getInstance();
    private Map<String, IProducerProcessor> mafkaProducers = new ConcurrentHashMap<>();
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();


    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        routeCfService.setMafkaBolt(this);
        Set<String> topics = routeCfService.getTopics();
        for (String topic : topics) {
            IProducerProcessor mafkaProducer = initMafkaProducer(topic);
            mafkaProducers.put(topic, mafkaProducer);
        }
        logger.info("Mafka topics={}", topics);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String appkey = tuple.getString(0);
        String logStr = tuple.getString(1);

        String routeTopic = routeCfService.getAppkeyRouteTopic(appkey);
        IProducerProcessor mafkaProducer;
        try {
            rwLock.readLock().lock();
            mafkaProducer = mafkaProducers.get(routeTopic);
        } finally {
            rwLock.readLock().unlock();
        }

        if (mafkaProducer == null) {
            logger.error("Get MafkaProducer fail, topic={}, appkey={}", routeTopic, appkey);
            return;
        }
        try {
            // 异步发送消息
            mafkaProducer.sendAsyncMessage(logStr, new FutureCallback() {
                public void onSuccess(AsyncProducerResult result) {
                }

                public void onFailure(Throwable t) {
                    logger.error("mafkaProducer send message failed", t);
                }
            });
        } catch (Exception e) {
            logger.error("Mafka producer send message error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    public void reinitMafkaProducer(Set<String> topicNames) {
        try {
            logger.info("Topics may change, reinitMaMafkaProducer, newTopic={}, oldTopic={}", topicNames, mafkaProducers.keySet());
            rwLock.writeLock().lock();
            for (String topic : topicNames) {
                IProducerProcessor mafkaProducer = mafkaProducers.get(topic);
                if (mafkaProducer == null) {
                    mafkaProducer = initMafkaProducer(topic);
                    mafkaProducers.put(topic, mafkaProducer);
                    logger.info("Add topic {}", topic);
                }
            }
            // 清理下掉Topic的Producer
            Set<String> topicSet = new HashSet<>(mafkaProducers.keySet());
            topicSet.removeAll(topicNames);
            for (String topic : topicSet) {
                IProducerProcessor mafkaProducer = mafkaProducers.remove(topic);
                mafkaProducer.close();
                logger.info("Remove topic {}", topic);
            }
        } catch (Exception e) {
            logger.error("Mafka producer close fail", e);
            throw new RuntimeException(e);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private IProducerProcessor initMafkaProducer(String topic) {
        Properties properties = new Properties();
        // 设置业务所在BG的namespace
        properties.setProperty(ConsumerConstants.MafkaBGNamespace, MAFKA_BG_NAMESPACE);
        // 设置生产者appkey
        properties.setProperty(ConsumerConstants.MafkaClientAppkey, MAFKA_PRODUSER_APPKEY);

        try {
            // 创建topic对应的producer对象（注意每次build调用会产生一个新的实例）
            IProducerProcessor mafkaProducer = MafkaClient.buildProduceFactory(properties, topic);
            return mafkaProducer;
        } catch (Exception e) {
            logger.error("Mafka producer init failed, topic={}", topic, e);
            throw new RuntimeException(e);
        }
    }
}
