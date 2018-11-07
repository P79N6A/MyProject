package com.sankuai.msgp.errorlog.task;

import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.AlarmSender;
import com.sankuai.msgp.errorlog.constant.LogAlarmSeverity;
import com.sankuai.msgp.errorlog.domain.LogAlarmConfiguration;
import com.sankuai.msgp.errorlog.pojo.ErrorLogFilter;
import com.sankuai.msgp.errorlog.pojo.ErrorLogFilterCount;
import com.sankuai.msgp.errorlog.pojo.LogAlarmSeverityConfig;
import com.sankuai.msgp.errorlog.service.ErrorLogFilterService;
import com.sankuai.msgp.errorlog.util.DefaultThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-10-15
 */
public class AlarmTask {
    private final Logger logger = LoggerFactory.getLogger(AlarmTask.class);

    public static final Integer MAX_FILTER_ERROR_EACH_ALARM = 2;
    public static final Integer STR_LEN = 100;

    private Boolean running;

    private final String appkey;

    private int gapSeconds;

    private String alarmNode;

    private String trapper;

    private LogAlarmConfiguration configuration;

    private ConcurrentHashMap<Integer, LogMinCount> countOfAlarmFilter;      // group by filter

    private ConcurrentHashMap<Integer, String> logsOfGroup;

    private ErrorLogFilterService errorLogFilterService;

    private class LogMinCount {
        private AtomicInteger minute;
        private AtomicInteger logCount;

        public LogMinCount(AtomicInteger minute, AtomicInteger logCount) {
            this.minute = minute;
            this.logCount = logCount;
        }

        public int decreaseMin() {
            return this.minute.decrementAndGet();
        }

        public void increseLogCount() {
            this.logCount.incrementAndGet();
        }

        public int getLogCount() {
            return this.logCount.get();
        }
    }

    public AlarmTask(final LogAlarmConfiguration configuration, ErrorLogFilterService errorLogFilterService) {
        this.appkey = configuration.getBasicConfig().getAppkey();
        this.gapSeconds = configuration.getBasicConfig().getGapSeconds();
        this.alarmNode = configuration.getBasicConfig().getAlarmVirtualNode();
        this.trapper = configuration.getBasicConfig().getTrapper();
        this.configuration = configuration;
        this.errorLogFilterService = errorLogFilterService;

        countOfAlarmFilter = new ConcurrentHashMap<>();
        logsOfGroup = new ConcurrentHashMap<>();

        running = Boolean.TRUE;
        final ScheduledExecutorService alarmScheduleService = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("LogAlarmTaskPool", false));

