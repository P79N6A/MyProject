package com.meituan.service.mobile.mtthrift.client.invoker;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/10/9
 * Description:
 */
public interface ITimeoutPolicy {
    /**
     * @param methodInvocation
     * @return timeout in milli-second
     */
    public int getTimeoutByConfig(MethodInvocation methodInvocation, int defaultTimeout);
}
