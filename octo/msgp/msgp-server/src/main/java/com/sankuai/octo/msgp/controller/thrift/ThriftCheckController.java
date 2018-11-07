package com.sankuai.octo.msgp.controller.thrift;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.service.thrift.ThriftCheckService;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.msgp.utils.ResultData;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/3/9
 */
@Controller
@RequestMapping("/thriftHttpSvc")
public class ThriftCheckController {

    @Autowired
    private ThriftCheckService checkService;

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.OTHER, function = "Mtthrift自检")
    @RequestMapping(value = "httpCheck", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String httpCheck(String appkey, String host, String port, String role, String checkType) {
        ResultData<String> result = checkService.httpCheck(appkey, host, port, role, checkType);
        if (result.isSuccess()) {
            return JsonHelper.dataJson(result.getData());
        } else {
            return JsonHelper.errorJson(result.getMsg());
        }
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.OTHER, function = "Mtthrift接口调用")
    @RequestMapping(value = "httpInvoke", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String httpInvoke(String appkey, String host, String port, String serviceName, String method, String params, HttpServletRequest request) {
        ResultData<String> result = checkService.httpInvoke(appkey, host, port, serviceName, method, params);
        if (result.isSuccess()) {
            return JsonHelper.dataJson(result.getData());
        } else {
            return JsonHelper.errorJson(result.getMsg());
        }
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "serverNode/get", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public String getServerNodesForHttpInvoke(String appkey, String env) {
        ResultData<Map<String, String>> result = checkService.getServerNodesForHttpInvoke(appkey, env);
        if (result.isSuccess()) {
            return JsonHelper.dataJson(result.getData());
        } else {
            return JsonHelper.errorJson(result.getMsg());
        }
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "serviceMethods/get", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public String getServiceMethods(String appkey, String host, String port) {
        ResultData<Map<String, Map<String, Integer>>> result = checkService.getServiceMethods(appkey, host, port);
        if (result.isSuccess()) {
            return JsonHelper.dataJson(result.getData());
        } else {
            return JsonHelper.errorJson(result.getMsg());
        }
    }

}
