package com.sankuai.octo.benchmark.client;

import com.codahale.metrics.Histogram;
//import com.meituan.service.mobile.mtthrift.netty.DefaultRequest;
//import com.meituan.service.mobile.mtthrift.netty.NettyClient;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.octo.benchmark.thrift.Message;
import com.sankuai.octo.benchmark.utils.ThriftMsgGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-11-8
 * Time: 下午4:03
 */
public class AsyncNettyClient {

//    private final static Logger logger = LoggerFactory.getLogger(AsyncNettyClient.class);
//
//    private static final AtomicLong SEQID = new AtomicLong();
//
//    protected static long getSequenceId() {
//        return SEQID.addAndGet(1L);
//    }
//
//    public int threadNum;
//    public long msgCount;
//    public int msgLength;
//    public String msgType;
//
//    private static Histogram histogram;
//    private static List<Message> msgList;
//    private static String str;
//    private static ByteBuffer buffer;
//
//    public AsyncNettyClient() {
//    }
//
//    public AsyncNettyClient(int threadNum, long msgCount, int msgLength, String msgType) {
//        this.threadNum = threadNum;
//        this.msgCount = msgCount;
//        this.msgLength = msgLength;
//        this.msgType = msgType;
//    }
//
//    public static void main(String[] args) throws Exception {
//
//        int threadNum = 1;
//        long msgCount = 3000000;
//        int msgLength = 10;
//        String msgType = "string";
//
//        if (null != args) {
//            if (args.length >= 1) {
//                threadNum = Integer.parseInt(args[0]);
//            }
//            if (args.length >= 2) {
//                msgCount = Long.parseLong(args[1]);
//            }
//            if (args.length >= 3) {
//                msgLength = Integer.parseInt(args[2]);
//            }
//            if (args.length >= 4) {
//                msgType = args[3];
//            }
//        }
//
//        AsyncNettyClient asyncNettyClient = new AsyncNettyClient(threadNum, msgCount, msgLength, msgType);
//        asyncNettyClient.test();
//    }
//
//    public void test() {
//
//        initMsg();
//        final String host = "10.4.99.157";
//        final int port = 9008;
//
//        NettyClient client = new NettyClient(host, port);
//        Class clazz = null;
//        try {
//            clazz = Class.forName("com.sankuai.octo.benchmark.thrift.EchoService");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < msgCount; i++) {
//            DefaultRequest request = new DefaultRequest();
//            request.setServiceInterface(clazz);
//            request.setMethodName("sendString");
//            request.setParameters(new Object[]{str});
//            request.setParameterTypes(new Class[]{String.class});
//            request.setSeq(getSequenceId());
//
//            try {
//                client.sent(request);
//                if (i % 49 == 0)
//                    Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
////            System.out.println(client.getResponse(request.getSeq()).getReturnVal());
//        }
//
//        System.out.println("finish!");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
////        client.destroy();
//
//    }
//
//    public void initMsg() {
//        buffer = ThriftMsgGenerator.getRandomBuffer(msgLength);
//        str = ThriftMsgGenerator.getRandomString(msgLength);
//        msgList = ThriftMsgGenerator.getRandomMessageList(msgLength);
//    }
}
