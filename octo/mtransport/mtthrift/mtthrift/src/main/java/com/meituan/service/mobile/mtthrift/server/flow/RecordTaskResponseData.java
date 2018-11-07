package com.meituan.service.mobile.mtthrift.server.flow;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/4
 * Time: 13:09
 */
public class RecordTaskResponseData {
    private String msg;
    private String ip;
    private int port;
    private String taskPhase;

    public RecordTaskResponseData() {
    }

    public RecordTaskResponseData(String data) {
        this.msg = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String data) {
        this.msg = data;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTaskPhase() {
        return taskPhase;
    }

    public void setTaskPhase(String taskPhase) {
        this.taskPhase = taskPhase;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RecordTaskResponseData{");
        sb.append("msg='").append(msg).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", port=").append(port);
        sb.append(", taskPhase='").append(taskPhase).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
