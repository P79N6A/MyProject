package com.sankuai.octo.msgp.model.reqpolicy;

/**
 * Created by Js on 2018/7/16.
 */
public class PolicyTaskRecord {
    private Long id;

    private Long tagId;

    private Long taskId;

    private Integer policyType;

    private Integer expectScaleNum;

    private Integer actualScaleNum;

    private String taskStatus;

    private Long createTime;

    private Long updateTime;

    private Integer channelType;

    public PolicyTaskRecord() {
    }

    public PolicyTaskRecord(Long tagId, Long taskId, Integer policyType, Integer expectScaleNum, Integer channelType) {
        this.tagId = tagId;
        this.taskId = taskId;
        this.policyType = policyType;
        this.expectScaleNum = expectScaleNum;
        this.channelType = channelType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Integer getPolicyType() {
        return policyType;
    }

    public void setPolicyType(Integer policyType) {
        this.policyType = policyType;
    }

    public Integer getExpectScaleNum() {
        return expectScaleNum;
    }

    public void setExpectScaleNum(Integer expectScaleNum) {
        this.expectScaleNum = expectScaleNum;
    }

    public Integer getActualScaleNum() {
        return actualScaleNum;
    }

    public void setActualScaleNum(Integer actualScaleNum) {
        this.actualScaleNum = actualScaleNum;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus == null ? null : taskStatus.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getChannelType() {
        return channelType;
    }

    public void setChannelType(Integer channelType) {
        this.channelType = channelType;
    }

    @Override
    public String toString() {
        return "PolicyTaskRecord{" +
                "id=" + id +
                ", tagId=" + tagId +
                ", taskId=" + taskId +
                ", policyType=" + policyType +
                ", expectScaleNum=" + expectScaleNum +
                ", actualScaleNum=" + actualScaleNum +
                ", taskStatus='" + taskStatus + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", channelType=" + channelType +
                '}';
    }
}
