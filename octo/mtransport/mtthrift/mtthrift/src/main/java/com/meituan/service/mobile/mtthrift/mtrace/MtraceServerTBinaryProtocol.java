package com.meituan.service.mobile.mtthrift.mtrace;

import com.meituan.mtrace.Endpoint;
import com.meituan.mtrace.Span;
import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.client.invoker.IMTThriftFilter;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.TraceInfo;
import com.meituan.service.mobile.mtthrift.transport.CustomizedServerTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.MtThriftManifest;
import com.meituan.service.mobile.mtthrift.util.TraceInfoUtil;
import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * User: YangXuehua
 * Date: 13-12-9
 * Time: 下午2:37
 */
public class MtraceServerTBinaryProtocol extends TBinaryProtocol {
    private final static Logger LOG = LoggerFactory.getLogger(MtraceServerTBinaryProtocol.class);

    private IMTThriftFilter reuqestFilter;
    private Class<?> serviceInterface;
    private String serviceSimpleName = "";
    static final org.apache.thrift.protocol.TField MTRACE_FIELD_DESC = new org.apache.thrift.protocol.TField("mtrace", org.apache.thrift.protocol.TType.STRUCT,
            (short) 32767);

    private Endpoint localEndpoint;
    private boolean serializeNullStringAsBlank;

    public void setLocalEndpoint(Endpoint localEndpoint) {
        this.localEndpoint = localEndpoint;
    }

