package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFilter2 implements Filter {
    private static Logger logger = LoggerFactory.getLogger(CustomFilter2.class);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("CustomFilter2 start");
        RpcResult result = nextHandler.handle(invocation);
        logger.info("CustomFilter2 end");
        return result;
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE + 1;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.MULTIROLE;
    }
}
