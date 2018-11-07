package com.sankuai.octo.msgp.controller.service;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.AppkeyGroup;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.msgp.serivce.service.ServiceGroup;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping("/service")
@Auth(level = Auth.Level.OBSERVER, responseMode = Auth.ResponseMode.JSON)
@Worth(project = Worth.Project.OCTO, model = Worth.Model.ROUTE)
public class GroupController {

    private static final Logger LOG = LoggerFactory.getLogger(GroupController.class);

    @Worth(model = Worth.Model.ROUTE, function = "查看分组")
    @RequestMapping(value = "{appkey}/group", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    public String group(@PathVariable("appkey") String appkey,
                        @RequestParam(value = "env", required = false,defaultValue = "0") Integer env,
                        Page page) {
        scala.collection.immutable.List<ServiceModels.Group> list = ServiceGroup.group(appkey, env, page);
        return JsonHelper.dataJson(list, page);
    }

    @Worth(model = Worth.Model.ROUTE, function = "查看分组")
    @RequestMapping(value = "{appkey}/group/attributes", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String attributes(@PathVariable("appkey") String appkey) {
        return JsonHelper.dataJson(AppkeyProviderService.groupAttributes(appkey));
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey}/group", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String saveGroup(@PathVariable("appkey") String appkey,
                            @RequestBody AppkeyGroup appkeyGroup) {
        try {
            LOG.info("saveGroup " + appkey + " with " + appkeyGroup);
            String response = ServiceGroup.saveGroup(appkeyGroup);
            LOG.info("response: " + response);
            return response;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey:.+}/group/{id:.+}", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String deleteGroup(@PathVariable("appkey") String appkey, @PathVariable("id") String id) {
        ServiceGroup.deleteGroup(appkey, id);
        return JsonHelper.dataJson(id);
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey}/group/verify", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String verifyDefault(@PathVariable(value = "appkey") String appkey, @RequestParam(value = "envId") int envId) {
        String result = AppkeyProviderService.verifyDefault(appkey, envId);
        if (result == null) {
            return JsonHelper.dataJson("ok");
        } else {
            return JsonHelper.errorJson(result);
        }
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey:.+}/group/default", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String defaultGroup(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        LOG.info("defaultGroup {} {} {}", new Object[]{appkey, json});
        ServiceGroup.defaultGroup(appkey, json);
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey:.+}/group/default/force", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String forceDefaultGroup(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        LOG.info("defaultGroup {} {} {}", new Object[]{appkey, json});
        ServiceGroup.forceDefaultGroup(appkey, json);
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.ROUTE, function = "查看分组")
    @RequestMapping(value = "{appkey}/group/detail", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String routeDetail(@PathVariable(value = "appkey") String appkey,
                              @RequestParam("id") String id,
                              @RequestParam("env") int env) {
        try {
            ServiceModels.Group data = ServiceGroup.getRouteDetail(appkey, env, id);
            return JsonHelper.dataJson(data);
        } catch (Exception e) {
            return JsonHelper.errorJson("服务器异常");
        }
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey}/group/edit", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String editRouteDetail(HttpServletRequest request) {
        String json ="";
        try {
            json = IOUtils.copyToString(request.getReader());
            if (ServiceGroup.editRouteDetail(json))
                return JsonHelper.dataJson("编辑成功");
            else {
                return JsonHelper.errorJson("编辑失败");
            }
        } catch (Exception e) {
            LOG.error("编辑分组失败",e);
            return JsonHelper.errorJson("服务器异常");
        }
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey}/group/verify/multicenter", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String verifyMultiCenter(@PathVariable(value = "appkey") String appkey, @RequestParam(value = "envId") int envId) {
        String result = AppkeyProviderService.verifyMultiCenter(appkey, envId);
        if (result == null) {
            return JsonHelper.dataJson("ok");
        } else {
            return JsonHelper.errorJson(result);
        }
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey:.+}/group/multicenter", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String editMultiCenter(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        LOG.info("defaultGroup {} {} {}", new Object[]{appkey, json});
        ServiceGroup.editMultiCenter(appkey, json);
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @RequestMapping(value = "{appkey:.+}/group/multicenter/force", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String forceMultiCenterGroup(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        LOG.info("defaultGroup {} {} {}", new Object[]{appkey, json});
        ServiceGroup.forceMultiCenterGroup(appkey, json);
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "{appkey}/group/providers", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.ROUTE, function = "查看分组")
    @ResponseBody
    public String allProviders(@PathVariable("appkey") String appkey,
                               @RequestParam(value = "env") int env,
                               @RequestParam(value = "type", required = false) String type) {
        boolean isHttp = !StringUtils.isEmpty(type) && "http".equalsIgnoreCase(type);
        return JsonHelper.dataJson(isHttp? AppkeyProviderService.providerNodeHttp(appkey, env): AppkeyProviderService.getProviderNode4Route(appkey, env));
    }

}
