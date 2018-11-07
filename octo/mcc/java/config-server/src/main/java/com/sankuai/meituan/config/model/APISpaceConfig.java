package com.sankuai.meituan.config.model;

import java.util.List;
import java.util.Map;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-24
 */
public class APISpaceConfig {
    private String nodeName;
    private Map<String, String> nodeData;
    private List<APISpaceConfig> nodeChildren;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Map<String, String> getNodeData() {
        return nodeData;
    }

    public void setNodeData(Map<String, String> nodeData) {
        this.nodeData = nodeData;
    }

    public List<APISpaceConfig> getNodeChildren() {
        return nodeChildren;
    }

    public void setNodeChildren(List<APISpaceConfig> nodeChildren) {
        this.nodeChildren = nodeChildren;
    }
}
