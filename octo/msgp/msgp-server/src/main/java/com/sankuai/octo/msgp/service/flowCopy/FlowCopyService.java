package com.sankuai.octo.msgp.service.flowCopy;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.zebra.util.StringUtils;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.MccConfigItem;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyBrokerProcess;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyBrokerProcessData;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyBrokerProcessValue;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyBrokerResponse;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyConfig;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyPtestConfig;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyPtestResponse;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyPtestResult;
import com.sankuai.octo.msgp.model.flowCopy.FlowCopyTaskConfig;
import com.sankuai.octo.msgp.model.flowCopy.MccAPIResponse;
import com.sankuai.octo.msgp.model.flowCopy.MccNodeData;
import com.sankuai.octo.msgp.model.flowCopy.MccNodeDataItem;
import com.sankuai.octo.msgp.model.flowCopy.UnifiedFlowCopyConfig;
import com.sankuai.octo.msgp.serivce.service.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/21
 * Time: 18:21
 */
public class FlowCopyService {
    private static final Logger logger = LoggerFactory.getLogger(FlowCopyService.class);
    private static final String MCC_FLOWCOPY_ENABLE_KEY = "octo.provider.flowcopy.enable";
    private static final String MCC_FLOWCOPY_CONFIG_KEY = "octo.provider.flowcopy.config";
    private static final String MCC_FLOWCOPY_IPPORT_KEY = "octo.provider.flowcopy.ipport";
    private static final String MCC_FLOWCOPY_BROKERURL_KEY = "octo.provider.flowcopy.brokerUrl";
    private static final String MCC_FLOWCOPY_BROKERIPLIST_KEY = "vcr.broker";
    private static final String MCC_FLOWCOPY_NEW_CONFIG_KEY = "octo.provider.flowcopy";
    private static final String MCC_MODEL = "v2";
    private static final String MCC_SCAN_PATH = "com.sankuai.octo.msgp.service.vcr";
    private static final String FINISHED = "Finished";
    private static final String OK = "ok";
    private static final String ERROR = "error";
    private static final String SSOID_COOKIE_NAME = "ssoid";
    private static List<String> VCR_BROKER_IPLIST;

    static {
        final String brokerApi = MsgpConfig.get(MCC_FLOWCOPY_BROKERIPLIST_KEY, "");
        if (StringUtil.isBlank(brokerApi)) {
            logger.warn("brokerApi[{}] is not valid, set VCR_BROKER_IPLIST to [10.72.208.105]", brokerApi);
            VCR_BROKER_IPLIST = Collections.singletonList("10.72.208.105");
        } else {
            VCR_BROKER_IPLIST = Arrays.asList(brokerApi.split(","));
        }

        MsgpConfig.addListener(MCC_FLOWCOPY_BROKERIPLIST_KEY, new IConfigChangeListener() {
            @Override
            public void changed(String s, String oldValue, String newValue) {
                logger.info("config[{}] changed from {} to {}", MCC_FLOWCOPY_BROKERIPLIST_KEY, oldValue, newValue);
                if (StringUtil.isNotBlank(newValue)) {
                    VCR_BROKER_IPLIST = Arrays.asList(newValue.split(","));
                }
            }
        });

    }

