package com.meituan.service.mobile.mtthrift.client.invoker;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Transaction;
import com.meituan.mtrace.Span;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.client.cluster.MtThrfitInvokeInfo;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.netty.exception.RequestTimeoutException;
import com.meituan.service.mobile.mtthrift.util.SizeUtil;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

/**
 * User: YangXuehua
 * Date: 14-3-21
 * Time: 下午1:20
 */
public class MTThriftAsyncMethodCallback<T> implements AsyncMethodCallback<T> {

    private long begin = System.currentTimeMillis();
    private AsyncMethodCallback<T> serviceCallback;
    private MTThriftMethodInterceptor mtThriftMethodInterceptor;
    private ServerConn serverConn;
    private TTransport socket;
    private Span clientSpan;
    private String methodName;
    private MtThrfitInvokeInfo mtThrfitInvokeInfo;
    private String routeType;
    private String protocolType;
    private boolean isNettyIO;
    private String ioType = "thrift";
    private String thriftType;
    private int requestSize;
    private int responseSize;
    private ForkableTransaction forkableTransaction;
    private int timeout;

    public MTThriftAsyncMethodCallback(AsyncMethodCallback<T> serviceCallback,
            MTThriftMethodInterceptor mtThriftMethodInterceptor,
            ServerConn serverConn,
            TTransport socket,
            String methodName,
            MtThrfitInvokeInfo mtThrfitInvokeInfo,
            ForkableTransaction forkableTransaction) {
        this.serviceCallback = serviceCallback;
        this.mtThriftMethodInterceptor = mtThriftMethodInterceptor;
        this.serverConn = serverConn;
        this.socket = socket;
        this.methodName = methodName;
        this.mtThrfitInvokeInfo = mtThrfitInvokeInfo;
        this.forkableTransaction = forkableTransaction;
    }

    @Override
    public void onComplete(T response) {

        mtThriftMethodInterceptor.returnConnection(serverConn, socket);
        String serverIpPort = serverConn.getServer().getIp() + ":" + serverConn.getServer().getPort();
        mtThriftMethodInterceptor.noticeInvoke(methodName, serverIpPort, System.currentTimeMillis() - begin);
        Object result = null;
        try {
            if (isNettyIO) {
                result = response;
            } else {
                Method m = response.getClass().getMethod("getResult");
                result = m.invoke(response);
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                Throwable realException = ((InvocationTargetException) e).getTargetException();

                if (realException instanceof TApplicationException) {
                    int type = ((TApplicationException) realException).getType();
                    if (type == TApplicationException.MISSING_RESULT) {
                        result = null;

                        execCatMonitor(null);
                        execMtraceMonitor(null);

                        if (serviceCallback instanceof OctoThriftCallback) {
                            ((OctoThriftCallback) serviceCallback).onCompleteWithoutReflect(result);
                        } else {
                            serviceCallback.onComplete(response);
                        }

                    } else {
                        execCatMonitor(e);
                        execMtraceMonitor(e);
                        if (serviceCallback != null) {
                            serviceCallback.onError(new TException(realException));
                        }
                    }
                    return;
                } else if (realException instanceof TBase) {
                    //自定义异常统计为SUCCESS
                    execCatMonitor(null);
                    execMtraceMonitor(null);
                    if (serviceCallback != null) {
                        serviceCallback.onError(new TException(realException));
                    }
                    return;
                }
            }
        }

        execCatMonitor(null);
        execMtraceMonitor(null);
        if (serviceCallback instanceof OctoThriftCallback) {
            ((OctoThriftCallback) serviceCallback).onCompleteWithoutReflect(result);
        } else if (serviceCallback != null) {
            serviceCallback.onComplete(response);
        }

    }

    @Override
    public void onError(Exception exception) {
        execCatMonitor(exception);
        execMtraceMonitor(exception);
        mtThriftMethodInterceptor.returnBrokenConnection(serverConn, socket);
        if (serviceCallback != null) {
            serviceCallback.onError(exception);
        }
    }

    public void execCatMonitor(Throwable exception) {
        ForkedTransaction forkedTransaction = null;
        try {
            if (ThriftClientGlobalConfig.isEnableCat()) {
                if (forkableTransaction != null) {
                    forkedTransaction = forkableTransaction.doFork();
                }
                Transaction transaction = Cat.newTransactionWithDuration("OctoCall", mtThrfitInvokeInfo.getSvcIdentification(), System.currentTimeMillis() - begin);
                Cat.logEvent("OctoCall.appkey", mtThrfitInvokeInfo.getServerAppKey());
                Cat.logEvent("OctoCall.serverIp", mtThrfitInvokeInfo.getServerIp());
                Cat.logEvent("OctoCall.callType", "async");
                Cat.logEvent("OctoCall.thriftType", thriftType);
                Cat.logEvent("OctoCall.timeout", String.valueOf(timeout));
                Cat.logEvent("OctoCall.routeType", routeType);
                Cat.logEvent("OctoCall.protocolType", protocolType);
                if (requestSize != 0)
                    Cat.logEvent("OctoCall.requestSize", SizeUtil.getLogSize(requestSize));
                if (responseSize != 0)
                    Cat.logEvent("OctoCall.responseSize", SizeUtil.getLogSize(responseSize));
                Cat.logEvent("OctoCall.IOType", ioType);

                if (exception != null) {
                    transaction.setStatus(exception);
                    Cat.logErrorWithCategory("OctoCall." + serverConn.getServer().getServerAppKey(), exception);
                } else {
                    transaction.setStatus(Transaction.SUCCESS);
                }
                transaction.complete();
            }
        } finally {
            if (forkedTransaction != null) {
                forkedTransaction.close();
            }
        }
    }

    public void execMtraceMonitor(Throwable exception) {
        if (ThriftClientGlobalConfig.isEnableMtrace()) {
            if (clientSpan != null) {
                if (exception != null) {
                    if (exception instanceof TimeoutException || exception instanceof RequestTimeoutException) {
                        clientSpan.setStatus(Tracer.STATUS.TIMEOUT);
                    } else if (exception.getCause() != null && exception.getCause().getMessage() != null &&
                            exception.getCause().getMessage().contains("ServiceDegradeException")) {
                        clientSpan.setStatus(Tracer.STATUS.DROP);
                    } else {
                        clientSpan.setStatus(Tracer.STATUS.EXCEPTION);
                    }
                } else {
                    clientSpan.setStatus(Tracer.STATUS.SUCCESS);
                }
            }
            Tracer.clientRecvAsync(clientSpan);
        }
    }

    public void setNettyIO(boolean nettyIO) {
        isNettyIO = nettyIO;
        ioType = "netty";
    }

    public void setRequestSize(int requestSize) {
        this.requestSize = requestSize;
    }

    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public void setClientSpan(Span clientSpan) {
        this.clientSpan = clientSpan;
    }

    public void setThriftType(String thriftType) {
        this.thriftType = thriftType;
    }

    public ForkableTransaction getForkableTransaction() {
        return forkableTransaction;
    }

    public void setForkableTransaction(ForkableTransaction forkableTransaction) {
        this.forkableTransaction = forkableTransaction;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
