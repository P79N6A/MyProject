package com.sankuai.octo.msgp.model.flowCopy;

public class FlowCopyBrokerProcessValue {
    private String name;
    private Long startTimeMillis;
    private Long collectTimeMillis;
    private Long uploadTimeMillis;
    private Long completeTimeMillis;
    private Integer processCount;
    private Integer dispatchCount;
    private Integer localSendCount;
    private Integer localStoreCount;
    private String taskPhase;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(Long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public Long getCollectTimeMillis() {
        return collectTimeMillis;
    }

    public void setCollectTimeMillis(Long collectTimeMillis) {
        this.collectTimeMillis = collectTimeMillis;
    }

    public Long getUploadTimeMillis() {
        return uploadTimeMillis;
    }

    public void setUploadTimeMillis(Long uploadTimeMillis) {
        this.uploadTimeMillis = uploadTimeMillis;
    }

    public Long getCompleteTimeMillis() {
        return completeTimeMillis;
    }

    public void setCompleteTimeMillis(Long completeTimeMillis) {
        this.completeTimeMillis = completeTimeMillis;
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public Integer getDispatchCount() {
        return dispatchCount;
    }

    public void setDispatchCount(Integer dispatchCount) {
        this.dispatchCount = dispatchCount;
    }

    public Integer getLocalSendCount() {
        return localSendCount;
    }

    public void setLocalSendCount(Integer localSendCount) {
        this.localSendCount = localSendCount;
    }

    public Integer getLocalStoreCount() {
        return localStoreCount;
    }

    public void setLocalStoreCount(Integer localStoreCount) {
        this.localStoreCount = localStoreCount;
    }

    public String getTaskPhase() {
        return taskPhase;
    }

    public void setTaskPhase(String taskPhase) {
        this.taskPhase = taskPhase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VcrPtestProcess{");
        sb.append("name='").append(name).append('\'');
        sb.append(", startTimeMillis=").append(startTimeMillis);
        sb.append(", collectTimeMillis=").append(collectTimeMillis);
        sb.append(", uploadTimeMillis=").append(uploadTimeMillis);
        sb.append(", completeTimeMillis=").append(completeTimeMillis);
        sb.append(", processCount=").append(processCount);
        sb.append(", dispatchCount=").append(dispatchCount);
        sb.append(", localSendCount=").append(localSendCount);
        sb.append(", localStoreCount=").append(localStoreCount);
        sb.append(", taskPhase='").append(taskPhase).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