    public static String queryRecordTaskPhase(Long taskId) {
        logger.info("[getRecordTaskPhase] begin, taskId:{}", taskId);
        Transaction catTransaction = Cat.newTransaction("FlowCopyService.getRecordTaskPhase", String.valueOf(taskId));
        FlowCopyPtestResponse flowCopyPtestResponse = new FlowCopyPtestResponse();
        try {
            String vcrBrokerUrl = getVcrBrokerUrl() + "fetch/status/" + taskId;
            String taskPhaseJson = HttpUtil.getResult(vcrBrokerUrl);
            if (StringUtil.isBlank(taskPhaseJson)) {
                throw new RuntimeException("获取任务数据失败, url: " + vcrBrokerUrl);
            }
            FlowCopyBrokerResponse flowCopyBrokerResponse = JsonHelper
                    .toObject(taskPhaseJson, FlowCopyBrokerResponse.class);
            if (OK.equals(flowCopyBrokerResponse.getStatus())) {
                FlowCopyPtestResult flowCopyPtestResult = new FlowCopyPtestResult();
                flowCopyPtestResult.setTaskId(taskId);
                flowCopyPtestResponse.setStatus(OK);

                if (flowCopyBrokerResponse.getData() == null) {
                    flowCopyPtestResult.setStatus("finished");
                } else {
                    String taskPhase = flowCopyBrokerResponse.getData().getTaskPhase();
                    if (Objects.equals(FINISHED, taskPhase)) {
                        flowCopyPtestResult.setStatus("finished");
                    } else {
                        flowCopyPtestResult.setStatus("unfinished");
                    }
                }
                flowCopyPtestResponse.setResult(flowCopyPtestResult);
            } else if (flowCopyBrokerResponse.getData() != null
                    && flowCopyBrokerResponse.getData().getError() != null) {
                flowCopyPtestResponse.setStatus(ERROR);
                flowCopyPtestResponse.setMessage(flowCopyBrokerResponse.getData().getError());
            } else {
                flowCopyPtestResponse.setStatus(ERROR);
                flowCopyPtestResponse.setMessage("unknown error");
            }

            Cat.logEvent("FlowCopyService.flowCopyPtestResponse", flowCopyPtestResponse.toString());

            catTransaction.setSuccessStatus();
        } catch (Exception e) {
            logger.error("[getRecordTaskPhase] fail", e);
            flowCopyPtestResponse.setStatus(ERROR);
            flowCopyPtestResponse.setMessage(e.getMessage());

            Cat.logEvent("FlowCopyService.error", e.getMessage());
            Cat.logEvent("FlowCopyService.", flowCopyPtestResponse.toString());
            catTransaction.setStatus(e);
        }

        catTransaction.complete();

        logger.info("[getRecordTaskPhase] taskId:{}, result:{}", taskId, flowCopyPtestResponse);
        return JsonHelper.jsonStr(flowCopyPtestResponse);
    }

    public static String ptestVcrStart(FlowCopyPtestConfig flowCopyPtestConfig, Cookie[] cookies) {
        String misId = flowCopyPtestConfig.getMisId();
        String appkey = flowCopyPtestConfig.getAppName();
        Transaction catTransaction = Cat.newTransaction("FlowCopyService.ptestVcrStart", appkey);
        Cat.logEvent("FlowCopyService.flowCopyPtestConfig", flowCopyPtestConfig.toString());
        Cat.logEvent("FlowCopyService.user", misId);
        try {
            logger.info("[ptestVcrStart] begin misId:{}, appkey:{}, flowCopyPtestConfig:{}", misId, appkey,
                    flowCopyPtestConfig);
            FlowCopyTaskConfig flowCopyTaskConfig = FlowCopyService.buildRecordTaskConfig(flowCopyPtestConfig);
            String recordTaskConfigJson = JsonHelper.jsonStr(flowCopyTaskConfig);

            Cat.logEvent("FlowCopyService.flowCopyTaskConfig", recordTaskConfigJson);

            String baseVcrBrokerUrl = getVcrBrokerUrl();
            String prepareVcrBrokerUrl = baseVcrBrokerUrl + "prepare";
            String vcrBrokerResponseString = HttpUtil.httpPostRequest(prepareVcrBrokerUrl, recordTaskConfigJson);
            if (StringUtil.isBlank(vcrBrokerResponseString)) {
                throw new RuntimeException(
                        "发送录制任务失败, url: " + prepareVcrBrokerUrl + ", data: " + recordTaskConfigJson);
            }
            FlowCopyBrokerResponse flowCopyBrokerResponse = JsonHelper
                    .toObject(vcrBrokerResponseString, FlowCopyBrokerResponse.class);
            if (!OK.equals(flowCopyBrokerResponse.getStatus()) || flowCopyBrokerResponse.getData() == null) {
                throw new RuntimeException(
                        "发送录制任务失败, url: " + prepareVcrBrokerUrl + ", data: " + recordTaskConfigJson + ", response: "
                                + flowCopyBrokerResponse);
            }

            if (flowCopyBrokerResponse.getData().getIp() == null
                    || flowCopyBrokerResponse.getData().getPort() == 0) {
                throw new RuntimeException(
                        "录制任务数据异常, url: " + prepareVcrBrokerUrl + ", data: " + recordTaskConfigJson + ", response: "
                                + flowCopyBrokerResponse);
            }

            String vcrIpPort =
                    flowCopyBrokerResponse.getData().getIp() + ":" + flowCopyBrokerResponse.getData().getPort();
            UnifiedFlowCopyConfig unifiedFlowCopyConfig = buildUniFiedFlowCopyConfig(baseVcrBrokerUrl, vcrIpPort,
                    flowCopyTaskConfig);

            String unifiedRecordTaskConfigStr = JsonHelper.jsonStr(unifiedFlowCopyConfig);

            logger.info("[ptestVcrStart] updateMccConfig: misId:{}, appkey:{}, key:{}, value:{}", misId, appkey,
                    MCC_FLOWCOPY_NEW_CONFIG_KEY, unifiedRecordTaskConfigStr);
            Transaction mccTransaction = Cat
                    .newTransaction("FlowCopyService.MccUpdate", flowCopyPtestConfig.toString());
            try {
                FlowCopyService.updateMccConfig(appkey, cookies,
                        new MccNodeDataItem(MCC_FLOWCOPY_NEW_CONFIG_KEY, unifiedRecordTaskConfigStr));
                mccTransaction.setSuccessStatus();
            } catch (Exception e) {
                mccTransaction.setStatus(e);
                throw new RuntimeException(
                        "mcc update failed, exception: " + e.getClass().getName() + ", message: " + e.getMessage());
            }
            mccTransaction.complete();

            FlowCopyPtestResponse flowCopyPtestResponse = FlowCopyService.buildVcrPtestResponse(flowCopyTaskConfig);
            Cat.logEvent("FlowCopyService.flowCopyPtestResponse", flowCopyPtestResponse.toString());
            return JsonHelper.jsonStr(flowCopyPtestResponse);
        } catch (NoSuchElementException e) {
            String msg = "获取用户失败";
            logger.info("[ptestVcrStart] fail misId:{}, appkey:{}, {}", misId, appkey, msg);
            Cat.logEvent("FlowCopyService.error", msg);
            catTransaction.setStatus(e);
            return errorJsonMessage(msg);
        } catch (Exception e) {
            logger.error(
                    "[ptestVcrStart] fail, misId:" + misId + ", appkey:" + appkey + ", param:" + flowCopyPtestConfig,
                    e);
            Cat.logEvent("FlowCopyService.error", e.getMessage());
            catTransaction.setStatus(e);
            return errorJsonMessage(e.getMessage());
        } finally {
            catTransaction.setSuccessStatus();
            catTransaction.complete();
        }
    }

