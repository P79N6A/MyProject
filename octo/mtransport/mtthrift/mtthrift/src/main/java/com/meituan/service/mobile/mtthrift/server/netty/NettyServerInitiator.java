package com.meituan.service.mobile.mtthrift.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.apache.thrift.TProcessor;

import java.util.HashMap;
import java.util.Map;

public class NettyServerInitiator extends ChannelInitializer<SocketChannel> {

    private TProcessor processor = null;
    private NettyServer server;
    private Map<String, TProcessor> serviceProcessorMap = new HashMap<String, TProcessor>();
    private int maxRequestMessageBytes;

    public NettyServerInitiator(Map<String, TProcessor> serviceProcessorMap, TProcessor processor, NettyServer server) {
        this.serviceProcessorMap = serviceProcessorMap;
        this.processor = processor;
        this.server = server;
    }

    public NettyServerInitiator(Map<String, TProcessor> serviceProcessorMap, TProcessor processor, NettyServer server, int maxRequestMessageBytes) {
        this.serviceProcessorMap = serviceProcessorMap;
        this.processor = processor;
        this.server = server;
        this.maxRequestMessageBytes = maxRequestMessageBytes;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        final ChannelPipeline pipe = ch.pipeline();

        pipe.addLast("connProtect", new ConnProtectHandler(server))
                .addLast("decoder", new DefaultServerDecoder(server, maxRequestMessageBytes))
                .addLast("channelAuth", new ChannelAuthHandler(server))
                .addLast("encoder", new DefaultServerEncoder())
                .addLast("handler", new DefaultServerHandler(serviceProcessorMap, processor, server));
    }


}
