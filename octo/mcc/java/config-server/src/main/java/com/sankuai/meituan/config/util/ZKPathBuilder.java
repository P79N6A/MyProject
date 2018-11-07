package com.sankuai.meituan.config.util;

import org.apache.commons.lang.StringUtils;

public class ZKPathBuilder {
    private final StringBuilder pathBuilder;

    private ZKPathBuilder(String root) {
        pathBuilder = new StringBuilder(root);
    }

    public static ZKPathBuilder newBuilder() {
        return new ZKPathBuilder("");
    }

    public static ZKPathBuilder newBuilder(String root) {
        return new ZKPathBuilder(root);
    }

    public ZKPathBuilder appendSpace(String spaceName) {
        if (StringUtils.isNotEmpty(spaceName) && ! "/".equals(spaceName)) {
            if (StringUtils.indexOf(spaceName, '/') != 0) {
                pathBuilder.append("/");
            }
            pathBuilder.append(spaceName);
        }
        return this;
    }

    public ZKPathBuilder appendNode(String nodeName) {
        if (StringUtils.isNotEmpty(nodeName)) {
            pathBuilder.append("/");
            pathBuilder.append(nodeName.replaceAll("\\.", "/"));
        }
        return this;
    }

    public ZKPathBuilder copy() {
        return new ZKPathBuilder(this.pathBuilder.toString());
    }

    public String toPath() {
        return pathBuilder.toString();
    }

    @Override
    public String toString() {
        return pathBuilder.toString();
    }
}
