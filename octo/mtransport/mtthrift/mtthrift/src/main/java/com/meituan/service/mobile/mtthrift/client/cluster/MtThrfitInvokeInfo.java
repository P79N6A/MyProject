package com.meituan.service.mobile.mtthrift.client.cluster;

/**
 * User: YangXuehua
 * Date: 13-12-13
 * Time: 上午11:34
 */
public class MtThrfitInvokeInfo {
    private static ThreadLocal<MtThrfitInvokeInfo> mtThrfitInvokeInfoThreadLocal = new ThreadLocal<MtThrfitInvokeInfo>();

    public static MtThrfitInvokeInfo getMtThrfitInvokeInfo() {
        return mtThrfitInvokeInfoThreadLocal.get();
    }

    public static void setMtThrfitInvokeInfo(MtThrfitInvokeInfo mtThrfitSpan) {
        mtThrfitInvokeInfoThreadLocal.set(mtThrfitSpan);
    }

    public static void clearMtThrfitInvokeInfo() {
        mtThrfitInvokeInfoThreadLocal.remove();
    }

    private String spanName;
    private String clientIp;
    private int clientPort;
    private String serverIp;
    private int serverPort;
    private String serverAppKey;
    private boolean uniProto = false;
    private String svcIdentification;

    public MtThrfitInvokeInfo(String serverAppKey, String spanName, String clientIp, int clientPort, String serverIp, int serverPort) {
        this.serverAppKey = serverAppKey;
        this.spanName = spanName;
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.svcIdentification = new StringBuilder(serverAppKey)
                .append(":").append(spanName).toString();
    }

    public MtThrfitInvokeInfo(String serverAppKey, String spanName, String clientIp, int clientPort, String serverIp, int serverPort, boolean uniProto) {
        this.serverAppKey = serverAppKey;
        this.spanName = spanName;
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.uniProto = uniProto;
        this.svcIdentification = new StringBuilder().append(serverAppKey)
                .append(":").append(spanName).toString();
    }

    public String getSpanName() {
        return spanName;
    }

    public String getClientIp() {
        return clientIp;
    }

    public int getClientPort() {
        return clientPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerAppKey() {
        return serverAppKey;
    }

    public boolean isUniProto() {
        return uniProto;
    }

    public void setUniProto(boolean uniProto) {
        this.uniProto = uniProto;
    }

    public String getSvcIdentification() {
        return svcIdentification;
    }
}
