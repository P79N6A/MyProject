package com.meituan.service.mobile.mtthrift.server.customize;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.util.NewProtocolUtil;
import com.meituan.service.mobile.mtthrift.transport.CustomizedServerTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.HeaderUtil;
import com.meituan.service.mobile.mtthrift.util.LoadInfoUtil;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.HeartbeatInfo;
import com.sankuai.octo.protocol.LoadInfo;
import com.sankuai.octo.protocol.MessageType;
import com.sankuai.sgagent.thrift.model.ConfigStatus;
import com.sankuai.sgagent.thrift.model.CustomizedStatus;
import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-5-10
 * Time: 上午11:15
 *
 *协议格式:
 1B	  | 1B	 |  1B	    |1B	        |4B	           |2B	           |header length	|total length - 2B - header lenght (-4B) |4B(可选)
 0xAB |	0xBA |	version	|protocol	|total length  |header length  |header	        |body                                    |checksum
 包头	                                                           |消息头	        |消息体	                                 |校验码

 */
public abstract class CustomizedAbstractNonblockingServer extends TServer {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    private ConfigStatus configStatus;

    public static abstract class CustomizedAbstractNonblockingServerArgs<T extends CustomizedAbstractNonblockingServerArgs<T>> extends AbstractServerArgs<T> {
        public long maxReadBufferBytes = Long.MAX_VALUE;

        public CustomizedAbstractNonblockingServerArgs(TNonblockingServerTransport transport) {
            super(transport);
            transportFactory(new TFramedTransport.Factory());
        }
    }

    /**
     * The maximum amount of memory we will allocate to client IO buffers at a
     * time. Without this limit, the server will gladly allocate client buffers
     * right into an out of memory exception, rather than waiting.
     */
    private final long MAX_READ_BUFFER_BYTES;

    private final String appkey;

    public ConfigStatus getConfigStatus() {
        return this.configStatus;
    }

    /**
     * How many bytes are currently allocated to read buffers.
     */
    private final AtomicLong readBufferBytesAllocated = new AtomicLong(0);

    public CustomizedAbstractNonblockingServer(CustomizedAbstractNonblockingServerArgs args, String appkey, ConfigStatus configStatus) {
        super(args);
        MAX_READ_BUFFER_BYTES = args.maxReadBufferBytes;
        this.appkey = appkey;
        this.configStatus = configStatus;
    }

    /**
     * Begin accepting connections and processing invocations.
     */
    public void serve() {
        // start any IO threads
        if (!startThreads()) {
            return;
        }

        // start listening, or exit
        if (!startListening()) {
            return;
        }

        setServing(true);

        // this will block while we serve
        waitForShutdown();

        setServing(false);

        // do a little cleanup
        stopListening();
    }

    /**
     * Starts any threads required for serving.
     *
     * @return true if everything went ok, false if threads could not be started.
     */
    protected abstract boolean startThreads();

    /**
     * A method that will block until when threads handling the serving have been
     * shut down.
     */
    protected abstract void waitForShutdown();

    /**
     * Have the server transport start accepting connections.
     *
     * @return true if we started listening successfully, false if something went
     * wrong.
     */
    protected boolean startListening() {
        try {
            serverTransport_.listen();
            return true;
        } catch (TTransportException ttx) {
            LOGGER.error("Failed to start listening on server socket!", ttx);
            return false;
        }
    }

    /**
     * Stop listening for connections.
     */
    protected void stopListening() {
        serverTransport_.close();
    }

    /**
     * Perform an invocation. This method could behave several different ways -
     * invoke immediately inline, queue for separate execution, etc.
     *
     * @return true if invocation was successfully requested, which is not a
     * guarantee that invocation has completed. False if the request
     * failed.
     */
    protected abstract boolean requestInvoke(FrameBuffer frameBuffer);

    /**
     * An abstract thread that handles selecting on a set of transports and
     * {@link FrameBuffer FrameBuffers} associated with selected keys
     * corresponding to requests.
     */
    protected abstract class AbstractSelectThread extends Thread {
        protected final Selector selector;

