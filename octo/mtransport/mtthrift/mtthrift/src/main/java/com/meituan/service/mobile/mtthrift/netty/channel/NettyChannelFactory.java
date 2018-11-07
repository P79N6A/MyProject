package com.meituan.service.mobile.mtthrift.netty.channel;

import io.netty.bootstrap.Bootstrap;

import java.net.SocketAddress;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/6
 * Time: 13:51
 */
public class NettyChannelFactory implements IChannelFactory {

    private Bootstrap bootstrap;
    private SocketAddress remoteAddress;
    private long connTimeOutMillis;

    public NettyChannelFactory(Bootstrap bootstrap, SocketAddress remoteAddress, int connTimeOut) {
        this.bootstrap = bootstrap;
        this.remoteAddress = remoteAddress;
        this.connTimeOutMillis = connTimeOut;
    }

    @Override
    public IChannel createChannel() {
        NettyChannel customizedChannel = new NettyChannel(bootstrap, remoteAddress, connTimeOutMillis);
        customizedChannel.connect();
        return customizedChannel;
    }
}
