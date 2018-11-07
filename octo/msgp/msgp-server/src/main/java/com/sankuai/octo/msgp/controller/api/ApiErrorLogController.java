package com.sankuai.octo.msgp.controller.api;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.errorlog.service.LogService;
import com.sankuai.octo.msgp.utils.ResultData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/2/7
 */
@Controller
@RequestMapping("/api/errorlog")
public class ApiErrorLogController {
    @Resource
    private LogService logService;

    /**
     * 给日志中心提供的需要启动解析的 appkey
     * @return
     */
    @RequestMapping(value = "/service_status/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getRouteConfig(String day) {
        ResultData<List<String>> result = logService.getErrorLogStartAppkey(day);
        if (!result.isSuccess()) {
            return JsonHelper.errorJson(result.getMsg());
        }
        return JsonHelper.dataJson(result.getData());
    }
}
