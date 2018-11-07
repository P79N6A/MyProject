package com.meituan.service.mobile.mtthrift.server.flow;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.netty.channel.IChannelFactory;
import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannel;
import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannelFactory;
import com.meituan.service.mobile.mtthrift.netty.exception.NetworkException;
import com.meituan.service.mobile.mtthrift.netty.metadata.RPCContext;
import com.meituan.service.mobile.mtthrift.netty.metadata.RequestType;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerRepository;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.meituan.service.mobile.mtthrift.server.http.handler.check.ServiceIfaceInfo;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.HttpUtils;
import com.meituan.service.mobile.mtthrift.util.MtConfigUtil;
import com.meituan.service.mobile.mtthrift.util.SizeUtil;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.octo.protocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/22
 */
public class FlowCopyTask {
    private static final Logger logger = LoggerFactory.getLogger(FlowCopyTask.class);

    public static FlowCopyStatus flowCopyStatus = FlowCopyStatus.STOP;
    private static final int CONNECT_TIMEOUT = 2000;
    private static final int STATUS_CHECK_INTERVAL = 2000;
    private static final int MAX_RECONN_TIMES = 5;
    private static final String TASK_FINISHED = "Finished";
    private static final String STATUS_OK = "ok";
    private static final String FETCH_STATUS = "fetch/status/";
    private static final String CLOSE_COPY = "close/";
    private static String brokerUrlPrefix = "http://10.72.208.105:8080/api/record/";

    private static volatile boolean started;

    private static FlowCopyConfig flowCopyConfig;
    private static FlowCopyCfgDetail configDetail;
    private static InetSocketAddress remoteNodeAddress;
    private static AtomicInteger copyCount;
    private static ConcurrentMap<String, Boolean> oldProtoApi;

    private static IChannelFactory channelFactory;
    private static NettyChannel channel;
    private static NioEventLoopGroup nioWorkerGroup;
    private static AtomicInteger reconnFailTimes;
    private static ScheduledExecutorService statusCheck;
    private static volatile ExecutorService flowCopyThreadPool;

    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isDoFlowCopy(final RPCContext context, String serviceName, String methodName) {
        if (!started || !isFlowCopyServiceName(serviceName) || !isFlowCopyMethod(methodName) ||
                !RequestType.unifiedProto.equals(context.getRequestType()) || !isChannelConnected()) {
            if (started && isFlowCopyMethod(methodName) && !RequestType.unifiedProto.equals(context.getRequestType())) {
                // 方便排查非统一协议请求录制为0问题
                if (oldProtoApi == null) {
                    oldProtoApi = new ConcurrentHashMap<String, Boolean>();
                }
                oldProtoApi.putIfAbsent(methodName, true);
            }
            return false;
        }
        return true;
    }

