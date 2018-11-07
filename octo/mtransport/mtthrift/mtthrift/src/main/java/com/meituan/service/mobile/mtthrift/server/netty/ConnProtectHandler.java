package com.meituan.service.mobile.mtthrift.server.netty;

import com.meituan.service.mobile.mtthrift.util.TokenBucket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-11-29
 * Time: 下午4:14
 */
public class ConnProtectHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ConnProtectHandler.class);

    private NettyServer server;

    public ConnProtectHandler(NettyServer server) {
        this.server = server;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

        if ((server.getLimitCount() > 0 && server.getLimitSecondsTime() > 0)
                || server.getMaxServerConn() > 0) {

            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIP = socketAddress.getAddress().getHostAddress();

            //同一个IP的链接频率控制
            if (server.getLimitCount() > 0 && server.getLimitSecondsTime() > 0) {
                if (server.getTokenBucketCacheMap().containsKey(clientIP)) {
                    TokenBucket tokenBucket = server.getTokenBucketCacheMap().get(clientIP);
                    if (tokenBucket != null && tokenBucket.limit()) {
                        logger.error("Client(" + clientIP + ") register is high frequency so reject the connection, " + "the server limitCount is "
                                + server.getLimitCount() + " and limitSecondsTime is " + server.getLimitSecondsTime());
                        ctx.close();
                        return;
                    }
                } else {
                    TokenBucket tokenBucket = new TokenBucket(server.getLimitCount(), server.getLimitSecondsTime());
                    server.getTokenBucketCacheMap().put(clientIP, tokenBucket);
                }
            }

            //链接总数控制
            if (server.getMaxServerConn() > 0) {
                if (server.getCtxCacheMap().size() + 1 > server.getMaxServerConn()) {
                    logger.error("Server actual connection more than " + server.getMaxServerConn() +
                            ", so reject current Conn(" + socketAddress.toString() + ")");
                    ctx.close();
                    return;
                } else {
                    server.getCtxCacheMap().put(socketAddress.toString(), ctx);
                }
            }
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        if (socketAddress != null) {
            server.getCtxCacheMap().remove(socketAddress.toString());
        }
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

}
