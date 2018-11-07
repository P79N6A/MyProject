package com.meituan.service.mobile.mtthrift.client.invoker;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/9/2
 * Description:
 */
public interface IResponseCollector {
    // result 是接口返回值类型, 需要根据业务返回值类型做转换.
    public void collect(MethodInvocation methodInvocation, Object result);
}
