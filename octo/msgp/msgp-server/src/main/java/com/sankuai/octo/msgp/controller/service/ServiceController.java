package com.sankuai.octo.msgp.controller.service;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.model.Env;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.Pdl;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.UserUtil;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.msgp.common.utils.client.TairClient;
import com.sankuai.msgp.common.utils.helper.AuthorityHelper;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.msgp.common.utils.helper.OptionHelper;
import com.sankuai.octo.msgp.controller.OncallController;
import com.sankuai.octo.msgp.domain.AppkeyDesc;
import com.sankuai.octo.msgp.domain.DescUser;
import com.sankuai.octo.msgp.model.Perf;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import com.sankuai.octo.msgp.serivce.service.*;
import com.sankuai.octo.msgp.task.ReportDailyMailJob;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import scala.Option;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

@Controller
@RequestMapping("/service")
@Worth(model = Worth.Model.MNS)
public class ServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Worth(model = Worth.Model.MNS, function = "查询服务")
    public String home(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {
        model.addAttribute("keyword", keyword);
        return "servicedetail/list";
    }

    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询服务")
    @ResponseBody
    public String serviceList(@RequestParam(value = "pdled", required = false, defaultValue = "false") boolean pdled, Page page) {
        scala.collection.immutable.List<ServiceModels.DescRich> list = ServiceCommon.listServiceRich(page, pdled);
        return JsonHelper.dataJson(list, page);
    }

    @RequestMapping(value = "filter", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询服务")
    @ResponseBody
    public String getService(
            @RequestParam(value = "type", required = false, defaultValue = "false") int type,
            @RequestParam(value = "business", required = false, defaultValue = "-1") int business,
            Page page) {
        com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
        ServiceModels.User simpleUser = new ServiceModels.User(user.getId(), user.getLogin(), user.getName());
        scala.collection.immutable.List list = ServiceFilter.serviceByType(page, type, simpleUser, business);
        return JsonHelper.dataJson(list, page);
    }

    @Worth(model = Worth.Model.MNS, function = "查询服务")
    @RequestMapping(value = "search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String search(@RequestParam("keyword") String keyword, Page page) {
        com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
        scala.collection.immutable.List<ServiceModels.DescRich> list = ServiceCommon.searchRich(keyword, page);
        return JsonHelper.dataJson(list, page);
    }

    @RequestMapping(value = "registry/illkeyword", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String illkeyword() {
        return JsonHelper.dataJson(ServiceCommon.getIllKeyword());
    }


    /**
     * 可以帮指定用户编辑
     */
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.VIEW)
    @Worth(model = Worth.Model.MNS, function = "查看服务详情")
    @RequestMapping(value = "desc", method = RequestMethod.GET)
    public String editDesc(@RequestParam("appkey") String appkey,
                           @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force,
                           @RequestParam(value = "username", required = false) String username,
                           Model model) {

        User user = UserUtils.getUser();
        ServiceModels.Desc desc = ServiceCommon.desc(appkey);
        model.addAttribute("isEdit", true);
        model.addAttribute("name", desc.name());
        model.addAttribute("appkey", desc.appkey());
        model.addAttribute("owner", desc.owner());
        model.addAttribute("observer", desc.observer());
        model.addAttribute("ownerId", desc.ownerId());
        model.addAttribute("observerId", desc.observerId());
        model.addAttribute("category", desc.category());
        model.addAttribute("intro", desc.intro());
        model.addAttribute("tags", desc.tags());
        model.addAttribute("regLimit", desc.regLimit());
        if(CommonHelper.isOffline()){
            model.addAttribute("regLimitPermission", true);
        }else{
            model.addAttribute("regLimitPermission", AuthorityHelper.isAdmin(user.getLogin()));
        }
        String owtVal = OptionHelper.defaultString(desc.owt());
        model.addAttribute("owtVal", owtVal);
        String pdlVal = OptionHelper.defaultString(desc.pdl());
        model.addAttribute("pdlVal", pdlVal);
        model.addAttribute("businessMap", CommonHelper.businessList());
        model.addAttribute("base", OptionHelper.defaultInt(desc.base()));
        boolean isShanghai = user.getLogin().contains(".");
        List<String> owts = OpsService.getOwtsbyUsername(user.getLogin());
        if (StringUtils.isNotBlank(owtVal) && !owts.contains(owtVal)) {
            owts.add(owtVal);
        }
        if (StringUtil.isNotBlank(username)) {
            owts = addUserOwts(username);
            isShanghai = username.contains(".");
        }
        //强制在octo注册
        if (isShanghai) {
            force = isShanghai;
        }
        model.addAttribute("owts", owts);
        List pdls = OpsService.pdlList(owtVal);
        if (StringUtils.isNotBlank(owtVal)) {
            Pdl pdl = new Pdl(owtVal, pdlVal);
            if (!pdls.contains(pdl)) {
                pdls.add(pdl);
            }
        }
        model.addAttribute("pdls", pdls);
        model.addAttribute("levelMap", CommonHelper.levelList());
        model.addAttribute("businessVal", OptionHelper.defaultInt(desc.business()));
        model.addAttribute("group", OptionHelper.defaultString(desc.group()));
        model.addAttribute("levelVal", OptionHelper.defaultInt(desc.level()));
        model.addAttribute("createTime", OptionHelper.defaultLong(desc.createTime()));
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("force", force);
        return "servicedetail/registry";
    }

    private List<String> addUserOwts(String username) {
        return OpsService.getOwtsbyUsername(username);
    }

    /**
     * @param model
     * @param username 可以帮指定用户注册
     *                 判定上海测的小伙伴强制在octo注册
     * @return
     */
    @Worth(model = Worth.Model.MNS, function = "注册服务")
    @RequestMapping(value = "registry", method = RequestMethod.GET)
    public String registry(@RequestParam(value = "username", required = false) String username,
                           @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force,
                           Model model) {
        User user = UserUtils.getUser();
        model.addAttribute("isEdit", false);
        List<String> owts = OpsService.getOwtsbyUsername(user.getLogin());
        if (StringUtil.isNotBlank(username)) {
            owts = addUserOwts(username);
        }
        model.addAttribute("owts", owts);
        model.addAttribute("owtVal", owts.size() > 0 ? owts.get(0) : "");
        model.addAttribute("pdls", owts.size() > 0 ? OpsService.pdlList(owts.get(0)) : new ArrayList<String>());
        model.addAttribute("pdlVal", "");

        model.addAttribute("force", force);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "servicedetail/registry";
    }

    @RequestMapping(value = "registry", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "注册服务")
    @ResponseBody
    public String registry(HttpServletRequest request, @RequestBody AppkeyDesc appkeyDesc) {
        try {
            return ServiceCommon.saveService(UserUtils.getUser(), appkeyDesc, request.getCookies());
        } catch (Exception e) {
            LOG.error("注册失败:appkeyDesc:" + appkeyDesc.toString(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/exist", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String exist(@PathVariable("appkey") String appkey) {
        Boolean exist = ServiceCommon.exist(appkey);
        return JsonHelper.dataJson(exist);
    }


    @RequestMapping(value = "delete", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "下线服务")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String forceDelete(@RequestParam("appkey") String appkey, @RequestParam(value = "force", required = false) Boolean force) {//判定是否为 ops那边维护的服务
        return ServiceDesc.delete(appkey, UserUtils.getUser().getLogin());
    }

    /**
     * @param appkey 如果appkey为空，则已当前用户从后台能访问到的appkey列表中选择第一个appkey作为默认的appkey.但这带有随机性。
     * @param model
     * @return
     */
    @RequestMapping(value = "detail", method = RequestMethod.GET)
    @Worth(model = Worth.Model.MNS, function = "查看服务概要")
    public String detail(@RequestParam(value = "appkey", required = false) String appkey,
                         @RequestParam(value = "type", required = false) String type,
                         @RequestParam(value = "env", required = false) String env,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam(value = "keyword", required = false) String keyword,
                         @RequestParam(value = "range", required = false) String range,
                         @RequestParam(value = "remoteApp", required = false) String remoteApp,
                         @RequestParam(value = "remoteHost", required = false) String remoteHost,
                         Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        model.addAttribute("apps", ServiceCommon.apps());
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("type", type);
        model.addAttribute("env", env);
        model.addAttribute("appkey", appkey);
        String ownerStr = ServiceCommon.ownerStrByAppkey(appkey);
        model.addAttribute("ownerStr", ownerStr);
        Option<String> oncallStr = TairClient.get(OncallController.ONCALL_PRIFIX+appkey);
        if(oncallStr.isDefined()){
            model.addAttribute("oncallStr", oncallStr.get());
        }else{
            model.addAttribute("oncallStr", "");
        }
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("range", range);
        model.addAttribute("remoteApp", remoteApp);
        model.addAttribute("remoteHost", remoteHost);
        model.addAttribute("isCellOpen", ServiceCommon.isCellOpen(appkey));

        return "servicedetail/detail";
    }

    @RequestMapping(value = "{appkey}/desc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务概要")
    @ResponseBody
    public String desc(@PathVariable("appkey") String appkey) {
        ServiceModels.DescRich descRich = ServiceCommon.desc(appkey).toRich();
        return JsonHelper.dataJson(descRich);
    }


    @RequestMapping(value = "{appkey}/desc", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Worth(model = Worth.Model.MNS, function = "修改服务概要")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateDesc(@PathVariable("appkey") String appkey, @RequestBody AppkeyDesc appkeyDesc, HttpServletRequest request) {
        try {
            return ServiceCommon.saveService(UserUtils.getUser(), appkeyDesc, request.getCookies());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    @RequestMapping(value = "{appkey}/observer", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务概要")
    @ResponseBody
    public String observer(@PathVariable("appkey") String appkey) {
        return ServiceDesc.observer(appkey, UserUtils.getUser());
    }


    @RequestMapping(value = "{appkey}/provider", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务节点")
    @ResponseBody
    public String provider(@PathVariable("appkey") String appkey,
                           @RequestParam(value = "ip", required = false) String ip,
                           @RequestParam(value = "env", required = false) String env,
                           @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                           @RequestParam("type") int type,
                           @RequestParam(value = "sort", required = false) int sort,
                           Page page) {
        scala.collection.immutable.List<ServiceModels.ProviderNode> list = AppkeyProviderService.getProviderByType(appkey, type, env == null ? Env.prod().toString() : env, ip, status, page, sort);
        return (null != list) ? JsonHelper.dataJson(list, page) : JsonHelper.errorJson("内部异常");
    }


    @RequestMapping(value = "{appkey}/provider/outline", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "提供者概要视图")
    @ResponseBody
    public String outlineOfProvider(@PathVariable("appkey") String appkey,
                                    @RequestParam(value = "ip", required = false) String ip,
                                    @RequestParam(value = "env", required = false) Integer env,
                                    @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                                    @RequestParam("type") int type,
                                    @RequestParam(value = "sort", required = false) int sort,
                                    Page page) {
        Object obj = ServiceProvider.getOutlineOfProvider(appkey, type, env == null ? 3 : env, ip, status, page, sort);
        return (null != obj) ? JsonHelper.dataJson(obj) : JsonHelper.errorJson("内部异常");
    }

    @RequestMapping(value = "{appkey}/provider/iplist", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String ipListOfProvider(@PathVariable("appkey") String appkey,
                                   @RequestParam(value = "ip", required = false) String ip,
                                   @RequestParam(value = "env", required = false) Integer env,
                                   @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                                   @RequestParam("type") int type,
                                   @RequestParam(value = "sort", required = false) int sort,
                                   Page page) {
        Object obj = ServiceProvider.getIPListofProvider(appkey, type, env == null ? 3 : env, ip, status, page, sort);
        return (null != obj) ? JsonHelper.dataJson(obj) : JsonHelper.errorJson("内部异常");
    }

    @RequestMapping(value = "/provider/idclist", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String idcListOfProvider(@RequestParam("appkey") String appkey, @RequestParam("type") int type) {
        Object obj = ServiceProvider.getServiceIdc(appkey, type);
        return (null != obj) ? JsonHelper.dataJson(obj) : JsonHelper.errorJson("内部异常");
    }


    @RequestMapping(value = "{appkey}/provider/querybyidc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "提供者概要视图")
    @ResponseBody
    public String queryProviderByIdc(@PathVariable("appkey") String appkey,
                                     @RequestParam(value = "ip", required = false) String ip,
                                     @RequestParam(value = "env", required = false) Integer env,
                                     @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                                     @RequestParam("type") int type,
                                     @RequestParam(value = "sort", required = false) int sort,
                                     @RequestParam(value = "idcname", required = false) String idcName,
                                     Page page) {
        scala.collection.immutable.List<ServiceModels.ProviderNode> list = ServiceProvider.getProviderByIDC(appkey, type, env == null ? 3 : env, ip, status, page, sort, idcName);
        return (null != list) ? JsonHelper.dataJson(list, page) : JsonHelper.errorJson("内部异常");
    }

    @RequestMapping(value = "{appkey}/provider/querybystatus", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务节点")
    @ResponseBody
    public String queryProviderByStatus(@PathVariable("appkey") String appkey,
                                        @RequestParam(value = "ip", required = false) String ip,
                                        @RequestParam(value = "env", required = false) Integer env,
                                        @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                                        @RequestParam("type") int type,
                                        @RequestParam(value = "sort", required = false) int sort,
                                        Page page) {
        scala.collection.immutable.List<ServiceModels.ProviderNode> list = ServiceProvider.getProviderByStatus(appkey, type, env == null ? 3 : env, ip, status, page, sort);
        return (null != list) ? JsonHelper.dataJson(list, page) : JsonHelper.errorJson("内部异常");
    }


    @RequestMapping(value = "{appkey}/provider/all", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务节点")
    @ResponseBody
    public String allProviders(@PathVariable("appkey") String appkey,
                               @RequestParam(value = "env") int env,
                               @RequestParam(value = "type", required = false) String type) {
        boolean isHttp = !StringUtils.isEmpty(type) && "http".equalsIgnoreCase(type);
        return JsonHelper.dataJson(isHttp ? AppkeyProviderService.providerNodeHttp(appkey, env) : AppkeyProviderService.providerNode(appkey, env));
    }

    @RequestMapping(value = "{appkey}/provider/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务节点")
    @ResponseBody
    public String searchProvider(@PathVariable("appkey") String appkey,
                                 @RequestParam("type") int type,
                                 @RequestParam(value = "env", required = false) String env,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                                 @RequestParam(value = "sort", required = false) int sort,
                                 Page page) {
        scala.collection.immutable.List<ServiceModels.ProviderNode> list = AppkeyProviderService.getProviderBySearch(appkey, type, env == null ? Env.prod().toString() : env, keyword, status, page, sort);

        return (null != list) ? JsonHelper.dataJson(list, page) : JsonHelper.errorJson("内部异常");
    }

    @RequestMapping(value = "{appkey}/provider/{type}", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "注册服务节点")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String addProvider(@PathVariable("appkey") String appkey,
                              @PathVariable("type") int type, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            String response = AppkeyProviderService.addProviderByType(appkey, type, json);
            LOG.info("response: " + response);
            return response;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/provider/{type}/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "配置服务节点")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String delProvider(@PathVariable("appkey") String appkey, @PathVariable("type") int type, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            String response = AppkeyProviderService.delProviderByType(appkey, type, json);
            return response;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/provider/{type}/{node:.+}", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "配置服务节点")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateProvider(@PathVariable("appkey") String appkey,
                                 @PathVariable("type") int type,
                                 HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            User user = UserUtils.getUser();
            String response = AppkeyProviderService.updateProviderByType(appkey, user.getLogin(), type, json);
            LOG.info("response: " + response);
            return response;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "/consumer", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询消费者")
    @ResponseBody
    public String consumer(@RequestParam("appkey") String appkey,
                           @RequestParam("range") String range,
                           @RequestParam(value = "env", required = false) String env,
                           @RequestParam("remoteApp") String remoteApp,
                           @RequestParam("remoteHost") String remoteHost) {
        String envStr = env == null ? "prod" : env;
        scala.collection.immutable.List<Perf.Consumer> list = ServiceConsumer.consumerList(appkey, range, envStr, remoteApp, remoteHost);
        return JsonHelper.dataJson(list);
    }

    @RequestMapping(value = "consumer/outline", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "消费者概要视图")
    @ResponseBody
    public String consumerOutline(@RequestParam("appkey") String appkey,
                                  @RequestParam("range") String range,
                                  @RequestParam(value = "env", required = false) String env) {
        String envStr = env == null ? "prod" : env;
        return JsonHelper.dataJson(ServiceConsumer.consumerOutline(appkey, range, envStr));
    }


    @RequestMapping(value = "owner", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询我负责的所有服务")
    @ResponseBody
    public String getAllServiceByOwner() {
        com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
        ServiceModels.User simpleUser = new ServiceModels.User(user.getId(), user.getLogin(), user.getName());
        scala.collection.immutable.List list = ServiceFilter.serviceByOwner(simpleUser, new Page(1, 1000));
        return JsonHelper.dataJson(list);
    }


    @RequestMapping(value = "{appkey}/provider/{type}/list", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "配置服务节点")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateProviderList(@PathVariable("appkey") String appkey,
                                     @PathVariable("type") int type, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            User user = UserUtils.getUser();
            return AppkeyProviderService.updateProviderListByType(appkey, user.getLogin(), type, json);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/provider/{type}/list/del", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "删除服务节点")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String delProviderList(@PathVariable("appkey") String appkey,
                                  @PathVariable("type") int type, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            String response = AppkeyProviderService.delProviderListByType(appkey, type, json);
            LOG.info("response: " + response);
            return response;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/healthUrlCheck", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String httpHealthUrlCheck(@PathVariable("appkey") String appkey,
                                     @RequestParam(value = "env") int env,
                                     @RequestParam(value = "healthUrl") String healthUrl) {
        String ipAndPort = ServiceProvider.getOneProvider(appkey, env);
        if (ipAndPort.isEmpty()) {
            LOG.info("providerTest is empty");
            return "emptyFailed";
        } else {
            String checkUrl = "http://" + ipAndPort + healthUrl;
            int checkCode = HttpUtil.getUrlCode(checkUrl);
            if (checkCode >= 200 && checkCode < 300) {
                return "success";
            } else if (checkCode >= 300 && checkCode < 400) {
                return "redirect";
            } else {
                return "checkFailed";
            }
        }
    }

    @Worth(model = Worth.Model.Monitor, function = "查看日志")
    @RequestMapping(value = "operation/{appkey}/log", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String operationLog(@PathVariable(value = "appkey") String appkey,
                               @RequestParam(value = "entityType", required = false) String entityType,
                               @RequestParam(value = "operator", required = false) String operator,
                               @RequestParam(value = "start", required = false) String start,
                               @RequestParam(value = "end", required = false) String end,
                               Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        scala.collection.immutable.List<BorpClient.operationDisplay> operationList = BorpClient.getOptLog(appkey, entityType, operator, startTime, endTime, page);
        return JsonHelper.dataJson(operationList, page);
    }


    @Worth(model = Worth.Model.Monitor, function = "查看日志")
    @RequestMapping(value = "operation/{appkey}/entity", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String allEntity(@PathVariable(value = "appkey") String appkey,
                            @RequestParam(value = "start", required = false) String start,
                            @RequestParam(value = "end", required = false) String end
    ) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        List<String> entityTypeList = BorpClient.getAllEntityType(appkey, startTime, endTime);
        return JsonHelper.dataJson(entityTypeList);
    }


    @Worth(model = Worth.Model.Monitor, function = "查看日志")
    @RequestMapping(value = "operation/{appkey}/operator", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String allOperator(@PathVariable(value = "appkey") String appkey,
                              @RequestParam(value = "start", required = false) String start,
                              @RequestParam(value = "end", required = false) String end
    ) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        List<String> operatorList = BorpClient.getAllOperator(appkey, startTime, endTime);
        return JsonHelper.dataJson(operatorList);
    }


    @RequestMapping(value = "{appkey}/healthCheckConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String healthCheckConfig(@PathVariable("appkey") String appkey,
                                    @RequestParam(value = "env", required = false) Integer env) {
        LOG.info("healthCheckConfig get appkey: " + appkey + " " + " env: " + env);
        ServiceModels.HealthCheckConfig data = ServiceHttpConfig.getHealthCheckConfig(appkey, env == null ? 1 : env);  // 如果不提供环境参数，默认为test环境
        String response = JsonHelper.dataJson(data);
        LOG.info("healthCheckConfig get response: " + response);
        return response;
    }

    @RequestMapping(value = "{appkey}/healthCheckConfig", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateHealthCheckConfig(@PathVariable("appkey") String appkey,
                                          @RequestParam(value = "env", required = false) Integer env,
                                          @RequestParam(value = "data", required = false) String data) {
        LOG.info("healthCheckConfig put appkey: " + appkey + " env: " + env + " data: " + data);
        try {
            String response = ServiceHttpConfig.updateHealthCheckConfig(appkey, env == null ? 1 : env, data); // 如果不提供环境参数，默认为test环境
            LOG.info("healthCheckConfig put response: " + response);
            return response;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/slowStartConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String slowStartConfig(@PathVariable("appkey") String appkey,
                                  @RequestParam(value = "env", required = false) Integer env) {
        LOG.info("slowStartConfig get appkey: " + appkey + " " + " env: " + env);
        ServiceModels.SlowStartConfig data = ServiceHttpConfig.getSlowStartConfig(appkey, env);
        String response = JsonHelper.dataJson(data);
        LOG.info("slowStartConfig get response: " + response);
        return response;
    }

    @RequestMapping(value = "{appkey}/slowStartConfig", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateSlowStartConfig(@PathVariable("appkey") String appkey,
                                        @RequestParam(value = "env", required = false) Integer env,
                                        @RequestParam(value = "data", required = false) String data) {
        LOG.info("slowStartConfig put appkey: " + appkey + " env: " + env + " data: " + data);
        try {
            String response = ServiceHttpConfig.updateSlowStartConfig(appkey, env, data);
            LOG.info("slowStartConfig put response: " + response);
            return response;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/domainConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String domainConfig(@PathVariable("appkey") String appkey,
                               @RequestParam(value = "env", required = false) Integer env) {
        LOG.info("domainConfig get " + appkey + "  env: " + env);
        ServiceModels.DomainConfig data = ServiceHttpConfig.getDomainConfig(appkey, env == null ? 1 : env);  // 如果不提供环境参数，默认为test环境
        String response = JsonHelper.dataJson(data);
        LOG.info("domainConfig get response: " + response);
        return response;
    }

    @RequestMapping(value = "{appkey}/domainConfig", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateDomainConfig(@PathVariable("appkey") String appkey,
                                     @RequestParam(value = "env", required = false) Integer env,
                                     @RequestParam(value = "data", required = false) String data,
                                     HttpServletRequest request) {
        LOG.info("domainConfig put " + appkey + " env: " + env + " data: " + data);
        try {
            String inputJson = IOUtils.copyToString(request.getReader());
            Map paramMap = HttpUtil.getUrlParams(inputJson);
            env = Integer.valueOf((String) paramMap.get("env"));
            data = (String) paramMap.get("data");
            String response = ServiceHttpConfig.updateDomainConfig(appkey, env == null ? 1 : env, data); // 如果不提供环境参数，默认为test环境
            LOG.info("domainConfig put response: " + response);
            return response;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/loadBalanceConfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String loadBalanceConfig(@PathVariable("appkey") String appkey,
                                    @RequestParam(value = "env", required = false) Integer env) {
        LOG.info("loadBalanceConfig get " + appkey + "  env: " + env);
        ServiceModels.LoadBalanceConfig data = ServiceHttpConfig.getLoadBalanceConfig(appkey, env == null ? 1 : env);  // 如果不提供环境参数，默认为test环境
        String response = JsonHelper.dataJson(data);
        LOG.info("loadBalanceConfig get response: " + response);
        return response;
    }

    @RequestMapping(value = "{appkey}/loadBalanceConfig", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateLoadBalanceConfig(@PathVariable("appkey") String appkey,
                                          @RequestParam(value = "env", required = false) Integer env,
                                          @RequestParam(value = "data", required = false) String data,
                                          HttpServletRequest request) {
        LOG.info("loadBalanceConfig put " + appkey + " env: " + env + " data: " + data);
        try {
            String inputJson = IOUtils.copyToString(request.getReader());
            Map paramMap = HttpUtil.getUrlParams(inputJson);
            env = Integer.valueOf((String) paramMap.get("env"));
            data = (String) paramMap.get("data");
            String response = ServiceHttpConfig.updateLoadBalanceConfig(appkey, env == null ? 1 : env, data); // 如果不提供环境参数，默认为test环境
            LOG.info("loadBalanceConfig put response: " + response);
            return response;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "owt/apps", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appsByOwt(@RequestParam(value = "owt", required = false) String owt) {
        return JsonHelper.dataJson(ServiceCommon.appsByOwt(owt));
    }

    @RequestMapping(value = "consumer/unknown_service", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String unknownService(@RequestParam(value = "appkey", required = false) String appkey,
                                 @RequestParam(value = "range", required = false) String range,
                                 @RequestParam(value = "remoteHost", required = false) String remoteHost,
                                 @RequestParam(value = "merge", required = false) Boolean merge) {
        return JsonHelper.dataJson(ServiceConsumer.getUnknownServiceListCache(appkey, range, remoteHost, merge));
    }

    /**
     * 以 zk为准，刷新zk的数据到数据库
     *
     * @return
     */
    @RequestMapping(value = "refresh", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshApp(@RequestParam(value = "appkey", required = false) String appkey) {
        ServiceDesc.refresh(appkey);
        return JsonHelper.dataJson("ok");
    }

    /**
     * 刷新没有owt和pdl信息的appkey
     *
     * @return
     */
    @RequestMapping(value = "/refreshOwtPdl", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshOwtPdl(@RequestParam(value = "login", required = true) String login) {
        User u = UserUtils.getUser();
        if (AppkeyAuth.hasAdminAuth("com.sankuai.inf.msgp", u.getLogin())) {
            LOG.info(login + " refresh the owt pdl message");
            ServiceCommon.refreshOwtPdl();
            return JsonHelper.dataJson("ok");
        } else {
            return JsonHelper.errorJson("没有权限");
        }
    }

    @RequestMapping(value = "/deleteSelf", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteSelf(@RequestParam("appkey") String appkey, @RequestParam("type") String type) {
        User u = UserUtils.getUser();
        if (null != u) {
            if ("owner".equalsIgnoreCase(type)) {
                if (ServiceCommon.isOwnerLogin(appkey, u.getLogin())) {
                    ServiceCommon.convergenceOwnerObserver(u, appkey, "owner");
                    return JsonHelper.dataJson("删除成功");
                } else {
                    return JsonHelper.errorJson("非" + appkey + "管理员，无法删除自己");
                }
            } else {
                if (ServiceCommon.isObserverLogin(appkey, u.getLogin())) {
                    ServiceCommon.convergenceOwnerObserver(u, appkey, "observer");
                    return JsonHelper.dataJson("删除成功");
                } else {
                    return JsonHelper.errorJson("非" + appkey + "关注者，无法删除自己");
                }

            }
        } else {
            return JsonHelper.errorJson("用户不存在无法删除!");
        }
    }

    @RequestMapping(value = "clearLeft", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String clear() {
        ServiceCommon.clearSubscribe();
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "clearObservers", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String clearObservers() {
        ServiceCommon.clearObservers();
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "cellSwitch", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String cellSwitch(@RequestParam("appkey") String appkey, @RequestParam("switch") String switchTrigger) {
        User u = UserUtils.getUser();
        if (AppkeyAuth.hasAdminAuth(appkey, u.getLogin())) {
            try {
                boolean isSwitch = "0".equals(switchTrigger) ? false : true;
                String result = isSwitch ? "开启成功" : "关闭成功";
                ServiceCommon.updateCellStatus(appkey, isSwitch);
                LOG.info(u.getLogin() + " " + result + " " + appkey + " set cell");
                return JsonHelper.dataJson(result);
            } catch (Exception e) {
                return JsonHelper.errorDataJson("zk操作失败");
            }
        } else {
            return JsonHelper.errorDataJson("开启失败");
        }
    }

}
