package com.sankuai.octo.oswatch.controller;

import com.meituan.jmonitor.LOG;
import com.sankuai.octo.msgp.thrift.service.MSGPService;
import com.sankuai.octo.oswatch.*;
import com.sankuai.octo.oswatch.model.WatchResult;
import com.sankuai.octo.oswatch.task.QuotaMainTask;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.sankuai.octo.oswatch.thrift.data.ProviderQuota;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chenxi on 6/4/15.
 */
public class QuotaController {

    private MSGPService.Iface msgpClient;
    private Object thriftClientProxy;
    private int QuotaThreadPoolSize;
    private int futureTimeout;
    private int checkInterval;
    private int pollingInterval;
    private String logAggregateURL;
    private String msgpURL;

    private final static Log logger = LogFactory.getLog(QuotaController.class);

    private CompletionService<WatchResult> completionService;

    private void init() {
        AtomicBoolean isMasterRunning = new AtomicBoolean(false);
        while (true) {
            try {
                if (Config.get("oswatch.master", "").equals(common.getLocalIp())) {
                    LOG.info("master");
                    if (!isMasterRunning.get()) {
                        //mtconfig
                        LOG.info("master 启动");
                        QuotaThreadPoolSize = Integer.parseInt(Config.get("QuotaThreadPoolSize", "10").trim());
                        checkInterval = Integer.parseInt(Config.get("checkInterval", "1").trim());
                        futureTimeout = Integer.parseInt(Config.get("futureTimeout", "1").trim());
                        if (Config.get("logAggregateURL", "") != "")
                            logAggregateURL = Config.get("logAggregateURL", "");
                        if (Config.get("msgpURL", "") != "") msgpURL = Config.get("msgpURL", "");

                        msgpClient = (MSGPService.Iface) this.thriftClientProxy;

                        completionService = new ExecutorCompletionService<WatchResult>(Executors.newFixedThreadPool(QuotaThreadPoolSize));

                        isMasterRunning.set(true);
                        QuotaMainTask mainTask = new QuotaMainTask(msgpClient, completionService, checkInterval, logAggregateURL, msgpURL, isMasterRunning);
                        Thread mainThread = new Thread(mainTask);
                        mainThread.start();
                    }
                } else {
                    LOG.info("slave");
                    isMasterRunning.set(false);
                }
                //等待“查询Master IP周期“
                pollingInterval = Integer.parseInt(Config.get("pollingInterval", "1").trim());
                TimeUnit.SECONDS.sleep(pollingInterval);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    public void notifyProviderQuota(List<ProviderQuota> quotaList) {
    }

    public int getQuotaThreadPoolSize() {
        return QuotaThreadPoolSize;
    }

    public void setQuotaThreadPoolSize(int quotaThreadPoolSize) {
        QuotaThreadPoolSize = quotaThreadPoolSize;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public int getFutureTimeout() {
        return futureTimeout;
    }

    public void setFutureTimeout(int futureTimeout) {
        this.futureTimeout = futureTimeout;
    }

    public String getLogAggregateURL() {
        return logAggregateURL;
    }

    public void setLogAggregateURL(String logAggregateURL) {
        this.logAggregateURL = logAggregateURL;
    }

    public String getMsgpURL() {
        return msgpURL;
    }

    public void setMsgpURL(String msgpURL) {
        this.msgpURL = msgpURL;
    }

    public void setThriftClientProxy(Object thriftClientProxy) {
        this.thriftClientProxy = thriftClientProxy;
    }
}
