package com.sankuai.octo.msgp.controller.service;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.AppkeyAuth;
import com.sankuai.octo.msgp.domain.AppkeyWhiteList;
import com.sankuai.octo.msgp.serivce.service.ServiceAuth;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by yves on 17/2/9.
 * 服务鉴权
 */

@Controller
@RequestMapping("/serverOpt/auth")
@Worth(model = Worth.Model.MNS)
public class ServiceAuthController {


    @RequestMapping(value = "/appkey/whitelist/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取服务鉴权白名单")
    @ResponseBody
    public String getAppkeyWhiteList(@RequestParam(value = "appkey", required = true) String appkey,
                                     @RequestParam(value = "env", required = true) String env) {
        return ServiceAuth.getAppkeyWhiteList(appkey, env);
    }

    /**
     * 注意: 当whitelist为空列表时,spring会将其转为null,因此设置一个默认值
     */
    @RequestMapping(value = "/appkey/whitelist/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "更新服务鉴权白名单")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String updateAppkeyWhiteList(@RequestBody AppkeyWhiteList appkeyWhiteList) {
        return ServiceAuth.updateAppkeyWhiteList(appkeyWhiteList.getAppkey(), appkeyWhiteList.getWhitelist(), appkeyWhiteList.getEnv());
    }

    @RequestMapping(value = "/appkey/auth/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取服务鉴权数据")
    @ResponseBody
    public String getAppkeyAuthList(@RequestParam(value = "appkey") String appkey,
                                    @RequestParam(value = "env", required = true) String env) {
        return ServiceAuth.getAppkeyAuthList(appkey, env);
    }


    @RequestMapping(value = "/appkey/auth/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "更新服务鉴权数据")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String updateAppkeyAuthList(@RequestBody AppkeyAuth appkeyAuthList) {
        return ServiceAuth.updateAppkeyAuthList(appkeyAuthList.getAppkey(), appkeyAuthList.getAllAuthList(), appkeyAuthList.getEnv());
    }


    @RequestMapping(value = "/span/auth/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取接口鉴权数据")
    @ResponseBody
    public String getSpanAuthList(@RequestParam(value = "appkey") String appkey,
                                  @RequestParam(value = "env", required = true) String env) {
        return ServiceAuth.getSpanAuthList(appkey, env);
    }


    @RequestMapping(value = "/span/auth/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "更新接口鉴权数据")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String updateSpanAuthList(@RequestBody String json) {
        return ServiceAuth.updateSpanAuthList(json);
    }


    @RequestMapping(value = "/apps/all", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取服务鉴权Appkey")
    @ResponseBody
    public String getAllAppkeys() {
        return JsonHelper.jsonStr(ServiceAuth.getAllAppkeys());
    }


    @RequestMapping(value = "/apps/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取服务鉴权Appkey")
    @ResponseBody
    public String searchAppkey(@RequestParam(value = "keyword") String keyword) {
        return JsonHelper.jsonStr(ServiceAuth.searchAppkey(keyword));
    }
}