    public static void copy(final RPCContext context, final String serviceName, final String methodName) {
        try {
            if (flowCopyThreadPool == null) {
                synchronized (FlowCopyTask.class) {
                    if (flowCopyThreadPool == null) {
                        flowCopyThreadPool = new ThreadPoolExecutor(1, 20, 30,
                                TimeUnit.SECONDS,
                                new SynchronousQueue<Runnable>(),
                                new MTDefaultThreadFactory("FlowCopyThreadPool"),
                                new ThreadPoolExecutor.DiscardPolicy());
                    }
                }
            }
            flowCopyThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    doCopy(context, serviceName, methodName);
                    if (copyCount == null) {
                        copyCount = new AtomicInteger();
                    }
                    copyCount.incrementAndGet();
                }
            });
        } catch (Throwable e) {
            catReportException(serviceName + "." + methodName, e);
        }
    }

    private static void doCopy(RPCContext context, String serviceName, String methodName) {
        Transaction transaction = null;
        try {
            rwLock.readLock().lock();
            if (!started || !isChannelConnected()) {
                return;
            }
            byte[] intactBytes = context.getIntactBytes();
            transaction = catReportPrepare(serviceName + "." + methodName, intactBytes.length);
            boolean tagged = configDetail.isTagged();
            if (tagged) {
                byte[] result = addTestTag(context);
                channel.write(result);
            } else {
                channel.write(intactBytes);
            }
            transaction.setStatus(Transaction.SUCCESS);
            transaction.complete();
        } catch (Throwable e) {
            logger.error("Do flow copy failed", e);
            if (transaction == null) {
                catReportException(serviceName + "." + methodName, e);
            } else {
                Cat.logErrorWithCategory("OctoFlowCopy." + serviceName + "." + methodName, e);
                transaction.setStatus(e);
                transaction.complete();
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private static void start(FlowCopyConfig flowCopyCfg) {
        long taskId = flowCopyCfg.getTaskId();
        Transaction transaction = null;
        try {
            rwLock.writeLock().lock();
            flowCopyConfig = flowCopyCfg;
            configDetail = flowCopyCfg.getCfgDetail();
            if (!isFlowCopyNode()) {
                logger.info("This is not flowCopy node, won't do flowCopy");
                flowCopyStatus = FlowCopyStatus.NOT_FLOW_COPY_NODE;
                started = false;
                return;
            }
            checkFlowCopyService();
            parseBrokerInfo();

            channelFactory = createChannelFactory();
            initStatusCheck();
            started = true;
            flowCopyStatus = FlowCopyStatus.START;
            logger.info("Start flow copy task, taskId={}", taskId);
            transaction = catReportPrepare("START", -1);
            channel = (NettyChannel) channelFactory.createChannel();
            logger.info("Succeed connect to the server {}, taskId={}", channel.getRemoteAddress(), taskId);
        } catch (NetworkException e) {
            logger.warn("Failed connect to the server {}, will retry, taskId={}", remoteNodeAddress, taskId);
            catReportException("connFail", e);
        } catch (Throwable e) {
            logger.warn("FlowCopy init failed, taskId={}, error: {}", taskId, e.getMessage());
            catReportException("initFail", e);
            stop(e);
        } finally {
            rwLock.writeLock().unlock();
            if (transaction != null) {
                transaction.setStatus(Transaction.SUCCESS);
                transaction.complete();
            }
        }
    }

    public static void stop() {
        stop(null);
    }

    private static void stop(Throwable exp) {
        long taskId = 0;
        if (flowCopyConfig != null) {
            taskId = flowCopyConfig.getTaskId();
        }
        Transaction transaction = null;
        try {
            rwLock.writeLock().lock();
            if (started) {
                transaction = catReportPrepare("STOP", -1);
                logger.info("Stop flow copy task, taskId={}", taskId);
            }
            started = false;
            if (flowCopyThreadPool != null) {
                flowCopyThreadPool.shutdownNow();
            }
            if (isChannelConnected()) {
                channel.disConnect();
            }
            if (nioWorkerGroup != null) {
                nioWorkerGroup.shutdownGracefully();
            }
            if (statusCheck != null) {
                statusCheck.shutdownNow();
            }
            if (exp == null) {
                flowCopyStatus = FlowCopyStatus.STOP;
            }
        } catch (Throwable e) {
            logger.error("FlowCopy stop failed, taskId={}", taskId, e);
            catReportException("stopFail", e);
        } finally {
            flowCopyThreadPool = null;
            statusCheck = null;
            channel = null;
            channelFactory = null;
            nioWorkerGroup = null;

            flowCopyConfig = null;
            configDetail = null;
            remoteNodeAddress = null;
            reconnFailTimes = null;
            copyCount = null;
            oldProtoApi = null;
            rwLock.writeLock().unlock();

            if (transaction != null) {
                transaction.setStatus(Transaction.SUCCESS);
                transaction.complete();
            }
        }
    }

    private static void restart(FlowCopyConfig flowCopyCfg) {
        logger.info("Restart flow copy task, taskId={}", flowCopyCfg.getTaskId());
        stop();
        start(flowCopyCfg);
    }

    public static void initFlowCopyCfg(String flowCopyCfgStr) {
        if (StringUtils.isBlank(flowCopyCfgStr)) {
            return;
        }
        try {
            FlowCopyConfig flowCopyCfg = getFlowCopyConfig(flowCopyCfgStr);
            if (flowCopyCfg.isEnable()) {
                start(flowCopyCfg);
            }
        } catch (Throwable e) {
            logger.error("FlowCopy parse failed, flowCopyCfgStr={}", flowCopyCfgStr, e);
            catReportException("parseFail", e);
        }
    }

    public static void changeFlowCopyCfg(String flowCopyCfgStr) {
        if (StringUtils.isBlank(flowCopyCfgStr)) {
            stop();
            return;
        }
        try {
            FlowCopyConfig flowCopyCfg = getFlowCopyConfig(flowCopyCfgStr);
            if (flowCopyCfg.isEnable()) {
                if (started) {
                    restart(flowCopyCfg);
                } else {
                    start(flowCopyCfg);
                }
            } else {
                stop();
            }
        } catch (Throwable e) {
            logger.error("FlowCopy parse failed, flowCopyCfgStr={}", flowCopyCfgStr, e);
            catReportException("parseFail", e);
        }
    }

    private static void initStatusCheck() {
        final long taskId = flowCopyConfig.getTaskId();
        Runnable statusCheckTask = new Runnable() {
            @Override
            public void run() {
                if (!started) {
                    return;
                }
                try {
                    // 1. 录制任务状态检查
                    doFlowCopyStatusCheck(taskId);
                    if (!started) {
                        return;
                    }

                    // 2. 连接状态检查
                    if (channelFactory == null) {
                        logger.error("ChannelFactory is null");
                        stop();
                        return;
                    }
                    if (reconnFailTimes == null) {
                        reconnFailTimes = new AtomicInteger(0);
                    }
                    if (!isChannelConnected()) {
                        RecordTaskResponseData flowCopyStatusRsp = queryFlowCopyStatus(taskId);
                        // flowCopyStatusRsp 是null表示任务结束且端口关闭
                        if (flowCopyStatusRsp != null && !TASK_FINISHED.equals(flowCopyStatusRsp.getTaskPhase())) {
                            // 只有在任务未结束时重连
                            reconnFailTimes.incrementAndGet();
                            channel = (NettyChannel) channelFactory.createChannel();
                            logger.info("Succeed reconnect to the server {}", channel.getRemoteAddress());
                        }
                    } else {
                        reconnFailTimes.set(0);
                        flowCopyStatus = FlowCopyStatus.START;
                    }
                } catch (Throwable e) {
                    if (e instanceof IllegalStateException) {
                        // 录制任务状态查询异常
                        catReportException("statusCheckFail", e);
                        logger.error("FlowCopy status check failed, taskId={}", taskId, e);
                    } else {
                        // 重连异常
                        logger.warn("Reconnect to the server {} failed, cause: {}", remoteNodeAddress, e.getClass().getName());
                        catReportException("reconnFail", e);
                        if (reconnFailTimes.get() >= MAX_RECONN_TIMES) {
                            logger.error("Reconnect to server {} fail {} times, stop flow copy task", remoteNodeAddress, MAX_RECONN_TIMES);
                            updateCfgToStop();
                            stop(e);
                        }
                    }
                }
            }
        };
        if (statusCheck == null) {
            statusCheck = Executors.newSingleThreadScheduledExecutor(new MTDefaultThreadFactory("FlowCopyStatusCheck"));
        }
        statusCheck.scheduleWithFixedDelay(statusCheckTask, STATUS_CHECK_INTERVAL, STATUS_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private static void doFlowCopyStatusCheck(long taskId) throws Exception {
        String closeCopyUrl = brokerUrlPrefix + CLOSE_COPY + taskId;
        RecordTaskResponseData data = queryFlowCopyStatus(taskId);
        if (data == null) {
            logger.info("FlowCopy task is finished and task[{}] is closed", taskId);
            stop();
            updateCfgToStop();
        } else if (TASK_FINISHED.equals(data.getTaskPhase())) {
            logger.info("FlowCopy task is finished, closing task[{}]", taskId);
            finishFlowCopy(closeCopyUrl);
        }
    }

    private static RecordTaskResponseData queryFlowCopyStatus(long taskId) throws Exception {
        String fetchStatusUrl = brokerUrlPrefix + FETCH_STATUS + taskId;
        String result = HttpUtils.get(fetchStatusUrl);
        if (StringUtils.isBlank(result)) {
            throw new IllegalStateException("Return empty to access " + fetchStatusUrl);
        }
        RecordTaskResponse response = objectMapper.readValue(result, RecordTaskResponse.class);
        String responseStatus = response.getStatus();
        if (responseStatus == null) {
            throw new IllegalStateException("No request status to access " + fetchStatusUrl);
        }
        RecordTaskResponseData data = response.getData();
        if (!STATUS_OK.equalsIgnoreCase(responseStatus)) {
            String errorMsg = data == null ? "" : data.getMsg();
            throw new IllegalStateException("Access " + fetchStatusUrl + " failed, msg=" + errorMsg);
        }
        return data;
    }

    private static void finishFlowCopy(String closeCopyUrl) throws Exception {
        stop();
        updateCfgToStop();

        int failTimes = 0;
        while (failTimes++ < 3) {
            String result = HttpUtils.get(closeCopyUrl);
            RecordTaskResponse response = objectMapper.readValue(result, RecordTaskResponse.class);
            if (response != null && STATUS_OK.equalsIgnoreCase(response.getStatus())) {
                break;
            }
            if (response == null) {
                logger.warn("Flow copy send close message to broker fail, response is null");
                catReportException("stopBrokerFail", new RuntimeException("Broker response is null"));
            } else {
                String errorMsg = response.getData() == null ? "" : response.getData().getMsg();
                logger.warn("Flow copy send close message to broker fail, cause: {}", errorMsg);
                catReportException("stopBrokerFail", new RuntimeException(errorMsg));
            }
        }
    }

    private static boolean isChannelConnected() {
        return channel != null && channel.isAvailable();
    }

    private static IChannelFactory createChannelFactory() {
        if (remoteNodeAddress == null) {
            throw new IllegalArgumentException("FlowCopy remote node is null");
        }
        if (nioWorkerGroup == null || nioWorkerGroup.isShutdown()) {
            nioWorkerGroup = new NioEventLoopGroup(1, new MTDefaultThreadFactory("FlowCopyClientWorker"));
        }
        Bootstrap bootstrap = new Bootstrap().group(nioWorkerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
                .remoteAddress(remoteNodeAddress).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new FlowCopyChannelHandler());
                    }
                });
        channelFactory = new NettyChannelFactory(bootstrap, remoteNodeAddress, CONNECT_TIMEOUT);
        return channelFactory;
    }

    private static boolean isFlowCopyNode() {
        if (configDetail == null) {
            throw new IllegalArgumentException(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY + " config is invalid, no cfgDetail");
        }
        if (configDetail.getServerIps() == null) {
            return false;
        }
        return configDetail.getServerIps().contains("*") || configDetail.getServerIps().contains(ProcessInfoUtil.getLocalIpV4());
    }

    private static void checkFlowCopyService() {
        if (configDetail == null) {
            throw new IllegalArgumentException(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY + " config is invalid, no cfgDetail");
        }
        String serviceName = configDetail.getServiceName();
        ConcurrentMap<String, List<ServiceIfaceInfo>> portServiceInfo = ThriftServerRepository.getPortServiceInfo();
        for (List<ServiceIfaceInfo> serviceInfos : portServiceInfo.values()) {
            for (ServiceIfaceInfo serviceInfo : serviceInfos) {
                if (serviceInfo.getIfaceName().equals(serviceName)) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY + " config is invalid, no server with serviceName=" + serviceName);
    }

    private static void parseBrokerInfo() {
        String remoteNodeCfg = StringUtils.deleteWhitespace(flowCopyConfig.getIpport());
        String[] ipPorts = remoteNodeCfg.split(":");
        if (ipPorts.length < 2) {
            throw new IllegalArgumentException(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY + " config is invalid, check ipport");
        }

        String ip = ipPorts[0];
        int port = Integer.valueOf(ipPorts[1]);
        remoteNodeAddress = new InetSocketAddress(ip, port);

        String brokerUrlCfg = flowCopyConfig.getBrokerUrl();
        if (StringUtils.isNotBlank(brokerUrlCfg)) {
            brokerUrlPrefix = brokerUrlCfg;
        }
    }

    private static boolean isFlowCopyServiceName(String serviceName) {
        return configDetail != null && serviceName != null && serviceName.equals(configDetail.getServiceName());
    }

    private static boolean isFlowCopyMethod(String methodName) {
        if (StringUtils.isBlank(methodName) || configDetail == null) {
            return false;
        }
        List<String> recordMethodNames = configDetail.getMethodNames();
        return recordMethodNames != null && (recordMethodNames.contains("*") || recordMethodNames.contains(methodName));
    }

    private static byte[] addTestTag(RPCContext context) throws TTransportException {
        Header header = newHeader(context.getHeader());
        header.globalContext.put(Tracer.IS_TEST, Boolean.TRUE.toString());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TIOStreamTransport tioStreamTransport = new TIOStreamTransport(byteArrayOutputStream);
        CustomizedTFramedTransport c = new CustomizedTFramedTransport(tioStreamTransport, header.requestInfo.sequenceId,
                10 * 1024 * 1024, header.requestInfo.getTimeout());
        c.setUnifiedProto(true);
        byte[] protocol = new byte[]{context.getIntactBytes()[3]};
        c.setProtocol(protocol);
        c.setHeaderInfo(header);
        c.write(context.getThriftRequestData());
        c.flush();
        return byteArrayOutputStream.toByteArray();
    }

    private static Header newHeader(Header other) {
        Header header = new Header();

        header.setGlobalContext(new HashMap<String, String>());
        header.setLocalContext(new HashMap<String, String>());

        if (other.isSetMessageType()) {
            header.setMessageType(other.getMessageType());
        }

        if (other.isSetRequestInfo()) {
            header.setRequestInfo(new RequestInfo(other.requestInfo));
        }
        if (other.isSetResponseInfo()) {
            header.setResponseInfo(new ResponseInfo(other.responseInfo));
        }
        if (other.isSetTraceInfo()) {
            header.setTraceInfo(new TraceInfo(other.traceInfo));
        }

        if (other.isSetHeartbeatInfo()) {
            header.setHeartbeatInfo(new HeartbeatInfo(other.heartbeatInfo));
        }

        if (other.isSetGlobalContext()) {
            header.globalContext.putAll(other.globalContext);
        }

        if (other.isSetLocalContext()) {
            header.localContext.putAll(other.localContext);
        }
        return header;
    }

    private static void catReportException(String name, Throwable e) {
        Transaction transaction = Cat.newTransaction("OctoFlowCopy", name);
        Cat.logErrorWithCategory("OctoFlowCopy." + name, e);
        if (flowCopyConfig != null) {
            Cat.logEvent("OctoFlowCopy.taskId", String.valueOf(flowCopyConfig.getTaskId()));
        }
        if (remoteNodeAddress != null) {
            Cat.logEvent("OctoFlowCopy.brokerInfo", remoteNodeAddress.getHostName() + ":" + remoteNodeAddress.getPort());
        }
        transaction.setStatus(e);
        transaction.complete();
        flowCopyStatus = FlowCopyStatus.EXCEPTION.setStatusInfo(name + ", cause:" + e.getClass().getName() + "-" + e.getMessage());
    }

    private static Transaction catReportPrepare(String name, int byteSize) {
        Transaction transaction = Cat.newTransaction("OctoFlowCopy", name);
        if (byteSize == -1) {
            if (flowCopyConfig != null) {
                Cat.logEvent("OctoFlowCopy.taskId", String.valueOf(flowCopyConfig.getTaskId()));
            }
            if (remoteNodeAddress != null) {
                Cat.logEvent("OctoFlowCopy.brokerInfo", remoteNodeAddress.getHostName() + ":" + remoteNodeAddress.getPort());
            }
        } else {
            Cat.logEvent("OctoFlowCopy.taskId", String.valueOf(flowCopyConfig.getTaskId()));
            Cat.logEvent("OctoFlowCopy.method", name);
            Cat.logEvent("OctoFlowCopy.savePath", configDetail.getSavePath());
            Cat.logEvent("OctoFlowCopy.isTagged", String.valueOf(configDetail.isTagged()));
            Cat.logEvent("OctoFlowCopy.brokerInfo", remoteNodeAddress.getHostName() + ":" + remoteNodeAddress.getPort());
            Cat.logEvent("OctoFlowCopy.packageSize", SizeUtil.getLogSize(byteSize));
        }
        return transaction;
    }

    private static void updateCfgToStop() {
        // 只修改enable字段，避免flowCopyConfig变更如为null, 重读再写
        String flowCopyCfgStr = MtConfigUtil.getMtConfigValue(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY);
        if (StringUtils.isBlank(flowCopyCfgStr)) {
            return;
        }
        try {
            FlowCopyConfig flowCopyCfg = objectMapper.readValue(flowCopyCfgStr, FlowCopyConfig.class);
            if (flowCopyCfg.isEnable()) {
                flowCopyCfg.setEnable(false);
                flowCopyCfgStr = objectMapper.writeValueAsString(flowCopyCfg);
                boolean ret = MtConfigUtil.getMtConfigClient().setValue(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY, flowCopyCfgStr);
                if (!ret) {
                    catReportException("updateMCCCfgFail", new RuntimeException("updateMCCCfgFail, return false"));
                }
            }
        } catch (Throwable e) {
            logger.error("FlowCopy parse failed when updateCfgToStop", e);
            catReportException("updateMCCCfgFail", e);
        }
    }

    public static FlowCopyConfig getFlowCopyConfig(String flowCopyCfgStr) throws IOException {
        FlowCopyConfig flowCopyCfg = objectMapper.readValue(flowCopyCfgStr, FlowCopyConfig.class);
        return flowCopyCfg;
    }

    private static class FlowCopyChannelHandler extends ChannelDuplexHandler {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (!"Connection reset by peer".equals(cause.getMessage())) {
                super.exceptionCaught(ctx, cause);
            }
        }
    }

    public static String getStatusInfo() {
        StringBuilder statusInfo = new StringBuilder();
        if (started) {
            int count = copyCount != null ? copyCount.get() : 0;
            statusInfo.append("当前录制条数: ").append(count).append(";");
        }
        if (oldProtoApi != null && !oldProtoApi.isEmpty()) {
            statusInfo.append(" Warning: 录制方法有旧协议: ").append(oldProtoApi.keySet());
        }
        if (statusInfo.length() > 0) {
            statusInfo.insert(0, flowCopyStatus.getStatus() + " -- ");
        } else {
            statusInfo.insert(0, flowCopyStatus.getStatus());
        }
        return statusInfo.toString();
    }
}
