package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.exception.SgAgentServiceException;
import com.sankuai.meituan.config.pojo.ConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferSgAgentService extends SgAgentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferSgAgentService.class);
    private volatile SnapshotService snapshotService = new SnapshotService();
    private volatile ConfigData cacheData = null;
    private static final String configTrace = System.getProperty("mcc.traceStack", "false");

    public BufferSgAgentService(String appkey, String env, String path) {
        super(appkey, env, path);
        cacheData = getConfigWithoutException();
        if (cacheData != null) {
            snapshotService.saveSnapshot(cacheData);
            LOGGER.info("重写snapshot文件:[{}]", cacheData);
        } else {
            ConfigData temp = snapshotService.getSnapshot(appkey, env, path);
            if (temp != null) {
                cacheData = temp;
            }
        }
        if (cacheData == null) {
            LOGGER.warn("无法加载到配置 {} {}", appkey, path);
        }
    }

    @Override
    public ConfigData getConfig() {
        ConfigData configData = null;
        try {
            configData = super.getConfig();
        } catch (Exception e) {
            if (cacheData == null) {
                throw new SgAgentServiceException("无法从服务端获取配置,也没有本地缓存,无法返回数据, appkey = " + getAppkey(), e);
            } else {
                return cacheData;
            }
        }

        if (null == configData) {
            return cacheData;
        }
        if(isCacheDiff(cacheData, configData)){
            this.cacheData = configData;
            snapshotService.saveSnapshot(configData);
        }
        return cacheData;
    }

    private boolean isCacheDiff(ConfigData oldCache, ConfigData newCache) {
        if (null == oldCache) {
            return true;
        }
        long oldVersion = oldCache.getVersion();
        long newVersion = newCache.getVersion();
        return oldVersion != newVersion;
    }

    private ConfigData getConfigWithoutException() {
        try {
            return super.getConfig();
        } catch (Exception e) {
            if ("true".equalsIgnoreCase(configTrace)) {
                LOGGER.warn("从远端获取配置失败，从本地读取", e);
            }
            return null;
        }
    }
}
