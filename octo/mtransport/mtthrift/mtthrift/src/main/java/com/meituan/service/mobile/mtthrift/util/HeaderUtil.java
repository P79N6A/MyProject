package com.meituan.service.mobile.mtthrift.util;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerInvoker;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.HeartbeatInfo;
import com.sankuai.octo.protocol.MessageType;
import com.sankuai.octo.protocol.RequestInfo;
import com.sankuai.octo.protocol.ResponseInfo;
import com.sankuai.octo.protocol.StatusCode;
import com.sankuai.octo.protocol.TraceInfo;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class HeaderUtil {

    private final static Logger logger = LoggerFactory.getLogger(HeaderUtil.class);

    public static Header headerAsRequest(long sequenceId, String serviceName, TraceInfo traceInfo) {
        return headerAsRequest(sequenceId, serviceName, 0,traceInfo, MessageType.Normal);
    }

    public static Header headerAsRequest(long sequenceId, String serviceName, int timeout,TraceInfo traceInfo, MessageType messageType) {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setSequenceId(sequenceId);
        requestInfo.setServiceName(serviceName);
        requestInfo.setTimeout(timeout);
        Header header = new Header();
        header.setRequestInfo(requestInfo);
        header.setTraceInfo(traceInfo);
        header.setMessageType((byte)messageType.getValue());
        Span span = Tracer.getClientSpan();
        if (span != null) {
            header.setLocalContext(span.getRemoteOneStepContext());
            header.setGlobalContext(span.getForeverContext());
        } else {
            if (header.globalContext == null) {
                header.setGlobalContext(new HashMap<String, String>());
            }
            if (header.localContext == null) {
                header.setLocalContext(new HashMap<String, String>());
            }
        }
        return header;
    }


    public static Header headerAsResponse(long sequenceId, TraceInfo traceInfo, byte messageType, HeartbeatInfo heartbeatInfo) {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setSequenceId(sequenceId);

        StatusCode statusCode = ThriftServerInvoker.statusCode.get();
        if (statusCode != null) {
            responseInfo.setStatus(((byte) statusCode.getValue()));
        }

        String errorMessage = ThriftServerInvoker.errorMessage.get();
        if (errorMessage != null) {
            responseInfo.setMessage(errorMessage);
        }

        Header header = new Header();
        header.setResponseInfo(responseInfo);
        header.setTraceInfo(traceInfo);
        header.setMessageType(messageType);
        header.setHeartbeatInfo(heartbeatInfo);
        return header;
    }

    public static byte[] headerSerialize(Header header) throws TException {
        TSerializer serializer = new TSerializer();
        return serializer.serialize(header);
    }

    public static Header headerDeserialize(byte[] headerBytes) throws TException {
        TDeserializer deserializer = new TDeserializer();
        Header header = new Header();
        deserializer.deserialize(header, headerBytes);
        return header;
    }

}
