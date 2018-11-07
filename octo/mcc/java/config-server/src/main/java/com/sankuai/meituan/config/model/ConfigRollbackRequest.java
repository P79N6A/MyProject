package com.sankuai.meituan.config.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by liangchen on 2017/9/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigRollbackRequest {
    private String nodeName;
    private String content;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
