package com.sankuai.msgp.common.model.errorlog;

import java.io.Serializable;
import java.util.Date;

/**
 * 异常日志字段
 */
public class ParsedLog implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String SET_LOGCENTER_DEFAULT_VAL= "unconfigured";
    public static final String ENV_LOGCENTER_DEFAULT_VAL= "unknown";

    private String uniqueKey;

    // 异常日志字段
    private String appkey;
    private String message = "";   // 异常信息
    private String exception = "";  // 异常堆栈
    private String location = "";   // 日志输出类/位置
    private Date logTime;
    private String host = "Unknow";
    private String exceptionName = "";  // 异常类型
    private String traceId = "";    // 异常traceId, 新日志业务使用xmdFormat时默认添加该字段
    private String hostSet = "default_cell";  // 日志来源机器set信息,可能无该字段, 有的话默认值: unconfigured
    private String env = "Unknow";  // 日志来源环境, 可能无该字段, 有的话默认值: unknown

    // 逻辑字段
    private Integer filterId;
    private Integer thresholdMin; // 统计分钟粒度, 默认1分钟

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ParsedLog{")
                .append("uniqueKey='").append(uniqueKey).append("', ")
                .append("appkey='").append(appkey).append("', ")
                .append("message='").append(message).append("', ")
                .append("exception='").append(exception).append("', ")
                .append("location='").append(location).append("', ")
                .append("logTime='").append(logTime).append("', ")
                .append("host='").append(host).append("', ")
                .append("exceptionName='").append(exceptionName).append("', ")
                .append("traceId='").append(traceId).append("', ")
                .append("hostSet='").append(hostSet).append("', ")
                .append("env='").append(env).append("'}");
        return sb.toString();
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Integer getFilterId() {
        return filterId;
    }

    public void setFilterId(Integer filterId) {
        this.filterId = filterId;
    }

    public Integer getThresholdMin() {
        return thresholdMin;
    }

    public void setThresholdMin(Integer thresholdMin) {
        this.thresholdMin = thresholdMin;
    }

    public String getHostSet() {
        return hostSet;
    }

    public void setHostSet(String hostSet) {
        this.hostSet = hostSet;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
