package com.meituan.service.mobile.mtthrift.proxy;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.facebook.swift.codec.ThriftStruct;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.auth.*;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.degrage.ServiceDegradeException;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import com.meituan.dorado.rpc.handler.filter.InvokeChainBuilder;
import com.meituan.service.mobile.mtthrift.monitor.IServerMonitor;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceUtils;
import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.meituan.service.mobile.mtthrift.util.LoadInfoUtil;
import com.meituan.service.mobile.mtthrift.util.ThreadLocalUtil;
import com.meituan.service.mobile.mtthrift.util.TraceInfoUtil;
import com.sankuai.octo.protocol.StatusCode;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ThriftServerInvoker<I> implements InvocationHandler {
    private final static Logger logger = LoggerFactory.getLogger(ThriftServerInvoker.class);
    private I target;
    private String serviceSimpleName = "";
    private IServerMonitor monitor;
    private ThriftServerPublisher thriftServerPublisher;
    private Class<?> serviceInterface;
    public static final ThreadLocal<Boolean> isDrop = new ThreadLocal<Boolean>();
    public static final ThreadLocal<String> serviceName = new ThreadLocal<String>();
    public static final ThreadLocal<String> methodName = new ThreadLocal<String>();
    public static final ThreadLocal<Throwable> throwable = new ThreadLocal<Throwable>();
    public static final ThreadLocal<Transaction> transactionThreadLocal = new ThreadLocal<Transaction>();
    public static final ThreadLocal<StatusCode> statusCode = new ThreadLocal<StatusCode>();
    public static final ThreadLocal<String> errorMessage = new ThreadLocal<String>();
    private FilterHandler invokeHandler;

    public ThriftServerInvoker(I serviceImpl, IServerMonitor monitor, ThriftServerPublisher thriftServerPublisher, Class<?> iface) {
        this.target = serviceImpl;
        this.serviceInterface = extractServiceInterface(iface);
        this.serviceSimpleName = serviceInterface.getSimpleName();
        this.monitor = monitor;
        this.thriftServerPublisher = thriftServerPublisher;

        invokeHandler = InvokeChainBuilder.initInvokeChain4Provider(this, thriftServerPublisher.getFilters());
    }

    public static StatusCode getStatusCodeByException(Throwable throwable) {
        Class<?> type = throwable.getClass();

        if (throwable instanceof TBase || type.getAnnotation(ThriftStruct.class) != null) {
            return StatusCode.ApplicationException;
        } else if (throwable instanceof RuntimeException) {
            return StatusCode.RuntimeException;
        } else if (TTransportException.class.equals(type)) {
            return StatusCode.TransportException;
        } else if (TProtocolException.class.equals(type)) {
            return StatusCode.ProtocolException;
        } else if (ServiceDegradeException.class.equals(type)) {
            return StatusCode.DegradeException;
        } else if (AuthFailedException.class.equals(type)) {
            return StatusCode.SecurityException;
        } else if (TException.class.equals(type)) {
            return StatusCode.RemoteException;
        }

        return StatusCode.RpcException;
    }

    private void monitorStats(Method method, Object[] args, long elapsedTime) {
        if (monitor != null) {
            monitor.noticeInvoke(serviceSimpleName, method.getName(), elapsedTime);
        }
    }

    private void monitorException(Method method, Object[] args, Throwable e) {
        if (monitor != null) {
            monitor.noticeException(serviceSimpleName, method.getName(), null, e);
        }
    }

    private void printAccessLog(String methodName, long reponseTime) {
        if (this.thriftServerPublisher.isPrintLog())
            logger.info("client ip: " + ClientInfoUtil.getClientIp() + " - - "
                    + "client appkey: " + ClientInfoUtil.getClientAppKey() + " - - "
                    + "method:" + methodName + " - - "
                    + "reponseTime:" + reponseTime + " ms."
            );
    }

    private boolean requestAuth(String simpleServiceName, Method method, Object[] args, AuthCodeRecord authCodeRecord) {
        if (!ThriftServerGlobalConfig.isEnableAuth()) {
            return true;
        }
        IAuthHandler authHandler = thriftServerPublisher.getAuthHandler();
        String clientAppkey = Tracer.getOneStepContext(AuthUtil.APPKEY);
        if (clientAppkey == null) {
            clientAppkey = ClientInfoUtil.getClientAppKey();
        }

        if (authHandler != null) {
            if (authHandler.getAuthType().equals(AuthType.requestAuth)) {
                String signature = Tracer.getOneStepContext(AuthUtil.SIGNATURE);
                String uniformSignInfo = Tracer.getOneStepContext(AuthUtil.INF_RPC_AUTH);
                AuthMetaData authMetaData = new AuthMetaData();
                authMetaData.setClientAppkey(clientAppkey);
                authMetaData.setClientIp(ClientInfoUtil.getClientIp());
                authMetaData.setSimpleServiceName(simpleServiceName);
                authMetaData.setMethodName(method.getName());
                authMetaData.setArgs(args);
                authMetaData.setSignature(signature);
                authMetaData.setUniformSignInfo(uniformSignInfo);
                boolean authResult = authHandler.auth(authMetaData);

                if (authResult) {
                   if (ThriftServerGlobalConfig.isEnableCat()) {
                       Cat.logEvent("OctoAuth.success.client.appkey", clientAppkey);
                       Cat.logEvent("OctoAuth.success.client.ip", ClientInfoUtil.getClientIp());
                   }
                } else {
                    if (ThriftServerGlobalConfig.isEnableCat()) {
                        if (ThriftServerGlobalConfig.isEnableGrayAuth()) {
                            Cat.logEvent("OctoAuth.gray.client.appkey", clientAppkey);
                            Cat.logEvent("OctoAuth.gray.client.ip", ClientInfoUtil.getClientIp());
                        } else {
                            Cat.logEvent("OctoAuth.fail.client.appkey", clientAppkey);
                            Cat.logEvent("OctoAuth.fail.client.ip", ClientInfoUtil.getClientIp());
                        }
                    }

                    authCodeRecord.setAuthCode(authMetaData.getAuthCode());
                    return authResult;
                }
            } else if (authHandler.getAuthType().equals(AuthType.channelAuth)) {
                if (ThriftServerGlobalConfig.isEnableCat()) {
                    boolean authGray = ThreadLocalUtil.getAuthGray() != null && ThreadLocalUtil.getAuthGray();
                    if (!authGray) {
                        Cat.logEvent("OctoAuth.success.client.appkey", clientAppkey);
                        Cat.logEvent("OctoAuth.success.client.ip", ClientInfoUtil.getClientIp());
                    }
                }
            }
        } else {
            if (ThriftServerGlobalConfig.isEnableCat()) {
                Cat.logEvent("OctoAuth.none.client.appkey", clientAppkey);
                Cat.logEvent("OctoAuth.none.client.ip", ClientInfoUtil.getClientIp());
            }
        }
        return true;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        if (thriftServerPublisher.isDestroyed()) {
            throw new TException("server has been destroyed, invalid invoke!");
        }

        throwable.remove();
        statusCode.remove();
        errorMessage.remove();
        transactionThreadLocal.remove();
        serviceName.set(serviceSimpleName);
        methodName.set(method.getName());

        //cat埋点提前
        if (ThriftServerGlobalConfig.isEnableCat()) {
            Transaction transaction = Cat.newTransaction("OctoService", serviceSimpleName + "." + method.getName());
            transactionThreadLocal.set(transaction);
        }

        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        if (!requestAuth(serviceSimpleName, method, args, authCodeRecord)) {
            String appkey = thriftServerPublisher.getAppKey();
            String clientAppkey = Tracer.getOneStepContext(AuthUtil.APPKEY);
            if (clientAppkey == null) {
                clientAppkey = ClientInfoUtil.getClientAppKey();
            }
            String clientIp = ClientInfoUtil.getClientIp();
            String methodName = method.getName();
            String spanName = serviceSimpleName + "." + methodName;
            int authCode = authCodeRecord.getAuthCode();
            String authType = null;
            if (thriftServerPublisher.getAuthHandler() != null) {
                authType = thriftServerPublisher.getAuthHandler().getAuthType().name();
            }

            String log = String.format("AuthFailedException: Client(%s:%s) auth failed, invoke Server(%s) method Name is %s,error code: %s",
                    clientAppkey, clientIp, appkey, spanName, authCode);

            if (ThriftServerGlobalConfig.isEnableAuthErrorLog()) {
                logger.error(log);
            }

            if (!ThriftServerGlobalConfig.isEnableGrayAuth()) {
                MtraceUtils.serverMark(Tracer.STATUS.EXCEPTION);
                // pegion依赖这个错误码
                statusCode.set(StatusCode.SecurityException);
                throw new AuthFailedException(log);
            } else {
                TraceInfoUtil.catRecordAuthFail(clientAppkey, spanName, clientIp, authCode, authType, ThreadLocalUtil.getIsUnifiedProto());
            }
        }

        if (!AuthorUtil.authoriseConsumer(ClientInfoUtil.getClientAppKey(),
                thriftServerPublisher.getAppKey(), ClientInfoUtil.getClientIp())) {
            String log = "unAhorizedConsumer:" + ClientInfoUtil.getClientIp()
                    + "|" + ClientInfoUtil.getClientAppKey() + "|" + method.getName();
            logger.error(log);
            MtraceUtils.serverMark(Tracer.STATUS.EXCEPTION);
            throw new AuthFailedException(log);
        }

        RpcInvocation invocation = new RpcInvocation(serviceInterface, method, args);
        return invokeHandler.handle(invocation).getReturnVal();
    }

    public Object doInvoke(RpcInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();

        long begin = System.currentTimeMillis();

        if (ThriftServerGlobalConfig.isEnableDegradation()) {
            boolean degrade = thriftServerPublisher.getServerDegradHandler().
                    checkDegradeEvent(serviceSimpleName, method.getName());

            if (degrade) {
                String log = "Request is degraded!" + ClientInfoUtil.getClientIp()
                        + "|" + ClientInfoUtil.getClientAppKey() + "|" + method.getName();
                MtraceUtils.serverMark(Tracer.STATUS.DROP);
                isDrop.set(true);
                statusCode.set(StatusCode.DegradeException);
                errorMessage.set(log);
                throw new ServiceDegradeException(log);
            } else {
                isDrop.set(false);
            }
        }

        try {
            Object ret = method.invoke(target, args);
            MtraceUtils.serverMark(Tracer.STATUS.SUCCESS);
            statusCode.set(StatusCode.Success);
            return ret;
        } catch (Exception e) {
            Throwable cause = (e instanceof InvocationTargetException) ? e.getCause() : e;
            if (cause == null) {
                cause = e;
            }

            monitorException(method, args, cause);
            statusCode.set(getStatusCodeByException(cause));
            errorMessage.set(cause.getMessage());

            if (!(cause instanceof TBase)) {
                logger.error("mtthrift server invoker Exception, traceId:{}", Tracer.id(), cause);
                throwable.set(cause);
                MtraceUtils.serverMark(Tracer.STATUS.EXCEPTION);
            } else {
                MtraceUtils.serverMark(Tracer.STATUS.SUCCESS);
            }
            throw cause;
        } finally {
            LoadInfoUtil.incrSpanInvocationCount(this.serviceSimpleName + "." + methodName);
            monitorStats(method, args, System.currentTimeMillis() - begin);
            printAccessLog(method.getName(), System.currentTimeMillis() - begin);

        }
    }

    private Class<?> extractServiceInterface(Class<?> iface) {
        // IDL使用方式传入的iface是com.**.**$Iface, 需要获取实际的接口类即Iface的外部类
        if (iface.isMemberClass() && iface.getEnclosingClass() != null) {
            return iface.getEnclosingClass();
        }
        return iface;
    }
}
