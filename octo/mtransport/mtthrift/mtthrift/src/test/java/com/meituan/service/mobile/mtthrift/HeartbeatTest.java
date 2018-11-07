package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.octo.protocol.HeartbeatInfo;
import com.sankuai.octo.protocol.MessageType;
import com.sankuai.sgagent.thrift.model.fb_status;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-20
 * Time: 下午6:27
 */
public class HeartbeatTest {

    public static fb_status detect() {

        CustomizedTFramedTransport transport = null;
        int status = 0;
        String ip = ProcessInfoUtil.getLocalIpV4();
        int port = 10001;
        TSocket socket = new TSocket(ip, port);
        socket.setTimeout(200);
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
                System.out.println(heartbeatInfo);
            }
            if (heartbeatInfo != null) {
                status = heartbeatInfo.getStatus();
            }
            return fb_status.findByValue(status);
        } catch (Exception e) {
            return fb_status.findByValue(status);
        } finally {
            if (transport != null)
                transport.close();
        }
    }

    public static void main(String[] args) {
        System.out.println(detect());
    }

}
