package com.sankuai.octo.oswatch.task;

import com.meituan.jmonitor.LOG;
import com.sankuai.octo.msgp.thrift.service.MSGPService;
import com.sankuai.octo.oswatch.model.WatchResult;
import com.sankuai.octo.oswatch.model.WatchResultType;
import com.sankuai.octo.oswatch.service.MSGPHttpService;
import com.sankuai.octo.oswatch.thrift.data.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Provider;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.sankuai.octo.oswatch.Config;
/**
 * Created by chenxi on 6/5/15.
 */
public class QuotaMainTask implements Runnable {
    private final static Log logger = LogFactory.getLog(QuotaMainTask.class);
    private int checkInterval;
    private String perURL;

    private CompletionService completionService;
    private Map<String, ProviderQuota> quotaMap;
    private MSGPHttpService msgpHttpService;

    private boolean continueLoop = true;
    private MSGPService.Iface msgpClient;
    private Map<String, Long> lastCheckMap;
    private AtomicBoolean isMasterRunning;

    public QuotaMainTask(MSGPService.Iface msgpClient,
                         CompletionService completionService,
                         int checkInterval,
                         String logAggregateURL,
                         String msgpURL,
                         AtomicBoolean isMasterRunning) {
        this.msgpClient = msgpClient;
        this.checkInterval = checkInterval;
        this.perURL = logAggregateURL;
        this.completionService = completionService;
        this.quotaMap = new HashMap<String, ProviderQuota>();
        this.lastCheckMap = new HashMap<String, Long>();
        this.msgpHttpService = new MSGPHttpService(msgpURL);
        this.isMasterRunning = isMasterRunning;
    }

    public void run() {
        while (continueLoop) {
            if (!isMasterRunning.get()){
                LOG.info("slave,quit");
                return;
            }
            try {
                LOG.info("master,enter");
                loadQuotaData();
                concurrentWatchProvider(quotaMap);
                executeWatchResult(quotaMap.size());
                TimeUnit.SECONDS.sleep(checkInterval);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    public void concurrentWatchProvider(Map<String, ProviderQuota> qMap) {
        Long currentTimestamp = System.currentTimeMillis();
        for (Map.Entry<String, ProviderQuota> entry : qMap.entrySet()) {
            Long lastTimestamp = lastCheckMap.get(entry.getKey());
            completionService.submit(new QuotaWatchTask(entry.getValue(), perURL, msgpHttpService, currentTimestamp, lastTimestamp));
        }
    }

    public void executeWatchResult(int futureCount) {
        for (int i = 0; i < futureCount; i++) {
            try {
                Future<WatchResult> future = completionService.take();
                if (future == null) {
                    logger.error("future is null");
                    continue;
                }

                WatchResult wr = future.get();
                ProviderQuota providerQuota = wr.getProviderQuota();

                if (wr.getResultType() == WatchResultType.NOT_AT_TIME) continue;
                if (wr.getResultType() == WatchResultType.CONSUMER_NOT_EXSIT) continue;

                if (wr.getActions().isEmpty()) deleteDegradeNode(providerQuota);
                else {
                    int status=0;
                    if (providerQuota.getStatus() == DegradeStatus.ENABLE){
                        status+=2;
                        updateDegradeToMSGP(providerQuota, wr.getActions());
                    } else deleteDegradeNode(providerQuota);

                    if (providerQuota.getAlarm() == AlarmStatus.ENABLE)
                        status+=1;
                    //如果status等于0/2(偶数)，即providerQuota.getAlarm()==AlarmStatus.DISABLE,只上报reportMsg，不报警
                    for (DegradeAction action : wr.getActions()) {
                            sendMsgToMSGP(providerQuota, action, wr.getNodeAlive(),status);
                    }
                }

                lastCheckMap.put(providerQuota.getId() , System.currentTimeMillis());
            } catch (InterruptedException ie) {
                logger.error("the future is interrupted", ie);
            } catch (ExecutionException ee) {
                logger.error("the future value is not return", ee);
            }
        }
    }

    public void deleteDegradeNode(ProviderQuota providerQuota) {
        try {
            msgpClient.removeDegradeNode(providerQuota.getEnv(), providerQuota.getProviderAppkey(), providerQuota.getMethod());
        } catch (TException te) {
            logger.fatal("delete degrade node fail", te);
        }
    }

    public void updateDegradeToMSGP(ProviderQuota providerQuota, List<DegradeAction> actions) {
        try {
            msgpClient.updateDegradeAction(providerQuota.getEnv(), providerQuota.getProviderAppkey(), providerQuota.getMethod(), actions);
            logger.info(String.format("env：%s, appkey: %s, method: %s degrade action", providerQuota.getEnv(), providerQuota.getProviderAppkey(), providerQuota.getMethod()));
        } catch (TException te) {
            logger.error("update degrade action to msgp error!!", te);
        }
    }

    private void loadQuotaData() {
        Boolean loop = true;
        while (loop) {
            try {
                int count = msgpClient.countProviderQuota();
                quotaMap.clear();

                List<ProviderQuota> quotaList = msgpClient.getProviderQuota(count, 0);

                if(quotaList != null && !quotaList.isEmpty())
                    for(ProviderQuota quota: quotaList)
                        if (!quota.consumerList.isEmpty()) quotaMap.put(quota.getId(), quota);

                for(Map.Entry<String, ProviderQuota> entry: quotaMap.entrySet()) {
                    String id  = entry.getKey();
                    if (!lastCheckMap.containsKey(entry.getKey())) lastCheckMap.put(id, 0L);
                }

                loop = false;
            } catch (Exception e) {
                logger.error(e);
                try { Thread.sleep(Integer.parseInt(Config.get("loadQuotaTime","2000")));} catch (InterruptedException ie) {logger.error(ie);}
            }
        }
    }

    public void sendMsgToMSGP(ProviderQuota providerQuota, DegradeAction action, int nodeAlive,int status) {
        try {
            msgpHttpService.alarm(providerQuota, action, nodeAlive,status);

        } catch(Exception e) {
            logger.info("alarmToMSGP error>>>>>"+e);
        }
    }

    public boolean isContinueLoop() {
        return continueLoop;
    }

    public void setContinueLoop(boolean continueLoop) {
        this.continueLoop = continueLoop;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }
}
