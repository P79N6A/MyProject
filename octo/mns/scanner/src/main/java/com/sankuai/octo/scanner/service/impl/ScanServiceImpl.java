package com.sankuai.octo.scanner.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.zkclient.MtZookeeperClient;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.octo.mnsc.idl.thrift.model.HttpPropertiesResponse;
import com.sankuai.octo.mnsc.idl.thrift.model.MNSBatchResponse;
import com.sankuai.octo.mnsc.idl.thrift.model.MNSResponse;
import com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService;
import com.sankuai.octo.scanner.Common;
import com.sankuai.octo.scanner.falcon.FalconItem;
import com.sankuai.octo.scanner.falcon.Item;
import com.sankuai.octo.scanner.falcon.ReportUtils;
import com.sankuai.octo.scanner.model.report.RoundTimeReport;
import com.sankuai.octo.scanner.service.*;
import com.sankuai.octo.scanner.util.ScanUtils;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service("scanService")
public class ScanServiceImpl {
    private final static Logger logger = LoggerFactory.getLogger(ScanServiceImpl.class);
    private String baseDir;
    private String zkUrl;

    private MtZookeeperClient zkClient;
    private static AtomicInteger scanRoundCounter = new AtomicInteger(0);
    private static AtomicInteger providerCount = new AtomicInteger(0);
    private static Map<String, HashSet<String>> ipPortMap = new ConcurrentHashMap<String, HashSet<String>>();

    private MNSCacheService.Iface mnsCacheClient = MNSCacheClient.getInstance();

    private static volatile List<String> excludedAppkeysList = new ArrayList<String>();
    private static volatile List<String> hlbUserDefinedScanAppkeysList = new ArrayList<String>();
    public static volatile boolean appCheck = false;
    public static volatile boolean newCloudCheck = false;
    public static volatile int rebootTimeoutNum = 3;
    public static volatile int zkUpdateFrequency = 1;
    public static volatile boolean slowStart = true;
    public static volatile boolean emergencySwitch = false;
    public static volatile boolean fusing = false;
    public static volatile boolean userDefinedHttpCheck = true;
    public static volatile boolean scanSgAgent = true;

    public static double FUSINGNUM = 98.0;

    private static volatile int BTACH_SIZE = 3;
    private static final List<String> ENV_LIST = new ArrayList<>();

