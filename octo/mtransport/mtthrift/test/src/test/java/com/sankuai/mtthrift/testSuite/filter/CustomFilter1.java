package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFilter1 implements Filter {
    private static Logger logger = LoggerFactory.getLogger(CustomFilter1.class);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("CustomFilter1 start");
        RpcResult result = nextHandler.handle(invocation);
        logger.info("CustomFilter1 end");
        return result;
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.MULTIROLE;
    }
}
