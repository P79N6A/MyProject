package com.sankuai.octo.dorado.core.server;

import com.sankuai.octo.dorado.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RPCServer {
    static {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }

    private static HashMap<String, Object> objects = new HashMap<String, Object>();
    private static BetterExecutorService threadPool;
    private final Logger logger = LoggerFactory.getLogger(RPCServer.class);
    private int port = Constants.port;
    private int backlog = Constants.backlog;
    private int ioThreadNum = Constants.ioThreadNum;

    public RPCServer(List<String> objClazz) throws Exception {

        List<String> objClassList = objClazz;
        logger.info("Object list:");
        for (String objClass : objClassList) {
            Object obj = RPCServer.class.forName(objClass).newInstance();
            Class[] interfaces = obj.getClass().getInterfaces();

            for (int i = 0; i < interfaces.length; i++) {
                objects.put(interfaces[i].getName(), obj);
                logger.info("   " + interfaces[i].getName());
            }
        }
    }

    public static Object getObject(String objName) {
        return objects.get(objName);
    }

    public static void submit(Runnable task) {
        if (threadPool == null) {
            synchronized (BaseObjectProxy.class) {
                if (threadPool == null) {
                    LinkedBlockingDeque<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>();
                    ThreadPoolExecutor executor = new ThreadPoolExecutor(0,
                            Integer.MAX_VALUE, 600L, TimeUnit.SECONDS,
                            new SynchronousQueue<Runnable>());
                    threadPool = new BetterExecutorService(linkedBlockingDeque,
                            executor, "Server async thread pool",
                            Constants.asyncThreadPoolSize);
                }
            }
        }

        threadPool.submit(task);
    }

    public static void main(String[] args) throws Exception {
        new RPCServer(
                Arrays.asList("com.sankuai.octo.jtransport.core.IHelloWordObj"))
                .run();
    }

    public void run() throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup(
                this.ioThreadNum);
        try {

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new DefaultServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, this.backlog)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            Channel ch = b.bind(port).sync().channel();

            logger.info("NettyRPC server listening on port " + port
                    + " and ready for connections...");

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {

                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();

                }
            });
            ch.closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
