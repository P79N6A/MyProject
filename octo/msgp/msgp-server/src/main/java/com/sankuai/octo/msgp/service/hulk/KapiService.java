package com.sankuai.octo.msgp.service.hulk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.inf.utils.cache.CellarCache;
import com.sankuai.inf.utils.cache.CellarCacheFactory;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.msgp.common.model.EntityType;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyConfig;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.taobao.tair3.client.error.TairException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import scala.collection.immutable.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by songjianjian on 2018/8/1.
 */
@Service
public class KapiService {

    private static final Logger LOG = LoggerFactory.getLogger(KapiService.class);

    private static final String REMOTE_APPKEY;
    private static final short AREA;
    private static CellarCache cache;
    private static final MtConfigClient manuConfigMccClient = MccHulkService.getMtConfigClient();
    private static final MtConfigClient manuConfigMccClientBannerApi = MccHulkService.getMtConfigClientBannerApi();

    static {
        if (ProcessInfoUtil.isLocalHostOnline()) {
            REMOTE_APPKEY = PolicyConfig.REMOTE_APPKEY_ONLINE;
        } else {
            REMOTE_APPKEY = PolicyConfig.REMOTE_APPKEY_OFFLINE;
        }

        switch (ProcessInfoUtil.getHostEnv()) {
            case PROD:
            case STAGING:
                AREA = 14;
                break;
            case TEST:
                AREA = 793;
                break;
            case DEV:
                AREA = 125;
                break;
            case PPE:
                AREA = 124;
                break;
            default:
                throw new IllegalStateException("env类型出错");
        }

        try {
            cache = CellarCacheFactory.newCache(PolicyConfig.LOCAL_APPKEY, REMOTE_APPKEY, AREA);
        } catch (TairException e) {
            LOG.error("create cellarCache error " + e);
        }
    }

    public static CellarCache getCellarClient() {
        return cache;
    }

