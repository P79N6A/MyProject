package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterException;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SPI配置
 */
public class ClientQpsLimitFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(ClientQpsLimitFilter.class);
    private static boolean enable = false;
    public static AtomicInteger count = new AtomicInteger(0);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("ClientQpsLimitFilter");
        if (enable && count.incrementAndGet() > 3) {
            throw new FilterException("QpsLimited");
        }
        RpcResult result = nextHandler.handle(invocation);
        logger.info("ClientQpsLimitFilter end");
        return result;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    public static void enable() {
        enable = true;
    }

    public static void disable() {
        enable = false;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.INVOKER;
    }
}
