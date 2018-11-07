package com.sankuai.octo.mnsc.web.api;

import com.sankuai.octo.mnsc.dataCache.appProviderDataCache;
import com.sankuai.octo.mnsc.dataCache.appProviderHttpDataCache;
import com.sankuai.octo.mnsc.model.Env;
import com.sankuai.octo.mnsc.service.apiProviders;
import com.sankuai.octo.mnsc.utils.api;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import play.libs.Json;
import scala.collection.JavaConversions;
import scala.collection.immutable.Map;

import java.util.List;


@Controller
@RequestMapping("/api/providers")
public class ProvidersController {
    private static final Logger LOG = LoggerFactory.getLogger(ProvidersController.class);

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getProviders(@RequestParam("appkey") String appkey,
                               @RequestParam("env") int env,
                               @RequestParam("protocol") String protocol) {
        try {
            if (StringUtils.isEmpty(appkey)) {
                return api.errorJson(400, "appkey is not allowed to empty");
            }

            if (!Env.isValid(env)) {
                return api.errorJson(400, "env invalid");
            }

            if (StringUtils.isEmpty(protocol)) {
                return api.errorJson(400, "protocol is not allowed to empty");
            }

            if ("thrift".equalsIgnoreCase(protocol)) {
                return api.dataJson(appProviderDataCache.getProviderCache(appkey, Env.apply(env).toString(),false));
            } else if ("http".equalsIgnoreCase(protocol)) {
                return api.dataJson(appProviderHttpDataCache.getProviderHttpCache(appkey, Env.apply(env).toString(),false));
            } else {
                return apiProviders.getProviders(appkey.trim(), env, protocol);
            }
        } catch (Exception e) {
            LOG.error("/api/providers error ", e);
            return api.errorJson(500, "server error");
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String postProviders(@RequestBody List<SGService> providers) {
        return api.jsonStr(apiProviders.postProviders(providers));
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public String deleteProviders(@RequestBody List<SGService> providers) {
        return api.jsonStr(apiProviders.deleteProviders(providers));
    }
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String deletePostProviders(@RequestBody List<SGService> providers) {
        // client may be able to delete the nodes because the jdk bug, so add a new API. more detail see: http://bugs.java.com/view_bug.do?bug_id=8148558
        return api.jsonStr(apiProviders.deleteProviders(providers));
    }
}
