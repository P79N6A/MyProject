package com.meituan.service.mobile.mtthrift.server.http.handler;

import com.google.common.collect.ImmutableMap;
import com.meituan.dorado.common.RpcRole;
import com.meituan.service.mobile.mtthrift.auth.DefaultAuthHandler;
import com.meituan.service.mobile.mtthrift.auth.DefaultSignHandler;
import com.meituan.service.mobile.mtthrift.auth.IAuthHandler;
import com.meituan.service.mobile.mtthrift.auth.ISignHandler;
import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.client.cluster.OctoAgentCluster;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientRepository;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerRepository;
import com.meituan.service.mobile.mtthrift.server.MTTServer;
import com.meituan.service.mobile.mtthrift.server.flow.FlowCopyConfig;
import com.meituan.service.mobile.mtthrift.server.flow.FlowCopyTask;
import com.meituan.service.mobile.mtthrift.server.http.NettyHttpSender;
import com.meituan.service.mobile.mtthrift.server.http.NettyHttpServer;
import com.meituan.service.mobile.mtthrift.server.http.handler.check.*;
import com.meituan.service.mobile.mtthrift.util.MtConfigUtil;
import com.meituan.service.mobile.mtthrift.util.MtThriftManifest;
import com.meituan.service.mobile.mtthrift.util.URLUtil;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.sgagent.thrift.model.CustomizedStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static com.meituan.service.mobile.mtthrift.server.http.handler.check.HttpCheckURI.SERVICE_REQUEST_PREFIX;
import static com.meituan.service.mobile.mtthrift.server.http.handler.check.HttpCheckURI.toHttpCheckURI;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/2
 */
