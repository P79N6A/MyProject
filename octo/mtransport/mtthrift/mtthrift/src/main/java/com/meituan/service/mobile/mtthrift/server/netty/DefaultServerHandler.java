package com.meituan.service.mobile.mtthrift.server.netty;

import com.dianping.cat.Cat;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.auth.AuthUtil;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.generic.GenericServiceTProcessor;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.netty.metadata.RPCContext;
import com.meituan.service.mobile.mtthrift.netty.metadata.RequestType;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerInvoker;
import com.meituan.service.mobile.mtthrift.server.flow.FlowCopyTask;
import com.meituan.service.mobile.mtthrift.transport.CustomizedServerTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.ThreadLocalUtil;
import com.meituan.service.mobile.mtthrift.util.TraceInfoUtil;
import com.sankuai.octo.protocol.StatusCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.*;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static com.meituan.service.mobile.mtthrift.auth.AuthType.channelAuth;

public class DefaultServerHandler extends SimpleChannelInboundHandler<RPCContext> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServerHandler.class);

    private Map<String, TProcessor> serviceProcessorMap = new HashMap<String, TProcessor>();
    private Map<String, Class<?>> serviceInterfaceCache = new ConcurrentHashMap<String, Class<?>>();
    private Map<String, String> serviceSimpleNameCache = new ConcurrentHashMap<String, String>();
    private TProcessor processor = null;
    private Map<Class<?>, TProcessor> genericServiceProcessorMap = new ConcurrentHashMap<Class<?>, TProcessor>();
    private final TProtocolFactory protocolFactory;
    private NettyServer server;

    public DefaultServerHandler(Map<String, TProcessor> serviceProcessorMap, TProcessor processor, NettyServer server) {
        super(false);
        this.protocolFactory = new MtraceServerTBinaryProtocol.Factory(true, true, server.getServiceInterface(), server.getServiceSimpleName(), server.getLocalEndpoint());
        this.serviceProcessorMap = serviceProcessorMap;
        this.processor = processor;
        this.server = server;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final RPCContext context) throws Exception {

        if (RequestType.scannerHeartbeat.equals(context.getRequestType())) {
            handleHeartbeatRequest(ctx, context);
            return;
        }

        //处理鉴权异常
        if (!context.isAuthSuccess() && !ThriftServerGlobalConfig.isEnableGrayAuth()) {
            if (ThriftServerGlobalConfig.isEnableCat()) {
                String clientIP = getClientIP(ctx);
                String clientAppkey = getClientAppkey(context);

                Cat.logEvent("OctoAuth.fail.client.appkey", clientAppkey);
                Cat.logEvent("OctoAuth.fail.client.ip", clientIP);
            }
            handleException(ctx, context, protocolFactory, "AuthFailedException", StatusCode.SecurityException,
                    ThriftServerGlobalConfig.isEnableAuthErrorLog());
        } else {
            if (!context.isAuthSuccess() && ThriftServerGlobalConfig.isEnableGrayAuth()) {
                String clientIP = getClientIP(ctx);
                String clientAppkey = getClientAppkey(context);

                if (ThriftServerGlobalConfig.isEnableCat()) {
                    Cat.logEvent("OctoAuth.gray.client.appkey", clientAppkey);
                    Cat.logEvent("OctoAuth.gray.client.ip", clientIP);
                }

                String errorMessage = String.format("AuthFailedException: Client(%s:%s) invoke Server(%s)",
                        clientAppkey, clientIP, server.getAppKey());

                if (ThriftServerGlobalConfig.isEnableAuthErrorLog()) {
                    logger.error(errorMessage);
                }

                TraceInfoUtil.catRecordAuthFail(clientAppkey, null, clientIP, -1, channelAuth.name(), String.valueOf(context.isUnifiedProto()));
            }

            //处理请求
            byte[] thriftData = context.getThriftRequestData();
            TMemoryInputTransport in = new TMemoryInputTransport(thriftData);
            final TByteArrayOutputStream out = new TByteArrayOutputStream();
            final MtraceServerTBinaryProtocol inProtocol = (MtraceServerTBinaryProtocol) protocolFactory.getProtocol(in);
            final MtraceServerTBinaryProtocol outProtocol = (MtraceServerTBinaryProtocol) protocolFactory.getProtocol(new TIOStreamTransport(out));

            String serviceName = null;
            String serviceSimpleName = null;
            if (context.getHeader() != null) {
                inProtocol.setHeaderInfo(context.getHeader());
                serviceName = context.getHeader().getRequestInfo().getServiceName();
                Class<?> serviceInterface = serviceInterfaceCache.get(serviceName);
                serviceSimpleName = serviceSimpleNameCache.get(serviceName);
                if (serviceInterface == null) {
                    try {
                        serviceInterface = Class.forName(serviceName);
                        serviceSimpleName = serviceInterface.getSimpleName();
                    } catch (ClassNotFoundException e) {
                        String clientIP = getClientIP(ctx);
                        logger.error("exception while handleRequest serviceName({}) ClassNotFoundException, client IP: {}", serviceName, clientIP);
                        return;
                    }
                    serviceInterfaceCache.put(serviceName, serviceInterface);
                    serviceSimpleNameCache.put(serviceName, serviceSimpleName);
                }
                inProtocol.setServiceSimpleName(serviceSimpleName);
                inProtocol.setServiceInterface(serviceInterface);
            }

            final String finalServiceName = serviceName;
            final String finalServiceSimpleName = serviceSimpleName;

            TMessage message = inProtocol.readTMessage();
            final String methodName = message.name;

            Executor executor = server.getExecutor(serviceName, methodName);
            try {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        handleRequest(finalServiceName, finalServiceSimpleName, inProtocol,
                                outProtocol, out, ctx, context);
                    }
                });
            } catch (RejectedExecutionException exception) {
                handleException(ctx, context, protocolFactory, "RejectedExecutionException", StatusCode.RpcException, true);
            }

            //流量录制
            if (FlowCopyTask.isDoFlowCopy(context, finalServiceName, methodName)) {
                FlowCopyTask.copy(context, finalServiceName, methodName);
            }
        }
    }

    private void handleHeartbeatRequest(ChannelHandlerContext ctx, RPCContext context) throws Exception {
        //处理心跳
        encodeAndFlush(context);
        ctx.writeAndFlush(context);
    }

    private String getClientAppkey(RPCContext context) {
        String clientAppkey = "";
        if (context.getHeader() != null && context.getHeader().getLocalContext() != null) {
            clientAppkey = context.getHeader().getLocalContext().get(AuthUtil.APPKEY);
        }
        if (StringUtils.isBlank(clientAppkey)) {
            if (context.getHeader() != null && context.getHeader().getTraceInfo() != null) {
                clientAppkey = context.getHeader().getTraceInfo().getClientAppkey();
            }
        }

        if (StringUtils.isBlank(clientAppkey)) {
            clientAppkey = ClientInfoUtil.getClientAppKey();
        }
        return clientAppkey;
    }

    public void handleRequest(String serviceName, String serviceSimpleName, MtraceServerTBinaryProtocol inProtocol, MtraceServerTBinaryProtocol outProtocol,
                              TByteArrayOutputStream out, ChannelHandlerContext ctx, RPCContext context) {
        ThreadLocalUtil.clear();

        if (serviceSimpleName != null) {
            ThreadLocalUtil.setServiceSimpleName(serviceSimpleName);
        }
        if (serviceName != null) {
            ThreadLocalUtil.setServiceCompleteName(serviceName);
        }

        if (!context.isAuthSuccess()) {
            ThreadLocalUtil.setAuthGray(true);
        }

        ThreadLocalUtil.setIsUnifiedProto(String.valueOf(context.isUnifiedProto()));

        TProcessor actualProcessor = null;
        if (isGeneric(context) && serviceName != null) {
            Class<?> serviceInterface = serviceInterfaceCache.get(serviceName);
            GenericServiceTProcessor.genericTypeThreadLocal.remove();
            actualProcessor = genericServiceProcessorMap.get(serviceInterface);
            if (actualProcessor == null) {
                actualProcessor = new GenericServiceTProcessor(server, serviceInterface);
                genericServiceProcessorMap.put(serviceInterface, actualProcessor);
            }
            GenericServiceTProcessor.genericTypeThreadLocal.set(getGenericType(context));
        } else {
            if (serviceName != null) {
                actualProcessor = serviceProcessorMap.get(serviceName);
            }

            if (actualProcessor == null) {
                actualProcessor = processor;
            }
        }

        if (actualProcessor == null) {
            handleException(ctx, context, protocolFactory, "NoMatchProcessor(serviceName:" + serviceName +
                    "), check if client api path is corresponding with server", StatusCode.RpcException, true);
            return;
        }

        try {
            actualProcessor.process(inProtocol, outProtocol);
        } catch (Throwable e) {
            handleProcessException(e, serviceName, ctx, inProtocol, outProtocol);
        }
        response(out.toByteArray(), context, ctx, serviceName);
    }

    private boolean isGeneric(RPCContext context) {
        if (context.getHeader() == null) {
            return false;
        }

        if (context.getHeader().localContext == null) {
            return false;
        }

        return context.getHeader().localContext.containsKey(Consts.GENERIC_TAG);
    }

    private String getGenericType(RPCContext context) {
        return context.getHeader().localContext.get(Consts.GENERIC_TAG);
    }

    public void encodeAndFlush(RPCContext context) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Consts.DEFAULT_BYTEARRAY_SIZE);
        TIOStreamTransport transport = new TIOStreamTransport(out);
        CustomizedServerTFramedTransport tFramedTransport = new CustomizedServerTFramedTransport(transport);
        boolean unifiedProto = context.isUnifiedProto();
        if (unifiedProto) {
            tFramedTransport.setUnifiedProto(true);
            //TODO protocol设置
            tFramedTransport.setProtocol((byte) 0x01);
            tFramedTransport.setSequenceId(context.getSeq());
        } else {
            tFramedTransport.setUnifiedProto(false);
        }

        if (RequestType.scannerHeartbeat.equals(context.getRequestType())) {
            tFramedTransport.setHeartbeatInfo(context.getHeartbeatInfo());
            tFramedTransport.setWriteBuffer_(new TByteArrayOutputStream(1));
        } else {
            byte[] bytes = context.getThriftResponseData();
            TByteArrayOutputStream in = new TByteArrayOutputStream(Consts.DEFAULT_BYTEARRAY_SIZE);
            in.write(bytes);
            tFramedTransport.setWriteBuffer_(in);
        }
        tFramedTransport.setBeginTime(context.getRequestTime());
        tFramedTransport.setRequestSize(context.getRequestSize());
        tFramedTransport.flush();
        context.setResponseBytes(out.toByteArray());
    }

    public void handleException(ChannelHandlerContext ctx, RPCContext context, TProtocolFactory protocolFactory,
            String errorMessage, StatusCode statusCode, boolean logError) {

        byte[] thriftData = context.getThriftRequestData();
        TMemoryInputTransport in = new TMemoryInputTransport(thriftData);
        TByteArrayOutputStream out = new TByteArrayOutputStream();
        MtraceServerTBinaryProtocol inProtocol = (MtraceServerTBinaryProtocol) protocolFactory.getProtocol(in);
        MtraceServerTBinaryProtocol outProtocol = (MtraceServerTBinaryProtocol) protocolFactory.getProtocol(new TIOStreamTransport(out));

        try {
            TMessage msg = inProtocol.readMessageBegin();
            TProtocolUtil.skip(inProtocol, TType.STRUCT);
            inProtocol.readMessageEnd();

            if (logError) {
                logger.error(errorMessage);
            }

            ThriftServerInvoker.statusCode.set(statusCode);
            ThriftServerInvoker.errorMessage.set(errorMessage);

            TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, errorMessage);
            outProtocol.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
            x.write(outProtocol);
            outProtocol.writeMessageEnd();
            outProtocol.getTransport().flush();

            byte[] result = out.toByteArray();
            context.setThriftResponseData(result);

            encodeAndFlush(context);
            ctx.writeAndFlush(context);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ThriftServerInvoker.statusCode.remove();
            ThriftServerInvoker.errorMessage.remove();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String clientIP = getClientIP(ctx);
        String message = cause.getMessage();
        if (message != null && message.contains("Connection reset by peer")) {
            logger.warn("exceptionCaught(client IP:{}): {}", clientIP, cause.getMessage());
        } else {
            logger.error("exceptionCaught(client IP:{})", clientIP, cause);
        }
    }

    private void handleProcessException(Throwable e, String serviceName, ChannelHandlerContext ctx,
                                        MtraceServerTBinaryProtocol inProtocol, MtraceServerTBinaryProtocol outProtocol) {
        String clientIP = getClientIP(ctx);
        String eMessage = getExceptionMessage(e) + " traceId:" + Tracer.id();
        TMessage msg = null;
        try {
            msg = inProtocol.reReadMessageBegin();

            outProtocol.rewriteMessageBegin(msg.name, TMessageType.EXCEPTION, msg.seqid);
            if (e instanceof TBase && e instanceof TException) {
                // 自定义异常不应该抛到这里，避免错误，先保留此判断
                logger.error("exception while handleRequest(serviceName:{}, methodName:{}, clientIP:{}, traceId:{})", serviceName, msg.name, clientIP, Tracer.id(), e);
                TBase excp = (TBase) e;
                excp.write(outProtocol);
            } else if (e instanceof TTransportException) {
                logger.error("exception while handleRequest(serviceName:{}, methodName:{}, clientIP:{}, traceId:{})", serviceName, msg.name, clientIP, Tracer.id(), e);
                TApplicationException excp = new TApplicationException(TApplicationException.INTERNAL_ERROR, eMessage);
                excp.write(outProtocol);
            } else {
                // 日志在ThriftServerInvoke已输出
                TApplicationException excp = new TApplicationException(TApplicationException.INTERNAL_ERROR, eMessage);
                excp.write(outProtocol);
            }
            outProtocol.writeMessageEnd();
            outProtocol.getTransport().flush();
        } catch (TException exception) {
            logger.error("exception while handleProcessException(serviceName:{}, client IP:{})", serviceName, clientIP, exception);
        }
    }

    private void response(byte[] result, RPCContext context, ChannelHandlerContext ctx, String serviceName) {
        try {
            context.setThriftResponseData(result);
            encodeAndFlush(context);
            ctx.writeAndFlush(context);
        } catch (Exception e) {
            String clientIP = getClientIP(ctx);
            logger.error("exception while response(serviceName:{}, client IP:{})", serviceName, clientIP, e);
        }
    }

    private String getClientIP(ChannelHandlerContext ctx) {
        String ip;
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            ip = socketAddress.getAddress().getHostAddress();
        } catch (Exception e) {
            ip = "unknown";
        }
        return ip;
    }

    private String getExceptionMessage(Throwable e) {
        if (e instanceof TException && e.getCause() != null) {
            e = e.getCause();
        }
        StackTraceElement[] stacks = e.getStackTrace();
        if (stacks != null && stacks.length > 0) {
            StackTraceElement stackTraceElement = stacks[0];
            return e.getClass().getName() + (e.getMessage() == null ? "" : ":" + e.getMessage()) + "(" + stackTraceElement.getFileName() + "," + stackTraceElement.getMethodName() + "() line " + stackTraceElement.getLineNumber() + ")";
        } else {
            return e.getClass().getName() + (e.getMessage() == null ? "" : ":" + e.getMessage());
        }
    }
}
