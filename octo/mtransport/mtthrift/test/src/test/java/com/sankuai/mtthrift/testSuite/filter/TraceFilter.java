package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPI配置
 */
public class TraceFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(TraceFilter.class);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("TraceFilter");
        long start = System.currentTimeMillis();
        RpcResult result = nextHandler.handle(invocation);
        long end = System.currentTimeMillis();
        logger.info("TraceFilter cost={}", (end - start) / 1000);
        return result;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.MULTIROLE;
    }
}
