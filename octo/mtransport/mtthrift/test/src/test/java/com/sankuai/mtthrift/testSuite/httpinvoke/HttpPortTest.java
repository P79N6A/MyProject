package com.sankuai.mtthrift.testSuite.httpinvoke;

import com.meituan.dorado.common.RpcRole;
import com.meituan.service.mobile.mtthrift.server.http.NettyHttpServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpPortTest {

    @Test
    public void testPortBind() {
        try {
            Class<?>[] params = new Class[1];
            params[0] = RpcRole.class;
            Constructor<NettyHttpServer> constructor = NettyHttpServer.class.getDeclaredConstructor(params);
            constructor.setAccessible(true);
            Field startedField = NettyHttpServer.class.getDeclaredField("started");
            startedField.setAccessible(true);
            NettyHttpServer httpServer = constructor.newInstance(RpcRole.INVOKER);
            startedField.set(httpServer, false);

            params[0] = ServerBootstrap.class;
            Method bindPortMethod = NettyHttpServer.class.getDeclaredMethod("bindPort", params);
            bindPortMethod.setAccessible(true);

            concurrentBindPort(httpServer, bindPortMethod);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void concurrentBindPort(final NettyHttpServer httpServer, final Method bindPortMethod) {
        final ServerBootstrap bootstrap = genServerBootstrap();

        int concurrentCount = 50;
        ExecutorService executorService = new ThreadPoolExecutor(concurrentCount, concurrentCount, 0, TimeUnit.MICROSECONDS, new SynchronousQueue<Runnable>());
        for (int i = 0; i < concurrentCount; i++) {
            final int finalI = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Object[] objects = new Object[1];
                    objects[0] = bootstrap;
                    try {
                        bindPortMethod.invoke(httpServer, objects);
                        System.out.println("第几次 " + finalI);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail();
                    }
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ServerBootstrap genServerBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("nettyHttpServerBossGroup"));
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(0,
                new DefaultThreadFactory("nettyHttpServerWorkerGroup"));
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("decoder", new HttpRequestDecoder())
                                .addLast("encoder", new HttpResponseEncoder());
                    }
                }).option(ChannelOption.SO_BACKLOG, 128);
        return bootstrap;
    }
}
