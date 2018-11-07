package com.meituan.service.mobile.mtthrift.transport;

import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.HeaderUtil;
import com.meituan.service.mobile.mtthrift.util.NewProtocolUtil;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.MessageType;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-5-10
 * Time: 下午3:38
 *
 *协议格式:
 1B	  | 1B	 |  1B	    |1B	        |4B	           |2B	           |header length	|total length - 2B - header lenght (-4B) |4B(可选)
 0xAB |	0xBA |	version	|protocol	|total length  |header length  |header	        |body                                    |checksum
 包头	                                                           |消息头	        |消息体	                                 |校验码

 */
public class CustomizedTFramedTransport extends TTransport {
    private static final Logger LOG = LoggerFactory.getLogger(CustomizedTFramedTransport.class);

    protected static final int DEFAULT_MAX_LENGTH = 16384000;
    private static final AtomicLong requestSequenceMaker = new AtomicLong();
    /**
     * Buffer for output
     */
    private TByteArrayOutputStream writeBuffer_ = new TByteArrayOutputStream(Consts.DEFAULT_BYTEARRAY_SIZE);
    private final byte[] i32buf = new byte[4];
    private int maxLength_;
    private boolean unifiedProto = false;
    private String serviceName = "";
    private TraceInfo traceInfo;
    /**
     * Underlying transport
     */
    private TTransport transport_ = null;
    /**
     * Buffer for input
     */
    private TMemoryInputTransport readBuffer_ = new TMemoryInputTransport(new byte[0]);
    private byte[] protocol;
    private MessageType messageType = MessageType.Normal;
    private Header headerInfo;
    private int timeout = 0;

    private int responseSize = 0;
    private int requestSize = 0;
    private long sequenceId = -1L;

    /**
     * Constructor wraps around another transport
     */
    public CustomizedTFramedTransport(TTransport transport, int maxLength) {
        transport_ = transport;
        maxLength_ = maxLength > DEFAULT_MAX_LENGTH ? maxLength : DEFAULT_MAX_LENGTH;
    }

    public CustomizedTFramedTransport(TTransport transport, int maxLength, int timeout) {
        transport_ = transport;
        maxLength_ = maxLength > DEFAULT_MAX_LENGTH ? maxLength : DEFAULT_MAX_LENGTH;
        this.timeout = timeout;
    }

    public CustomizedTFramedTransport(TTransport transport, int maxLength, boolean unifiedProto) {
        transport_ = transport;
        maxLength_ = maxLength;
        this.unifiedProto = unifiedProto;
    }

    public CustomizedTFramedTransport(TTransport transport) {
        transport_ = transport;
        maxLength_ = CustomizedTFramedTransport.DEFAULT_MAX_LENGTH;
    }

    public CustomizedTFramedTransport(TTransport transport, long messageId) {
        transport_ = transport;
        maxLength_ = CustomizedTFramedTransport.DEFAULT_MAX_LENGTH;
        sequenceId = messageId;
    }

    public CustomizedTFramedTransport(TTransport tTransport, long messageId, int maxLength) {
        this(tTransport, messageId);
        this.maxLength_ = maxLength > DEFAULT_MAX_LENGTH ? maxLength : DEFAULT_MAX_LENGTH;
    }

    public CustomizedTFramedTransport(TTransport tTransport, long messageId, int maxLength, int timeout) {
        this(tTransport, messageId);
        this.maxLength_ = maxLength > DEFAULT_MAX_LENGTH ? maxLength : DEFAULT_MAX_LENGTH;
        this.timeout = timeout;
    }

    public TByteArrayOutputStream getWriteBuffer_() {
        return writeBuffer_;
    }

