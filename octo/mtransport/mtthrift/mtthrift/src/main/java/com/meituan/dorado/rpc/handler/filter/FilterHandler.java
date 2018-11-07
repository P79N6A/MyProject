package com.meituan.dorado.rpc.handler.filter;

import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/5/18
 */
public interface FilterHandler {
    RpcResult handle(RpcInvocation invocation) throws Throwable;
}
