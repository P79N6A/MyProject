package com.meituan.service.mobile.mtthrift.server.netty;

import com.meituan.service.mobile.mtthrift.auth.*;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.netty.metadata.RPCContext;
import com.meituan.service.mobile.mtthrift.netty.metadata.RequestType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-11-28
 * Time: 下午4:52
 */
public class ChannelAuthHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServerHandler.class);

    public static final AttributeKey<Boolean> CHANNEL_AUTH_KEY = AttributeKey.valueOf("channel.auth");

    private IAuthHandler authHandler;

    private long lastAuthTimestamp = 0;

    public ChannelAuthHandler(NettyServer server) {
        this.authHandler = server.getAuthHandler();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Attribute<Boolean> attr = ctx.channel().attr(CHANNEL_AUTH_KEY);
        attr.set(false);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!ThriftServerGlobalConfig.isEnableAuth()) {
            super.channelRead(ctx, msg);
            return;
        }

        Attribute<Boolean> attr = ctx.channel().attr(CHANNEL_AUTH_KEY);

        if (authHandler != null && authHandler.getAuthType().equals(AuthType.channelAuth)) {
            if (attr.get() && (AuthClock.currentTimeMillis() - lastAuthTimestamp) < ThriftServerGlobalConfig.getChannelAuthTimeIntervalMillis()) {
                super.channelRead(ctx, msg);
                return;
            }

            lastAuthTimestamp = AuthClock.currentTimeMillis();

            RPCContext context = (RPCContext) msg;
            boolean authResult = false;

            if (RequestType.scannerHeartbeat.equals(context.getRequestType())) {
                authResult = false;
            } else if (RequestType.oldProto.equals(context.getRequestType())) {
                authResult = false;
            } else if (RequestType.unifiedProto.equals(context.getRequestType())) {
                String clientAppkey = null;
                String signature = null;
                String uniformSignInfo = null;
                if (context.getHeader() != null) {
                    Map<String, String> tempMap = context.getHeader().getLocalContext();
                    if (tempMap != null) {
                        clientAppkey = tempMap.get(AuthUtil.APPKEY);
                        signature = tempMap.get(AuthUtil.SIGNATURE);
                        uniformSignInfo = tempMap.get(AuthUtil.INF_RPC_AUTH);
                    }
                    if (clientAppkey == null && context.getHeader().getTraceInfo() != null) {
                        clientAppkey = context.getHeader().getTraceInfo().getClientAppkey();
                    }
                }

                InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                String clientIP = socketAddress.getAddress().getHostAddress();
                AuthMetaData authMetaData = new AuthMetaData();
                authMetaData.setClientIp(clientIP);
                authMetaData.setClientAppkey(clientAppkey);
                authMetaData.setSignature(signature);
                authMetaData.setUniformSignInfo(uniformSignInfo);
                authResult = authHandler.auth(authMetaData);
            }

            if (authResult) {
                attr.set(true);
                context.setAuthSuccess(true);
            } else {
                attr.set(false);
                context.setAuthSuccess(false);
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

}
