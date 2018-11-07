package com.sankuai.octo.msgp.controller.service;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.msgp.serivce.service.ServiceAccessCtrl;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by lhmily on 09/28/2015.
 */
@Controller
@RequestMapping("/service/accessctrl")
@Auth(level = Auth.Level.OBSERVER, responseMode = Auth.ResponseMode.JSON)
@Worth(model = Worth.Model.AUTH)
public class AccessCtrlController {
    private static final Logger LOG = LoggerFactory.getLogger(AccessCtrlController.class);

    @Worth(model = Worth.Model.AUTH, function = "查看配置")
    @RequestMapping(value = "{appkey}/accessData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String accessData(@PathVariable("appkey") String appkey,
                             @RequestParam("env") int env,
                             @RequestParam("type") int type) {
        try {
            return JsonHelper.dataJson(ServiceAccessCtrl.getAccessData(appkey, env, type));
        } catch (NoNodeException e) {
            return JsonHelper.dataJson("");
        } catch (IllegalArgumentException e) {
            //参数非法
            return JsonHelper.errorJson("非法访问");
        } catch (Exception e) {
            return JsonHelper.errorJson("服务器异常");
        }
    }

    @Worth(model = Worth.Model.AUTH, function = "修改配置")
    @RequestMapping(value = "{appkey}/saveData/{env}/{type}", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String saveData(@PathVariable("appkey") String appkey,
                           @PathVariable("env") int env,
                           @PathVariable("type") String type,
                           HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceAccessCtrl.saveRegistryData(appkey, env, type, json) ? JsonHelper.dataJson("保存成功") : JsonHelper.errorJson("保存失败");

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("服务器异常");
        }
    }

    @RequestMapping(value = "{appkey}/provider/all", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String allProvider(@PathVariable("appkey") String appkey,
                              @RequestParam("env") int env) {
        return JsonHelper.dataJson(AppkeyProviderService.getProviderAllIPs(appkey, env));
    }
}
