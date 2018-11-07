package com.sankuai.octo.util;

import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.sankuai.octo.protocol.HeartbeatInfo;
import com.sankuai.octo.protocol.MessageType;
import com.sankuai.octo.scanner.model.Provider;
import com.sankuai.octo.updater.util.Common;
import com.sankuai.sgagent.thrift.model.fb_status;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-8-15
 * Time: 下午8:41
 */
public class HeartbeatDetector {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatDetector.class);

    public static fb_status detect(Provider provider) {

        CustomizedTFramedTransport transport = null;
        int status = 0;

        String ip = provider.getIp();
        int port = provider.getPort();

        TSocket socket = new TSocket(ip, port);
        socket.setTimeout(Common.longTimeOutInMills());
        try {
            socket.getSocket().setReuseAddress(true);
            socket.getSocket().setTcpNoDelay(true);
            socket.open();
            transport = new CustomizedTFramedTransport(socket);
            transport.setUnifiedProto(true);
            transport.setProtocol(Consts.protocol);
            transport.setServiceName("ScannerHeartbeat");
            transport.setMessageType(MessageType.ScannerHeartbeat);
            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            protocol.getTransport().flush();
            transport.readFrame();
            HeartbeatInfo heartbeatInfo = null;
            if (transport.getHeaderInfo() != null) {
                heartbeatInfo = transport.getHeaderInfo().getHeartbeatInfo();
            }
            if (heartbeatInfo != null) {
                status = heartbeatInfo.getStatus();
            }
            return fb_status.findByValue(status);
        } catch (Exception e) {
            provider.setExceptionMsg(e.getMessage());
            if (Common.isOnline() && e.getMessage().contains("SocketTimeoutException")) {
                //detectAgain
                return fb_status.findByValue(status);
            } else {
                return fb_status.findByValue(status);
            }
        } finally {
            if (transport != null)
                transport.close();
        }

    }

    public static boolean isReachable(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(Common.longTimeOutInMills());
        } catch (Exception e) {
            return false;
        }
    }

}
