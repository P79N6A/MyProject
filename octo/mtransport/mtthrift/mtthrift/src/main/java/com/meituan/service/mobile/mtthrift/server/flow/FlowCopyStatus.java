package com.meituan.service.mobile.mtthrift.server.flow;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/30
 */
public enum FlowCopyStatus {
    STOP("stop"), START("start"), NOT_FLOW_COPY_NODE("not flow copy node"), EXCEPTION("Exception");

    private String statusInfo = "";

    FlowCopyStatus(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public FlowCopyStatus setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
        return this;
    }

    public String getStatus() {
        return statusInfo;
    }
}
