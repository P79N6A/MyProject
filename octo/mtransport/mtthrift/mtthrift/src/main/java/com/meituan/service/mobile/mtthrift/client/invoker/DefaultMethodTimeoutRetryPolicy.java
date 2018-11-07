package com.meituan.service.mobile.mtthrift.client.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * User: Zhanran
 * Date: 2017-07-25
 * Time: 下午2:30
 * 管理方法和超时次数
 */
public class DefaultMethodTimeoutRetryPolicy implements IMethodTimeoutRetryPolicy {
    private static final Logger log = LoggerFactory.getLogger(DefaultMethodTimeoutRetryPolicy.class);
    private String serviceInterfaceSimpleName;
    private Map<String, String> methodTimeoutMap;


    public DefaultMethodTimeoutRetryPolicy(String jsonString, String serviceInterfaceSimpleName) {
        this.serviceInterfaceSimpleName = serviceInterfaceSimpleName;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            methodTimeoutMap = objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            log.warn("Json parse failed,will not retry method. error message: {}", e.getMessage());
        }
    }

    @Override
    public int getMethodTimeoutTimes(MethodInvocation methodInvocation) {
        String methodName = serviceInterfaceSimpleName + "." + methodInvocation.getMethod().getName();
        try {
            if (methodTimeoutMap.containsKey(methodName)) {
                return Integer.valueOf(methodTimeoutMap.get(methodName));
            } else {
                return 0;
            }
        } catch (Exception e) {
            log.warn("Json parse failed, will use defaultTimeout. error message: {}", e.getMessage());
            return 0;
        }
    }
}