        // List of FrameBuffers that want to change their selection interests.
        protected final Set<FrameBuffer> selectInterestChanges = new HashSet<FrameBuffer>();

        public AbstractSelectThread() throws IOException {
            this.selector = SelectorProvider.provider().openSelector();
        }

        /**
         * If the selector is blocked, wake it up.
         */
        public void wakeupSelector() {
            selector.wakeup();
        }

        /**
         * Add FrameBuffer to the list of select interest changes and wake up the
         * selector if it's blocked. When the select() call exits, it'll give the
         * FrameBuffer a chance to change its interests.
         */
        public void requestSelectInterestChange(FrameBuffer frameBuffer) {
            synchronized (selectInterestChanges) {
                selectInterestChanges.add(frameBuffer);
            }
            // wakeup the selector, if it's currently blocked.
            selector.wakeup();
        }

        /**
         * Check to see if there are any FrameBuffers that have switched their
         * interest type from read to write or vice versa.
         */
        protected void processInterestChanges() {
            synchronized (selectInterestChanges) {
                for (FrameBuffer fb : selectInterestChanges) {
                    fb.changeSelectInterests();
                }
                selectInterestChanges.clear();
            }
        }

        /**
         * Do the work required to read from a readable client. If the frame is
         * fully read, then invoke the method call.
         */
        protected void handleRead(SelectionKey key) {
            FrameBuffer buffer = (FrameBuffer) key.attachment();
            if (!buffer.read()) {
                cleanupSelectionKey(key);
                return;
            }

            // if the buffer's frame read is complete, invoke the method.
            if (buffer.isFrameFullyRead()) {
                if (!requestInvoke(buffer)) {
                    cleanupSelectionKey(key);
                }
            }
        }

        /**
         * Let a writable client get written, if there's data to be written.
         */
        protected void handleWrite(SelectionKey key) {
            FrameBuffer buffer = (FrameBuffer) key.attachment();
            if (!buffer.write()) {
                cleanupSelectionKey(key);
            }
        }

        /**
         * Do connection-close cleanup on a given SelectionKey.
         */
        protected void cleanupSelectionKey(SelectionKey key) {
            // remove the records from the two maps
            FrameBuffer buffer = null;
            try {
                buffer = (FrameBuffer) key.attachment();
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            } finally {
                if (buffer != null) {
                    // close the buffer
                    buffer.close();
                }
            }
            // cancel the selection key
            key.cancel();
        }
    } // SelectThread

    /**
     * Possible states for the FrameBuffer state machine.
     */
    private enum FrameBufferState {
        // in the midst of reading the frame size off the wire
        READING_FRAME_SIZE,
        // reading the actual frame data now, but not all the way done yet
        READING_FRAME,
        // completely read the frame, so an invocation can now happen
        READ_FRAME_COMPLETE,
        // waiting to get switched to listening for write events
        AWAITING_REGISTER_WRITE,
        // started writing response data, not fully complete yet
        WRITING,
        // another thread wants this framebuffer to go back to reading
        AWAITING_REGISTER_READ,
        // we want our transport and selection key invalidated in the selector
        // thread
        AWAITING_CLOSE
    }

    /**
     * Class that implements a sort of state machine around the interaction with a
     * client and an invoker. It manages reading the frame size and frame data,
     * getting it handed off as wrapped transports, and then the writing of
     * response data back to the client. In the process it manages flipping the
     * read and write bits on the selection key for its client.
     */
    protected class FrameBuffer {
        // the actual transport hooked up to the client.
        private final TNonblockingTransport trans_;

        // the SelectionKey that corresponds to our transport
        private final SelectionKey selectionKey_;

        // the SelectThread that owns the registration of our transport
        private final AbstractSelectThread selectThread_;

        // where in the process of reading/writing are we?
        private FrameBufferState state_ = FrameBufferState.READING_FRAME_SIZE;

        // the ByteBuffer we'll be using to write and read, depending on the state
        private ByteBuffer buffer_;

        private TByteArrayOutputStream response_;

        private boolean unifiedProto = false;

        private long sequenceId = 0L;

        private byte protocol;

        private Header header = null;

