package com.sankuai.octo.msgp.model.flowCopy;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/14
 * Time: 16:39
 */
public class FlowCopyBrokerResponse {
    private String status;
    private FlowCopyBrokerData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FlowCopyBrokerData getData() {
        return data;
    }

    public void setData(FlowCopyBrokerData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyBrokerResponse{");
        sb.append("status='").append(status).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
