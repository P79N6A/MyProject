package com.meituan.service.mobile.mtthrift.netty;

import com.meituan.service.mobile.mtthrift.netty.metadata.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 2017/7/4
 * Time: 下午10:21
 */
public class DefaultClientEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(rpcRequest.getRequestBytes());
    }
}
