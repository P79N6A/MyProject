package com.meituan.service.mobile.mtthrift.httpproxy;

import com.facebook.swift.codec.ThriftCodecManager;
import com.google.common.collect.ImmutableMap;
import com.meituan.service.mobile.mtthrift.annotation.ThriftMethodProcessor;
import com.meituan.service.mobile.mtthrift.annotation.metadata.ThriftMethodMetadata;
import com.meituan.service.mobile.mtthrift.annotation.metadata.ThriftServiceMetadata;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.util.AnnotationUtil;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-7-28
 * Time: 下午5:22
 */
public class ThriftServletServerProxy extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftServletServerProxy.class);

    private final TProcessor processor;
    private final TProtocolFactory inProtocolFactory;
    private final TProtocolFactory outProtocolFactory;
    private final Map<String, String> customHeaders;

    private String appKey;
    private Class<?> serviceInterface;
    private Object serviceImpl;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        try {
            response.setContentType("application/x-thrift");
            if (null != this.customHeaders) {
                for (Map.Entry<String, String> entry : customHeaders
                        .entrySet()) {
                    response.addHeader(entry.getKey(), entry.getValue());
                }
            }

            ServletInputStream te1 = request.getInputStream();
            ServletOutputStream out1 = response.getOutputStream();
            TIOStreamTransport transport = new TIOStreamTransport(te1, out1);
            TProtocol inProtocol = this.inProtocolFactory
                    .getProtocol(transport);
            TProtocol outProtocol = this.outProtocolFactory
                    .getProtocol(transport);
            this.processor.process(inProtocol, outProtocol);
            out1.flush();
        } catch (TException e) {
            LOG.error(e.getMessage(), e);
            //TODO: 将异常信息传递到Client端
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void addCustomHeader(final String key, final String value) {
        this.customHeaders.put(key, value);
    }

    public void setCustomHeaders(Map<String, String> headers) {
        this.customHeaders.clear();
        this.customHeaders.putAll(headers);
    }

    public ThriftServletServerProxy(Class<?> serviceInterface,
            Class<?> serviceImpl) throws Exception {
        super();

        this.serviceInterface = serviceInterface;
        this.serviceImpl = serviceImpl.newInstance();

        if (AnnotationUtil.detectThriftAnnotation(serviceInterface)) {
            this.processor = new ThriftServiceProcessor();
        } else {
            this.processor = getProcessorConstructor()
                    .newInstance(this.serviceImpl);
        }

        this.inProtocolFactory = new MtraceServerTBinaryProtocol.Factory();
        this.outProtocolFactory = new MtraceServerTBinaryProtocol.Factory();
        this.customHeaders = new HashMap<String, String>();

    }

    public class ThriftServiceProcessor implements TProcessor {

        private final ThriftCodecManager codecManager = new ThriftCodecManager();
        private Map<String, ThriftMethodProcessor> methods;

        public ThriftServiceProcessor() {
            Map<String, ThriftMethodProcessor> processorMap = newHashMap();

            ThriftServiceMetadata serviceMetadata = new ThriftServiceMetadata(
                    serviceImpl.getClass(), codecManager.getCatalog());
            for (ThriftMethodMetadata methodMetadata : serviceMetadata
                    .getMethods().values()) {
                String methodName = methodMetadata.getName();
                ThriftMethodProcessor methodProcessor = new ThriftMethodProcessor(
                        serviceImpl, serviceMetadata.getName(), methodMetadata,
                        codecManager);
                if (processorMap.containsKey(methodName)) {
                    throw new IllegalArgumentException(
                            "Multiple @ThriftMethod-annotated methods named '"
                                    + methodName
                                    + "' found in the given services");
                }
                processorMap.put(methodName, methodProcessor);
            }

            methods = ImmutableMap.copyOf(processorMap);

        }

        @Override
        public boolean process(TProtocol in, TProtocol out)
                throws TException {
            TMessage message = in.readMessageBegin();
            String methodName = message.name;
            int sequenceId = message.seqid;// lookup method
            ThriftMethodProcessor method = methods.get(methodName);
            if (method == null) {
                TProtocolUtil.skip(in, TType.STRUCT);
                writeApplicationException(out, methodName, sequenceId,
                        TApplicationException.UNKNOWN_METHOD,
                        "Invalid method name: '" + methodName + "'", null);
                return false;
            }
            try {
                return method.process(in, out, sequenceId);
            } catch (Throwable e) {
                if (e instanceof TTransportException
                        || e instanceof TProtocolException || (
                        e instanceof TBase && e instanceof TException)) {
                    throw (TException) e;
                } else {
                    //捕获服务端抛出的未知异常，避免客户端释放连接
                    String eMessage = null;
                    if (in instanceof MtraceServerTBinaryProtocol
                            && (eMessage = getExceptionMessage(e)) != null) {
                        TMessage msg = ((MtraceServerTBinaryProtocol) in)
                                .reReadMessageBegin();
                        if (msg != null) {
                            //将服务端异常描述写入响应
                            TApplicationException x = new TApplicationException(
                                    TApplicationException.INTERNAL_ERROR,
                                    eMessage);
                            ((MtraceServerTBinaryProtocol) out)
                                    .rewriteMessageBegin(msg.name,
                                            TMessageType.EXCEPTION, msg.seqid);
                            x.write(out);
                            out.writeMessageEnd();
                            out.getTransport().flush();
                            LOG.debug("failed when process...", e);
                            return false;
                        }
                    }
                    throw new TException("server exception:" + e.getMessage(),
                            e);
                }
            }
        }

        public TApplicationException writeApplicationException(
                TProtocol outputProtocol,
                String methodName,
                int sequenceId,
                int errorCode,
                String errorMessage,
                Throwable cause)
                throws TException {
            // unexpected exception
            TApplicationException applicationException = new TApplicationException(
                    errorCode, errorMessage);
            if (cause != null) {
                applicationException.initCause(cause);
            }

            LOG.error(applicationException.getMessage(), errorMessage);

            // Application exceptions are sent to client, and the connection can be reused
            outputProtocol.writeMessageBegin(
                    new TMessage(methodName, TMessageType.EXCEPTION,
                            sequenceId));
            applicationException.write(outputProtocol);
            outputProtocol.writeMessageEnd();
            outputProtocol.getTransport().flush();

            return applicationException;
        }
    }

    private String getExceptionMessage(Throwable e) {
        StackTraceElement[] stacks = e.getStackTrace();
        if (stacks != null && stacks.length > 0) {
            StackTraceElement stackTraceElement = stacks[0];
            return e.getClass().getName() + (e.getMessage() == null ?
                    "" :
                    ":" + e.getMessage()) + "(" + stackTraceElement
                    .getFileName() + "," + stackTraceElement.getMethodName()
                    + "() line " + stackTraceElement.getLineNumber() + ")";
        } else {
            return e.getClass().getName() + (e.getMessage() == null ?
                    "" :
                    ":" + e.getMessage());
        }
    }

    public Class<?> getIfaceInterface() {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes) {
            if (c.isMemberClass() && c.isInterface() && c.getSimpleName()
                    .equals("Iface")) {
                return c;
            }
        }
        throw new IllegalArgumentException(
                "serviceInterface must contain Sub Interface of Iface");
    }

    public Class<TProcessor> getProcessorClass() {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes) {
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName()
                    .equals("Processor")) {
                return c;
            }
        }
        throw new IllegalArgumentException(
                "serviceInterface must contain Sub Class of Processor");
    }

    public Constructor<TProcessor> getProcessorConstructor() {
        try {
            return getProcessorClass().getConstructor(getIfaceInterface());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalArgumentException(
                    "serviceInterface must contain Sub Class of Processor with Constructor(Iface.class)");
        }
    }
}
