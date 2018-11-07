package com.sankuai.octo.msgp.model.flowCopy;

import java.util.Arrays;
import java.util.List;

public class MccNodeData {
    private String spaceName;
    private String nodeName;
    private List<MccNodeDataItem> data;
    private Object[] childrenNodes;
    private Integer version;

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<MccNodeDataItem> getData() {
        return data;
    }

    public void setData(List<MccNodeDataItem> data) {
        this.data = data;
    }

    public Object[] getChildrenNodes() {
        return childrenNodes;
    }

    public void setChildrenNodes(Object[] childrenNodes) {
        this.childrenNodes = childrenNodes;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MccNodeData{");
        sb.append("spaceName='").append(spaceName).append('\'');
        sb.append(", nodeName='").append(nodeName).append('\'');
        sb.append(", data=").append(data);
        sb.append(", childrenNodes=").append(Arrays.toString(childrenNodes));
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }
}

