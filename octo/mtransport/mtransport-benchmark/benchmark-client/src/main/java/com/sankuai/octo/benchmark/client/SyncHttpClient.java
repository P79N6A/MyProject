package com.sankuai.octo.benchmark.client;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 16/12/19
 * Time: 16:30
 */
public class SyncHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(SyncHttpClient.class);

    private int threadNum;
    private long msgCount;
    private int msgLength;
    private int maxConnection;

    private AtomicLong totalCount = new AtomicLong(0);

    private static Histogram histogram;

    public SyncHttpClient(int threadNum, long msgCount, int msgLength, int maxConnection) {
        this.threadNum = threadNum;
        this.msgCount = msgCount;
        this.msgLength = msgLength;
        this.maxConnection = maxConnection;
    }

    public static void main(String[] args) {
        int threadNum = 1;
        long msgCount = 1000;
        int msgLength = 1000;
        int maxConnection = 250000;

        if (null != args) {
            if (args.length >= 1) {
                threadNum = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                msgCount = Long.parseLong(args[1]);
            }
            if (args.length >= 3) {
                msgLength = Integer.parseInt(args[2]);
            }
        }

        SyncHttpClient syncHttpClient = new SyncHttpClient(threadNum, msgCount, msgLength, maxConnection);
        syncHttpClient.test();
        System.exit(0);
    }

    public void test() {
        histogram = new Histogram(new UniformReservoir(10000));
        totalCount = new AtomicLong(0);

        Long start = System.currentTimeMillis();
        Thread[] threads = new Thread[threadNum];
        CloseableHttpClient client = getClient();
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new TestThread(i, client);
            threads[i].start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < threadNum; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            client.close();
        } catch (Exception e) {
            logger.error("close client error", e);
        }
        Long end = System.currentTimeMillis();

        logger.info(
                "*****SyncHttp:ThreadNum:" + threadNum + " MsgCount:" + msgCount + " MsgLength:" + msgLength + "*****");
        logger.info("TPS:" + msgCount * 1000 / (end - start));
        logger.info("99th:" + histogram.getSnapshot().get99thPercentile());
        logger.info("95th:" + histogram.getSnapshot().get95thPercentile());
        logger.info("Mean:" + histogram.getSnapshot().getMean());
        logger.info("Median:" + histogram.getSnapshot().getMedian());
        logger.info("Max:" + histogram.getSnapshot().getMax());
        logger.info("check histogram count:" + histogram.getCount());
    }

    public CloseableHttpClient getClient() {
        //同步连接池管理器配置
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(maxConnection);
        connManager.setDefaultMaxPerRoute(maxConnection);

        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setCharset(StandardCharsets.UTF_8)
                .build();
        connManager.setDefaultConnectionConfig(connConfig);

        //Client配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(1000)
                .setConnectTimeout(1000)
                .setSocketTimeout(1000)
                .setStaleConnectionCheckEnabled(true)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return httpClient;
    }

    class TestThread extends Thread {
        long id = 0;
        private int index;
        private CloseableHttpClient client;
        private HttpGet request;
        private final String url = "http://10.32.170.222:9090?param=" + msgLength;

        public TestThread(int i, CloseableHttpClient client) {
            this.index = i;
            this.setName("sync-http-thread-" + index);
            this.client = client;
            request = new HttpGet(url);
        }

        public void run() {
            while (true) {
                try {
                    id = totalCount.incrementAndGet();
                    if (id > msgCount) {
                        break;
                    }
                    CloseableHttpResponse response = null;
                    try {
                        long before = System.currentTimeMillis();
                        response = client.execute(request);
                        HttpEntity entity = response.getEntity();

                        InputStream content = entity.getContent();
                        JsonParser jp = new JsonFactory().createParser(new InputStreamReader(content));
                        jp.nextToken();
                        for (JsonToken token; (token = jp.nextToken()) != JsonToken.END_OBJECT && token != null; ) {
                            String fieldName = jp.getCurrentName();
                            jp.nextToken();
                            if ("result".equals(fieldName)) {
                                jp.getValueAsString();
                            } else {
                                jp.skipChildren();
                                continue;
                            }
                        }
                        long after = System.currentTimeMillis();
                        long cost = after - before;
                        histogram.update(cost);
                    } finally {
                        if (response != null) {
                            EntityUtils.consumeQuietly(response.getEntity());
                            try {
                                response.close();
                            } catch (IOException e) {
                                logger.info("exception caught", e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.info("exception caught", e);
                }
            }
        }
    }
}
