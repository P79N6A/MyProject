package com.meituan.service.mobile.mtthrift.client.invoker;

import org.aopalliance.intercept.MethodInvocation;

/**
 * User: Zhanran
 * Date: 2017-07-25
 * Time: 下午2:30
 * 方法粒度超时重传
 */
public interface IMethodTimeoutRetryPolicy {
    /**
     * @return methodTimeoutTimes  方法粒度超时重传次数
     */
    public int getMethodTimeoutTimes(MethodInvocation methodInvocation);
}