    private static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1, new ScheduleTaskFactory("Falcon-Schedule"));

    static {
        ENV_LIST.add("prod");
        ENV_LIST.add("stage");
        ENV_LIST.add("test");

        scheduExec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (LeaderElection.isMaster) {
                        double rate = 100.0;
                        if (FalconItem.providerTotalNum.get() != 0)
                            rate = (FalconItem.providerTotalNum.get() - FalconItem.providerFailNum.get()) / (double) FalconItem.providerTotalNum.get() * 100;
                        List<Item> list = new ArrayList<Item>();
                        list.add(new Item("scanner.provider.successRate", String.valueOf(rate), System.currentTimeMillis()));
                        list.add(new Item("scanner.provider.totalNum", String.valueOf(FalconItem.providerTotalNum.get()), System.currentTimeMillis()));
                        list.add(new Item("scanner.provider.failNum", String.valueOf(FalconItem.providerFailNum.get()), System.currentTimeMillis()));
                        ReportUtils.doIOWrite(list, 60);

                        if (Double.compare(rate, FUSINGNUM) < 0)
                            fusing = true;

                        if (fusing && Double.compare(rate, 100.0) == 0)
                            fusing = false;

                        FalconItem.providerTotalNum = new AtomicInteger(0);
                        FalconItem.providerFailNum = new AtomicInteger(0);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void initZkConn() {

        if (StringUtils.isEmpty(zkUrl)) {
            return;
        }
        if (zkClient == null) {
            synchronized (ScanServiceImpl.class) {
                if (zkClient == null) {
                    zkClient = new MtZookeeperClient(zkUrl, 30000, true);
                    Common.zkClient = zkClient;
                }
            }
        }
        LeaderElection.init();
    }

    public void scanZK() {
        if (LeaderElection.isMaster) {
            ipPortMap.clear();
            providerCount.set(0);
            scanRoundCounter.getAndAdd(1);

            long start = System.currentTimeMillis();
            List<String> appkeys = new ArrayList<>();
            try {
                getScannerConfig();
                //以prod环境的appkey为准
                appkeys = zkClient.getChildren(baseDir + "/prod");
                scanByAppkey(appkeys);
            } catch (Throwable e) {
                logger.error("scanByAppkey Throwable:" + e.getMessage(), e);
            }
            long roundTime = (System.currentTimeMillis() - start);
            logger.info("scanRoundCounter:" + scanRoundCounter.get() + " scanRoundtime:" + roundTime + " scanAppkeys:" + appkeys.size());
            JMonitor.add("scanner.roundtime", roundTime);
            SendReport.send(new RoundTimeReport(0, "RoundTime", "seconds", (int) (roundTime + 500) / 1000, Common.localIp));
        }
    }

    public void scanByAppkey(List<String> appkeys) {

        List<String> validAppkeys = new ArrayList<>();
        if (appkeys != null && appkeys.size() > 0) {
            for (String appkey : appkeys) {

                logger.info("scanRoundCounter:" + scanRoundCounter.get() + " scan appkey:" + appkey);

                if (StringUtils.isBlank(appkey))
                    continue;
                // sg_agent 暂不处理; 后续用以单独队列扫描
                if (appkey.trim().equalsIgnoreCase(Common.agentAppkey))
                    continue;

                // kms_agent 暂不处理; 后续用以单独队列扫描
                if (appkey.trim().equalsIgnoreCase(Common.kmsAgentAppkey))
                    continue;

                // 根据配置判断该appkey是否扫描
                if (excludedAppkeysList != null && excludedAppkeysList.contains(appkey)) {
                    logger.info("scanRoundCounter:" + scanRoundCounter.get() + " skip appkey:" + appkey);
                    continue;
                }
                validAppkeys.add(appkey);
            }

            for (int i = 0; i < validAppkeys.size(); i += BTACH_SIZE) {
                int toIndex;
                if (i + BTACH_SIZE > validAppkeys.size())
                    toIndex = validAppkeys.size();
                else
                    toIndex = i + BTACH_SIZE;
                List<String> subAppkeyList = validAppkeys.subList(i, toIndex);

                handlerThriftServerlist(subAppkeyList);
                handlerHttpServerlist(subAppkeyList);
            }
        }
    }

    public void handlerHttpServerlist(final List<String> subAppkeyList) {
        MNSBatchResponse httpResponse = null;
        try {
            httpResponse = mnsCacheClient.getMNSCacheByAppkeys(subAppkeyList, "http");
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }

        if (httpResponse != null && httpResponse.getCode() == 200 && httpResponse.getCache() != null) {
            Map<String, Map<String, List<SGService>>> map = httpResponse.getCache();
            String appkey;
            String env;
            List<String> providers;
            for (Map.Entry<String, Map<String, List<SGService>>> appkeyEntry : map.entrySet()) {

                appkey = appkeyEntry.getKey();

                for (Map.Entry<String, List<SGService>> envEntry : appkeyEntry.getValue().entrySet()) {
                    env = envEntry.getKey();
                    providers = new ArrayList<>();
                    List<SGService> sgServiceList = envEntry.getValue();
                    for (SGService sgService : sgServiceList) {
                        providers.add(sgService.getIp() + Common.colon + sgService.getPort());
                    }
                    List<String> validProviders = filterHttpServers(env, appkey, providers);
                    sendHttpTask(env, appkey, validProviders);
                }
            }

        } else {

            for (String appkey : subAppkeyList) {
                for (String env : ENV_LIST) {
                    String providersDir = baseDir + "/" + env + "/" + appkey + "/provider-http";
                    List<String> providers = getProviderNameListByZK(providersDir);
                    List<String> validProviders = filterHttpServers(env, appkey, providers);
                    sendHttpTask(env, appkey, validProviders);
                }
            }
        }
    }

    public void handlerThriftServerlist(final List<String> subAppkeyList) {
        MNSBatchResponse thriftResponse = null;
        try {
            thriftResponse = mnsCacheClient.getMNSCacheByAppkeys(subAppkeyList, "thrift");
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }

        if (thriftResponse != null && thriftResponse.getCode() == 200 && thriftResponse.getCache() != null) {
            Map<String, Map<String, List<SGService>>> map = thriftResponse.getCache();
            String appkey;
            String env;
            List<String> providers;
            for (Map.Entry<String, Map<String, List<SGService>>> appkeyEntry : map.entrySet()) {

                appkey = appkeyEntry.getKey();

                for (Map.Entry<String, List<SGService>> envEntry : appkeyEntry.getValue().entrySet()) {
                    env = envEntry.getKey();
                    providers = new ArrayList<>();
                    List<SGService> sgServiceList = envEntry.getValue();
                    for (SGService sgService : sgServiceList) {
                        providers.add(sgService.getIp() + Common.colon + sgService.getPort());
                    }
                    List<String> validProviders = filterMtthriftServers(env, appkey, providers);
                    sendThriftTask(env, appkey, validProviders);
                }
            }

        } else {

            for (String appkey : subAppkeyList) {
                for (String env : ENV_LIST) {
                    String providersDir = baseDir + "/" + env + "/" + appkey + "/provider";
                    List<String> providers = getProviderNameListByZK(providersDir);
                    List<String> validProviders = filterMtthriftServers(env, appkey, providers);
                    sendThriftTask(env, appkey, validProviders);
                }
            }
        }
    }

    public void sendThriftTask(final String env, final String appkey, final List<String> providers) {
        if (providers == null || providers.size() <= 0)
            return;
        DetectorServiceClient.check(env, appkey, "provider", providers, scanRoundCounter);
    }

    public void sendHttpTask(final String env, final String appkey, final List<String> providers) {
        if (providers == null || providers.size() <= 0)
            return;
        HttpPropertiesResponse response = null;
        try {
            response = mnsCacheClient.getHttpPropertiesByAppkey(appkey, env);
        } catch (TException e) {
            logger.error(e.getMessage());
        }
        String type = "tcp";
        String checkUrl = "";
        if (response != null) {
            Map<String, Map<String, String>> propertiesMap = response.getPropertiesMap();
            if (propertiesMap != null) {
                Map<String, String> map = propertiesMap.get(appkey);
                if (map != null && map.size() > 0) {
                    type = map.get("centra_check_type");
                    checkUrl = map.get("centra_http_send");
                }
            }
        }

        if (userDefinedHttpCheck && type != null && type.equals("http") && checkUrl != null && !checkUrl.trim().equals("")) {
            DetectorServiceClient.userDefinedHttpCheck(env, appkey, "provider-http", providers, checkUrl, scanRoundCounter);
        } else if (!userDefinedHttpCheck && hlbUserDefinedScanAppkeysList.contains(appkey) &&
                type != null && type.equals("http") && checkUrl != null && !checkUrl.trim().equals("")) {
            DetectorServiceClient.userDefinedHttpCheck(env, appkey, "provider-http", providers, checkUrl, scanRoundCounter);
        } else {
            DetectorServiceClient.check(env, appkey, "provider-http", providers, scanRoundCounter);
        }

    }

    private List<String> filterHttpServers(String env, String appkey, List<String> providerNameList) {
        List<String> providers = new ArrayList<>();
        if (null == providerNameList || 0 == providerNameList.size())
            return providers;

        for (String providerName : providerNameList) {

            if (Common.isOnline) {
                if (!ScanUtils.isSameDC(providerName)) {
                    String ipPrefix = ScanUtils.getProviderIpPrefix(providerName);
                    boolean isBeijingIp = Common.beijingIpPrefixSet.contains(ipPrefix) ? true : false;
                    if (isBeijingIp && ScanUtils.hostIpPrefix.startsWith(Common.cqIpPrefix)) {
                        //cq机房的Scanner扫描北京侧除dx和yf之外的其他所有机房
                        if (providerName.startsWith(Common.yfIpPrefix) || providerName.startsWith(Common.dxIpPrefix)) {
                            continue;
                        }
                    } else if (!isBeijingIp && ScanUtils.hostIpPrefix.startsWith(Common.gqIpPrefix)) {
                        //gq机房的Scanner扫描上海侧所有机房
                    } else {
                        continue;
                    }
                }
            }

            // 检查 providerName 格式
            if (!providerName.matches("\\d+.\\d+.\\d+.\\d+:\\d+")) {
                String providerPath = baseDir + "/" + env + "/" + appkey + "/provider-http/" + providerName;
                logger.error("wrong server node format:" + providerPath);
                continue;
            }
            providers.add(providerName);
            // 检查重复注册
            String identifierString = env + Common.vbar + appkey + Common.vbar + providerName;
            ScannerTasks.checkDuplicateRegistry(ipPortMap, identifierString, providerName);

            providerCount.addAndGet(1);

        }
        return providers;
    }

    public List<String> filterMtthriftServers(String env, String appkey, List<String> providerNameList) {

        List<String> providers = new ArrayList<>();

        if (null == providerNameList || 0 == providerNameList.size())
            return providers;

        for (String providerName : providerNameList) {

            if (Common.isOnline) {
                if (!ScanUtils.isSameDC(providerName)) {
                    String ipPrefix = ScanUtils.getProviderIpPrefix(providerName);
                    boolean isBeijingIp = Common.beijingIpPrefixSet.contains(ipPrefix) ? true : false;
                    if (isBeijingIp && ScanUtils.hostIpPrefix.startsWith(Common.cqIpPrefix)) {
                        //cq机房的Scanner扫描北京侧除dx和yf之外的其他所有机房
                        if (providerName.startsWith(Common.yfIpPrefix) || providerName.startsWith(Common.dxIpPrefix)) {
                            continue;
                        }
                    } else if (!isBeijingIp && ScanUtils.hostIpPrefix.startsWith(Common.gqIpPrefix)) {
                        //gq机房的Scanner扫描上海侧所有机房
                    } else {
                        continue;
                    }
                }
            }

            // 检查 providerName 格式
            if (!providerName.matches("\\d+.\\d+.\\d+.\\d+:\\d+")) {
                String providerPath = baseDir + "/" + env + "/" + appkey + "/provider/" + providerName;
                logger.error("wrong server node format:" + providerPath);
                continue;
            }

            // 检查重复注册
            String identifierString = env + Common.vbar + appkey + Common.vbar + providerName;
            ScannerTasks.checkDuplicateRegistry(ipPortMap, identifierString, providerName);
            providerCount.addAndGet(1);
            providers.add(providerName);
        }
        return providers;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getZkUrl() {
        return zkUrl;
    }

    public void setZkUrl(String zkUrl) {
        this.zkUrl = zkUrl;
        if (Common.isOnline && ScanUtils.hostIpPrefix.startsWith(Common.gqIpPrefix)) {
            this.zkUrl = Common.gqZkObserverAddress;
        }
        initZkConn();
    }

    public void setMsgpHost(String msgpHost) {
        Common.msgpHost = msgpHost;
    }

    public void setAllowUpdateZKData(boolean allowUpdateZKData) {
        Common.allowUpdateZKData = allowUpdateZKData;
    }

    public void destroy() {
        int sleepSeconds = 15;
        logger.info("sleep " + sleepSeconds + " seconds.");
        try {
            Thread.sleep(sleepSeconds * 1000L);
        } catch (InterruptedException e) {
            logger.error("exception while waitting " + sleepSeconds + " seconds to stop scanner", e);
        }
    }


    public static void getScannerConfig() {
        excludedAppkeysList.clear();
        hlbUserDefinedScanAppkeysList.clear();
        String result = null;
        try {
            result = MnsInvoker.getConfig(Common.appkey);
        } catch (TException e) {
            logger.error("exception while MnsInvoker.getConfig by appkey:" + Common.appkey, e);
        }
        //result: {"ret":0,"msg":"success","data":{"excludedAppkeys":"appkey1,appkey2,appkey3"},"version":"1181116538095"}
        JSONObject jo = JSON.parseObject(result);
        if (jo != null && jo.getJSONObject("data") != null) {
            String excludedAppkeys = (String) jo.getJSONObject("data").get("excludedAppkeys");
            if (excludedAppkeys != null) {
                String strs[] = excludedAppkeys.split(",");
                for (String str : strs) {
                    excludedAppkeysList.add(str);
                }
            }

            String hlbUserDefinedScanAppkeys = (String) jo.getJSONObject("data").get("hlbUserDefinedScanAppkeys");
            if (hlbUserDefinedScanAppkeys != null) {
                String strs[] = hlbUserDefinedScanAppkeys.split(",");
                for (String str : strs) {
                    hlbUserDefinedScanAppkeysList.add(str);
                }
            }

            String appCheckStr = (String) jo.getJSONObject("data").get("appCheck");
            if (appCheckStr != null && appCheckStr.equals("true"))
                appCheck = true;
            else
                appCheck = false;

            String newCloudCheckStr = (String) jo.getJSONObject("data").get("newCloudCheck");
            if (newCloudCheckStr != null && newCloudCheckStr.equals("true"))
                newCloudCheck = true;
            else
                newCloudCheck = false;

            String rebootTimeoutNumStr = (String) jo.getJSONObject("data").get("rebootTimeoutNum");
            if (rebootTimeoutNumStr != null)
                rebootTimeoutNum = Integer.valueOf(rebootTimeoutNumStr);
            else
                rebootTimeoutNum = 3;

            String zkUpdateFrequencyStr = (String) jo.getJSONObject("data").get("zkUpdateFrequency");
            if (zkUpdateFrequencyStr != null)
                zkUpdateFrequency = Integer.valueOf(zkUpdateFrequencyStr);
            else
                zkUpdateFrequency = 1;

            String slowStartStr = (String) jo.getJSONObject("data").get("slowStart");
            if (slowStartStr != null && slowStartStr.equals("true"))
                slowStart = true;
            else if (slowStartStr != null && slowStartStr.equals("false"))
                slowStart = false;

            String emergencySwitchStr = (String) jo.getJSONObject("data").get("emergencySwitch");
            if (emergencySwitchStr != null && emergencySwitchStr.equals("true"))
                emergencySwitch = true;
            else if (emergencySwitchStr != null && emergencySwitchStr.equals("false"))
                emergencySwitch = false;

            String userDefinedHttpCheckStr = (String) jo.getJSONObject("data").get("userDefinedHttpCheck");
            if (userDefinedHttpCheckStr != null && userDefinedHttpCheckStr.equals("false"))
                userDefinedHttpCheck = false;
            else
                userDefinedHttpCheck = true;

            String scanSgAgentStr = (String) jo.getJSONObject("data").get("scanSgAgent");
            if (scanSgAgentStr != null && scanSgAgentStr.equals("false"))
                scanSgAgent = false;
            else
                scanSgAgent = true;

            String batchSize = (String) jo.getJSONObject("data").get("MNSCBatchSize");
            if (batchSize != null)
                BTACH_SIZE = Integer.valueOf(batchSize);
            else
                BTACH_SIZE = 3;


        } else {
            appCheck = false;
            newCloudCheck = true;
            slowStart = true;
            emergencySwitch = false;
            userDefinedHttpCheck = true;
            scanSgAgent = true;
        }

    }

    private List<String> getHttpProviderNameList(String appkey, String env, String pathAppkey) {
        String providersDir = pathAppkey + "/provider-http";
        int counter = scanRoundCounter.get();

        if (zkUpdateFrequency == 0 || (counter % zkUpdateFrequency) != 0)
            return getHttpProviderNameListByMNSC(providersDir, appkey, env);
        else
            return getHttpProviderNameListByZK(providersDir);
    }

    private List<String> getHttpProviderNameListByZK(String providersDir) {
        List<String> providerNameList;
        providerNameList = zkClient.getChildren(providersDir);
        return providerNameList;
    }


    private List<String> getHttpProviderNameListByMNSC(String providersDir, String appkey, String env) {
        List<String> providerNameList = new ArrayList<String>();
        List<SGService> sgServiceList;
        MNSResponse mnsResponse = null;
        try {
            String version = "";
            mnsResponse = mnsCacheClient.getMNSCache4HLB(appkey, version, env);
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }
        if (mnsResponse != null && mnsResponse.getCode() == 200 && mnsResponse.getDefaultMNSCache() != null) {
            sgServiceList = mnsResponse.getDefaultMNSCache();
            for (SGService sgService : sgServiceList) {
                providerNameList.add(sgService.getIp() + Common.colon + sgService.getPort());
            }
            return providerNameList;
        }
        return getHttpProviderNameListByZK(providersDir);
    }

    public List<String> getProviderNameList(String appkey, String env, String pathAppkey) {

        String providersDir = pathAppkey + "/provider";
        int counter = scanRoundCounter.get();

        if (zkUpdateFrequency == 0 || (counter % zkUpdateFrequency) != 0)
            return getProviderNameListByMNSC(providersDir, appkey, env);
        else
            return getProviderNameListByZK(providersDir);

    }


    private List<String> getProviderNameListByZK(String providersDir) {
        List<String> providerNameList;
        providerNameList = zkClient.getChildren(providersDir);
        return providerNameList;
    }

    private List<String> getProviderNameListByMNSC(String providersDir, String appkey, String env) {
        List<String> providerNameList = new ArrayList<String>();
        List<SGService> sgServiceList;
        MNSResponse mnsResponse = null;
        try {
            String version = "";
            mnsResponse = mnsCacheClient.getMNSCache(appkey, version, env);
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }
        if (mnsResponse != null && mnsResponse.getCode() == 200 && mnsResponse.getDefaultMNSCache() != null) {
            sgServiceList = mnsResponse.getDefaultMNSCache();
            for (SGService sgService : sgServiceList) {
                providerNameList.add(sgService.getIp() + Common.colon + sgService.getPort());
            }
            return providerNameList;
        }
        return getProviderNameListByZK(providersDir);
    }

}
