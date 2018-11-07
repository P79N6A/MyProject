package com.sankuai.octo.msgp.controller.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dianping.rhino.Rhino;
import com.dianping.rhino.limit.RequestLimiter;
import com.sankuai.mms.util.Log;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.service.org.UserService;
import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.*;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import com.sankuai.octo.msgp.serivce.data.DataQuery;
import com.sankuai.octo.msgp.serivce.service.*;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceDailyReport;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceReport;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.joda.time.DateTime;
import com.sankuai.meituan.common.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import scala.collection.JavaConversions;
import com.sankuai.octo.msgp.domain.SreUser;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/api/service")
public class ApiServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceController.class);

    //http://rhino.inf.dev.sankuai.com/ 限流配置单机 3次/s
    private static RequestLimiter registryRequestLimiter = Rhino.newRequestLimiter("octo_registry_limit");

    /**
     * 注册服务
     *
     * @param request
     * @return
     */
    @Worth(model = Worth.Model.MNS, function = "服务注册")
    @RequestMapping(value = "/registry", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String registry(@RequestBody AppkeyReg appkeyReg, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        String result = "";
        try {
            if (registryRequestLimiter.tryAcquire()) {
                if ((appkeyReg.getData().getOwners().size() < ServiceCommon.OWNER_LOWER_LIMIT() || appkeyReg.getData().getOwners().size() > ServiceCommon.OWNER_UPPER_LIMIT()) && !CommonHelper.isOffline()) {
                    result = JsonHelper.errorJson("服务负责人数量介于2～12人之间，请确认下人数");
                } else if (appkeyReg.getData().getOwners().size() < ServiceCommon.OWNER_LOWER_LIMIT() && CommonHelper.isOffline()) {
                    result = JsonHelper.errorJson("服务负责人数量大于2人，请确认下人数");
                } else {
                    result = ServiceCommon.saveService(appkeyReg, request.getCookies());
                }
            } else {
                LOG.info("registry limit! {}", appkeyReg);
                return JsonHelper.errorJson("frequency limit please try again later!");
            }

        } catch (Exception e) {
            LOG.error("注册服务失败: " + appkeyReg.toString(), e);
            result = JsonHelper.errorJson("注册失败");
        } finally {
            LOG.info("注册服务耗时: " + (System.currentTimeMillis() - start) + "," + appkeyReg.toString());
        }
        return result;
    }

    @Worth(model = Worth.Model.MNS, function = "修改用户")
    @RequestMapping(value = "/updateuser", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateUser(@RequestBody AppkeyUser appkeyUser) {
        try {
            LOG.info("api修改用户:" + appkeyUser.toString());
            if ((appkeyUser.getOwners().size() < ServiceCommon.OWNER_LOWER_LIMIT() || appkeyUser.getOwners().size() > ServiceCommon.OWNER_UPPER_LIMIT()) && !CommonHelper.isOffline()) {
                return JsonHelper.errorJson("修改负责人数量介于2～12人之间，请确认人数");
            } else if (appkeyUser.getOwners().size() < ServiceCommon.OWNER_LOWER_LIMIT() && CommonHelper.isOffline()) {
                return JsonHelper.errorJson("服务负责人数量大于2人，请确认下人数");
            } else {
                return ServiceCommon.updateServiceUser(appkeyUser);
            }
        } catch (Exception e) {
            LOG.error("保存服务负责人失败", e);
            return JsonHelper.errorJson("保存失败");
        }
    }

    /**
     * @param
     * @return
     * @description 运维系统调用，根据usermisId获取appkey及负责人信息
     * @author zhanghui24
     * @date Created in 23:44 2018/5/15
     */
    @RequestMapping(value = "sre/getUserData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String sreGetUser(@RequestParam(value = "user", required = true) String user) {
        List<SreUser> userList = new ArrayList<SreUser>();
        SreUserData userData = new SreUserData();
        Exception errorMessage = null;
        try {
            List<String> appkeyList = ServiceFilter.serviceByOwner(user);
            for (String appkey : appkeyList) {
                userList.add(new SreUser(JavaConversions.asJavaList(ServiceCommon.getOwnersLoginWhole(appkey)), "https://octo.sankuai.com/service/detail?appkey=" + appkey + "&type=1&env=stage&status=-1&keyword=#outline", appkey, "auditor", "负责人"));
            }

        } catch (Exception e) {
            LOG.error("sre获取用户信息失败", e);
            errorMessage = e;
        }
        return JsonHelper.errorDataJson(userList, errorMessage);
    }

    /**
     * @param
     * @return
     * @description 运维系统调用, 根据appkey信息更新服务负责人
     * @author zhanghui24
     * @date Created in 23:33 2018/5/15
     */
    @RequestMapping(value = "sre/updateUser", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String sreUpdateUser(HttpServletRequest requestBody) throws Exception {
        Exception error = null;
        String jsonString = null;
        try {
            jsonString = IOUtils.copyToString(requestBody.getReader());
            JSONObject postData = JSONObject.parseObject(jsonString);
            if (postData != null && !postData.isEmpty()) {
                String userData = postData.getString("data");
                List<SreUser> users = JSON.parseObject(userData, new TypeReference<List<SreUser>>() {
                });
                for (SreUser user : users) {
                    if (user.getCurrent_users().containsAll(user.getNew_users()) && user.getNew_users().containsAll(user.getCurrent_users())) {
                        continue;
                    } else {
                        ServiceCommon.updateServiceUser(new AppkeyUser("", user.getService(), user.getNew_users()));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("sre post请求更新服务负责人失败", e);
            error = e;
        }
        return JsonHelper.errorDataJson(jsonString, error);

    }

    @Worth(model = Worth.Model.MNS, function = "删除离职员工")
    @RequestMapping(value = "/removeLeftUser", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String removeLeftUsers(@RequestBody AppkeyUser appkeyUser) {
        try {
            LOG.info("api删除离职员工服务负责人:" + appkeyUser.toString());
            return ServiceCommon.removeLeftUsers(appkeyUser);
        } catch (Exception e) {
            LOG.error("删除离职服务负责人失败!");
            return JsonHelper.errorJson("删除离职服务负责人失败");
        }
    }

    /**
     * @param appkey
     * @return
     * @desc 判定服务是否存在
     */

    @RequestMapping(value = "/{appkey}/exist", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String checkServiceExist(@PathVariable("appkey") String appkey) {
        Boolean exist = ServiceCommon.exist(appkey);
        return JsonHelper.dataJson(exist);
    }

    /**
     * @param appkey
     * @param login
     * @return
     * @desc 删除服务
     */
    @RequestMapping(value = "/{appkey:.+}", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String registryService(@PathVariable("appkey") String appkey,
                                  @RequestParam(value = "login", required = true) String login) {
        if (AppkeyAuth.hasAdminAuth(appkey, login)) {
            if (!ServiceCommon.exist(appkey)) {
                return JsonHelper.dataJson("已经删除");
            } else {
                return ServiceDesc.delete(appkey, login);
            }
        } else {
            return JsonHelper.errorJson("没有权限");
        }
    }

    /**
     * @param appkey
     * @param login
     * @return
     * @desc 删除备用方法，如果上面的方法406的话 尝试调用下面的方法
     */
    @RequestMapping(value = "/deleteApp", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteAppkeyBackup(@RequestParam(value = "appkey", required = true) String appkey,
                                     @RequestParam(value = "login", required = true) String login) {
        if (AppkeyAuth.hasAdminAuth(appkey, login)) {
            if (!ServiceCommon.exist(appkey)) {
                return JsonHelper.dataJson("已经删除");
            } else {
                return ServiceDesc.delete(appkey, login);
            }
        } else {
            return JsonHelper.errorJson("没有权限");
        }
    }

    /**
     * 获取服务描述信息
     *
     * @param appkey
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppkeyDesc2(@RequestParam(value = "appkey", required = true) String appkey) {
        ServiceModels.DescRich descRich = ServiceCommon.apiDesc(appkey).toRich();
        return JsonHelper.dataJson(descRich);
    }

    @Deprecated
    @RequestMapping(value = "/{appkey:.+}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppkeyDesc(@PathVariable("appkey") String appkey) {
        ServiceModels.DescRich descRich = ServiceCommon.apiDesc(appkey).toRich();
        return JsonHelper.dataJson(descRich);
    }


    /**
     * 获取服务描述信息
     *
     * @param appkey
     * @return
     */
    @RequestMapping(value = "/spanname", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String spanname(@RequestParam("appkey") String appkey,
                           @RequestParam(value = "env", required = false, defaultValue = "prod") String env) {
        DataQuery.RemoteSpannameData remoteSpannameData = DataQuery.getAppRemoteAppkey(appkey, env, "server");
        return JsonHelper.dataJson(remoteSpannameData.spannames());
    }

    /**
     * 获取服务描述信息
     *
     * @param owt
     * @param pdl
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String search(
            @RequestParam(value = "owt", required = false) String owt,
            @RequestParam(value = "pdl", required = false) String pdl
    ) {
        return JsonHelper.dataJson(ServiceFilter.serviceByOwtPdl(owt, pdl, new Page(1, 10000)));
    }


    @RequestMapping(value = "/regLimit", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getRegisterationLimited() {
        return JsonHelper.dataJson(ServiceDesc.getRegisterationLimited());
    }

    /**
     * 依赖统计
     */
    @RequestMapping(value = "/depends", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String depend(@RequestParam(value = "appkey") String appkey,
                         @RequestParam(value = "env", required = false, defaultValue = "prod") String env,
                         @RequestParam(value = "source", required = false, defaultValue = "server") String source
    ) {
        return JsonHelper.dataJson(DataQuery.getAppRemoteAppkey(appkey, env, source).remoteAppKeys());
    }

    /**
     * 依赖统计
     */
    @RequestMapping(value = "depend", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String depend(@RequestParam(value = "appkey") String appkey,
                         @RequestParam(value = "day") String day,
                         @RequestParam(value = "type", defaultValue = "true") boolean type
    ) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getDepend(appkey, type, date));
    }

    /**
     * 服务分组
     */

    @RequestMapping(value = "group", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String group(@RequestBody AppkeyGroup appkeyGroup) {
        UserService.bindUser(appkeyGroup.getUsername());
        return ServiceGroup.saveGroup(appkeyGroup);
    }

    @RequestMapping(value = "group/delete", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deletegroup(@RequestParam String appkey, @RequestParam String id, @RequestParam String username) {
        UserService.bindUser(username);
        ServiceGroup.deleteGroup(appkey, id);
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "http/loadBalanceConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getLoadBalanceConfig(@RequestParam(value = "appkey", required = true) String appkey,
                                       @RequestParam(value = "env", required = true) Integer env) {
        try {
            ServiceModels.LoadBalanceConfig config = ServiceHttpConfig.getLoadBalanceConfig(appkey, env);
            LOG.info("get http loadBalanceConfig appkey={},env={},config={}", appkey, env, config);
            return JsonHelper.dataJson(config);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "http/loadBalanceConfig", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateLoadBalanceConfig(@RequestParam("appkey") String appkey,
                                          @RequestParam(value = "env", required = true, defaultValue = "1") Integer env,
                                          @RequestParam(value = "load_balance_type", required = true) String load_balance_type,
                                          @RequestParam(value = "load_balance_value", required = false, defaultValue = "") String load_balance_value,
                                          @RequestParam(value = "username", required = true) String username) {
        try {
            String config = JsonHelper.jsonStr(new ServiceModels.LoadBalanceConfig(appkey, load_balance_type, load_balance_value, null, null, null));
            String response = ServiceHttpConfig.updateLoadBalanceConfig(appkey, env, config);
            LOG.info("loadBalanceConfig put response: " + response + "username: " + username);
            return JsonHelper.dataJson("update balance config successfully.");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    @RequestMapping(value = "http/healthCheckConfig", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateHealthCheckConfig(@RequestParam("appkey") String appkey,
                                          @RequestParam(value = "env", required = false) Integer env,
                                          @RequestParam(value = "data", required = false) String data) {
        LOG.info("healthCheckConfig put appkey: " + appkey + " env: " + env + " data: " + data);
        try {
            return ServiceHttpConfig.addHealthCheckConfig(appkey, env == null ? 1 : env, data); // 默认为test环境
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @Worth(model = Worth.Model.ROUTE, function = "查看分组")
    @RequestMapping(value = "{appkey}/group", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String group(@PathVariable("appkey") String appkey,
                        @RequestParam(value = "env", required = false, defaultValue = "0") Integer env,
                        Page page) {
        scala.collection.immutable.List<ServiceModels.Group> list = ServiceGroup.group(appkey, env, page);
        return JsonHelper.dataJson(list, page);
    }

    /**
     * 获取httpcheck接口
     *
     * @param appkey
     * @param env
     * @return
     */
    @RequestMapping(value = "{appkey}/healthCheckConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String healthCheckConfig(@PathVariable("appkey") String appkey,
                                    @RequestParam(value = "env", required = false) Integer env) {
        LOG.info("healthCheckConfig get appkey: " + appkey + " " + " env: " + env);
        ServiceModels.HealthCheckConfig data = ServiceHttpConfig.getHealthCheckConfig(appkey, env == null ? 1 : env);  // 如果不提供环境参数，默认为test环境
        String response = JsonHelper.dataJson(data);
        LOG.info("healthCheckConfig get response: " + response);
        return response;
    }


    /**
     * 查询拥有服务性能数据的appkey
     *
     * @return
     */
    @RequestMapping(value = "haskpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String haskpi() {
        try {
            return JsonHelper.dataJson(ServiceDailyReport.getAllKpiAppkey());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    private DateTime getDate(String day) {
        long time = System.currentTimeMillis();
        if (StringUtil.isNotBlank(day)) {
            time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime();
        }
        DateTime date = new DateTime(time);
        return date.withDayOfWeek(1);
    }

}
