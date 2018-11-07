package com.sankuai.octo.async;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * http异步客户端工厂
 * 生产一个“自带连接管理器”的http异步客户端
 * Created by wangchao23 on 2016-07-19.
 */
public class DefaultHttpAsyncClientFactory {
    private int reactorConnTimeout = 1000;   //reactor发起连接超时时间
    private int reactorIoThreadCount = Runtime.getRuntime().availableProcessors(); //io线程(默认可用核数)
    private int reactorSoTimeout = 1000; //reactor套接字等待时间
    private Charset connCharset = StandardCharsets.UTF_8; //连接默认字符集
    private int mgrMaxTotal = 10000;  //全局最大连接数
    private int mgrDefaultMaxPerRoute = 1000; //每个主机最大连接数
    private int connReqTimeout = 1000;   //从连接池获取连接超时时间
    private int connTimout = 1000;   //发起连接超时时间
    private int connSocketTimeout = 1000;    //连接套接字等待时间
    private boolean staleConnCheck = true;  //检测已经被关闭的链接

    public HttpAsyncClient getClient() throws IOReactorException {
        //Reactor配置
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(reactorConnTimeout)
                .setIoThreadCount(reactorIoThreadCount)
                .setSoTimeout(reactorSoTimeout)
                .setSoKeepAlive(true)
                .setSoReuseAddress(true)
                .setTcpNoDelay(true)
                .build();
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(reactorConfig);

        //异步连接池管理器配置
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connManager.setMaxTotal(mgrMaxTotal);
        connManager.setDefaultMaxPerRoute(mgrDefaultMaxPerRoute);

        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setCharset(connCharset)
                .build();
        connManager.setDefaultConnectionConfig(connConfig);

        //Client配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connReqTimeout)
                .setConnectTimeout(connTimout)
                .setSocketTimeout(connSocketTimeout)
                        //4.4以后官方建议用同步管理器“PoolingHttpClientConnectionManager#setValidateAfterInactivity(int)”代替
                        //可问题是对应的异步管理器（PoolingNHttpClientConnectionManager）根本就没有对应的方法啊啊啊啊
                .setStaleConnectionCheckEnabled(staleConnCheck)
                .build();

        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        httpAsyncClient.start();
        return httpAsyncClient;
    }

    public int getReactorConnTimeout() {
        return reactorConnTimeout;
    }

    public void setReactorConnTimeout(int reactorConnTimeout) {
        this.reactorConnTimeout = reactorConnTimeout;
    }

    public int getReactorIoThreadCount() {
        return reactorIoThreadCount;
    }

    public void setReactorIoThreadCount(int reactorIoThreadCount) {
        this.reactorIoThreadCount = reactorIoThreadCount;
    }

    public int getReactorSoTimeout() {
        return reactorSoTimeout;
    }

    public void setReactorSoTimeout(int reactorSoTimeout) {
        this.reactorSoTimeout = reactorSoTimeout;
    }

    public Charset getConnCharset() {
        return connCharset;
    }

    public void setConnCharset(Charset connCharset) {
        this.connCharset = connCharset;
    }

    public int getMgrMaxTotal() {
        return mgrMaxTotal;
    }

    public void setMgrMaxTotal(int mgrMaxTotal) {
        this.mgrMaxTotal = mgrMaxTotal;
    }

    public int getMgrDefaultMaxPerRoute() {
        return mgrDefaultMaxPerRoute;
    }

    public void setMgrDefaultMaxPerRoute(int mgrDefaultMaxPerRoute) {
        this.mgrDefaultMaxPerRoute = mgrDefaultMaxPerRoute;
    }

    public int getConnReqTimeout() {
        return connReqTimeout;
    }

    public void setConnReqTimeout(int connReqTimeout) {
        this.connReqTimeout = connReqTimeout;
    }

    public int getConnTimout() {
        return connTimout;
    }

    public void setConnTimout(int connTimout) {
        this.connTimout = connTimout;
    }

    public int getConnSocketTimeout() {
        return connSocketTimeout;
    }

    public void setConnSocketTimeout(int connSocketTimeout) {
        this.connSocketTimeout = connSocketTimeout;
    }

    public boolean isStaleConnCheck() {
        return staleConnCheck;
    }

    public void setStaleConnCheck(boolean staleConnCheck) {
        this.staleConnCheck = staleConnCheck;
    }
}