package com.sankuai.meituan.config.model;

import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigRequest {
    private String appkey;
    private String env;
    private String path;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toPath() {
        ZKPathBuilder pathBuilder = ZKPathBuilder.newBuilder();
        NodeNameUtil.checkAppkey(appkey);
        pathBuilder.appendSpace(appkey);
        if (StringUtils.isNotEmpty(env)) {
            NodeNameUtil.checkEnv(env);
            pathBuilder.appendSpace(env);
        }
        if (StringUtils.isNotEmpty(path)) {
            NodeNameUtil.checkPath(path);
            pathBuilder.appendSpace(path);
        }
        return pathBuilder.toPath();
    }
}
