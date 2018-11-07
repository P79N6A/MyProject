package com.meituan.service.mobile.mtthrift.proxy;

import com.google.common.collect.ImmutableMap;
import com.meituan.service.mobile.mtthrift.auth.IAuthHandler;
import com.meituan.service.mobile.mtthrift.server.MTTServer;
import com.meituan.service.mobile.mtthrift.server.http.handler.check.ServiceIfaceInfo;
import com.meituan.service.mobile.mtthrift.util.AnnotationUtil;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/2
 */
public class ThriftServerRepository {

    private static final Logger logger = LoggerFactory.getLogger(ThriftServerRepository.class);

    private static Date startTime;
    // 应该只有一个appkey, 避免多appkey问题, 使用set
    private static final Set<String> appkeys = new HashSet<String>();
    private static final ConcurrentMap<String, MTTServer> portServer = new ConcurrentHashMap<String, MTTServer>();
    private static final ConcurrentMap<String, List<ServiceIfaceInfo>> portServiceInfo = new ConcurrentHashMap<String, List<ServiceIfaceInfo>>();
    private static final ConcurrentMap<String, Object> serviceImplMap = new ConcurrentHashMap<String, Object>();
    private static final ConcurrentMap<String, Class<?>> serviceIfaceMap = new ConcurrentHashMap<String, Class<?>>();
    private static final ConcurrentMap<String, ImmutableMap<String, Method>> serviceMethodsMap = new ConcurrentHashMap<String, ImmutableMap<String, Method>>();

    public static void addServiceInfo(String appkey, MTTServer server, IAuthHandler authHandler) {
        try {
            addServerInfo(appkey, server, authHandler);

            String port = String.valueOf(server.getLocalEndpoint().getPort());
            List<ServiceIfaceInfo> serviceIfaceInfos = new ArrayList<ServiceIfaceInfo>();
            serviceIfaceInfos.add(new ServiceIfaceInfo(server.getServiceInterface().getName(), server.getServiceImpl().getClass().getName()));
            portServiceInfo.put(port, serviceIfaceInfos);
            serviceImplMap.put(server.getServiceInterface().getName(), server.getServiceImpl());
            serviceIfaceMap.put(server.getServiceInterface().getName(), server.getServiceInterface());
        } catch (Exception e) {
            // 防御性容错, 避免影响服务启动
            logger.error("Mhthrift record server info fail.", e);
        }
    }

    public static void addServiceInfo(String appkey, Map<Class<?>, ThriftServiceBean> serviceProcessorMap, MTTServer server, IAuthHandler authHandler) {
        try {
            addServerInfo(appkey, server, authHandler);

            String port = String.valueOf(server.getLocalEndpoint().getPort());
            List<ServiceIfaceInfo> serviceIfaceInfos = new ArrayList<ServiceIfaceInfo>();
            for (Map.Entry<Class<?>, ThriftServiceBean> entry : serviceProcessorMap.entrySet()) {
                serviceIfaceInfos.add(new ServiceIfaceInfo(entry.getKey().getName(), entry.getValue().serviceImpl.getClass().getName()));
                serviceImplMap.put(entry.getKey().getName(), entry.getValue().serviceImpl);
                serviceIfaceMap.put(entry.getKey().getName(), entry.getKey());
            }
            portServiceInfo.put(port, serviceIfaceInfos);
        } catch (Exception e) {
            // 防御性容错, 避免影响服务启动
            logger.error("Mhthrift record server info fail.", e);
        }
    }

    private static void addServerInfo(String appkey, MTTServer server, IAuthHandler authHandler) {
        appkeys.add(appkey);
        String port = String.valueOf(server.getLocalEndpoint().getPort());
        if (server != null) {
            portServer.put(port, server);
        }

        if (startTime == null) {
            startTime = new Date();
        }
    }

    public static Date getStartTime() {
        return startTime;
    }

    public static Set<String> getAppkeys() {
        return appkeys;
    }

    public static ConcurrentMap<String, MTTServer> getPortServer() {
        return portServer;
    }

    public static ConcurrentMap<String, List<ServiceIfaceInfo>> getPortServiceInfo() {
        return portServiceInfo;
    }

    public static ConcurrentMap<String, Object> getServiceImplMap() {
        return serviceImplMap;
    }

    public static ConcurrentMap<String, Class<?>> getServiceIfaceMap() {
        return serviceIfaceMap;
    }

    public static ImmutableMap<String, Method> getServiceMethods(String serviceName) {
        ImmutableMap<String, Method> methodMap = serviceMethodsMap.get(serviceName);
        if (methodMap == null) {
            synchronized (ThriftServerRepository.class) {
                methodMap = serviceMethodsMap.get(serviceName);
                if (methodMap == null) {
                    Class interfaceClazz = getInterface(serviceIfaceMap.get(serviceName));
                    if (interfaceClazz == null) {
                        throw new IllegalArgumentException("No serviceIface of serviceName=" + serviceName);
                    }
                    ImmutableMap.Builder<String, Method> methodMapBuilder = ImmutableMap.builder();
                    for (Method method : interfaceClazz.getMethods()) {
                        methodMapBuilder.put(MethodUtil.generateMethodSignature(method), method);
                    }
                    methodMap = methodMapBuilder.build();
                    serviceMethodsMap.putIfAbsent(serviceName, methodMap);
                }
            }
        }
        return methodMap;
    }

    public static ConcurrentMap<String, ImmutableMap<String, Method>> getAllMethods() {
        if (serviceMethodsMap.size() != serviceImplMap.size()) {
            for (String serviceName : serviceImplMap.keySet()) {
                getServiceMethods(serviceName);
            }
        }
        return serviceMethodsMap;
    }

    private static Class getInterface(Class serviceInterface) {
        if (AnnotationUtil.detectThriftAnnotation(serviceInterface)) {
            return serviceInterface;
        }
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals(Consts.THRIFT_IDL_IFACE)) {
                return c;
            }
        throw new IllegalArgumentException(serviceInterface.getName() + " no sub interface of Iface");
    }
}
