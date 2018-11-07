package com.meituan.mtrace;

public class TraceParam {

    private String traceId;
    private String spanId;
    private String spanName;
    private String localAppKey;
    private String localIp;
    private int localPort;
    private String remoteAppKey;
    private String remoteIp;
    private int remotePort;
    private String infraName;
    private String version;
    private int packageSize;
    private boolean sample = false;
    private boolean debug = false;
    private String extend;

    public TraceParam(String spanName) {
        this.spanName = spanName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public void setLocal(String appKey, String ip, int port) {
        this.localAppKey = appKey;
        this.localIp = ip;
        this.localPort = port;
    }
    public void setRemote(String appKey, String ip, int port) {
        this.remoteAppKey = appKey;
        this.remoteIp = ip;
        this.remotePort = port;
    }

    public int getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(int packageSize) {
        this.packageSize = packageSize;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public String getLocalAppKey() {
        return localAppKey;
    }

    public void setLocalAppKey(String localAppKey) {
        this.localAppKey = localAppKey;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getInfraName() {
        return infraName;
    }

    public void setInfraName(String infraName) {
        this.infraName = infraName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean sample) {
        this.sample = sample;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
