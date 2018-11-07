package com.meituan.service.mobile.mtthrift.util;

import com.sankuai.sgagent.thrift.model.ConfigStatus;
import static com.sankuai.sgagent.thrift.model.CustomizedStatus.ALIVE;
import static com.sankuai.sgagent.thrift.model.CustomizedStatus.DEAD;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2016/12/26
 * Time: 09:59
 */
public class ConfigStatusUtil {
    public static ConfigStatus newDefaultConfigStatus() {
        ConfigStatus configStatus = new ConfigStatus();
        configStatus.setInitStatus(DEAD);
        configStatus.setRuntimeStatus(ALIVE);

        return configStatus;
    }

    public static void checkConfigStatus(ConfigStatus configStatus) {
        if (configStatus.getInitStatus() == null)
            configStatus.setInitStatus(DEAD);
        if (configStatus.getRuntimeStatus() == null)
            configStatus.setRuntimeStatus(ALIVE);

        if (!configStatus.getInitStatus().equals(DEAD) &&
                !configStatus.getRuntimeStatus().equals(ALIVE)) {
            String message = "invalid configStatus, you can change one and only one filed of " +
                    "configStatus. initStatus: " + configStatus.getInitStatus() + "[default:DEAD]" +
                    "runtimeStatus: " + configStatus.getRuntimeStatus() + "[default:ALIVE]";
            throw new RuntimeException(message);
        }
    }
}
