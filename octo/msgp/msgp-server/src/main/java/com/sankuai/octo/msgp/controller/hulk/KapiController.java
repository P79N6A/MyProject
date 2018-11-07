package com.sankuai.octo.msgp.controller.hulk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.inf.utils.cache.CellarCache;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.msgp.common.model.EntityType;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyConfig;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.service.hulk.HttpService;
import com.sankuai.octo.msgp.service.hulk.KapiService;
import com.sankuai.octo.msgp.service.hulk.MccHulkService;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import com.taobao.tair3.client.error.TairException;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping("/kapi")
public class KapiController {

    private static final Logger LOG = LoggerFactory.getLogger(KapiController.class);
    private static String IMAGE_URL = "http://registryapionline.inf.vip.sankuai.com";//http://registryapionline.inf.test.sankuai.com
    private static CellarCache cache = KapiService.getCellarClient();
    public static final MtConfigClient manuConfigMccClient = MccHulkService.getMtConfigClient();
    private static ExecutorService taskThreadPool;

    @Autowired
    private KapiService kapiService;

    static {
        if (ProcessInfoUtil.isLocalHostOnline()) {
            IMAGE_URL = PolicyConfig.IMAGE_URL_ONLINE;
        } else {
            IMAGE_URL = PolicyConfig.IMAGE_URL_OFFLINE;
        }

        taskThreadPool = Executors.newCachedThreadPool();
    }