    public String checkIsAccepted(String appkey) {
        try {
            String url = PolicyConfig.APPKEY_BELONG_SRV_URL + appkey + "/srvs";
            String appkeyBelongSrvResult = HttpService.getRequestToOps(url);
            JSONObject appkeySrvJson = JSON.parseObject(appkeyBelongSrvResult);
            LOG.info("appkey {} belong srv json {}", appkey, appkeyBelongSrvResult);
            if (appkeySrvJson == null || appkeySrvJson.isEmpty()) {
                return AccessResponse(400, "暂未开放该业务线接入");
            }
            JSONArray appkeySrvJsonArray = JSON.parseArray(appkeySrvJson.getString("srvs"));
            if (appkeySrvJsonArray == null ||appkeySrvJsonArray.isEmpty()) {
                return AccessResponse(400, "暂未开放该业务线接入");
            }
            String srv = appkeySrvJsonArray.getJSONObject(0).getString("key");
            LOG.info("appkey {} srv is {}", appkey, srv);
            if (srv == null || srv.isEmpty()) {
                return AccessResponse(400, "暂未开放该业务线接入");
            }
            String urlOwt = PolicyConfig.SRV_BELONG_OWT_URL + srv;
            String srvBelongOwtResult = HttpService.getRequestToOps(urlOwt);
            JSONObject srvOwtJson = JSON.parseObject(srvBelongOwtResult);
            if (srvOwtJson == null || srvOwtJson.isEmpty()) {
                return AccessResponse(400, "暂未开放该业务线接入");
            }
            String owt = JSON.parseObject(srvOwtJson.getString("owt")).getString("key");
            LOG.info("appkey {} owt is {}", appkey, owt);
            if (owt == null || owt.isEmpty()) {
                return AccessResponse(400, "暂未开放该业务线接入");
            }
            String allowOwt = manuConfigMccClient.getValue("hulk2_admittance_owt");
            String allowAppkey = manuConfigMccClientBannerApi.getValue("hulk2_admittance_appkey");
            boolean appkeyAllow = false;
            if(allowAppkey == null || allowAppkey.equals("")) {
                appkeyAllow = true;
            } else if(allowAppkey.contains(",")) {
                if(Arrays.asList(allowAppkey.split(",")).contains(appkey)) {
                    appkeyAllow = true;
                }
            } else {
                if(allowAppkey.equals(appkey)) {
                    appkeyAllow = true;
                }
            }
            LOG.info("allow appkey {}", allowAppkey);
            if (allowOwt == null || allowOwt.contains(owt) || appkeyAllow) {
                return AccessResponse(200, "");
            }
            return AccessResponse(400, "暂未开放该业务线接入");
        } catch (Exception e) {
            LOG.error("checkAccess error ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? AccessResponse(400, "解析错误") : e.getMessage());
        }
    }

    public String checkIsOwner(String appkey) {
        try {
            String superManager = manuConfigMccClient.getValue("hulk2_super_manager");
            LOG.info("super manager {}", superManager);
            JSONObject jsonObject = new JSONObject();
            if (ServiceCommon.isOwnerLogin(appkey, UserUtils.getUser().getLogin()) || superManager.contains(UserUtils.getUser().getLogin())) {
                jsonObject.put("code", 200);
                jsonObject.put("errorInfo", "existed");
                return jsonObject.toJSONString();
            }
            return AccessResponse(400, "非业务负责人");
        } catch (Exception e) {
            LOG.error("check owner ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? AccessResponse(400, "解析错误") : e.getMessage());
        }
    }

    public String checkIsConf(String appkey, String env) {
        try {
            String url = PolicyConfig.APPKEY_BELONG_SRV_URL + appkey + "/srvs";
            String appkeyBelongSrvResult = HttpService.getRequestToOps(url);
            JSONObject appkeySrvJson = JSON.parseObject(appkeyBelongSrvResult);
            LOG.info("appkey {} belong srv json {}", appkey, appkeyBelongSrvResult);
            if (appkeySrvJson == null || appkeySrvJson.isEmpty()) {
                return AccessResponse(400, "srv result is null");
            }
            JSONArray appkeySrvJsonArray = JSON.parseArray(appkeySrvJson.getString("srvs"));
            if (appkeySrvJsonArray == null ||appkeySrvJsonArray.isEmpty()) {
                return AccessResponse(400, "srv array is null");
            }
            LOG.info("jsonArray is {}", appkeyBelongSrvResult);
            JSONObject srvJson = appkeySrvJsonArray.getJSONObject(0);
            if (srvJson == null ||srvJson.isEmpty()) {
                return AccessResponse(400, "srv item is null");
            }
            String scaleConfig = srvJson.getString("scale_config");
            if(StringUtils.isBlank(scaleConfig)) {
                return AccessResponse(400, "scale_config is blank");
            }
            JSONObject confJson = JSON.parseObject(scaleConfig);
            if(confJson.isEmpty()) {
                return AccessResponse(400, "conf of App is blank");
            }
            String targetEnvConf = confJson.getString(env);
            if(confJson.isEmpty()) {
                return AccessResponse(400, "target env conf is null");
            }
            JSONObject targetEnvConfJson = JSON.parseObject(targetEnvConf);
            if(confJson.isEmpty()) {
                return AccessResponse(400, "target env conf json is null");
            }
            String allowAppkey = manuConfigMccClientBannerApi.getValue("hulk2_admittance_appkey");
            LOG.info("allow appkey {}", allowAppkey);
            boolean appkeyAllow = false;
            if(allowAppkey == null || allowAppkey.equals("")) {
                appkeyAllow = true;
            }else if(allowAppkey.contains(",")) {
                if(Arrays.asList(allowAppkey.split(",")).contains(appkey)) {
                    //appkey for allow
                    appkeyAllow = true;
                    LOG.info("appkey is {}", appkeyAllow);
                }
            } else {
                if(allowAppkey.equals(appkey)) {
                    appkeyAllow = true;
                }
            }
            int cpu = targetEnvConfJson.getIntValue("cpu_count");
            int mem = targetEnvConfJson.getIntValue("mem_size");
            int hd = targetEnvConfJson.getIntValue("disk_size");
            LOG.info("cpu {} mem {} hd {}", cpu, mem, hd);
            if(appkeyAllow || (cpu <= 8 && mem <= 8 && hd <= 1000)) {
                return AccessResponse(200, "");
            }
            return AccessResponse(400, "conf is high need call manager");

        } catch (Exception e) {
            LOG.error("checkIsConf error ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? AccessResponse(400, "解析错误") : e.getMessage());
        }
    }

    private static String AccessResponse(int code, String errorInfo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("errorInfo", errorInfo);
        return jsonObject.toJSONString();
    }

    public String getOperationLog(String appkey, String entityType, String operator, String start, String end, Page page) {
        if (StringUtils.isBlank(entityType)) {
            LOG.info("log search all entityType={}", entityType);
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
            Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
            List<BorpClient.operationDisplay> operationListScaleIn = BorpClient.getOptLog(appkey, "缩容", operator, startTime, endTime, page);
            List<BorpClient.operationDisplay> operationListScaleOut = BorpClient.getOptLog(appkey, "扩容", operator, startTime, endTime, page);
            java.util.List<BorpClient.operationDisplay> javaScaleInList = convert(operationListScaleIn);
            java.util.List<BorpClient.operationDisplay> javaScaleOutList = convert(operationListScaleOut);
            ArrayList<BorpClient.operationDisplay> operationListScaleAll = new ArrayList<>();
            int scaleInIndex = 0;
            int scaleOutIndex = 0;
            int recordsSum = 0;
            while (scaleInIndex < javaScaleInList.size() && scaleOutIndex < javaScaleOutList.size()) {
                if (javaScaleInList.get(scaleInIndex).time().getTime() <= javaScaleOutList.get(scaleOutIndex).time().getTime()) {
                    operationListScaleAll.add(javaScaleInList.get(scaleInIndex));
                    scaleInIndex++;
                    continue;
                }
                if (javaScaleInList.get(scaleInIndex).time().getTime() > javaScaleOutList.get(scaleOutIndex).time().getTime()) {
                    operationListScaleAll.add(javaScaleOutList.get(scaleOutIndex));
                    scaleOutIndex++;
                    continue;
                }
            }
            if (scaleInIndex < javaScaleInList.size()) {
                recordsSum = scaleOutIndex + javaScaleInList.size();
                for (int mi = scaleInIndex; mi < javaScaleInList.size(); mi++) {
                    operationListScaleAll.add(javaScaleInList.get(mi));
                }
            }
            if (scaleOutIndex < javaScaleOutList.size()) {
                recordsSum = scaleInIndex + javaScaleOutList.size();
                for (int mi = scaleOutIndex; mi < javaScaleOutList.size(); mi++) {
                    operationListScaleAll.add(javaScaleOutList.get(mi));
                }
            }
            page.setTotalCount(recordsSum);
            return JsonHelper.dataJson(operationListScaleAll, page);
        } else {
            LOG.info("log search scaleIn or scaleOut entityType={}", entityType);
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
            Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
            List<BorpClient.operationDisplay> operationList = BorpClient.getOptLog(appkey, entityType, operator, startTime, endTime, page);
            return JsonHelper.dataJson(operationList, page);
        }
    }

    public java.util.List<BorpClient.operationDisplay> convert(scala.collection.immutable.Seq<BorpClient.operationDisplay> seq) {
        return scala.collection.JavaConversions.seqAsJavaList(seq);
    }

    public class TaskRunnable implements Runnable {

        private int task_id;
        private int type;//-1 缩容 1 扩容
        private String appkey;
        private String env;

        public TaskRunnable(int task_id, int type, String appkey, String env) {
            this.task_id = task_id;
            this.type = type;
            this.appkey = appkey;
            this.env = env;
        }

        @Override
        public void run() {
            String getTaskBody = HttpService.getRequestToBannerApi(PolicyConfig.BANNERAPI_URL + "/api/policy-task-record/records?tagId=-1&taskId=" + this.task_id);
            LOG.info("getTaskBody result {}", getTaskBody);
            JSONObject taskBodyJson = JSON.parseObject(getTaskBody);
            taskBodyJson = JSON.parseObject(taskBodyJson.getString("policyTaskRecordList").substring(1, taskBodyJson.getString("policyTaskRecordList").length() - 1));
            LOG.info("getTaskBody policyTaskRecordList result {}", getTaskBody);
            boolean flag = true;
            try {
                while (flag) {
                    String kapiScaleQuery = "";
                    if (1 == type) {
                        kapiScaleQuery = HttpService.getRequestToKApi(PolicyConfig.KAPI_URL + "/api/scaleout/" + this.task_id);
                    }
                    if (-1 == type) {
                        kapiScaleQuery = HttpService.getRequestToKApi(PolicyConfig.KAPI_URL + "/api/scalein/" + this.task_id);
                    }
                    if (kapiScaleQuery.equals(""))
                        break;
                    LOG.info("query kapiScaleResult {}", kapiScaleQuery);
                    JSONObject scaleQueryJson = JSON.parseObject(kapiScaleQuery);
                    if (scaleQueryJson.getString("status").equals("failed") || scaleQueryJson.getString("status").equals("completed")) {
                        taskBodyJson.put("taskStatus", scaleQueryJson.getString("status"));
                        String saveTaskResult = HttpService.postRequestToBannerApi(PolicyConfig.BANNERAPI_URL + "/api/policy-task-record/records/octo-update", taskBodyJson.toJSONString());
                        LOG.info("saveTaskResult in run {}", saveTaskResult);
                        if (-1 == type) {
                            if (scaleQueryJson.getString("status").equals("completed")) {
                                int scaleInNum = scaleQueryJson.getString("successSets").contains(",") ? scaleQueryJson.getString("successSets").split(",").length : 1;
                                BorpClient.saveOpt(UserUtils.getUser(), 3, appkey, EntityType.manualScaleIn(), "default", "",
                                        "TaskId:" + taskBodyJson.getString("taskId") +
                                                " 结束一键缩容,成功:" + scaleInNum + "台");
                                LOG.info("manual scaleIn operator {}", UserUtils.getUser().toString());
                                MessageService.sendScaleMessage(
                                        "事件: 一键缩容" +
                                                "\n服务:" + appkey +
                                                "\n环境:" + env +
                                                "\n操作人:" + UserUtils.getUser().getLogin() +
                                                "\n结果:成功 请刷新页面查看" +
                                                "\n信息:" + scaleQueryJson.getString("errorInfo") +
                                                "\n成功机器:" + scaleQueryJson.getString("successSets") +
                                                "\n失败机器:" + scaleQueryJson.getString("failSets"),
                                        Arrays.asList(UserUtils.getUser().getLogin(), "songjianjian"));
                            }else if (scaleQueryJson.getString("status").equals("failed")) {
                                BorpClient.saveOpt(UserUtils.getUser(), 3, appkey, EntityType.manualScaleIn(), "default", "",
                                        "TaskId:" + taskBodyJson.getString("taskId") +
                                                " 结束一键缩容,成功:0台,原因:" + scaleQueryJson.getString("errorInfo"));
                                LOG.info("manual scaleIn operator {}", UserUtils.getUser().toString());
                                MessageService.sendScaleMessage(
                                        "事件: 一键缩容" +
                                                "\n服务:" + appkey +
                                                "\n环境:" + env +
                                                "\n操作人:" + UserUtils.getUser().getLogin() +
                                                "\n结果:失败 请刷新页面查看" +
                                                "\n信息:" + scaleQueryJson.getString("errorInfo") +
                                                "\n失败机器:" + scaleQueryJson.getString("failSets"),
                                        Arrays.asList(UserUtils.getUser().getLogin(), "songjianjian"));
                            }
                        }
                        if (1 == type) {
                            StringBuffer setStringList = new StringBuffer();
                            int scaleOutNum = 0;
                            LOG.info("manual scaleOut operator {}", UserUtils.getUser().toString());
                            if (scaleQueryJson.getString("status").equals("completed")) {
                                String code = scaleQueryJson.getString("code");
                                switch (code) {
                                    case "0":
                                        com.alibaba.fastjson.JSONArray scaleOutResultSets = JSON.parseArray(scaleQueryJson.getString("setsInfo"));
                                        for (int outSetIndex = 0; outSetIndex < scaleOutResultSets.size(); outSetIndex++) {
                                            scaleOutNum ++;
                                            JSONObject item = scaleOutResultSets.getJSONObject(outSetIndex);
                                            setStringList.append(item.getString("name"));
                                        }
                                        BorpClient.saveOpt(UserUtils.getUser(), 1, appkey, EntityType.manualScaleOut(), "default", "",
                                                " TaskId:" + taskBodyJson.getString("taskId") +
                                                        " 结束一键扩容,成功:" + scaleOutNum + "台");
                                        MessageService.sendScaleMessage(
                                                "事件: 一键扩容" +
                                                        "\n服务:" + appkey +
                                                        "\n环境:" + env +
                                                        "\n操作人:" + UserUtils.getUser().getLogin() +
                                                        "\n结果:成功 请刷新页面查看" +
                                                        "\n扩容机器:" + setStringList.toString(),
                                                Arrays.asList(UserUtils.getUser().getLogin(), "songjianjian"));
                                        break;
                                    case "100":
                                        com.alibaba.fastjson.JSONArray scaleOutResultSetsPart = JSON.parseArray(scaleQueryJson.getString("setsInfo"));
                                        for (int outSetIndex = 0; outSetIndex < scaleOutResultSetsPart.size(); outSetIndex++) {
                                            scaleOutNum ++;
                                            JSONObject item = scaleOutResultSetsPart.getJSONObject(outSetIndex);
                                            setStringList.append(item.getString("name"));
                                        }
                                        BorpClient.saveOpt(UserUtils.getUser(), 1, appkey, EntityType.manualScaleOut(), "default", "",
                                                " TaskId:" + taskBodyJson.getString("taskId") +
                                                        " 结束一键扩容,成功:" + scaleOutNum + "台");
                                        MessageService.sendScaleMessage(
                                                "事件: 一键扩容" +
                                                        "\n服务:" + appkey +
                                                        "\n环境:" + env +
                                                        "\n操作人:" + UserUtils.getUser().getLogin() +
                                                        "\n结果:部分成功 请刷新页面查看" +
                                                        "\n信息:" + scaleQueryJson.getString("errorInfo") +
                                                        "\n扩容机器:" + setStringList.toString(),
                                                Arrays.asList(UserUtils.getUser().getLogin(), "songjianjian"));
                                        break;
                                    case "102":
                                        setStringList.append("");
                                        break;

                                }
                            }else if (scaleQueryJson.getString("status").equals("failed")) {
                                BorpClient.saveOpt(UserUtils.getUser(), 1, appkey, EntityType.manualScaleOut(), "default", "",
                                        " TaskId:" + taskBodyJson.getString("taskId") +
                                                " 结束一键扩容,成功:0台,原因:" + scaleQueryJson.getString("errorInfo"));
                                MessageService.sendScaleMessage(
                                        "事件: 一键扩容" +
                                                "\n服务:" + appkey +
                                                "\n环境:" + env +
                                                "\n操作人:" + UserUtils.getUser().getLogin() +
                                                "\n结果:失败 请刷新页面查看" +
                                                "\n信息:" + scaleQueryJson.getString("errorInfo"),
                                        Arrays.asList(UserUtils.getUser().getLogin(), "songjianjian"));
                            }
                        }
                        try {
                            if (cache.lock(appkey)) {
                                cache.put(PolicyConfig.CELLAR_KEY_PREFIX + appkey, "empty");
                                cache.unlock(appkey);
                                flag = false;
                            }
                        } catch (TairException e) {
                            LOG.error("unlock cache error {}", e);
                        } catch (IOException e) {
                            LOG.error("unlock cache error {}", e);
                        }
                    } else {
                        Thread.sleep(1000);
                        continue;
                    }
                }
            } catch (InterruptedException e) {
                LOG.error("InterruptedException thread run " + e);
            }
        }
    }

    public JSONObject makePolicyTask(JSONObject requestKapiResult, JSONObject request) {
        JSONObject policyTaskInfo = new JSONObject();
        policyTaskInfo.put("tagId", -1);
        policyTaskInfo.put("taskId", Integer.parseInt(requestKapiResult.getString("rescode")));
        policyTaskInfo.put("policyType", 0);
        policyTaskInfo.put("taskStatus", "creating");
        policyTaskInfo.put("createTime", System.currentTimeMillis());
        policyTaskInfo.put("createTime", System.currentTimeMillis());
        policyTaskInfo.put("expectScaleNum", request.getString("num"));
        policyTaskInfo.put("expectScaleNum", request.getString("num"));
        policyTaskInfo.put("channelType", 1);
        return policyTaskInfo;
    }
}
