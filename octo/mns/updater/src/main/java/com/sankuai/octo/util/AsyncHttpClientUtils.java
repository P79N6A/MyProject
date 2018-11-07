package com.sankuai.octo.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-7-1
 * Time: 下午5:16
 */
public class AsyncHttpClientUtils {

    public final static Logger logger = LoggerFactory.getLogger(AsyncHttpClientUtils.class);
    private static CloseableHttpAsyncClient httpAsyncClient = null;
    private static int maxTotal = 1000;
    private static int readTimeOutInMills = 150;
    private static int connectTimeOutInMills = 100;

    static {
        ConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor();
        } catch (IOReactorException e) {
            logger.error(e.getMessage(), e);
        }
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        cm.setMaxTotal(maxTotal);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeOutInMills).setConnectTimeout(connectTimeOutInMills).build();
        httpAsyncClient = HttpAsyncClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
        httpAsyncClient.start();
    }

    public static CloseableHttpAsyncClient getInstance() {
        return httpAsyncClient;
    }

    public static void destory() {
        try {
            httpAsyncClient.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        HttpGet request = new HttpGet("http://10.5.239.192:7878/api/alive");
        FutureCallback<HttpResponse> callback = new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse httpResponse) {
                System.out.println(httpResponse.getStatusLine().getStatusCode());
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void cancelled() {

            }
        };

        AsyncHttpClientUtils.getInstance().execute(request, callback);
        Thread.sleep(100000);
    }
}
