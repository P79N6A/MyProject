package com.sankuai.octo.benchmark.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

import java.net.InetSocketAddress;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-26
 * Time: 下午12:59
 */
public class ThriftServer {

    public static final int SERVER_PORT = 9008;
    public static final String SERVER_IP = "10.4.96.162";

    public void startServer() {
        try {
            System.out.println("ThriftServer start ....");

            TProcessor tprocessor = new EchoService.Processor(new EchoServiceImpl());

            TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(new InetSocketAddress(SERVER_IP, SERVER_PORT));
            TThreadedSelectorServer.Args tnbArgs = new TThreadedSelectorServer.Args(tnbSocketTransport);
            tnbArgs.selectorThreads(4);
            tnbArgs.processor(tprocessor);
            tnbArgs.transportFactory(new TFramedTransport.Factory());
            tnbArgs.protocolFactory(new TBinaryProtocol.Factory());

            TServer server = new TThreadedSelectorServer(tnbArgs);
            server.serve();

        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        ThriftServer server = new ThriftServer();
        server.startServer();
    }

}
