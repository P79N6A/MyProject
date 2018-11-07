package com.sankuai.octo.msgp.model.flowCopy;

public class FlowCopyBrokerProcessData {
    private FlowCopyBrokerProcessKey key;
    private FlowCopyBrokerProcessValue value;

    public FlowCopyBrokerProcessKey getKey() {
        return key;
    }

    public void setKey(FlowCopyBrokerProcessKey key) {
        this.key = key;
    }

    public FlowCopyBrokerProcessValue getValue() {
        return value;
    }

    public void setValue(FlowCopyBrokerProcessValue value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyBrokerProcessData{");
        sb.append("key=").append(key);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
