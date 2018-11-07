package com.sankuai.octo.msgp.controller.component;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.service.org.BusinessOwtService;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.ComponentMessage;
import com.sankuai.octo.msgp.serivce.component.*;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@RequestMapping("/component")
public class ComponentController {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentController.class);

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "tabNav", method = RequestMethod.GET)
    public String tabNav(
            @RequestParam(value = "style", required = false, defaultValue = "cmpt_trend") String style,
            @RequestParam(value = "groupId", required = false, defaultValue = "com.meituan.inf") String groupId,
            @RequestParam(value = "artifactId", required = false, defaultValue = "xmd-common-log4j2") String artifactId,
            Model model) {

        model.addAttribute("style", style);
        model.addAttribute("groupId", groupId);
        model.addAttribute("artifactId", artifactId);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "component/componentTabNavigation";
    }

    //对外使用的组件依赖
    @RequestMapping(value = "tabNavExt", method = RequestMethod.GET)
    @Worth(model = Worth.Model.Report, function = "服务组件治理")
    public String tabNavExt(
            @RequestParam(value = "style", required = false, defaultValue = "cmpt_version") String style,
            @RequestParam(value = "groupId", required = false, defaultValue = "com.meituan.inf") String groupId,
            @RequestParam(value = "artifactId", required = false, defaultValue = "xmd-common-log4j2") String artifactId,
            Model model) {

        model.addAttribute("style", style);
        model.addAttribute("groupId", groupId);
        model.addAttribute("artifactId", artifactId);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        model.addAttribute("businessGroup", scala.collection.JavaConversions.asJavaSet(BusinessOwtService.getAllBusiness()));
        return "component/componentTabNavigationExt";
    }

    @RequestMapping(value = "cmpt", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getDefaultComponent() {
        return JsonHelper.dataJson(ComponentHelper.getDefaultComponent());
    }


    @RequestMapping(value = "owt", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getOwt(@RequestParam(value = "business") String business
    ) {
        return JsonHelper.dataJson(ComponentHelper.getOwt(business));
    }

    @RequestMapping(value = "pdl", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getPdl(@RequestParam(value = "owt") String owt
    ) {
        return JsonHelper.dataJson(ComponentHelper.getPdl(owt));
    }

    @RequestMapping(value = "app", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getApp(@RequestParam(value = "owt") String owt,
                         @RequestParam(value = "pdl") String pdl
    ) {
        return JsonHelper.dataJson(ComponentHelper.getApp(owt, pdl));
    }

    @RequestMapping(value = "group_id", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getGroupIdByKeyword(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "limitNumber") int limitNumber

    ) {
        return JsonHelper.jsonStr(ComponentHelper.getGroupIdByKeyword(keyword, limitNumber));
    }

    @RequestMapping(value = "artifact_id", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getArtifactIdByKeyword(
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "limitNumber") int limitNumber

    ) {
        return JsonHelper.jsonStr(ComponentHelper.getArtifactIdByKeyword(groupId, keyword, limitNumber));
    }

    @RequestMapping(value = "version", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getVersion(
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId

    ) {
        return JsonHelper.dataJson(ComponentHelper.getVersion(groupId, artifactId));
    }

    @RequestMapping(value = "version_count", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "组件版本分布")
    @ResponseBody
    public String gdtVersionCount(
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl", required = false) String pdl
    ) {
        return JsonHelper.dataJson(ComponentService.getComponentVersionCount(groupId, artifactId, base, business, owt, pdl));
    }

    @RequestMapping(value = "version_detail", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getVersionDetail(
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "version") String version,
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl", required = false) String pdl,
            Page page
    ) {
        return JsonHelper.dataJson(ComponentService.getComponentVersionDetails(groupId, artifactId, version, base, business, owt, pdl, page), page);
    }

    @RequestMapping(value = "coverage", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "组件覆盖清单")
    @ResponseBody
    public String getComponentCoverage(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl", required = false) String pdl,
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "matching_type", required = false) int matchingType,
            Page page
    ) {
        return JsonHelper.dataJson(ComponentService.getComponentCoverage(base, business, owt, pdl, groupId, artifactId, version, matchingType, page), page);
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "trend", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "组件使用趋势")
    @ResponseBody
    public String getComponentTrend(
            @RequestParam(value = "start") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date start,
            @RequestParam(value = "end") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date end,
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl", required = false) String pdl,
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "version", required = false) String version
    ) {
        return JsonHelper.dataJson(TrendService.getComponentTrend(start, end, base, business, owt, pdl, groupId, artifactId, version));
    }


    @RequestMapping(value = "details", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "组件详情检索")
    @ResponseBody
    public String getDetails(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl") String pdl,
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "version") String version,
            Page page
    ) {
        return JsonHelper.dataJson(ComponentService.getDetails(base, business, owt, pdl, groupId, artifactId, version, page), page);
    }


    @RequestMapping(value = "app_count", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppCount(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl") String pdl
    ) {
        return JsonHelper.dataJson(ActivenessService.getAppCount(base, business, owt, pdl));
    }

    @RequestMapping(value = "app_activeness", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "线上应用分布")
    @ResponseBody
    public String getAppActiveness(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl") String pdl
    ) {
        return JsonHelper.dataJson(ActivenessService.getAppActiveness(base, business, owt, pdl));
    }


    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "message", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "组件消息提醒")
    @ResponseBody
    public String sendMessage(@RequestBody ComponentMessage componentMessage) {
        String result = ComponentService.sendMessage(componentMessage.getIsTesting(), componentMessage.getSubject(), componentMessage.getOption_type(), componentMessage.getMessage_type(), componentMessage.getDependencies(), componentMessage.getRecommend_dependencies(), componentMessage.getWikis());
        if (result.equalsIgnoreCase("success")) {
            return JsonHelper.dataJson("messages sent successfully");
        } else if (result.equalsIgnoreCase("offline is not supported")) {
            return JsonHelper.dataJson("offline is not supported");
        } else {
            return JsonHelper.errorJson("messages sent failed");
        }
    }

    @RequestMapping(value = "category/main", method = RequestMethod.GET)
    @Worth(model = Worth.Model.Component, function = "事业群技术栈")
    public String Category() {
        return "component/cmpt_stack";
    }

    @RequestMapping(value = "category/outline", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getCategoryOutline(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "category") String category
    ) {
        return JsonHelper.dataJson(CategoryService.getCategoryOutline(base, business, category));
    }

    @RequestMapping(value = "details/appkey", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "组件依赖(服务详情)")
    @ResponseBody
    public String getDetailsByAppkey(
            @RequestParam(value = "appkey") String appkey) {
        return JsonHelper.dataJson(ComponentService.getDetailsByAppkey(appkey));
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "trend/refresh", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String refresh(
            @RequestParam(value = "date") @DateTimeFormat(pattern = DateTimeUtil.DATE_DAY_FORMAT) Date date
    ) {
        TrendService.refresh(date);
        return JsonHelper.dataJson("ok");
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "delete/deprecated", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteDeprecated() {
        ComponentHelper.deleteDeprecatedDependencies();
        return JsonHelper.dataJson("ok");
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "delete/application", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteApplication(@RequestParam(value = "groupId") String groupId,
                                    @RequestParam(value = "artifactId") String artifactId,
                                    @RequestParam(value = "reason") String reason) {
        User user = UserUtils.getUser();
        ComponentHelper.deleteApplication(user.getLogin(), groupId, artifactId, reason);
        return JsonHelper.dataJson("ok");
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "delete/reject", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteReject(@RequestParam(value = "username") String username,
                                    @RequestParam(value = "groupId") String groupId,
                                    @RequestParam(value = "artifactId") String artifactId) {
        ComponentHelper.deleteReject(username, groupId, artifactId);
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "delete/artifact", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteArtifact(@RequestParam(value = "username", required = false) String username,
                                 @RequestParam(value = "groupId") String groupId,
                                  @RequestParam(value = "artifactId") String artifactId) {
        return JsonHelper.dataJson(ComponentHelper.deleteArtifact(username, groupId, artifactId));
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "delete/invalid", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteInvalidArtifact() {
        return JsonHelper.dataJson(ComponentHelper.deleteInvalidArtifact());
    }


    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "delete/item", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteItem( @RequestParam(value = "id") long id) {
        return JsonHelper.dataJson(ComponentHelper.deleteItem(id));
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "category/update_category", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateCategory(
            @RequestParam(value = "category") String category
    ) {
        return JsonHelper.dataJson(ComponentHelper.updateCategory(category));
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "category/update_category_plus", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateCategoryPlus(
            @RequestParam(value = "category") String category,
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId
    ) {
        return JsonHelper.dataJson(ComponentHelper.updateCategoryPlus(category, groupId, artifactId));
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "config/add/blacklist", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addBlackListConfig(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(AppConfigService.addBlackListConfig(json));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "config/get/blacklist/rich", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getRichConfig(
            @RequestParam(value = "business", required = false) String business,
            @RequestParam(value = "owt", required = false) String owt,
            @RequestParam(value = "pdl", required = false) String pdl,
            @RequestParam(value = "base", required = false) String base,
            @RequestParam(value = "groupId", required = false) String groupId,
            @RequestParam(value = "artifactId", required = false) String artifactId,
            @RequestParam(value = "action", required = false) String action
    ) {
        return JsonHelper.dataJson(AppConfigService.getRichBlackListConfig(groupId, artifactId, base, business, owt, pdl, action));
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "config/add/whitelist", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addWhiteListConfig(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(AppConfigService.addWhiteListConfig(json));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "config/get/whitelist", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getWhiteListConfig(@RequestParam(value = "app_config_id", required = false) int appConfigId) {
        try {
            return JsonHelper.dataJson(AppConfigService.getWhiteListConfig(appConfigId));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "config/delete/blacklist", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteBlackListConfig(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(AppConfigService.deleteBlackListConfig(json));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "config/delete/whitelist", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteWhiteListConfig(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return JsonHelper.dataJson(AppConfigService.deleteWhiteListConfig(json));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "/update_business_simple", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateBusinessWithCorrectValue(
            @RequestParam(value = "error") String error,
            @RequestParam(value = "correct") String correct
    ) {
        return JsonHelper.dataJson(ComponentHelper.updateBusinessWithCorrectValue(error, correct));
    }

    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "/update_business", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateBusiness(
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt
    ) {
        return JsonHelper.dataJson(ComponentHelper.updateBusiness(business, owt));
    }
}