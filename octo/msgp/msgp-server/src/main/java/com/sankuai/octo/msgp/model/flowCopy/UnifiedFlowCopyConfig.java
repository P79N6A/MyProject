package com.sankuai.octo.msgp.model.flowCopy;

import com.sankuai.msgp.common.utils.helper.JsonHelper;

public class UnifiedFlowCopyConfig {
    private Long taskId;
    private FlowCopyConfig cfgDetail;
    private Boolean enable;
    private String brokerUrl;
    private String ipport;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public FlowCopyConfig getCfgDetail() {
        return cfgDetail;
    }

    public void setCfgDetail(FlowCopyConfig cfgDetail) {
        this.cfgDetail = cfgDetail;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UnifiedFlowCopyConfig{");
        sb.append("taskId=").append(taskId);
        sb.append(", cfgDetail=").append(cfgDetail);
        sb.append(", enable=").append(enable);
        sb.append(", brokerUrl='").append(brokerUrl).append('\'');
        sb.append(", ipport='").append(ipport).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static void main(String[] args) {
        String json = "{\"enable\":\"true\",\n" + "\"taskId\":7460347472719568411,\n"
                + "\"brokerUrl\":\"http://10.72.208.105:8080/api/record/\",\n" + "\"ipport\":\"10.72.208.105:8889\",\n"
                + "\"cfgDetail\":{\"serviceName\":\"com.sankuai.octo.benchmark.thrift.EchoService\",\"methodNames\":[\"sendString\"],\"sumCount\":1000,\"serverIps\":[\"172.18.185.152\"],\"savePath\":\"testPath\",\"tagged\":true,\"description\":\"æµ\u008Bè¯\u0095\"}}";
        UnifiedFlowCopyConfig obj = JsonHelper.toObject(json, UnifiedFlowCopyConfig.class);
        System.out.println(obj);
        System.out.println(JsonHelper.jsonStr(obj));
    }
}