        alarmScheduleService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (running) {
                        String alarmMessage = createAlarmMessage();

                        logger.debug("runing alarm task appkey:" + appkey);
                        logger.debug("send to falcon, appkey={} alarmNode={} trapper={} msg={}", appkey, alarmNode, trapper, alarmMessage);
                        AlarmSender.send(alarmNode, trapper, alarmMessage);
                        logger.debug("AlarmTask is running, appkey=" + appkey);
                    } else {
                        alarmScheduleService.shutdown();
                        logger.debug("AlarmTask is stopped: appkey=" + appkey);
                    }
                } catch (Exception e) {
                    logger.error("AlarmTask timer exception, appkey=" + appkey, e);
                }
            }
        }, 0L, gapSeconds, TimeUnit.SECONDS);
    }

    public void addLog(ParsedLog log) {
        if (log.getFilterId() == null) {
            logger.warn("log.filterId is null, log.rowkey=" + log.getUniqueKey());
            return;
        }

        Integer filterId = log.getFilterId();
        Integer alarmThresholdMin = log.getThresholdMin() == null ? 0 : log.getThresholdMin();

        LogMinCount filterMinLogCount = countOfAlarmFilter.putIfAbsent(filterId, new LogMinCount(
                new AtomicInteger(alarmThresholdMin), new AtomicInteger(1)));
        if (filterMinLogCount != null) {
            countOfAlarmFilter.get(filterId).increseLogCount();
        }

        logsOfGroup.putIfAbsent(filterId, parsedLogToString(log));
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }

    private String createAlarmMessage() {
        ConcurrentHashMap<Integer, LogMinCount> copyLogCounts = new ConcurrentHashMap();
        ConcurrentHashMap<Integer, String> copyLogContents = new ConcurrentHashMap();
        for (Map.Entry<Integer, LogMinCount> logCount : countOfAlarmFilter.entrySet()) {
            Integer filterId = logCount.getKey();
            if (logCount.getValue().decreaseMin() <= 0) {
                // 某个filter时间阈值到0, 开始统计log判断是否报警
                copyLogCounts.put(filterId, logCount.getValue());
                countOfAlarmFilter.remove(filterId);

                copyLogContents.put(filterId, logsOfGroup.remove(filterId));
            }
        }

        Map<Integer, ErrorLogFilter> filterMap = new HashMap<>();
        if (!copyLogCounts.isEmpty()) {
            List<ErrorLogFilter> errorLogFilters = errorLogFilterService.selectEnabledFiltersByAppkey(appkey);
            for (ErrorLogFilter errorLogFilter : errorLogFilters) {
                filterMap.put(errorLogFilter.getId(), errorLogFilter);
            }
        }

        Integer errorNum = 0;
        List<ErrorLogFilterCount> countList = new ArrayList<>(filterMap.size());
        for (Map.Entry<Integer, LogMinCount> entry : copyLogCounts.entrySet()) {
            Integer filterId = entry.getKey();
            ErrorLogFilter filter = filterMap.get(filterId);
            String filterName = (filter == null || filterId == 0) ? "Others" : filter.getName();
            if (entry.getKey() == 0 || (filter != null && entry.getValue().getLogCount() >= filter.getThrehold())) {
                errorNum += entry.getValue().getLogCount();

                StringBuffer filterAlarmInfo = new StringBuffer();
                filterAlarmInfo.append("\n").append(filterName).append(" - ").append(entry.getValue().getLogCount()).append(" errors");
                if (!StringUtils.isBlank(copyLogContents.get(filterId))) {
                    filterAlarmInfo.append("\n").append(copyLogContents.get(filterId));
                }
                filterAlarmInfo.append("\n");
                countList.add(new ErrorLogFilterCount(entry.getValue().getLogCount(), filterAlarmInfo));
            }
        }
        Collections.sort(countList, Collections.reverseOrder());
        StringBuffer body = new StringBuffer();
        // 避免消息过长, 发送前两个异常多的过滤报警信息
        for (int i = 0; i < countList.size() && i < MAX_FILTER_ERROR_EACH_ALARM; i++) {
            body.append(countList.get(i).getMessage());
        }
        StringBuffer alarmHead = new StringBuffer();
        LogAlarmSeverity severity = getLogAlarmSeverity(errorNum, configuration.getSeverityConfig());
        boolean isAlarm = false;
        if (severity.equals(LogAlarmSeverity.DEFAULT) || severity.equals(LogAlarmSeverity.OK)) {
            alarmHead.append(severity.getValue()).append(" - ").append(errorNum);
        } else {
            isAlarm = true;
            alarmHead.append(severity.getValue()).append(" - ").append(errorNum).append(" errors");
        }
        alarmHead.append(body);
        String report = "\n[OCTO异常日志统计报告|" + "http://octo.sankuai.com/log/report?appkey=" + appkey + " ]";
        alarmHead.append(report).append("\n");
        if (isAlarm) {
            logger.info("alarm msg, appkey={} alarmNode={} msg={}", appkey, alarmNode, alarmHead.toString());
        } else {
            logger.debug("ok msg, appkey={} alarmNode={} msg={}", appkey, alarmNode, alarmHead.toString());
        }
        return alarmHead.toString();
    }

    private String parsedLogToString(ParsedLog log) {
        StringBuffer sb = new StringBuffer();
        // 精简报警信息，方便短信查看
        String detail = "[异常详情|" + "http://octo.sankuai.com/log/detail?uniqueKey=" + log.getUniqueKey().replace(" ", "%20") + " ]";
        String errorMessage = formatMessage(log.getMessage(), STR_LEN);
        sb.append(formatDate(log.getLogTime())).append(" ")
                .append(log.getHost()).append(" ")
                .append(log.getLocation()).append(" ")
                .append(errorMessage).append(" ")
                .append(detail);
        return sb.toString();
    }

    public static LogAlarmSeverity getLogAlarmSeverity(Integer errorNum, LogAlarmSeverityConfig severityConfig) {
        if (errorNum == null || severityConfig == null) {
            return LogAlarmSeverity.DEFAULT;
        }

        if (severityConfig.getDisaster() <= errorNum) {
            return LogAlarmSeverity.DISASTER;
        }

        if (severityConfig.getError() <= errorNum) {
            return LogAlarmSeverity.ERROR;
        }

        if (severityConfig.getWarning() <= errorNum) {
            return LogAlarmSeverity.WARNING;
        }

        if (severityConfig.getOk() <= errorNum) {
            return LogAlarmSeverity.OK;
        }

        return LogAlarmSeverity.DEFAULT;
    }

    public Boolean isRunning() {
        return running;
    }

    public void stop() {
        running = Boolean.FALSE;
    }

    public String getAppkey() {
        return appkey;
    }

    private static String formatMessage(String str, int maxLen) {
        if (str == null) {
            return null;
        }
        // just use the first line
        int len = str.indexOf("<br/>");
        if (len >= 0) {
            str = str.substring(0, len);
        }
        str = cut(str, maxLen);
        return str;
    }

    private static String cut(String str, int maxLen) {
        if (str != null && str.length() > maxLen) {
            return str.substring(0, maxLen);
        }
        return str;
    }
}