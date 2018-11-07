package com.meituan.dorado.rpc.handler.filter;

import com.meituan.dorado.common.Role;
import com.meituan.dorado.common.extension.SPI;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;

/**
 * 过滤器接口, 业务可自行实现
 * 全局生效Filter,通过SPI配置
 * bean生效Filter,通过config配置
 *
 * 注意：同一个Filter重复添加无效, 只执行一次
 *
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/5/18
 */
@SPI
public interface Filter extends Role {

    RpcResult filter(RpcInvocation invocation, FilterHandler nextHandler) throws Throwable;

    /**
     * 值越大 优先级越高, 接口调用最先执行
     *
     * @return
     */
    int getPriority();
}
