package com.meituan.dorado.rpc.handler.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.common.extension.ExtensionLoader;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import com.meituan.service.mobile.mtthrift.client.invoker.MTThriftMethodInterceptor;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerInvoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/3/5
 */
public class InvokeChainBuilder {

    // 调用端或服务端全局生效的Filter
    private static ConcurrentMap<RpcRole, List<Filter>> globalFilters = new ConcurrentHashMap<RpcRole, List<Filter>>();

    static {
        List<Filter> invokeFilters = ExtensionLoader.getExtensionListByRole(Filter.class, RpcRole.INVOKER);
        List<Filter> providerFilters = ExtensionLoader.getExtensionListByRole(Filter.class, RpcRole.PROVIDER);
        globalFilters.put(RpcRole.INVOKER, invokeFilters);
        globalFilters.put(RpcRole.PROVIDER, providerFilters);
    }

    public static FilterHandler initInvokeChain4Invoker(final MTThriftMethodInterceptor methodInterceptor,
                                                        List<Filter> cfgFilters) {
        FilterHandler serviceInvokeHandler = new FilterHandler() {
            @Override
            public RpcResult handle(RpcInvocation invocation) throws Throwable {
                Object result = methodInterceptor.doInvoke(invocation);
                return new RpcResult(result);
            }
        };
        return buildInvokeChain(serviceInvokeHandler, RpcRole.INVOKER, cfgFilters);
    }

    public static FilterHandler initInvokeChain4Provider(final ThriftServerInvoker serverInvoker,
                                                         List<Filter> cfgFilters) {
        FilterHandler serviceInvokeHandler = new FilterHandler() {
            @Override
            public RpcResult handle(RpcInvocation invocation) throws Throwable {
                Object result = serverInvoker.doInvoke(invocation);
                return new RpcResult(result);
            }
        };
        return buildInvokeChain(serviceInvokeHandler, RpcRole.PROVIDER, cfgFilters);
    }

    public static FilterHandler buildInvokeChain(FilterHandler actualHandler, RpcRole role, List<Filter> cfgFilters) {
        List<Filter> serviceFilters = new ArrayList<Filter>();
        if (globalFilters.get(role) != null) {
            serviceFilters.addAll(globalFilters.get(role));
        }
        if (cfgFilters != null) {
            // 避免在config中又配置了SPI的Filter或重复配置导致重复添加
            for (Filter filter : cfgFilters) {
                boolean hasExist = false;
                for (Filter existFilter : serviceFilters) {
                    if (existFilter.getClass().getName().equals(filter.getClass().getName())) {
                        hasExist = true;
                          break;
                    }
                }
                if (!hasExist) {
                    serviceFilters.add(filter);
                }
            }
        }

        Collections.sort(serviceFilters, FilterComparator.INSTANCE);
        FilterHandler first = actualHandler;
        for (final Filter filter : serviceFilters) {
            final FilterHandler next = first;
            first = new FilterHandler() {
                @Override
                public RpcResult handle(RpcInvocation invocation) throws Throwable {
                    return filter.filter(invocation, next);
                }
            };
        }
        return first;
    }

    private static class FilterComparator implements Comparator<Filter> {
        private static FilterComparator INSTANCE = new FilterComparator();

        @Override
        public int compare(Filter o1, Filter o2) {
            // 按priority顺序排序
            return Integer.compare(o1.getPriority(), o2.getPriority());
        }
    }
}
