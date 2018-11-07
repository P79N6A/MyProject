package com.meituan.service.mobile.mtthrift.netty.channel;

import com.meituan.service.mobile.mtthrift.netty.exception.NetworkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;
import java.util.concurrent.Future;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/1/17
 * Time: 15:41
 */
public class NettyChannel implements IChannel {
    private Bootstrap bootstrap;
    private Channel channel;
    private SocketAddress remoteAddress;
    private long connTimeOutMillis;

    public NettyChannel(Bootstrap bootstrap, SocketAddress remoteAddress, long connTimeOutMillis) {
        this.bootstrap = bootstrap;
        this.remoteAddress = remoteAddress;
        this.connTimeOutMillis = connTimeOutMillis;
    }

    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public boolean isAvailable() {
        return this.channel != null && this.channel.isActive();
    }

    @Override
    public boolean isWritable() {
        return this.channel != null && this.channel.isWritable();
    }

    @Override
    public void disConnect() {
        try {
            this.channel.close().sync();
            this.channel = null;
        } catch (Exception e) {
            throw new NetworkException("disconnect to remote " + remoteAddress + " failed.", e);
        }
    }

    @Override
    public void connect() {
        ChannelFuture future = bootstrap.connect();
        try {
            if (future.awaitUninterruptibly(connTimeOutMillis) && future.isSuccess()) {
                this.channel = future.channel();
            } else {
                throw new NetworkException("connected to remote " + remoteAddress + " failed.");
            }
        } catch (Exception e) {
            throw new NetworkException("connected to remote " + remoteAddress + " failed.", e);
        } finally {
            if (future.channel() != null && !future.channel().isActive()) {
                future.cancel(false);
            }
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Future write(byte[] bytes) {
        return channel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NettyChannel{");
        sb.append("bootstrap=").append(bootstrap);
        sb.append(", channel=").append(channel);
        sb.append(", remoteAddress=").append(remoteAddress);
        sb.append(", connTimeOutMillis=").append(connTimeOutMillis);
        sb.append('}');
        return sb.toString();
    }
}
