package com.meituan.service.mobile.mtthrift.server.http.handler;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.meituan.dorado.common.RpcRole;
import com.meituan.mtrace.ITracer;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerRepository;
import com.meituan.service.mobile.mtthrift.server.http.NettyHttpSender;
import com.meituan.service.mobile.mtthrift.server.http.meta.HttpInvokeParam;
import com.meituan.service.mobile.mtthrift.server.http.meta.MethodParameter;
import com.meituan.service.mobile.mtthrift.util.*;
import com.meituan.service.mobile.mtthrift.util.json.JacksonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/2/9
 */
public class HttpInvokeHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpInvokeHandler.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private Map<String, Class<?>> classMap = new ConcurrentHashMap<String, Class<?>>();

    @Override
    public void handle(NettyHttpSender httpSender, String uri, byte[] content) {
        HttpInvokeParam httpInvokeParam = null;

        Transaction catTransaction = null;
        try {
            String invokeParamStr = new String(content, "UTF-8");
            httpInvokeParam = mapper.readValue(invokeParamStr, HttpInvokeParam.class);
            if (!requestCheck(httpSender, httpInvokeParam)) {
                return;
            }
            catTransaction = reportPrepare(httpInvokeParam, httpSender.getClientIP(), content.length);

            Object result = invoke(httpInvokeParam);
            httpSender.sendCustomObjectJson(result);
            catTransaction.setStatus(Message.SUCCESS);
            Tracer.getServerTracer().setStatus(Tracer.STATUS.SUCCESS);
        } catch (Throwable e) {
            String callMethod = "";
            if (httpInvokeParam != null) {
                callMethod = httpInvokeParam.getServiceName() + "." + httpInvokeParam.getMethodName();
            }
            Throwable actualException = e;
            if (e instanceof InvocationTargetException) {
                actualException = e.getCause();
                httpSender.sendErrorResponse("服务方法调用异常{" + callMethod + "}:" + actualException.getClass().getName() + ":" + actualException.getMessage());
                logger.warn("Http invoke failed(method:{}), cause: {}", callMethod, actualException.getMessage(), actualException);
            } else {
                httpSender.sendErrorResponse("调用异常:" + e.getClass().getName() + ":" + e.getMessage());
                logger.error("Http invoke failed, cause:{}", e.getMessage(), e);
            }

            String catReportName = callMethod + "[httpInvoke]";
            if (catTransaction == null) {
                catTransaction = catReportException(catReportName, actualException);
            } else {
                Cat.logErrorWithCategory("OctoService" + catReportName, actualException);
                catTransaction.setStatus(actualException);
            }
            Tracer.getServerTracer().setStatus(Tracer.STATUS.EXCEPTION);
        } finally {
            if (catTransaction != null) {
                catTransaction.complete();
            }
            Tracer.getServerTracer().flush();
        }
    }

    private boolean requestCheck(NettyHttpSender httpSender, HttpInvokeParam httpInvokeParam) {
        if (StringUtils.isBlank(httpInvokeParam.getClientAppkey())) {
            httpSender.sendErrorResponse("Client Appkey为空, 不能发起调用");
            return false;
        }
        if (!ThriftServerRepository.getAppkeys().contains(httpInvokeParam.getServerAppkey())) {
            httpSender.sendErrorResponse("Appkey校验失败, 不能发起调用");
            return false;
        }
        return true;
    }

    private Object invoke(HttpInvokeParam httpInvokeParam) throws Exception {
        String serviceName = httpInvokeParam.getServiceName();
        String methodName = httpInvokeParam.getMethodName();
        if (StringUtils.isBlank(serviceName) || StringUtils.isBlank(methodName)) {
            throw new IllegalArgumentException("Http params no serviceName or methodName");
        }
        List<MethodParameter> parameterList = httpInvokeParam.getParameters();

        Object serviceImpl = ThriftServerRepository.getServiceImplMap().get(serviceName);
        if (serviceImpl == null) {
            throw new IllegalArgumentException("No serviceImpl of serviceName=" + serviceName);
        }
        ImmutableMap<String, Method> methodMap = ThriftServerRepository.getServiceMethods(serviceName);
        Class<?>[] paramTypes = null;
        Object[] args = null;
        if (parameterList != null && parameterList.size() > 0) {
            paramTypes = new Class<?>[parameterList.size()];
            args = new Object[parameterList.size()];
            for (int i = 0; i < parameterList.size(); i++) {
                MethodParameter parameter = parameterList.get(i);
                String type = parameter.getTypeStr();
                Class<?> clazz = classMap.get(type);
                if (clazz == null) {
                    clazz = ClassLoaderUtil.loadClass(type);
                    classMap.put(type, clazz);
                }
                String argStr = parameter.getArgStr();
                Object arg = JacksonUtils.deserialize(argStr, clazz);
                if (StringUtils.isNotBlank(argStr) && arg == null) {
                    throw new IllegalArgumentException("Deserialize failed, paramType=" + type + ", arg=" + argStr);
                }
                paramTypes[i] = clazz;
                args[i] = arg;
            }
        }
        String methodSignature = MethodUtil.generateMethodSignature(methodName, paramTypes);
        Method method = methodMap.get(methodSignature);
        if (method == null) {
            throw new NoSuchMethodException(
                    "http invoke failed，can not find method with signature: " + serviceName + "."
                            + methodSignature);
        }
        Object result = method.invoke(serviceImpl, args);
        return result;
    }

    @Override
    public void setRole(RpcRole role) {
    }

    @Override
    public RpcRole getRole() {
        return RpcRole.PROVIDER;
    }

    private Transaction catReportException(String catReportName, Throwable exception) {
        Transaction transaction = Cat.newTransaction("OctoService", catReportName);
        Cat.logErrorWithCategory("OctoService." + catReportName, exception);
        transaction.setStatus(exception);
        return transaction;
    }

    private Transaction reportPrepare(HttpInvokeParam httpInvokeParam, String clientIp, int size) {
        String serviceSimpleName = ThriftServerRepository.getServiceIfaceMap().get(httpInvokeParam.getServiceName()).getSimpleName();
        String name = serviceSimpleName + "." + httpInvokeParam.getMethodName() + "[httpInvoke]";
        Transaction transaction = Cat.newTransaction("OctoService", name);
        Cat.logEvent("OctoService.appkey", httpInvokeParam.getClientAppkey());
        Cat.logEvent("OctoService.clientIp", clientIp);
        Cat.logEvent("OctoService.handleType", "accept");
        Cat.logEvent("OctoService.requestSize", SizeUtil.getLogSize(size));
        Cat.logEvent("OctoService.httpinvoke", "true");
        Cat.logEvent("OctoService.httpinvoke.methodName", httpInvokeParam.getMethodName());
        String parameters = httpInvokeParam.getParameters() == null ? null : httpInvokeParam.getParameters().toString();
        Cat.logEvent("OctoService.httpinvoke.parameters", parameters);

        ITracer tracer = Tracer.getServerTracer();
        tracer.record(name);
        tracer.setRemoteAppKey(httpInvokeParam.getClientAppkey());
        tracer.setRemoteIp(clientIp);
        tracer.setInfraName(Consts.mtraceInfra);
        tracer.setInfraVersion(MtThriftManifest.getVersion());
        Tracer.setTest(httpInvokeParam.isTest());

        return transaction;
    }
}