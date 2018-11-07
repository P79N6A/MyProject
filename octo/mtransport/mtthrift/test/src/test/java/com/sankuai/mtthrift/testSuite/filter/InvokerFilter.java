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
public class InvokerFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(InvokerFilter.class);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("InvokerFilter start");
        RpcResult result = null;
        try {
            result = nextHandler.handle(invocation);
        } catch (Exception e) {
            logger.info("InvokerFilter end have Exception");
            FilterTest.exceptionInfoStr.append("InvokerFilter end have Exception");
            throw e;
        }
        logger.info("InvokerFilter end");
        return result;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.INVOKER;
    }
}
