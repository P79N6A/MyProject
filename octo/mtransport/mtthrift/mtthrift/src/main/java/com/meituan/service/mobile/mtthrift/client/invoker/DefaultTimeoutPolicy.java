package com.meituan.service.mobile.mtthrift.client.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/5/19
 * Time: 11:04
 */
public class DefaultTimeoutPolicy implements ITimeoutPolicy {
    private static final Logger log = LoggerFactory.getLogger(DefaultTimeoutPolicy.class);
    private Map<String, String> timeoutMap;
    private String serviceInterfaceSimpleName;

    public DefaultTimeoutPolicy(String jsonString, String serviceInterfaceSimpleName) {
        this.serviceInterfaceSimpleName = serviceInterfaceSimpleName;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            timeoutMap = objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            log.warn("Json parse failed, will use defaultTimeout. error message: {}", e.getMessage());
        }
    }

    @Override
    public int getTimeoutByConfig(MethodInvocation methodInvocation, int defaultTimeout) {
        String methodName = serviceInterfaceSimpleName + "." + methodInvocation.getMethod().getName();
        try {
            if (timeoutMap.containsKey(methodName)) {
                return Integer.valueOf(timeoutMap.get(methodName));
            } else {
                return defaultTimeout;
            }
        } catch (Exception e) {
            log.warn("Json parse failed, will use defaultTimeout. error message: {}", e.getMessage());
            return defaultTimeout;
        }
    }
}
