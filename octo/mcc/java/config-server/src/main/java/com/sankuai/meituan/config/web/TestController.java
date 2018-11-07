/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 *
 */

package com.sankuai.meituan.config.web;

import com.google.common.collect.Lists;
import com.sankuai.meituan.config.model.APIResponse;
import com.sankuai.meituan.config.service.SgNotifyService;
import com.sankuai.meituan.config.service.ZookeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;

/**
 * @author liuxu<liuxu04@meituan.com>
 */
@Controller
@RequestMapping("/test")
public class TestController {
    private static final Logger LOG = LoggerFactory.getLogger(TestController.class);

    @Resource
    private ZookeeperService zookeepeService;
    @Resource
    private SgNotifyService sgNotifyService;

    @RequestMapping(value = "/nullObjectData", method = RequestMethod.GET)
    @ResponseBody
    public Object nullObjectData() {

        return APIResponse.newResponse(true, null);
    }

    @RequestMapping(value = "/nullListData", method = RequestMethod.GET)
    @ResponseBody
    public Object nullListData() {

        return APIResponse.newResponse(true, (List) null);
    }

    @RequestMapping(value = "/nullListElementData", method = RequestMethod.GET)
    @ResponseBody
    public Object nullListElementData() {

        return APIResponse.newResponse(true, Lists.newArrayList(null, null));
    }

    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    @ResponseBody
    public Object properties() {

        Properties properties = new Properties();
        properties.setProperty("a", "1");
        properties.setProperty("b", "2");

        return properties;
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ResponseBody
    public Object config() {

        return "yes";
    }

    @RequestMapping(value = "/adaptor", method = RequestMethod.GET)
    @ResponseBody
    public Object adaptor() {

        return "ok";
    }

    @RequestMapping(value = "/log", method = RequestMethod.GET)
    @ResponseBody
    public Object log() {
        com.meituan.jmonitor.LOG.error("test");
        return "log";
    }
    @RequestMapping(value = "/updatenotifyips", method = RequestMethod.GET)
    @ResponseBody
    public Object updateNotifyIps() {
        sgNotifyService.flushDeprecatedRelation();
        return "true";
    }

    @RequestMapping(value = "/monitor/log", method = RequestMethod.GET)
    @ResponseBody
    public String testErrorLog(@RequestParam("level") String level) {
        String msg = "this is test "+level+" log, pls ignore it.";
        if("error".equalsIgnoreCase(level)){
            LOG.error(msg);
        }else if("warn".equalsIgnoreCase(level)){
            LOG.warn(msg);
        }else if("info".equalsIgnoreCase(level)){
            LOG.info(msg);
        } else if("debug".equalsIgnoreCase(level)){
            LOG.debug(msg);
        }
        return msg;
    }
}
