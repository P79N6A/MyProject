package com.meituan.service.mobile.mtthrift.server.flow;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/1
 * Time: 15:24
 */
public class RecordTaskResponse {
    private String status;
    private RecordTaskResponseData data;

    public RecordTaskResponse() {
    }

    public RecordTaskResponse(String status, RecordTaskResponseData data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RecordTaskResponseData getData() {
        return data;
    }

    public void setData(RecordTaskResponseData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecordTaskResponse{");
        sb.append("status='").append(status).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
