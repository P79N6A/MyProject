package com.sankuai.meituan.config.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 临时对象,新的前端api完成后删除
 */
@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config2Request {
    private String spaceName;
    private String nodeName;
    private String nodeData;
    private Long version;
    private boolean rollback;//true表示回滚导致的保存， false表示正常保存
    private boolean swimlaneGroup; //true表示泳道分组，false表示普通分组

    public boolean isSwimlaneGroup() {
        return swimlaneGroup;
    }

    public void setSwimlaneGroup(boolean swimlaneGroup) {
        this.swimlaneGroup = swimlaneGroup;
    }

    public boolean isRollback() {
        return rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }


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

    public String getNodeData() {
        return nodeData;
    }

    public void setNodeData(String nodeData) {
        this.nodeData = nodeData;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
