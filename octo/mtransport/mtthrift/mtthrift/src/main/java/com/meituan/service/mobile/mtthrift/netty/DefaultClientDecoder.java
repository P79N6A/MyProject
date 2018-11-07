package com.meituan.service.mobile.mtthrift.netty;

import com.google.common.util.concurrent.SettableFuture;
import com.meituan.service.mobile.mtthrift.annotation.ThriftMethodHandler;
import com.meituan.service.mobile.mtthrift.client.invoker.MTThriftAsyncMethodCallback;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceClientTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcRequest;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcResponse;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.sankuai.octo.protocol.Header;
import com.sankuai.octo.protocol.HeartbeatInfo;
import com.sankuai.sgagent.thrift.model.CustomizedStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-8-25
 * Time: 下午11:11
 */
public class DefaultClientDecoder extends ByteToMessageDecoder {
    private static ConcurrentMap<Class, Class> cachedClientClasses = new ConcurrentHashMap<Class, Class>();
    private static ConcurrentMap<Class, Constructor> cachedClientConstructors = new ConcurrentHashMap<Class, Constructor>();
    private static ConcurrentMap<Class, Field> cachedClientSeqField = new ConcurrentHashMap<Class, Field>();
    private static ConcurrentMap<String, Method> cachedRecvMethod = new ConcurrentHashMap<String, Method>();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        if (byteBuf.readableBytes() < 10) {
            return;
        }

        byteBuf.markReaderIndex();
        int readerIndex = byteBuf.readerIndex();

        byte[] first4bytes = new byte[4];
        byteBuf.getBytes(readerIndex, first4bytes);
        if (!(first4bytes[0] == Consts.first && first4bytes[1] == Consts.second)) {
            channelHandlerContext.close();
        }
        RpcRequest request = null;
        RpcResponse response = null;
        Long seqId = null;

