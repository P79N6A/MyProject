package com.sankuai.octo.msgp.controller;

import com.sankuai.msgp.common.model.Env;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.HLBGroup;
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService;
import com.sankuai.octo.msgp.serivce.service.ServiceHlbGroup;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by zhoufeng on 16/8/19.
 */

@Controller
@RequestMapping("/hlb/group")
public class HlbGroupController {
    //创建分组
    @RequestMapping(value = "/{env}/create", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String createGroup(@PathVariable String env, @RequestBody HLBGroup hlbGroup) {
        try {
            return ServiceHlbGroup.saveHlbGroup(hlbGroup).toString();
            } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //删除分组
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteGroup(@RequestParam(value = "env") String env,
                              @RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "group_name") String group_name) {

        return ServiceHlbGroup.deleteHlbGroup(env, appkey, group_name).toString();
    }


    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务节点")
    @ResponseBody
    public String searchGroup(@RequestParam(value = "appkey") String appkey,
                                       @RequestParam(value = "ip", required = false) String ip,
                                       @RequestParam(value = "env",required = false) String env,
                                       @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                                       @RequestParam(value = "type", required = false, defaultValue = "2") int type,
                                       @RequestParam(value = "sort", required = false,defaultValue = "-8") int sort,
                                       Page page) {

        scala.collection.immutable.List<ServiceModels.ProviderNode> list = AppkeyProviderService.getHttpProviderByType(appkey, type, env == null ? Env.prod().toString() : env, ip, status, page, sort);
        return (null != list) ? JsonHelper.dataJson(list) : JsonHelper.errorJson("内部异常");

    }

    //修改分组，全量覆盖
    @RequestMapping(value = "/{env}/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateGroup(@RequestBody HLBGroup hlbGroup,
                              @PathVariable String env) {
        return ServiceHlbGroup.saveHlbGroup(hlbGroup).toString();

    }

    //根据appkey获取当前服务老的分组列表
    @RequestMapping(value = "/listold", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getGroupListByAppkey(@RequestParam(value = "env") String env,
                                       @RequestParam(value = "appkey") String appkey) {
        try {
            return JsonHelper.dataJson(ServiceHlbGroup.getHlbGroupByAppkey(env, appkey));
        } catch (Exception e) {
            return JsonHelper.errorJson("服务器异常");
        }
    }

    //根据appkey获取当前服务的分组列表
    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MNS, function = "查看服务节点")
    @ResponseBody
    public String getGroupListByAppkey(@RequestParam(value = "appkey") String appkey,
                                   @RequestParam(value = "ip", required = false) String ip,
                                   @RequestParam(value = "env",required = false) String env,
                                   @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status,
                                   @RequestParam(value = "type", required = false, defaultValue = "2") int type,
                                   @RequestParam(value = "sort", required = false,defaultValue = "-8") int sort,
                                   Page page
                                   ) {

    scala.collection.immutable.List<ServiceModels.ProviderNode> list = AppkeyProviderService.getHttpProviderByType(appkey, type, env == null ? Env.prod().toString() : env, ip, status, page, sort);
    return (null != list) ? JsonHelper.dataJson(list) : JsonHelper.errorJson("内部异常");


    }
}
