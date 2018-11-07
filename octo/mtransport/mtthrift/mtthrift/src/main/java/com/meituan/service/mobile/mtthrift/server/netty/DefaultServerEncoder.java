package com.meituan.service.mobile.mtthrift.server.netty;

import com.meituan.service.mobile.mtthrift.netty.metadata.RPCContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-14
 * Time: 下午3:59
 */
public class DefaultServerEncoder extends MessageToByteEncoder<RPCContext> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RPCContext context, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(context.getResponseBytes());
    }

}
