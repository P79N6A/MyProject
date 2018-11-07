package com.sankuai.msgp.errorlog.service;

import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.utils.CloneUtil;
import com.sankuai.msgp.common.utils.client.Messager;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.errorlog.consumer.XMDErrorLogConsumer;
import com.sankuai.msgp.errorlog.task.ErrorLogTaskHost;
import com.sankuai.msgp.errorlog.util.IPUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by emma on 2017/9/21.
 */
@Service
public class ErrorLogRouteCfgService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogRouteCfgService.class);

    private final String ERROR_LOG_ROUTE_CFG = "error_log_appkey_route";
    private final String ERROR_LOG_CONSUMER_TOPIC_CFG = "error_log_consumer_topic";
    private final String DEFAULTS = "defaults";

    @Autowired
    private MtConfigClient errorLogConfigClient;
    @Autowired
    private LogAlarmService logAlarmService;
    @Autowired
    private ErrorLogRouteAdjustService routeAdjustService;
    @Autowired
    private ErrorLogTaskHost taskHost;
    @Autowired
    private XMDErrorLogConsumer logConsumer;

    private String localHostname = "";
    private String nodeName = "";
    private String consumerGroupName = "";
    private String topicName = "";

    private TreeMap<String, Set<String>> nodeAppkeyRouteMap = new TreeMap<>();
    private Set<String> allSpecifyAppkey = new HashSet();

    enum HostInitCfgItem {
        NodeName, ConsumerGroup, Topic
    }

    @PostConstruct
    private void init() {
        localHostname = IPUtil.getHostnameByIP(CommonHelper.getLocalIp());

        String consumerTopicCfgStr = errorLogConfigClient.getValue(ERROR_LOG_CONSUMER_TOPIC_CFG);
        parseConsumerTopicCfg(consumerTopicCfgStr);
        errorLogConfigClient.addListener(ERROR_LOG_CONSUMER_TOPIC_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed,new value {}", key, newValue);
                if (parseConsumerTopicCfg(newValue)) {
                    logConsumer.reinitMafkaConsumer(topicName, consumerGroupName);
                    checkNodeChange(newValue);
                }
            }
        });

        String routeCfgStr = errorLogConfigClient.getValue(ERROR_LOG_ROUTE_CFG);
        parseRouteConfig(routeCfgStr);
        errorLogConfigClient.addListener(ERROR_LOG_ROUTE_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed,new value {}", key, newValue);
                parseRouteConfig(newValue);
                logAlarmService.updateAlarmTasks();
            }
        });
        logAlarmService.startAlarmTasks(this);
        logConsumer.initMafkaClientAndConsumeErrorLog(topicName, consumerGroupName);
        LOGGER.info("ErrorLogRouteCfgService finish init.");
    }

    private void checkNodeChange(String newValue) {
        Map<String, Map<HostInitCfgItem, String>> newHostInitCfgMap = doParseConsumerTopicCfg(newValue);
        String taskHostname = taskHost.getTaskHost();
        if (!newHostInitCfgMap.containsKey(taskHostname)) {
            Messager.sendXMAlarmToErrorLogAdmin("【重要】[异常日志]" + ERROR_LOG_CONSUMER_TOPIC_CFG + "配置变更删除了任务主机" + taskHostname +
                    "请变更任务主机配置后，重新修改" + ERROR_LOG_CONSUMER_TOPIC_CFG + "触发变更进行动态路由");
            return;
        }
        if (!localHostname.equals(taskHostname)) {
            // 非任务主机
            return;
        }
        Map<String, Map<HostInitCfgItem, String>> newNodeCfgMap = new HashMap<>();
        for (Map.Entry<String, Map<HostInitCfgItem, String>> entry : newHostInitCfgMap.entrySet()) {
            Map<HostInitCfgItem, String> newCfgMap = entry.getValue();
            newNodeCfgMap.put(newCfgMap.get(HostInitCfgItem.NodeName), newCfgMap);
        }
        if (nodeAppkeyRouteMap.isEmpty()) {
            String routeCfgStr = errorLogConfigClient.getValue(ERROR_LOG_ROUTE_CFG);
            parseRouteConfig(routeCfgStr);
        }
        List<String> increaseNodeList = new ArrayList<>();
        for (String nodeName : newNodeCfgMap.keySet()) {
            if (nodeAppkeyRouteMap.get(nodeName) == null) {
                increaseNodeList.add(nodeName);
            }
        }
        List<String> decreaseNodeList = new ArrayList<>();
        for (String nodeName : nodeAppkeyRouteMap.keySet()) {
            if (newNodeCfgMap.get(nodeName) == null) {
                decreaseNodeList.add(nodeName);
            }
        }
        if (!increaseNodeList.isEmpty()) {
            LOGGER.info("Increase server node {}, dynamic adjust appkey route", increaseNodeList);
            routeAdjustService.dynamicAdjustIncreaseNode(increaseNodeList);
        }
        if (!decreaseNodeList.isEmpty()) {
            LOGGER.info("Decrease server node {}, dynamic adjust appkey route", decreaseNodeList);
            routeAdjustService.dynamicAdjustDecreaseNode(decreaseNodeList);
        }
    }

    public boolean isRouteAppkey(String appkey) {
        if (StringUtils.isBlank(nodeName)) {
            return false;
        }
        Set<String> appkeys = nodeAppkeyRouteMap.get(nodeName);
        if (CollectionUtils.isNotEmpty(appkeys)) {
            if (appkeys.size() == 1 && appkeys.contains(DEFAULTS)) {
                // 默认节点
                return !allSpecifyAppkey.contains(appkey);
            } else {
                return appkeys.contains(appkey);
            }
        }
        return false;
    }

    public void updateNodeRouteCfg(String routeCfg) {
        errorLogConfigClient.setValue(ERROR_LOG_ROUTE_CFG, routeCfg);
    }

    private boolean parseConsumerTopicCfg(String consumerTopicCfgStr) {
        if (StringUtils.isBlank(consumerTopicCfgStr)) {
            LOGGER.error("{} no config value", ERROR_LOG_CONSUMER_TOPIC_CFG);
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]节点初始化配置为空, 请检查" + ERROR_LOG_CONSUMER_TOPIC_CFG);
            return false;
        }
        Map<String, Map<HostInitCfgItem, String>> hostInitCfgMap = doParseConsumerTopicCfg(consumerTopicCfgStr);
        if (hostInitCfgMap == null) {
            return false;
        }
        if (hostInitCfgMap != null && !hostInitCfgMap.containsKey(localHostname)) {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志] " + localHostname + "节点未获取到初始化配置, 请检查" + ERROR_LOG_CONSUMER_TOPIC_CFG
                    + ", 如果是下线主机请及时下掉机器");
            LOGGER.error("Curr node no mafka cfg in {}, content={}", ERROR_LOG_CONSUMER_TOPIC_CFG, consumerTopicCfgStr);
            logConsumer.destroyMafkaClient();
            return false;
        }
        Map<HostInitCfgItem, String> initCfgMap = hostInitCfgMap.get(localHostname);
        nodeName = initCfgMap.get(HostInitCfgItem.NodeName);
        consumerGroupName = initCfgMap.get(HostInitCfgItem.ConsumerGroup);
        topicName = initCfgMap.get(HostInitCfgItem.Topic);
        LOGGER.info("Curr node: nodeName={}, consumerGroupName={}, topic={}", nodeName, consumerGroupName, topicName);
        return true;
    }

    public Map<String, Map<HostInitCfgItem, String>> doParseConsumerTopicCfg(String consumerTopicCfgStr) {
        Map<String, Map<HostInitCfgItem, String>> hostInitCfgMap = new HashMap<>();
        String[] consumerTopicCfgs = consumerTopicCfgStr.replaceAll("\\s", "").split(";");
        Set<String> hosts = new HashSet<>();
        Set<String> nodes = new HashSet<>();
        Set<String> consumers = new HashSet<>();
        for (String consumerTopicCfg : consumerTopicCfgs) {
            String[] hostNodeCfgs = consumerTopicCfg.split(":");
            if (hostNodeCfgs.length < 2) {
                LOGGER.error("{} config is not right", ERROR_LOG_CONSUMER_TOPIC_CFG);
                Messager.sendXMAlarmToErrorLogAdmin("[异常日志]节点初始化配置有误, 请检查" + ERROR_LOG_CONSUMER_TOPIC_CFG);
                return null;
            }
            String hostname = hostNodeCfgs[0];
            String cfgItemStr = hostNodeCfgs[1];
            String[] cfgItems = cfgItemStr.split("\\|");
            if (cfgItems.length < 3) {
                LOGGER.error("{} config is not right", ERROR_LOG_CONSUMER_TOPIC_CFG);
                Messager.sendXMAlarmToErrorLogAdmin("[异常日志] " + localHostname + "节点初始化配置有误, 请检查" +
                        ERROR_LOG_CONSUMER_TOPIC_CFG + "的配置项:" + hostname);
                return null;
            }
            Map<HostInitCfgItem, String> hostCfgInfoMap = new HashMap<>();
            hostInitCfgMap.put(hostname, hostCfgInfoMap);
            hostCfgInfoMap.put(HostInitCfgItem.NodeName, cfgItems[0]);
            hostCfgInfoMap.put(HostInitCfgItem.Topic, cfgItems[1]);
            hostCfgInfoMap.put(HostInitCfgItem.ConsumerGroup, cfgItems[2]);
            hosts.add(hostname);
            nodes.add(cfgItems[0]);
            consumers.add(cfgItems[2]);
        }
        if (hosts.size() != nodes.size() || nodes.size() != consumers.size()) {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志] 请检查配置" + ERROR_LOG_CONSUMER_TOPIC_CFG + ", host,node,consumer不是一一对应。");
            return null;
        }
        return hostInitCfgMap;
    }

    private void parseRouteConfig(String routeCfgStr) {
        TreeMap<String, Set<String>> routeMap = new TreeMap<>();
        Set<String> allAppkey = new HashSet<>();
        if (StringUtils.isBlank(routeCfgStr)) {
            LOGGER.error("{} no config value", ERROR_LOG_ROUTE_CFG);
            return;
        }
        String[] routes = routeCfgStr.replaceAll("\\s", "").split(";");
        for (String route : routes) {
            String[] routerConfig = route.split(":");
            if (routerConfig.length < 2) {
                LOGGER.error("{} config is not right", ERROR_LOG_ROUTE_CFG);
                continue;
            }
            String nodeName = routerConfig[0];
            String[] routeAppkeys = routerConfig[1].split(",");
            List<String> list = Arrays.asList(routeAppkeys);
            routeMap.put(nodeName, new HashSet<>(list));
            allAppkey.addAll(list);
        }
        nodeAppkeyRouteMap = routeMap;
        allSpecifyAppkey = allAppkey;
        if (CollectionUtils.isEmpty(nodeAppkeyRouteMap.get(nodeName))) {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志] " + localHostname + "-" + nodeName + "未获取到Appkey路由配置, 请检查" + ERROR_LOG_ROUTE_CFG);
            LOGGER.error("Curr node no route appkey cfg in {}, content={}", ERROR_LOG_ROUTE_CFG, routeCfgStr);
        }
        LOGGER.info("Curr node route appkey:{}, routeMapSize={}", nodeAppkeyRouteMap.get(nodeName), nodeAppkeyRouteMap.size());
    }


    public Map<String, Set<String>> getNodeAppkeyRouteMap() {
        return CloneUtil.clone(nodeAppkeyRouteMap);
    }

    public String getConsumerGroupName() {
        return consumerGroupName;
    }

    public void setConsumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