    public Endpoint getLocalEndpoint() {
        if (localEndpoint != null) {
            return localEndpoint;
        } else {
            return LocalPointConf.getLocalEndpoint();
        }
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setServiceSimpleName(String serviceSimpleName) {
        this.serviceSimpleName = serviceSimpleName;
    }

    private static ThreadLocal<Boolean> isOldTrace = new ThreadLocal<Boolean>();
    public static final ThreadLocal<Boolean> requestDegraded = new ThreadLocal<Boolean>();
    public static final ThreadLocal<RequestHeader> requestHeaderInfo = new ThreadLocal<RequestHeader>();

    private static ThreadLocal<String> traceId = new ThreadLocal<String>();
    private static ThreadLocal<String> spanName = new ThreadLocal<String>();
    private static ThreadLocal<Boolean> debug = new ThreadLocal<Boolean>();
    private boolean unifiedProto = false;
    private Header headerInfo = null;
    private int bodyBeginPos = 0;


    private static String localIp = ProcessInfoUtil.getLocalIpV4();

    public boolean isSerializeNullStringAsBlank() {
        return serializeNullStringAsBlank;
    }

    public void setSerializeNullStringAsBlank(
            boolean serializeNullStringAsBlank) {
        this.serializeNullStringAsBlank = serializeNullStringAsBlank;
    }

    public void setHeaderInfo(Header header) {
        this.headerInfo = header;
    }


    public boolean isUnifiedProto() {
        return unifiedProto;
    }

    public void setUnifiedProto(boolean unifiedProto) {
        this.unifiedProto = unifiedProto;
    }

    public int getBodyBeginPos() {
        return bodyBeginPos;
    }

    public void setBodyBeginPos(int bodyBeginPos) {
        this.bodyBeginPos = bodyBeginPos;
    }

    /**
     * Factory
     */
    public static class Factory implements TProtocolFactory {
        protected boolean strictRead_ = false;
        protected boolean strictWrite_ = true;
        protected int readLength_;
        private transient IMTThriftFilter reuqestFilter;
        private Class<?> serviceInterface;
        private String serviceSimpleName = "";

        public Factory() {
            this(false, true);
        }

        public Factory(boolean strictRead, boolean strictWrite) {
            this(strictRead, strictWrite, 0);
        }

        public Factory(boolean strictRead, boolean strictWrite, int readLength) {
            strictRead_ = strictRead;
            strictWrite_ = strictWrite;
            readLength_ = readLength;
        }

        public Factory(boolean strictRead, boolean strictWrite, IMTThriftFilter reuqestFilter) {
            this(strictRead, strictWrite, 0);
            this.reuqestFilter = reuqestFilter;
        }

        public Factory(boolean strictRead, boolean strictWrite, Class<?> serviceInterface, String serviceSimpleName, Endpoint endpoint) {
            this(strictRead, strictWrite, 0);
            this.serviceInterface = serviceInterface;
            this.serviceSimpleName = serviceSimpleName;
            this.localEndpoint = endpoint;
        }

        public Factory(boolean strictRead, boolean strictWrite, IMTThriftFilter reuqestFilter, Class<?> serviceInterface
                , String serviceSimpleName) {
            this(strictRead, strictWrite, 0);
            this.reuqestFilter = reuqestFilter;
            this.serviceInterface = serviceInterface;
            this.serviceSimpleName = serviceSimpleName;
        }

        public TProtocol getProtocol(TTransport trans) {
            MtraceServerTBinaryProtocol proto = new MtraceServerTBinaryProtocol(trans, strictRead_, strictWrite_);
            if (readLength_ != 0) {
                proto.setReadLength(readLength_);
            }
            proto.reuqestFilter = reuqestFilter;
            proto.serviceInterface = serviceInterface;
            proto.serviceSimpleName = serviceSimpleName;
            proto.setLocalEndpoint(localEndpoint);
            proto.setSerializeNullStringAsBlank(serializeNullStringAsBlank);
            return proto;
        }

        private transient Endpoint localEndpoint;
        private boolean serializeNullStringAsBlank;

        public void setLocalEndpoint(Endpoint localEndpoint) {
            this.localEndpoint = localEndpoint;
        }

        public Class<?> getServiceInterface() {
            return serviceInterface;
        }

        public void setServiceInterface(Class<?> serviceInterface) {
            this.serviceInterface = serviceInterface;
        }

        public boolean isSerializeNullStringAsBlank() {
            return serializeNullStringAsBlank;
        }

        public void setSerializeNullStringAsBlank(
                boolean serializeNullStringAsBlank) {
            this.serializeNullStringAsBlank = serializeNullStringAsBlank;
        }
    }

    public MtraceServerTBinaryProtocol(TTransport trans) {
        super(trans);
    }

    public MtraceServerTBinaryProtocol(TTransport trans, boolean strictRead, boolean strictWrite) {
        super(trans, strictRead, strictWrite);
    }

    @Override
    public TMessage readMessageBegin() throws TException {

        isOldTrace.set(false);
        requestDegraded.set(false);
        requestHeaderInfo.set(null);

        Span span = Tracer.getServerSpan();
        int frameSize = getTransport().getBuffer().length;
        int requestSize = 0;
        if (unifiedProto) {
            requestSize = frameSize + 6 + Consts.totalLenBytesCount;
        } else {
            requestSize = frameSize + 4;
        }

        TMessage ret = super.readMessageBegin();
        ret = readMtrace(ret);

        if (span != null) {
            span.setPackageSize(requestSize);
        }
        return ret;
    }

    public TMessage reReadMessageBegin() throws TException {
        int bodyLength;
        int pos = trans_.getBufferPosition();
        if (unifiedProto) {
            bodyLength = pos - bodyBeginPos;
        } else {
            bodyLength = pos;
        }
        trans_.consumeBuffer(-1 * bodyLength);
        TMessage ret = super.readMessageBegin();
        trans_.consumeBuffer(bodyLength);

        return ret;
    }


    public TMessage readTMessage() throws TException {
        TMessage message = super.readMessageBegin();
        int pos = trans_.getBufferPosition();
        trans_.consumeBuffer(-1 * pos);
        return message;
    }

    private TMessage readMtrace(TMessage message) throws TException {

        RequestHeader requestHeader = null;
        String clientAppKey = "";
        String clientIp = "";

        if (peekFieldIsTrace()) {
            trans_.consumeBuffer(3);// 实际等价于readFieldBegin()
            requestHeader = new RequestHeader();
            requestHeader.read(this);
            super.writeFieldEnd();

            if (requestHeader.clientAppkey != null)
                clientAppKey = requestHeader.clientAppkey;
            if (requestHeader.clientIp != null)
                clientIp = requestHeader.clientIp;

            isOldTrace.set(true);
            requestHeaderInfo.set(requestHeader);

            TraceParam param = new TraceParam(requestHeader.getSpanName());
            param.setLocal(getLocalEndpoint().getAppkey(), localIp, getLocalEndpoint().getPort());
            param.setInfraName(Consts.mtraceInfra);
            param.setVersion(MtThriftManifest.getVersion());
            param.setPackageSize(this.getTransport().getBytesRemainingInBuffer());
            param.setTraceId(requestHeader.getTraceId());
            param.setSpanId(requestHeader.getSpanId());
            param.setRemoteAppKey(requestHeader.getClientAppkey());
            param.setRemoteIp(requestHeader.getClientIp());
            param.setSample(requestHeader.isSample());
            param.setDebug(requestHeader.isDebug());

            Span span = Tracer.serverRecv(param);
            traceId.set(span.getTraceId());
            spanName.set(span.getSpanName());
            debug.set(span.isDebug());

            span = Tracer.getServerTracer().getSpan();
            if (span != null) {
                Map<String, String> mtraceForeverContext = span.getForeverContext();
                Map<String, String> mtraceOneStepContext = span.getRemoteOneStepContext();

                Map<String, String> globalContext = requestHeader.getGlobalContext();
                if (mtraceForeverContext != null && globalContext != null) {
                    mtraceForeverContext.putAll(globalContext);
                }

                Map<String, String> requestContext = requestHeader.getLocalContext();
                if (mtraceOneStepContext != null && requestContext != null) {
                    mtraceOneStepContext.putAll(requestContext);
                }
            }

        } else if (null != headerInfo) {
            String spanname_ = "";
            if (null != this.serviceInterface)
                spanname_ = this.serviceSimpleName + "." + message.name;
            TraceInfo traceInfo = headerInfo.getTraceInfo();
            if (null != traceInfo) {
                clientIp = traceInfo.getClientIp();
                if (ThriftServerGlobalConfig.isEnableMtrace()) {
                    TraceInfoUtil.serverRecv(traceInfo, spanname_, getLocalEndpoint(), clientIp, this.getTransport().getBytesRemainingInBuffer(), headerInfo);
                }
                traceId.set(headerInfo.traceInfo.getTraceId());
                spanName.set(spanname_);
                debug.set(traceInfo.isDebug());
                // handle requestHeader info
                requestHeader = new RequestHeader();
                requestHeader.setClientAppkey(traceInfo.getClientAppkey());
                requestHeader.setClientIp(clientIp);
                requestHeader.setTraceId(traceInfo.getTraceId());
                requestHeaderInfo.set(requestHeader);
            }
        } else {
            TraceParam param = new TraceParam(message.name);
            param.setLocal(getLocalEndpoint().getAppkey(), localIp, getLocalEndpoint().getPort());
            param.setInfraName(Consts.mtraceInfra);
            param.setVersion(MtThriftManifest.getVersion());
            param.setPackageSize(this.getTransport().getBytesRemainingInBuffer());
            Tracer.serverRecv(param);
        }

        /*******************************框架拦截处理************************/
        MtthriftErrorCode rejectMessage = null;
        if ("_mtthriftReject".equals(message.name)) {
            rejectMessage = new MtthriftErrorCode(403, "method forbiden");
        } else if (reuqestFilter != null) {
            rejectMessage = reuqestFilter.isReject(message.name, clientAppKey, clientIp);
        }

        if (rejectMessage != null) {//伪造消息头
            TMemoryBuffer memoryBuffer = new TMemoryBuffer(512);
            TBinaryProtocol outProtocol = new TBinaryProtocol(memoryBuffer);
            outProtocol.writeByte(TType.STRING);
            outProtocol.writeI16((short) 1);
            outProtocol.writeString(message.name + ":" + rejectMessage.toString());
            outProtocol.writeByte(TType.I32);
            outProtocol.writeI16((short) 2);
            outProtocol.writeI32(message.seqid);
            outProtocol.writeByte(TType.STOP);

            int pos = 0;
            byte[] buffer = trans_.getBuffer();
            for (int i = 0; i < memoryBuffer.length(); i++) {
                buffer[pos + i] = memoryBuffer.getArray()[i];
            }
            trans_.consumeBuffer(trans_.getBufferPosition() * -1);
            message = new TMessage("_mtthriftReject", message.type, message.seqid);
        }
        return message;
    }

    public boolean peekFieldIsTrace() throws TException {
        boolean nextFieldIsTrace = false;
        if (trans_.getBytesRemainingInBuffer() >= 3) {
            byte type = trans_.getBuffer()[trans_.getBufferPosition()];
            if (type == org.apache.thrift.protocol.TType.STRUCT) {
                short id = (short) (((trans_.getBuffer()[trans_.getBufferPosition() + 1] & 0xff) << 8) | ((trans_.getBuffer()[trans_.getBufferPosition() + 2] & 0xff)));
                if (id == MtraceClientTBinaryProtocol.MTRACE_FIELD_DESC.id)
                    nextFieldIsTrace = true;
            }
        }
        return nextFieldIsTrace;
    }

    @Override
    public void writeMessageEnd() {
        //只有老协议方式才传输trace信息
        if (isOldTrace != null && isOldTrace.get()) {
            RequestHeader requestHeader = new RequestHeader();
            requestHeader.setTraceId(traceId.get());
            requestHeader.setSpanName(spanName.get());
            requestHeader.setDebug(debug.get());

            try {
                super.writeFieldBegin(MTRACE_FIELD_DESC);
                requestHeader.write(this);
                super.writeFieldEnd();
            } catch (TException e) {
                LOG.info("write exception...", e.getMessage());
            }
        }
    }

    public boolean rewriteMessageBegin(String methodName, byte type, int seqid) {
        try {
            if (trans_ instanceof TIOStreamTransport) {
                TIOStreamTransport transport = (TIOStreamTransport) (super.trans_);
                Field field = transport.getClass().getDeclaredField("outputStream_");
                field.setAccessible(true);
                TByteArrayOutputStream writeBuffer_ = (TByteArrayOutputStream) field.get(transport);
                writeBuffer_.reset();
                super.writeMessageBegin(new TMessage(methodName, type, seqid));
            } else {
                CustomizedServerTFramedTransport transport = (CustomizedServerTFramedTransport) (super.trans_);
                Field field = transport.getClass().getDeclaredField("writeBuffer_");
                field.setAccessible(true);
                TByteArrayOutputStream writeBuffer_ = (TByteArrayOutputStream) field.get(transport);
                writeBuffer_.reset();
                super.writeMessageBegin(new TMessage(methodName, type, seqid));
            }
            return true;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void writeString(String str) throws TException {
        try {
            byte[] dat = null;
            if (null != str) {
                dat = str.getBytes("UTF-8");
            } else if (null == str && serializeNullStringAsBlank) {
                dat = new byte[0];
            }
            // may cause NPE, however, this is thrift original implement.
            writeI32(dat.length);
            trans_.write(dat, 0, dat.length);
        } catch (UnsupportedEncodingException uex) {
            throw new TException("JVM DOES NOT SUPPORT UTF-8", uex);
        }
    }

}