        private int bodyLength;

        private int bodyBeginPos;

        boolean needChecksum = false;

        private byte messageType = 0;

        private HeartbeatInfo heartbeatInfo = null;

        byte[] first4bytes = null;
        int frameSize = -1;
        byte[] frameSizeBytes = new byte[4];

        private int requestSize = 0;

        public FrameBuffer(final TNonblockingTransport trans,
                           final SelectionKey selectionKey,
                           final AbstractSelectThread selectThread) {
            trans_ = trans;
            selectionKey_ = selectionKey;
            selectThread_ = selectThread;
            buffer_ = ByteBuffer.allocate(4);

        }

        /**
         * Give this FrameBuffer a chance to read. The selector loop should have
         * received a read event for this FrameBuffer.
         *
         * @return true if the connection should live on, false if it should be
         * closed
         */
        public boolean read() {
            ByteBuffer checkSumByteBuffer = null;

            if (state_ == FrameBufferState.READING_FRAME_SIZE) {
                // try to read the frame size completely
                if (!internalRead()) {
                    return false;
                }

                if (buffer_.remaining() == 0) {
                    first4bytes = buffer_.array();
                    unifiedProto = (first4bytes[0] == Consts.first && first4bytes[1] ==  Consts.second);
                    if (unifiedProto) {
                        protocol = first4bytes[3];
                        needChecksum = (0x80 == (protocol & 0x80));

                        buffer_ = ByteBuffer.allocate(4);   //total length
                        if (!internalRead()) {
                            return false;
                        }
                    }

                    // pull out the frame size as an integer.
                    frameSize = buffer_.getInt(0);
                    frameSizeBytes = buffer_.array();
                    if (unifiedProto)
                        requestSize = frameSize + 4 + Consts.totalLenBytesCount;
                    else
                        requestSize = frameSize + 4;
                    if (!validateFrameSize(frameSize)) {
                        return false;
                    }
                    if (readBufferBytesAllocated.get() + frameSize > MAX_READ_BUFFER_BYTES) {
//                        System.out.println("read()" + "readBufferBytesAllocated:" + readBufferBytesAllocated +
//                        "|MAX_READ_BUFFER_BYTES:" + MAX_READ_BUFFER_BYTES);
                        return true;
                    }
                    buffer_ = ByteBuffer.allocate(frameSize);
                    state_ = FrameBufferState.READING_FRAME;
                } else {
                    return true;
                }
            }

            // it is possible to fall through from the READING_FRAME_SIZE section
            // to READING_FRAME if there's already some frame data available once
            // READING_FRAME_SIZE is complete.

            if (state_ == FrameBufferState.READING_FRAME) {
                if (!internalRead()) {
                    return false;
                }

                // since we're already in the select loop here for sure, we can just
                // modify our selection key directly.
                if (buffer_.remaining() == 0) {
                    // get rid of the read select interests
                    selectionKey_.interestOps(0);
                    state_ = FrameBufferState.READ_FRAME_COMPLETE;


                    if (unifiedProto) {
                        int headerBodyLength = frameSize - Consts.headerLenBytesCount;
                        buffer_.position(0);
                        short headerLength = buffer_.getShort(0);
                        buffer_.position(Consts.headerLenBytesCount);

                        if (0 > headerLength) {
                            System.out.println("headerLenth error");
                            return false;
                        }

                        if (needChecksum)
                            headerBodyLength -= Consts.checkSumBytesCount;

                        final boolean zip = (protocol & 0x40) == 0x40;
                        final boolean snap = (protocol & 0x20) == 0x20;

                        byte[] headBodybytes = new byte[headerBodyLength];
                        buffer_.get(headBodybytes, 0, headerBodyLength);

                        if (needChecksum) {
                            checkSumByteBuffer = ByteBuffer.allocate(Consts.bytesCountOfAllLenghInfo + headerBodyLength);
                            checkSumByteBuffer.put(first4bytes);
                            checkSumByteBuffer.put(frameSizeBytes);
                            checkSumByteBuffer.putShort(headerLength);
                            checkSumByteBuffer.put(headBodybytes);
                            byte[] fourByteCheckSum = new byte[Consts.checkSumBytesCount];
                            buffer_.get(fourByteCheckSum, 0, Consts.checkSumBytesCount);
                            if (!NewProtocolUtil.bytesEquals(fourByteCheckSum, NewProtocolUtil.getChecksum(checkSumByteBuffer.array())) ) {
                                System.out.println("checksum failed");
                                return false;
                            }
                        }

                        try {
                            if (zip && !snap) {
                                buffer_ = ByteBuffer.wrap(NewProtocolUtil.unGZip(headBodybytes));
                                headerBodyLength = buffer_.limit();
                            } else if (!zip && snap) {
                                buffer_ = ByteBuffer.wrap(NewProtocolUtil.unCompressSnappy(headBodybytes));
                                headerBodyLength = buffer_.limit();
                            } else {
                                buffer_ = ByteBuffer.wrap(headBodybytes);
                                headerBodyLength = buffer_.limit();
                            }
                        } catch (IOException e) {
                            LOGGER.warn("", e);
                            return false;
                        }

                        // get header from buffer_
                        byte[] headerBytes = new byte[headerLength];
                        buffer_.get(headerBytes, 0, headerLength);
                        try {
                            header = HeaderUtil.headerDeserialize(headerBytes);
                        } catch (TException e) {
                            LOGGER.error("headerDeserialize failed", e);
                            return false;
                        }
                        if(null != header) {
                            this.messageType = header.messageType;
                            this.heartbeatInfo = genHeartbeatInfo(header.messageType);
                            bodyLength = headerBodyLength - headerLength;
                            bodyBeginPos = buffer_.position();
                            buffer_.position(buffer_.limit());
                            if(null != header.getRequestInfo()) {
                                sequenceId = header.getRequestInfo().getSequenceId();
                            }
                        }

                    }
                }

                return true;
            }

            // if we fall through to this point, then the state must be invalid.
            LOGGER.error("Read was called but state is invalid (" + state_ + ")");
            return false;
        }


