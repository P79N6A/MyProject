package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/4/12
 */
public class SpecificFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(SpecificFilter.class);

    @Override
    public RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable {
        FilterTest.invokeChainStr.append(this.getClass().getSimpleName());

        logger.info("SpecificFilter start");
        RpcResult result = nextHandler.handle(invocation);
        logger.info("SpecificFilter end");
        return result;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.MULTIROLE;
    }
}
