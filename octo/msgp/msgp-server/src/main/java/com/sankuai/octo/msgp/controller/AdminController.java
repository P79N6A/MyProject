package com.sankuai.octo.msgp.controller;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.serivce.service.ServiceHost;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@Worth(model = Worth.Model.OTHER)
@RequestMapping("/admin")
public class AdminController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @RequestMapping(value = "correctnesscheck/http", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String correctnessCheck4Http(HttpServletRequest request) {
        try {
            ServiceCommon.correctnessCheck4Http();
            return JsonHelper.dataJson(true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "stat", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String stat() {
        try {
            ServiceHost.appHosts();
            return JsonHelper.dataJson(true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
}
