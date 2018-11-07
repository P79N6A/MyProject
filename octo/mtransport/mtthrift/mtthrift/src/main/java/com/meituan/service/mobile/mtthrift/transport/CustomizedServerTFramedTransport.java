package com.meituan.service.mobile.mtthrift.transport;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerInvoker;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.HeaderUtil;
import com.meituan.service.mobile.mtthrift.util.NewProtocolUtil;
import com.meituan.service.mobile.mtthrift.util.SizeUtil;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.HeartbeatInfo;
import com.sankuai.octo.protocol.ResponseInfo;
import com.sankuai.octo.protocol.TraceInfo;
import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-6-1
 * Time: 下午10:04
 *
 *
 *协议格式:
 1B	  | 1B	 |  1B	    |1B	        |4B	           |2B	           |header length	|total length - 2B - header lenght (-4B) |4B(可选)
 0xAB |	0xBA |	version	|protocol	|total length  |header length  |header	        |body                                    |checksum
 包头	                                                           |消息头	        |消息体	                                 |校验码

 */
public class CustomizedServerTFramedTransport extends TTransport {
    private static final Logger LOG = LoggerFactory.getLogger(CustomizedServerTFramedTransport.class);

    protected static final int DEFAULT_MAX_LENGTH = 16384000;
    /**
     * Buffer for output
     */
    private TByteArrayOutputStream writeBuffer_ = new TByteArrayOutputStream(Consts.DEFAULT_BYTEARRAY_SIZE);
    private final byte[] i32buf = new byte[4];
    private int maxLength_;
    /**
     * Underlying transport
     */
    private TTransport transport_ = null;

    private boolean unifiedProto = false;
    private byte protocol = Consts.protocol[0];
    private boolean isServer = false;
    private long sequenceId = 0;

    private byte messageType = 0;

    private HeartbeatInfo heartbeatInfo = null;

    /**
     * Buffer for input
     */
    private TMemoryInputTransport readBuffer_ = new TMemoryInputTransport(new byte[0]);

    private int responseSize = 0;
    private int requestSize = 0;
    private long beginTime;

    /**
     * Constructor wraps around another transport
     */
    public CustomizedServerTFramedTransport(TTransport transport, int maxLength) {
        transport_ = transport;
        maxLength_ = maxLength;
    }

    /**
     * Constructor wraps around another transport
     */
    public CustomizedServerTFramedTransport(TTransport transport, int maxLength, boolean isServer) {
        transport_ = transport;
        maxLength_ = maxLength;
        this.isServer = isServer;
    }

    public CustomizedServerTFramedTransport(TTransport transport) {
        transport_ = transport;
        maxLength_ = CustomizedServerTFramedTransport.DEFAULT_MAX_LENGTH;
    }

    public TByteArrayOutputStream getWriteBuffer_() {
        return writeBuffer_;
    }

    public void setWriteBuffer_(TByteArrayOutputStream writeBuffer_) {
        this.writeBuffer_ = writeBuffer_;
    }

