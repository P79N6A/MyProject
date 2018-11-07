package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.client.invoker.ITimeoutPolicy;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/10/9
 * Description:
 */
public class MyTimeoutPolicy implements ITimeoutPolicy {

    public int getTimeoutByConfig(MethodInvocation methodInvocation, int defaultTimeout) {
        int timeout = defaultTimeout;
        if (methodInvocation.getMethod().getName().equals("testBool")) {
            timeout = 100;
        } else if (methodInvocation.getMethod().getName().equals("testByte")) {
            timeout = 200;
        } else if (methodInvocation.getMethod().getName().equals("testI16")) {
            timeout = 300;
        }
        return timeout;
    }
}
