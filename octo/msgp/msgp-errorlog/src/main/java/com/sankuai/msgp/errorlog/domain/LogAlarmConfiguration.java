package com.sankuai.msgp.errorlog.domain;

import com.sankuai.msgp.errorlog.pojo.LogAlarmConfig;
import com.sankuai.msgp.errorlog.pojo.LogAlarmSeverityConfig;

public class LogAlarmConfiguration {
    private LogAlarmConfig basicConfig;

    private LogAlarmSeverityConfig severityConfig;
    // others


    public LogAlarmConfig getBasicConfig() {
        return basicConfig;
    }

    public void setBasicConfig(LogAlarmConfig basicConfig) {
        this.basicConfig = basicConfig;
    }

    public LogAlarmSeverityConfig getSeverityConfig() {
        return severityConfig;
    }

    public void setSeverityConfig(LogAlarmSeverityConfig severityConfig) {
        this.severityConfig = severityConfig;
    }
}