public class HttpCheckHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpCheckHandler.class);

    private HttpInvokeHandler httpInvokeHandler;
    private RpcRole role;
    private Set<String> supportReqs;

    @Override
    public void handle(NettyHttpSender httpSender, String uri, byte[] content) {
        String path = URLUtil.getURIPath(uri);

        /**
         * http接口调用
         */
        if (path.startsWith(SERVICE_REQUEST_PREFIX.uri())) {
            if (RpcRole.INVOKER == role) {
                String errorMsg = role + " not support service invoke";
                logger.warn(errorMsg);
                httpSender.sendErrorResponse(errorMsg);
                return;
            }
            if (httpInvokeHandler == null) {
                httpInvokeHandler = new HttpInvokeHandler();
            }
            httpInvokeHandler.handle(httpSender, uri, content);
            return;
        }

        if (!getSupportReqs().contains(path)) {
            httpSender.sendErrorResponse(role + " not support the request. Support uri: " + supportReqs);
            return;
        }
        try {
            handleHttpCheckReq(httpSender, uri, content);
        } catch (Exception e) {
            logger.warn("Http check fail.", e);
            httpSender.sendErrorResponse(e.getClass().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public void setRole(RpcRole rpcRole) {
        if (role != null && role != rpcRole) {
            role = RpcRole.MULTIROLE;
            updateSupportReqs();
        } else {
            role = rpcRole;
        }
    }

    @Override
    public RpcRole getRole() {
        return role;
    }

    private void handleHttpCheckReq(NettyHttpSender httpSender, String uri, byte[] content) {
        String path = URLUtil.getURIPath(uri);

        switch (toHttpCheckURI(path)) {
            case SERVICE_BASE_INFO:
                getServiceBaseInfo(httpSender);
                break;
            case SERVICE_METHOD_INFO:
                getServiceMethodInfo(httpSender);
                break;
            case AUTH_INFO:
                getAuthInfo(httpSender);
                break;
            case PROVIDER_INFO:
                getProviderInfo(httpSender);
                break;
            case FLOWCOPY_INFO:
                getFlowCopyInfo(httpSender);
                break;
            case UNKNOW:
                httpSender.sendErrorResponse("not support the request");

        }
    }

    /**
     * 服务基本信息
     * 角色: 服务端
     *
     * @param httpSender
     */
    private void getServiceBaseInfo(NettyHttpSender httpSender) {
        ServerInfo info = new ServerInfo();

        String appkeyInfo = getProviderAppkey();
        info.setAppkey(appkeyInfo);
        info.setEnv(ProcessInfoUtil.getHostEnv().name());
        info.setSwimlane(ProcessInfoUtil.getSwimlane());
        info.setVersion("mtthrift-v" + MtThriftManifest.getVersion());
        info.setStartTime(ThriftServerRepository.getStartTime().toString());

        List<PortServiceInfo> serviceInfos = new ArrayList<PortServiceInfo>();
        for (Map.Entry<String, MTTServer> entry : ThriftServerRepository.getPortServer().entrySet()) {
            MTTServer server = entry.getValue();
            String port = entry.getKey();
            String status = server.getConfigStatus().getRuntimeStatus().toString();

            List<ServiceIfaceInfo> serviceIfaceInfos = ThriftServerRepository.getPortServiceInfo().get(port);
            PortServiceInfo portServiceInfo = new PortServiceInfo(port, serviceIfaceInfos, status);
            serviceInfos.add(portServiceInfo);
        }
        int httpPort = NettyHttpServer.getHttpServer().getLocalAddress().getPort();
        serviceInfos.add(new PortServiceInfo(httpPort + "(http)", Collections.EMPTY_LIST, String.valueOf(CustomizedStatus.ALIVE)));
        info.setServiceInfo(serviceInfos);

        httpSender.sendObjectJson(info);
    }

    /**
     * 服务方法信息
     * 角色: 服务端
     *
     * @param httpSender
     */
    private void getServiceMethodInfo(NettyHttpSender httpSender) {
        ServerInfo info = new ServerInfo();
        String appkeyInfo = getProviderAppkey();
        info.setAppkey(appkeyInfo);
        info.setVersion("mtthrift-v" + MtThriftManifest.getVersion());

        List<ServiceMethodInfo> serviceMethods = new ArrayList<ServiceMethodInfo>();
        ConcurrentMap<String, ImmutableMap<String, Method>> serviceMethodsMap = ThriftServerRepository.getAllMethods();
        for (Map.Entry<String, ImmutableMap<String, Method>> entry : serviceMethodsMap.entrySet()) {
            String serviceName = entry.getKey();
            ImmutableMap<String, Method> methods = entry.getValue();
            Set<String> methodNames = new HashSet<String>();
            for (String methodName : methods.keySet()) {
                String returnType = methods.get(methodName).getReturnType().getSimpleName();
                methodNames.add(methodName + ":" + returnType);
            }
            ServiceMethodInfo methodsInfo = new ServiceMethodInfo(serviceName, methodNames);
            serviceMethods.add(methodsInfo);
        }
        info.setServiceMethods(serviceMethods);
        httpSender.sendObjectJson(info);
    }

    /**
     * 查询可调用的服务列表
     * 角色: 调用端
     *
     * @param httpSender
     */
    private void getProviderInfo(NettyHttpSender httpSender) {
        List<ClientInfo> clientInfoList = ThriftClientRepository.getClientInfoList();
        List<ClientInfo> clientInfos = new ArrayList<ClientInfo>();
        for (ClientInfo clientInfo : clientInfoList) {
            ClientInfo info = new ClientInfo(clientInfo);
            ICluster cluster = clientInfo.getCluster();

            List<Server> providers = new ArrayList<Server>();
            List<ServerConn> serverConns = cluster.getServerConnList();
            for (ServerConn serverConn : serverConns) {
                Server provider = serverConn.getServer();
                providers.add(provider);
            }
            info.setProviders(providers);

            if (cluster instanceof OctoAgentCluster) {
                info.setRemoteAppIsCell(((OctoAgentCluster) cluster).isRemoteAppIsCell());
            }
            clientInfos.add(info);
        }

        httpSender.sendObjectJson(clientInfos);
    }

    /**
     * 鉴权信息
     * 角色: 服务端/调用端
     *
     * @param httpSender
     */
    private void getAuthInfo(NettyHttpSender httpSender) {
        Object object;
        if (RpcRole.MULTIROLE == role) {
            Map<String, Object> authMap = new HashMap<String, Object>();
            authMap.put("Provider", getProviderAuthInfo(httpSender));
            authMap.put("Invoker", getInvokerAuthInfo(httpSender));
            object = authMap;
        } else if (RpcRole.PROVIDER == role) {
            object = getProviderAuthInfo(httpSender);
        } else {
            object = getInvokerAuthInfo(httpSender);
        }
        httpSender.sendObjectJson(object);
    }

    /**
     * 流量录制信息
     * 角色: 服务端
     *
     * @param httpSender
     */
    private void getFlowCopyInfo(NettyHttpSender httpSender) {
        Map<String, Object> flowCopyMap = new HashMap<String, Object>();
        String flowCopyStatus = FlowCopyTask.getStatusInfo();
        try {
            String flowCopyCfgStr = MtConfigUtil.getMtConfigValue(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY);
            FlowCopyConfig config = null;
            if (StringUtils.isNotBlank(flowCopyCfgStr)) {
                config = FlowCopyTask.getFlowCopyConfig(flowCopyCfgStr);
            }
            flowCopyMap.put("configInfo", config);
        } catch (IOException e) {
            flowCopyMap.put("configInfo", "parse failed.");
        }
        flowCopyMap.put("status", flowCopyStatus);
        httpSender.sendObjectJson(flowCopyMap);
    }

    private Object getProviderAuthInfo(NettyHttpSender httpSender) {
        ServerInfo info = new ServerInfo();

        String appkeyInfo = getProviderAppkey();
        info.setAppkey(appkeyInfo);

        List<PortServiceInfo> serviceInfos = new ArrayList<PortServiceInfo>();
        for (Map.Entry<String, MTTServer> entry : ThriftServerRepository.getPortServer().entrySet()) {
            String port = entry.getKey();
            IAuthHandler authHandler = entry.getValue().getAuthHandler();

            AuthInfo authInfo;
            if (authHandler == null) {
                authInfo = new AuthInfo("No auth config");
            } else {
                try {
                    if (authHandler instanceof DefaultAuthHandler) {
                        DefaultAuthHandler defaultAuthHandler = (DefaultAuthHandler) authHandler;
                        Map<String, String> appkeyTokenMap = defaultAuthHandler.getAppkeyTokenMap();
                        Set<String> appkeyWhitelist = defaultAuthHandler.getAppkeyWhitelist();
                        Map<String, Map<String, String>> methodAppkeyTokenMap = defaultAuthHandler.getMethodAppkeyTokenMap();
                        authInfo = new AuthInfo(authHandler.getClass(), appkeyTokenMap, appkeyWhitelist, methodAppkeyTokenMap);
                    } else {
                        authInfo = new AuthInfo(authHandler.getClass(), "Not default AuthHandler, cannot get auth info");
                    }
                } catch (Exception e) {
                    logger.warn("{} getAuthInfo fail", this.getClass(), e);
                    authInfo = new AuthInfo(authHandler.getClass(), e.getClass().getName() + ":" + e.getMessage());
                }
            }
            PortServiceInfo portServiceInfo = new PortServiceInfo(port, authInfo);
            serviceInfos.add(portServiceInfo);
        }
        if (serviceInfos.isEmpty()) {
            serviceInfos.add(new PortServiceInfo(null, new AuthInfo("No auth config")));
        }
        info.setServiceInfo(serviceInfos);
        return info;
    }

    private Object getInvokerAuthInfo(NettyHttpSender httpSender) {
        List<ClientInfo> clientInfoList = ThriftClientRepository.getClientInfoList();
        List<ClientInfo> clientInfos = new ArrayList<ClientInfo>();
        for (ClientInfo clientInfo : clientInfoList) {
            ClientInfo info = new ClientInfo(clientInfo);
            ISignHandler signHandler = clientInfo.getSignHandler();

            SignInfo signInfo;
            if (signHandler == null) {
                signInfo = new SignInfo("No auth config");
            } else {
                try {
                    if (signHandler instanceof DefaultSignHandler) {
                        Map<String, String> map = ((DefaultSignHandler) signHandler).getLocalAppkeyTokenMap();
                        signInfo = new SignInfo(signHandler.getClass(), map);
                    } else {
                        signInfo = new SignInfo(signHandler.getClass(), "Not default SignHandler, cannot get sign info");
                    }
                } catch (Exception e) {
                    logger.warn("{} getAuthInfo fail", this.getClass(), e);
                    signInfo = new SignInfo(signHandler.getClass(), e.getMessage());
                }
            }
            info.setSignInfo(signInfo);
            clientInfos.add(info);
        }
        return clientInfos;
    }

    private String getProviderAppkey() {
        String appkeyInfo;
        Set<String> appkeys = ThriftServerRepository.getAppkeys();
        if (appkeys.isEmpty()) {
            appkeyInfo = "未获取到Appkey";
        } else if (appkeys.size() > 1) {
            appkeyInfo = appkeys.toString() + "(服务不能有多个Appkey!)";
        } else {
            appkeyInfo = appkeys.iterator().next();
        }
        return appkeyInfo;
    }

    private synchronized Set<String> getSupportReqs() {
        if (supportReqs == null) {
            supportReqs = HttpCheckURI.getSupportUriOfRole(role);
        }
        return supportReqs;
    }

    private synchronized void updateSupportReqs() {
        supportReqs = HttpCheckURI.getSupportUriOfRole(role);
    }
}
