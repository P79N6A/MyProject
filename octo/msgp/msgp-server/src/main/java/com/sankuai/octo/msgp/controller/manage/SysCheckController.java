package com.sankuai.octo.msgp.controller.manage;

import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.manage.MsgpChecker;
import com.sankuai.octo.msgp.serivce.manage.ScannerChecker;
import com.sankuai.octo.msgp.serivce.manage.ThriftChecker;
import com.sankuai.octo.msgp.serivce.service.ServiceConfig;
import com.sankuai.octo.msgp.serivce.sgagent.SgAgentChecker;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manage")
public class SysCheckController {

    private final static String systemSelfCheckFtlDir = "manage/systemSelfCheck/";

    @RequestMapping(value = "octoSelfCheck", method = RequestMethod.GET)
    @Worth(model = Worth.Model.Monitor, function = "查看扫描日志")
    public String octoSelfCheck(@RequestParam(value = "appkey", required = false) String appkey) {
        return systemSelfCheckFtlDir + "octoSelfCheck";
    }

    @RequestMapping(value = "{appkey}/sgAgentSelfCheck", method = RequestMethod.GET)
    @Worth(model = Worth.Model.Monitor, function = "查看扫描日志")
    @ResponseBody
    public String sgAgentSelfCheck(@PathVariable(value = "appkey") String appkey, @RequestParam(value = "envId") int envId,
                                   @RequestParam(value = "region", required = false) String region) {
        if (StringUtils.isEmpty(region)) {
            region = "all";
        }
        return JsonHelper.dataJson(SgAgentChecker.SGAVersionCheck(appkey, envId, region));
    }

    @RequestMapping(value = "{appkey}/sgAgentProvide", method = RequestMethod.GET)
    @Worth(model = Worth.Model.Monitor, function = "查看扫描日志")
    public String sgAgentProvide(@PathVariable(value = "appkey") String appkey,
                                 @RequestParam(value = "version") String version,
                                 @RequestParam(value = "envId") int envId,
                                 @RequestParam("thrifttype") String thrifttype,
                                 @RequestParam(value ="region", required = false) String region,
                                 Model model) {
        model.addAttribute("appkey", appkey);
        model.addAttribute("version", version);
        model.addAttribute("envId", envId);
        model.addAttribute("thrifttype", thrifttype);
        model.addAttribute("region", region);

        return systemSelfCheckFtlDir + "sgAgentProvide";
    }

    @RequestMapping(value = "{appkey}/provideGroupByVersion", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String provideGroupByVersion(@PathVariable(value = "appkey") String appkey,
                                        @RequestParam(value = "envId") int envId,
                                        @RequestParam(value = "version") String version,
                                        @RequestParam(value = "region", required = false) String region) {
        return JsonHelper.dataJson(SgAgentChecker.provideGroupByVersion(appkey, envId, version, region));
    }

    @RequestMapping(value = "{appkey}/msgpSelfCheck", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String msgpSelfCheck(@PathVariable(value = "appkey") String appkey) {
        return JsonHelper.dataJson(MsgpChecker.scheduleCheck());
    }

    @RequestMapping(value = "route/checker", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String routeCheck(@RequestParam(value = "envId") int envId) {
        return JsonHelper.dataJson(MsgpChecker.routeCheck(envId));
    }

    @RequestMapping(value = "/thriftSelfCheck", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String cthriftSelfCheck(@RequestParam(value = "business") int business,
                                   @RequestParam(value = "owt") String owt,
                                   @RequestParam(value = "pdl") String pdl) {
        return JsonHelper.dataJson(ThriftChecker.getThriftVersionCount(business, owt, pdl));
    }

    @RequestMapping(value = "octoProvide", method = RequestMethod.GET)
    public String octoProvide(@RequestParam(value = "version") String version,
                              @RequestParam(value = "envId") int envId,
                              @RequestParam("thrifttype") String thrifttype,
                              Model model) {
        model.addAttribute("version", version);
        model.addAttribute("envId", envId);
        model.addAttribute("thrifttype", thrifttype);
        return systemSelfCheckFtlDir + "providerByVersion";
    }

    @RequestMapping(value = "provideBythriftVersion", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String provideBythriftVersion(@RequestParam(value = "version") String version,
                                         @RequestParam("thrifttype") String thrifttype) {
        return JsonHelper.dataJson(ThriftChecker.provideGroupByVersion(thrifttype, version));
    }

    @RequestMapping(value = "job/check", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String octoJob(@RequestParam(value = "appkeys") List<String> appkeys) {
        return JsonHelper.dataJson(ScannerChecker.getJobLog(appkeys));
    }

    @RequestMapping(value = "action/check", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String actionCheck(Page page) {
        return JsonHelper.dataJson(ScannerChecker.getBorpLog(page), page);
    }

    @RequestMapping(value = "scanner/availability", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String scannerAvailability() {
        return JsonHelper.dataJson(ScannerChecker.getScannerAvailability());
    }

    @RequestMapping(value = "scanner/log", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String scannerLogCheck(Page page) {
        return JsonHelper.dataJson(ScannerChecker.getSLog(page), page);
    }

    @RequestMapping(value = "mcc/statistic/file", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String mccStatisticFile() {
        return ServiceConfig.getStatisticData(true);
    }

    @RequestMapping(value = "mcc/statistic/dynamic", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String mccStatisticDynamic() {
        return ServiceConfig.getStatisticData(false);
    }

    @RequestMapping(value = "falcon/mnszk", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getMnsZkFalconURL() {
        return JsonHelper.dataJson(CommonHelper.isOffline()?"http://10.4.243.28:8087/screen/2772":"http://falcon.sankuai.com/screen/4378");
    }
}
