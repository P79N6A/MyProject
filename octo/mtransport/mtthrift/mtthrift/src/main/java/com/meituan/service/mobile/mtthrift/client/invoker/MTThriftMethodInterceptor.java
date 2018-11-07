package com.meituan.service.mobile.mtthrift.client.invoker;

import com.dianping.cat.Cat;
import com.dianping.cat.message.ForkableTransaction;
import com.dianping.cat.message.Transaction;
import com.google.common.util.concurrent.SettableFuture;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.mtrace.ITracer;
import com.meituan.mtrace.Span;
import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.annotation.MTThriftInvocationHandler;
import com.meituan.service.mobile.mtthrift.annotation.MTThriftInvocationHandler.ThriftClientMetadata;
import com.meituan.service.mobile.mtthrift.annotation.ThriftMethodHandler;
import com.meituan.service.mobile.mtthrift.auth.ISignHandler;
import com.meituan.service.mobile.mtthrift.auth.SignMetaData;
import com.meituan.service.mobile.mtthrift.client.cell.RouterMetaData;
import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.client.cluster.MtThrfitInvokeInfo;
import com.meituan.service.mobile.mtthrift.client.model.Entry;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.client.rhino.RhinoContext;
import com.meituan.service.mobile.mtthrift.client.rhino.RhinoHandler;
import com.meituan.service.mobile.mtthrift.client.rhino.faultInject.FaultInjectHandler;
import com.meituan.service.mobile.mtthrift.client.route.ILoadBalancer;
import com.meituan.service.mobile.mtthrift.client.route.RandomLoadBalancer;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import com.meituan.dorado.rpc.handler.filter.InvokeChainBuilder;
import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.netty.NettyTSocket;
import com.meituan.service.mobile.mtthrift.netty.channel.IChannel;
import com.meituan.service.mobile.mtthrift.netty.channel.IChannelPool;
import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannel;
import com.meituan.service.mobile.mtthrift.netty.exception.ChannelPoolException;
import com.meituan.service.mobile.mtthrift.netty.exception.RequestTimeoutException;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcRequest;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcResponse;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.*;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.pool.ObjectPool;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-6-6
 * Time: 下午6:26
 * <p/>
 * To change this template use File | Settings | File Templates.
 */
