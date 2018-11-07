package com.sankuai.octo.errorlog.model;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-10-17
 */
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
