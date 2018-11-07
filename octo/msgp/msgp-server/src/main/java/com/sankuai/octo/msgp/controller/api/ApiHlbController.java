package com.sankuai.octo.msgp.controller.api;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.HLBGroup;
import com.sankuai.octo.msgp.serivce.hlb.HlbService;
import com.sankuai.octo.msgp.serivce.service.ServiceHlbGroup;
import com.sankuai.octo.msgp.utils.Result;
import com.sankuai.octo.msgp.utils.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by emma on 2017/5/25.
 */
@Controller
@RequestMapping("/api/hlb/")
public class ApiHlbController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiHlbController.class);

    @RequestMapping(value = "serverPort/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getHttpServerPort(String env, String appkey) {
        try {
            ResultData<Map<String, String>> result = HlbService.getHttpServerPort(env, appkey);
            return result.isSuccess() ? JsonHelper.dataJson(result.getData()) : JsonHelper.errorJson(result.getMsg());
        } catch (Exception e) {
            LOG.error("getHttpServerPort fail", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "serverPort/set", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String setHttpServerPort(String env, String appkey, String svrPort) {
        try {
            Result result = HlbService.setHttpServerPort(env, appkey, svrPort);
            return result.getIsSuccess() ? JsonHelper.dataJson("ok") : JsonHelper.errorJson(result.getMsg());
        } catch (Exception e) {
            LOG.error("setHttpServerPort fail", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "serverPort/delete", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String delHttpServerPort(String env, String appkey) {
        try {
            Result result = HlbService.delHttpServerPort(env, appkey);
            return result.getIsSuccess() ? JsonHelper.dataJson("ok") : JsonHelper.errorJson(result.getMsg());
        } catch (Exception e) {
            LOG.error("delHttpServerPort fail", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //创建分组
    @RequestMapping(value = "group/{env}/create", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String createGroup(@PathVariable String env, @RequestBody HLBGroup hlbGroup) {
        try {
            return ServiceHlbGroup.saveHlbGroup(hlbGroup).toString();
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //删除分组
    @RequestMapping(value = "group/delete", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteGroup(@RequestParam(value = "env") String env,
                              @RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "group_name") String group_name) {

        return ServiceHlbGroup.deleteHlbGroup(env, appkey, group_name).toString();
    }

    //查询分组信息
    @RequestMapping(value = "group/search",
            method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String searchGroup(@RequestParam(value = "env") String env,
                              @RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "group_name") String group_name) {
        try {
            return JsonHelper.dataJson(ServiceHlbGroup.searchHlbGroup(env, appkey, group_name));
        } catch (Exception e) {
            return JsonHelper.errorJson("服务器异常");
        }
    }

    //修改分组，全量覆盖
    @RequestMapping(value = "group/{env}/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateGroup(@RequestBody HLBGroup hlbGroup,
                              @PathVariable String env) {
        return ServiceHlbGroup.saveHlbGroup(hlbGroup).toString();

    }

    //根据appkey获取当前服务的分组列表
    @RequestMapping(value = "group/list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getGroupListByAppkey(@RequestParam(value = "env") String env,
                                       @RequestParam(value = "appkey") String appkey) {
        try {
            return JsonHelper.dataJson(ServiceHlbGroup.getHlbGroupByAppkey(env, appkey));
        } catch (Exception e) {
            return JsonHelper.errorJson("服务器异常");
        }
    }
}
