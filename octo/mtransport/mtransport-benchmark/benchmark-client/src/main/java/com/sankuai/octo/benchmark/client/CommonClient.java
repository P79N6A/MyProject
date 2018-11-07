package com.sankuai.octo.benchmark.client;

import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-5
 * Time: 下午12:36
 */
public class CommonClient {

    private final static Logger logger = LoggerFactory.getLogger(CommonClient.class);

    public static String rpcType = "mtthrift"; //mtthrift、pigeon、dorado、cthrift
    public static int threadNum = 32;
    public static long msgCount = 100000;
    public static int msgLength = 100;
    public static String msgType = "pojo"; //pojo、string、byte
    public static int maxConnection = 1000;


    public MtConfigClient client;

    public void init() {
        client = new MtConfigClient();
        client.setModel("v2");
        client.setAppkey(Common.MTCONFIG_APPKEY);
        client.setId("rpc-benchmark");
        client.setScanBasePackage("com.meituan.inf");
        client.init();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.addListener(Common.RUN, new IConfigChangeListener() {
            public void changed(String s, String oldValue, String newValue) {
                if (newValue.equals("on")) {
                    runClient();
                } else if (newValue.equals("quit")) {
                    logger.info("\n System exit! \n");
                    System.exit(0);
                }
            }
        });

        client.addListener(Common.RPC_TYPE, new IConfigChangeListener() {
            public void changed(String s, String oldValue, String newValue) {
                rpcType = client.getValue(Common.RPC_TYPE);
            }
        });
    }

    public void runClient() {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadNum = Integer.valueOf(client.getValue(Common.THREAD_NUM));
        msgCount = Long.valueOf(client.getValue(Common.MSG_COUNT));
        msgLength = Integer.valueOf(client.getValue(Common.MSG_LENGTH));
        msgType = client.getValue(Common.MSG_TYPE);
        rpcType = client.getValue(Common.RPC_TYPE);
        maxConnection = Integer.parseInt(client.getValue(Common.MAX_CONN));
        logger.info("rpcType:" + rpcType);

        if ("mtthrift".equals(rpcType)) {
            MtthriftClient mtthriftClient = new MtthriftClient(threadNum, msgCount, msgLength, msgType);
            mtthriftClient.test();
        } else if ("pigeon".equals(rpcType)) {
            PigeonClient pigeonClient = new PigeonClient(threadNum, msgCount, msgLength, msgType);
            pigeonClient.test();
        } else if ("dorado".equals(rpcType)) {
            DoradoClient doradoClient = new DoradoClient(threadNum, msgCount, msgLength, msgType);
            doradoClient.test();
        } else if ("cthrift".equals(rpcType)) {
            CthriftClient cthriftClient = new CthriftClient(threadNum, msgCount, msgLength, msgType);
            cthriftClient.test();
        } else if ("asyncpigeon".equals(rpcType)) {
            AsyncPigeonClient asyncPigeonClient = new AsyncPigeonClient(threadNum, msgCount, msgLength, msgType);
            asyncPigeonClient.test();
        } else if ("thrift".equals(rpcType)) {
            ThriftClient thriftClient = new ThriftClient(threadNum, msgCount, msgLength, msgType);
            thriftClient.test();
        } else if ("asyncmtthrift".equals(rpcType)) {
            AsyncMtthriftClient asyncMtthriftClient = new AsyncMtthriftClient(threadNum, msgCount, msgLength, msgType);
            asyncMtthriftClient.test();
        } else if ("asyncnetty".equals(rpcType)) {
//            AsyncNettyClient asyncNettyClient = new AsyncNettyClient(threadNum, msgCount, msgLength, msgType);
//            asyncNettyClient.test();
        }
//        } else if ("octofuture".equals(rpcType)) {
//            OctoFutureClient octoFutureClient = new OctoFutureClient(threadNum, msgCount, msgLength, msgType);
//            octoFutureClient.test();
//        }
        else if ("octocallback".equals(rpcType)) {
            OctoCallbackClient octoCallbackClient = new OctoCallbackClient(threadNum, msgCount, msgLength, msgType);
            octoCallbackClient.test();
        } else if ("asynchttp".equals(rpcType)) {
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient(threadNum, msgCount, msgLength, maxConnection);
            asyncHttpClient.test();
        } else if("synchttp".equals(rpcType)) {
            SyncHttpClient syncHttpClient = new SyncHttpClient(threadNum, msgCount, msgLength, maxConnection);
            syncHttpClient.test();
        }
    }

}

