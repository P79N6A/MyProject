package com.sankuai.logparser.service;

import com.sankuai.logparser.bolt.MafkaProducerBolt;
import com.sankuai.logparser.util.MtConfig;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.utils.client.Messager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by emma on 2017/9/22.
 */
public class RouteCfService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteCfService.class);
    private MtConfigClient errorLogCfgClient = MtConfig.getErrorLogCfgClient();

    private final String ERROR_LOG_ROUTE_CFG = "error_log_appkey_route";
    private final String ERROR_LOG_CONSUMER_TOPIC_CFG = "error_log_consumer_topic";
    private final String DEFAULTS = "defaults";

    private Map<String, String> appkeyTopicRouteMap = new HashMap<>(); // <Appkey, Topic>
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Map<String, String> nodeTopicMap = new HashMap<>();
    private Set<String> topics = new HashSet<>();

    private MafkaProducerBolt mafkaBolt = null;


    public static RouteCfService getInstance() {
        return RouteCfServiceHolder.instance;
    }

    private static class RouteCfServiceHolder {
        private static final RouteCfService instance = new RouteCfService();
    }

    private RouteCfService() {
        initConfig();
    }

    public String getAppkeyRouteTopic(String appkey) {
        String topic;
        try {
            rwLock.readLock().lock();
            topic = appkeyTopicRouteMap.get(appkey);
            if (StringUtils.isBlank(topic)) {
                topic = appkeyTopicRouteMap.get(DEFAULTS);
                if (topic == null) {
                    LOGGER.error("appkey={} no topic map, and appkey route no {} cfg", appkey, DEFAULTS);
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }
        return topic;
    }

    public Set<String> getTopics() {
        return topics;
    }

    private void initConfig() {
        String consumerTopicCfgStr = errorLogCfgClient.getValue(ERROR_LOG_CONSUMER_TOPIC_CFG);
        parseConsumerTopicCfg(consumerTopicCfgStr);
        errorLogCfgClient.addListener(ERROR_LOG_CONSUMER_TOPIC_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed,new value {}", key, newValue);
                parseConsumerTopicCfg(newValue);
            }
        });

        String routeCfgStr = errorLogCfgClient.getValue(ERROR_LOG_ROUTE_CFG);
        parseRouteConfig(routeCfgStr);
        errorLogCfgClient.addListener(ERROR_LOG_ROUTE_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed,new value {}", key, newValue);
                parseRouteConfig(newValue);
            }
        });
        LOGGER.info("RouteCfg initialized.");
    }

    /**
     * 更新Topics和node到Topic的映射
     * 如果Topics有变化, 调用parseRouteConfig更新Appkey到Topic的映射
     * @param consumerTopicCfgStr
     */
    private synchronized void parseConsumerTopicCfg(String consumerTopicCfgStr) {
        if (StringUtils.isBlank(consumerTopicCfgStr)) {
            LOGGER.error("{} no config value", ERROR_LOG_CONSUMER_TOPIC_CFG);
            return;
        }

        String[] consumerTopicCfgs = consumerTopicCfgStr.replaceAll("\\s", "").split(";");
        Map<String, String> nodeTopic = new HashMap<>();
        Set<String> topicNames = new HashSet<>();
        for (String consumerTopicCfg : consumerTopicCfgs) {
            String[] hostNodeCfgs = consumerTopicCfg.split(":");
            if (hostNodeCfgs.length < 2) {
                LOGGER.error("{} config is not right", ERROR_LOG_CONSUMER_TOPIC_CFG);
            }
            String cfgItemStr = hostNodeCfgs[1];
            String[] cfgItems = cfgItemStr.split("\\|");
            if (cfgItems.length < 3) {
                LOGGER.error("{} config is not right", ERROR_LOG_CONSUMER_TOPIC_CFG);
            }
            String nodeName = cfgItems[0];
            String topicName = cfgItems[1];
            topicNames.add(topicName);
            nodeTopic.put(nodeName, topicName);
        }
        nodeTopicMap = nodeTopic;

        if (!topics.isEmpty()) {
            // 触发Producer重新初始化
            mafkaBolt.reinitMafkaProducer(topicNames);
            parseRouteConfig(errorLogCfgClient.getValue(ERROR_LOG_ROUTE_CFG));
        }
        topics = topicNames;
    }

    private synchronized void parseRouteConfig(String routeCfgStr) {
        if (StringUtils.isBlank(routeCfgStr)) {
            LOGGER.error("{} no config value", ERROR_LOG_ROUTE_CFG);
            return;
        }
        Map<String, String> appkeyTopicMap = new HashMap<>();
        String[] routes = routeCfgStr.replaceAll("\\s", "").split(";");
        for (String route : routes) {
            String[] routerConfig = route.split(":");
            if (routerConfig.length < 2) {
                LOGGER.error("{} config is not right", ERROR_LOG_ROUTE_CFG);
                continue;
            }
            String nodeName = routerConfig[0];
            String[] routeAppkeys = routerConfig[1].split(",");
            String topicName = nodeTopicMap.get(nodeName);

            for (String appkey : routeAppkeys) {
                if (DEFAULTS.equalsIgnoreCase(appkey)) {
                    appkey = DEFAULTS;
                }
                appkeyTopicMap.put(appkey, topicName);
            }
        }
        try {
            rwLock.writeLock().lock();
            appkeyTopicRouteMap = appkeyTopicMap;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void setMafkaBolt(MafkaProducerBolt mafkaBolt) {
        this.mafkaBolt = mafkaBolt;
    }
}