    public int getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }

    public int getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(int requestSize) {
        this.requestSize = requestSize;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public static final void encodeFrameSize(final int frameSize, final byte[] buf) {
        buf[0] = (byte) (0xff & (frameSize >> 24));
        buf[1] = (byte) (0xff & (frameSize >> 16));
        buf[2] = (byte) (0xff & (frameSize >> 8));
        buf[3] = (byte) (0xff & (frameSize));
    }


    public static final void encodeFrameSize(final short frameSize, final byte[] buf) {
        buf[0] = (byte) (0xff & (frameSize >> 8));
        buf[1] = (byte) (0xff & (frameSize));
    }

    public static final int decodeFrameSize(final byte[] buf) throws TTransportException {
        if (4 > buf.length) {
            throw new TTransportException("buf length" + buf.length + " < 4");
        }

        return
                ((buf[0] & 0xff) << 24) |
                        ((buf[1] & 0xff) << 16) |
                        ((buf[2] & 0xff) << 8) |
                        ((buf[3] & 0xff));
    }

    public boolean isUnifiedProto() {
        return unifiedProto;
    }

    public void setUnifiedProto(boolean unifiedProto) {
        this.unifiedProto = unifiedProto;
    }

    public void setProtocol(byte protocol) {
        this.protocol = protocol;
    }

    public void open() throws TTransportException {
        transport_.open();
    }

    public boolean isOpen() {
        return transport_.isOpen();
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public HeartbeatInfo getHeartbeatInfo() {
        return heartbeatInfo;
    }

    public void setHeartbeatInfo(HeartbeatInfo heartbeatInfo) {
        this.heartbeatInfo = heartbeatInfo;
    }

    public void close() {
        transport_.close();
    }

    public int read(byte[] buf, int off, int len) throws TTransportException {
        if (readBuffer_ != null) {
            int got = readBuffer_.read(buf, off, len);
            if (got > 0) {
                return got;
            }
        }

        // Read another frame of data
        readFrame();

        return readBuffer_.read(buf, off, len);
    }

    @Override
    public byte[] getBuffer() {
        return readBuffer_.getBuffer();
    }

    @Override
    public int getBufferPosition() {
        return readBuffer_.getBufferPosition();
    }

    @Override
    public int getBytesRemainingInBuffer() {
        return readBuffer_.getBytesRemainingInBuffer();
    }

    @Override
    public void consumeBuffer(int len) {
        readBuffer_.consumeBuffer(len);
    }

    private byte[] readBySize(final int size) throws TTransportException {
        if (0 > size) {
            throw new TTransportException("Read a negative frame size (" + size + ")!");
        }

        if (size > maxLength_) {
            throw new TTransportException("Frame size (" + size + ") larger than max length (" + maxLength_ + ")!");
        }

        byte[] buff = new byte[size];
        transport_.readAll(buff, 0, size);

        return buff;
    }

    @Deprecated
    private void readFrame() throws TTransportException {
    }

    public void write(byte[] buf, int off, int len) throws TTransportException {
        writeBuffer_.write(buf, off, len);
    }


    public final Header getHeaderAtServer() {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setSequenceId(getSequenceId());
        Header header = new Header();
        header.setResponseInfo(responseInfo);
        return header;
    }

    public void doCatLog(boolean unifiedProto) {

        if (ThriftServerGlobalConfig.isEnableCat() && ThriftServerInvoker.serviceName.get() != null && ThriftServerInvoker.methodName.get() != null
                && ThriftServerInvoker.isDrop.get() != null && ThriftServerInvoker.transactionThreadLocal.get() != null) {
            Transaction transaction = ThriftServerInvoker.transactionThreadLocal.get();
            Cat.logEvent("OctoService.appkey", ClientInfoUtil.getClientAppKey());
            Cat.logEvent("OctoService.clientIp", ClientInfoUtil.getClientIp());
            Cat.logEvent("OctoService.thriftType", ThriftServerPublisher.serviceInterfaceThriftTypeMap.get(ThriftServerInvoker.serviceName.get()));
            Cat.logEvent("OctoService.protocolType", unifiedProto ? "unified" : "old");
            Cat.logEvent("OctoService.handleType", ThriftServerInvoker.isDrop.get() ? "drop" : "accept");
            Cat.logEvent("OctoService.requestSize", SizeUtil.getLogSize(this.requestSize));
            Cat.logEvent("OctoService.responseSize", SizeUtil.getLogSize(this.responseSize));
            Throwable throwable = ThriftServerInvoker.throwable.get();
            if (throwable == null) {
                transaction.setStatus(Transaction.SUCCESS);
            } else {
                transaction.setStatus(throwable);
                Cat.logError(throwable);
            }
            transaction.complete();
        }
    }

    @Override
    public void flush() throws TTransportException {
        if (!unifiedProto) {
            byte[] buf = this.writeBuffer_.get();
            int len = this.writeBuffer_.len();
            this.writeBuffer_.reset();
            encodeFrameSize(len, this.i32buf);
            this.transport_.write(this.i32buf, 0, 4);
            this.transport_.write(buf, 0, len);
            this.transport_.flush();
            this.responseSize = len + 4;
            doCatLog(unifiedProto);
            if (ThriftServerGlobalConfig.isEnableMtrace()) {
                Tracer.serverSend();
            }
            return;
        }

        transport_.write(Consts.magic);
        transport_.write(Consts.version);
        transport_.write(new byte[]{protocol});

        byte[] header;
        try {
            header = HeaderUtil.headerSerialize(HeaderUtil.headerAsResponse(
                    sequenceId, getTraceInfo(), getMessageType(), getHeartbeatInfo()));
        } catch (TException e) {
            LOG.error("headerSerialize failed...", e);
            return;
        }

        byte[] body = writeBuffer_.get();
        final int bodyLength = writeBuffer_.len();
        writeBuffer_.reset();

        byte[] headerBodyBuf = new byte[header.length + bodyLength];
        System.arraycopy(header, 0, headerBodyBuf, 0, header.length);
        System.arraycopy(body, 0, headerBodyBuf, header.length, bodyLength);

        //compress header & body
        final boolean zip = (protocol & 0x40) == 0x40;
        final boolean snap = (protocol & 0x20) == 0x20;
        if (zip && !snap) {
            try {
                headerBodyBuf = NewProtocolUtil.gZip(headerBodyBuf);
            } catch (IOException e) {
                LOG.debug("gZip failed...", e);
                return;
            }
        } else if (!zip && snap) {
            try {
                headerBodyBuf = NewProtocolUtil.compressSnappy(headerBodyBuf);
            } catch (IOException e) {
                LOG.debug("compressSnappy failed...", e);
                return;
            }
        }

        int totalLen = Consts.headerLenBytesCount + headerBodyBuf.length;
        final boolean needChecksum = (0x80 == (protocol & 0x80));
        if (needChecksum) {
            totalLen += Consts.checkSumBytesCount;
        }

        this.responseSize = totalLen + Consts.totalLenBytesCount + 4;

        byte[] totalLenBytes = new byte[Consts.totalLenBytesCount];
        encodeFrameSize(totalLen, totalLenBytes);
        transport_.write(totalLenBytes, 0, Consts.totalLenBytesCount);

        byte[] headerLen = new byte[Consts.headerLenBytesCount];
        encodeFrameSize((short) header.length, headerLen);
        transport_.write(headerLen, 0, Consts.headerLenBytesCount);

        transport_.write(headerBodyBuf, 0, headerBodyBuf.length);

        if (needChecksum) {

            ByteBuffer checkSumByteBuffer = ByteBuffer.allocate(Consts.bytesCountOfAllLenghInfo + headerBodyBuf.length);
            checkSumByteBuffer.put(Consts.magic);
            checkSumByteBuffer.put(Consts.version);
            checkSumByteBuffer.put(protocol);
            checkSumByteBuffer.put(totalLenBytes);
            checkSumByteBuffer.put(headerLen);
            checkSumByteBuffer.put(headerBodyBuf);

            byte[] checksum = NewProtocolUtil.getChecksum(checkSumByteBuffer.array());
            transport_.write(checksum, 0, checksum.length);
        }

        transport_.flush();
        if (heartbeatInfo == null) {
            doCatLog(unifiedProto);
            if (ThriftServerGlobalConfig.isEnableMtrace()) {
                Tracer.serverSend();
            }
        }

    }

    public static TraceInfo getTraceInfo() {
        TraceInfo traceInfo = new TraceInfo();
        String clientAppkey = ClientInfoUtil.getClientAppKey();
        if (clientAppkey == null) {
            clientAppkey = "unknownService";
        }
        traceInfo.setClientAppkey(clientAppkey);
        traceInfo.setDebug(Tracer.getServerTracer().isSample());
        return traceInfo;
    }

    public static class Factory extends TTransportFactory {
        private int maxLength_;
        private boolean isServer;

        public Factory() {
            maxLength_ = CustomizedServerTFramedTransport.DEFAULT_MAX_LENGTH;
        }

        public Factory(boolean isServer) {
            maxLength_ = CustomizedServerTFramedTransport.DEFAULT_MAX_LENGTH;
            this.isServer = isServer;
        }

        public Factory(int maxLength) {
            maxLength_ = maxLength > DEFAULT_MAX_LENGTH ? maxLength : DEFAULT_MAX_LENGTH;
        }

        @Override
        public TTransport getTransport(TTransport base) {
            return new CustomizedServerTFramedTransport(base, maxLength_, isServer);
        }

    }
}
