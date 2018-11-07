/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 *
 */


package com.sankuai.meituan.config.model;

import com.google.common.base.Objects;

import java.util.Collection;
import java.util.List;

/**
 * @author liuxu<liuxu04@meituan.com>
 */

public class ConfigNode {

    private String spaceName;
    private String nodeName;
    private Collection<? extends PropertyValue> data;
    private List<EmbededNode> childrenNodes;
    private Long version;

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

    public Collection<? extends PropertyValue> getData() {
        return data;
    }

    public void setData(Collection<? extends PropertyValue> data) {
        this.data = data;
    }

    public List<EmbededNode> getChildrenNodes() {
        return childrenNodes;
    }

    public void setChildrenNodes(List<EmbededNode> childrenNodes) {
        this.childrenNodes = childrenNodes;
    }


    @Override
    public String toString(){
        return Objects.toStringHelper(getClass())
          .add("nodeName", this.nodeName)
          .add("spaceName", this.spaceName)
          .add("data", this.data)
          .add("childrenNodes", this.childrenNodes)
           .add("version",this.version)
          .toString();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

