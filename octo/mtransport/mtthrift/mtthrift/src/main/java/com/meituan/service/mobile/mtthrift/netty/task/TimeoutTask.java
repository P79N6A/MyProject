package com.meituan.service.mobile.mtthrift.netty.task;

import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.netty.exception.RequestTimeoutException;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcRequest;
import com.meituan.service.mobile.mtthrift.netty.metadata.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/6/1
 * Time: 19:20
 */
public class TimeoutTask implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(TimeoutTask.class);
    private static ConcurrentMap<Long, RpcRequest> requestMap = ContextStore.getRequestMap();
    private static ConcurrentMap<Long, BlockingQueue<RpcResponse>> responseMap = ContextStore.getResponseMap();

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(ThriftClientGlobalConfig.getTimeoutInterval());
                long currentTime = System.currentTimeMillis();

                for (Long sequence : requestMap.keySet()) {
                    RpcRequest request = requestMap.get(sequence);
                    if (request != null) {
                        if (request.getTimeoutMillis() > 0 && request.getStartMillis() > 0
                                && request.getStartMillis() + request.getTimeoutMillis() < currentTime) {
                            disposeRequest(request);
                            requestMap.remove(sequence);

                            if (ThriftClientGlobalConfig.isTimeoutLog()) {
                                StringBuilder msg = new StringBuilder();
                                msg.append("remove timeout request, process time:").append(System.currentTimeMillis())
                                        .append("\r\n").append("request:").append(request);
                                logger.warn(msg.toString());
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                logger.warn("checking remote call timeout failed", e);
            }
        }
    }

    private void disposeRequest(RpcRequest request) {
        RpcResponse response = null;
        if (request.isAsync()) {
            request.getFuture().setException(new RequestTimeoutException());
            if (request.getCallback() != null) {
                request.getCallback().onError(new RequestTimeoutException());
            }
        } else {
            response = new RpcResponse(request);
            response.setException(new RequestTimeoutException());

            Long seq = request.getSeq();
            if (requestMap.get(seq) != null) {
                responseMap.putIfAbsent(seq, new LinkedBlockingQueue<RpcResponse>(1));
                responseMap.get(seq).add(response);
            }
        }
    }

}