        private HeartbeatInfo genHeartbeatInfo(byte messageType) {
            HeartbeatInfo ret;
            if (messageType == MessageType.Normal.getValue()) {
                //  Normal
                ret = null;
            } else if (messageType == MessageType.NormalHeartbeat.getValue()) {
                ret = null;
            } else {
                //  ScannerHeartbeat
                ret = genScannerHeartbeatInfo();
            }
            return ret;
        }

        private HeartbeatInfo genScannerHeartbeatInfo() {
            LoadInfo loadInfo = new LoadInfo();
            loadInfo.setAverageLoad(LoadInfoUtil.getAvgLoad());
            loadInfo.setOldGC(LoadInfoUtil.getOldGcCount());
            loadInfo.setMethodQpsMap(LoadInfoUtil.getQpsMap());

            HeartbeatInfo heartbeatInfo = new HeartbeatInfo();
            heartbeatInfo.setAppkey(appkey);
            long currentTimeUs = System.nanoTime() / 1000;
            heartbeatInfo.setSendTime(currentTimeUs);
            heartbeatInfo.setLoadInfo(loadInfo);

            CustomizedStatus runtimeStatus = configStatus.getRuntimeStatus();
            heartbeatInfo.setStatus(runtimeStatus.getValue());
            return heartbeatInfo;
        }


        private boolean validateFrameSize(int frameSize) {
            if (frameSize <= 0) {
                LOGGER.warn("Read an invalid frame size of " + frameSize
                        + ". Are you using TFramedTransport on the client side?");
                return false;
            }

            // if this frame will always be too large for this server, log the
            // error and close the connection.
            if (frameSize > MAX_READ_BUFFER_BYTES) {
                LOGGER.info("Read a frame size of " + frameSize
                        + ", which is bigger than the maximum allowable buffer size for ALL connections.");
                return false;
            }
            readBufferBytesAllocated.addAndGet(frameSize);
//            System.out.println("validateFrameSize()" + "readBufferBytesAllocated:" + readBufferBytesAllocated +
//                    "|MAX_READ_BUFFER_BYTES:" + MAX_READ_BUFFER_BYTES);

            return true;
        }

