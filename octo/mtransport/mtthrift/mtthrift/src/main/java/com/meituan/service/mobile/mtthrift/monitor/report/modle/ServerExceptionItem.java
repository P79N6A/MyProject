package com.meituan.service.mobile.mtthrift.monitor.report.modle;

import java.io.Serializable;
import java.util.Date;

/**
 * User: YangXuehua
 * Date: 14-5-13
 * Time: 下午6:50
 */
@Deprecated
public class ServerExceptionItem implements Serializable {
    private static final long serialVersionUID = -1L;

    private String methodName;
    private String exceptionStr;
    private long timestamp;
    private String clientAppKey;
    private String clientIp;

    public ServerExceptionItem(String methodName, String exceptionStr, long timestamp, String clientAppKey, String clientIp) {
        this.methodName = methodName;
        this.exceptionStr = exceptionStr;
        this.timestamp = timestamp;
        this.clientAppKey = clientAppKey;
        this.clientIp = clientIp;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getExceptionStr() {
        return exceptionStr;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getClientAppKey() {
        return clientAppKey;
    }

    public String getClientIp() {
        return clientIp;
    }

    @Override
    public String toString() {
        return "ServerExceptionItem{" + "methodName='" + methodName + '\'' + ", exceptionStr='" + exceptionStr + '\'' + ", timestamp="
                + ExceptionItem.formatSS(new Date(timestamp)) + ", clientAppKey='" + clientAppKey + '\'' + ", clientIp='" + clientIp + '\'' + '}';
    }

}