        try {
            byte[] i32buf = new byte[4];
            byteBuf.getBytes(readerIndex + 4, i32buf);
            int size = CustomizedTFramedTransport.decodeFrameSize(i32buf, 0, 4);

            if (byteBuf.readableBytes() < size + 8) {
                byteBuf.resetReaderIndex();
                return;
            }

            ByteBufInputStream inputStream = new ByteBufInputStream(byteBuf);
            TIOStreamTransport transport = new TIOStreamTransport(inputStream);
            CustomizedTFramedTransport customizedTFramedTransport = new CustomizedTFramedTransport(transport);
            customizedTFramedTransport.readFrame();
            Header header = customizedTFramedTransport.getHeaderInfo();
            seqId = header.getResponseInfo().getSequenceId();
            HeartbeatInfo heartBeatInfo = header.getHeartbeatInfo();

            if (header.getHeartbeatInfo() != null) {
                SettableFuture<Boolean> settableFuture = ContextStore.getHeartbeatRequestFuture(seqId);
                if (settableFuture != null) {
                    settableFuture.set(heartBeatInfo.getStatus() == CustomizedStatus.ALIVE.getValue());
                }
                return;
            }

            request = ContextStore.getRequestById(seqId);
            if (request == null) {
                return;
            }

            response = new RpcResponse(request);
            response.setResponseSize(customizedTFramedTransport.getResponseSize());

            MtraceClientTBinaryProtocol mtraceClientTBinaryProtocol = new MtraceClientTBinaryProtocol(customizedTFramedTransport);

            if (request.isAnnotatedThrift()) {
                ThriftMethodHandler thriftMethodHandler = request.getThriftMethodHandler();
                response.setReturnVal(thriftMethodHandler.receive(mtraceClientTBinaryProtocol, request.getAnnoSeq()));
            } else {
                Class<?> serviceInterface = request.getServiceInterface();
                Class<?> syncClientClass = getSyncClientClass(serviceInterface);
                Constructor syncConstructor = getSyncConstructor(syncClientClass);

                TServiceClient serviceClient = (TServiceClient) syncConstructor.newInstance(mtraceClientTBinaryProtocol);

                //每发送一次请求即在sendBase()方法中，TServiceClient的seqid会自增1，
                //同时在解析响应即receiveBase()方法中会校验消息的seqid是否与自身的seqid一致。
                //因为每次请求都创建新的TServiceClient进行发送，所以接收响应时要将seqid设置为1以确保通过校验
                Field seqid_field = getClientSeqField(syncClientClass);
                seqid_field.set(serviceClient, 1);

                Method method = getRecvMethod(request.getMethodName(), syncClientClass);

                Object result = null;
                try {
                    result = method.invoke(serviceClient);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof TApplicationException) {
                        int type = ((TApplicationException) e.getCause()).getType();
                        if (type == TApplicationException.MISSING_RESULT) {
                            result = null;
                        } else {
                            throw e;
                        }
                    } else {
                        throw e;
                    }

                }
                response.setReturnVal(result);
            }
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                e = (Exception) e.getCause();
            }
            if (response != null) {
                response.setReturnVal(null);
                response.setException(e);
            }
        } finally {
            if (request != null && response != null) {
                if (request.isAsync()) {
                    if (response.getException() == null) {
                        request.getFuture().set(response.getReturnVal());
                    } else {
                        request.getFuture().setException(response.getException());
                    }
                    AsyncMethodCallback callback = request.getCallback();
                    if (callback != null) {
                        if (callback instanceof MTThriftAsyncMethodCallback) {
                            ((MTThriftAsyncMethodCallback) callback).setRequestSize(request.getRequestSize());
                            ((MTThriftAsyncMethodCallback) callback).setResponseSize(response.getResponseSize());
                        }
                        if (response.getException() == null) {
                            callback.onComplete(response.getReturnVal());
                        } else {
                            callback.onError(response.getException());
                        }
                    }

                    if (ContextStore.getRequestMap().containsKey(request.getSeq())) {
                        ContextStore.getRequestMap().remove(request.getSeq());
                    }
                } else {
                    ContextStore.getResponseMap().putIfAbsent(seqId, new LinkedBlockingQueue<RpcResponse>(1));
                    BlockingQueue<RpcResponse> queue = ContextStore.getResponseMap().get(seqId);
                    queue.add(response);
                    list.add(response);
                }
            }
        }
    }

    private Method getRecvMethod(String methodName, Class<?> syncClientClass) throws NoSuchMethodException {
        String className = syncClientClass.getName();
        String methodKey = className + "#" + methodName;
        Method method = cachedRecvMethod.get(methodKey);

        if (method == null) {
            method = syncClientClass.getMethod("recv_" + methodName);
            cachedRecvMethod.put(methodKey, method);
        }

        return method;
    }

    private Field getClientSeqField(Class syncClientClass) throws NoSuchFieldException {
        Field seqid_field = cachedClientSeqField.get(syncClientClass);
        if (seqid_field == null) {
            seqid_field = TServiceClient.class.getDeclaredField("seqid_");
            seqid_field.setAccessible(true);
            cachedClientSeqField.put(syncClientClass, seqid_field);
        }

        return seqid_field;
    }

    private Constructor getSyncConstructor(Class<?> syncClientClass) throws NoSuchMethodException {
        Constructor syncConstructor = cachedClientConstructors.get(syncClientClass);
        if (syncConstructor == null) {
            syncConstructor = syncClientClass.getConstructor(TProtocol.class);
            cachedClientConstructors.put(syncClientClass, syncConstructor);
        }

        return syncConstructor;
    }

    private Class<?> getSyncClientClass(Class<?> serviceInterface) {
        Class syncClientClass = cachedClientClasses.get(serviceInterface);

        if (syncClientClass == null) {
            Class<?>[] classes = serviceInterface.getClasses();
            for (Class c : classes) {
                if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("Client")) {
                    syncClientClass = c;
                    cachedClientClasses.put(serviceInterface, syncClientClass);
                }
            }
            if (syncClientClass == null) {
                throw new IllegalArgumentException("serviceInterface must contain Sub Class of Client");
            }
        }

        return syncClientClass;
    }

}
