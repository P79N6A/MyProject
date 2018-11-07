package com.meituan.service.mobile.mtthrift.monitor.report.modle;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: YangXuehua
 * Date: 14-5-14
 * Time: 上午11:17
 */
@Deprecated
public class ExceptionItem implements Serializable {
    private static final long serialVersionUID = -1L;

    private String serverAppKey;
    private String serverIp;
    private int serverPort;

    private String methodName;
    private String exceptionStr;
    private long timestamp;
    private String clientAppKey;
    private String clientIp;
    private String clusterManager;


    public ExceptionItem(String serverAppKey, String serverIp, int serverPort, String methodName, String exceptionStr, long timestamp, String clientAppKey,
                         String clientIp) {
        this(serverAppKey, serverIp, serverPort, methodName, exceptionStr, timestamp, clientAppKey, clientIp, "ZK");
    }

    public ExceptionItem(String serverAppKey, String serverIp, int serverPort, String methodName, String exceptionStr, long timestamp, String clientAppKey,
                         String clientIp, String clusterManager) {
        this.serverAppKey = serverAppKey;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.methodName = methodName;
        this.exceptionStr = exceptionStr;
        this.timestamp = timestamp;
        this.clientAppKey = clientAppKey;
        this.clientIp = clientIp;
        this.clusterManager = clusterManager;
    }

    public String getServerAppKey() {
        return serverAppKey;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
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

    public String getServerKey() {
        return serverIp + ":" + serverPort;
    }

    public ServerExceptionItem getServerExceptionItem() {
        return new ServerExceptionItem(methodName, exceptionStr, timestamp, clientAppKey, clientIp);
    }

    @Override
    public String toString() {
        return "ExceptionItem{" + "serverAppKey='" + serverAppKey + '\'' + ", serverIp='" + serverIp + '\'' + ", serverPort=" + serverPort + ", methodName='"
                + methodName + '\'' + ", exceptionStr='" + exceptionStr + '\'' + ", timestamp=" + formatSS(new Date(timestamp)) + ", clientAppKey='"
                + clientAppKey + '\'' + ", clientIp='" + clientIp + '\'' + '}';
    }


    public static String formatSS(Date date) {
        SimpleDateFormat YYYY_MM_DD_HH_MM_SS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return YYYY_MM_DD_HH_MM_SS_FORMAT.format(date);
    }

    public String getClusterManager() {
        return clusterManager;
    }

}
