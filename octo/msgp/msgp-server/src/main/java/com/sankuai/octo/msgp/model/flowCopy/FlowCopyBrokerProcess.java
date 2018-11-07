package com.sankuai.octo.msgp.model.flowCopy;

import java.util.List;

public class FlowCopyBrokerProcess {
    private String status;
    private List<FlowCopyBrokerProcessData> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FlowCopyBrokerProcessData> getData() {
        return data;
    }

    public void setData(List<FlowCopyBrokerProcessData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyBrokerProcess{");
        sb.append("status='").append(status).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
