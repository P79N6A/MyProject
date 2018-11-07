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
public class ProviderFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(ProviderFilter.class);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("ProviderFilter start");
        RpcResult result = null;
        try {
            result = nextHandler.handle(invocation);
        } catch (Exception e) {
            logger.info("ProviderFilter end have Exception");
            FilterTest.exceptionInfoStr.append("ProviderFilter end have Exception");
            throw e;
        }
        logger.info("ProviderFilter end");
        return result;
    }

    @Override
    public int getPriority() {
        return -10;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.PROVIDER;
    }
}
