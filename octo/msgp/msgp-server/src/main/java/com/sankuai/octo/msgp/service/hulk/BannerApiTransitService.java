package com.sankuai.octo.msgp.service.hulk;

import com.alibaba.fastjson.JSON;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.google.gson.Gson;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyConfig;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyTask;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyTaskRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Jsong on 2018/5/30.
 * 各种请求BannerApi封装类;BannerAPI是弹性相关数据库操作类
 */
@Service
public class BannerApiTransitService {

    private static final Logger LOG = LoggerFactory.getLogger(BannerApiTransitService.class);

    private static String HOST_URL = "http://bannerapi.inf.dev.sankuai.com";
    private static String IMAGE_URL = "http://registryapi.inf.vip.sankuai.com";

    static {
        if (ProcessInfoUtil.isLocalHostOnline()) {
            HOST_URL = PolicyConfig.HOST_URL_ONLINE;
            IMAGE_URL = PolicyConfig.IMAGE_URL_ONLINE;
        } else {
            HOST_URL = PolicyConfig.HOST_URL_OFFLINE;
            IMAGE_URL = PolicyConfig.IMAGE_URL_OFFLINE;
        }
    }

    public BannerApiTransitService() {

    }

    public String getAllUnifiedPolicy() {
        String allPolicysInDb = HttpService.getResponseNeedJsonStr("/api/unified-policy/policies", "unifiedPolicyList");
        return allPolicysInDb;
    }

