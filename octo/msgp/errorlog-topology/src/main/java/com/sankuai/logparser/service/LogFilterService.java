package com.sankuai.logparser.service;

import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by emma on 2017/8/8.
 */
public class LogFilterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFilterService.class);
    private static final long OUT_OF_DATE_SECONDS = 12 * 60 * 60; // 12 hour

    private static BlackListService blackListService = BlackListService.getInstance();

    public static boolean isFilteredLog(ParsedLog log) {
        if (null == log) {
            return true;
        }
        String appkey = log.getAppkey();
        // 不规范Appkey不处理
        if (isNotStandardAppkey(appkey)) {
            LOGGER.warn("appkey={} is not standard, records dropped.", appkey);
            return true;
        }
        // 限制fixed黑名单中Appkey的流量
        if (blackListService.isInFixedBlackList(appkey)) {
            LOGGER.debug("appkey={} is in fixed blacklist, records dropped.", appkey);
            return true;
        }
        // 限制Octo不存在的Appkey
        Set<String> appkeys = AppkeyInfoService.getAllAppkey();
        if (!appkeys.isEmpty() && !appkeys.contains(appkey)) {
            LOGGER.debug("appkey={} is not in Octo, records dropped.", appkey);
            return true;
        }
        return false;
    }

    public static Date getLogTime(Object logTimeParam, String appkey) {
        Date currDate = new Date();
        if (logTimeParam == null) {
            return currDate;
        }

        Date logTime = DateTimeUtil.parse(logTimeParam.toString(), DateTimeUtil.DATE_TIME_FORMAT);
        if (logTime == null) {
            logTime = DateTimeUtil.parse(logTimeParam.toString(), "\"yyyy/MM/dd HH:mm:ss\"");
            logTime = logTime == null ? currDate : logTime;
            return logTime;
        } else if ((logTime.getTime() / 1000) < (currDate.getTime() / 1000 - OUT_OF_DATE_SECONDS)) {
            LOGGER.warn("Log 12 hours ago, appkey={}, logTime={}", appkey, logTimeParam.toString());
            return null;
        } else if (logTime.getTime() > currDate.getTime()) {
            LOGGER.warn("Log time exceeded currTime, appkey={} logTime={}", appkey, logTime);
            return currDate;
        }
        return logTime;
    }

    private static boolean isNotStandardAppkey(String appkey) {
        if (StringUtils.isBlank(appkey) || appkey.length() > 64) {
            return true;
        }
        String pattern = ".*[\\[\\]=@*${}:?();#\\/\\\\]{1,}.*";
        return Pattern.matches(pattern, appkey);
    }
}