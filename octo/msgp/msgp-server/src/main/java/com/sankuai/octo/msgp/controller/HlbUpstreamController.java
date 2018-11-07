package com.sankuai.octo.msgp.controller;


import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.service.ServiceHlbUpstream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Controller
@RequestMapping("/api/dyups")
public class HlbUpstreamController{

    final String AUTHENTICATE = "JIUN8DYpKDtOLCwo";

    //创建upstream
    @RequestMapping(value="/createUpstream", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String createUpstream(HttpServletRequest request) {
        String authenticate = request.getHeader("authenticate");
        if (!authenticate.equals(AUTHENTICATE)) {
            return JsonHelper.errorJson("你没有权限创建数据");
        }
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceHlbUpstream.addUpstreamData(json);
        } catch (IOException e){
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //删除upstream
    @RequestMapping(value="/deleteUpstream", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteUpstream(HttpServletRequest request) {
        String authenticate = request.getHeader("authenticate");
        if (!authenticate.equals(AUTHENTICATE)) {
            return JsonHelper.errorJson("你没有权限删除数据");
        }
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceHlbUpstream.deleteUpstreamData(json);
        } catch (IOException e){
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //查找upstream
    @RequestMapping(value="/searchUpstream",
                    method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String findUpstream(HttpServletRequest request,
                               @RequestParam(value = "env") String env,
                               @RequestParam(value = "nginx") String nginx,
                               @RequestParam(value = "idc") String idc,
                               @RequestParam(value = "upstream", required = false) String upstream) {
        String authenticate = request.getHeader("authenticate");
        if (!authenticate.equals(AUTHENTICATE)) {
            return JsonHelper.errorJson("你没有权限访问数据");
        }
        String path = null;
        if (upstream != null) {
            if(idc != "shared") {
                path = "/dyups/" + env + "/" + nginx + "/" + idc + "/" + upstream;
            } else {
                return JsonHelper.errorJson("shared not regal");
            }
            return ServiceHlbUpstream.findUpstreamData(path);
        } else {
            if(idc != "shared") {
                path = "/dyups/" + env + "/" + nginx + "/" + idc;
            } else {
                return JsonHelper.errorJson("shared not regal");
            }
            return ServiceHlbUpstream.findUpstreamList(path).toString();
        }
    }

    //删除某个upstream中的server节点
    @RequestMapping(value="/deleteServer", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteServer(HttpServletRequest request) {
        String authenticate = request.getHeader("authenticate");
        if (!authenticate.equals(AUTHENTICATE)) {
            return JsonHelper.errorJson("你没有权限删除数据");
        }
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceHlbUpstream.deleteServer(json);
        } catch (IOException e){
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //修改schedule_strategy,server,check_strategy字段
    @RequestMapping(value="/updateUpstream", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateServer(HttpServletRequest request) {
        String authenticate = request.getHeader("authenticate");
        if (!authenticate.equals(AUTHENTICATE)) {
            return JsonHelper.errorJson("你没有权限修改数据");
        }
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceHlbUpstream.updateUpstreamData(json);
        } catch (IOException e){
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //全量替换server
    @RequestMapping(value="/exchangeServer", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String exchangeServer(HttpServletRequest request) {
        String authenticate = request.getHeader("authenticate");
        if (!authenticate.equals(AUTHENTICATE)) {
            return JsonHelper.errorJson("你没有权限替换数据");
        }
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceHlbUpstream.exchangeServer(json);
        } catch (IOException e){
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value="/searchOperationLog",
            method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String searchOperationLog(HttpServletRequest request,
                                     @RequestParam(value = "upstreamName") String upstreamName,
                                     @RequestParam(value = "startTime") String startTime,
                                     @RequestParam(value = "endTime") String endTime
    ) {
        String authenticate = request.getHeader("authenticate");
        if (!authenticate.equals(AUTHENTICATE)) {
            return JsonHelper.errorJson("你没有权限查询操作日志数据");
        }
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            return JsonHelper.dataJson(ServiceHlbUpstream.searchOperationLog(upstreamName, start, end));
        } catch (java.text.ParseException e){
            return JsonHelper.errorJson("时间格式解析失败");
        }
    }
}