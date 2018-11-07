package com.meituan.service.mobile.mtthrift.mtrace;

import com.meituan.mtrace.*;
import com.meituan.service.mobile.mtthrift.client.cluster.MtThrfitInvokeInfo;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.ContextUtil;
import com.meituan.service.mobile.mtthrift.util.MtThriftManifest;
import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.TraceInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * User: YangXuehua
 * Date: 13-12-9
 * Time: 下午2:37
 */
public class MtraceClientTBinaryProtocol extends TBinaryProtocol {
    private static final Logger LOG = LoggerFactory.getLogger(MtraceClientTBinaryProtocol.class);
    public static final org.apache.thrift.protocol.TField MTRACE_FIELD_DESC = new org.apache.thrift.protocol.TField("mtrace", org.apache.thrift.protocol.TType.STRUCT,
            (short) 32767);

    private Endpoint localEndpoint;
    private String clusterManager;
    private boolean uniProto = false;

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

    public String getClusterManager() {
        return clusterManager;
    }

    public void setClusterManager(String clusterManager) {
        this.clusterManager = clusterManager;
    }

    public boolean isUniProto() {
        return uniProto;
    }

    public void setUniProto(boolean uniProto) {
        this.uniProto = uniProto;
    }

    /**
     * Factory
     */
    public static class Factory implements TProtocolFactory {
        protected boolean strictRead_ = false;
        protected boolean strictWrite_ = true;
        protected int readLength_;
        protected transient Endpoint localEndpoint;

        public Factory() {
            this(false, true);
        }

        public Factory(Endpoint endpoint) {
            strictRead_ = false;
            strictWrite_ = true;
            readLength_ = 0;
            localEndpoint = endpoint;
        }

        public Factory(boolean strictRead, boolean strictWrite) {
            this(strictRead, strictWrite, 0);
        }

        public Factory(boolean strictRead, boolean strictWrite, int readLength) {
            strictRead_ = strictRead;
            strictWrite_ = strictWrite;
            readLength_ = readLength;
        }

        public TProtocol getProtocol(TTransport trans) {
            MtraceClientTBinaryProtocol proto = new MtraceClientTBinaryProtocol(trans, strictRead_, strictWrite_);
            if (readLength_ != 0) {
                proto.setReadLength(readLength_);
            }
            proto.localEndpoint = localEndpoint;
            proto.mtThrfitInvokeInfo = MtThrfitInvokeInfo.getMtThrfitInvokeInfo();
            return proto;
        }
    }

    private MtThrfitInvokeInfo mtThrfitInvokeInfo;

    public MtraceClientTBinaryProtocol(TTransport trans) {
        super(trans);
    }

    public MtraceClientTBinaryProtocol(TTransport trans, MtThrfitInvokeInfo mtThrfitInvokeInfo) {
        super(trans);
        if(null != mtThrfitInvokeInfo) {
            this.mtThrfitInvokeInfo = mtThrfitInvokeInfo;
            setUniProto(mtThrfitInvokeInfo.isUniProto());
        }

    }

    public MtraceClientTBinaryProtocol(TTransport trans, boolean strictRead, boolean strictWrite) {
        super(trans, strictRead, strictWrite);
    }

    @Override
    public void writeMessageBegin(TMessage message) throws TException {
        super.writeMessageBegin(message);
        if(uniProto)
            return;

        this.writeMtrace(message); // 发送请求时写入trace信息
    }

    protected void writeMtrace(TMessage message) throws TException {
        RequestHeader requestHeader = new RequestHeader();
        if (mtThrfitInvokeInfo != null) {
            requestHeader.setServerIpPort( mtThrfitInvokeInfo.getServerIp() + ":" + mtThrfitInvokeInfo.getServerPort());
            requestHeader.setClientIp(mtThrfitInvokeInfo.getClientIp());
        } else {
            requestHeader.setClientIp(ProcessInfoUtil.getLocalIpV4());
        }

        if (ThriftClientGlobalConfig.isEnableMtrace()) {
            // trace 参数初始化
            ITracer tracer = Tracer.getClientTracer();
            Span span = tracer.getSpan();

            Map<String, String> localContext = ContextUtil.getLocalContext();
            if (localContext != null && !localContext.isEmpty()) {
                for (Map.Entry<String, String> entry : localContext.entrySet()) {
                    tracer.putRemoteOneStepContext(entry.getKey(), entry.getValue());
                }
            }

            // 装填 需要网络传输 RequestHeader 参数
            requestHeader.setTraceId(tracer.getTraceId());
            requestHeader.setSpanId(tracer.getSpanId());
            requestHeader.setClientAppkey(tracer.getLocalAppKey());
            if (span != null) {
                requestHeader.setSpanName(span.getSpanName());
                requestHeader.setVersion(span.getVersion());
                requestHeader.setSample(span.isSample());
                requestHeader.setDebug(span.isDebug());

                Map<String, String> mtraceForeverContext = span.getForeverContext();
                Map<String, String> mtraceOneStepContext = span.getRemoteOneStepContext();

                if (mtraceForeverContext != null) {
                    requestHeader.setGlobalContext(mtraceForeverContext);
                }
                if (mtraceOneStepContext != null) {
                    requestHeader.setLocalContext(mtraceOneStepContext);
                }
            }
        }

        super.writeFieldBegin(MTRACE_FIELD_DESC);
        requestHeader.write(this);
        super.writeFieldEnd();
    }

    @Override
    public void readMessageEnd() {
        super.readMessageEnd();
        try {
            readMtrace();
        } catch (TException e) {
            LOG.warn("readMessageEnd failed...", e);
        }
    }

    private void readMtrace() throws TException {
        RequestHeader requestHeader = null;
        if (peekFieldIsTrace()) {
            trans_.consumeBuffer(3);// 实际等价于readFieldBegin()
            requestHeader = new RequestHeader();
            requestHeader.read(this);
            super.writeFieldEnd();
        }

        if (ThriftClientGlobalConfig.isEnableMtrace()) {
            Span span = Tracer.getClientSpan();

            TTransport transport = getTransport();
            if (transport instanceof CustomizedTFramedTransport) {
                Header header = ((CustomizedTFramedTransport) transport).getHeaderInfo();
                if (header != null) {
                    TraceInfo traceInfo = header.getTraceInfo();
                    if (traceInfo != null && span != null) {
                        span.setDebug(traceInfo.isDebug());
                    }
                }
            }

            int frameSize = getTransport().getBuffer().length;
            int responseSize = 0;
            if (uniProto) {
                responseSize = frameSize + 6 + Consts.totalLenBytesCount;
            } else {
                responseSize = frameSize + 4;
            }
            if (span != null) {
                span.setPackageSize(responseSize);
            }


            if (LocalPointConf.isTraceLog() && span != null) {
                span.setPackageSize(this.getTransport().getBytesRemainingInBuffer());
            }
        }

        MtThrfitInvokeInfo.clearMtThrfitInvokeInfo();

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

}
