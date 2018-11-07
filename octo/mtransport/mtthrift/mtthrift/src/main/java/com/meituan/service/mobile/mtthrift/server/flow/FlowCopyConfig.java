package com.meituan.service.mobile.mtthrift.server.flow;

public class FlowCopyConfig {

    private boolean enable;
    private long taskId;
    private String brokerUrl;
    private String ipport;
    private FlowCopyCfgDetail cfgDetail;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getIpport() {
        return ipport;
    }

    public void setIpport(String ipport) {
        this.ipport = ipport;
    }

    public FlowCopyCfgDetail getCfgDetail() {
        return cfgDetail;
    }

    public void setCfgDetail(FlowCopyCfgDetail cfgDetail) {
        this.cfgDetail = cfgDetail;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyConfig{")
                .append("enable=").append(enable)
                .append(", brokerUrl=").append(brokerUrl)
                .append(", ipport=").append(ipport)
                .append(", taskId=").append(taskId)
                .append(", cfgDetail=").append(cfgDetail)
                .append('}');
        return sb.toString();
    }
}
