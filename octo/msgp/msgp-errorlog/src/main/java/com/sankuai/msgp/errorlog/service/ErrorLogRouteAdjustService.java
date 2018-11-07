package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.common.utils.client.Messager;
import com.sankuai.msgp.errorlog.dao.ErrorLogDayReportDao;
import com.sankuai.msgp.errorlog.pojo.ErrorLogCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by emma on 2017/9/15.
 *
 * 初始化Appkey路由和路由动态调整
 */
@Service
public class ErrorLogRouteAdjustService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogRouteAdjustService.class);

    private final Double AVG_APPKEY_COUNT_OFFSET_RATE = 0.2;
    private final Integer HIST_DATA_DAYS = 3;
    private final Integer AVG_LOG_COUNT_OFFSET_RANGE = 100 * 60 * 24 * HIST_DATA_DAYS; // 即每分钟处理日志偏差是100

    @Autowired
    private ErrorLogDayReportDao errorLogDayReportDao;
    @Autowired
    private ErrorLogRouteCfgService routeCfgService;

    enum SortType {
        ASC, DESC, NO
    }

    enum AdjustType {
        DEFAULT_NODE, SPECIFY_KEY_NODE, DECREASE_NODE, INCREASE_NODE
    }

    /**
     * 生成初始化appkey路由配置
     *
     * @param hostCount
     * @return
     */
    public String generateInitAppkeyRouteConfig(int hostCount) {
        hostCount = hostCount - 1;
        int logCountSum = errorLogDayReportDao.get3DaysLogCountSum();
        int avgLogCountPerHost = logCountSum / hostCount;
        List<ErrorLogCount> appkeysLogCount = errorLogDayReportDao.get3DaysAppkeyLogCount();

        Map<String, Map<String, Integer>> nodeAppkeyLogCountMap = new HashMap<>();
        Map<String, Integer> nodeLogSumMap = new HashMap<>();
        Map<String, Integer> nodeAppkeyNumMap = new HashMap<>();
        // 初始化Map
        for (int i = 1; i <= hostCount; i++) {
            Map<String, Integer> appkeyLogCountMap = new HashMap<>();
            String nodeName = "node" + i;
            nodeAppkeyLogCountMap.put(nodeName, appkeyLogCountMap);
            nodeLogSumMap.put(nodeName, 0);
            nodeAppkeyNumMap.put(nodeName, 0);
        }

        for (ErrorLogCount logCountObj : appkeysLogCount) {
            String appkey = logCountObj.getAppkey();
            int logCount = logCountObj.getLogCount();

            int hostIndex = 0;
            List<Map.Entry<String, Integer>> nodeSortByAppkeyNum = sortMapByValue(nodeAppkeyNumMap, SortType.ASC);
            for (Map.Entry<String, Integer> entry : nodeSortByAppkeyNum) {
                String nodeName = entry.getKey();

                int logSum = nodeLogSumMap.get(nodeName);
                if (logSum < avgLogCountPerHost && logSum + logCount <= avgLogCountPerHost + AVG_LOG_COUNT_OFFSET_RANGE) {
                    nodeAppkeyLogCountMap.get(nodeName).put(appkey, logCount);
                    nodeLogSumMap.put(nodeName, logSum + logCount);
                    nodeAppkeyNumMap.put(nodeName, nodeAppkeyLogCountMap.get(nodeName).size());
                    break;
                }
                hostIndex++;
            }

            if (hostIndex == hostCount) {
                List<Map.Entry<String, Integer>> nodeSortByLogSum = sortMapByValue(nodeLogSumMap, SortType.ASC);
                String minCountNodeName = nodeSortByLogSum.get(0).getKey();

                nodeAppkeyLogCountMap.get(minCountNodeName).put(appkey, logCount);
                nodeLogSumMap.put(minCountNodeName, logCount + nodeLogSumMap.get(minCountNodeName));
                nodeAppkeyNumMap.put(minCountNodeName, nodeAppkeyLogCountMap.get(minCountNodeName).size());
            }
        }

        printRouteState(logCountSum, nodeAppkeyLogCountMap, "初始化路由结果");

        String defaultNode = "node" + (hostCount + 1);
        String routeConfig = transferMapCfgToString(nodeAppkeyLogCountMap, defaultNode);
        return routeConfig;
    }

    public boolean dynamicAdjustAppkeyRoute(Map<String, Set<String>> currNodeRouteMap, boolean forceUpdateRoute) {
        boolean isAdjusted = false;
        String defaultNode = "";
        int routeSize = currNodeRouteMap.size();
        for (Map.Entry<String, Set<String>> entry : currNodeRouteMap.entrySet()) {
            if (entry.getValue().contains("defaults")) {
                defaultNode = entry.getKey();
            }
            if (--routeSize == 0 && defaultNode.isEmpty()) {
                defaultNode = entry.getKey();
            }
        }

        NodeAppkeyLogCount nodeAppkeyLogCount = getNodeAppkeyLogCount(defaultNode, currNodeRouteMap);
        Map<String, Integer> nodeLogCountMap = nodeAppkeyLogCount.getNodeLogCountMap(); // <node1, logcount>
        Map<String, Map<String, Integer>> nodeAppkeyLogCountMap = nodeAppkeyLogCount.getNodeAppkeyLogCountMap(); // <node1, <appkey1, logcount>>
        Map<String, Integer> nodeAppkeyNumMap = nodeAppkeyLogCount.getNodeAppkeyNumMap(); // <node1, appkeyCount>

        int logCountSum = errorLogDayReportDao.get3DaysLogCountSum();
        printRouteState(logCountSum, nodeAppkeyLogCountMap, "调整前");

        int nodeCount = nodeLogCountMap.size();
        if (nodeCount <= 1) {
            LOGGER.info("Do manual node adjustment, but node num={}, no need adjust.", nodeCount);
            return false;
        }
        int avgLogCountPerHost = logCountSum / (nodeCount - 1);

        // 1. 检查默认节点是否处理日志过多
        // 节点处理日志数超过指定路由机器平均数的1/3, 进行调整
        if (nodeLogCountMap.get(defaultNode) > avgLogCountPerHost / 3) {
            dynamicAdjustDefaultNode(defaultNode, nodeLogCountMap, nodeAppkeyNumMap, nodeAppkeyLogCountMap);
            isAdjusted = true;
            printRouteState(logCountSum, nodeAppkeyLogCountMap, "调整默认节点后");
        }

        Map<String, Integer> nodeLogCountMapNoDefalut = new HashMap<>(nodeLogCountMap);
        Map<String, Integer> nodeAppkeyNumMapNoDefalut = new HashMap<>(nodeAppkeyNumMap);
        nodeLogCountMapNoDefalut.remove(defaultNode);
        nodeAppkeyNumMapNoDefalut.remove(defaultNode);
        // 2. 检查其他节点是否均衡
        // 非默认节点中, 日志处理最多的节点日志多于最少节点的30%, 进行调整
        List<Map.Entry<String, Integer>> nodeCountList = sortMapByValue(nodeLogCountMapNoDefalut, SortType.ASC);
        Double maxLogCount = nodeCountList.get(nodeCountList.size() - 1).getValue().doubleValue();
        Double minLogCount = nodeCountList.get(0).getValue().doubleValue();
        if ((maxLogCount - minLogCount) > minLogCount * 0.3) {
            dynamicAdjustSpecifyNode(defaultNode, nodeCountList, nodeLogCountMapNoDefalut, nodeAppkeyNumMapNoDefalut, nodeAppkeyLogCountMap);
            isAdjusted = true;
            printRouteState(logCountSum, nodeAppkeyLogCountMap, "调整其他节点后");
        }

        // 更新路由配置
        if (isAdjusted || forceUpdateRoute) {
            // 条件二, 是为了避免减少节点后, 没有满足前面两个调整条件，未触发动态路由导致路由配置还有下掉节点
            updateAppkeyRouteCfg(nodeAppkeyLogCountMap, defaultNode);
            isAdjusted = true;
        }
        return isAdjusted;
    }

    public void dynamicAdjustIncreaseNode(List<String> increaseNodeList) {
        Map<String, Set<String>> currNodeRouteMap = routeCfgService.getNodeAppkeyRouteMap();
        for (String nodeName : increaseNodeList) {
            currNodeRouteMap.put(nodeName, new HashSet<String>());
        }
        boolean isAdjusted = dynamicAdjustAppkeyRoute(currNodeRouteMap, true);
        LOGGER.info("Increase server node, finish dynamic adjust appkey route, isAjusted=" + isAdjusted);
        if (isAdjusted) {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志] 增加节点路由动态调整结束");
        } else {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志] 增加节点" + increaseNodeList + "Appkey路由未做调整!!请检查服务日志及配置");
        }
    }

    public void dynamicAdjustDecreaseNode(List<String> decreaseNodeList) {
        Map<String, Set<String>> currNodeRouteMap = routeCfgService.getNodeAppkeyRouteMap();
        for (String nodeName : decreaseNodeList) {
            Set<String> removeValue = currNodeRouteMap.remove(nodeName);
            if (removeValue == null) {
                Messager.sendXMAlarmToErrorLogAdmin("[异常日志] 减少节点，当前路由无" + nodeName + "节点, 当前路由节点=" + currNodeRouteMap.keySet());
                LOGGER.error("Decrease server node, curr route no {}, currRouteNode={}", nodeName, currNodeRouteMap.keySet());
            }
        }
        boolean isAdjusted = dynamicAdjustAppkeyRoute(currNodeRouteMap, true);
        LOGGER.info("Decrease server node, finish dynamic adjust appkey route, isAjusted=" + isAdjusted);
        if (isAdjusted) {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志] 减少节点路由动态调整结束");
        } else {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志] 减少节点" + decreaseNodeList + "，Appkey路由未做调整!!请检查服务日志及配置");
        }
    }

    /**
     * 默认节点动态路由策略:
     * 默认节点的appkey依次从 日志处理最少的节点开发分配，直到默认节点日志数小于avgLogCountPerHost/2
     *
     * @param defaultNode
     * @param nodeAppkeyLogCountMap
     * @return
     */
    private void dynamicAdjustDefaultNode(String defaultNode, Map<String, Integer> nodeLogCountMap,
                                          Map<String, Integer> nodeAppkeyNumMap,
                                          Map<String, Map<String, Integer>> nodeAppkeyLogCountMap) {
        LogAndAppkeyCount logAndAppkeyCount = getLogAndAppkeyCount(nodeLogCountMap, nodeAppkeyNumMap);

        int nodeCount = logAndAppkeyCount.getNodeCount();
        int avgLogCountPerNode = logAndAppkeyCount.getLogCountSum() / (nodeCount - 1);
        int defaultNodeLogCountVal = avgLogCountPerNode / 3;
        int avgAppkeyCountPerNode = logAndAppkeyCount.getAppkeyCountSum() / (nodeCount - 1);
        int defaultNodeAppkeyCountVal = avgAppkeyCountPerNode / 3;

        migrateNodeAppkey(AdjustType.DEFAULT_NODE, defaultNode, defaultNode, avgLogCountPerNode, defaultNodeLogCountVal,
                avgAppkeyCountPerNode, defaultNodeAppkeyCountVal, nodeLogCountMap, nodeAppkeyNumMap, nodeAppkeyLogCountMap);
    }

    /**
     * 非默认节点, 路由动态调整
     *
     * @param nodeLogCountMapNoDefalut
     * @param nodeAppkeyLogCountMap
     */
    private void dynamicAdjustSpecifyNode(String defaultNode, List<Map.Entry<String, Integer>> nodeCountListNoDefault,
                                          Map<String, Integer> nodeLogCountMapNoDefalut,
                                          Map<String, Integer> nodeAppkeyNumMapNoDefault,
                                          Map<String, Map<String, Integer>> nodeAppkeyLogCountMap) {

        LogAndAppkeyCount logAndAppkeyCount = getLogAndAppkeyCount(nodeLogCountMapNoDefalut, nodeAppkeyNumMapNoDefault);
        int nodeCount = logAndAppkeyCount.getNodeCount();
        int avgLogCountPerNode = logAndAppkeyCount.getLogCountSum() / nodeCount;
        int avgAppkeyCountPerNode = logAndAppkeyCount.getAppkeyCountSum() / nodeCount;


        int loopCount = nodeCount % 2 == 0 ? nodeCount / 2 : nodeCount / 2 + 1;
        while (loopCount-- > 0) {
            // 从日志量最大node开始，迁移其多的appkey至其他node
            Map.Entry<String, Integer> nodeLogCountEntry = nodeCountListNoDefault.remove(nodeCountListNoDefault.size() - 1);
            String nodeName = nodeLogCountEntry.getKey();

            migrateNodeAppkey(AdjustType.SPECIFY_KEY_NODE, nodeName, defaultNode, avgLogCountPerNode, Integer.MAX_VALUE,
                    avgAppkeyCountPerNode, Integer.MAX_VALUE, nodeLogCountMapNoDefalut, nodeAppkeyNumMapNoDefault, nodeAppkeyLogCountMap);
        }
    }

    private void migrateNodeAppkey(AdjustType adjustType, String migNodeName, String defaultNode,
                                   int avgLogCountPerNode, int defaultNodeLogCountVal, int avgAppkeyCountPerNode, int defaultNodeAppkeyCountVal,
                                   Map<String, Integer> nodeLogCountMap, Map<String, Integer> nodeAppkeyNumMap,
                                   Map<String, Map<String, Integer>> nodeAppkeyLogCountMap) {
        SortType appkeyWithLogCountSortType = SortType.NO;
        SortType changeSortType = appkeyWithLogCountSortType;
        int countFlag = 0; // 避免死循环变量
        while ((AdjustType.SPECIFY_KEY_NODE.equals(adjustType) && nodeLogCountMap.get(migNodeName) > avgLogCountPerNode + AVG_LOG_COUNT_OFFSET_RANGE) ||
                (AdjustType.DEFAULT_NODE.equals(adjustType) && nodeLogCountMap.get(migNodeName) > defaultNodeLogCountVal)) {
            Map<String, Integer> appkeyLogCountMap = nodeAppkeyLogCountMap.get(migNodeName);
            if (countFlag == appkeyLogCountMap.size()) {
                // 避免死循环
                break;
            }
            countFlag = appkeyLogCountMap.size();
            List<Map.Entry<String, Integer>> appkeySortByLogCount = new ArrayList<>(appkeyLogCountMap.entrySet());
            if (!appkeyWithLogCountSortType.equals(changeSortType)) {
                appkeySortByLogCount = sortMapByValue(nodeAppkeyLogCountMap.get(migNodeName), changeSortType);
                appkeyWithLogCountSortType = changeSortType;
            }

            for (Map.Entry<String, Integer> appkeyCountEntry : appkeySortByLogCount) {
                String appkey = appkeyCountEntry.getKey();
                Integer logCount = appkeyLogCountMap.get(appkey);
                if ((AdjustType.SPECIFY_KEY_NODE.equals(adjustType) && nodeLogCountMap.get(migNodeName) <= avgLogCountPerNode + AVG_LOG_COUNT_OFFSET_RANGE) ||
                        (AdjustType.DEFAULT_NODE.equals(adjustType) && nodeLogCountMap.get(migNodeName) <= defaultNodeLogCountVal)) {
                    break;
                }
                // 遍历节点, 将当前Appkey迁移到符合条件的node上
                // 迁入的节点按ppkey数量排序, 优先放入Appkey少的node上
                List<Map.Entry<String, Integer>> nodeSortByAppkeyNum = sortMapByValue(nodeAppkeyNumMap, SortType.ASC);
                boolean isMoved = false;
                for (Map.Entry<String, Integer> entry : nodeSortByAppkeyNum) {
                    String toNodeName = entry.getKey();
                    if (toNodeName.equals(migNodeName) || toNodeName.equalsIgnoreCase(defaultNode)) {
                        continue;
                    }

                    int nodeLogSum = nodeLogCountMap.get(toNodeName);
                    if ((nodeLogSum < avgLogCountPerNode && nodeLogSum + logCount <= avgLogCountPerNode + AVG_LOG_COUNT_OFFSET_RANGE) &&
                            ((AdjustType.SPECIFY_KEY_NODE.equals(adjustType) && nodeLogCountMap.get(migNodeName) - logCount >= avgLogCountPerNode - AVG_LOG_COUNT_OFFSET_RANGE) ||
                                    AdjustType.DEFAULT_NODE.equals(adjustType))) {
                        nodeLogCountMap.put(toNodeName, nodeLogSum + logCount);
                        nodeAppkeyLogCountMap.get(toNodeName).put(appkey, logCount);
                        nodeAppkeyNumMap.put(toNodeName, nodeAppkeyLogCountMap.get(toNodeName).size());

                        nodeLogCountMap.put(migNodeName, nodeLogCountMap.get(migNodeName) - logCount);
                        nodeAppkeyLogCountMap.get(migNodeName).remove(appkey);
                        nodeAppkeyNumMap.put(migNodeName, nodeAppkeyLogCountMap.get(migNodeName).size());
                        isMoved = true;
                        break;
                    }
                }
                if (!isMoved && AdjustType.DEFAULT_NODE.equals(adjustType)) {
                    // 如果是默认节点的Appkey迁出, 没有符合条件时就将其Appkey迁移到日志最少的节点上
                    // 避免出现某个Appkey日志量巨大，迁移不出去
                    List<Map.Entry<String, Integer>> nodeSortByLogSum = sortMapByValue(nodeLogCountMap, SortType.ASC);
                    String minLogCountNodeName = nodeSortByLogSum.get(0).getKey();
                    if (minLogCountNodeName.equals(migNodeName)) {
                        minLogCountNodeName = nodeSortByLogSum.get(1).getKey();
                    }
                    nodeLogCountMap.put(minLogCountNodeName, nodeLogCountMap.get(minLogCountNodeName) + logCount);
                    nodeAppkeyLogCountMap.get(minLogCountNodeName).put(appkey, logCount);
                    nodeAppkeyNumMap.put(minLogCountNodeName, nodeAppkeyLogCountMap.get(minLogCountNodeName).size());

                    nodeLogCountMap.put(migNodeName, nodeLogCountMap.get(migNodeName) - logCount);
                    nodeAppkeyLogCountMap.get(migNodeName).remove(appkey);
                    nodeAppkeyNumMap.put(migNodeName, nodeAppkeyLogCountMap.get(migNodeName).size());
                } else if (!isMoved) {
                    continue;
                }
                int appkeyCountVal = AdjustType.DEFAULT_NODE.equals(adjustType) ? defaultNodeAppkeyCountVal : avgAppkeyCountPerNode;

                if (nodeAppkeyNumMap.get(migNodeName) < appkeyCountVal * (1 - AVG_APPKEY_COUNT_OFFSET_RATE) &&
                        !SortType.DESC.equals(appkeyWithLogCountSortType)) {
                    // 迁移节点的Appkey较少, 避免Appkey迁出较多, 将Appkey按日志数逆序排序
                    changeSortType = SortType.DESC;
                    break;
                } else if (nodeAppkeyNumMap.get(migNodeName) > appkeyCountVal * (1 + AVG_APPKEY_COUNT_OFFSET_RATE) &&
                        !SortType.ASC.equals(appkeyWithLogCountSortType)) {
                    // 迁移节点的Appkey较多, 避免Appkey迁出较少, 将Appkey按日志数顺序排序
                    changeSortType = SortType.ASC;
                    break;
                }
            }
        }
    }

    private void updateAppkeyRouteCfg(Map<String, Map<String, Integer>> nodeAppkeyLogCountMap, String defaultNode) {
        String routeCfg = transferMapCfgToString(nodeAppkeyLogCountMap, defaultNode);
        routeCfgService.updateNodeRouteCfg(routeCfg);
    }

    private String transferMapCfgToString(Map<String, Map<String, Integer>> nodeAppkeyLogCountMap, String defaultNode) {
        Map<String, Map<String, Integer>> sortedMap = new TreeMap<>(nodeAppkeyLogCountMap);
        StringBuilder routeConfig = new StringBuilder();
        for (Map.Entry<String, Map<String, Integer>> nodeAppkeyLogCount : sortedMap.entrySet()) {
            String nodeName = nodeAppkeyLogCount.getKey();
            if (nodeName.equalsIgnoreCase(defaultNode)) {
                continue;
            }
            String appkeys = nodeAppkeyLogCount.getValue().keySet().toString().replaceAll("[\\[\\]\\s]", "");
            ;
            routeConfig.append(nodeName).append(":").append(appkeys).append(";");
        }
        routeConfig.append(defaultNode).append(":defaults");
        return routeConfig.toString();
    }

    private static List<Map.Entry<String, Integer>> sortMapByValue(Map<String, Integer> map, final SortType sortType) {
        List<Map.Entry<String, Integer>> nodeCountList = new ArrayList<>(map.entrySet());
        Collections.sort(nodeCountList, new Comparator<Map.Entry<String, Integer>>() {
            //从小到大排序
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if (SortType.ASC.equals(sortType)) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });
        return nodeCountList;
    }

    public NodeAppkeyLogCount getNodeAppkeyLogCount(String defaultNode, Map<String, Set<String>> routeMap) {
        List<ErrorLogCount> appkeysLogCount = errorLogDayReportDao.get3DaysAppkeyLogCount();

        Map<String, Integer> nodeLogCountMap = new HashMap<>();
        Map<String, Map<String, Integer>> nodeAppkeyLogCountMap = new HashMap<>();
        Map<String, Integer> nodeAppkeyNumMap = new HashMap<>();
        nodeLogCountMap.put(defaultNode, 0);
        nodeAppkeyLogCountMap.put(defaultNode, new HashMap<String, Integer>());
        for (ErrorLogCount appkeyLogCount : appkeysLogCount) {
            String appkey = appkeyLogCount.getAppkey();
            Integer logCount = appkeyLogCount.getLogCount();
            boolean hasCount = false;
            for (Map.Entry<String, Set<String>> entry : routeMap.entrySet()) {
                String node = entry.getKey();
                if (!entry.getValue().contains(appkey)) {
                    continue;
                }
                // 构造nodeLogCount
                if (nodeLogCountMap.get(node) == null) {
                    nodeLogCountMap.put(node, logCount);
                } else {
                    nodeLogCountMap.put(node, nodeLogCountMap.get(node) + logCount);
                }
                // 构造nodeAppkeyLogCountMap
                Map<String, Integer> appkeyLogCountMap = nodeAppkeyLogCountMap.get(node);
                if (appkeyLogCountMap == null) {
                    appkeyLogCountMap = new HashMap<>();
                    nodeAppkeyLogCountMap.put(node, appkeyLogCountMap);
                }
                appkeyLogCountMap.put(appkey, logCount);

                // 构造nodeAppkeyNumMap
                nodeAppkeyNumMap.put(node, nodeAppkeyNumMap.get(node) == null ? 1 : nodeAppkeyNumMap.get(node) + 1);
                hasCount = true;
            }
            if (hasCount) {
                continue;
            }
            nodeLogCountMap.put(defaultNode, nodeLogCountMap.get(defaultNode) + logCount);
            Map<String, Integer> appkeyLogCountMap = nodeAppkeyLogCountMap.get(defaultNode);
            appkeyLogCountMap.put(appkey, logCount);
            nodeAppkeyNumMap.put(defaultNode, nodeAppkeyNumMap.get(defaultNode) == null ? 1 : nodeAppkeyNumMap.get(defaultNode) + 1);
        }

        for (Map.Entry<String, Set<String>> entry : routeMap.entrySet()) {
            String nodeName = entry.getKey();
            if (entry.getValue().isEmpty()) {
                nodeLogCountMap.put(nodeName, 0);
                nodeAppkeyLogCountMap.put(nodeName, new HashMap<String, Integer>());
                nodeAppkeyNumMap.put(nodeName, 0);
            }
        }

        NodeAppkeyLogCount nodeAppkeyLogCount = new NodeAppkeyLogCount();
        nodeAppkeyLogCount.setNodeLogCountMap(nodeLogCountMap);
        nodeAppkeyLogCount.setNodeAppkeyLogCountMap(nodeAppkeyLogCountMap);
        nodeAppkeyLogCount.setNodeAppkeyNumMap(nodeAppkeyNumMap);
        return nodeAppkeyLogCount;
    }

    private LogAndAppkeyCount getLogAndAppkeyCount(Map<String, Integer> nodeLogCountMap, Map<String, Integer> nodeAppkeyNumMap) {
        int logCountSum = 0;
        int appkeyCountSum = 0;
        for (Map.Entry<String, Integer> entry : nodeLogCountMap.entrySet()) {
            String nodeName = entry.getKey();
            logCountSum += entry.getValue();
            appkeyCountSum += nodeAppkeyNumMap.get(nodeName);
        }
        int nodeNum = nodeLogCountMap.size();

        LogAndAppkeyCount logAndAppkeyCount = new LogAndAppkeyCount();
        logAndAppkeyCount.setNodeCount(nodeNum);
        logAndAppkeyCount.setAppkeyCountSum(appkeyCountSum);
        logAndAppkeyCount.setLogCountSum(logCountSum);
        return logAndAppkeyCount;
    }

    private class NodeAppkeyLogCount {
        private Map<String, Integer> nodeLogCountMap;
        private Map<String, Map<String, Integer>> nodeAppkeyLogCountMap;
        private Map<String, Integer> nodeAppkeyNumMap;

        public Map<String, Integer> getNodeLogCountMap() {
            return nodeLogCountMap;
        }

        public void setNodeLogCountMap(Map<String, Integer> nodeLogCountMap) {
            this.nodeLogCountMap = nodeLogCountMap;
        }

        public Map<String, Map<String, Integer>> getNodeAppkeyLogCountMap() {
            return nodeAppkeyLogCountMap;
        }

        public void setNodeAppkeyLogCountMap(Map<String, Map<String, Integer>> nodeAppkeyLogCountMap) {
            this.nodeAppkeyLogCountMap = nodeAppkeyLogCountMap;
        }

        public Map<String, Integer> getNodeAppkeyNumMap() {
            return nodeAppkeyNumMap;
        }

        public void setNodeAppkeyNumMap(Map<String, Integer> nodeAppkeyNumMap) {
            this.nodeAppkeyNumMap = nodeAppkeyNumMap;
        }
    }

    private class LogAndAppkeyCount {
        private int appkeyCountSum;
        private int logCountSum;
        private int nodeCount;


        public int getNodeCount() {
            return nodeCount;
        }

        public void setNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
        }

        public int getAppkeyCountSum() {
            return appkeyCountSum;
        }

        public void setAppkeyCountSum(int appkeyCountSum) {
            this.appkeyCountSum = appkeyCountSum;
        }

        public int getLogCountSum() {
            return logCountSum;
        }

        public void setLogCountSum(int logCountSum) {
            this.logCountSum = logCountSum;
        }
    }

    private void printRouteState(Integer logCountSum, Map<String, Map<String, Integer>> nodeAppkeyLogCountMap, String msg) {
        int sum = 0;
        StringBuilder infoStr = new StringBuilder(msg);
        for (Map.Entry<String, Map<String, Integer>> entry : nodeAppkeyLogCountMap.entrySet()) {
            String nodeName = entry.getKey();
            Map<String, Integer> appkeyLogCount = entry.getValue();
            int appkeyCount = 0;
            Integer nodelogCountSum = 0;
            for (Map.Entry<String, Integer> logCount : appkeyLogCount.entrySet()) {
                appkeyCount++;
                nodelogCountSum += logCount.getValue();
            }
            sum += nodelogCountSum;
            String str = nodeName + ": " + nodelogCountSum + ";   " + appkeyCount + ";------" + new DecimalFormat("#.00").format(nodelogCountSum.doubleValue() / logCountSum.doubleValue() * 100) + "%";
            infoStr.append("\n");
            infoStr.append(str);
        }
        infoStr.append("\n");
        infoStr.append(sum + " " + logCountSum);
        LOGGER.info(infoStr.toString());
    }
}
