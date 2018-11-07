package com.meituan.service.mobile.mtthrift.auth;



public class SignMetaData {

    private String appkey;
    private String remoteAppkey;
    private String localIp;
    private String methodName;
    private Object[] args;
    private Long timestamp;
    private String signature;

    public SignMetaData() {
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getRemoteAppkey() {
        return remoteAppkey;
    }

    public void setRemoteAppkey(String remoteAppkey) {
        this.remoteAppkey = remoteAppkey;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "SignMetaData{" +
                "appkey='" + appkey + '\'' +
                ", remoteAppkey='" + remoteAppkey + '\'' +
                ", localIp='" + localIp + '\'' +
                ", methodName='" + methodName + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
