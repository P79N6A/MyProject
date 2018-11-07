package com.meituan.service.mobile.mtthrift.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.InetSocketAddress;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/8
 * Time: 10:59
 */
public class NettyChannelFactoryTest {
    private static MockServerBootstrap mockServer;
    private static EventLoopGroup eventLoopGroup;

    @BeforeClass
    public static void init() throws InterruptedException {
        mockServer = new MockServerBootstrap();
    }

//    @Test
    public void test() {
        eventLoopGroup = new NioEventLoopGroup();
        InetSocketAddress remoteAddress = new InetSocketAddress(mockServer.host, mockServer.port);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(remoteAddress)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //do nothing
                    }
                });

//        NettyChannelFactory factory = new NettyChannelFactory(bootstrap, remoteAddress, mockServer.connTimeOutMillis, true);
//        IChannel channel = factory.createChannel();
//        Assert.assertTrue(channel.isAvailable());
    }

    @AfterClass
    public static void destroy() {
        mockServer.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
