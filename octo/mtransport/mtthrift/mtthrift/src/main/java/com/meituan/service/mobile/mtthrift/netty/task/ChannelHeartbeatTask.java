package com.meituan.service.mobile.mtthrift.netty.task;

import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.netty.channel.IChannel;
import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannel;
import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannelPool;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.sankuai.octo.protocol.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.apache.thrift.transport.TIOStreamTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/7/5
 * Time: 11:17
 */
public class ChannelHeartbeatTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ChannelHeartbeatTask.class);

    private final NettyChannelPool channelPool;

    public ChannelHeartbeatTask(NettyChannelPool pool) {
        this.channelPool = pool;
    }

    @Override
    public void run() {
        boolean allFailed = heartbeatChannel();
        notifyServerConnStateChanged(allFailed);
    }

    private boolean heartbeatChannel() {
        List<IChannel> channels = this.channelPool.getChannels();

        boolean allFailed = true;

        if (channels != null) {

            for (int index = 0; index < channels.size(); index++) {

                IChannel channel = channels.get(index);
                if (channel != null) {
                    try {
                        if (channel.isAvailable() && channelPool.isRemoteUniProto()) {
                            boolean isSuccess = sendHeartBeat(((NettyChannel) channel));

                            if (isSuccess) {
                                allFailed = false;
                            }
                        }

                    } catch (Exception e) {
                        logger.warn("[run] heartbeat failed. Channel" + channel, e);
                    }
                }
            }

        }
        return allFailed;
    }

    private void notifyServerConnStateChanged(boolean allFailed) {
        ServerConn serverConn = channelPool.getServerConn();
        Server server = serverConn.getServer();
        ICluster cluster = channelPool.getCluster();

        if (allFailed) {
            server.addSocketNullNum();
        } else if (serverConn.getServer().getSocketNullNum() != 0) {
            server.resetSocketNullNum();
            logger.info("[notifyServerConnStateChanged]heartbeat resetSocketNullNum, server address:{}",
                    channelPool.getRemoteAddress());
        }

        if (server.getSocketNullNum() >= 3) {
            if (ThriftClientGlobalConfig.isHeartbeatAutoDegrade()) {
                server.resetSocketNullNum();
                server.degrade();
                cluster.updateServerConn(serverConn);
                logger.info("[notifyServerConnStateChanged]heartbeat degrade, server address:{}",
                        channelPool.getRemoteAddress());
            }
        }
    }

    public boolean sendHeartBeat(NettyChannel channel) {
        return sendHeartRequest(channel.getChannel());
    }

    private boolean sendHeartRequest(Channel channel) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        TIOStreamTransport transport = new TIOStreamTransport(bos);
        CustomizedTFramedTransport tFramedTransport = new CustomizedTFramedTransport(transport);
        tFramedTransport.setUnifiedProto(true);
        tFramedTransport.setServiceName("channelHeartbeat");
        tFramedTransport.setMessageType(MessageType.ScannerHeartbeat);
        tFramedTransport.setProtocol(Consts.protocol);

        boolean alive = false;
        long seqId = -1;
        try {
            tFramedTransport.flush();
            seqId = tFramedTransport.getSequenceId();
            Future<Boolean> future = ContextStore.createHeartbeatRequest(seqId);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bos.toByteArray());
            channel.writeAndFlush(byteBuf);
            alive = future.get(getHeartbeatTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.warn("time out during heartbeat, heartbeat failed. seq:{}, timeout:{}, remote address:{}",
                    seqId, getHeartbeatTimeout(), channel.remoteAddress());
        } catch (Exception e) {
            logger.warn("exception caught during heartbeat, heartbeat failed. seq:{}, remote address:{}, message:{}",
                    seqId, channel.remoteAddress(), e.getMessage());
        } finally {
            ContextStore.removeHeartbeatRequestFuture(seqId);
        }
        return alive;
    }

    private long getHeartbeatTimeout() {
        return 3000;
    }
}
