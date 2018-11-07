package com.meituan.service.mobile.mtthrift.netty;

import com.google.common.util.concurrent.SettableFuture;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcRequest;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcResponse;
import com.meituan.service.mobile.mtthrift.netty.task.TimeoutTask;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ContextStore {

    private static final ConcurrentHashMap<Long, BlockingQueue<RpcResponse>> responseMap = new ConcurrentHashMap<Long, BlockingQueue<RpcResponse>>();
    private static final ConcurrentHashMap<Long, RpcRequest> requestMap = new ConcurrentHashMap<Long, RpcRequest>();
    private static final ConcurrentHashMap<Long, SettableFuture<Boolean>> heartbeatResultMap = new ConcurrentHashMap<Long, SettableFuture<Boolean>>();
    private static final ThreadLocal<OctoThriftCallback> threadLocalCallback = new ThreadLocal<OctoThriftCallback>();
    private static final ThreadLocal<Future> threadLocalFuture = new ThreadLocal<Future>();

    private static final Executor TIMEOUT_EXECUTOR = Executors.newCachedThreadPool(new MTDefaultThreadFactory("ContextStore-timeout"));

    static {
        TIMEOUT_EXECUTOR.execute(new TimeoutTask());
    }

    public static ConcurrentHashMap<Long, BlockingQueue<RpcResponse>> getResponseMap() {
        return responseMap;
    }

    public static ConcurrentHashMap<Long, RpcRequest> getRequestMap() {
        return requestMap;
    }


    public static OctoThriftCallback getCallback() {
        return threadLocalCallback.get();
    }

    public static void setCallBack(OctoThriftCallback callBack) {
        threadLocalCallback.set(callBack);
    }

    public static void removeCallback() {
        threadLocalCallback.remove();
    }

    public static Future getFuture() {
        return threadLocalFuture.get();
    }

    public static SettableFuture getSettableFuture() {
        return (SettableFuture) threadLocalFuture.get();
    }

    public static void setFuture(Future future) {
        threadLocalFuture.set(future);
    }

    public static void removeFuture() {
        threadLocalFuture.remove();
    }

    public static void putRequestIfAbsent(long seq, RpcRequest request) {
        requestMap.putIfAbsent(seq, request);
    }

    public static RpcRequest getRequestById(Long seqId) {
        return requestMap.get(seqId);
    }

    public static void removeRequest(long seq) {
        requestMap.remove(seq);
    }

    public static Future<Boolean> createHeartbeatRequest(Long seq) {
        SettableFuture<Boolean> future = SettableFuture.create();
        heartbeatResultMap.put(seq, future);
        return future;
    }

    public static SettableFuture<Boolean> getHeartbeatRequestFuture(Long seq) {
        return heartbeatResultMap.get(seq);
    }

    public static void removeHeartbeatRequestFuture(Long seq) {
        heartbeatResultMap.remove(seq);
    }

    public static Map<Long, SettableFuture<Boolean>> getHeartbeatResultMap() {
        return heartbeatResultMap;
    }
}
