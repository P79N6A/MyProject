package com.meituan.service.mobile.mtthrift.auth;


public class AuthMetaData {

    private String clientIp;
    private String clientAppkey;
    private String simpleServiceName;
    private String methodName;
    private Object[] args;
    private String signature;
    // 下面是统一鉴权使用
    private String completeServiceName;
    private String uniformSignInfo;

    // 记录鉴权结果
    private int authCode = -1;

    public AuthMetaData() {
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientAppkey() {
        return clientAppkey;
    }

    public void setClientAppkey(String clientAppkey) {
        this.clientAppkey = clientAppkey;
    }

    public String getSimpleServiceName() {
        return simpleServiceName;
    }

    public void setSimpleServiceName(String serviceName) {
        this.simpleServiceName = serviceName;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getUniformSignInfo() {
        return uniformSignInfo;
    }

    public void setUniformSignInfo(String uniformSignInfo) {
        this.uniformSignInfo = uniformSignInfo;
    }

    public String getCompleteServiceName() {
        return completeServiceName;
    }

    public void setCompleteServiceName(String completeServiceName) {
        this.completeServiceName = completeServiceName;
    }

    public int getAuthCode() {
        return authCode;
    }

    public void setAuthCode(int authCode) {
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return "AuthMetaData{" +
                "clientIp='" + clientIp + '\'' +
                ", clientAppkey='" + clientAppkey + '\'' +
                ", methodName='" + methodName + '\'' +
                ", signature='" + signature + '\'' +
                ", uniformSignInfo='" + uniformSignInfo + '\'' +
                '}';
    }
}