        /**
         * Give this FrameBuffer a chance to write its output to the final client.
         */
        public boolean write() {
            if (state_ == FrameBufferState.WRITING) {
                try {
                    if (trans_.write(buffer_) < 0) {
                        return false;
                    }
                } catch (IOException e) {
                    LOGGER.warn("Got an IOException during write!", e);
                    return false;
                }

                // we're done writing. now we need to switch back to reading.
                if (buffer_.remaining() == 0) {
                    prepareRead();
                }
                return true;
            }

            LOGGER.error("Write was called, but state is invalid (" + state_ + ")");
            return false;
        }

        /**
         * Give this FrameBuffer a chance to set its interest to write, once data
         * has come in.
         */
        public void changeSelectInterests() {
            if (state_ == FrameBufferState.AWAITING_REGISTER_WRITE) {
                // set the OP_WRITE interest
                selectionKey_.interestOps(SelectionKey.OP_WRITE);
                state_ = FrameBufferState.WRITING;
            } else if (state_ == FrameBufferState.AWAITING_REGISTER_READ) {
                prepareRead();
            } else if (state_ == FrameBufferState.AWAITING_CLOSE) {
                close();
                selectionKey_.cancel();
            } else {
                LOGGER.error("changeSelectInterest was called, but state is invalid (" + state_ + ")");
            }
        }

        /**
         * Shut the connection down.
         */
        public void close() {
            // if we're being closed due to an error, we might have allocated a
            // buffer that we need to subtract for our memory accounting.
            if (state_ == FrameBufferState.READING_FRAME || state_ == FrameBufferState.READ_FRAME_COMPLETE) {
                readBufferBytesAllocated.addAndGet(-frameSize);

//                System.out.println("close()" + "|readBufferBytesAllocated:" + readBufferBytesAllocated +
//                        "|MAX_READ_BUFFER_BYTES:" + MAX_READ_BUFFER_BYTES);
            }
            trans_.close();
        }

        public long getSequenceId() {
            return sequenceId;
        }

        public void setSequenceId(long sequenceId) {
            this.sequenceId = sequenceId;
        }

        /**
         * Check if this FrameBuffer has a full frame read.
         */
        public boolean isFrameFullyRead() {
            return state_ == FrameBufferState.READ_FRAME_COMPLETE;
        }

        /**
         * After the processor has processed the invocation, whatever thread is
         * managing invocations should call this method on this FrameBuffer so we
         * know it's time to start trying to write again. Also, if it turns out that
         * there actually isn't any data in the response buffer, we'll skip trying
         * to write and instead go back to reading.
         */
        public void responseReady() {
            // the read buffer is definitely no longer in use, so we will decrement
            // our read buffer count. we do this here as well as in close because
            // we'd like to free this read memory up as quickly as possible for other
            // clients.
            readBufferBytesAllocated.addAndGet(-frameSize);


//            System.out.println("responseReady()" + "|readBufferBytesAllocated:" + readBufferBytesAllocated +
//                    "|MAX_READ_BUFFER_BYTES:" + MAX_READ_BUFFER_BYTES);

            if (response_.len() == 0) {
                // go straight to reading again. this was probably an oneway method
                state_ = FrameBufferState.AWAITING_REGISTER_READ;
                buffer_ = null;
            } else {
                buffer_ = ByteBuffer.wrap(response_.get(), 0, response_.len());

                // set state that we're waiting to be switched to write. we do this
                // asynchronously through requestSelectInterestChange() because there is
                // a possibility that we're not in the main thread, and thus currently
                // blocked in select(). (this functionality is in place for the sake of
                // the HsHa server.)
                state_ = FrameBufferState.AWAITING_REGISTER_WRITE;
            }
            requestSelectInterestChange();
        }