    private static UnifiedFlowCopyConfig buildUniFiedFlowCopyConfig(String brokerUrl, String ipPort,
            FlowCopyTaskConfig flowCopyTaskConfig) {
        UnifiedFlowCopyConfig unifiedFlowCopyConfig = new UnifiedFlowCopyConfig();
        unifiedFlowCopyConfig.setBrokerUrl(brokerUrl);
        unifiedFlowCopyConfig.setIpport(ipPort);
        unifiedFlowCopyConfig.setCfgDetail(flowCopyTaskConfig.getRecordConfig());
        unifiedFlowCopyConfig.setTaskId(flowCopyTaskConfig.getTaskId());
        unifiedFlowCopyConfig.setEnable(true);
        return unifiedFlowCopyConfig;
    }

    private static FlowCopyPtestResponse buildVcrPtestResponse(FlowCopyTaskConfig flowCopyTaskConfig) {
        FlowCopyPtestResponse flowCopyPtestResponse = new FlowCopyPtestResponse();
        flowCopyPtestResponse.setStatus("success");
        FlowCopyPtestResult flowCopyPtestResult = new FlowCopyPtestResult();
        flowCopyPtestResult.setTaskId(flowCopyTaskConfig.getTaskId());
        flowCopyPtestResponse.setResult(flowCopyPtestResult);
        return flowCopyPtestResponse;
    }

    private static void updateMccConfig(String appkey, Cookie[] cookies, MccNodeDataItem... dataItems) {
        boolean ssoidExists = false;
        for (Cookie cookie : cookies) {
            if (SSOID_COOKIE_NAME.equals(cookie.getName())) {
                ssoidExists = true;
                break;
            }
        }

        Cat.logEvent("FlowCopyService.ssoidExists", String.valueOf(ssoidExists));
        if (ssoidExists) {
            updateMccWithAPI(appkey, cookies, dataItems);
        } else {
            updateMccWithClient(appkey, dataItems);
        }
    }

    private static void updateMccWithClient(String appkey, MccNodeDataItem[] items) {
        MtConfigClient mtConfigClient = new MtConfigClient();
        mtConfigClient.setAppkey(appkey);
        mtConfigClient.setId(appkey + System.nanoTime());
        mtConfigClient.setModel(MCC_MODEL);
        mtConfigClient.setScanBasePackage(MCC_SCAN_PATH);
        mtConfigClient.init();
        for (MccNodeDataItem item : items) {
            Cat.logEvent("FlowCopyService.MccUpdate." + item.getKey(), item.getValue());
            mtConfigClient.setValue(item.getKey(), item.getValue());
        }
        mtConfigClient.close();
    }