    @RequestMapping(value = "/app/instance", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getAllSetByAppkeyAndEnv(@RequestParam("appkey") String appkey, @RequestParam("env") String env) {
        String url = "";
        try {
            url = String.format("%s/api/app/instance?appkey=%s&env=%s", PolicyConfig.KAPI_URL, appkey, env);
            LOG.info("get instances url {}", url);
            return HttpService.getRequestToKApi(url);
        } catch (Exception e) {
            LOG.error("get all sets from kapi {} ", url, e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/scalein", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String scaleInToKApi(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("manual scalein request {}", json);
            JSONObject jsonObject = JSON.parseObject(json);
            if (jsonObject.isEmpty()) {
                return RequestError("请求参数错误");
            }
            if (isTaskCanExecute(PolicyConfig.CELLAR_KEY_PREFIX + jsonObject.getString("appkey"))) {
                String resultJson = HttpService.postRequestToKApi(PolicyConfig.KAPI_URL + "/api/scalein", json);
                JSONObject resultJsonObject = JSON.parseObject(resultJson);
                JSONObject policyTaskInfo = kapiService.makePolicyTask(resultJsonObject, jsonObject);
                if (cache.lock(jsonObject.getString("appkey"))) {
                    cache.put(PolicyConfig.CELLAR_KEY_PREFIX + jsonObject.getString("appkey"), resultJsonObject.getString("rescode"));
                    cache.unlock(jsonObject.getString("appkey"));
                } else {
                    return RequestError("有扩缩动作未完成,请稍后执行动作");
                }
                String saveTaskResult = HttpService.postRequestToBannerApi(PolicyConfig.BANNERAPI_URL + "/api/policy-task-record/records/octo-add", policyTaskInfo.toJSONString());
                LOG.info("save scaleIn task result {}", saveTaskResult);
                int scaleInNum = jsonObject.getString("setIps").contains(",") ? jsonObject.getString("setIps").split(",").length : 1;
                BorpClient.saveOpt(UserUtils.getUser(), 3, jsonObject.getString("appkey"), EntityType.manualScaleIn(), "default", "", "TaskId:" + resultJsonObject.getString("rescode") + " 开始一键缩容,缩容:" + scaleInNum + "台");
                taskThreadPool.submit(new KapiService().new TaskRunnable(Integer.parseInt(resultJsonObject.getString("rescode")), -1, jsonObject.getString("appkey"), jsonObject.getString("env")));
                return resultJson;
            } else {
                return RequestError("有扩缩动作未完成,请稍后执行动作");
            }
        } catch (Exception e) {
            LOG.error("scale in use kapiserver ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/scaleout/judge", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String scaleOutJudge(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("request params {}", json);
            JSONObject jsonObject = JSON.parseObject(json);
            if (jsonObject.isEmpty()) {
                return RequestError("请求参数错误");
            }
            String result = HttpService.postRequestToKApi(PolicyConfig.KAPI_URL + "/api/scaleout/judge", json);
            return result;
        } catch (Exception e) {
            LOG.error("check before ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/repository/image/list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getStableImageList(@RequestParam("appkey") String appkey, @RequestParam("env") String env) {
        String url = "";
        try {
            url = String.format("%s/api/repository/image/list?appkey=%s&env=%s", IMAGE_URL, appkey, env);
            String resultStr = HttpService.getRequestToKApi(url);
            LOG.info("get stable image Result {}", resultStr);
            return resultStr;
        } catch (Exception e) {
            LOG.error("get stable images {} ", url, e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/repository/image/stable", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String getStableImage(@RequestParam("appkey") String appkey, @RequestParam("env") String env) {
        String url = "";
        try {
            url = String.format("%s/api/repository/image/stable?appkey=%s&env=%s", IMAGE_URL, appkey, env);
            return HttpService.getRequestToKApi(url);
        } catch (Exception e) {
            LOG.error("get stable image {} ", url, e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/check/isRight", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String checkIsOwner(@RequestParam("appkey") String appkey, @RequestParam("env") String env) {
        try {
            JSONObject jsonObject = new JSONObject();
            String supper_manager = manuConfigMccClient.getValue("hulk2_super_manager");
            if (ServiceCommon.isOwnerLogin(appkey, UserUtils.getUser().getLogin()) || supper_manager.contains(UserUtils.getUser().getLogin())) {
                jsonObject.put("code", 0);
                jsonObject.put("errorInfo", "existed");
                return jsonObject.toJSONString();
            }
            jsonObject.put("code", -1);
            jsonObject.put("errorInfo", "error");
            return "";
        } catch (Exception e) {
            LOG.error("check right {}", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/mcc/manuconfig", method = RequestMethod.GET)
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN)
    public String manuConfig() {
        //从MCC中获取一键扩容相关参数
        try {
            String manuConfigMccParam = manuConfigMccClient.getValue("manual_config_params_hulk");
            LOG.info("manual result {}", manuConfigMccParam);
            Map<String, String> map = new HashMap<>();
            map.put("data", manuConfigMccParam);
            ObjectMapper json = new ObjectMapper();
            return json.writeValueAsString(map);
        } catch (Exception e) {
            LOG.error("manual error {}", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @RequestMapping(value = "/scaleout", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String scaleOut(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("scaleOut param {}", json);
            String result = HttpService.postRequestToKApi(PolicyConfig.KAPI_URL + "/api/scaleout", json);
            LOG.info("scaleOut result from kapi {}", result);
            JSONObject jsonObject = JSON.parseObject(json);
            if (jsonObject.isEmpty()) {
                return RequestError("请求参数错误");
            }
            if (isTaskCanExecute(PolicyConfig.CELLAR_KEY_PREFIX + jsonObject.getString("appkey"))) {
                JSONObject resultJsonObject = JSON.parseObject(result);
                JSONObject policyTaskInfo = kapiService.makePolicyTask(resultJsonObject, jsonObject);
                String saveTaskResult = HttpService.postRequestToBannerApi(PolicyConfig.BANNERAPI_URL + "/api/policy-task-record/records/octo-add", policyTaskInfo.toJSONString());
                LOG.info("save scaleOut task result {}", saveTaskResult);
                BorpClient.saveOpt(UserUtils.getUser(), 1, jsonObject.getString("appkey"), EntityType.manualScaleOut(), "default", "", "TaskId:" + resultJsonObject.getString("rescode") + " 开始一键扩容,扩容:" + jsonObject.getString("num") + "台");
                taskThreadPool.submit(new KapiService().new TaskRunnable(Integer.parseInt(resultJsonObject.getString("rescode")), 1, jsonObject.getString("appkey"), jsonObject.getString("env")));
                return result;
            } else {
                return RequestError("有扩缩动作未完成,请稍后执行动作");
            }
        } catch (Exception e) {
            LOG.error("scaleout error ", e);
            return JsonHelper.errorJson(e.getMessage() == null ? this.getClass().getName() : e.getMessage());
        }
    }

    @Worth(model = Worth.Model.Monitor, function = "查看日志")
    @RequestMapping(value = "operation/{appkey}/log", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String operationLog(@PathVariable(value = "appkey") String appkey,
                               @RequestParam(value = "entityType", required = false) String entityType,
                               @RequestParam(value = "operator", required = false) String operator,
                               @RequestParam(value = "start", required = false) String start,
                               @RequestParam(value = "end", required = false) String end,
                               Page page) {
        String result = kapiService.getOperationLog(appkey, entityType, operator, start, end, page);
        return result;
    }

    public boolean isTaskCanExecute(String key) {
        try {
            if (cache.get(key) == null || cache.get(key) == "" || cache.get(key).equals(key) || cache.get(key).equals("empty")) {
                return true;
            }
        } catch (TairException e) {
            LOG.error("cellar cache error " + e);
        } catch (IOException e) {
            LOG.error("cellar cache error " + e);
        } catch (InterruptedException e) {
            LOG.error("cellar cache error " + e);
        }
        return false;
    }

    public String RequestError(String errorInfo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 400);
        jsonObject.put("errorInfo", errorInfo);
        return jsonObject.toJSONString();
    }
}
