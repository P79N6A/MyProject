package com.sankuai.octo.oswatch.task;

import com.sankuai.octo.oswatch.model.OctoEnv;
import com.sankuai.octo.oswatch.model.WatchResult;
import com.sankuai.octo.oswatch.model.WatchResultType;
import com.sankuai.octo.oswatch.service.LogCollectorService;
import com.sankuai.octo.oswatch.service.MSGPHttpService;
import com.sankuai.octo.oswatch.service.PerfService;
import com.sankuai.octo.oswatch.service.QPSReader;
import com.sankuai.octo.oswatch.thrift.data.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by chenxi on 6/5/15.
 */
public class QuotaWatchTask implements Callable<WatchResult> {
    private final static Log logger = LogFactory.getLog(QuotaWatchTask.class);
    final static String ALL_QPS_KEY = "all";
    final static String DEFAULT_CONSUMER_KEY = "default";
    ProviderQuota quota;
    LogCollectorService logCollectorService;
    MSGPHttpService msgpHttpService;
    Long lastCheckTimestamp;
    Long currentTimestamp;

    public QuotaWatchTask(ProviderQuota quota, String logAggregateURL, MSGPHttpService msgpClient, long currentTimestamp, long lastCheckTimestamp) {
        this.quota = quota;
        this.lastCheckTimestamp = lastCheckTimestamp;
        this.currentTimestamp = currentTimestamp;
        this.logCollectorService = new LogCollectorService(logAggregateURL);
        msgpHttpService = msgpClient;
    }

    @Override
    public WatchResult call() throws Exception {
        List<DegradeAction> actions = new ArrayList<DegradeAction>();

        //no need to check watch time, because it is a moving time window
        long secondDiff = (currentTimestamp - lastCheckTimestamp) / QPSReader.ONE_SECOND_IN_MS;
        if (secondDiff < quota.getWatchPeriodInSeconds()) {
            logger.debug("the watch task is not at time");
            return new WatchResult(quota, actions, WatchResultType.NOT_AT_TIME, 0);
        }

        //get qps map from performance service
        Map<String, Double> qpsMap = logCollectorService.getCurrentQPS(
                        quota.providerAppkey,
                        quota.method,
                        OctoEnv.getEnv(quota.getEnv()),
                        currentTimestamp,
                        quota.getWatchPeriodInSeconds());

        //check qps capacity
        long allQps = 0;
        for(Map.Entry<String, Double> entry: qpsMap.entrySet()){
            logger.info("comsumer: " + entry.getKey() + "  qps: " + entry.getValue());
            allQps += entry.getValue();
        }

        logger.info("QPS from log collector: "+ allQps);

        int aliveNode = 0;
        for (int i=0; i< 3; i++) {
            try {
                aliveNode = msgpHttpService.getAliveNode(quota);
            } catch (Exception e) {
                logger.error(e);
            }

            if (aliveNode != 0) break;
        }
        if (aliveNode == 0) logger.info("alive node is zero, appkey: " + quota.getProviderAppkey());

        if (quota.QPSCapacity * aliveNode >= allQps) {
            logger.debug(String.format("current qps: %d, quota qps: %d", quota.QPSCapacity * aliveNode, allQps));
            return new WatchResult(quota, actions, WatchResultType.OK, aliveNode);
        }
        //create degrade action depends on the current qps beyond consumer quota qps
        for (ConsumerQuota consumerQuota: quota.consumerList) {
            if (!qpsMap.containsKey(consumerQuota.getConsumerAppkey())) {
                logger.error(String.format("consumer %s has not request to provider %s", consumerQuota.getConsumerAppkey(), quota.providerAppkey));
                return new WatchResult(quota, actions, WatchResultType.CONSUMER_NOT_EXSIT, aliveNode);
            }

            double currentQPS = qpsMap.get(consumerQuota.getConsumerAppkey());
            logger.info("appkey: "+consumerQuota.getConsumerAppkey()+"qps: "+qpsMap.get(consumerQuota.getConsumerAppkey()));
            DegradeAction action = createDegradeAction(consumerQuota, currentQPS, aliveNode);

            if (action != null) actions.add(action);
        }

        return new WatchResult(quota, actions, WatchResultType.OK, aliveNode);
    }

    private DegradeAction createDegradeAction(ConsumerQuota consumerQuota, double currentQPS, int aliveNode) {
        double configQPS = consumerQuota.getQPSRatio() * quota.QPSCapacity * aliveNode;
        if (configQPS > currentQPS) return null;

        double degradeRatio = (currentQPS - configQPS)/currentQPS;

        return genDegradeAction(generateDegradeActionId(consumerQuota.consumerAppkey),
                consumerQuota.getConsumerAppkey(),
                degradeRatio,
                consumerQuota.getDegradeStrategy(),
                consumerQuota.getDegradeRedirect(),
                currentQPS);
    }

    private DegradeAction genDegradeAction(String actionId, String cAppkey, double ratio, DegradeStrategy strategy, String redirect, Double currentQPS){
        DegradeAction action = new DegradeAction();
        action.setId(actionId)
                .setConsumerAppkey(cAppkey)
                .setTimestamp(currentTimestamp)
                .setDegradeRatio(ratio)
                .setDegradeStrategy(strategy)
                .setMethod(quota.getMethod())
                .setProviderAppkey(quota.getProviderAppkey())
                .setEnv(quota.getEnv())
                .setConsumerQPS(currentQPS.intValue())
                .setDegradeEnd(quota.getDegradeEnd());

        if (redirect != null) action.setDegradeRedirect(redirect);

        return action;
    }

    private String generateDegradeActionId(String cAppkey) {
        return  cAppkey +'/'+quota.getMethod()+'/'+ quota.getId();
    }

    public LogCollectorService getLogCollectorService() {
        return logCollectorService;
    }

    public void setLogCollectorService(LogCollectorService logCollectorService) {
        this.logCollectorService = logCollectorService;
    }
}
