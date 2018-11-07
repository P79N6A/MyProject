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
 * mt_errorlog 日志处理Bolt
 * 只处理新log4j2 输出的log, mt_errorlog
 */
public class ErrorLogMtBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogMtBolt.class);

    private static final String APP_KEY = "_mt_appkey";
    private static final String MESSAGE = "message";
    private static final String EXCEPTION = "exception";
    private static final String LOGGER_CLASS = "_mt_logger_name";
    private static final String DATE_TIME = "_mt_datetime";
    private static final String SERVER_NAME = "_mt_servername";
    private static final String EXCEPTION_NAME = "exceptionName";
    private static final String TRACE_ID = "traceID";
    private static final String TRACE_ID_NEW = "__traceId__";
    private static final String HOST_SET_INFO= "_mt_client_cell_";
    private static final String ENV = "_mt_client_env_";

    private static final int MAX_LENGTH = 30000;

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try {
            String jsonLog = tuple.getString(0);
            Map<String, Object> logMap = (Map<String, Object>) JsonUtil.toMap(jsonLog);
            ParsedLog logRecord = transfer(logMap);
            if (LogFilterService.isFilteredLog(logRecord)) {
                return;
            }
            String appkey = logRecord.getAppkey();
            collector.emit(new Values(appkey, logRecord));
        } catch (Exception e) {
            LOGGER.error("ErrorLogMtBolt execute fail", e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("appkey", "log"));
    }

    public ParsedLog transfer(Map<String, Object> logMap) {
        if (logMap == null || logMap.size() == 0) {
            return null;
        }

        ParsedLog logRecord = new ParsedLog();

        if (logMap.containsKey(APP_KEY)) {
            logRecord.setAppkey((String) logMap.get(APP_KEY));
        } else {
            LOGGER.debug("logMap don't contain appkey. {}", logMap);
            return null;
        }
        getLogMessageAndException(logRecord, logMap.get(MESSAGE), logMap.get(EXCEPTION));

        if (logMap.containsKey(LOGGER_CLASS)) {
            logRecord.setLocation((String) logMap.get(LOGGER_CLASS));
        }

        if (logMap.containsKey(SERVER_NAME)) {
            logRecord.setHost((String) logMap.get(SERVER_NAME));
        }

        Date logTime = LogFilterService.getLogTime(logMap.get(DATE_TIME), logRecord.getAppkey());
        if (logTime == null) {
            return null;
        }
        logRecord.setLogTime(logTime);

        if (logMap.containsKey(EXCEPTION_NAME)) {
            logRecord.setExceptionName((String) logMap.get(EXCEPTION_NAME));
        }
        if (logMap.containsKey(TRACE_ID)) {
            logRecord.setTraceId((String) logMap.get(TRACE_ID));
        } else if (logMap.containsKey(TRACE_ID_NEW)) {
            logRecord.setTraceId((String) logMap.get(TRACE_ID_NEW));
        }

        if (logMap.containsKey(HOST_SET_INFO)) {
            String hostSet = (String) logMap.get(HOST_SET_INFO);
            if (StringUtils.isNotBlank(hostSet) && !ParsedLog.SET_LOGCENTER_DEFAULT_VAL.equals(hostSet)) {
                logRecord.setHostSet(hostSet);
            }
        }

        if (logMap.containsKey(ENV)) {
            String env = (String) logMap.get(ENV);
            if (StringUtils.isNotBlank(env) && !ParsedLog.ENV_LOGCENTER_DEFAULT_VAL.equals(env)) {
                logRecord.setEnv(env);
            }
        }

        String errorUniqueKey = logRecord.getAppkey() + "_" + UUID.randomUUID().toString();
        logRecord.setUniqueKey(errorUniqueKey);
        return logRecord;
    }

    private void getLogMessageAndException(ParsedLog log, Object message, Object exception) {
        if (message != null && StringUtils.isNotBlank(message.toString())) {
            String errorMessage = message.toString();
            log.setMessage(errorMessage.length() > MAX_LENGTH ? errorMessage.substring(0, MAX_LENGTH) : errorMessage);
        }
        if (exception != null && StringUtils.isNotBlank(exception.toString())) {
            String errorException = exception.toString();
            log.setException(errorException.length() > MAX_LENGTH ? errorException.substring(0, MAX_LENGTH) : errorException);
        } else {
            // error_exception是从error_message以\n换行符截断的(对应数据组的message, exception字段), 数据组新的日志解析逻辑不再截断(因简单的\n截断会误提取),
            // 但导致没有error_exception字段, 所以这里增加判断如果error_exception为空, 则从error_message截断提取, 兼容之前的展示
            String errorInfo = log.getMessage();
            String[] strs = errorInfo.split("\n");
            String errorMsg = errorInfo;
            String errorException = "";
            if (strs.length > 1) {
                errorMsg = strs[0];
                errorException = errorInfo.substring(errorMsg.length() + 1);
            }
            log.setMessage(errorMsg);
            log.setException(errorException);
        }
    }
}
