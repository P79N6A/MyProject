package com.sankuai.msgp.errorlog.service;

import com.alibaba.fastjson.JSONObject;
import com.meituan.inf.xmdlog.XMDLogFormat;
import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.errorlog.constant.ErrorLogFilterRuleCondition;
import com.sankuai.msgp.errorlog.constant.ErrorLogFilterStatus;
import com.sankuai.msgp.errorlog.constant.ErrorLogRuleKey;
import com.sankuai.msgp.errorlog.dao.ErrorLogFilterDao;
import com.sankuai.msgp.errorlog.domain.ErrorLogParsedFilter;
import com.sankuai.msgp.errorlog.pojo.ErrorLogFilter;
import com.sankuai.msgp.errorlog.pojo.ErrorLogStatistic;
import com.sankuai.msgp.errorlog.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ErrorLogFilterService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogFilterService.class);
    private static final Logger errorLog = LoggerFactory.getLogger("ErrorLog");
    public final static Integer FILTER_ID_OTHERS = 0;
    private static final int MAX_LENGTH = 30000;

    @Resource
    private ErrorLogFilterDao errorLogFilterDao;

    @Resource
    private ErrorLogStatisticService errorLogStatisticService;
    @Resource
    private ApplicationContext applicationContext;

    @Cacheable(cacheNames = "LRUCache-1m", key = "#appkey")
    public List<ErrorLogFilter> selectEnabledFiltersByAppkey(String appkey) {
        if (StringUtils.isBlank(appkey)) {
            return Collections.emptyList();
        }
        return errorLogFilterDao.selectBy(appkey, ErrorLogFilterStatus.ACTIVE.getValue(), true);
    }

    public Boolean handleLog(ParsedLog log) {
        List<ErrorLogParsedFilter> parsedFilters = getParsedFiltersByAppkey(log.getAppkey());

        if (log == null) {
            return Boolean.FALSE;
        }
        String appkey = log.getAppkey();
        if (StringUtils.isBlank(appkey)) {
            return Boolean.FALSE;
        }
        if (parsedFilters == null) {
            parsedFilters = getParsedFiltersByAppkey(log.getAppkey());
        }
        if (StringUtils.isEmpty(log.getExceptionName())) {
            log.setExceptionName("Others");
        }
        Boolean isAlarm = Boolean.FALSE;
        Boolean isFiltered = Boolean.FALSE;
        Map<String, String> tags = getLogTags(log);
        String tagsJson = JsonUtil.toString(tags);
        XMDLogFormat xmdLog;
        if (tagsJson == null) {
            xmdLog = XMDLogFormat.build().putTags(tags);
        } else {
            xmdLog = XMDLogFormat.build().putJson(tagsJson);
        }
        for (ErrorLogParsedFilter filter : parsedFilters) {
            if (isSatisfyFilter(log, filter)) {
                int duplicateCount = isFiltered ? 1 : 0;
                isFiltered = Boolean.TRUE;
                if (filter.getAlarm()) {
                    isAlarm = Boolean.TRUE;
                    log.setFilterId(filter.getId());
                    log.setExceptionName(filter.getName());
                    log.setThresholdMin(filter.getThresholdMin());
                }
                errorLog.info(xmdLog.putTag("error_filter_id", filter.getId().toString()).toString());
                //计数，可能匹配到多个, 重复匹配时 duplicateCount = 1
                ErrorLogStatistic errorLogStatistic = new ErrorLogStatistic(0L, CommonUtil.getMinuteStart((int) (log.getLogTime().getTime() / 1000)),
                        log.getAppkey(), log.getHostSet(), log.getHost(), filter.getId(), filter.getName(), 1, duplicateCount);
                errorLogStatisticService.saveStatistic(errorLogStatistic);
                if (filter.getTerminate()) {
                    break;
                }
            }
        }
        // 匹配filter中有报警配置
        // 或者，没有filter匹配
        if (!isFiltered) {
            //计数others
            ErrorLogStatistic errorLogStatistic = new ErrorLogStatistic(0L, CommonUtil.getMinuteStart((int) (log.getLogTime().getTime() / 1000)),
                    log.getAppkey(), log.getHostSet(), log.getHost(), FILTER_ID_OTHERS, log.getExceptionName(), 1, 0);
            errorLogStatisticService.saveStatistic(errorLogStatistic);
            log.setFilterId(FILTER_ID_OTHERS);
            errorLog.info(xmdLog.putTag("error_filter_id", "" + log.getFilterId()).toString());
            isAlarm = Boolean.TRUE;
        }
        return isAlarm;
    }

    private Map<String, String> getLogTags(ParsedLog log) {
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put("error_appkey", log.getAppkey());
        if (null != log.getLogTime()) {
            tagsMap.put("error_time", String.valueOf(log.getLogTime().getTime()));
        } else {
            tagsMap.put("error_time", String.valueOf(System.currentTimeMillis()));
        }

        tagsMap.put("error_host", log.getHost());
        tagsMap.put("error_exception_name", log.getExceptionName());
        tagsMap.put("error_location", log.getLocation());

        String errorMessage = log.getMessage();
        String errorException = log.getException();
        if (errorMessage.length() > MAX_LENGTH) {
            tagsMap.put("error_message", subStrByByte(errorMessage, MAX_LENGTH));
        } else {
            tagsMap.put("error_message", errorMessage);
        }
        if (errorException.length() > MAX_LENGTH) {
            tagsMap.put("error_exception", subStrByByte(errorException, MAX_LENGTH));
        } else {
            tagsMap.put("error_exception", errorException);
        }

        if (StringUtils.isBlank(log.getTraceId())) {
            tagsMap.put("error_trace_id", "0");
        } else {
            tagsMap.put("error_trace_id", log.getTraceId());
        }

        tagsMap.put("error_host_set", log.getHostSet());

        tagsMap.put("error_env", log.getEnv());

        if (StringUtils.isBlank(log.getUniqueKey())) {
            String errorUniqueKey = log.getAppkey() + "_" + UUID.randomUUID().toString();
            log.setUniqueKey(errorUniqueKey);
            tagsMap.put("error_unique_key", errorUniqueKey);
        } else {
            tagsMap.put("error_unique_key", log.getUniqueKey());
        }
        return tagsMap;
    }

    private Boolean isSatisfyFilter(ParsedLog log, ErrorLogParsedFilter filter) {
        Map<String, List<Pattern>> ruleMap = filter.getParsedRules();
        if (ruleMap.isEmpty()) {
            return Boolean.FALSE;
        }
        // all of
        if (ErrorLogFilterRuleCondition.ALLOF.getIndex().equals(filter.getRuleCondition())) {
            if (ruleMap.containsKey(ErrorLogRuleKey.MESSAGE.getValue())) {
                for (Pattern rule : ruleMap.get(ErrorLogRuleKey.MESSAGE.getValue())) {
                    if (!match(log.getMessage(), rule)) {
                        return Boolean.FALSE;
                    }
                }
            }
            if (ruleMap.containsKey(ErrorLogRuleKey.EXCEPTION.getValue())) {
                for (Pattern rule : ruleMap.get(ErrorLogRuleKey.EXCEPTION.getValue())) {
                    if (!match(log.getException(), rule)) {
                        return Boolean.FALSE;
                    }
                }
            }
            if (ruleMap.containsKey(ErrorLogRuleKey.HOSTNAME.getValue())) {
                for (Pattern rule : ruleMap.get(ErrorLogRuleKey.HOSTNAME.getValue())) {
                    if (!match(log.getHost(), rule)) {
                        return Boolean.FALSE;
                    }
                }
            }
            return Boolean.TRUE;
        }
        // any of
        else if (ErrorLogFilterRuleCondition.ANYOF.getIndex().equals(filter.getRuleCondition())) {
            if (ruleMap.containsKey(ErrorLogRuleKey.MESSAGE.getValue())) {
                for (Pattern rule : ruleMap.get(ErrorLogRuleKey.MESSAGE.getValue())) {
                    if (match(log.getMessage(), rule)) {
                        return Boolean.TRUE;
                    }
                }
            }
            if (ruleMap.containsKey(ErrorLogRuleKey.EXCEPTION.getValue())) {
                for (Pattern rule : ruleMap.get(ErrorLogRuleKey.EXCEPTION.getValue())) {
                    if (match(log.getException(), rule)) {
                        return Boolean.TRUE;
                    }
                }
            }
            if (ruleMap.containsKey(ErrorLogRuleKey.HOSTNAME.getValue())) {
                for (Pattern rule : ruleMap.get(ErrorLogRuleKey.HOSTNAME.getValue())) {
                    if (match(log.getHost(), rule)) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }
        // others
        return Boolean.FALSE;
    }

    public List<ErrorLogParsedFilter> getParsedFiltersByAppkey(String appkey) {
        // 调用同类的缓存方法没有缓存效果, 需要使用代理
        ErrorLogFilterService proxy = applicationContext.getBean(ErrorLogFilterService.class);
        List<ErrorLogFilter> filters = proxy.selectEnabledFiltersByAppkey(appkey);
        List<ErrorLogParsedFilter> parsedFilters = new ArrayList<ErrorLogParsedFilter>(filters.size());
        for (ErrorLogFilter filter : filters) {
            ErrorLogParsedFilter parsedFilter = new ErrorLogParsedFilter();
            BeanUtils.copyProperties(filter, parsedFilter);
            parsedFilter.setParsedRules(parseErrorLogRule(filter.getRules()));
            parsedFilters.add(parsedFilter);
        }
        return parsedFilters;
    }

    private Map<String, List<Pattern>> parseErrorLogRule(String json) {
        Map<String, List<String>> rules = null;
        try {
            rules = (Map<String, List<String>>) JSONObject.parseObject(json, Map.class);
        } catch (Exception e) {
            rules = null;
            logger.error("parse rule error: " + json, e);
        }
        if (rules == null) {
            return Collections.emptyMap();
        }
        Map<String, List<Pattern>> parsedRules = new HashMap<String, List<Pattern>>();
        for (String key : rules.keySet()) {
            if (!parsedRules.containsKey(key)) {
                parsedRules.put(key, new ArrayList<Pattern>());
            }
            if (rules.get(key) == null) {
                continue;
            }
            for (String rule : rules.get(key)) {
                parsedRules.get(key).add(Pattern.compile(rule));
            }
        }
        return parsedRules;
    }

    public Boolean match(String str, Pattern pattern) {
        if (StringUtils.isBlank(str) || pattern == null) {
            return Boolean.FALSE;
        }
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 按字节数截取
     *
     * @param input
     * @param length
     * @return
     */
    public static String subStrByByte(String input, int length) {
        int len = 0;
        try {
            int characterNum = input.length();
            int total = 0;
            //应当截取到的字符的长度
            len = 0;
            for (int i = 0; i < characterNum; i++) {
                String temp = input.substring(i, i + 1);
                int tempLen = temp.getBytes("UTF-8").length;
                total += tempLen;
                if (total <= length) {
                    len++;
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("截取字符串失败，input：" + input, e);
        }
        return input.substring(0, len);
    }
}