public class MTThriftMethodInterceptor implements MethodInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(MTThriftMethodInterceptor.class);
    private static ConcurrentMap<String, Method> cachedMethod = new ConcurrentHashMap<String, Method>();
    private ThriftClientProxy clientProxy;
    private ICluster cluster;
    private ILoadBalancer loadBalancer;
    private boolean retryRequest = true;
    private int retryTimes = 3;
    private ThriftClientMetadata clientMetadata = null;
    private Map<Method, ThriftMethodHandler> methods;
    private final AtomicInteger sequenceId = new AtomicInteger(1);
    private static final AtomicLong nettySeqId = new AtomicLong(1);
    private IResponseCollector responseCollector = null;
    private StringBuilder invokeInfo = new StringBuilder();
    private final LinkedList<RhinoHandler> rhinoHandlerLinkedList;

    private static final String AOP_METHOD_INVOCATION = "AOP_METHOD_INVOCATION";
    private FilterHandler invokeHandler;

    public MTThriftMethodInterceptor(ThriftClientProxy clientProxy,
                                     ICluster cluster, int slowStartSeconds, ILoadBalancer loadBalancer, IResponseCollector responseCollector) {
        this.clientProxy = clientProxy;
        this.cluster = cluster;
        this.loadBalancer = loadBalancer;
        if (null == this.loadBalancer) {
            this.loadBalancer = new RandomLoadBalancer(slowStartSeconds);
        }
        this.responseCollector = responseCollector;
        this.rhinoHandlerLinkedList = new LinkedList<RhinoHandler>();
        rhinoHandlerLinkedList.add(new FaultInjectHandler(clientProxy.getAppKey(), clientProxy.getServiceName()));

        invokeHandler = InvokeChainBuilder.initInvokeChain4Invoker(this, clientProxy.getFilters());

        this.invokeInfo.append("|clientIP:").append(LocalPointConf.getAppIp())
                .append("|appkey:").append(clientProxy.getAppKey())
                .append("|env:").append(ProcessInfoUtil.getHostEnv())
                .append("|swimlane:").append(ProcessInfoUtil.getSwimlane())
                .append("|remoteAppkey:").append(clientProxy.getRemoteAppkey())
                .append("|remoteServerPort:").append(clientProxy.getRemoteServerPort())
                .append("|clusterManager:").append(clientProxy.getClusterManager())
                .append("|version:").append("mtthrift-v" + MtThriftManifest.getVersion());
    }

    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        if (clientProxy.isDestroyed()) {
            throw new TException("client has been destroyed, invalid invoke!");
        }
        RpcInvocation invocation = new RpcInvocation(clientProxy.getServiceInterface(), methodInvocation.getMethod(), methodInvocation.getArguments());
        invocation.putAttachment(AOP_METHOD_INVOCATION, methodInvocation);

        return invokeHandler.handle(invocation).getReturnVal();
    }

    public Object doInvoke(RpcInvocation invocation) throws Throwable {
        MethodInvocation methodInvocation = (MethodInvocation) invocation.getAttachment(AOP_METHOD_INVOCATION);

        Method method = methodInvocation.getMethod();
        String methodName = method.getName();
        Object[] args = methodInvocation.getArguments();

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return this.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return this.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return this.equals(args[0]);
        }

        traceStart(methodName);

        long begin = System.currentTimeMillis();

        ServerConn serverConn = null;
        Object socket = null;

        Object result = null;
        Throwable toThrow = null;

        List<ServerConn> connList = cluster.getServerConnList(new RouterMetaData(methodInvocation, method, args));// 是list对象为全局的，不可直接修改

        if (null == connList || connList.isEmpty()) {
            traceEndWithException();
            if (clientProxy.getRemoteAppkey() == null) {
                throw new TException("connection list is empty! cause by: remoteAppkey is null! " + invokeInfo.toString());
            } else {
                throw new TException("connection list is empty! " +
                        " ref:https://123.sankuai.com/km/page/28403325 " + invokeInfo.toString());
            }
        }

        int timeout = getTimeout(methodInvocation);
        int methodTimeoutRetryTimes = getTimeoutRetryTimes(methodInvocation);
        boolean retryFirst = true;
        /**
         * 重试逻辑：
         * 1 如果从连接池中取不到连接/ Server Socket 关闭/ Socket 被重置，重试下一目标服务器
         * 2 如果一次rpc调用发生SocketException，重试下一目标服务器进行RPC调用
         * 3 重试次数可以设置, 默认3次
         * 4 如果 retryRequest == false, 不做重试
         * 5 触发重试同时, 将发生异常的连接池从当前连接池列表中去掉, 且降低其全局范围内权重(后续请求可感知),
         *   以降低异常发生次数
         * 6 如果 methodTimeoutRetryTimes == 0, 不做方法粒度超时重试
         *
         */

        for (int i = 0, j = 0; connList.size() > 0 && i < this.retryTimes; ) {
            serverConn = loadBalancer.select(connList, methodInvocation);
            Server server = serverConn.getServer();

            RhinoContext rhinoContext = null;
            if (rhinoHandlerLinkedList != null && !rhinoHandlerLinkedList.isEmpty()) {
                rhinoContext = prepareRhinoContext(methodInvocation, server, timeout);

                for (RhinoHandler rh : rhinoHandlerLinkedList) {
                    try {
                        rh.handle(rhinoContext);
                    } catch (Exception e) {
                        String exceptionName = e.getClass().getName();
                        String message = "RhinoHandler Exception " + exceptionName + " while invoke(" + methodName + "), " + invokeInfo;
                        traceEndWithException();
                        throw new TException(message, e);
                    }
                }

                try {
                    rhinoContext.throwExceptionIfPresented();
                } catch (Exception e) {
                    String exceptionName = e.getClass().getName();
                    String message = "Rhino Exception " + exceptionName + " while invoke(" + methodName + "), " + invokeInfo;
                    traceEndWithException();
                    throw new TException(message, e);
                }

                timeout = rhinoContext.getCurrentTimeout();
            }

            reportTraceInfo(server, timeout);
            requestSign(methodInvocation, begin);

            long before = System.currentTimeMillis();
            socket = getConnection(serverConn, clientProxy, methodName, timeout);
            long after = System.currentTimeMillis();
            long cost = after - before;
            Tracer.getClientTracer().info("getConnection", String.valueOf(cost), after);

            if (socket == null) {
                connList = removeConn(connList, serverConn, retryFirst);
                retryFirst = false;
                serverConn.getServer().addSocketNullNum();
                //连续三次则降级
                if (serverConn.getServer().getSocketNullNum() >= 3) {
                    serverConn.getServer().resetSocketNullNum();
                    serverConn.getServer().degrade();
                    cluster.updateServerConn(serverConn);
                }
            } else {
                //节点正常后, socketNullNum置0
                serverConn.getServer().resetSocketNullNum();

                Entry<Object, Throwable> rpcResult = this.getRpcResult(methodInvocation, serverConn, socket, begin, timeout, rhinoContext);

                if (rpcResult != null) {
                    result = rpcResult.getT1();
                    toThrow = rpcResult.getT2();
                    if (result == null && toThrow == null) {
                        //RPC接口返回值为void
                        return null;
                    } else if (result != null || toThrow == null) {
                        if (responseCollector != null)
                            responseCollector.collect(methodInvocation, result);
                        return result;
                    } else if (toThrow.getCause() != null && toThrow.getCause() instanceof TTransportException
                            && ((((TTransportException) toThrow.getCause()).getType() == TTransportException.END_OF_FILE) || (toThrow.getCause().getCause() != null
                            && toThrow.getCause().getCause() instanceof SocketException))) {
                        LOG.info(methodName + " invoke " + toThrow.getCause() + ", retry it");
                        if (!retryRequest) {
                            break;
                        }
                        connList = removeConn(connList, serverConn, retryFirst);
                        retryFirst = false;
                        i++;
                        traceStart(methodName);
                        continue;
                    } else if (toThrow.getMessage() != null && toThrow.getMessage().contains("method timeout")
                            && j < methodTimeoutRetryTimes && !clientProxy.isAsync()) {//方法超时的判断条件,只在同步调用时有效
                        j++;
                        LOG.info(methodName + " invoke timeout, Number of remaining retries is " + (methodTimeoutRetryTimes - j));
                        traceStart(methodName);
                        continue;
                    } else {
                        throw toThrow;
                    }
                }
            }
        }

        if (socket == null) {
            String serverIpPort = "unknown";
            if (serverConn != null && serverConn.getServer() != null) {
                Server server = serverConn.getServer();
                serverIpPort = server.getIp() + ":" + server.getPort();
            }
            throw new TException("can't get valid connection(" + serverIpPort + ") to invoke " + methodName + invokeInfo);
        } else if (toThrow != null) {
            throw toThrow;
        } else {
            throw new TException("thrift rpc unknown Exception");
        }
    }

    private RhinoContext prepareRhinoContext(MethodInvocation methodInvocation, Server remoteServer, int timeout) {
        RhinoContext rhinoContext = new RhinoContext();
        rhinoContext.setLocalAppkey(clientProxy.getAppKey());
        rhinoContext.setRemoteAppkey(clientProxy.getRemoteAppkey());
        rhinoContext.setServiceInterface(clientProxy.getServiceInterface());
        rhinoContext.setMethodInvocation(methodInvocation);
        rhinoContext.setRemoteServer(remoteServer);
        rhinoContext.setOriginalTimeout(timeout);
        rhinoContext.setCurrentTimeout(timeout);
        rhinoContext.setAsync(clientProxy.isAsync());

        return rhinoContext;
    }

    private List<ServerConn> removeConn(List<ServerConn> connAll, ServerConn delete, boolean retryFirst) {
        if (retryFirst) {// 第一次修改时，拷贝全局的connlist
            List<ServerConn> remainConns = new ArrayList<ServerConn>();
            for (ServerConn conn : connAll) {
                if (conn != delete) {
                    remainConns.add(conn);
                }
            }
            return remainConns;
        } else {
            connAll.remove(delete);
            return connAll;
        }
    }

    public Entry<Object, Throwable> getRpcResult(MethodInvocation methodInvocation, ServerConn serverConn,
                                                 Object socket, long begin, int timeout, RhinoContext rhinoContext) throws Throwable {
        Entry<Object, Throwable> rpcResult;
        if (clientProxy.isAsync()) {
            rpcResult = asyncRpcInvoke(methodInvocation, serverConn, socket, begin, timeout);
        } else {
            rpcResult = syncRpcInvoke(methodInvocation, serverConn, socket, begin, timeout, rhinoContext);
        }
        return rpcResult;
    }

    private Entry<Object, Throwable> syncRpcInvoke(MethodInvocation methodInvocation, ServerConn serverConn,
                                                   Object socket, long begin, int timeout, RhinoContext rhinoContext) throws Throwable {

        Server server = serverConn.getServer();
        String serverIpPort = server.getIp() + ":" + server.getPort();
        String methodName = methodInvocation.getMethod().getName();

        Throwable toThrow = null;
        Object o = null;
        Transaction transaction = null;
        CustomizedTFramedTransport transport = null;
        RpcRequest request = null;
        RpcResponse response = null;

        MtThrfitInvokeInfo mtThrfitInvokeInfo = new MtThrfitInvokeInfo(
                serverConn.getServer().getServerAppKey(),
                clientProxy.getServiceSimpleName() + "." + methodName,
                LocalPointConf.getAppIp(), 0,
                serverConn.getServer().getIp(),
                serverConn.getServer().getPort(),
                serverConn.getServer().isUnifiedProto());

        try {

            transaction = execCatMonitor(mtThrfitInvokeInfo, clientProxy, server, timeout);
            if (socket instanceof IChannel) {
                //Netty IO 必须使用统一协议
                mtThrfitInvokeInfo.setUniProto(true);
                request = this.assembleRequest(methodInvocation, serverConn, timeout);

                ContextStore.putRequestIfAbsent(request.getSeq(), request);
                NettyTSocket tSocket = new NettyTSocket((NettyChannel) socket, request);
                transport = new CustomizedTFramedTransport(tSocket, request.getSeq(), clientProxy.getMaxResponseMessageBytes(), timeout);

                if (clientProxy.isGeneric() || clientProxy.getAnnotatedThrift()) {
                    TProtocol protocol = clientProxy.getProtocol(transport, mtThrfitInvokeInfo);
                    ThriftMethodHandler methodHandler = methods.get(methodInvocation.getMethod());
                    request.setThriftMethodHandler(methodHandler);
                    request.setAnnoSeq(sequenceId.getAndIncrement());
                    methodHandler.send(protocol, request.getAnnoSeq(), request.getParameters());
                } else {
                    Object service = clientProxy.getClientInstance(transport, mtThrfitInvokeInfo);
                    Method method = getSendMethod(service.getClass(), methodName, request.getParameterTypes());
                    method.invoke(service, methodInvocation.getArguments());
                }

                request.setRequestSize(transport.getRequestSize());
                response = this.waitForResponse(request.getSeq(), timeout);
                o = this.readResponse(response);
            } else {
                TSocket tSocket = ((TSocket) socket);
                transport = new CustomizedTFramedTransport(tSocket, clientProxy.getMaxResponseMessageBytes(), timeout);
                tSocket.setTimeout(timeout);
                if (clientProxy.isGeneric() || clientProxy.getAnnotatedThrift()) {
                    TProtocol protocol = clientProxy.getProtocol(transport, mtThrfitInvokeInfo);
                    o = methods.get(methodInvocation.getMethod()).invoke(protocol, protocol,
                            sequenceId.getAndIncrement(), methodInvocation.getArguments());
                } else {
                    Object service = clientProxy.getClientInstance(transport, mtThrfitInvokeInfo);
                    o = methodInvocation.getMethod().invoke(service, methodInvocation.getArguments());
                }

            }
            this.logCallSize(request, response, transport);

            if (transaction != null) {
                transaction.setStatus(Transaction.SUCCESS);
            }
            Tracer.getClientTracer().setStatus(Tracer.STATUS.SUCCESS);

        } catch (Exception e) {
            this.logCallSize(request, response, transport);

            Throwable cause = (e.getCause() == null) ? e : e.getCause();

            boolean userDefinedException = false;
            if (socket != null) {
                if (cause != null && cause instanceof TApplicationException) {
                    int type = ((TApplicationException) cause).getType();
                    if (type == TApplicationException.MISSING_RESULT
                            || type == TApplicationException.INTERNAL_ERROR
                            || type == TApplicationException.PROTOCOL_ERROR
                            || type == TApplicationException.UNKNOWN_METHOD
                            || type == 10001) {
                        //应用层异常归还链接
                        returnConnection(serverConn, socket);
                        socket = null;
                    }
                }
                if (cause instanceof TBase) {
                    //自定义异常
                    userDefinedException = true;
                    returnConnection(serverConn, socket);
                    socket = null;
                }
                if (socket != null) {
                    returnBrokenConnection(serverConn, socket);
                    socket = null;
                }
            }

            String timeoutString = "" + timeout;
            if (rhinoContext != null && rhinoContext.isDelay()) {
                timeoutString = timeoutString + " delayed(" + rhinoContext.getDelayTime() + ")";
            }

            toThrow = this.wrapException(cause, serverConn, methodName, transaction, begin, timeoutString, userDefinedException);
        } finally {
            if (socket != null && socket instanceof TSocket) {
                returnConnection(serverConn, socket);
            }

            clientProxy.noticeInvoke(methodName, serverIpPort, System.currentTimeMillis() - begin);
            if (transaction != null) {
                transaction.complete();
            }
            Tracer.getClientTracer().flush();

            if (request != null) {
                Long seq = request.getSeq();
                ContextStore.getRequestMap().remove(seq);
                if (!request.isAsync()) {
                    ContextStore.getResponseMap().remove(seq);
                }
            }
        }
        return new Entry<Object, Throwable>(o, toThrow);
    }

    private Method getSendMethod(Class<?> clazz, String methodName, Class[] parameterTyps) throws NoSuchMethodException {
        String className = clazz.getName();
        String methodKey = className + "#" + methodName;
        Method method = cachedMethod.get(methodKey);
        if (method == null) {
            method = clazz.getMethod("send_" + methodName, parameterTyps);
            cachedMethod.put(methodKey, method);
        }
        return method;
    }

    private Transaction execCatMonitor(MtThrfitInvokeInfo mtThrfitInvokeInfo, ThriftClientProxy clientProxy, Server server, int timeout) {
        Transaction transaction = null;
        if (ThriftClientGlobalConfig.isEnableCat()) {
            String svcIdentification = mtThrfitInvokeInfo.getSvcIdentification();

            if (clientProxy.isGeneric()) {
                svcIdentification += "[generic]";
            }

            transaction = Cat.newTransaction("OctoCall", svcIdentification);
            Cat.logEvent("OctoCall.appkey", mtThrfitInvokeInfo.getServerAppKey());
            Cat.logEvent("OctoCall.serverIp", mtThrfitInvokeInfo.getServerIp());
            Cat.logEvent("OctoCall.generic", String.valueOf(clientProxy.isGeneric()));

            if (clientProxy.isAsync()) {
                Cat.logEvent("OctoCall.callType", "async");
            } else {
                Cat.logEvent("OctoCall.callType", "sync");
            }

            if (clientProxy.isNettyIO() && server.isNettyIOSupported()) {
                Cat.logEvent("OctoCall.IOType", "netty");
            } else {
                Cat.logEvent("OctoCall.IOType", "thrift");
            }

            if (loadBalancer instanceof RandomLoadBalancer) {
                Cat.logEvent("OctoCall.routeType", "default");
            } else {
                Cat.logEvent("OctoCall.routeType", "user-defined");
            }

            if (mtThrfitInvokeInfo.isUniProto()) {
                Cat.logEvent("OctoCall.protocolType", "unified");
            } else {
                Cat.logEvent("OctoCall.protocolType", "old");
            }

            if (clientProxy.getAnnotatedThrift()) {
                Cat.logEvent("OctoCall.thriftType", "annotation");
            } else {
                Cat.logEvent("OctoCall.thriftType", "idl");
            }

            Cat.logEvent("OctoCall.timeout", String.valueOf(timeout));
        }
        return transaction;
    }

    private RpcRequest assembleRequest(MethodInvocation methodInvocation, ServerConn serverConn, int timeout) {
        RpcRequest request = new RpcRequest();
        request.setServiceInterface(clientProxy.getServiceInterface());
        request.setMethodName(methodInvocation.getMethod().getName());
        request.setParameters(methodInvocation.getArguments());
        request.setParameterTypes(methodInvocation.getMethod().getParameterTypes());
        request.setTimeoutMillis(timeout);
        request.setSeq(nettySeqId.getAndIncrement());
        request.setAsync(clientProxy.isAsync());
        request.setUniProto(serverConn.getServer().isUnifiedProto());
        return request;
    }

    private RpcResponse waitForResponse(long messageId, int timeout) throws InterruptedException {
        RpcResponse response = null;
        ContextStore.getResponseMap().putIfAbsent(messageId, new LinkedBlockingQueue<RpcResponse>(1));
        try {
            BlockingQueue<RpcResponse> queue = ContextStore.getResponseMap().get(messageId);
            if (queue != null) {
                response = queue.poll(timeout, TimeUnit.MILLISECONDS);
            }
        } finally {
            ContextStore.getResponseMap().remove(messageId);
        }
        return response;
    }

    private Object readResponse(RpcResponse response) throws Exception {
        Object result = null;
        if (response != null) {
            if (response.getException() != null) {
                throw response.getException();
            } else {
                result = response.getReturnVal();
            }
        } else {
            throw new RequestTimeoutException();
        }
        return result;
    }

    private void logCallSize(RpcRequest request, RpcResponse response, CustomizedTFramedTransport transport) {
        if (request != null && request.getRequestSize() > 0) {
            Cat.logEvent("OctoCall.requestSize", SizeUtil.getLogSize(request.getRequestSize()));
        } else if (transport != null) {
            Cat.logEvent("OctoCall.requestSize", SizeUtil.getLogSize(transport.getRequestSize()));
        }

        if (response != null && response.getResponseSize() > 0) {
            Cat.logEvent("OctoCall.responseSize", SizeUtil.getLogSize(response.getResponseSize()));
        } else if (transport != null) {
            Cat.logEvent("OctoCall.responseSize", SizeUtil.getLogSize(transport.getResponseSize()));
        }
    }

    private Throwable wrapException(Throwable cause, ServerConn serverConn, String methodName, Transaction transaction,
                                    long begin, String timeout, boolean userDefinedException) {

        boolean returnNull = false;
        boolean timeoutException = false;
        boolean serviceDegradeException = false;
        Throwable toThrow = null;
        String serverIpPort = serverConn.getServer().getIp() + ":" + serverConn.getServer().getPort();

        if (cause != null && cause instanceof TApplicationException
                && ((TApplicationException) cause).getType()
                == TApplicationException.MISSING_RESULT) {
            // server return null
            returnNull = true;
        } else {
            //前一个逻辑判断IDL方式（IDL方式把异常包装成InvocationTargetException抛出），后一个逻辑判断注解方式（注解直接抛出异常）
            if ((cause != null && cause instanceof TTransportException && cause.getCause() != null
                    && cause.getCause() instanceof SocketTimeoutException)
                    || (cause != null && cause instanceof SocketTimeoutException)
                    || (cause != null && cause instanceof RequestTimeoutException)) {
                // 接口响应超时
                if (clientProxy.isDisableTimeoutStackTrace()) {
                    toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke("
                            + methodName + ") method timeout, traceId:" +
                            ClientInfoUtil.getClientTracerTraceId() + ", timeout:" + timeout, null);
                    toThrow.setStackTrace(new StackTraceElement[0]);
                } else {
                    toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke("
                            + methodName + ") method timeout, traceId:" +
                            ClientInfoUtil.getClientTracerTraceId() + ", timeout:" + timeout, cause);
                }
                timeoutException = true;
            } else {
                if (cause != null && cause.getMessage() != null && cause.getMessage().contains("ServiceDegradeException")) {
                    serviceDegradeException = true;
                }
                if (cause != null && ((cause instanceof org.apache.thrift.TBase) || (cause instanceof TProtocolException))) {
                    // 1：org.apache.thrift.TBase
                    // 服务端主动抛出的异常；2：TApplicationException.type=10001
                    // mtthrift抛出的异常; 3:TProtocolException
                    toThrow = cause;
                } else if (cause != null && (cause instanceof TApplicationException)) {
                    toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke("
                            + methodName + "), traceId:" + ClientInfoUtil.getClientTracerTraceId()
                            + " Exception:" + cause.getMessage(), cause);
                } else {
                    toThrow = new TException("mtthrift remote(" + serverIpPort + ") invoke("
                            + methodName + ") Exception", cause);
                }
            }
        }

        ITracer clientTracer = Tracer.getClientTracer();
        if (returnNull || userDefinedException) {
            clientTracer.setStatus(Tracer.STATUS.SUCCESS);
            if (transaction != null) {
                transaction.setStatus(Transaction.SUCCESS);
            }
        } else {
            if (transaction != null) {
                Throwable throwable = toThrow != null ? toThrow : cause;
                transaction.setStatus(throwable);
                Cat.logErrorWithCategory("OctoCall." + clientProxy.getRemoteAppkey(), throwable);
            }
            if (serviceDegradeException) {
                clientTracer.setStatus(Tracer.STATUS.DROP);
            } else if (timeoutException) {
                clientTracer.setStatus(Tracer.STATUS.TIMEOUT);
            } else {
                clientTracer.setStatus(Tracer.STATUS.EXCEPTION);
            }
        }
        return toThrow;
    }

    private Entry<Object, Throwable> asyncRpcInvoke(MethodInvocation methodInvocation, ServerConn serverConn,
                                                    Object socket, long begin, int timeout) throws Throwable {

        Throwable toThrow = null;
        Object o = null;
        String methodName = methodInvocation.getMethod().getName();
        RpcRequest request;
        MTThriftAsyncMethodCallback callback = null;
        Server server = serverConn.getServer();
        MtThrfitInvokeInfo mtThrfitInvokeInfo = new MtThrfitInvokeInfo(
                serverConn.getServer().getServerAppKey(),
                clientProxy.getServiceSimpleName() + "." + methodName,
                LocalPointConf.getAppIp(), 0,
                serverConn.getServer().getIp(),
                serverConn.getServer().getPort(),
                server.isUnifiedProto());
        ForkableTransaction forkableTransaction = null;
        try {
            MtThrfitInvokeInfo.setMtThrfitInvokeInfo(mtThrfitInvokeInfo);
            if (socket instanceof IChannel) {
                //Netty IO 必须使用统一协议
                mtThrfitInvokeInfo.setUniProto(true);
                request = this.assembleRequest(methodInvocation, serverConn, timeout);
                request.setStartMillis(begin);
                Object[] args = request.getParameters();
                Object originalCallback;
                if (clientProxy.getAnnotatedThrift()) {
                    originalCallback = ContextStore.getCallback();
                } else {
                    originalCallback = args[args.length - 1];
                }

                if (args != null && args.length > 0) {
                    Transaction parentTransaction = Cat.getManager().getPeekTransaction();
                    if (parentTransaction != null) {
                        forkableTransaction = parentTransaction.forFork();
                    }
                    callback = new MTThriftAsyncMethodCallback((AsyncMethodCallback) originalCallback,
                            this,
                            serverConn,
                            null,
                            methodName,
                            mtThrfitInvokeInfo,
                            forkableTransaction);
                    callback.setTimeout(timeout);
                    callback.setNettyIO(true);

                    if (loadBalancer instanceof RandomLoadBalancer) {
                        callback.setRouteType("default");
                    } else {
                        callback.setRouteType("user-defined");
                    }

                    if (serverConn.getServer().isUnifiedProto()) {
                        callback.setProtocolType("unified");
                    } else {
                        callback.setProtocolType("old");
                    }
                    if (!clientProxy.getAnnotatedThrift()) {
                        callback.setThriftType("idl");
                        args[args.length - 1] = callback;
                    } else {
                        callback.setThriftType("annotation");
                    }

                    request.setCallback(callback);
                }

                ContextStore.putRequestIfAbsent(request.getSeq(), request);
                request.setFuture(SettableFuture.create());
                ContextStore.setFuture(request.getFuture());
                NettyTSocket tSocket = new NettyTSocket((NettyChannel) socket, request);
                CustomizedTFramedTransport transport = new CustomizedTFramedTransport(tSocket, request.getSeq(), clientProxy.getMaxResponseMessageBytes());

                if (clientProxy.getAnnotatedThrift()) {
                    TProtocol protocol = clientProxy.getProtocol(transport, mtThrfitInvokeInfo);
                    ThriftMethodHandler methodHandler = methods.get(methodInvocation.getMethod());
                    request.setThriftMethodHandler(methodHandler);
                    request.setAnnoSeq(sequenceId.getAndIncrement());
                    methodHandler.send(protocol, request.getAnnoSeq(), methodInvocation.getArguments());
                } else {
                    Object service = clientProxy.getClientInstance(transport, mtThrfitInvokeInfo);

                    Class[] argTypes = request.getParameterTypes();
                    Object[] newArgs = Arrays.copyOf(args, args.length - 1);
                    Class[] newArgTypes = Arrays.copyOf(argTypes, argTypes.length - 1);

                    Method method = getSendMethod(service.getClass(), methodName, newArgTypes);
                    method.invoke(service, newArgs);
                }
                request.setRequestSize(transport.getRequestSize());
                o = ThriftUtil.getDefaultResult(methodInvocation);
            } else {
                TNonblockingSocket tNonblockingSocket = ((TNonblockingSocket) socket);
                Object service = clientProxy.getClientInstance(tNonblockingSocket, mtThrfitInvokeInfo);
                ((TAsyncClient) service).setTimeout(timeout);
                Object[] args = methodInvocation.getArguments();

                if (args != null && args.length > 0 && args[args.length - 1] instanceof AsyncMethodCallback) {
                    Transaction parentTransaction = Cat.getManager().getPeekTransaction();
                    if (parentTransaction != null) {
                        forkableTransaction = parentTransaction.forFork();
                    }
                    AsyncMethodCallback asyncMethodCallback = (AsyncMethodCallback) args[args.length - 1];
                    callback = new MTThriftAsyncMethodCallback(asyncMethodCallback,
                            this,
                            serverConn,
                            tNonblockingSocket,
                            methodName,
                            mtThrfitInvokeInfo,
                            forkableTransaction);
                    callback.setTimeout(timeout);
                    if (loadBalancer instanceof RandomLoadBalancer) {
                        callback.setRouteType("default");
                    } else {
                        callback.setRouteType("user-defined");
                    }

                    if (serverConn.getServer().isUnifiedProto()) {
                        callback.setProtocolType("unified");
                    } else {
                        callback.setProtocolType("old");
                    }

                    callback.setThriftType("idl");
                    args[args.length - 1] = callback;
                }
                o = methodInvocation.getMethod().invoke(service, args);
            }
            if (callback != null) {
                callback.setClientSpan(Tracer.getClientSpan());
                Tracer.getClientTracer().clearCurrentSpan();
            }
            return new Entry<Object, Throwable>(o, null);
        } catch (Exception e) {
            toThrow = e.getCause();
            if (socket != null) {
                returnBrokenConnection(serverConn, socket);
            }
        } finally {
            ContextStore.removeCallback();
            if (forkableTransaction != null) {
                forkableTransaction.complete();
            }
        }
        return new Entry<Object, Throwable>(o, toThrow);
    }

    void noticeInvoke(String methodName, String serverIpPort, long takesMills) {
        clientProxy.noticeInvoke(methodName, serverIpPort, takesMills);
    }

    /**
     * @param serverConn
     * @param methodName
     * @return thrift IO方式中同步时是TSocket，异步时是TNonblockingSocket；netty IO方式中是NettyChannel
     */
    private Object getConnection(ServerConn serverConn, ThriftClientProxy clientProxy, String methodName, int timeout) {
        Object socket = null;
        ObjectPool socketPool = null;
        IChannelPool channelPool = null;
        Server server = serverConn.getServer();
        String serverIpPort = null;
        long start = System.currentTimeMillis();
        long takes = 0;
        String message = null;
        Exception exception = null;

        try {
            serverIpPort = server.getIp() + ":" + server.getPort();
            if (clientProxy.isNettyIO() && server.isNettyIOSupported()) {
                channelPool = serverConn.getChannelPool();
                if (channelPool == null) {
                    return null;
                }
                socket = channelPool.selectChannel();
            } else {
                if (clientProxy.getAnnotatedThrift() && clientProxy.isAsync()) {
                    throw new RuntimeException("async call with annotated thrift is only supported in nettyIO!");
                }
                socketPool = serverConn.getObjectPool();
                if (socketPool == null) {// 连接池被销毁时，此处为null
                    return null;
                }
                socket = socketPool.borrowObject();
            }
            takes = System.currentTimeMillis() - start;
            if (takes > timeout / 3) {
                message = "Get Connection from " + serverIpPort + " Timeout! Time:" + takes + ",actives:"
                        + socketPool.getNumActive() + ",idle:" + socketPool.getNumIdle();
                exception = new RuntimeException(message);
            }
            return socket;
        } catch (RuntimeException e) {
            if (e instanceof ChannelPoolException) {
                LOG.error("cannot get writable channel(" + serverIpPort + ") to invoke " + methodName + invokeInfo, e);
            }
            if (e.getCause() instanceof TTransportException) {// 连接不上
                if (socket != null) {
                    returnBrokenConnection(serverConn, socket);
                    socket = null;
                }
            }
            takes = System.currentTimeMillis() - start;
            if (socketPool != null) {
                message = "Get Connection from " + serverIpPort + " Exception! Time:" + takes + ",actives:"
                        + socketPool.getNumActive() + ",idle:" + socketPool.getNumIdle();
            } else if (channelPool != null) {
                message = "Get Connection from " + serverIpPort + " Exception! Time:" + takes + ",size:"
                        + channelPool.getSize();
            }
            exception = e;
        } catch (Exception e) {
            takes = System.currentTimeMillis() - start;
            if (socketPool != null) {
                message = "Get Connection from " + serverIpPort + " Exception! Time:" + takes + ",actives:"
                        + socketPool.getNumActive() + ",idle:" + socketPool.getNumIdle();
            } else if (channelPool != null) {
                message = "Get Connection from " + serverIpPort + " Exception! Time:" + takes + ",size:"
                        + channelPool.getSize();
            }
            exception = e;
        } finally {
//            if (message != null && exception != null) {
//                Transaction transaction = Cat.newTransaction("OctoSelfCheck", "MTThriftMethodInterceptor.getConnection");
//                transaction.addData("timeout=", timeout);
//                transaction.addData("methodName=", methodName);
//                transaction.addData("invokeInfo=", invokeInfo);
//                transaction.setStatus(message);
//                Cat.logError(exception);
//                transaction.complete();
//                if (transaction instanceof DefaultTransaction)
//                    ((DefaultTransaction)transaction).setDurationInMillis(takes);
//            }
        }
        return socket;
    }

    void returnConnection(ServerConn serverConn, Object socket) {
        try {
            if (socket == null || serverConn == null
                    || serverConn.getObjectPool() == null) {
                return;
            }
            serverConn.getObjectPool().returnObject(socket);
        } catch (Exception e) {
            throw new RuntimeException("error returnBrokenConnection()", e);
        }
    }

    void returnBrokenConnection(ServerConn serverConn, Object socket) {
        try {
            if (socket == null || serverConn == null
                    || serverConn.getObjectPool() == null) {
                return;
            }
            serverConn.getObjectPool().invalidateObject(socket);
        } catch (Exception e) {
            throw new RuntimeException("error returnBrokenConnection()", e);
        }
    }

    public boolean isRetryRequest() {
        return retryRequest;
    }

    public void setRetryRequest(boolean retryRequest) {
        this.retryRequest = retryRequest;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void setClientMetadata(
            MTThriftInvocationHandler.ThriftClientMetadata clientMetadata) {
        this.clientMetadata = clientMetadata;
        this.methods = clientMetadata.getMethodHandlers();
    }

    public void requestSign(MethodInvocation methodInvocation, long begin) {
        ISignHandler signHandler = clientProxy.getSignHandler();
        if (ThriftClientGlobalConfig.isEnableAuth() && signHandler != null) {
            SignMetaData signMetaData = new SignMetaData();
            signMetaData.setAppkey(clientProxy.getAppKey());
            signMetaData.setRemoteAppkey(clientProxy.getRemoteAppkey());
            signMetaData.setLocalIp(ProcessInfoUtil.getLocalIpV4());
            signMetaData.setMethodName(methodInvocation.getMethod().getName());
            signMetaData.setArgs(methodInvocation.getArguments());
            signMetaData.setTimestamp(begin);
            signHandler.sign(signMetaData);
        }
    }

    public void afterPropertiesSet() {
        // do something
    }

    private void traceStart(String methodName) {
        if (ThriftClientGlobalConfig.isEnableMtrace()) {
            ITracer clientTracer = Tracer.getClientTracer();
            clientTracer.record(clientProxy.getServiceSimpleName() + "." + methodName);
        }
    }

    private void traceEndWithException() {
        if (ThriftClientGlobalConfig.isEnableMtrace()) {
            ITracer clientTracer = Tracer.getClientTracer();
            clientTracer.setStatus(Tracer.STATUS.EXCEPTION);
            if (clientProxy.isAsync()) {
                clientTracer.flushAsync(clientTracer.getSpan());
            } else {
                clientTracer.flush();
            }
        }
    }

    private void reportTraceInfo(Server server, long timeout) {
        if (ThriftClientGlobalConfig.isEnableMtrace()) {
            if (clientProxy.isAsync()) {
                reportTraceInfoAsync(server);
            } else {
                reportTraceInfoSync(server);
            }
            Tracer.getClientTracer().putRemoteOneStepContext(Consts.REQUEST_TIMEOUT, String.valueOf(timeout));
        }
    }

    private void reportTraceInfoSync(Server server) {
        ITracer clientTracer = Tracer.getClientTracer();
        clientTracer.setLocalAppKey(clientProxy.getAppKey());
        clientTracer.setLocalIp(LocalPointConf.getAppIp());
        clientTracer.setRemoteAppKey(clientProxy.getRemoteAppkey());
        clientTracer.setRemoteIp(server.getIp());
        clientTracer.setRemotePort(server.getPort());
        clientTracer.setInfraName(Consts.mtraceInfra);
        clientTracer.setInfraVersion(MtThriftManifest.getVersion());
    }

    private void reportTraceInfoAsync(Server server) {
        Span span = Tracer.getClientTracer().getSpan();
        if(span != null) {
            span.setLocalAppKey(clientProxy.getAppKey());
            span.setLocalIp(LocalPointConf.getAppIp());
            span.setRemote(clientProxy.getRemoteAppkey(), server.getIp(), server.getPort());
            span.setInfraName(Consts.mtraceInfra);
            span.setVersion(MtThriftManifest.getVersion());
        }
    }

    public int getTimeout(MethodInvocation methodInvocation) {
        int timeout = clientProxy.getTimeout();
        ITimeoutPolicy timeoutPolicy = clientProxy.getTimeoutPolicy();
        if (null != timeoutPolicy) {
            int timeoutInPolicy = timeoutPolicy.getTimeoutByConfig(methodInvocation, clientProxy.getTimeout());
            if (timeoutInPolicy > 0)
                timeout = timeoutInPolicy;
        }
        return timeout;
    }

    public int getTimeoutRetryTimes(MethodInvocation methodInvocation) {
        IMethodTimeoutRetryPolicy methodTimeoutPolicy = clientProxy.getMethodTimeoutPolicy();
        if (null != methodTimeoutPolicy) {
            int methodRetryTimes = methodTimeoutPolicy.getMethodTimeoutTimes(methodInvocation);
            return methodRetryTimes > 0 ? methodRetryTimes : 0;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MTThriftMethodInterceptor that = (MTThriftMethodInterceptor) o;

        if (retryRequest != that.retryRequest)
            return false;
        if (retryTimes != that.retryTimes)
            return false;
        if (clientProxy != null ? !clientProxy.equals(that.clientProxy) : that.clientProxy != null)
            return false;
        if (cluster != null ? !cluster.equals(that.cluster) : that.cluster != null)
            return false;
        if (loadBalancer != null ? !loadBalancer.equals(that.loadBalancer) : that.loadBalancer != null)
            return false;
        if (clientMetadata != null ? !clientMetadata.equals(that.clientMetadata) : that.clientMetadata != null)
            return false;
        if (methods != null ? !methods.equals(that.methods) : that.methods != null)
            return false;
        if (sequenceId != null ? !sequenceId.equals(that.sequenceId) : that.sequenceId != null)
            return false;
        if (responseCollector != null ?
                !responseCollector.equals(that.responseCollector) :
                that.responseCollector != null)
            return false;
        return invokeInfo != null ? invokeInfo.equals(that.invokeInfo) : that.invokeInfo == null;
    }

    @Override
    public int hashCode() {
        int result = clientProxy != null ? clientProxy.hashCode() : 0;
        result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
        result = 31 * result + (loadBalancer != null ? loadBalancer.hashCode() : 0);
        result = 31 * result + (retryRequest ? 1 : 0);
        result = 31 * result + retryTimes;
        result = 31 * result + (clientMetadata != null ? clientMetadata.hashCode() : 0);
        result = 31 * result + (methods != null ? methods.hashCode() : 0);
        result = 31 * result + (sequenceId != null ? sequenceId.hashCode() : 0);
        result = 31 * result + (responseCollector != null ? responseCollector.hashCode() : 0);
        result = 31 * result + (invokeInfo != null ? invokeInfo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MTThriftMethodInterceptor{");
        sb.append("clientProxy=").append(clientProxy);
        sb.append(", cluster=").append(cluster);
        sb.append(", loadBalancer=").append(loadBalancer);
        sb.append(", retryRequest=").append(retryRequest);
        sb.append(", retryTimes=").append(retryTimes);
        sb.append(", clientMetadata=").append(clientMetadata);
        sb.append(", methods=").append(methods);
        sb.append(", sequenceId=").append(sequenceId);
        sb.append(", responseCollector=").append(responseCollector);
        sb.append(", invokeInfo=").append(invokeInfo);
        sb.append('}');
        return sb.toString();
    }
}
