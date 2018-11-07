package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPI配置
 */
public class AccessLogFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("AccessLogFilter request({})", invocation.getServiceInterface().getName() + "." + invocation.getMethod().getName());
        RpcResult result = nextHandler.handle(invocation);
        logger.info("AccessLogFilter response({})", invocation.getServiceInterface().getName() + "." + invocation.getMethod().getName());
        return result;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.MULTIROLE;
    }
}
