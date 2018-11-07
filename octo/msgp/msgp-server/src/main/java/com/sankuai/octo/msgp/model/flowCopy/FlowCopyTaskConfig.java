package com.sankuai.octo.msgp.model.flowCopy;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/14
 * Time: 13:01
 */
public class FlowCopyTaskConfig {
    private long taskId;
    private FlowCopyConfig recordConfig;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public FlowCopyConfig getRecordConfig() {
        return recordConfig;
    }

    public void setRecordConfig(FlowCopyConfig recordConfig) {
        this.recordConfig = recordConfig;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyTaskConfig{");
        sb.append("taskId=").append(taskId);
        sb.append(", recordConfig=").append(recordConfig);
        sb.append('}');
        return sb.toString();
    }
}
