package com.sankuai.octo.test.utils;

import com.sankuai.meituan.config.MtConfigClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * for scala usage
 */
@Component
public class ConfigHolder {
    private static ConfigHolder INSTANCE;

    @Resource
    private MtConfigClient mtConfigClient;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static MtConfigClient get() {
        return INSTANCE == null ? null : INSTANCE.mtConfigClient;
    }

    public static String getAppkey() {
        String agentHost = get().getValue("octo.agentHost");
        agentHost = (agentHost != null ? agentHost : "127.0.0.1:5266");
        System.setProperty("octo.agentHost", agentHost);
        return agentHost;
    }
}