    private static void updateMccWithAPI(String appkey, Cookie[] cookies, MccNodeDataItem[] items) {
        String nodeName = getMccNodeName(appkey);
        String content = ServiceConfig.getNodeData(appkey, nodeName, cookies);
        if (content.isEmpty()) {
            throw new RuntimeException("get mcc data failed, probably the cookie[ssoid] is not presented");
        }
        MccAPIResponse node = JsonHelper.toObject(content, MccAPIResponse.class);

        if (!node.isSuccess()) {
            throw new RuntimeException("get mcc data failed, message: " + node.getMsg());
        }

        if (node.getData() == null) {
            throw new RuntimeException("get mcc data failed, data is null");
        }

        MccNodeData nodeData = node.getData();
        for (MccNodeDataItem item : items) {
            boolean insert = true;
            for (MccNodeDataItem originalDataItem : nodeData.getData()) {
                if (item.getKey().equals(originalDataItem.getKey())) {
                    originalDataItem.setValue(item.getValue());
                    originalDataItem.setComment(item.getComment());
                    originalDataItem.setOriComment(null);
                    originalDataItem.setOriValue(null);

                    insert = false;
                    break;
                }
            }

            if (insert) {
                nodeData.getData().add(item);
            }
        }

        MccConfigItem mccConfigItem = new MccConfigItem();
        mccConfigItem.setAppkey(appkey);
        mccConfigItem.setNodeName(nodeName);
        mccConfigItem.setSpaceName(appkey);

        mccConfigItem.setNodeData(JsonHelper.jsonStr(nodeData.getData()));
        mccConfigItem.setVersion(String.valueOf(nodeData.getVersion()));
        mccConfigItem.setRollback(false);
        String updateResponseStr = ServiceConfig.updateNodeData(appkey, mccConfigItem, cookies);
        MccAPIResponse updateResponse = JsonHelper.toObject(updateResponseStr, MccAPIResponse.class);
        if (!updateResponse.isSuccess()) {
            throw new RuntimeException(updateResponse.getMsg());
        }
    }

    private static String getMccNodeName(String appkey) {
        if (ProcessInfoUtil.isLocalHostOnline()) {
            return appkey + ".prod";
        } else {
            return appkey + ".test";
        }
    }

    private static FlowCopyTaskConfig buildRecordTaskConfig(FlowCopyPtestConfig flowCopyPtestConfig) {
        FlowCopyConfig flowCopyConfig = new FlowCopyConfig();
        flowCopyConfig.setServiceName(flowCopyPtestConfig.getServiceName());

        List<String> methodNameList = Arrays.asList(flowCopyPtestConfig.getMethodNames().split(","));
        flowCopyConfig.setMethodNames(methodNameList);

        List<String> serverIpList = Arrays.asList(flowCopyPtestConfig.getServerIps().split(","));
        flowCopyConfig.setServerIps(serverIpList);

        flowCopyConfig.setSavePath(flowCopyPtestConfig.getSavePath());
        flowCopyConfig.setTagged(true);
        flowCopyConfig.setSumCount(flowCopyPtestConfig.getSumCount());
        flowCopyConfig.setDescription(flowCopyPtestConfig.getDescription());

        FlowCopyTaskConfig flowCopyTaskConfig = new FlowCopyTaskConfig();
        flowCopyTaskConfig.setTaskId(FlowCopyService.genUUID());
        flowCopyTaskConfig.setRecordConfig(flowCopyConfig);

        return flowCopyTaskConfig;
    }

    private static long genUUID() {
        UUID uuid = UUID.randomUUID();
        return Math.abs(uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits());
    }

    private static String errorJsonMessage(String message) {
        FlowCopyPtestResponse flowCopyPtestResponse = new FlowCopyPtestResponse();
        flowCopyPtestResponse.setStatus(ERROR);
        flowCopyPtestResponse.setMessage(message);
        Cat.logEvent("FlowCopyService.error", message);
        return JsonHelper.jsonStr(flowCopyPtestResponse);
    }

    private static String getVcrBrokerUrl() {
        int index = new Random().nextInt(VCR_BROKER_IPLIST.size());
        return "http://" + VCR_BROKER_IPLIST.get(index) + ":8080/api/record/";
    }

    private static String getVcrRootBrokerUrl() {
        int index = new Random().nextInt(VCR_BROKER_IPLIST.size());
        return "http://" + VCR_BROKER_IPLIST.get(index) + ":8080/";
    }

