package com.meituan.service.mobile.mtthrift.proxy;

import com.meituan.service.mobile.mtthrift.auth.ISignHandler;
import com.meituan.service.mobile.mtthrift.client.cluster.DirectlyCluster;
import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.server.http.handler.check.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/3
 */
public class ThriftClientRepository {
    private static final Logger logger = LoggerFactory.getLogger(ThriftClientRepository.class);

    private static final List<ClientInfo> clientInfoList = new Vector<ClientInfo>();

    public static void addClientInfo(String appKey, String remoteAppkey, int remoteServerPort, Class<?> serviceInterface, ICluster cluster, ISignHandler signHandler) {
        try {
            ClientInfo clientInfo = new ClientInfo(appKey, remoteAppkey, remoteServerPort, serviceInterface.getName(), cluster, signHandler);
            clientInfoList.add(clientInfo);
        } catch (Exception e) {
            // 防御性容错, 避免影响服务启动
            logger.error("MTthrift record client info fail.", e);
        }
    }

    public static List<ClientInfo> getClientInfoList() {
        return clientInfoList;
    }
}
