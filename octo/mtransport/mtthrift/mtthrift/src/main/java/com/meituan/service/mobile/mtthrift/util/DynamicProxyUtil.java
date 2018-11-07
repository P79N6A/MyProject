package com.meituan.service.mobile.mtthrift.util;

import com.meituan.service.mobile.mtthrift.monitor.IServerMonitor;
import com.meituan.service.mobile.mtthrift.proxy.ThriftInterceptor;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerInterceptor;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerInvoker;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class DynamicProxyUtil {

    public static <V, R> R createJdkDynamicProxy(Class<R> interfac, V impl, IServerMonitor serverMonitor, ThriftServerPublisher thriftServerPublisher) {

        Class[] interfaces = impl.getClass().getInterfaces();

        List<Class> list = new ArrayList<Class>();
        for (Class inter : interfaces) {
            list.add(inter);
        }

        if (!list.contains(interfac)) {
            list.add(interfac);
        }
        Class[] newInterfaces = list.toArray(new Class[1]);

        R proxy;
        InvocationHandler thriftServerInvoker = new ThriftServerInvoker<V>(impl, serverMonitor, thriftServerPublisher, interfac);
        List<ThriftInterceptor> interceptors = thriftServerPublisher.getInterceptors();

        if(interceptors != null && !interceptors.isEmpty()) {
            ThriftServerInterceptor thriftServerInterceptor = new ThriftServerInterceptor(thriftServerInvoker, interceptors);
            proxy = (R) Proxy
                    .newProxyInstance(impl.getClass().getClassLoader(), newInterfaces, thriftServerInterceptor);
        } else {
            proxy = (R) Proxy
                    .newProxyInstance(impl.getClass().getClassLoader(), newInterfaces, thriftServerInvoker);

        }

        return proxy;
    }
}
