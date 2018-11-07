package com.meituan.service.mobile.mtthrift.netty;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/8
 * Time: 10:11
 */
public class NettyChannelPoolTest {
    private static final Logger log = LoggerFactory.getLogger(NettyChannelPoolTest.class);
    private static MockServerBootstrap mockServer;

    @BeforeClass
    public static void init() throws InterruptedException {
        mockServer = new MockServerBootstrap();
    }

    @Test
    public void test() {
//        MTThriftPoolConfig config = new MTThriftPoolConfig();
//        config.setInitialSize(10);
//        config.setNormalSize(10);
//        InetSocketAddress remoteAddress = new InetSocketAddress(mockServer.host, mockServer.port);
//        NettyChannelPool channelPool = new NettyChannelPool(config, null, null, mockServer.connTimeOutMillis);
//        log.info(channelPool.getChannelPoolDesc());
//        System.out.println(channelPool.getChannelPoolDesc());
//        Assert.assertEquals(config.getInitialSize(), channelPool.getSize());
//        Assert.assertFalse(channelPool.isClosed());
//
//        for (int i = 0; i < config.getInitialSize(); i++) {
//            channelPool.selectChannel();
//        }
//        Assert.assertEquals(config.getInitialSize(), channelPool.getSize());
//        IChannel channel = channelPool.createChannel();
//        Assert.assertTrue(channel.isAvailable());
//        channelPool.close();
//        Assert.assertTrue(channelPool.isClosed());
    }

    @AfterClass
    public static void destroy() {
       mockServer.shutdown();
    }
}
