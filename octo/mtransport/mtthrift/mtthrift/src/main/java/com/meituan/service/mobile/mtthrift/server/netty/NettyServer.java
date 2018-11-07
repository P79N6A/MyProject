package com.meituan.service.mobile.mtthrift.server.netty;

import com.meituan.service.mobile.mtthrift.auth.AuthClock;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.falcon.Collector;
import com.meituan.service.mobile.mtthrift.falcon.model.WorkerThreadMonitor;
import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.meituan.service.mobile.mtthrift.server.MTTServer;
import com.meituan.service.mobile.mtthrift.util.MTTThreadedSelectorWorkerExcutorUtil;
import com.meituan.service.mobile.mtthrift.util.TokenBucket;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.sgagent.thrift.model.CustomizedStatus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/9/9
 * Description:
 */
public class NettyServer extends MTTServer {

    private final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Map<String, ChannelHandlerContext> ctxCacheMap = new ConcurrentHashMap<String, ChannelHandlerContext>();
    private Map<String, TokenBucket> tokenBucketCacheMap = new ConcurrentHashMap<String, TokenBucket>();

    public final int backlog = 1024;
    private BlockingQueue<Runnable> threadPoolQueue;
    private ThreadPoolExecutor threadPool;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel ch;
    private int status = CustomizedStatus.ALIVE.getValue();

    public NettyServer(int port) throws Exception {
        super(port);
    }

    @Override
    public void run(boolean daemon) throws Exception {
        initThreadPool();
        if (Epoll.isAvailable()) {
            logger.info("Netty Server use EpollEventLoopGroup!");
            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup(selectorThreads);
        } else {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(selectorThreads);
        }

        long updateInterval = ThriftServerGlobalConfig.getChannelAuthTimeIntervalMillis();

        workerGroup.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                AuthClock.updateNow();
            }
        }, updateInterval, updateInterval, TimeUnit.MILLISECONDS);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(workerGroup instanceof EpollEventLoopGroup ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitiator(serviceProcessorMap, tprocessor, this, (int) this.maxReadBufferBytes))
                    .option(ChannelOption.SO_BACKLOG, this.backlog)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true);

            if (ThriftServerGlobalConfig.isEnablePooledByteBuf()) {
                b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            }

            ch = b.bind(port).sync().channel();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        init();
        WorkerThreadMonitor workerThreadMonitor = new WorkerThreadMonitor(LocalPointConf.getAppIp(), getAppKey(), this.port, threadPool);
        Collector.getWorkerThreadMonitorMap().put(getAppKey() + this.port, workerThreadMonitor);
    }

    private void initThreadPool() {
        String threadPoolName = "mtthrift-workThread-" + port;
        if (serviceInterface != null) {
            threadPoolName = serviceInterface.getName();
        }
        if (threadPoolQueue != null) {
            threadPool = MTTThreadedSelectorWorkerExcutorUtil.
                    getWorkerExecutorWithQueue(workerThreads, maxWorkerThreads, threadPoolQueue,
                            new MTDefaultThreadFactory(threadPoolName));
        } else {
            threadPool = MTTThreadedSelectorWorkerExcutorUtil.
                    getWorkerExecutorWithQueue(workerThreads, maxWorkerThreads, workQueueSize,
                            new MTDefaultThreadFactory(threadPoolName));
        }
    }

    public void submit(Runnable task) {
        threadPool.execute(task);
    }

    public void shutdown() {
        if (serverStatus == ServerStatus.START) {
            serverStatus = ServerStatus.STOPPING;
            String info = String.format("stopping netty server(%s:%d), sleep %d seconds!",
                    appKey, port, shutdownWaitTime);
            logger.info(info);
            try {
                this.setStatus(CustomizedStatus.DEAD.getValue());
                MnsInvoker.unRegisterThriftService(appKey, port);
            } catch (TException e) {
                logger.debug(e.getMessage(), e);
            }
            try {
                Thread.sleep(this.shutdownWaitTime * 1000L);
            } catch (InterruptedException e) {
                logger.error("exception while waiting " + this.shutdownWaitTime +
                        " seconds to close thrift server", e);
            }
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            threadPool.shutdown();
            serverStatus = ServerStatus.STOPPED;
        }
        // 进程退出时shutdown会执行两次，保证二次执行时等待Server停止
        while (serverStatus == ServerStatus.STOPPING) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("exception while waiting thrift server stopped", e);
            }
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, ChannelHandlerContext> getCtxCacheMap() {
        return ctxCacheMap;
    }

    public void setCtxCacheMap(Map<String, ChannelHandlerContext> ctxCacheMap) {
        this.ctxCacheMap = ctxCacheMap;
    }

    public Map<String, TokenBucket> getTokenBucketCacheMap() {
        return tokenBucketCacheMap;
    }

    public void setTokenBucketCacheMap(Map<String, TokenBucket> tokenBucketCacheMap) {
        this.tokenBucketCacheMap = tokenBucketCacheMap;
    }

    public Executor getExecutor(String serviceName, String methodName) {
        Executor executor;
        String key = serviceName + "#" + methodName;
        executor = executorMap.get(key);
        if (executor == null) {
            executor = executorMap.get(serviceName);
            if (executor == null) {
                executor = threadPool;
            }
        }
        return executor;
    }

    public int getPort() {
        return this.port;
    }

    public BlockingQueue<Runnable> getThreadPoolQueue() {
        return threadPoolQueue;
    }

    public void setThreadPoolQueue(BlockingQueue<Runnable> threadPoolQueue) {
        this.threadPoolQueue = threadPoolQueue;
    }
}
