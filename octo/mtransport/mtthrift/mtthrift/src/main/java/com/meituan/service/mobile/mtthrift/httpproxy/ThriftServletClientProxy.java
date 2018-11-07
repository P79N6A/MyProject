package com.meituan.service.mobile.mtthrift.httpproxy;

import com.facebook.swift.codec.ThriftCodecManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.meituan.mtrace.Endpoint;
import com.meituan.service.mobile.mtthrift.util.AnnotationUtil;
import com.meituan.service.mobile.mtthrift.annotation.MTThriftInvocationHandler;
import com.meituan.service.mobile.mtthrift.annotation.ThriftMethodHandler;
import com.meituan.service.mobile.mtthrift.client.cluster.MtThrfitInvokeInfo;
import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceClientTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.util.Consts;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-7-28
 * Time: 下午4:44
 */
public class ThriftServletClientProxy implements FactoryBean<Object>, ApplicationContextAware, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(ThriftServletClientProxy.class);
    private ApplicationContext applicationContext;

    private int socketTimeout = Consts.defaultTimeoutInMills;

    private String appKey;
    private Class<?> serviceInterface;
    private String servletUrl;

    private Object clientProxy;
    private Endpoint localEndpoint;


    private final ThriftCodecManager codecManager;
    private final LoadingCache<MTThriftInvocationHandler.TypeAndName, MTThriftInvocationHandler.ThriftClientMetadata> clientMetadataCache = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<MTThriftInvocationHandler.TypeAndName, MTThriftInvocationHandler.ThriftClientMetadata>()
            {
                @Override
                public MTThriftInvocationHandler.ThriftClientMetadata load(MTThriftInvocationHandler.TypeAndName typeAndName)
                        throws Exception
                {
                    return new MTThriftInvocationHandler.ThriftClientMetadata(typeAndName.getType(), typeAndName.getName(), codecManager);
                }
            });

    public ThriftServletClientProxy() {
        this.codecManager = checkNotNull(new ThriftCodecManager(), "codecManager is null");
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getServletUrl() {
        return servletUrl;
    }

    public void setServletUrl(String servletUrl) {
        this.servletUrl = servletUrl;
    }

    public Object getClientProxy() {
        return clientProxy;
    }

    public void setClientProxy(Object clientProxy) {
        this.clientProxy = clientProxy;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() throws Exception {
        return clientProxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        localEndpoint = new Endpoint(appKey, LocalPointConf.getAppIp(), 0);

        HttpMethodInterceptor clientInterceptor = new HttpMethodInterceptor(this);

        Class<?> _interface;
        if(AnnotationUtil.detectThriftAnnotation(serviceInterface)) {
            _interface = serviceInterface;
            MTThriftInvocationHandler.ThriftClientMetadata clientMetadata = clientMetadataCache
                    .getUnchecked(new MTThriftInvocationHandler.TypeAndName(
                            serviceInterface, serviceInterface.getName()));
            clientInterceptor.setClientMetadata(clientMetadata);
        }
        else {
            _interface = getIfaceInterface();
        }
        ProxyFactory pf = new ProxyFactory(_interface, clientInterceptor);
        clientProxy = pf.getProxy();
    }


    class HttpMethodInterceptor implements MethodInterceptor{

        private ThriftServletClientProxy clientProxy;
        private MTThriftInvocationHandler.ThriftClientMetadata clientMetadata = null;
        private Map<Method, ThriftMethodHandler> methods;
        private final AtomicInteger sequenceId = new AtomicInteger(1);


        public HttpMethodInterceptor(ThriftServletClientProxy clientProxy) {
            this.clientProxy = clientProxy;
        }

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            Method method = methodInvocation.getMethod();
            Object[] args = methodInvocation.getArguments();

            THttpClient thc = new THttpClient(getServletUrl(), getHttpClient());

            MtThrfitInvokeInfo mtThrfitInvokeInfo = new MtThrfitInvokeInfo(appKey,
                    serviceInterface.getSimpleName() + "." + method.getName(), LocalPointConf.getAppIp(), 0,
                    "", 0);  //server ip和port暂时不考虑
            MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(thc, mtThrfitInvokeInfo);
            protocol.setLocalEndpoint(localEndpoint);

            Object result = null;
            if(AnnotationUtil.detectThriftAnnotation(serviceInterface)) {
                result = methods.get(method).invoke(protocol, protocol, sequenceId.getAndIncrement(), args);
            } else {
                Class client = getSynClientClass();
                Constructor con = client.getConstructor(TProtocol.class);
                Object service = con.newInstance(protocol);
                result = method.invoke(service, args);
            }
            return result;

            //TODO:异常处理
        }

        public void setClientMetadata(
                MTThriftInvocationHandler.ThriftClientMetadata clientMetadata) {
            this.clientMetadata = clientMetadata;
            this.methods = clientMetadata.getMethodHandlers();
        }

        public HttpClient getHttpClient() {

            HttpParams params = new BasicHttpParams();
            //设置一些基本参数
            HttpProtocolParams.setVersion(params, Consts.VERSION);
            HttpProtocolParams.setContentCharset(params, Consts.CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);

            //从连接池中取连接的超时时间
            ConnManagerParams.setTimeout(params, Consts.getConnectTimeout);
            //连接超时
            HttpConnectionParams.setConnectionTimeout(params, Consts.connectTimeout);
            //请求超时
            HttpConnectionParams.setSoTimeout(params, socketTimeout);

            //使用线程安全的连接管理来创建HttpClient
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
                    params, getSchemeRegistry());
            HttpClient httpClient = new DefaultHttpClient(conMgr, params);

            return httpClient;
        }

        public SchemeRegistry getSchemeRegistry() {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            URL url;
            try {
                url = new URL(getServletUrl());
                String protocol = url.getProtocol();
                int port = url.getPort();
                if (-1 == port) {
                    if ("https".equals(protocol)) {
                        port = Consts.HTTPS_PORT;
                    } else {
                        port = Consts.HTTP_PORT;
                    }
                }
                if ("https".equals(protocol)) {
                    schemeRegistry.register(new Scheme("https", port,
                            SSLSocketFactory.getSocketFactory()));
                } else {
                    schemeRegistry.register(new Scheme("http", port,
                            PlainSocketFactory.getSocketFactory()));
                }
            } catch (MalformedURLException e) {
                logger.error(e.getMessage(), e);
            }
            return schemeRegistry;
        }
    }


    public Class<?> getIfaceInterface() {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals("Iface")) {
                return c;
            }
        throw new IllegalArgumentException("serviceInterface must contain Sub Interface of Iface");
    }

    private Class<?> getSynClientClass() {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("Client")) {
                return c;
            }
        throw new IllegalArgumentException("serviceInterface must contain Sub Class of Client");
    }
}