    public static String closeRecordTask(Long taskId) {
        logger.info("[closeRecordTask] begin, taskId:{}", taskId);
        Transaction catTransaction = Cat.newTransaction("FlowCopyService.closeRecordTask", String.valueOf(taskId));
        FlowCopyPtestResponse flowCopyPtestResponse = new FlowCopyPtestResponse();
        try {
            String vcrBrokerUrl = getVcrBrokerUrl() + "close/" + taskId;
            String taskPhaseJson = HttpUtil.getResult(vcrBrokerUrl);
            if (StringUtil.isBlank(taskPhaseJson)) {
                throw new RuntimeException("关闭任务失败, url: " + vcrBrokerUrl);
            }
            FlowCopyBrokerResponse flowCopyBrokerResponse = JsonHelper
                    .toObject(taskPhaseJson, FlowCopyBrokerResponse.class);
            if (OK.equals(flowCopyBrokerResponse.getStatus())) {
                FlowCopyPtestResult flowCopyPtestResult = new FlowCopyPtestResult();
                flowCopyPtestResult.setTaskId(taskId);
                flowCopyPtestResponse.setStatus(OK);

                flowCopyPtestResponse.setResult(flowCopyPtestResult);
            } else if (flowCopyBrokerResponse.getData() != null
                    && flowCopyBrokerResponse.getData().getError() != null) {
                flowCopyPtestResponse.setStatus(ERROR);
                flowCopyPtestResponse.setMessage(flowCopyBrokerResponse.getData().getError());
            } else {
                flowCopyPtestResponse.setStatus(ERROR);
                flowCopyPtestResponse.setMessage("unknown error");
            }

            Cat.logEvent("FlowCopyService.flowCopyPtestResponse", flowCopyPtestResponse.toString());
            catTransaction.setSuccessStatus();
        } catch (Exception e) {
            logger.error("[closeRecordTask] fail", e);
            flowCopyPtestResponse.setStatus(ERROR);
            flowCopyPtestResponse.setMessage(e.getMessage());

            Cat.logEvent("FlowCopyService.error", e.getMessage());
            Cat.logEvent("FlowCopyService.flowCopyPtestResponse", flowCopyPtestResponse.toString());
            catTransaction.setStatus(e);
        }
        catTransaction.complete();
        logger.info("[closeRecordTask] taskId:{}, result:{}", taskId, flowCopyPtestResponse);
        return JsonHelper.jsonStr(flowCopyPtestResponse);
    }

    public static String getRecordTaskProcess(Long taskId) {
        logger.info("[getRecordTaskProcess], taskId={}", taskId);
        Transaction catTransaction = Cat.newTransaction("FlowCopyService.getRecordTaskProcess", String.valueOf(taskId));
        FlowCopyPtestResponse flowCopyPtestResponse = new FlowCopyPtestResponse();
        try {
            String vcrBrokerUrl = getVcrRootBrokerUrl() + "monitor/record/list";
            String taskProcessJson = HttpUtil.getResult(vcrBrokerUrl);
            if (StringUtils.isBlank(taskProcessJson)) {
                throw new RuntimeException("查询进度失败，url: " + vcrBrokerUrl);
            }
            FlowCopyBrokerProcess flowCopyBrokerProcess = JsonHelper
                    .toObject(taskProcessJson, FlowCopyBrokerProcess.class);
            if (OK.equals(flowCopyBrokerProcess.getStatus()) && flowCopyBrokerProcess.getData() != null) {
                for (FlowCopyBrokerProcessData data : flowCopyBrokerProcess.getData()) {
                    FlowCopyBrokerProcessValue value = data.getValue();
                    if (value == null) {
                        continue;
                    }
                    if (String.valueOf(taskId).equals(value.getName())) {
                        flowCopyPtestResponse.setProcess(value);
                        break;
                    }
                }
            }
            flowCopyPtestResponse.setStatus(OK);
            if (flowCopyPtestResponse.getProcess() == null) {
                flowCopyPtestResponse.setStatus(ERROR);
                flowCopyPtestResponse.setMessage("can not find task process for taskId " + taskId);
            }

            Cat.logEvent("FlowCopyService.flowCopyPtestResponse", flowCopyPtestResponse.toString());
            catTransaction.setSuccessStatus();
        } catch (Exception e) {
            logger.error("[getRecordTaskProcess] fail", e);
            flowCopyPtestResponse.setStatus(ERROR);
            flowCopyPtestResponse.setMessage(e.getMessage());

            Cat.logEvent("FlowCopyService.error", e.getMessage());
            Cat.logEvent("FlowCopyService.flowCopyPtestResponse", flowCopyPtestResponse.toString());
            catTransaction.setStatus(e);
        }

        logger.info("[getRecordTaskProcess] taskId:{}, result:{}", taskId, flowCopyPtestResponse);
        catTransaction.complete();
        return JsonHelper.jsonStr(flowCopyPtestResponse);
    }
}
