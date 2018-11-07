package com.meituan.service.mobile.mtthrift.server.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.service.mobile.mtthrift.server.http.meta.DefaultHttpResponse;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.json.JacksonUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/2
 */
public class NettyHttpSender {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpSender.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    private HttpRequest request;
    private Channel channel;

    public NettyHttpSender(Channel channel, HttpRequest request) {
        this.channel = channel;
        this.request = request;
    }

    public void sendObjectJson(Object object) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(object);
            DefaultHttpResponse response = new DefaultHttpResponse(bytes, Consts.CONTENT_TYPE_JSON);
            send(response);
        } catch (JsonProcessingException e) {
            logger.error("Object to json fail", e);
            sendErrorResponse("Server handle fail: transform object to json fail");
        }
    }

    public void sendCustomObjectJson(Object object) {
        try {
            String result = JacksonUtils.serialize(object);
            String returnMessage = genReturnMessage(true, result);
            DefaultHttpResponse response = new DefaultHttpResponse(returnMessage.getBytes("UTF-8"), Consts.CONTENT_TYPE_JSON);
            send(response);
        } catch (Exception e) {
            logger.error("Object to json fail", e);
            sendErrorResponse("Server handle fail: transform object to json fail");
        }
    }

    public void send(DefaultHttpResponse httpResponse) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(httpResponse.getContent()));
        setHeaders(response, httpResponse.getContentType());
        channel.writeAndFlush(response);
    }

    public void sendErrorResponse(String errorMsg) {
        FullHttpResponse response = null;
        try {
            String returnMessage = genReturnMessage(false, errorMsg);
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(returnMessage.getBytes("UTF-8")));
            setHeaders(response, Consts.CONTENT_TYPE_JSON);
            channel.writeAndFlush(response);
        } catch (Exception e) {
            logger.error("Send error response fail", e);
        }
    }

    public String getClientIP() {
        String ip = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        return ip;
    }

    private void setHeaders(FullHttpResponse response, String contentType) {
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        if (HttpHeaders.isKeepAlive(request)) {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
    }

    private String genReturnMessage(boolean isSuccess, String result) throws JsonProcessingException {
        ReturnMessage returnMessage = new ReturnMessage(isSuccess, result);
        return objectMapper.writeValueAsString(returnMessage);
    }

    class ReturnMessage {
        private Boolean success;
        private String result;

        public ReturnMessage(Boolean success, String result) {
            this.success = success;
            this.result = result;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            success = success;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
