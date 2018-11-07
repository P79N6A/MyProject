package com.meituan.service.mobile.mtthrift.util;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;
import com.meituan.mtrace.ClientTracer;
import com.meituan.mtrace.Endpoint;
import com.meituan.mtrace.Span;
import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.auth.AuthType;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerRepository;
import com.meituan.service.mobile.mtthrift.server.MTTServer;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.TraceInfo;

import java.util.HashMap;
import java.util.Map;

public class TraceInfoUtil {

    public static boolean useServiceProcessorMap;
    public static boolean useDirect;


    public static TraceInfo getTraceInfo(String clientAppkey, String clientIp) {

        TraceInfo traceInfo = new TraceInfo();

        Span span = Tracer.getClientSpan();
        Map<String, String> localContext = ContextUtil.getLocalContext();
        if (localContext != null && !localContext.isEmpty()) {
            for (Map.Entry<String, String> entry : localContext.entrySet()) {
                ClientTracer.getInstance().putRemoteOneStepContext(entry.getKey(), entry.getValue());
            }
        }

        traceInfo.setClientAppkey(clientAppkey);
        traceInfo.setClientIp(clientIp);
        if (span != null) {
            traceInfo.setTraceId(span.getTraceId());
            traceInfo.setSpanId(span.getSpanId());
            traceInfo.setDebug(span.isDebug());
            traceInfo.setSample(span.isSample());
        }
        return traceInfo;
    }

    public static void serverRecv(TraceInfo traceInfo, String spanName, Endpoint localEndpoint, String clientIp,
                                  int packageSize, Header header) {
        TraceParam param = new TraceParam(spanName);
        param.setLocal(localEndpoint.getAppkey(), localEndpoint.getHost(), localEndpoint.getPort());
        param.setInfraName(Consts.mtraceInfra);
        param.setVersion(MtThriftManifest.getVersion());
        param.setPackageSize(packageSize);
        if (null != traceInfo) {
            param.setTraceId(traceInfo.getTraceId());
            param.setSpanId(traceInfo.getSpanId());
            param.setRemoteAppKey(traceInfo.getClientAppkey());
            param.setRemoteIp(clientIp);
            param.setSample(traceInfo.isSample());
            param.setDebug(traceInfo.isDebug());
        }
        Span span = Tracer.serverRecv(param);
        if (span != null) {
            if (header.getLocalContext() != null) {
                span.getRemoteOneStepContext().putAll(header.getLocalContext());
            }
            if (header.getGlobalContext() != null) {
                span.getForeverContext().putAll(header.getGlobalContext());
            }
        }

    }

    public static void catInitAtClient(ThriftClientProxy thriftClientProxy) {
        Cat.initializeByDomain(thriftClientProxy.getAppKey());
        registerCatHeartbeat(thriftClientProxy);
    }

    public static void catInitAtServer(ThriftServerPublisher serverPublisher) {
        Cat.initializeByDomain(serverPublisher.getAppKey());
        registerCatHeartbeat(serverPublisher);
    }

    public static void mtraceInitAtClient(String appkey) {
        TraceParam param = new TraceParam(Consts.ThriftClientInitSpan);
        param.setLocalAppKey(appkey);
        param.setInfraName(Consts.mtraceInfra);
        param.setVersion(MtThriftManifest.getVersion());
        Tracer.clientSend(param);
        Tracer.clientRecv();
    }

    public static void catRecordAuthFail(String clientAppkey, String spanName,
                                         String clientIP, int authCode, String authType, String isUnifiedProto) {
        Transaction transaction = Cat.newTransaction("OctoAuthFail", clientAppkey);
        Cat.logEvent("OctoAuthFail.clientAppkey", clientAppkey);
        Cat.logEvent("OctoAuthFail.clientIp", clientIP);
        Cat.logEvent("OctoAuthFail.authType", authType);
        Cat.logEvent("OctoAuthFail.spanName", spanName);
        Cat.logEvent("OctoAuthFail.authCode", String.valueOf(authCode));
        Cat.logEvent("OctoAuthFail.isUnifiedProto", isUnifiedProto);
        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
    }

