package com.sankuai.octo.msgp.controller.appkey;

import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.dao.appkey.AppkeyProviderDao;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by zava on 16/8/23.
 */
@Controller
@Worth(model = Worth.Model.OTHER)
@RequestMapping("/appkey/provider")
public class AppkeyProviderController {

    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询服务")
    @ResponseBody
    public String statusSearch(@RequestParam(value = "appkey",required = false) String appkey,
                        @RequestParam(value = "env",required = false) Integer env,
                              @RequestParam(value = "type",required = false) String type,
                              @RequestParam(value = "status",required = false) Integer status,
                              Page page) {
        scala.collection.immutable.List<AppkeyProviderDao.ProviderCount> list = AppkeyProviderDao.statusSearch(appkey, type, env, status, page);
        return JsonHelper.dataJson(list, page);
    }
    @RequestMapping(value = "/idc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询服务")
    @ResponseBody
    public String idcSearch(@RequestParam(value = "appkey",required = false) String appkey,
                         @RequestParam(value = "env",required = false) Integer env,
                         @RequestParam(value = "type",required = false) String type,
                            @RequestParam(value = "status",required = false) Integer status,
                         @RequestParam(value = "idc",required = false) String idc,
                         Page page) {
        scala.collection.immutable.List<AppkeyProviderDao.ProviderCount> list = AppkeyProviderDao.idcSearch(appkey, type, env,status, idc, page);
        return JsonHelper.dataJson(list, page);
    }

    @RequestMapping(value = "/outline/status", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询服务")
    @ResponseBody
    public String statusOutline(@RequestParam(value = "env",required = false) Integer env,
                          @RequestParam(value = "type",required = false) String type,
                          @RequestParam(value = "status",required = false) Integer status,
                          @RequestParam(value = "idc",required = false) String idc
    ){
        AppkeyProviderService.AppkeyProviderOutline  outline = AppkeyProviderService.getOutline(type, env, status, null,"status");
        return JsonHelper.dataJson(outline);
    }
    @RequestMapping(value = "/outline/idc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查询服务")
    @ResponseBody
    public String idcOutline(@RequestParam(value = "env",required = false) Integer env,
                          @RequestParam(value = "type",required = false) String type,
                          @RequestParam(value = "status",required = false) Integer status,
                          @RequestParam(value = "idc",required = false) String idc
    ){
        AppkeyProviderService.AppkeyProviderOutline  outline = AppkeyProviderService.getOutline(type, env, null,idc,"idc");
        return JsonHelper.dataJson(outline);
    }
}
