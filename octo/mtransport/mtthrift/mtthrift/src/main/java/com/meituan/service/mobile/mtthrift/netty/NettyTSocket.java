package com.meituan.service.mobile.mtthrift.netty;

import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannel;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcRequest;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/6/27
 * Time: 15:39
 */
public class NettyTSocket extends TIOStreamTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyTSocket.class);

    private NettyChannel nettyChannel;
    private RpcRequest request;
    private ByteArrayOutputStream byteArrayOutputStream;

    public NettyTSocket(NettyChannel socket, RpcRequest request) throws TTransportException {
        this.nettyChannel = socket;
        this.request = request;

        if (isOpen()) {
            byteArrayOutputStream = new ByteArrayOutputStream(1024);
            outputStream_ = new BufferedOutputStream(byteArrayOutputStream, 1024);
        }
    }

    /**
     * Checks whether the socket is connected.
     */
    public boolean isOpen() {
        if (nettyChannel == null) {
            return false;
        }
        return nettyChannel.isAvailable();
    }

    /**
     * Connects the socket, creating a new socket object if necessary.
     */
    public void open() throws TTransportException {
        if (isOpen()) {
            throw new TTransportException(TTransportException.ALREADY_OPEN, "Socket already connected.");
        }

        try {
            nettyChannel.connect();
            outputStream_ = new BufferedOutputStream(byteArrayOutputStream, 1024);
        } catch (Exception e) {
            close();
            throw new TTransportException(TTransportException.NOT_OPEN, e);
        }
    }

    /**
     * Closes the socket.
     */
    public void close() {
        // Close the underlying streams
        super.close();

        // Close the socket
        if (nettyChannel != null) {
            try {
                nettyChannel.disConnect();
            } catch (Exception e) {
                LOGGER.warn("Could not close socket.", e);
            }
            nettyChannel = null;
        }
    }

    /**
     * Flushes the underlying output stream if not null.
     */
    public void flush() throws TTransportException {
        if (outputStream_ == null) {
            throw new TTransportException(TTransportException.NOT_OPEN, "Cannot flush null outputStream");
        }
        try {
            outputStream_.flush();
            request.setRequestBytes(byteArrayOutputStream.toByteArray());
            nettyChannel.write(byteArrayOutputStream.toByteArray());

        } catch (IOException iox) {
            throw new TTransportException(TTransportException.UNKNOWN, iox);
        }
    }
}
