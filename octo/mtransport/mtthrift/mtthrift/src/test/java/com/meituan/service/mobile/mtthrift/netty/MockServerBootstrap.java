package com.meituan.service.mobile.mtthrift.netty;

import com.meituan.service.mobile.mtthrift.util.Consts;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/8
 * Time: 11:03
 */
public class MockServerBootstrap {
    final int port = 9901;
    final String host = ProcessInfoUtil.getLocalIpV4();
    final int connTimeOutMillis = Consts.getConnectTimeout;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private ChannelFuture future;

    MockServerBootstrap() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventLoopGroup, eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new EmptyChannelInitializer());
        future = serverBootstrap.bind(host, port).sync();
    }

    public void shutdown() {
        try {
            future.channel().close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        eventLoopGroup.shutdownGracefully();

    }

    static final class EmptyChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            //do nothing
        }
    }
}
