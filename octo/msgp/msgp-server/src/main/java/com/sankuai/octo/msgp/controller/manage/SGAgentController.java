package com.sankuai.octo.msgp.controller.manage;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.utils.UserUtil;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.AppkeyIps;
import com.sankuai.octo.msgp.serivce.manage.AgentAvailability;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.serivce.service.ServiceConfig;
import com.sankuai.octo.msgp.serivce.sgagent.*;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.sgagent.thrift.model.SGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lhmily on 08/21/2015.
 */
@RequestMapping("/manage/agent")
@Controller
@Worth(project = Worth.Project.OCTO, model = Worth.Model.OTHER)
public class SGAgentController {
    private static final Logger LOG = LoggerFactory.getLogger(SGAgentController.class);

    @RequestMapping(value = "tabNav", method = RequestMethod.GET)
    public String tabNav(@RequestParam(value = "appkey", required = false) String appkey, Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps,appkey);
        model.addAttribute("appkey", appkey);
        model.addAttribute("apps", ServiceCommon.apps());
        return "manage/sgagent/agentTabNavigation";
    }

    @RequestMapping(value = "checker/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addAgentChecker(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("sg_agent checker" + json);
            return AgentAvailability.addCheck(json);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "checker/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAgentChecker() {
        return JsonHelper.dataJson(AgentAvailability.getCheckJob());
    }

    @RequestMapping(value = "checker/del", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String delAgentChecker(@RequestParam(value = "id") long id) {
        return AgentAvailability.deleteCheckJob(id);
    }

    @RequestMapping(value = "checker/update", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateAgentChecker(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info("sg_agent checker" + json);
            return AgentAvailability.updateCheck(json);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "checker/providers", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAgentProviders(@RequestParam(value = "env") int envId) {
        return JsonHelper.dataJson(AgentAvailability.getAgentProvider(envId));
    }

    @RequestMapping(value = "checker/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String searchAgentProviders(@RequestParam(value = "env") int envId, @RequestParam(value = "keyword") String keyword) {
        return JsonHelper.dataJson(AgentAvailability.searchProvider(keyword, envId));
    }

    /**
     * @param appkey
     * @param ip
     * @param appIP
     * @param port
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "{appkey}/service/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String searchService(@PathVariable(value = "appkey") String appkey,
                                @RequestParam(value = "ip") String ip,
                                @RequestParam(value = "appIP") String appIP,
                                @RequestParam(value = "port") String port,
                                @RequestParam(value = "pageNo") int pageNo,
                                @RequestParam(value = "pageSize") int pageSize,
                                @RequestParam(value = "protocol") String protocol) {
        Page page = new Page();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);

        String username = null != UserUtils.getUser() ? UserUtils.getUser().getLogin() : "未知用户";
        List<String> adminList = Arrays.asList(MsgpConfig.get("admins", "").split(","));
        if (!adminList.contains(username) && !OpsService.getOwnerByIp(ip).contains(username)) {
            return JsonHelper.errorJson("您不是这个ip所属机器的负责人，没有检测权限。");
        }
        List<String> appkeyBackList = Arrays.asList(MsgpConfig.get("appkey_blacklist", "com.sankuai.inf.sg_agent,com.sankuai.inf.kms_agent").split(","));
        if (appkeyBackList.contains(appkey)) {
            return JsonHelper.errorJson("该appkey存在大量服务节点，暂不支持检测。");
        } else {
            List<SGService> serviceList = SgAgentService.getServiceListByAppkeyIP(protocol, appkey, ip, appIP, port, page);
            return JsonHelper.dataJson(serviceList, page);
        }
    }

    @RequestMapping(value = "switchEnv/applyListByUser", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "主机管理")
    @ResponseBody
    public String getApplyListByUser(@RequestParam(value = "pageNo") int pageNo,
                                     @RequestParam(value = "pageSize") int pageSize) {
        Page page = new Page();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return JsonHelper.dataJson(SgAgentSwitchEnv.getApplyListByUser(page), page);
    }

    @RequestMapping(value = "switchEnv/applySwitchEnv", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "修改节点环境")
    @ResponseBody
    public String applySwitchEnv(@RequestParam("ip") String ip,
                                 @RequestParam("newEnv") String newEnv) {
        try {
            return SgAgentSwitchEnv.applySwitchEnv(ip, newEnv);
        } catch (Exception e) {
            LOG.error("申请修改环境异常失败，ip " + ip + " newEnv" + newEnv, e);
            return JsonHelper.errorJson("申请失败，请重试");
        }
    }

    @RequestMapping(value = "switchEnv/switchEnvList", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getSwitchEnvList(@RequestParam("flag") int flag,
                                   @RequestParam("searchIP") String searchIP,
                                   @RequestParam(value = "pageNo") int pageNo,
                                   @RequestParam(value = "pageSize") int pageSize) {
        Page page = new Page();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        if (null == searchIP) searchIP = "";
        searchIP = "%" + searchIP.trim() + "%";
        return JsonHelper.dataJson(SgAgentSwitchEnv.getSwitchEnv(flag, searchIP, page), page);
    }

    @RequestMapping(value = "switchEnv/switchEnv", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteSwitchEnv(@RequestParam("id") Long id) {
        return SgAgentSwitchEnv.deleteSwitchEnv(id) ? JsonHelper.dataJson("删除成功") : JsonHelper.errorJson("删除失败");
    }


    @RequestMapping(value = "switchEnv/resetAgent", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String resetAgent(@RequestParam("id") Long id) {
        //限制重置环境切换的人员
        User user = UserUtils.getUser();
        List<String> myAuthList = SgAgentSwitchEnv.getSwtichAuthList();
        if (!myAuthList.contains(user.getLogin())) {
            return JsonHelper.errorJson("请联系" + myAuthList.toString());
        }

        return SgAgentSwitchEnv.updateAndRestartAgent(id);
    }

    @RequestMapping(value = "switchEnv/comfirmSwitchEnv", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String comfirmSwitchEnv(@RequestParam("id") Long id) {
        //限制审核环境切换的人员
        User user = UserUtils.getUser();
        List<String> myAuthList = SgAgentSwitchEnv.getSwtichAuthList();
        if (!myAuthList.contains(user.getLogin())) {
            return JsonHelper.errorJson("请联系" + myAuthList.toString());
        }
        return SgAgentSwitchEnv.comfirmSwitchEnv(id);
    }

    @RequestMapping(value = "switchEnv/rejectSwitchEnv", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String rejectSwitchEnv(@RequestParam("id") Long id,
                                  @RequestParam("note") String note) {
        User user = UserUtils.getUser();
        List<String> myAuthList = SgAgentSwitchEnv.getSwtichAuthList();
        if (!myAuthList.contains(user.getLogin())) {
            return JsonHelper.errorJson("请联系" + myAuthList.toString());
        }

        SgAgentSwitchEnv.ErrMsg ret = SgAgentSwitchEnv.updataSwitchEnvDB(id, 2, note);
        int SUCCESS = SgAgentSwitchEnv.SUCCESS();
        return (SUCCESS == ret.code()) ? JsonHelper.dataJson("拒绝成功") : JsonHelper.errorJson(ret.msg());
    }

    @RequestMapping(value = "{appkey}/provider/batch", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String batchProviders(@PathVariable("appkey") String appkey,
                                 HttpServletRequest request) {
        User user = UserUtils.getUser();
        if (!"yangjie17".equals(user.getLogin())) {
            return JsonHelper.errorJson("请联系杨杰(yangjie17)");
        }
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(AddBatchProviders.batchProviders(appkey, json));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }

    }

    @RequestMapping(value = "{appkey}/host/name2ip", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hostName2IP(@PathVariable("appkey") String appkey,
                              @RequestParam("prefix") String prefix,
                              @RequestParam("startNum") int startNum,
                              @RequestParam("endNum") int endNum) {
        User user = UserUtils.getUser();
        if (!"yangjie17".equals(user.getLogin())) {
            return JsonHelper.errorJson("请联系杨杰(yangjie17)");
        }
        try {
            return JsonHelper.dataJson(AddBatchProviders.hostName2IP(prefix, startNum, endNum));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }

    }

    @RequestMapping(value = "{appkey}/mcc/dynamicdata", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getMccDynamicData(@PathVariable("appkey") String appkey,
                                    @RequestParam("ip") String ip,
                                    @RequestParam(value = "path", defaultValue = "/") String path,
                                    @RequestParam(value = "swimlane", defaultValue = "") String swimlane) {
        String username = null != UserUtils.getUser() ? UserUtils.getUser().getLogin() : "未知用户";
        List<String> adminList = Arrays.asList(MsgpConfig.get("admins", "").split(","));
        if (!adminList.contains(username) && !OpsService.getOwnerByIp(ip).contains(username)) {
            return JsonHelper.errorJson("您不是这个ip所属机器的负责人，没有检测权限。");
        }
        return SgAgentChecker.getMccDynamicData(appkey, ip, path, swimlane);
    }

    @RequestMapping(value = "{appkey}/mcc/filedata", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getMccFileData(@PathVariable("appkey") String appkey,
                                 @RequestParam("ip") String ip,
                                 @RequestParam("filename") String fileName) {
        return SgAgentChecker.getMccFileData(appkey, ip, fileName);
    }


    @RequestMapping(value = "/provider", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getSGAgentIPAndPort(@RequestParam("env") int env,
                                      @RequestParam(value = "keyword", required = false) String keyword) {
        return JsonHelper.dataJson(SgAgentCommon.getSGAgentIPAndPort(env, keyword));
    }

    @RequestMapping(value = "/switchname", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getSwitchName() {
        return JsonHelper.dataJson(SgAgentSwitch.getSwitchTypes());
    }


    @RequestMapping(value = "/switchsgagent", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String switchSGAgent(HttpServletRequest request) {
        User user = UserUtils.getUser();
        List<String> authList = SgAgentSwitchEnv.getSwtichAuthList();
        if (!authList.contains(user.getLogin())) {
            return JsonHelper.errorJson("请联系" + authList.toString());
        }
        try {
            String json = IOUtils.copyToString(request.getReader());
            return SgAgentSwitch.switchSGAgent(json);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }

    }

    @RequestMapping(value = "/shutdownagent", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String shutdownAgent(@RequestBody AppkeyIps appkeyIps) {
        return handleShutdown(false, appkeyIps);
    }

    @RequestMapping(value = "/shutdownagentworker", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String shutdownAgentWorker(@RequestBody AppkeyIps appkeyIps) {
        return handleShutdown(true, appkeyIps);
    }

    private String handleShutdown(boolean isWorker, AppkeyIps appkeyIps) {
        User user = UserUtils.getUser();
        List<String> authList = SgAgentSwitchEnv.getSwtichAuthList();
        if (!authList.contains(user.getLogin())) {
            return JsonHelper.errorJson("请联系" + authList.toString());
        }
        try {
            return SgAgentShutdown.shutdownSgAgent(isWorker, appkeyIps);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "/healthcheck", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String sgagentHealthCheck(@RequestParam("ip") String ip) {
        return JsonHelper.dataJson(SgAgentChecker.sgagentHealthCheck(null != ip ? ip : ""));
    }

    @RequestMapping(value = "/install", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String sgagentInstall(@RequestParam("ip") String ip) {
        return JsonHelper.dataJson(SgAgentChecker.sgagentInstall(null != ip ? ip : ""));
    }

    @RequestMapping(value = "/reinstall", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String sgagentReinstall(@RequestParam("ip") String ip) {
        return JsonHelper.dataJson(SgAgentChecker.sgagentReinstall(null != ip ? ip : ""));
    }

    @RequestMapping(value = "/restart", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String sgagentRestart(@RequestParam("ip") String ip) {
        return JsonHelper.dataJson(SgAgentChecker.sgagentRestart(null != ip ? ip : ""));
    }

    @RequestMapping(value = "/availabilityData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String availabilityData(@RequestParam(value = "appkey") String appkey, @RequestParam(value = "serverType") String serverType) {
        return JsonHelper.dataJson(SgAgentChecker.getDailyAvailability(appkey, serverType));
    }

    @RequestMapping(value = "/addAuthItem", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addHttpAuthItem(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return SgAgentChecker.saveHttpAuthItem(json) ? JsonHelper.dataJson("保存成功") : JsonHelper.errorJson("保存失败");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("内部异常");
        }
    }

    @RequestMapping(value = "/httpAuths", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String httpAuth(@RequestParam("pageNo") int pageNo) {
        Page page = new Page(pageNo);
        return JsonHelper.dataJson(SgAgentChecker.getHttpAuthItems(page), page);
    }

    @RequestMapping(value = "/httpAuth/usernames", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAuthUsernames() {
        return JsonHelper.dataJson(SgAgentChecker.getAuthUsernames());
    }

    @RequestMapping(value = "/httpAuth", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getHttpAuth(@RequestParam("authID") String authID) {
        return JsonHelper.dataJson(SgAgentChecker.getHttpAuth(authID));
    }

    @RequestMapping(value = "/httpAuth", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteHttpAuth(@RequestParam("authID") String authID) {
        return JsonHelper.dataJson(SgAgentChecker.deleteHttpAuth(authID));
    }
}
