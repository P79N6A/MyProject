package com.meituan.service.mobile.mtthrift.server.http;

import com.meituan.service.mobile.mtthrift.server.http.handler.HttpHandler;
import com.meituan.service.mobile.mtthrift.server.http.meta.DefaultHttpResponse;
import com.meituan.service.mobile.mtthrift.util.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/2
 */
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServerHandler.class);

    private HttpRequest request;
    private HttpHandler httpHandler;
    private NettyHttpServer server;
    private byte[] frontBuff;

    public NettyHttpServerHandler(HttpHandler httpHandler, NettyHttpServer server) {
        this.httpHandler = httpHandler;
        this.server = server;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
        }
        if (msg instanceof HttpContent) {
            if ("/favicon.ico".equals(request.getUri())) {
                return;
            }
            if (msg instanceof LastHttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                byte[] buffer = new byte[buf.readableBytes()];
                buf.readBytes(buffer);
                buf.release();

                byte[] fullBuff;
                if (frontBuff != null) {
                    fullBuff = new byte[frontBuff.length + buffer.length];
                    System.arraycopy(frontBuff, 0, fullBuff, 0, frontBuff.length);
                    System.arraycopy(buffer, 0, fullBuff, frontBuff.length, buffer.length);
                    frontBuff = null;
                } else {
                    fullBuff = buffer;
                }
                NettyHttpSender httpSender = new NettyHttpSender(ctx.channel(), request);
                try {
                    httpHandler.handle(httpSender, request.getUri(), fullBuff);
                } catch (Throwable e) {
                    String errorMsg = e.getMessage();
                    errorMsg = errorMsg == null ? e.getClass().getName() : errorMsg;
                    DefaultHttpResponse httpResponse = new DefaultHttpResponse();
                    httpResponse.generateFailContent(errorMsg);
                    httpSender.send(httpResponse);
                }
            } else {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                byte[] buffer = new byte[buf.readableBytes()];
                buf.readBytes(buffer);
                buf.release();

                if (frontBuff == null) {
                    frontBuff = buffer;
                } else {
                    byte[] newBuff = new byte[frontBuff.length + buffer.length];
                    System.arraycopy(frontBuff, 0, newBuff, 0, frontBuff.length);
                    System.arraycopy(buffer, 0, newBuff, frontBuff.length, buffer.length);
                    frontBuff = newBuff;
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        server.getChannels().put(NetUtil.toIpPort((InetSocketAddress) ctx.channel().remoteAddress()), channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        server.getChannels().remove(NetUtil.toIpPort((InetSocketAddress) ctx.channel().remoteAddress()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Netty http request fail", cause);
        ctx.close();
    }
}
