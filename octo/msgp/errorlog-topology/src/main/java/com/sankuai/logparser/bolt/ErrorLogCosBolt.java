package com.sankuai.logparser.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.sankuai.logparser.service.LogFilterService;
import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * cos日志处理Bolt
 * 旧的log4j配置输出到cos_errorlog topic
 */
public class ErrorLogCosBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogCosBolt.class);
    //{"_mt_servername":"hotelsc01","_mt_datetime":"2017-08-08 20:17:26","location":"HystrixProxyServiceImpl.java:436","_mt_millisecond":"568","appkey":"mobile-hotel","_mt_level":"ERROR","rawexception":"","_mt_action":"errorlog","_mt_yearmo":"201708","rawlog":"switch平台获取高星Poi详情调用失败:4DATA_NOT_EXISTS_ERROR","_mt_clientip":"127.0.0.1","splitdt":"20170808"}

    private static final String APP_KEY = "appkey";
    private static final String LOCATION = "location";
    private static final String MESSAGE = "rawlog";
    private static final String EXCEPTION = "rawexception";

    private static final String LOG_LEVEL = "_mt_level";
    private static final String SERVER_NAME = "_mt_servername";
    private static final String DATE_TIME = "_mt_datetime";
    private static final String EXCEPTION_NAME = "exceptionName";

    private static final int MAX_LENGTH = 30000;

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        try {
            String jsonStr = tuple.getString(0);
            Map<String, Object> logMap = (Map<String, Object>) JsonUtil.toMap(jsonStr);
            ParsedLog logRecord = transferCosLog(logMap);
            if (LogFilterService.isFilteredLog(logRecord)) {
                return;
            }
            String appkey = logRecord.getAppkey();
            basicOutputCollector.emit(new Values(appkey, logRecord));
        } catch (Exception e) {
            LOGGER.error("ErrorLogCosBolt execute fail", e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("appkey", "log"));
    }

    public ParsedLog transferCosLog(Map<String, Object> logMap) {
        if (logMap == null || logMap.size() == 0) {
            return null;
        }
        ParsedLog logRecord = new ParsedLog();
        if (logMap.containsKey(APP_KEY)) {
            logRecord.setAppkey((String) logMap.get(APP_KEY));
        } else {
            LOGGER.debug("logMap don't contain appkey.");
            return null;
        }
        if (!logMap.containsKey(LOG_LEVEL)) {
            LOGGER.debug("logMap don't contain _mt_level.");
        } else if (!"ERROR".equalsIgnoreCase(logMap.get(LOG_LEVEL).toString())) {
            LOGGER.debug("appkey={} log _mt_level is not error. level={}", logMap.get(APP_KEY), logMap.get(LOG_LEVEL).toString());
            return null;
        }

        if (logMap.containsKey(MESSAGE)) {
            String message = (String) logMap.get(MESSAGE);
            logRecord.setMessage(message.length() > MAX_LENGTH ? message.substring(0, MAX_LENGTH) : message);
        }
        if (logMap.containsKey(EXCEPTION)) {
            String exception = (String) logMap.get(EXCEPTION);
            logRecord.setException(exception.length() > MAX_LENGTH ? exception.substring(0, MAX_LENGTH) : exception);
        }

        Date logTime = LogFilterService.getLogTime(logMap.get(DATE_TIME), logRecord.getAppkey());
        if (logTime == null) {
            return null;
        }
        logRecord.setLogTime(logTime);

        if (logMap.containsKey(EXCEPTION_NAME)) {
            logRecord.setExceptionName((String) logMap.get(EXCEPTION_NAME));
        }
        if (logMap.containsKey(SERVER_NAME)) {
            logRecord.setHost((String) logMap.get(SERVER_NAME));
        } else {
            LOGGER.warn("logMap don't contain _mt_servername.");
            logRecord.setHost("unknown");
        }
        if (logMap.containsKey(LOCATION)) {
            logRecord.setLocation((String) logMap.get(LOCATION));
        }

        String errorUniqueKey = logRecord.getAppkey() + "_" + UUID.randomUUID().toString();
        logRecord.setUniqueKey(errorUniqueKey);

        replaceXMDTStr(logRecord);
        return logRecord;
    }

    private void replaceXMDTStr(ParsedLog record) {
        String illegalStr = "#XMDT#";
        String replaceStr = "xmdtTag";
        if (StringUtils.isNotBlank(record.getMessage()) && record.getMessage().contains(illegalStr)) {
            record.setMessage(record.getMessage().replace(illegalStr, replaceStr));
        }
        if (StringUtils.isNotBlank(record.getException()) && record.getException().contains(illegalStr)) {
            record.setException(record.getException().replace(illegalStr, replaceStr));
        }
        if (StringUtils.isNotBlank(record.getExceptionName()) && record.getExceptionName().contains(illegalStr)) {
            record.setExceptionName(record.getExceptionName().replace(illegalStr, replaceStr));
        }
    }
}
