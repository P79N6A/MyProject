package com.sankuai.octo.msgp.controller.service;

import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/checker")
@Worth(model = Worth.Model.OTHER)
public class UserCheckController {
    private static final Logger LOG = LoggerFactory.getLogger(UserCheckController.class);

    @Worth(model = Worth.Model.MCC, function = "查看日志")
    @RequestMapping(value = "userCheck", method = RequestMethod.GET)
    public String userCheck(@RequestParam(value = "appkey", required = false) String appkey, Model model) {
        List<String> apps = ServiceCommon.appsByUser();
        model.addAttribute("apps", ServiceCommon.apps());
        appkey = StringUtils.isEmpty(appkey) ? (apps.isEmpty() ? "" : apps.get(0)) : appkey;
        model.addAttribute("appkey", appkey);

        Boolean isOffline = CommonHelper.isOffline();
        String hostUrl = isOffline ? "http://octo.test.sankuai.com" : "http://octo.sankuai.com";
        String xmdUrl = isOffline ? "http://log.inf.dev.sankuai.com" : "http://xmdlog.sankuai.com";
        model.addAttribute("xmdUrl", xmdUrl);
        model.addAttribute("hostUrl", hostUrl);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "checker/userCheck";
    }
}
