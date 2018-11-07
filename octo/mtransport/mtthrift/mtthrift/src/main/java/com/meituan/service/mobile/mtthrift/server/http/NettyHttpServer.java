package com.meituan.service.mobile.mtthrift.server.http;

import com.meituan.dorado.common.RpcRole;
import com.meituan.service.mobile.mtthrift.server.http.handler.HttpCheckHandler;
import com.meituan.service.mobile.mtthrift.server.http.handler.HttpHandler;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/2
 */
public class NettyHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    private static volatile int port;
    private static volatile boolean started;
    private static volatile NettyHttpServer httpServer;
    private static final int PORT_BIND_RETRY_TIMES = 10;

    private HttpHandler httpHandler;
    private InetSocketAddress localAddress;
    private volatile ShutDownHook _hook;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerChannel serverChannel;

    private Random random = new Random();

    /**
     * <ip:port, channel>
     */
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    public static NettyHttpServer buildHttpServer(RpcRole rpcRole) {
        if (httpServer != null) {
            httpServer.httpHandler.setRole(rpcRole);
            return httpServer;
        }
        synchronized (NettyHttpServer.class) {
            if (httpServer == null) {
                httpServer = new NettyHttpServer(rpcRole);
            }
        }
        return httpServer;
    }

    private NettyHttpServer(RpcRole rpcRole) {
        this.httpHandler = new HttpCheckHandler();
        httpHandler.setRole(rpcRole);
        start();
    }

    private void start() {
        try {
            if (Epoll.isAvailable()) {
                logger.info("NettyHttpServer use EpollEventLoopGroup!");
                bossGroup = new EpollEventLoopGroup(1, new DefaultThreadFactory("nettyHttpServerBossGroup"));
                workerGroup = new EpollEventLoopGroup(2,
                        new DefaultThreadFactory("nettyHttpServerWorkerGroup"));
            } else {
                bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("nettyHttpServerBossGroup"));
                workerGroup = new NioEventLoopGroup(2,
                        new DefaultThreadFactory("nettyHttpServerWorkerGroup"));
            }

            bootstrap = new ServerBootstrap();
            final NettyHttpServer httpServer = this;
            bootstrap.group(bossGroup, workerGroup)
                    .channel(workerGroup instanceof EpollEventLoopGroup ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("decoder", new HttpRequestDecoder())
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("handler", new NettyHttpServerHandler(httpHandler, httpServer));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128);
            bindPort(bootstrap);
            started = true;
        } catch (Throwable e) {
            logger.error("NettyHttpServer start failed", e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void bindPort(ServerBootstrap bootstrap) throws Throwable {
        int bindCount = 0;
        while (!started) {
            try {
                bindCount++;
                if (bindCount == 1) {
                    port = NetUtil.getAvailablePort(Consts.DEFAULT_HTTP_SERVER_PORT);
                } else {
                    // 测试该重试策略, 50个进程并发没有问题
                    try {
                        // 随机等待 减少并发冲突
                        Thread.sleep(random.nextInt(100));
                    } catch (InterruptedException e) {
                    }
                    if (bindCount <= PORT_BIND_RETRY_TIMES / 2) {
                        port = NetUtil.getAvailablePort(port++);
                    } else {
                        // 重试五次后都失败, 则端口尝试增加随机值避免冲突
                        port = NetUtil.getAvailablePort(port + random.nextInt(10));
                    }
                }
                localAddress = new InetSocketAddress(port);

                ChannelFuture channelFuture = bootstrap.bind(localAddress);
                channelFuture.syncUninterruptibly();
                serverChannel = (ServerChannel) channelFuture.channel();
                logger.info("Start NettyHttpServer bind {}", localAddress);
                break;
            } catch (Throwable e) {
                if (bindCount > PORT_BIND_RETRY_TIMES) {
                    throw e;
                }
                logger.info("NettyHttpServer bind {} failed, will do {} retry, errorMsg: {}", localAddress, bindCount, e.getMessage());
            }
        }
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public synchronized void addShutDownHook() {
        if (_hook == null) {
            _hook = new ShutDownHook(this);
            Runtime.getRuntime().addShutdownHook(_hook);
        }
    }

    public synchronized void shutdown() {
        if (!started) {
            return;
        }
        logger.info("Close NettyHttpServer bind {}", localAddress);
        try {
            if (serverChannel != null) {
                // unbind.
                serverChannel.close();
            }
        } catch (Throwable e) {
            logger.warn("Http ServerChannel close failed", e);
        }

        try {
            Set<Channel> channels = getConnectedChannels();
            if (channels != null && channels.size() > 0) {
                for (Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                        logger.warn("Http connected channel close failed", e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("Http connected channels close failed", e);
        }

        try {
            if (bootstrap != null) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            logger.warn("Http NioWorkerGroup shutdown failed", e);
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn("Http clear channels failed", e);
        }
        started = false;
        httpServer = null;
    }

    public Set<Channel> getConnectedChannels() {
        Set<Channel> chs = new HashSet<Channel>();
        for (Channel channel : this.channels.values()) {
            if (channel.isActive()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtil.toIpPort((InetSocketAddress) channel.remoteAddress()));
            }
        }
        return chs;
    }

    public ConcurrentMap<String, Channel> getChannels() {
        return channels;
    }

    public static NettyHttpServer getHttpServer() {
        return httpServer;
    }

    class ShutDownHook extends Thread {
        private NettyHttpServer _server;

        public ShutDownHook(NettyHttpServer server) {
            this._server = server;
        }

        @Override
        public void run() {
            _hook = null;
            _server.shutdown();
        }
    }
}
