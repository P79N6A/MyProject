package com.sankuai.octo.msgp.controller.provider;

import com.sankuai.octo.msgp.domain.CapacityResult;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.msgp.service.ptest.PtestApiService;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/provider")
@Worth(model = Worth.Model.MNS)
@Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
public class ProviderController {

    /**
     * 获取pest 压测的服务容量
     */
    @RequestMapping(value = "capacity/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public CapacityResult getCapacity(@RequestParam(value = "appkey") String appkey) {
        return PtestApiService.getCapacity(appkey);
    }

    /**
     * 获取服务节点数量
     * status 默认值为-1
     */
    @RequestMapping(value = "count", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public int count(@RequestParam(value = "appkey") String appkey,
                     @RequestParam(value = "env", required = false, defaultValue = "prod") String env,
                     @RequestParam(value = "type", defaultValue = "1") Integer type,
                     @RequestParam(value = "status", defaultValue = "-1") Integer status) {
        return AppkeyProviderService.getProviderNodeCountBy(appkey,env,status,type);
    }

}
