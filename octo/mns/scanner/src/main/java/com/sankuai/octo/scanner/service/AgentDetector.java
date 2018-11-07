package com.sankuai.octo.scanner.service;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.octo.scanner.Common;
import com.sankuai.octo.scanner.model.Provider;
import com.sankuai.sgagent.thrift.model.SGAgent;
import com.sankuai.sgagent.thrift.model.fb_status;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-10-30
 * Time: 下午5:49
 */
public class AgentDetector {

    private static Logger logger = LoggerFactory.getLogger(AgentDetector.class);

    public static fb_status detect(Provider provider) {
        fb_status status = fb_status.DEAD;
        SGAgent.Iface agentClient;
        TSocket socket = new TSocket(provider.getIp(), provider.getPort(), Common.longTimeOutInMills);
        try {
            socket.open();
            socket.setTimeout(Consts.defaultTimeoutInMills);
            TFramedTransport transport = new TFramedTransport(socket, Consts.defaltMaxResponseMessageBytes);
            TProtocol protocol = new TBinaryProtocol(transport);
            agentClient = new SGAgent.Client(protocol);
            status = agentClient.getStatus();
            logger.info("AgentDetector:" + provider.getIpPort() + "status: " + status.toString());
            return status;
        } catch (TException e) {
            String ex = e.getMessage();
            provider.setExceptionMsg(ex);
            logger.error(ex);
            if (Common.isOnline && e.getMessage().contains("SocketTimeoutException")) {
                status = detectAgain(provider);
            }
            return status;
        } finally {
            socket.close();

        }
    }


    private static fb_status detectAgain(Provider provider) {
        fb_status status = fb_status.DEAD;
        SGAgent.Iface agentClient;
        TSocket socket = new TSocket(provider.getIp(), provider.getPort(), Common.longTimeOutInMills);
        try {
            socket.open();
            socket.setTimeout(Consts.defaultTimeoutInMills);
            TFramedTransport transport = new TFramedTransport(socket, Consts.defaltMaxResponseMessageBytes);
            TProtocol protocol = new TBinaryProtocol(transport);
            agentClient = new SGAgent.Client(protocol);
            status = agentClient.getStatus();
            return status;
        } catch (TException e) {
            String ex = e.getMessage();
            provider.setExceptionMsg(ex);
            logger.error(ex);
            return status;
        } finally {
            socket.close();

        }
    }

    public static boolean isReachable(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(Common.longTimeOutInMills);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

}
