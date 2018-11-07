package com.sankuai.octo.mnsc.web.api;

import com.sankuai.inf.octo.mns.util.IpUtil;
import com.sankuai.octo.idc.model.Idc;
import com.sankuai.octo.mnsc.idl.thrift.model.AppKeyListResponse;
import com.sankuai.octo.mnsc.model.Appkeys;
import com.sankuai.octo.mnsc.model.Env;
import com.sankuai.octo.mnsc.service.apiService;
import com.sankuai.octo.mnsc.service.mnscService;
import com.sankuai.octo.mnsc.util.IdcXml;
import com.sankuai.octo.mnsc.utils.api;
import com.sankuai.octo.mnsc.utils.ipCommon;
import com.sankuai.octo.mnsc.utils.mnscCommon;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lhmily on 01/12/2016.
 */
@Controller
@RequestMapping("/api")
public class ApiController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);
    private static final String newSgSentinel = "com.sankuai.inf.sgsentinel";
    private static final String oldSgSentinel = "com.sankuai.inf.sg_sentinel";

    @RequestMapping(value = "/monitor/alive")
    @ResponseBody
    public Map<String, Object> monitorAlive() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "ok");
        return result;
    }

    @RequestMapping(value = "/servicelist", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getServiceList(@RequestParam("appkey") String appkey,
                                 @RequestParam("env") String env,
                                 @RequestParam("host") String host,
                                 @RequestParam("hostname") String hostname,
                                 @RequestParam("ip") String ip) {
        //all parameters are required.
        try {
            if (!(newSgSentinel.equals(appkey) || oldSgSentinel.equals(appkey))) {
                return api.errorJson(400, "appkey currently only supports com.sankuai.inf.sgsentinel or com.sankuai.inf.sg_sentinel");

            }

            if (StringUtils.isEmpty(ip) || !(ipCommon.checkIP(ip.trim()))) {
                return api.errorJson(400, "ip invalid");
            }
            if (!Env.isValid(env.trim())) {
                return api.errorJson(400, "env invalid");
            }

            return apiService.getServiceList(appkey, env.trim(), host.trim(), hostname.trim(), ip.trim());
        } catch (Exception e) {
            LOG.error("/api/servicelist error ", e);
            return api.errorJson(500, "server error");
        }
    }

    @RequestMapping(value = "/sgagentlist", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getSgagentList(@RequestParam("ip") String ip) {
        try {
            return ipCommon.checkIP(ip) ? apiService.getSgagentList(ip) : api.errorJson(400, "invalid ip");
        } catch (Exception e) {
            LOG.error("/api/sgagentlist error ", e);
            return api.errorJson(500, "server error");
        }
    }

    @RequestMapping(value = "/idcinfo", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getIdcInfo(@RequestParam(value = "ips") List<String> ips) {
        Map<String, Idc> idcs = IpUtil.getIdcInfoFromLocal(ips);
        return api.dataJson(200, "success", idcs);
    }

    @RequestMapping(value = "/idcxml", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getIdcXml() {
        if (IdcXml.isIdcXmlValid()) {
            List<IdcXml.IDC> idcs = IdcXml.getIdcCache();
            return api.dataJson(200, "success", idcs);
        } else {
            return api.errorJson(500, "fail to get idc.xml");
        }
    }

    // 获取有服务节点的appkey列表
    @RequestMapping(value = "/appkeylist", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getAppkeyList(@RequestParam("env") String env,
                                @RequestParam("protocol") String protocol) {
        //all parameters are required.
        try {
            if (StringUtils.isEmpty(protocol)) {
                return api.errorJson(400, "protocol is not allowed to empty");
            }

            if (!Env.isValid(env.trim())) {
                return api.errorJson(400, "env invalid");
            }

            return mnscService.getAppkeysWithProviders(env, protocol);
        } catch (Exception e) {
            LOG.error("/api/servicelist error ", e);
            return api.errorJson(500, "server error");
        }
    }

    @RequestMapping(value = "/{ip}/appkeylist", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getAppkeyList(@PathVariable("ip") String ip) {
        AppKeyListResponse ret = new AppKeyListResponse();
        if (StringUtils.isEmpty(ip)) {
            ret.setCode(400);
        } else {
            List<String> appkeys = mnscService.getAppkeyListByIP(ip.trim());
            ret.setCode(200).setAppKeyList(appkeys);
        }
        return api.dataJson(ret.getCode(), ret.getAppKeyList());
    }


    @RequestMapping(value = "/allappkeys", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getAppkeys() {
        return api.dataJson200(mnscCommon.allAppkeysList());
    }
}
