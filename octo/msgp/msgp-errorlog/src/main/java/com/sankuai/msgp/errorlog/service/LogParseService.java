package com.sankuai.msgp.errorlog.service;

import com.meituan.jmonitor.JMonitor;
import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LogParseService {
    private static final Logger LOG = LoggerFactory.getLogger(LogParseService.class);

    @Resource
    private ErrorLogFilterService errorLogFilterService;
    @Resource
    private LogAlarmService logAlarmService;
    @Autowired
    private ErrorLogRouteCfgService routeCfgService;

    private AtomicInteger logCounter = new AtomicInteger();

    public void handleMessage(String error) {
        try {
            ParsedLog record = messageToParsedLog(error);
            if (record != null && errorLogFilterService.handleLog(record)) {
                logAlarmService.addLog(record);
            }
        } catch (Exception e) {
            LOG.error("Error log handleMessage fail, errorLog={}", error, e);
        }
    }

    public ParsedLog messageToParsedLog(String error) {
        ParsedLog record = JsonUtil.toObject(error, ParsedLog.class);
        if (record.getLogTime().getTime() > System.currentTimeMillis()) {
            record.setLogTime(new Date());
        }

        if (null == record || !routeCfgService.isRouteAppkey(record.getAppkey())) {
            return null;
        }
        LOG.debug("collect: {}", error);

        JMonitor.add("errorlog.consume.count");
        printlnLog(record.getAppkey());
        return record;
    }


    private void printlnLog(String appkey) {
        int count = logCounter.incrementAndGet();
        if (count % 1000 == 0) {
            LOG.info("error log count:" + count + ",appkey:" + appkey);
            if (count > 50000) {
                logCounter.set(0);
            }
        }
    }
}
