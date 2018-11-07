package com.sankuai.meituan.config.model;

import com.sankuai.meituan.config.domain.ClientSyncLog;

import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-26
 */
public class NodeClientSyncLog {
    private String spaceName;
    private String nodeName;
    private Long version;
    private List<ClientSyncLog> logs;

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<ClientSyncLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ClientSyncLog> logs) {
        this.logs = logs;
    }
}