    /**
     * 获取appkey下选定环境中所配置的所有策略
     *
     * @return #todo error
     */
    public String getAllUnifiedPolicyByAppkeyAndEnv(String appkey, String env) {
        String allPolicy = HttpService.getResponseNeedJsonStr(HOST_URL + "/api/unified-policy/policies?appkey=" + appkey + "&env=" + env, "unifiedPolicyList");
        LOG.info("getAllUnifiedPolicy result {}", allPolicy);
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(allPolicy);
            String allQuota = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject temp_object = new JSONObject(jsonArray.get(i).toString());
                String ele = temp_object.getString("metricsBound");
                Map<String, Map<String, String>> metricBoundMap = new HashMap<>();
                metricBoundMap = new Gson().fromJson(ele, metricBoundMap.getClass());
                LOG.info("metricBoundMap", metricBoundMap.toString());
                allQuota = metricBoundMap.keySet().toString();
                temp_object.put("all_quota", allQuota);
            }
        } catch (JSONException e) {
            LOG.error("all unifiedPolicy error ", e);
        }
        return jsonArray.toString();
    }

    public String updateUnifiedPolicyState(String data) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/unified-policy/policies/update-state", data);
        return result;
    }

    public String addTagRecord(String jsonStr) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/tag-info/tags/create", jsonStr);
        LOG.info("add tag record result:{}", jsonStr);
        return result;
    }

    public String fastAddTagRecord(String jsonStr) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/tag-info/tags/fast-create", jsonStr);
        LOG.info("fast add tag record result:{}", jsonStr);
        return result;
    }

    public String getAllTagByAppkeyAndEnv(String appkey, String env) {
        String url = HOST_URL + "/api/tag-info/tags?appkey=" + appkey + "&env=" + env;
        return HttpService.executeRequestBannerApi(url);
    }

    public String addUnifiedPolicy(String data) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/unified-policy/policies/create", data);
        return result;
    }

    public String removeUnifiedPolicy(String data) {
        String result = HttpService.executePostRequestBannerApi(String.format(HOST_URL + "/api/unified-policy/policies/remove"), data);
        return result;
    }

    public String getAllTagByPolicyId(int policyId) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/unified-policy/policies/tags?policyId=" + policyId);
        return result;
    }

    public String updateTagRecord(String json) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/tag-info/tags/octo-update", json);
        return result;
    }

    public String updateTagInfoName(long tagId, String tagName) {
        String result = HttpService.executePostRequestBannerApi(String.format(HOST_URL + "/api/tag-info/tags/update-name?tagId=%s&tagName=%s", tagId, tagName), "");
        return result;
    }

    public String removeTagInfo(long tagId, String user) {
        String result = HttpService.executePostRequestBannerApi(String.format(HOST_URL + "/api/tag-info/tags/remove?tagId=%s&user=%s", tagId, user), "");
        return result;
    }

    public String getIdcList(String env, String region) {
        String url = String.format(HOST_URL + "/api/tag-info/tags/idcs?env=%s&region=%s", env, region);
        return HttpService.executeRequestBannerApi(url);
    }

    public String getRichIdcList(String env, String region, String origin) {
        String url = String.format(HOST_URL + "/api/tag-info/tags/rich-idcs?env=%s&region=%s&origin=%s", env, region, origin);
        return HttpService.executeRequestBannerApi(url);
    }

    public String getCellList(String env) {
        String url = String.format(HOST_URL + "/api/tag-info/tags/cells?env=%s", env);
        return HttpService.executeRequestBannerApi(url);
    }

    public String insertPolicyTaskRecord(String json) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/policy-task-record/create", json);
        return result;
    }

    public String updateUniPolicyById(String content) {
        String reqResult = HttpService.executePostRequestBannerApi(HOST_URL + "/api/unified-policy/policies/update", content);
        return reqResult;
    }

    public List<PolicyTaskRecord> getPolicyTaskRecord(String tagIdList, long startTime, long endTime) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/policy-task-record/task-records?tagId=" +
                tagIdList + "&startTime=" + startTime + "&endTime=" + endTime);
        LOG.info("task record result by tagIdList {}", result);
        try {
            JSONObject resultJson = new JSONObject(result);
            if (!resultJson.getString("errorMsg").equals("success"))
                return null;
            List<PolicyTaskRecord> resultJsonArray = JSON.parseArray(resultJson.getString("policyTaskRecordList"), PolicyTaskRecord.class);
            return resultJsonArray;
        } catch (JSONException e) {
            LOG.error("make jsonObject {}", e);
        }
        return null;
    }

    public List<PolicyTaskRecord> getPolicyTaskRecord(long startTime, long endTime) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/policy-task-record/task-records-time?startTime=" + startTime + "&endTime=" + endTime);
        LOG.info("task record result {}", result);
        try {
            JSONObject resultJson = new JSONObject(result);
            if (!resultJson.getString("errorMsg").equals("success"))
                return null;
            List<PolicyTaskRecord> resultJsonArray = JSON.parseArray(resultJson.getString("policyTaskRecordList"), PolicyTaskRecord.class);
            return resultJsonArray;
        } catch (JSONException e) {
            LOG.error("JSON error {}", e);
        }
        return null;
    }

    public String insertPolicyOpRecord(String json) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/policy-op-record/insert", json);
        return result;
    }

    public String getPolicyOpRecord(long policyId) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/policy-op-record/records" + policyId);
        return result;
    }

    public String getTagById(long l) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/tag-info/tags?tagId=" + l);
        LOG.info("get tag by tagId {}", result);
        return result;
    }

    public String getPolicyIdByAppkeyAndTime(String appkey, long time) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/unified-policy/tags/id?appkey=" + appkey + "&createTime=" + time);
        return result;
    }

    public String getAllSetByAppkeyAndEnv(String appkey, String env) {
        String result = HttpService.executeRequestBannerApi(PolicyConfig.KAPI_URL + "/api/app/instance?appkey=" + appkey + "&env=prod");
        return result;
    }

    public String checkIsRightForHulk(String appkey, String env) {
        String result = HttpService.executeRequestImage(IMAGE_URL+"/api/repository/image/stable?appkey=" + appkey + "&env=" + env);
        return result;
    }


    public String getAllPeriodicPolicy() {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/periodic-policy/policies");
        return result;
    }

    public String getAllPeriodicPolicyByAppkeyAndEnv(String appkey, String env) {
        String result = HttpService.executeRequestBannerApi(String.format(HOST_URL + "/api/periodic-policy/policies?appkey=%s&env=%s", appkey, env));
        return result;
    }


    public String getPeriodicPolicyTags(Long policyId) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/periodic-policy/policies/tags?policyId=" + policyId);
        return result;
    }


    public String updatePeriodicPolicy(String data) {
        String reqResult = HttpService.executePostRequestBannerApi(HOST_URL + "/api/periodic-policy/policies/update", data);
        return reqResult;
    }

    public String addPeriodicPolicy(String data) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/periodic-policy/policies/create", data);
        return result;
    }

    public String removePeriodicPolicy(String data) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/periodic-policy/policies/remove", data);
        return result;
    }

    public String updatePeriodicPolicyState(String data) {
        String result = HttpService.executePostRequestBannerApi(HOST_URL + "/api/periodic-policy/policies/update-state", data);
        return result;
    }

    public String getPolicyByTagIds(String appkey, String tags) {
        String result = HttpService.executeRequestBannerApi(HOST_URL + "/api/unified-policy/policies?appkey=" + appkey + "&tagsId=" + tags);
        LOG.info("request url {}", HOST_URL + "/api/unified-policy/policies?appkey=" + appkey + "&tagsId=" + tags);
        LOG.info("request result {}", result);
        return result;
    }

    public String getOperationLog(String appkey, String entityType, String operator, String start, String end, String env, Page page) {
        //获取该服务所有的tagId
        String allTags = getAllTagByAppkeyAndEnv(appkey, env);
        LOG.info("allTags info {}", allTags);
        if(allTags.isEmpty()){
            return null;
        }else{
            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(allTags);
            if(jsonObject == null){
                return null;
            }else{
                allTags = jsonObject.getString("tagInfoList");
            }
        }
        if (StringUtils.isBlank(entityType)) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
            Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
            String allTagsString = "";
            com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(allTags);
            if (jsonArray.isEmpty()) {
                return null;
            }
            for (int i = 0; i < jsonArray.size(); i++){
                com.alibaba.fastjson.JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(i != jsonArray.size() - 1) {
                    allTagsString += jsonObject.getString("id") + ",";
                }else{
                    allTagsString += jsonObject.getString("id");
                }
            }
            LOG.info("get all task by tagId {}", allTagsString);
            List<PolicyTaskRecord> listPolicyTaskRecord = getPolicyTaskRecord(allTagsString, startTime.getTime(), endTime.getTime());
            if(listPolicyTaskRecord.isEmpty()){
                return null;
            }
            List<PolicyTask> allPolicyRecords = new ArrayList<>();
            for(PolicyTaskRecord policyTaskRecord: listPolicyTaskRecord){
                if(policyTaskRecord != null && policyTaskRecord.getPolicyType() == 3){
                    LOG.info("get all unified policy policyTaskRecord {}", policyTaskRecord);
                    PolicyTask policyTask = new PolicyTask();
                    policyTask.setTime(policyTaskRecord.getUpdateTime() == null?policyTaskRecord.getCreateTime():policyTaskRecord.getUpdateTime());
                    policyTask.setOperatorName("policy");
                    policyTask.setTagId(policyTaskRecord.getTagId());
                    policyTask.setEntityType("触发监控策略");
                    String tagInfoRecord = getTagById(policyTaskRecord.getTagId());
                    LOG.info("tag info by tagid {} is {}", policyTaskRecord.getTagId(), tagInfoRecord);
                    policyTask.setTagName(getTagName(tagInfoRecord));
                    policyTask.setNewValue("TaskId: " + policyTaskRecord.getTaskId() +
                            " 期望" + (policyTaskRecord.getExpectScaleNum() >= 0 ? "扩容:" + policyTaskRecord.getExpectScaleNum() : "缩容:" + Math.abs(policyTaskRecord.getExpectScaleNum()))
                            + " 实际" + (policyTaskRecord.getActualScaleNum() >= 0 ? "扩容:" + policyTaskRecord.getActualScaleNum() : "缩容:" + Math.abs(policyTaskRecord.getActualScaleNum())));
                    allPolicyRecords.add(policyTask);
                }
                if(policyTaskRecord != null && policyTaskRecord.getPolicyType() == 2) {
                    LOG.info("get all periodic policy policyTaskRecord {}", policyTaskRecord);
                    PolicyTask policyTask = new PolicyTask();
                    policyTask.setTime(policyTaskRecord.getUpdateTime() == null?policyTaskRecord.getCreateTime():policyTaskRecord.getUpdateTime());
                    policyTask.setOperatorName("policy");
                    policyTask.setTagId(policyTaskRecord.getTagId());
                    policyTask.setEntityType("触发周期策略");
                    String tagInfoRecord = getTagById(policyTaskRecord.getTagId());
                    LOG.info("tag info by tagid {} is {}", policyTaskRecord.getTagId(), tagInfoRecord);
                    policyTask.setTagName(getTagName(tagInfoRecord));
                    policyTask.setNewValue("TaskId: " + policyTaskRecord.getTaskId() +
                            " 期望" + (policyTaskRecord.getExpectScaleNum() >= 0 ? "扩容:" + policyTaskRecord.getExpectScaleNum() : "缩容:" + Math.abs(policyTaskRecord.getExpectScaleNum()))
                            + " 实际" + (policyTaskRecord.getActualScaleNum() >= 0 ? "扩容:" + policyTaskRecord.getActualScaleNum() : "缩容:" + Math.abs(policyTaskRecord.getActualScaleNum())));
                    allPolicyRecords.add(policyTask);
                }
            }
            page.setTotalCount(allPolicyRecords.size());
            int startIndex = (page.getPageNo() - 1) * page.getPageSize() < allPolicyRecords.size() ? (page.getPageNo() - 1) * page.getPageSize():allPolicyRecords.size();
            int endIndex = page.getPageNo() * page.getPageSize() > allPolicyRecords.size()?allPolicyRecords.size():page.getPageNo() * page.getPageSize();
            return JsonHelper.dataJson(allPolicyRecords.subList(startIndex, endIndex), page);
        } else {
            if (entityType.equals("监控策略新增") || entityType.equals("监控策略更新") || entityType.equals("周期策略新增") || entityType.equals("周期策略更新") || entityType.equals("分组创建") || entityType.equals("分组更新")) {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
                Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
                scala.collection.immutable.List<BorpClient.operationDisplay> operationList = BorpClient.getOptLog(appkey, entityType, operator, startTime, endTime, page);
                LOG.info("records form borp {}", operationList);
                if (operationList.isEmpty() || operationList.size() == 0) {
                    return null;
                }
                return JsonHelper.dataJson(operationList, page);
            } else {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
                Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
                LOG.info("task request time startTime={}, endTime={}", startTime.getTime(), endTime.getTime());
                String allTagsString = "";
                com.alibaba.fastjson.JSONArray jsonArray = JSON.parseArray(allTags);
                if (jsonArray.isEmpty()) {
                    return null;
                }
                for (int i = 0; i < jsonArray.size(); i++){
                    com.alibaba.fastjson.JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if(i != jsonArray.size() - 1) {
                        LOG.info("last tag id{}", jsonObject.getString("id"));
                        allTagsString += jsonObject.getString("id") + ",";
                    }else{
                        allTagsString += jsonObject.getString("id");
                    }
                }
                LOG.info("get all task by tagId {}", allTagsString);
                List<PolicyTaskRecord> listPolicyTaskRecord = getPolicyTaskRecord(allTagsString, startTime.getTime(), endTime.getTime());
                if(listPolicyTaskRecord.isEmpty()){
                    return null;
                }
                List<PolicyTask> resultLogList = new ArrayList<>();
                LOG.info("Get task record from bannerapi {}", listPolicyTaskRecord);
                if(entityType.equals("触发监控策略")){
                    for (int ui = 0; ui < listPolicyTaskRecord.size(); ui++) {
                        PolicyTaskRecord oneRecord = listPolicyTaskRecord.get(ui);
                        if (oneRecord != null && oneRecord.getPolicyType() == 3) {
                            PolicyTask policyTask = new PolicyTask();
                            policyTask.setTime(oneRecord.getUpdateTime() == null ? oneRecord.getCreateTime() : oneRecord.getUpdateTime());
                            policyTask.setOperatorName("policy");
                            policyTask.setTagId(oneRecord.getTagId());
                            policyTask.setEntityType("触发监控策略");
                            String tagInfoRecord = getTagById(oneRecord.getTagId());
                            LOG.info("tag info by tagid {} is {}", oneRecord.getTagId(), tagInfoRecord);
                            policyTask.setTagName(getTagName(tagInfoRecord));
                            policyTask.setNewValue("TaskId: " + oneRecord.getTaskId() +
                                    " 期望" + (oneRecord.getExpectScaleNum() >= 0 ? "扩容:" + oneRecord.getExpectScaleNum() : "缩容:" + Math.abs(oneRecord.getExpectScaleNum()))
                                    + " 实际" + (oneRecord.getActualScaleNum() >= 0 ? "扩容:" + oneRecord.getActualScaleNum() : "缩容:" + Math.abs(oneRecord.getActualScaleNum())));
                            resultLogList.add(policyTask);
                        }
                    }
                }
                if (entityType.equals("触发周期策略")) {
                    for (int pi = 0; pi < listPolicyTaskRecord.size(); pi++) {
                        PolicyTaskRecord oneRecord = listPolicyTaskRecord.get(pi);
                        if (oneRecord != null && oneRecord.getPolicyType() == 2) {
                            PolicyTask policyTask = new PolicyTask();
                            policyTask.setTime(oneRecord.getUpdateTime() == null ? oneRecord.getCreateTime() : oneRecord.getUpdateTime());
                            policyTask.setOperatorName("policy");
                            policyTask.setTagId(oneRecord.getTagId());
                            policyTask.setEntityType("触发周期策略");
                            String tagInfoRecord = getTagById(oneRecord.getTagId());
                            LOG.info("tag info by tagid {} is {}", oneRecord.getTagId(), tagInfoRecord);
                            policyTask.setTagName(getTagName(tagInfoRecord));
                            policyTask.setNewValue("TaskId: " + oneRecord.getTaskId() +
                                    " 期望" + (oneRecord.getExpectScaleNum() >= 0 ? "扩容:" + oneRecord.getExpectScaleNum() : "缩容:" + Math.abs(oneRecord.getExpectScaleNum()))
                                    + " 实际" + (oneRecord.getActualScaleNum() >= 0 ? "扩容:" + oneRecord.getActualScaleNum() : "缩容:" + Math.abs(oneRecord.getActualScaleNum())));
                            resultLogList.add(policyTask);
                        }
                    }
                }
                page.setTotalCount(resultLogList.size());
                int startIndex = (page.getPageNo() - 1) * page.getPageSize() < resultLogList.size() ? (page.getPageNo() - 1) * page.getPageSize():resultLogList.size();
                int endIndex = page.getPageNo() * page.getPageSize() >= resultLogList.size() ? resultLogList.size() : page.getPageNo() * page.getPageSize();
                return JsonHelper.dataJson(resultLogList.subList(startIndex, endIndex), page);
            }
        }
    }

    private String getTagName(String tagInfoRecord) {
        JSONObject resultJson = null;
        try {
            resultJson = new JSONObject(tagInfoRecord);
            if(resultJson == null) {
                return "null";
            } else {
                String tagString = resultJson.getString("tagInfoList");
                if(tagString == null || tagString.equals("") || tagString.equals("[]")) {
                    return "null";
                } else {
                    JSONObject tagInfoJson = new JSONObject(tagString.substring(1, tagString.length() - 1));
                    return tagInfoJson.getString("tagName");
                }
            }
        } catch (JSONException e) {
            LOG.error("json error", e);
        }
        return "null";
    }

    public String getOperationActions(String appkey, String env, String entityType, String actionType, String start, String end, Integer pageNo, Integer pageSize) {
        if(StringUtils.isNotEmpty(start)){
            try {
                start = URLEncoder.encode(start, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("getOperationActions encode startTime error,start:" + start);
            }
        }

        if(StringUtils.isNotEmpty(end)){
            try {
                end = URLEncoder.encode(end, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("getOperationActions encode endTime error,end:" + end);
            }
        }

        String params = String.format("/api/policy-op-record/actions?appkey=%s&env=%s&entityType=%s&actionType=%s&start=%s&end=%s&pageNo=%s&pageSize=%s",
                appkey, env, entityType, actionType, start, end, pageNo, pageSize);
        String result = HttpService.executeRequestBannerApi(HOST_URL + params);
        return result;
    }
}
