package com.sankuai.octo.msgp.controller.hulk;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.msgp.common.model.EntityType;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyTaskRecord;
import com.sankuai.octo.msgp.serivce.service.serviceHulkPolicy;
import com.sankuai.octo.msgp.service.hulk.BannerApiTransitService;
import com.sankuai.octo.msgp.service.hulk.MccHulkService;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/hulk")
@Worth(project = Worth.Project.OCTO, model = Worth.Model.QUOTA)
public class HulkPolicyController {
    private static final Logger LOG = LoggerFactory.getLogger(HulkPolicyController.class);

    private static final MtConfigClient manuConfigMccClientBannerApi = MccHulkService.getMtConfigClientBannerApi();

    @Autowired
    private BannerApiTransitService bannerApiTransitService;

    public static java.util.List<BorpClient.operationDisplay> convert(scala.collection.immutable.List<BorpClient.operationDisplay> list) {
        return scala.collection.JavaConversions.seqAsJavaList(list);
    }

    @RequestMapping(value = "idc/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getIDC() {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getIDCInfo());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingGroup/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getScalingGroup(@PathVariable String appkey, @PathVariable int env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getScalingGroup(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "getIdcsByAppkeyAndEnv/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getIdcsByAppkeyAndEnv(@PathVariable String appkey, @PathVariable int env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getIdcsByAppkeyAndEnv(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingGroup/{appkey}/create", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String createScalingGroup(@PathVariable String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            return serviceHulkPolicy.createScalingGroup(json, user);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingGroup/{appkey}/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateScalingGroup(@PathVariable String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            return serviceHulkPolicy.updateScalingGroup(json, user);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingPolicy/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getScalingPolicy(@PathVariable String appkey, @PathVariable int env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getScalingPolicy(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingPolicy/{appkey}/save", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String saveScalingPolicy(@PathVariable String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            return serviceHulkPolicy.saveScalingPolicy(json, user);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingPolicy/{appkey}/{spId}/delete", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getScalingPolicy(@PathVariable String appkey, @PathVariable long spId) {
        try {
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            return JsonHelper.dataJson(serviceHulkPolicy.deleteScalingPolicy(appkey, user, spId));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingRecord/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getScalingRecord(
            @PathVariable String appkey,
            @PathVariable int env,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end,
            @RequestParam(value = "operatorType", required = false) int operatorType,
            @RequestParam(value = "idcType", required = false) String idcType,
            Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        long startTime = (start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis();
        long endTime = (end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis();
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getScalingRecord(appkey, env, startTime, endTime, operatorType, idcType, page), page);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "getScalingRecordScaleOut/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getScalingRecordScaleOut(
            @PathVariable String appkey,
            @PathVariable int env,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end,
            @RequestParam(value = "operatorType", required = false) int operatorType,
            @RequestParam(value = "idcType", required = false) String idcType,
            Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        long startTime = (start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis();
        long endTime = (end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis();
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getScalingRecordScaleOut(appkey, env, startTime, endTime, operatorType, idcType, page), page);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingPolicyAndGroup/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getScalingPolicyAndGroup(@PathVariable String appkey, @PathVariable int env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getScalingPolicyAndGroup(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "peakQPS/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getPeakQPS(@PathVariable String appkey, @PathVariable String env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getQpsMax(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingOut/{appkey}", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String scalingOut(@PathVariable String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            return serviceHulkPolicy.scaleOut(json, user);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "getScalingGroupAndRunningSet/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getScalingGroupAndRunningSet(@PathVariable String appkey, @PathVariable int env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getScalingGroupAndRunningSet(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "scalingIn/{appkey}", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String scalingIn(@PathVariable String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            return serviceHulkPolicy.scaleIn(json, user);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "checkIsImagineExist/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String checkIsImagineExist(@PathVariable String appkey, @PathVariable int env) {
        try {
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            return serviceHulkPolicy.checkIsImagineExist(appkey, env, user);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "periodicPolicyAndGroup/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getPeriodicPolicyAndGroup(@PathVariable String appkey, @PathVariable int env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getPeriodicPolicyAndGroup(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "latestImageInfo/{appkey}/{env}/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getLatestimageInfo(@PathVariable String appkey, @PathVariable int env) {
        try {
            return JsonHelper.dataJson(serviceHulkPolicy.getLatestImageInfo(appkey, env));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "/unifiedPolicy/getAll", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getAllUnifiedPolicy() {
        try {
            return bannerApiTransitService.getAllUnifiedPolicy();
        } catch (Exception e) {
            LOG.error("get all uni ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/unifiedPolicy/getAllPolicyByAppkeyAndEnv/{appkey}/{env}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getAllUnifiedPolicyByAppkeyAndEnv(@PathVariable String appkey, @PathVariable String env) {
        try {
            return bannerApiTransitService.getAllUnifiedPolicyByAppkeyAndEnv(appkey, env);
        } catch (Exception e) {
            LOG.error("get all uni by app and env ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/unifiedPolicy/updateUniPolicy", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String updateUniPolicyById(HttpServletRequest httpServletRequest) {
        try {
            String json = IOUtils.copyToString(httpServletRequest.getReader());
            JSONObject policyJson = JSON.parseObject(json);
            String updateUniPolicyResult = bannerApiTransitService.updateUniPolicyById(json);
            LOG.info("update uniPolicy result {}", updateUniPolicyResult);
            JSONObject updateResultJson = JSON.parseObject(updateUniPolicyResult);
            BorpClient.saveOpt(UserUtils.getUser(), 2, policyJson.getString("appkey"), EntityType.unifiedPolicyUpdate(), "default", "",
                    "监控策略更新:监控指标" + policyJson.getString("metricsBound") + "\n禁止缩容时间:" + policyJson.getString("noScaleinPeriods") + "\n分组ID:" + policyJson.getString("tags") + "\n执行结果: " + updateResultJson.getString("errorMsg"));
            return updateUniPolicyResult;
        } catch (Exception e) {
            LOG.error("update uni policy", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/unifiedPolicy/removePolicy", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String removeUnifiedPolicy(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject unifiedPolicyJson = JSON.parseObject(json);
            String removePolicyResult = bannerApiTransitService.removeUnifiedPolicy(json);
            LOG.info("remove uniPolicy result {}", removePolicyResult);
            JSONObject removeResultJson = JSON.parseObject(removePolicyResult);
            BorpClient.saveOpt(UserUtils.getUser(), 1, unifiedPolicyJson.getString("appkey"), EntityType.unifiedPolicyUpdate(), "default", "",
                    "删除统一监控策略:policyId:" + unifiedPolicyJson.get("policyId") + "\n执行结果: " + removeResultJson.getString("errorMsg"));
            return removePolicyResult;
        } catch (Exception e) {
            LOG.error("remove policy ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/getAllIndex", method = RequestMethod.GET)
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN)
    public String manuConfig() {
        //从MCC中获取一键扩容相关参数
        try {
            String bannerApiMccParams = manuConfigMccClientBannerApi.getValue("hulk2_monitor_index");
            LOG.info("bannerapi mcc result {}", bannerApiMccParams);
            List<String> allIndexMccList = new ArrayList<>();
            if(StringUtils.isEmpty(bannerApiMccParams)) {
                allIndexMccList.add("qps");
            } else if(!bannerApiMccParams.contains(",")) {
                allIndexMccList.add(bannerApiMccParams);
            } else {
                allIndexMccList = Arrays.asList(bannerApiMccParams.split(","));
            }
            Map<String, List<String>> map = new HashMap<>();
            map.put("data", allIndexMccList);
            ObjectMapper json = new ObjectMapper();
            return json.writeValueAsString(map);
        } catch (Exception e) {
            LOG.error("bannerapi mcc error {}", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 监控策略 修改监控策略状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/unifiedPolicy/updatePolicyState", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String updateUnifiedPolicyState(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject policyJson = JSON.parseObject(json);
            String updateUniPolicyResult = bannerApiTransitService.updateUnifiedPolicyState(json);
            LOG.info("update uniPolicy state result {}", updateUniPolicyResult);
            JSONObject addResultJson = JSON.parseObject(updateUniPolicyResult);
            String state = policyJson.getString("state");
            switch (state) {
                case "0":
                    state = "关闭";
                    break;
                case "1":
                    state = "开启";
                    break;
                case "-1":
                    state = "删除";
                    break;
            }
            BorpClient.saveOpt(UserUtils.getUser(), 1, policyJson.getString("appkey"), EntityType.unifiedPolicyUpdate(), "default", "",
                    "修改统一监控策略状态:" + state + "\n执行结果: " + addResultJson.getString("errorMsg"));
            return updateUniPolicyResult;
        } catch (Exception e) {
            LOG.error("update uni state ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/unifiedPolicy/addUniPolicy", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String addUnifiedPolicy(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject policyJson = JSON.parseObject(json);
            String addUniPolicyResult = bannerApiTransitService.addUnifiedPolicy(json);
            LOG.info("add uniPolicy result {}", addUniPolicyResult);
            JSONObject addResultJson = JSON.parseObject(addUniPolicyResult);
            BorpClient.saveOpt(UserUtils.getUser(), 1, policyJson.getString("appkey"), EntityType.unifiedPolicyAdd(), "default", "",
                    "监控策略新增: 监控指标:" + policyJson.getString("metricsBound") + "\n禁止缩容时间段:" + policyJson.getString("noScaleinPeriods") +
                            "\n分组ID:" + policyJson.getString("tags") + "\n执行结果: " + addResultJson.getString("errorMsg"));
            return addUniPolicyResult;
        } catch (Exception e) {
            LOG.error("add uni policy ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/getAllTagByAppkeyAndEnv/{appkey}/{env}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String addUnifiedPolicy(@PathVariable String appkey, @PathVariable String env) {
        try {
            return bannerApiTransitService.getAllTagByAppkeyAndEnv(appkey, env);
        } catch (Exception e) {
            LOG.error("add uni policy", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/getAllTagByPolicyId/{policyId}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getAllTagByPolicyId(@PathVariable int policyId) {
        try {
            return bannerApiTransitService.getAllTagByPolicyId(policyId);
        } catch (Exception e) {
            LOG.error("get all tag ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 创建分组
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/addTagRecord", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String addTagRecord(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject tagJson = JSON.parseObject(json);
            LOG.info("request getReader result {}", json);
            String addTagRecordResult = bannerApiTransitService.addTagRecord(json);
            LOG.info("add tagRecord result {}", addTagRecordResult);
            JSONObject addTagResult = JSON.parseObject(addTagRecordResult);
            BorpClient.saveOpt(UserUtils.getUser(), 1, tagJson.getString("appkey"), EntityType.groupOperationAdd(), "default", "",
                    "分组创建: 分组名称:" + tagJson.getString("tagName") + "分组内容:" +
                            "环境:" + tagJson.getString("env") +
                            "地域:" + tagJson.getString("region") +
                            "机房:" + tagJson.getString("idc") +
                            "单元:" + tagJson.getString("cell") +
                            "泳道:" + tagJson.getString("swimlane") + "执行结果: " + addTagResult.getString("errorMsg"));
            return addTagRecordResult;
        } catch (Exception e) {
            LOG.error("add tag ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 快速创建分组
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/fastAddTagRecord", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String fastAddTagRecord(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject tagJson = JSON.parseObject(json);
            LOG.info("request getReader result {}", json);
            String addTagRecordResult = bannerApiTransitService.fastAddTagRecord(json);
            LOG.info("fast add tagRecord result {}", addTagRecordResult);
            JSONObject addTagResult = JSON.parseObject(addTagRecordResult);
            BorpClient.saveOpt(UserUtils.getUser(), 1, tagJson.getString("appkey"), EntityType.groupOperationAdd(), "default", "",
                    "分组创建: 分组名称:" + tagJson.getString("tagName") + "分组内容:" +
                            "环境:" + tagJson.getString("env") +
                            "地域:" + tagJson.getString("region") +
                            "机房:" + tagJson.getString("idc") +
                            "单元:" + tagJson.getString("cell") +
                            "泳道:" + tagJson.getString("swimlane") + "执行结果: " + addTagResult.getString("errorMsg"));
            return addTagRecordResult;
        } catch (Exception e) {
            LOG.error("fast add tag ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/updateTagRecord", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String updateTagRecord(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject tagJson = JSON.parseObject(json);
            String updateTagRecordResult = bannerApiTransitService.updateTagRecord(json);
            LOG.info("update tag record result {}", updateTagRecordResult);
            JSONObject updateTagResultJson = JSON.parseObject(updateTagRecordResult);
            BorpClient.saveOpt(UserUtils.getUser(), 1, tagJson.getString("appkey"), EntityType.groupOperationUpdate(), "default", "",
                    "分组创建: 分组名称:" + tagJson.getString("tagName") + "分组内容:" +
                            "环境:" + tagJson.getString("env") +
                            "地域:" + tagJson.getString("region") +
                            "机房:" + tagJson.getString("idc") +
                            "单元:" + tagJson.getString("cell") +
                            "泳道:" + tagJson.getString("swimlane") + "执行结果: " + updateTagResultJson.getString("errorMsg"));
            return updateTagRecordResult;
        } catch (Exception e) {
            LOG.error("update tag all ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/updateTagInfoName", method = RequestMethod.POST, params = {"tagId", "tagName"})
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String updateTagInfoName(@RequestParam("tagId") long tagId, @RequestParam("tagName") String tagName, HttpServletRequest request) {
        try {
            String updateTagNameResult = bannerApiTransitService.updateTagInfoName(tagId, tagName);
            LOG.info("updateTag operator {} tag id {} and name {}", UserUtils.getUser().getLogin(), tagId, tagName);
            return updateTagNameResult;
        } catch (Exception e) {
            LOG.error("update tag name ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/removeTagInfo", method = RequestMethod.POST, params = {"tagId", "user"})
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String removeTagInfo(@RequestParam("tagId") long tagId, @RequestParam("user") String user, HttpServletRequest request) {
        try {
            String removeTagNameResult = bannerApiTransitService.removeTagInfo(tagId, user);
            LOG.info("deleteTag tag id {} operator {}", tagId, user);
            return removeTagNameResult;
        } catch (Exception e) {
            LOG.error("remove tag", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/getIdcList", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getIdcList(@RequestParam("env") String env, @RequestParam("region") String region) {
        try {
            return bannerApiTransitService.getIdcList(env, region);
        } catch (Exception e) {
            LOG.error("get idc list ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/getRichIdcList", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getRichIdcList(@RequestParam("env") String env, @RequestParam("region") String region, @RequestParam("origin") String origin) {
        try {
            return bannerApiTransitService.getRichIdcList(env, region, origin);
        } catch (Exception e) {
            LOG.error("get rich idc list ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/getCellList", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getCellList(@RequestParam("env") String env) {
        try {
            return bannerApiTransitService.getCellList(env);
        } catch (Exception e) {
            LOG.error("get cell list ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * @table policy_op_record
     * 弹性操作记录:插入记录
     */
    @RequestMapping(value = "/insertPolicyOpRecord", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String insertPolicyOpRecord(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return bannerApiTransitService.insertPolicyOpRecord(json);
        } catch (Exception e) {
            LOG.error("insert record ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    //获取一段时间的弹性伸缩记录
    @RequestMapping(value = "/getPolicyTaskRecord", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public List<PolicyTaskRecord> getPolicyOpRecord(@RequestParam("tagString") String tagString,
                                                    @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime, HttpServletRequest request) {
        try {
            return bannerApiTransitService.getPolicyTaskRecord(tagString, Long.parseLong(startTime), Long.parseLong(endTime));
        } catch (Exception e) {
            LOG.error("get period record", e);
            return null;
        }
    }

    //获取一段时间操作记录
    @RequestMapping(value = "/getPolicyOpRecord", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getPolicyOpRecord(@RequestParam("policyId") long policyId, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return bannerApiTransitService.getPolicyOpRecord(policyId);
        } catch (Exception e) {
            LOG.error("get policy record", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    //获取一段时间操作记录
    @RequestMapping(value = "/getTagById", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getTagById(@RequestParam("tagId") String tagId, HttpServletRequest request) {
        try {
            if (tagId.equals(""))
                return "";
            return bannerApiTransitService.getTagById(Long.parseLong(tagId));
        } catch (Exception e) {
            LOG.error("get tag by id ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    //获取一段时间操作记录
    @RequestMapping(value = "/getPolicyIdByAppkeAndTime", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getPolicyIdByAppkeAndTime(@RequestParam("appkey") String appkey, @RequestParam("createTime") long createTime, HttpServletRequest request) {
        try {
            return bannerApiTransitService.getPolicyIdByAppkeyAndTime(appkey, createTime);
        } catch (Exception e) {
            LOG.error("get policyId ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    //获取一段时间操作记录
    @RequestMapping(value = "/getAllSetByAppkeyAndEnv", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getAllSetByAppkeyAndEnv(@RequestParam("appkey") String appkey, @RequestParam("env") String env, HttpServletRequest request) {
        try {
            return bannerApiTransitService.getAllSetByAppkeyAndEnv(appkey, env);
        } catch (Exception e) {
            LOG.error("get log ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    //判断是否可以接入弹性
    @RequestMapping(value = "/stableImage/check", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String checkIsRightForHulk(@RequestParam("appkey") String appkey, @RequestParam("env") String env, HttpServletRequest request) {
        try {
            return bannerApiTransitService.checkIsRightForHulk(appkey, env);
        } catch (Exception e) {
            LOG.error("check is right ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/getPolicyByTagIds", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getPolicyByTagIds(@RequestParam("appkey") String appkey, @RequestParam("tags") String tags, HttpServletRequest request) {
        try {
            return bannerApiTransitService.getPolicyByTagIds(appkey, tags);
        } catch (Exception e) {
            LOG.error("update policy by tag ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @Worth(model = Worth.Model.Monitor, function = "组合日志")
    @RequestMapping(value = "operation/{appkey}/log", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String operationLog(@PathVariable(value = "appkey") String appkey,
                               @RequestParam(value = "entityType", required = false) String entityType,
                               @RequestParam(value = "operator", required = false) String operator,
                               @RequestParam(value = "start", required = false) String start,
                               @RequestParam(value = "end", required = false) String end,
                               @RequestParam(value = "env", required = false) String env,
                               Page page) {
        String result = bannerApiTransitService.getOperationLog(appkey, entityType, operator, start, end, env, page);
        return result;
    }

    /**
     * 周期策略2.0 周期策略绑定的tags
     *
     * @param policyId 周期策略id
     * @return
     */
    @RequestMapping(value = "/periodicPolicy/getTags", method = RequestMethod.GET, produces = "application/json;charset=utf-8", params = {"policyId"})
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getPeriodicPolicyTags(@RequestParam("policyId") Long policyId) {
        try {
            return bannerApiTransitService.getPeriodicPolicyTags(policyId);
        } catch (Exception e) {
            LOG.error("get tags in periodic", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 周期策略2.0 根据appkey和env筛选周期策略
     *
     * @param appkey
     * @param env
     * @return
     */
    @RequestMapping(value = "/periodicPolicy/getAllPolicyByAppkeyAndEnv", method = RequestMethod.GET, produces = "application/json;charset=utf-8", params = {"appkey", "env"})
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getAllPeriodicPolicyByAppkeyAndEnv(@RequestParam("appkey") String appkey, @RequestParam("env") String env) {
        try {
            return bannerApiTransitService.getAllPeriodicPolicyByAppkeyAndEnv(appkey, env);
        } catch (Exception e) {
            LOG.error("get periodic", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 周期策略2.0 修改周期策略
     *
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/periodicPolicy/updatePolicy", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String updatePeriodicPolicy(HttpServletRequest httpServletRequest) {
        try {
            String json = IOUtils.copyToString(httpServletRequest.getReader());
            JSONObject periodPolicyJson = JSON.parseObject(json);
            LOG.info("request getReader result {}", json);
            String updatePeriodPolicyResult = bannerApiTransitService.updatePeriodicPolicy(json);
            LOG.info("update periodPolicy result {}", updatePeriodPolicyResult);
            JSONObject updatePeriodPolicyJson = JSON.parseObject(updatePeriodPolicyResult);
            BorpClient.saveOpt(UserUtils.getUser(), 2, periodPolicyJson.getString("appkey"), EntityType.periodPolicyUpdate(), "default", "",
                    "周期策略更新: " + " 分组ID:" + periodPolicyJson.getString("tags") +
                            " 周内策略:" + periodPolicyJson.getString("policiesInWeek") +
                            " 跨周策略:" + periodPolicyJson.getString("policiesBtwDatetime") +
                            " 执行结果: " + updatePeriodPolicyJson.getString("errorMsg"));
            return updatePeriodPolicyResult;
        } catch (Exception e) {
            LOG.error("update periodic all", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 周期策略2.0 新增周期策略
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/periodicPolicy/addPolicy", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String addPeriodicPolicy(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject periodPolicyJson = JSON.parseObject(json);
            LOG.info("request getReader result {}", json);
            String addPeriodPolicyResult = bannerApiTransitService.addPeriodicPolicy(json);
            LOG.info("add periodPolicy result {}", addPeriodPolicyResult);
            JSONObject addPeriodPolicyJson = JSON.parseObject(addPeriodPolicyResult);
            BorpClient.saveOpt(UserUtils.getUser(), 2, periodPolicyJson.getString("appkey"), EntityType.periodPolicyAdd(), "default", "",
                    "周期策略更新: " + " 分组ID:" + periodPolicyJson.getString("tags") +
                            " 周内策略:" + periodPolicyJson.getString("policiesInWeek") +
                            " 跨周策略:" + periodPolicyJson.getString("policiesBtwDatetime") +
                            " 执行结果: " + addPeriodPolicyJson.getString("errorMsg"));
            return addPeriodPolicyResult;
        } catch (Exception e) {
            LOG.error("add periodic", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 周期策略 删除周期策略
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/periodicPolicy/removePolicy", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String removePeriodicPolicy(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject periodPolicyJson = JSON.parseObject(json);
            String removePeriodPolicyResult = bannerApiTransitService.removePeriodicPolicy(json);
            JSONObject updatePeriodPolicyJson = JSON.parseObject(removePeriodPolicyResult);
            BorpClient.saveOpt(UserUtils.getUser(), 2, periodPolicyJson.getString("appkey"), EntityType.periodPolicyUpdate(), "default", "",
                    "删除周期策略: policyId:" + periodPolicyJson.get("policyId") + "执行结果: " + updatePeriodPolicyJson.getString("errorMsg"));
            return removePeriodPolicyResult;
        } catch (Exception e) {
            LOG.error("remove periodic", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    /**
     * 周期策略 修改周期策略状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/periodicPolicy/updatePolicyState", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String updatePeriodicPolicyState(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject periodPolicyJson = JSON.parseObject(json);
            String updatePeriodPolicyResult = bannerApiTransitService.updatePeriodicPolicyState(json);
            JSONObject updatePeriodPolicyJson = JSON.parseObject(updatePeriodPolicyResult);
            BorpClient.saveOpt(UserUtils.getUser(), 2, periodPolicyJson.getString("appkey"), EntityType.periodPolicyUpdate(), "default", "",
                    "更新周期策略状态: " + periodPolicyJson.getString("state") + "执行结果: " + updatePeriodPolicyJson.getString("errorMsg"));
            return updatePeriodPolicyResult;
        } catch (Exception e) {
            LOG.error("update periodic", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/operation/actions", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hulkOperationActions(@RequestParam(value = "appkey") String appkey,
                                       @RequestParam(value = "env", required = false) String env,
                                       @RequestParam(value = "entityType") String entityType,
                                       @RequestParam(value = "actionType", required = false) String actionType,
                                       @RequestParam(value = "start", required = false) String start,
                                       @RequestParam(value = "end", required = false) String end,
                                       Page page) {
        try {
            if (page == null) {
                page = new Page();
            }
            return bannerApiTransitService.getOperationActions(appkey, env, entityType, actionType, start, end, page.getPageNo(), page.getPageSize());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
}
