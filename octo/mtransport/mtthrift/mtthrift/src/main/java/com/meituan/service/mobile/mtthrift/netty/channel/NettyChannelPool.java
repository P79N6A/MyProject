package com.meituan.service.mobile.mtthrift.netty.channel;

import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.meituan.service.mobile.mtthrift.netty.DefaultClientDecoder;
import com.meituan.service.mobile.mtthrift.netty.DefaultClientEncoder;
import com.meituan.service.mobile.mtthrift.netty.DefaultClientHandler;
import com.meituan.service.mobile.mtthrift.netty.exception.ChannelPoolException;
import com.meituan.service.mobile.mtthrift.netty.exception.NetworkException;
import com.meituan.service.mobile.mtthrift.netty.task.ChannelHeartbeatTask;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.meituan.service.mobile.mtthrift.util.AtomicPositiveInteger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/6
 * Time: 11:52
 */
public class NettyChannelPool implements IChannelPool {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannelPool.class);
    private static final EventLoopGroup WORKER_GROUP = new NioEventLoopGroup(0, new MTDefaultThreadFactory("MtthriftClientNioGroup"));
    private static final int CORE_NUM = Runtime.getRuntime().availableProcessors();
    private static final byte SCANNER_HEARTBEAT_SUPPORT = 2;
    private static final byte BOTH_HEARTBEAT_SUPPORT = 3;

    private boolean remoteUniProto;
    private InetSocketAddress remoteAddress;
    private ICluster cluster;
    private ServerConn serverConn;
    private int connTimeout;

    private final List<IChannel> pooledChannels = new ArrayList<IChannel>();
    private AtomicInteger size = new AtomicInteger();
    private AtomicPositiveInteger selectedIndex = new AtomicPositiveInteger(0);
    private AtomicBoolean isClosed = new AtomicBoolean(true);

    private MTThriftPoolConfig poolConfig;
    private IChannelFactory channelFactory;

    private static ScheduledExecutorService heartbeatExec = Executors.newScheduledThreadPool(CORE_NUM,
            new MTDefaultThreadFactory("NettyChannelPool-heartbeat-thread"));

    public NettyChannelPool(MTThriftPoolConfig poolConfig, ICluster cluster, ServerConn serverConn, int connTimeOut) throws ChannelPoolException {

        isClosed.set(false);
        this.poolConfig = poolConfig;
        this.cluster = cluster;
        this.serverConn = serverConn;
        this.connTimeout = connTimeOut;

        Server server = serverConn.getServer();
        this.remoteUniProto = server.isUnifiedProto();
        this.remoteAddress = new InetSocketAddress(server.getIp(), server.getPort());

        Bootstrap bootstrap = newBootstrap();
        this.channelFactory = new NettyChannelFactory(bootstrap, remoteAddress, connTimeOut);

        init(poolConfig);

        boolean heartbeatSupported = (server.getHeartbeatSupport() == SCANNER_HEARTBEAT_SUPPORT
                || server.getHeartbeatSupport() == BOTH_HEARTBEAT_SUPPORT);
        if (remoteUniProto && heartbeatSupported) {
            heartbeatExec.scheduleWithFixedDelay(
                    new ChannelHeartbeatTask(this),
                    10000 * 4,
                    10000,
                    TimeUnit.MILLISECONDS);

        }
    }

    private Bootstrap newBootstrap() {
        Bootstrap bootstrap = new Bootstrap();

        if (poolConfig.getWriteLowWaterMark() > MTThriftPoolConfig.DEFAULT_WRITE_HIGH_WATER_MARK) {
            bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, poolConfig.getWriteHighWaterMark());
            bootstrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, poolConfig.getWriteLowWaterMark());
        } else {
            bootstrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, poolConfig.getWriteLowWaterMark());
            bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, poolConfig.getWriteHighWaterMark());
        }

        bootstrap.group(WORKER_GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout)
                .remoteAddress(remoteAddress)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline().addLast(
                                new DefaultClientEncoder(),
                                new DefaultClientDecoder(),
                                new DefaultClientHandler());
                    }
                });

        return bootstrap;
    }

    private void init(MTThriftPoolConfig poolConfig) throws ChannelPoolException {

        try {

            for (int i = 0; i < poolConfig.getInitialSize(); i++) {
                selectChannel();
            }

        } catch (ChannelPoolException e) {
            logger.info("[init] unable to create initial connections of pool.", e);
        } catch (NetworkException e) {
            logger.info("[init] unable to create initial connections of pool.", e);
        }
    }

    @Override
    public int getSize() {
        return pooledChannels.size();
    }

    @Override
    public IChannel selectChannel() throws ChannelPoolException {
        if (isClosed()) {
            throw new ChannelPoolException("Channel pool is closed.");
        }

        long now = System.nanoTime();

        long maxWait = (poolConfig.getMaxWait() < 0) ? Long.MAX_VALUE : poolConfig.getMaxWait();

        IChannel channel = null;

        while (channel == null) {

            // create
            if (size.get() < poolConfig.getNormalSize()) {
                if (size.incrementAndGet() > poolConfig.getNormalSize()) {
                    size.decrementAndGet();
                } else {
                    channel = createChannel();
                }
            }

            // random
            if (!pooledChannels.isEmpty()) {
                int selected = selectedIndex.getAndIncrement() % pooledChannels.size();
                IChannel selectedChannel = pooledChannels.get(selected);

                if ((selectedChannel != null && !selectedChannel.isAvailable()) || selectedChannel == null) {
                    removeChannel(selectedChannel);

                    if (selectedChannel != null) {
                        selectedChannel.disConnect();
                    }
                }

                if (selectedChannel != null && selectedChannel.isAvailable() && selectedChannel.isWritable()) {
                    channel = selectedChannel;
                }
            }

            // timeout
            long cost = (System.nanoTime() - now) / 1000000;
            if (cost >= maxWait) {
                throw new ChannelPoolException("select channel time out. Unable to fetch a channel, none available in use."
                        + getChannelPoolDesc() + " cost=" + cost + ", maxWait=" + maxWait);
            }
        }

        return channel;
    }

    public IChannel createChannel() {
        IChannel channel = null;

        try {
            channel = channelFactory.createChannel();
        } finally {
            if (channel != null) {
                synchronized (pooledChannels) {
                    pooledChannels.add(channel);
                }
            } else {
                size.decrementAndGet();
            }
        }

        return channel;
    }

    private void removeChannel(IChannel channel) {
        synchronized (pooledChannels) {
            if (pooledChannels.remove(channel)) {
                size.decrementAndGet();
            }
        }
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {

            for (int index = 0; index < pooledChannels.size(); index++) {
                IChannel pooledChannel = pooledChannels.get(index);

                if (pooledChannel != null && pooledChannel.isAvailable()) {
                    pooledChannel.disConnect();
                }
            }

        }
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    public String getChannelPoolDesc() {
        return "ChannelPool[poolSize=" + pooledChannels.size() + "]";
    }

    public List<IChannel> getChannels() {
        return pooledChannels;
    }

    public boolean isRemoteUniProto() {
        return remoteUniProto;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public ICluster getCluster() {
        return cluster;
    }

    public ServerConn getServerConn() {
        return serverConn;
    }
}
