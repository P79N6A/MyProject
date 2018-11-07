package com.sankuai.octo.mnsc.web.api;

import com.sankuai.octo.mnsc.idl.thrift.model.*;
import com.sankuai.octo.mnsc.model.Env;
import com.sankuai.octo.mnsc.service.apiService;
import com.sankuai.octo.mnsc.service.mnscService;
import com.sankuai.octo.mnsc.utils.api;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by lhmily on 12/22/2016.
 * only for test
 */
@Controller
@RequestMapping("/api/test")
public class TestController {

    @RequestMapping(value = "/businessline/appkeys", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getAppKeyListByBusinessLine(@RequestParam("bizcode") int bizCode,
                                              @RequestParam("env") String env) {

        if (!Env.isValid(env)) {
            return api.errorJson(Constants.ILLEGAL_ARGUMENT, "illegal argument.");
        }
        return api.dataJson(mnscService.getAppKeyListByBusinessLine(bizCode, env, false));
    }

    @RequestMapping(value = "/businessline/httpproperties", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getHttpPropertiesByBusinessLine(@RequestParam("bizcode") int bizCode,
                                                  @RequestParam("env") String env) {

        if (!Env.isValid(env)) {
            return api.errorJson(Constants.ILLEGAL_ARGUMENT, "illegal argument.");
        }
        return api.dataJson(mnscService.getHttpPropertiesByBusinessLine(bizCode, env));
    }

    @RequestMapping(value = "/mnscache/versioncheck", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getMNSCacheWithVersionCheck(@RequestParam("appkey") String appkey,
                                              @RequestParam("env") String env,
                                              @RequestParam("protocol") String protocol) {
        MnsRequest mnsRequest = new MnsRequest();
        mnsRequest.setAppkey(appkey)
                .setProtoctol(com.sankuai.octo.mnsc.idl.thrift.model.Protocols.valueOf(protocol))
                .setEnv(env);

        if (StringUtils.isEmpty(mnsRequest.getAppkey()) || !Env.isValid(mnsRequest.getEnv())) {
            return api.errorJson(Constants.ILLEGAL_ARGUMENT,"illegal argument.");
        }
        return api.dataJson(mnscService.getMnsc(mnsRequest));
    }

    @RequestMapping(value = "/allgroups", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getMNSCacheWithVersionCheck(@RequestParam("env") String env) {
        return api.dataJson(mnscService.getAllGroups(env));
    }
}
