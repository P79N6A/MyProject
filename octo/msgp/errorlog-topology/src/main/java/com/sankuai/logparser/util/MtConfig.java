package com.sankuai.logparser.util;

import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.exception.MtConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yves on 16/12/28.
 * Config utility for mt-config
 */
public class MtConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MtConfig.class);
    private volatile MtConfigClient topologyCfgClient;
    private volatile MtConfigClient errorLogCfgClient;

    public MtConfig(){
        initTopologyCfg();
        initErrorLogCfg();
    }

    private static class MtConfigSingletonInstance {
        private static MtConfig INSTANCE = new MtConfig();
    }

    public static MtConfigClient getTopologyCfgClient() {
        return MtConfigSingletonInstance.INSTANCE.topologyCfgClient;
    }

    public static MtConfigClient getErrorLogCfgClient() {
        return MtConfigSingletonInstance.INSTANCE.errorLogCfgClient;
    }

    private void initTopologyCfg() {
        topologyCfgClient = new MtConfigClient();
        topologyCfgClient.setAppkey("com.sankuai.inf.octo.errorlog.topology");
        topologyCfgClient.setModel("v2");
        topologyCfgClient.setId("topologyCfg");
        LOGGER.info("topologyCfg client initialized, thread: " + Thread.currentThread().getName());
        try {
            topologyCfgClient.init();
        } catch (MtConfigException e) {
            LOGGER.error("init topologyCfgClient (" + topologyCfgClient.getNodeName()+ ") failed", e);
        }
    }

    private void initErrorLogCfg() {
        errorLogCfgClient = new MtConfigClient();
        errorLogCfgClient.setAppkey("com.sankuai.inf.octo.errorlog");
        errorLogCfgClient.setModel("v2");
        errorLogCfgClient.setId("errorLogCfg");
        LOGGER.info("errorLogCfg client initialized, thread: " + Thread.currentThread().getName());
        try {
            errorLogCfgClient.init();
        } catch (MtConfigException e) {
            LOGGER.error("init errorLogCfgClient (" + errorLogCfgClient.getNodeName()+ ") failed", e);
        }
    }
}
