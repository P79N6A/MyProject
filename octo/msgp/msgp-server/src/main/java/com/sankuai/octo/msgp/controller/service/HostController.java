package com.sankuai.octo.msgp.controller.service;

import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.service.ServiceHost;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by lhmily on 11/25/2015.
 */
@RequestMapping("/serverOpt/")
@Controller
@Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
@Worth(model = Worth.Model.MNS)
public class HostController {
    private static final Logger LOG = LoggerFactory.getLogger(HostController.class);

    @Worth(model = Worth.Model.MNS, function = "查询节点环境")
    @RequestMapping(value = "host/hostInfo", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getHostInfo(@RequestParam("ip") String ip) {
        try {
            return JsonHelper.dataJson(ServiceHost.getHostInfo(ip));
        } catch (Exception e) {
            LOG.error("ip查询失败,"+ip,e);
            return JsonHelper.errorJson("无法查询到该主机的相关信息");
        }
    }

    @RequestMapping(value = "host/hostname2ip", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hostname2ip(@RequestParam(value = "hostname", required = false) String hostname) {
        String ip = OpsService.host2ip(hostname);
        if(ip.startsWith("10.")){
            return JsonHelper.dataJson(ip);
        }else{
            return JsonHelper.dataJson("健康检查失败，该主机不在服务树上。");
        }
    }
}
