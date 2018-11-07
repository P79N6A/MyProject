package com.meituan.service.mobile.mtthrift.server.netty;

import com.meituan.service.mobile.mtthrift.netty.metadata.RPCContext;
import com.meituan.service.mobile.mtthrift.netty.metadata.RequestType;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.LoadInfoUtil;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.HeartbeatInfo;
import com.sankuai.octo.protocol.LoadInfo;
import com.sankuai.octo.protocol.MessageType;
import com.sankuai.sgagent.thrift.model.ConfigStatus;
import com.sankuai.sgagent.thrift.model.CustomizedStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.thrift.transport.TIOStreamTransport;

import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-14
 * Time: 下午3:58
 */
public class DefaultServerDecoder extends ByteToMessageDecoder {

    private NettyServer server;
    private int maxRequestMessageBytes;

    public DefaultServerDecoder(NettyServer server) {
        this.server = server;
    }

    public DefaultServerDecoder(NettyServer server, int maxRequestMessageBytes) {
        this.server = server;
        this.maxRequestMessageBytes = maxRequestMessageBytes;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {

        if (byteBuf.readableBytes() < 10)
            return;

        byte[] intactBytes = new byte[byteBuf.readableBytes()];
        Unpooled.wrappedBuffer(byteBuf).readBytes(intactBytes);

        ByteBufInputStream inputStream = new ByteBufInputStream(byteBuf);
        TIOStreamTransport transport = new TIOStreamTransport(inputStream);
        CustomizedTFramedTransport customizedTFramedTransport = new CustomizedTFramedTransport(transport, maxRequestMessageBytes);

        int readerIndex = byteBuf.readerIndex();
        int bodyLength;
        byte[] first4bytes = new byte[4];
        byteBuf.markReaderIndex();
        byteBuf.getBytes(readerIndex, first4bytes);
        if (first4bytes[0] == Consts.first && first4bytes[1] == Consts.second) {
            //统一协议
            byte[] i32buf = new byte[4];
            byteBuf.getBytes(readerIndex + 4, i32buf);
            bodyLength = CustomizedTFramedTransport.decodeFrameSize(i32buf, 0, 4);

            if (bodyLength < 0)
                ctx.close();

            if (byteBuf.readableBytes() < bodyLength + 8) {
                byteBuf.resetReaderIndex();
                return;
            }

            customizedTFramedTransport.readFrame();

            Header header = customizedTFramedTransport.getHeaderInfo();
            byte messageType = header.messageType;
            if (messageType == MessageType.ScannerHeartbeat.getValue()) {
                //Scanner心跳协议请求
                HeartbeatInfo heartbeatInfo = genScannerHeartbeatInfo();
                RPCContext context = new RPCContext();
                context.setRequestType(RequestType.scannerHeartbeat);
                context.setSeq(header.getRequestInfo().getSequenceId());
                context.setHeartbeatInfo(heartbeatInfo);
                context.setUnifiedProto(true);
                list.add(context);
            } else {
                //统一协议请求
                byte[] bytes = customizedTFramedTransport.getReadBuffer_().getBuffer();
                RPCContext context = new RPCContext();
                context.setRequestType(RequestType.unifiedProto);
                context.setSeq(header.getRequestInfo().getSequenceId());
                context.setThriftRequestData(bytes);
                context.setUnifiedProto(true);
                context.setHeader(header);
                context.setRequestSize(customizedTFramedTransport.getResponseSize());
                context.setIntactBytes(intactBytes);
                list.add(context);
            }
        } else {
            //原生Thrift协议请求
            bodyLength = CustomizedTFramedTransport.decodeFrameSize(first4bytes, 0, 4);

            if (bodyLength < 0) {
                ctx.close();
            }

            if (byteBuf.readableBytes() < bodyLength + 4) {
                byteBuf.resetReaderIndex();
                return;
            }

            customizedTFramedTransport.readFrame();

            byte[] bytes = customizedTFramedTransport.getReadBuffer_().getBuffer();
            RPCContext context = new RPCContext();
            context.setRequestType(RequestType.oldProto);
            context.setThriftRequestData(bytes);
            context.setUnifiedProto(false);
            context.setRequestSize(customizedTFramedTransport.getResponseSize());
            list.add(context);
        }
    }


    private HeartbeatInfo genScannerHeartbeatInfo() {
        LoadInfo loadInfo = new LoadInfo();
        loadInfo.setAverageLoad(LoadInfoUtil.getAvgLoad());
        loadInfo.setOldGC(LoadInfoUtil.getOldGcCount());
        loadInfo.setMethodQpsMap(LoadInfoUtil.getQpsMap());
        HeartbeatInfo heartbeatInfo = new HeartbeatInfo();
        heartbeatInfo.setAppkey(server.getAppKey());
        long currentTimeUs = System.nanoTime() / 1000;
        heartbeatInfo.setSendTime(currentTimeUs);
        heartbeatInfo.setLoadInfo(loadInfo);
        if (server.getStatus() == CustomizedStatus.DEAD.getValue()) {
            heartbeatInfo.setStatus(server.getStatus());
        } else {
            ConfigStatus configStatus = server.getConfigStatus();
            CustomizedStatus runtimeStatus = CustomizedStatus.ALIVE;
            if (configStatus != null) {
                runtimeStatus = configStatus.getRuntimeStatus();
            }
            heartbeatInfo.setStatus(runtimeStatus.getValue());
        }
        return heartbeatInfo;
    }

}
