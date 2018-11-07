package com.sankuai.octo.msgp.model.flowCopy;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/13
 * Time: 17:39
 */
public class FlowCopyPtestResponse {
    private String status;
    private String message;
    private FlowCopyPtestResult result;
    private FlowCopyBrokerProcessValue process;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FlowCopyPtestResult getResult() {
        return result;
    }

    public void setResult(FlowCopyPtestResult result) {
        this.result = result;
    }

    public FlowCopyBrokerProcessValue getProcess() {
        return process;
    }

    public void setProcess(FlowCopyBrokerProcessValue process) {
        this.process = process;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyPtestResponse{");
        sb.append("status='").append(status).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", result=").append(result);
        sb.append(", process=").append(process);
        sb.append('}');
        return sb.toString();
    }
}
