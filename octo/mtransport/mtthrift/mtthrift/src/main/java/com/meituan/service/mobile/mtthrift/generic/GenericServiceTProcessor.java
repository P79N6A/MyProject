package com.meituan.service.mobile.mtthrift.generic;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.facebook.swift.codec.ThriftCodec;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.codec.internal.TProtocolReader;
import com.facebook.swift.codec.internal.TProtocolWriter;
import com.facebook.swift.codec.internal.builtin.StringThriftCodec;
import com.facebook.swift.codec.metadata.ThriftFieldMetadata;
import com.google.common.collect.ImmutableMap;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.annotation.metadata.ThriftMethodMetadata;
import com.meituan.service.mobile.mtthrift.annotation.metadata.ThriftServiceMetadata;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.degrage.ServerDegradHandler;
import com.meituan.service.mobile.mtthrift.degrage.ServiceDegradeException;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceUtils;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerInvoker;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServiceBean;
import com.meituan.service.mobile.mtthrift.server.netty.NettyServer;
import com.meituan.service.mobile.mtthrift.util.ClassLoaderUtil;
import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.meituan.service.mobile.mtthrift.util.MethodUtil;
import com.meituan.service.mobile.mtthrift.util.SizeUtil;
import com.meituan.service.mobile.mtthrift.util.json.JacksonUtils;
import com.sankuai.octo.protocol.StatusCode;
import org.apache.thrift.*;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.meituan.service.mobile.mtthrift.util.Consts.*;

public class GenericServiceTProcessor implements TProcessor {
    private static final Logger logger = LoggerFactory.getLogger(GenericServiceTProcessor.class);
    private static final String resultStructName = GenericService.class.getName() + ".$invoke_result";
    private static final String resultFieldName = "success";
    private static final short resultFieldId = 0;
    private static final ThriftCodec resultCodec = new StringThriftCodec();

    public static ThreadLocal<String> genericTypeThreadLocal = new ThreadLocal<String>();

    private Class<?> serviceInterface;
    private ThriftServiceBean serviceBean;
    private Object service;
    private String serviceName;
    private String simpleServiceName;
    private ServerDegradHandler serverDegradHandler;
    private ImmutableMap<Short, ThriftCodec<?>> parameterCodecs;
    private ImmutableMap<String, Method> methodImmutableMap;

