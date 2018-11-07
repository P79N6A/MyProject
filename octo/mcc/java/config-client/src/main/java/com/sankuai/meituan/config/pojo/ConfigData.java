package com.sankuai.meituan.config.pojo;

import com.sankuai.meituan.config.util.MtConfigNameUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigData implements Serializable {
    private String appkey, env, path;
    private ConcurrentMap<String, String> config;
    private Long version;

    public ConfigData(String appkey, String env, String path, Map<String, String> config, Long version) {
        this.appkey = appkey;
        this.env = env;
        this.path = path;
        this.config = new ConcurrentHashMap<String, String>(config);
        this.version = version;
        check();
    }

    private void check() {
        MtConfigNameUtil.checkAppkey(appkey);
        MtConfigNameUtil.checkEnv(env);
        MtConfigNameUtil.checkPath(path);
    }

    public ConcurrentMap<String, String> getConfig() {
        return config;
    }

    public void setConfig(ConcurrentMap<String, String> config) {
        this.config = config;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigData{");
        sb.append("appkey='").append(appkey).append('\'');
        sb.append(", env='").append(env).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", config=").append(config);
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }
}
