package com.sankuai.octo.aggregator.controller;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.octo.aggregator.MafkaService;
import com.sankuai.octo.aggregator.operation.Opt;
import com.sankuai.octo.aggregator.thrift.LogCollectorServiceImpl;
import com.sankuai.octo.aggregator.thrift.service.LogCollectorService;
import com.sankuai.octo.statistic.helper.api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;


@Controller
public class LogController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    LogCollectorService.Iface logCollectorService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String hello() {
        return api.jsonStr("hell world");
    }

    @RequestMapping(value = "/apps/statistic/node", method = RequestMethod.POST)
    @ResponseBody
    public String appkeyRecvCount(HttpServletRequest request) {
        String json;
        try {
            BufferedReader bRead = request.getReader();
            json = IOUtils.copyToString(bRead);
            return api.jsonStr(Opt.getStatisticNode(json));
        } catch (IOException e) {
            return api.jsonStr(e.getMessage());
        }
    }

}
