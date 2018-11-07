package com.sankuai.octo.msgp.controller.api;

import com.sankuai.octo.msgp.model.flowCopy.FlowCopyPtestConfig;
import com.sankuai.octo.msgp.service.flowCopy.FlowCopyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 流量录制api，开放给ptest作为流量录制入口以及流量录制任务状态查询
 * 参考 <a href="https://123.sankuai.com/km/page/28409681">RPC 流量录制 - octo 与 ptest对接接口</a>
 * <p>
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/13
 * Time: 17:36
 */
@Controller
@RequestMapping("/api/vcr")
public class ApiFlowCopyController {

    /**
     * 发起流量录制
     *
     * @param flowCopyPtestConfig
     * @return
     *
     */
    @ResponseBody
    @RequestMapping(value = "/ptestVcrStart", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public String ptestVcrStart(@RequestBody FlowCopyPtestConfig flowCopyPtestConfig, HttpServletRequest request) {
        return FlowCopyService.ptestVcrStart(flowCopyPtestConfig, request.getCookies());
    }

    /**
     * 查询流量录制状态
     *
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getRecordTaskPhase(@RequestParam Long taskId) {
        return FlowCopyService.queryRecordTaskPhase(taskId);
    }

    /**
     * 关闭流量录制任务
     *
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/close", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String closeRecordTask(@RequestParam Long taskId) {
        return FlowCopyService.closeRecordTask(taskId);
    }

    /**
     * 查询流量录制任务进度
     *
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/process", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getRecordTaskProcess(@RequestParam Long taskId) {
        return FlowCopyService.getRecordTaskProcess(taskId);
    }
}
