package com.meituan.service.mobile.mtthrift.netty.metadata;

import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.HeartbeatInfo;

import java.util.Arrays;

public class RPCContext {

    private Long seq;
    private byte[] thriftRequestData;
    private byte[] thriftResponseData;
    private boolean unifiedProto;
    private HeartbeatInfo heartbeatInfo;
    private Header header;
    private Long requestTime;
    private int requestSize;
    private byte[] responseBytes;
    private com.meituan.service.mobile.mtthrift.netty.metadata.RequestType requestType;
    private boolean authSuccess = true;
    private byte[] intactBytes;

    public RPCContext() {
        requestTime = System.currentTimeMillis();
    }

    public byte[] getThriftRequestData() {
        return thriftRequestData;
    }

    public void setThriftRequestData(byte[] thriftRequestData) {
        this.thriftRequestData = thriftRequestData;
    }

    public byte[] getThriftResponseData() {
        return thriftResponseData;
    }

    public void setThriftResponseData(byte[] thriftResponseData) {
        this.thriftResponseData = thriftResponseData;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public boolean isUnifiedProto() {
        return unifiedProto;
    }

    public void setUnifiedProto(boolean unifiedProto) {
        this.unifiedProto = unifiedProto;
    }

    public HeartbeatInfo getHeartbeatInfo() {
        return heartbeatInfo;
    }

    public void setHeartbeatInfo(HeartbeatInfo heartbeatInfo) {
        this.heartbeatInfo = heartbeatInfo;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }

    public int getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(int requestSize) {
        this.requestSize = requestSize;
    }

    public byte[] getResponseBytes() {
        return responseBytes;
    }

    public void setResponseBytes(byte[] responseBytes) {
        this.responseBytes = responseBytes;
    }

    public com.meituan.service.mobile.mtthrift.netty.metadata.RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(com.meituan.service.mobile.mtthrift.netty.metadata.RequestType requestType) {
        this.requestType = requestType;
    }

    public boolean isAuthSuccess() {
        return authSuccess;
    }

    public void setAuthSuccess(boolean authSuccess) {
        this.authSuccess = authSuccess;
    }

    public byte[] getIntactBytes() {
        return intactBytes;
    }

    public void setIntactBytes(byte[] intactBytes) {
        this.intactBytes = intactBytes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RPCContext{");
        sb.append("seq=").append(seq);
        sb.append(", thriftRequestData=").append(Arrays.toString(thriftRequestData));
        sb.append(", thriftResponseData=").append(Arrays.toString(thriftResponseData));
        sb.append(", unifiedProto=").append(unifiedProto);
        sb.append(", heartbeatInfo=").append(heartbeatInfo);
        sb.append(", header=").append(header);
        sb.append(", requestTime=").append(requestTime);
        sb.append(", requestSize=").append(requestSize);
        sb.append(", responseBytes=").append(Arrays.toString(responseBytes));
        sb.append(", requestType=").append(requestType);
        sb.append(", authSuccess=").append(authSuccess);
        sb.append(", intactBytes=").append(Arrays.toString(intactBytes));
        sb.append('}');
        return sb.toString();
    }
}