    public GenericServiceTProcessor(NettyServer server, Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
        this.serviceName = serviceInterface.getName();
        this.simpleServiceName = serviceInterface.getSimpleName();

        this.serviceBean = server.getServiceBeanMap().get(serviceInterface);

        if (serviceBean == null || (this.service = serviceBean.getServiceImpl()) == null) {
            throw new IllegalArgumentException(
                    "generic service failed，can not find serviceInterface: " + serviceInterface);
        }

        this.serverDegradHandler = new ServerDegradHandler(null, server.getAppKey());

        ImmutableMap.Builder<String, Method> methodMapBuilder = ImmutableMap.builder();
        for (Method method : service.getClass().getMethods()) {
            methodMapBuilder.put(MethodUtil.generateMethodSignature(method), method);
        }
        methodImmutableMap = methodMapBuilder.build();

        ThriftCodecManager codecManager = new ThriftCodecManager();
        ThriftServiceMetadata serviceMetadata = new ThriftServiceMetadata(GenericService.class,
                codecManager.getCatalog());
        for (ThriftMethodMetadata methodMetadata : serviceMetadata.getMethods().values()) {
            ImmutableMap.Builder<Short, ThriftCodec<?>> builder = ImmutableMap.builder();
            for (ThriftFieldMetadata fieldMetadata : methodMetadata.getParameters()) {
                builder.put(fieldMetadata.getId(), codecManager.getCodec(fieldMetadata.getThriftType()));
            }
            parameterCodecs = builder.build();
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(final TProtocol inProtocol, final TProtocol outProtocol) throws TException {
        TMessage request = null;
        String messageName = null;
        int messageSeq = 0;

        Transaction transaction = null;
        try {
            request = inProtocol.readMessageBegin();
            messageName = request.name;
            messageSeq = request.seqid;

            Object[] args = readArgs(inProtocol);
            String methodName = ((String) args[0]);
            List<String> parameterTypesArg = ((List<String>) args[1]);
            List<String> parametersArg = ((List<String>) args[2]);

            transaction = this.doCatLog(methodName, inProtocol);

            Object result = doProcess(methodName, parameterTypesArg, parametersArg);
            writeResponse(result, messageName, messageSeq, outProtocol);

            inProtocol.readMessageEnd();
            transaction.setStatus(Message.SUCCESS);
            MtraceUtils.serverMark(Tracer.STATUS.SUCCESS);
            ThriftServerInvoker.statusCode.set(StatusCode.Success);
        } catch (Exception e) {
            Throwable toThrow = e;
            if (e instanceof InvocationTargetException) {
                toThrow = ((InvocationTargetException) e).getTargetException();
            }

            ThriftServerInvoker.statusCode.set(ThriftServerInvoker.getStatusCodeByException(toThrow));
            ThriftServerInvoker.errorMessage.set(toThrow.getMessage());

            if (isUserDefinedException(toThrow)) {
                MtraceUtils.serverMark(Tracer.STATUS.SUCCESS);
            } else if (toThrow instanceof ServiceDegradeException) {
                MtraceUtils.serverMark(Tracer.STATUS.DROP);
            } else {
                logger.error("mtthrift server invoker Exception:" + e.getMessage() + ", cause:" + toThrow.getMessage(),
                        toThrow);
                MtraceUtils.serverMark(Tracer.STATUS.EXCEPTION);
            }

            if (request != null) {
                inProtocol.readMessageEnd();
                TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR,
                        toThrow.getClass().getName() + ":" + toThrow.getMessage());
                if (outProtocol instanceof MtraceServerTBinaryProtocol) {
                    (((MtraceServerTBinaryProtocol) outProtocol))
                            .rewriteMessageBegin(messageName, TMessageType.EXCEPTION, messageSeq);
                } else {
                    outProtocol.writeMessageBegin(new TMessage(messageName, TMessageType.EXCEPTION, messageSeq));
                }
                x.write(outProtocol);
                outProtocol.writeMessageEnd();
                outProtocol.getTransport().flush();
            } else {
                logger.error(toThrow.getMessage(), toThrow);
            }

            if (transaction != null) {
                transaction.setStatus(toThrow);
            }
        } finally {
            if (transaction != null) {
                transaction.complete();
            }
        }
        return true;
    }

    private static boolean isUserDefinedException(Throwable toThrow) {
        return toThrow instanceof TBase || toThrow.getClass().getAnnotation(ThriftStruct.class) != null;
    }

    private Transaction doCatLog(String methodName, TProtocol inProtocol) {
        Transaction transaction = Cat
                .newTransaction("OctoService", serviceInterface.getSimpleName() + "." + methodName + "[generic]");
        Cat.logEvent("OctoService.appkey", ClientInfoUtil.getClientAppKey());
        Cat.logEvent("OctoService.clientIp", ClientInfoUtil.getClientIp());
        Cat.logEvent("OctoService.requestSize", SizeUtil.getLogSize(inProtocol.getTransport().getBuffer().length));
        Cat.logEvent("OctoService.generic", "true");
        Cat.logEvent("OctoService.genericService.methodName", methodName);
        return transaction;
    }

    private Object[] readArgs(TProtocol inProtocol) throws Exception {
        Object[] args = new Object[3];
        TProtocolReader reader = new TProtocolReader(inProtocol);

        // Map incoming arguments from the ID passed in on the wire to the position in the
        // java argument list we expect to see a parameter with that ID.
        reader.readStructBegin();
        while (reader.nextField()) {
            short fieldId = reader.getFieldId();

            ThriftCodec<?> codec = parameterCodecs.get(fieldId);
            if (codec == null) {
                // unknown field
                reader.skipFieldData();
            } else {
                args[fieldId - 1] = reader.readField(codec);
            }
        }
        reader.readStructEnd();
        return args;
    }

    private Object doProcess(String methodName, List<String> parameterTypesArg, List<String> parametersArg)
            throws Exception {
        checkDegrade(methodName);
        Class[] parameterTypes = new Class[parameterTypesArg.size()];
        Object[] parameters = new Object[parametersArg.size()];
        for (int i = 0; i < parameterTypesArg.size(); i++) {
            String className = parameterTypesArg.get(i);
            Class<?> clazz = ClassLoaderUtil.loadClass(className);
            parameterTypes[i] = clazz;
            parameters[i] = jsonToObject(parametersArg.get(i), clazz);
        }

        String methodSignature = MethodUtil.generateMethodSignature(methodName, parameterTypes);
        Method method = methodImmutableMap.get(methodSignature);
        if (method == null) {
            throw new NoSuchMethodException(
                    "generic service failed，can not find method with signature: " + serviceName + "."
                            + methodSignature);
        }
        return method.invoke(service, parameters);
    }

    private void checkDegrade(String methodName) throws TException {
        if (ThriftServerGlobalConfig.isEnableDegradation()) {
            boolean degrade = this.serverDegradHandler.checkDegradeEvent(simpleServiceName, methodName);

            if (degrade) {
                String log =
                        "Request is degraded!" + ClientInfoUtil.getClientIp() + "|" + ClientInfoUtil.getClientAppKey()
                                + "|" + methodName;
                ThriftServerInvoker.isDrop.set(true);
                Cat.logEvent("OctoService.handleType", "drop");
                throw new ServiceDegradeException(log);
            } else {
                Cat.logEvent("OctoService.handleType", "accept");
                ThriftServerInvoker.isDrop.set(false);
            }
        }
    }

    private Object jsonToObject(String value, Class<?> type) {
        String genericType = genericTypeThreadLocal.get();
        if (GENERIC_TYPE_SIMPLE.equals(genericType)) {
            return JacksonUtils.simpleDeserialize(value, type);
        }
        return JacksonUtils.deserialize(value, type);
    }

    private void writeResponse(Object result, String messageName, int messageSeq, TProtocol outProtocol)
            throws Exception {
        outProtocol.writeMessageBegin(new TMessage(messageName, TMessageType.REPLY, messageSeq));

        TProtocolWriter writer = new TProtocolWriter(outProtocol);
        writer.writeStructBegin(resultStructName);
        String json;
        String genericType = genericTypeThreadLocal.get();

        if (GENERIC_TYPE_DEFAULT.equals(genericType)) {
            json = JacksonUtils.serialize(result);
        } else if (GENERIC_TYPE_COMMON.equals(genericType) || GENERIC_TYPE_SIMPLE.equals(genericType)) {
            if (result instanceof TBase) {
                json = new TSerializer(new TSimpleJSONProtocol.Factory()).toString(((TBase) result));
            } else {
                json = JacksonUtils.simpleSerialize(result);
            }
        } else {
            logger.warn("unknown genericType:{}, returning customized json", genericType);
            json = JacksonUtils.serialize(result);
        }

        writer.writeField(resultFieldName, resultFieldId, resultCodec, json);
        writer.writeStructEnd();

        outProtocol.writeMessageEnd();

        outProtocol.getTransport().flush();
    }
}