    public void setWriteBuffer_(TByteArrayOutputStream writeBuffer_) {
        this.writeBuffer_ = writeBuffer_;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setUnifiedProto(boolean unifiedProto) {
        this.unifiedProto = unifiedProto;
    }

    public boolean isUnifiedProto() {
        return unifiedProto;
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

    @Deprecated
    public static final int decodeFrameSize(final byte[] buf) {
        return
                ((buf[0] & 0xff) << 24) |
                        ((buf[1] & 0xff) << 16) |
                        ((buf[2] & 0xff) << 8) |
                        ((buf[3] & 0xff));
    }

    public static final int decodeFrameSize(final byte[] buf, final int pos, final int length) throws TTransportException {
        if (2 != length && 4 != length)
            throw new TTransportException("decodeFrameSize must be 2 or 4");
        if (pos < 0 || buf.length < pos + length)
            throw new TTransportException("decodeFrameSize out of array index of buf");

        int size = 0;
        for (int i = 0, j = 8 * (length - 1); i < length; i++, j = j - 8) {
            size = size | ((buf[i + pos] & 0xff) << j);
        }
        return size;
    }

    public void setTraceInfo(TraceInfo traceInfo) {
        this.traceInfo = traceInfo;
    }

    public void open() throws TTransportException {
        transport_.open();
    }

    public boolean isOpen() {
        return transport_.isOpen();
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

    public TMemoryInputTransport getReadBuffer_() {
        return readBuffer_;
    }

    public void setReadBuffer_(TMemoryInputTransport readBuffer_) {
        this.readBuffer_ = readBuffer_;
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

    public void readFrame() throws TTransportException {
        byte[] first4bytes = new byte[4];
        transport_.readAll(first4bytes, 0, 4);
        int size = decodeFrameSize(first4bytes, 0, 4);

        ByteBuffer checkSumByteBuffer = null;
        boolean checksumExist = false;
        unifiedProto = (first4bytes[0] == Consts.first && first4bytes[1] == Consts.second);

        final boolean zip = (first4bytes[3] & 0x40) == 0x40;
        final boolean snap = (first4bytes[3] & 0x20) == 0x20;

        if (unifiedProto) {
            checksumExist = (0x80 == (first4bytes[3] & 0x80));

            transport_.readAll(i32buf, 0, Consts.totalLenBytesCount);      //read total length

            size = decodeFrameSize(i32buf, 0, Consts.totalLenBytesCount); //total length
            if (checksumExist) {
                checkSumByteBuffer = ByteBuffer.allocate(Consts.checkSumBytesCount + size);
                checkSumByteBuffer.put(first4bytes);
                checkSumByteBuffer.put(i32buf);
            }
        }

        this.responseSize = size + 4;

        byte[] buff = readBySize(size);

        if (unifiedProto) {
            //header length
            final int headerUnzipLen = decodeFrameSize(buff, 0, Consts.headerLenBytesCount);

            int headerBodyLength;
            if (checksumExist) {
                byte[] headerSizeBytes = new byte[Consts.headerLenBytesCount];
                System.arraycopy(buff, 0, headerSizeBytes, 0, Consts.headerLenBytesCount);
                checkSumByteBuffer.put(headerSizeBytes);
                headerBodyLength = buff.length - Consts.totalLenBytesCount - Consts.headerLenBytesCount; //exclude
                byte[] tempBuf = new byte[headerBodyLength];
                System.arraycopy(buff, Consts.headerLenBytesCount, tempBuf, 0, headerBodyLength);

                checkSumByteBuffer.put(tempBuf);
            } else {
                headerBodyLength = buff.length - Consts.headerLenBytesCount;
            }

            byte[] headerBodyBytes = new byte[headerBodyLength];
            System.arraycopy(buff, Consts.headerLenBytesCount, headerBodyBytes, 0,
                    headerBodyLength);

            if (checksumExist) {
                System.arraycopy(buff, Consts.headerLenBytesCount + (size - Consts.headerLenBytesCount -
                        Consts.totalLenBytesCount), i32buf, 0, Consts.checkSumBytesCount);

                if (!NewProtocolUtil.bytesEquals(i32buf, NewProtocolUtil.getChecksum(checkSumByteBuffer.array()))) {
                    throw new TTransportException("checksum failed");
                }
            }

            if (zip && !snap) {
                try {
                    headerBodyBytes = NewProtocolUtil.unGZip(headerBodyBytes);
                    headerBodyLength = headerBodyBytes.length;
                } catch (IOException e) {
                    LOG.debug("unGZip failed...", e);
                    return;
                }
            } else if (!zip && snap) {
                try {
                    headerBodyBytes = NewProtocolUtil.unCompressSnappy(headerBodyBytes);
                    headerBodyLength = headerBodyBytes.length;
                } catch (IOException e) {
                    LOG.debug("unCompressSnappy failed...", e);
                    return;
                }
            } else {
                headerBodyLength = headerBodyBytes.length;
            }

            byte[] headerBytes = new byte[headerUnzipLen];
            System.arraycopy(headerBodyBytes, 0, headerBytes, 0, headerUnzipLen);

            try {
                headerInfo = HeaderUtil.headerDeserialize(headerBytes);
            } catch (TException e) {
                LOG.error("headerDeserialize failed...", e);
                return;
            }

            final int bodyLen = headerBodyLength - headerUnzipLen;
            byte[] bodyBuf = new byte[bodyLen];
            System.arraycopy(headerBodyBytes, headerUnzipLen, bodyBuf, 0, bodyLen);
            buff = bodyBuf;
        }

        readBuffer_.reset(buff);
    }


    public void write(byte[] buf, int off, int len) throws TTransportException {
        writeBuffer_.write(buf, off, len);
    }

    @Override
    public void flush() throws TTransportException {
        if (!unifiedProto) {
            byte[] buf = this.writeBuffer_.get();
            int len = this.writeBuffer_.len();
            this.writeBuffer_.reset();
            if (len > maxLength_) {
                throw new TTransportException("Frame size (" + len + ") larger than max length (" + maxLength_ + ")!");
            }
            encodeFrameSize(len, this.i32buf);
            this.transport_.write(this.i32buf, 0, 4);
            this.transport_.write(buf, 0, len);
            this.transport_.flush();
            this.requestSize = len + 4;
            return;
        }

        byte[] body = this.writeBuffer_.get();
        final int bodyLength = this.writeBuffer_.len();
        this.writeBuffer_.reset();
        if (bodyLength > maxLength_) {
            throw new TTransportException("Frame size (" + bodyLength + ") larger than max length (" + maxLength_ + ")!");
        }
        transport_.write(Consts.magic);
        transport_.write(Consts.version);
        transport_.write(this.protocol);

        byte[] header;
        try {
            if (sequenceId == -1) {
                sequenceId = requestSequenceMaker.addAndGet(1L);
            }
            if (headerInfo == null) {
                headerInfo = HeaderUtil.headerAsRequest(sequenceId, serviceName, timeout, traceInfo, messageType);
            }
            header = HeaderUtil.headerSerialize(headerInfo);
        } catch (TException e) {
            LOG.error("headerSerialize failed...", e);
            return;
        }

        byte[] headerBodyBuf = new byte[header.length + bodyLength]; //header.length
        System.arraycopy(header, 0, headerBodyBuf, 0, header.length);
        System.arraycopy(body, 0, headerBodyBuf, header.length, bodyLength);

        //compress header & body
        final boolean zip = ((this.protocol)[0] & 0x40) == 0x40;
        final boolean snap = ((this.protocol)[0] & 0x20) == 0x20;

        try {
            if (zip && !snap) {
                headerBodyBuf = NewProtocolUtil.gZip(headerBodyBuf);
            } else if (!zip && snap) {
                headerBodyBuf = NewProtocolUtil.compressSnappy(headerBodyBuf);
            } else {
            }
        } catch (IOException e) {
            LOG.debug("gZip or compressSnappy failed...", e);
            return;
        }

        int totalLen = Consts.headerLenBytesCount + headerBodyBuf.length;
        final boolean needChecksum = (0x80 == ((this.protocol)[0] & 0x80));
        if (needChecksum) {
            totalLen += Consts.checkSumBytesCount;
        }

        byte[] totalLenBytes = new byte[Consts.totalLenBytesCount];
        encodeFrameSize(totalLen, totalLenBytes);
        transport_.write(totalLenBytes, 0, Consts.totalLenBytesCount);

        byte[] headerLen = new byte[Consts.headerLenBytesCount];
        encodeFrameSize((short) header.length, headerLen);      //unzip len
        transport_.write(headerLen, 0, Consts.headerLenBytesCount);

        transport_.write(headerBodyBuf, 0, headerBodyBuf.length);

        if (needChecksum) {

            ByteBuffer checkSumByteBuffer = ByteBuffer.allocate(Consts.bytesCountOfAllLenghInfo + headerBodyBuf.length);
            checkSumByteBuffer.put(Consts.magic);
            checkSumByteBuffer.put(Consts.version);
            checkSumByteBuffer.put(this.protocol);
            checkSumByteBuffer.put(totalLenBytes);
            checkSumByteBuffer.put(headerLen);
            checkSumByteBuffer.put(headerBodyBuf);

            byte[] checksum = NewProtocolUtil.getChecksum(checkSumByteBuffer.array());
            transport_.write(checksum, 0, checksum.length);
            this.requestSize += Consts.checkSumBytesCount;
        }
        transport_.flush();
        this.requestSize += headerBodyBuf.length + Consts.bytesCountOfAllLenghInfo;
    }

    public void setProtocol(byte[] protocol) {
        this.protocol = protocol;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Header getHeaderInfo() {
        return headerInfo;
    }

    public static class Factory extends TTransportFactory {
        private int maxLength_;

        public Factory() {
            maxLength_ = CustomizedTFramedTransport.DEFAULT_MAX_LENGTH;
        }

        public Factory(int maxLength) {
            maxLength_ = maxLength;
        }

        @Override
        public TTransport getTransport(TTransport base) {
            return new CustomizedTFramedTransport(base, maxLength_);
        }
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

    public long getSequenceId() {
        return sequenceId;
    }

    public void setHeaderInfo(Header headerInfo) {
        this.headerInfo = headerInfo;
    }
}