    /**
     * 在cat心跳上报信息中添加调用端相关信息
     *
     * @param thriftClientProxy
     */
    private static void registerCatHeartbeat(final ThriftClientProxy thriftClientProxy) {
        StatusExtensionRegister.getInstance().register(new StatusExtension() {
            @Override
            public String getId() {
                return "client info from mtthrift";
            }

            @Override
            public String getDescription() {
                return "client info";
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> clientInfo = new HashMap<String, String>();
                String processId = "pid:" + com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getPid();
                String hostName = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getHostNameInfoByIp();

                String localAppName = MtConfigUtil.getAppName();
                if (localAppName != null) {
                    clientInfo.put("OctoCall.clientAppKey", localAppName);
                } else {
                    clientInfo.put("OctoCall.clientAppKey", thriftClientProxy.getAppKey());
                }
                clientInfo.put("OctoCall.MTthriftVersion", MtThriftManifest.getVersion());
                clientInfo.put("OctoCall.hostName", hostName);
                clientInfo.put("OctoCall.processId", processId);

                clientInfo.put("OctoCall.isUseDirect", String.valueOf(useDirect));
                clientInfo.put("OctoCall.isUseAuth", String.valueOf(ThriftClientGlobalConfig.isEnableAuth()));

                Cat.logEvent("OctoCall.isUseAuth", String.valueOf(ThriftClientGlobalConfig.isEnableAuth()));
                Cat.logEvent("OctoCall.isUseDirect", String.valueOf(useDirect));

                return clientInfo;
            }
        });
    }

    /**
     * 在cat心跳上报信息中添加服务端相关信息
     *
     * @param serverPublisher
     */
    private static void registerCatHeartbeat(final ThriftServerPublisher serverPublisher) {
        StatusExtensionRegister.getInstance().register(new StatusExtension() {
            @Override
            public String getId() {
                return "server info from mtthrift";
            }

            @Override
            public String getDescription() {
                return "server info";
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> clientInfo = new HashMap<String, String>();
                String processId = "pid:" + com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getPid();
                String hostName = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getHostNameInfoByIp();
                clientInfo.put("OctoService.serverAppKey", serverPublisher.getAppKey());
                clientInfo.put("OctoService.MTthriftVersion", MtThriftManifest.getVersion());
                clientInfo.put("OctoService.hostName", hostName);
                clientInfo.put("OctoService.processId", processId);
                clientInfo.put("OctoService.isUseServiceProcessorMap", String.valueOf(useServiceProcessorMap));
                clientInfo.put("OctoService.isUseAuth", String.valueOf(ThriftServerGlobalConfig.isEnableAuth()));
                clientInfo.put("OctoService.isGrayAuth", String.valueOf(ThriftServerGlobalConfig.isEnableGrayAuth()));

                Cat.logEvent("OctoService.isUseServiceProcessorMap", String.valueOf(useServiceProcessorMap));
                Cat.logEvent("OctoService.isUseAuth", String.valueOf(ThriftServerGlobalConfig.isEnableAuth()));
                Cat.logEvent("OctoService.isGrayAuth", String.valueOf(ThriftServerGlobalConfig.isEnableGrayAuth()));

                // 记录各端口是否配置authHandler
                Map<String, MTTServer> portToServer = ThriftServerRepository.getPortServer();
                String realAppkey = serverPublisher.getAppKey();
                StringBuilder eachPortToAuthHandlerExistedStrBuilder = new StringBuilder();
                for (Map.Entry<String, MTTServer> entry : portToServer.entrySet()) {
                    eachPortToAuthHandlerExistedStrBuilder.append(realAppkey);
                    eachPortToAuthHandlerExistedStrBuilder.append("-");
                    eachPortToAuthHandlerExistedStrBuilder.append(entry.getKey()); // 端口
                    eachPortToAuthHandlerExistedStrBuilder.append("-");
                    int authHandlerStatus = -1;
                    if (entry.getValue().getAuthHandler() == null) {
                        authHandlerStatus = 0;
                    } else if (entry.getValue().getAuthHandler().getAuthType() == AuthType.channelAuth) {
                        authHandlerStatus = 1;
                    } else if (entry.getValue().getAuthHandler().getAuthType() == AuthType.requestAuth) {
                        authHandlerStatus = 2;
                    }
                    eachPortToAuthHandlerExistedStrBuilder.append(authHandlerStatus); // 是否配置handler
                    eachPortToAuthHandlerExistedStrBuilder.append("_"); // 下滑线分割每个端口内容
                }
                String eachPortToAuthHandlerExistedValue = eachPortToAuthHandlerExistedStrBuilder.toString();
                Cat.logEvent("OctoService.existAuthHandler", eachPortToAuthHandlerExistedValue.substring(0, eachPortToAuthHandlerExistedValue.length() - 1));

                return clientInfo;
            }
        });
    }
}
