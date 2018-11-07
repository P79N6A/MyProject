package com.sankuai.octo.msgp.controller.hulk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyConfig;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.service.hulk.HttpService;
import com.sankuai.octo.msgp.service.hulk.KapiService;
import com.sankuai.octo.msgp.service.hulk.MccHulkService;
import com.sankuai.octo.msgp.utils.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Jsong on 2018/7/29.
 */

@Controller
@RequestMapping("/hulk2-auth")
public class AuthorizationController {

    /**
     * 判断不同owt服务,是否有权限接入弹性2.0
     * 判断服务负责人,增加超级管理员权限
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationController.class);

    @Autowired
    private KapiService kapiService;

    @RequestMapping(value = "/check/hulk-allow", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String checkIsAccepted(@RequestParam("appkey") String appkey) {
        String result = kapiService.checkIsAccepted(appkey);
        return result;
    }

    @RequestMapping(value = "/check/isOwner", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String checkIsOwner(@RequestParam("appkey") String appkey) {
        String result = kapiService.checkIsOwner(appkey);
        return result;
    }

    @RequestMapping(value = "/check/isConf", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String checkIsConf(@RequestParam("appkey") String appkey, @RequestParam("env") String env) {
        String result = kapiService.checkIsConf(appkey, env);
        return result;
    }

}
