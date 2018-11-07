package com.meituan.service.mobile.mtthrift.client.invoker;

import com.meituan.service.mobile.mtthrift.mtrace.MtthriftErrorCode;

/**
 * User: YangXuehua
 * Date: 14-4-9
 * Time: 下午2:30
 * mtthrift请求过滤器
 */
public interface IMTThriftFilter {
    /**
     * 请求过滤器
     * @param methodName 要调用的方法名
     * @param clientAppKey 客户端appKey
     * @param clientIp 客户端ip
     * @return null继续执行；MtthriftErrorCode 中断执行的拦截理由
     */
    public MtthriftErrorCode isReject(String methodName, String clientAppKey, String clientIp);
}
