package com.meituan.control.zookeeper.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class NettyHttpServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
        ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
        ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ch.pipeline().addLast("http-nettyserver", new NettyHttpServerHandler());
    }
}