        /**
         * Actually invoke the method signified by this FrameBuffer.
         */
        public void invoke() {
            TTransport inTrans = getInputTransport();
            MtraceServerTBinaryProtocol inProt = (MtraceServerTBinaryProtocol)inputProtocolFactory_.getProtocol(inTrans);
            if (unifiedProto) {
                inProt.setUnifiedProto(true);
                inProt.setBodyBeginPos(bodyBeginPos);
                if(null != header) {
                    inProt.setHeaderInfo(header);
                }
            }

            CustomizedServerTFramedTransport serverTFramedTransport = (CustomizedServerTFramedTransport)getOutputTransport();
            MtraceServerTBinaryProtocol outProt = (MtraceServerTBinaryProtocol)outputProtocolFactory_.getProtocol(serverTFramedTransport);

            try {
                if(inTrans.getBytesRemainingInBuffer() < 4 ) {
                    outProt.getTransport().flush();
                } else {
                    processorFactory_.getProcessor(inTrans).process(inProt, outProt);
                }
                responseReady();
                return;
            } catch (TException te) {
                LOGGER.warn("Exception while invoking!", te);
            } catch (Exception e) {
                LOGGER.error("Unexpected throwable while invoking!", e);
            }
            // This will only be reached when there is a throwable.
            state_ = FrameBufferState.AWAITING_CLOSE;
            requestSelectInterestChange();
        }

        private boolean getUnifiedProto() {
            return unifiedProto;
        }

        public byte getMessageType() {
            return messageType;
        }

        public HeartbeatInfo getHeartbeatInfo() {
            return heartbeatInfo;
        }

        /**
         * Wrap the read buffer in a memory-based transport so a processor can read
         * the data it needs to handle an invocation.
         */
        private TTransport getInputTransport() {
            if(unifiedProto) {
                return new TMemoryInputTransport(buffer_.array(), bodyBeginPos, bodyLength);
            } else {
                return new TMemoryInputTransport(buffer_.array());
            }

        }

        /**
         * Get the transport that should be used by the invoker for responding.
         */
        private TTransport getOutputTransport() {
            response_ = new TByteArrayOutputStream();
            CustomizedServerTFramedTransport transport = (CustomizedServerTFramedTransport) outputTransportFactory_.getTransport(new TIOStreamTransport(response_));
            transport.setUnifiedProto(getUnifiedProto());
            transport.setProtocol(protocol);
            transport.setSequenceId(getSequenceId());
            transport.setMessageType(getMessageType());
            transport.setHeartbeatInfo(getHeartbeatInfo());
            transport.setRequestSize(requestSize);
            transport.setBeginTime(System.currentTimeMillis());
            return transport;
        }

        /**
         * Perform a read into buffer.
         *
         * @return true if the read succeeded, false if there was an error or the
         * connection closed.
         */
        private boolean internalRead() {
            try {
                if (trans_.read(buffer_) < 0) {
                    return false;
                }
                return true;
            } catch (IOException e) {
                LOGGER.warn("Got an IOException in internalRead!");
                return false;
            }
        }

        /**
         * Perform a read into buffer.
         *
         * @return true if the read succeeded, false if there was an error or the
         * connection closed.
         */
        private boolean internalRead(ByteBuffer byteBuffer) {
            try {
                if (trans_.read(byteBuffer) < 0) {
                    return false;
                }
                return true;
            } catch (IOException e) {
                LOGGER.warn("Got an IOException in internalRead!", e);
                return false;
            }
        }

        /**
         * We're done writing, so reset our interest ops and change state
         * accordingly.
         */
        private void prepareRead() {
            // we can set our interest directly without using the queue because
            // we're in the select thread.
            selectionKey_.interestOps(SelectionKey.OP_READ);
            // get ready for another go-around
            buffer_ = ByteBuffer.allocate(4);
            state_ = FrameBufferState.READING_FRAME_SIZE;
        }

        /**
         * When this FrameBuffer needs to change its select interests and execution
         * might not be in its select thread, then this method will make sure the
         * interest change gets done when the select thread wakes back up. When the
         * current thread is this FrameBuffer's select thread, then it just does the
         * interest change immediately.
         */
        private void requestSelectInterestChange() {
            if (Thread.currentThread() == this.selectThread_) {
                changeSelectInterests();
            } else {
                this.selectThread_.requestSelectInterestChange(this);
            }
        }
    } // FrameBuffer
}

