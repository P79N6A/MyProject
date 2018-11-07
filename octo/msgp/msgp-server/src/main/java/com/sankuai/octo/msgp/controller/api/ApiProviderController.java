package com.sankuai.octo.msgp.controller.api;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.service.org.UserService;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.*;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.serivce.service.ServiceProvider;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping("/api/provider")
public class ApiProviderController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiProviderController.class);


    /**
     * @param appkey
     * @param env    prod/stage/test
     * @param type   http/thirft
     * @param page
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务节点")
    @ResponseBody
    public String provider(@RequestParam("appkey") String appkey,
                           @RequestParam(value = "env", required = false, defaultValue = "3") Integer env,
                           @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                           @RequestParam("type") int type,
                           @RequestParam(value = "ip", required = false) String ip,
                           Page page) {
        scala.collection.immutable.List<ServiceModels.ProviderSimple> list = AppkeyProviderService.apiProvider(appkey, type, env, ip, status, page);
        return (null != list) ? JsonHelper.dataJson(list, page) : JsonHelper.errorJson("内部异常");
    }

    /**
     * 指定状态获取所有的服务提供者节点 只提供IP,port
     */
    @RequestMapping(value = "simple", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hosts(@RequestParam(value = "appkey") String appkey,
                        @RequestParam(value = "env", required = false, defaultValue = "3") Integer env,
                        @RequestParam(value = "status", required = false) Integer status) {
        try {
            return JsonHelper.dataJson(AppkeyProviderService.getProvider(appkey, env, status));
        } catch (Exception e) {
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }


    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "保存服务节点")
    @ResponseBody
    public String saveProviders(@RequestBody AppKeyProviderEdit providerEdit) {
        try {
            UserService.bindUser(providerEdit.getUsername());
            if (providerEdit.getNodes().isEmpty()) {
                return JsonHelper.errorJson("参数不能为空");
            }
            LOG.info("保存服务节点:" + providerEdit.toString());
            return AppkeyProviderService.saveProviderEditList(providerEdit);
        } catch (Exception e) {
            LOG.error("保存服务节点失败", e);
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }

    /**
     * 修改指定appkey 下的的IP 状态
     */
    @RequestMapping(value = "cellar/create", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "创建celler节点")
    @ResponseBody
    public String createCeller(@RequestBody CellarNode cellarNode) {
        try {
            UserService.bindUser(cellarNode.getUsername());
            ServiceCommon.createCellar(cellarNode);
            return JsonHelper.dataJson("ok");
        } catch (Exception e) {
            LOG.error("创建celler节点失败，cellarNode:" + cellarNode.toString(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 修改指定appkey 下的的IP 状态
     */
    @RequestMapping(value = "update/status", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "编辑服务节点")
    @ResponseBody
    public String updateStatus(@RequestBody AppKeyProviderStatus appKeyProviderStatus) {
        try {
            UserService.bindUser(appKeyProviderStatus.getUsername());
            String data = AppkeyProviderService.updateProviderStatus(appKeyProviderStatus);
            LOG.info("修订服务节点状态:" + appKeyProviderStatus.toString() + ",返回值:" + data);
            return data;
        } catch (Exception e) {
            LOG.error("修订服务节点状态失败：" + appKeyProviderStatus.toString(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 修改appkey 的IP与Port 状态
     */
    @RequestMapping(value = "updatenode/status", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "编辑服务节点")
    @ResponseBody
    public String updateNodeStatus(@RequestBody AppKeyProviderNodeStatus appKeyProviderNodeStatus) {
        try {
            UserService.bindUser(appKeyProviderNodeStatus.getUsername());
            LOG.info("编辑服务节点：" + appKeyProviderNodeStatus.toString());
            return AppkeyProviderService.updateProviderNodeStatus(appKeyProviderNodeStatus);
        } catch (Exception e) {
            LOG.error("编辑服务节点：" + appKeyProviderNodeStatus.toString(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 获取 服务节点状态
     * 废弃中
     */
    @RequestMapping(value = "getstatus", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取服务节点")
    @ResponseBody
    public String getStatus(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "appkey", required = true) String appkey,
            @RequestParam(value = "ips", required = true) String ips
    ) {
        try {
            UserService.bindUser(username);
            return AppkeyProviderService.getProviderProtocolStatus(appkey, ips);
        } catch (Exception e) {
            LOG.error("获取服务节点状态失败:appkey:" + appkey + "ips：" + ips, e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 返回 服务节点的类型和 节点状态
     */
    @RequestMapping(value = "nodestatus", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取服务节点")
    @ResponseBody
    public String nodeStatus(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "appkey", required = true) String appkey,
            @RequestParam(value = "env", required = false, defaultValue = "prod") String env,
            @RequestParam(value = "ips", required = true) String ips
    ) {
        try {
            UserService.bindUser(username);
            LOG.info("获取服务节点状态，username:" + username + ",appkey:" + appkey + ",env:" + env + ",ips:" + ips);
            return AppkeyProviderService.getProviderNodeProtocolStatus(appkey, env, ips);
        } catch (Exception e) {
            LOG.error("获取服务节点状态失败，username:" + username + ",appkey:" + appkey + ",env:" + env + ",ips:" + ips, e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/{type}/{username}/edit", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "编辑服务节点")
    @ResponseBody
    public String updateProviders(@PathVariable("appkey") String appkey,
                                  @PathVariable("type") int type,
                                  @PathVariable("username") String username,
                                  HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            if (StringUtil.isBlank(json) || StringUtil.isBlank(type) || StringUtil.isBlank(username)) {
                return JsonHelper.errorJson("参数不能为空");
            }
            LOG.info("编辑服务节点，username:,appkey:" + appkey + ",type:" + type + "username:" + username + ",json:" + json);
            UserService.bindUser(username);
            return AppkeyProviderService.updateProviderListByType(appkey, username, type, json);
        } catch (IOException e) {
            LOG.error("编辑服务节点失败", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 获取 服务端口：如果服务当前环境存在节点，那么获取某个节点的端口，否则获取失败
     */
    @RequestMapping(value = "getport", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "获取服务端口")
    @ResponseBody
    public String getHttpPort(
            @RequestParam(value = "appkey", required = true) String appkey,
            @RequestParam(value = "env", required = true) int env) {
        int port = ServiceProvider.getProviderPort(appkey, env);
        if (port != 0) {
            return JsonHelper.dataJson(port);
        } else {
            return JsonHelper.errorDataJson("none exist ip");
        }
    }

    /**
     * 获取指定ip下的appkey
     *
     * @param ips
     * @param username
     * @param status
     * @return
     */
    @RequestMapping(value = "service", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getServiceByIP(
            @RequestParam(value = "ips", required = true) String ips,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "username", required = true) String username) {
        LOG.info("根据IP查询服务,ips: {}, username:{},status:{}", ips, username, status);
        return JsonHelper.dataJson(ServiceCommon.getServiceByIp(ips.trim(), status));
    }

    /**
     * 1：关闭 指定ip的机器，删除所有相关的服务下的服务节点
     * 2：删除失败需要周知用户
     *
     * @param ip
     * @param username
     * @param request
     * @return
     */
    @RequestMapping(value = "shutdown", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String delProviderByIP(@RequestParam(value = "ip", required = false) String ip,
                                  @RequestParam(value = "username", required = false) String username,
                                  HttpServletRequest request) {
        if (StringUtils.isEmpty(ip)) {
            try {
                String json = IOUtils.copyToString(request.getReader());
                LOG.info("shutdown，params:" + json);
                return ServiceCommon.batchDelProviderByIPs(json);
            } catch (Exception e) {
                LOG.error("shutdown error", e);
                return JsonHelper.errorJson("shutdown error");
            }
        } else {
            return ServiceCommon.delProviderByIP(ip.trim(), username);
        }
    }

    /**
     * 1：禁用 指定ip的机器，删除所有相关的服务下的服务节点
     * 2：禁用 失败需要周知用户
     *
     * @param ip
     * @param username
     * @param request
     * @return
     */
    @RequestMapping(value = "disable", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String disableProviderByIP(@RequestParam(value = "ip", required = false) String ip,
                                      @RequestParam(value = "username", required = false) String username,
                                      HttpServletRequest request) {
        if (StringUtils.isEmpty(ip)) {
            try {
                String json = IOUtils.copyToString(request.getReader());
                LOG.info("禁用节点，params:" + json);
                return ServiceCommon.batchDisableProviderByIPs(json);
            } catch (Exception e) {
                LOG.error("disable error", e);
                return JsonHelper.errorJson("disable error");
            }
        } else {
            return ServiceCommon.delProviderByIP(ip.trim(), username);
        }
    }


    /**
     * 删除指定节点
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String delete(@RequestBody ProviderDel providerDel) {
        UserService.bindUser(providerDel.getUsername());
        LOG.info("删除服务提供者,username:" + providerDel.getUsername() + ",providerDel:" + providerDel.toString());
        try {
            AppkeyProviderService.delProvider(providerDel);
            return JsonHelper.dataJson("ok");
        } catch (Exception e) {
            LOG.error("删除服务提供者,username:" + providerDel.getUsername() + ",providerDel:" + providerDel.toString(), e);
            return JsonHelper.errorJson("删除节点失败");
        }

    }

    /**
     * 删除指定的serviceinfo信息，供上海pigeon清理使用，暂时支持线下环境，thrift使用
     */
    @RequestMapping(value = "deleteServiceInfo", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteServiceInfo(@RequestBody ProviderServiceInfoDel providerServiceInfoDel) {
        if (CommonHelper.isOffline()) {
            UserService.bindUser(providerServiceInfoDel.getUsername());
            LOG.info("删除服务信息,username:" + providerServiceInfoDel.getUsername() + ",providerServiceInfoDel:" + providerServiceInfoDel.toString());
            String[] result = AppkeyProviderService.delProviderServiceInfoByIpPort(providerServiceInfoDel);
            if (result[0].equalsIgnoreCase("0")) {
                return JsonHelper.dataJson("删除成功!");
            } else {
                return JsonHelper.errorDataJson(result[1]);
            }
        } else {
            return JsonHelper.errorDataJson("非线下环境禁止操作");
        }
    }


}
