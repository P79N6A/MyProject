package com.sankuai.octo.benchmark.client;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sankuai.octo.async.AbstractHttpAsyncCommand;
import com.sankuai.octo.async.DefaultHttpAsyncClientFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 16/12/19
 * Time: 12:09
 */
public class AsyncHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpClient.class);
    private int threadNum;
    private long msgCount;
    private int msgLength;
    private int maxConnection;
    private DefaultHttpAsyncClientFactory factory;
    private HttpAsyncClient httpAsyncClient;
    private Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
    private CountDownLatch countDownLatch;

    private AtomicLong totalCount = new AtomicLong(0);

    private static Histogram histogram;

    public AsyncHttpClient(int threadNum, long msgCount, int msgLength, int maxConnection) {
        this.threadNum = threadNum;
        this.msgCount = msgCount;
        this.msgLength = msgLength;
        this.maxConnection = maxConnection;
        this.countDownLatch = new CountDownLatch((int)msgCount);

        factory = new DefaultHttpAsyncClientFactory();
        factory.setMgrDefaultMaxPerRoute(maxConnection);
        factory.setMgrMaxTotal(maxConnection);
        factory.setConnSocketTimeout(10000);
        factory.setConnReqTimeout(10000);
        try {
            httpAsyncClient = factory.getClient();
        } catch (IOReactorException e) {
            logger.error("get client error", e);
        }
    }

    public static void main(String[] args) {
        int threadNum = 1;
        long msgCount = 1000;
        int msgLength = 1000;
        int maxConnection = 1000;

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

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient(threadNum, msgCount, msgLength, maxConnection);
        asyncHttpClient.test();
        System.exit(0);
    }

    public void test() {
        histogram = new Histogram(new UniformReservoir(10000));
        totalCount = new AtomicLong(0);

        Long start = System.currentTimeMillis();
        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new TestThread(i, httpAsyncClient);
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
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long end = System.currentTimeMillis();

        logger.info("*****AsyncHttp:ThreadNum:" + threadNum + " MsgCount:" + msgCount + " MsgLength:" + msgLength + "*****");
        logger.info("TPS:" + msgCount * 1000 / (end - start));
        logger.info("99th:" + histogram.getSnapshot().get99thPercentile());
        logger.info("95th:" + histogram.getSnapshot().get95thPercentile());
        logger.info("Mean:" + histogram.getSnapshot().getMean());
        logger.info("Median:" + histogram.getSnapshot().getMedian());
        logger.info("Max:" + histogram.getSnapshot().getMax());
        logger.info("check histogram count:" + histogram.getCount());
    }

    class TestThread extends Thread {
        long id = 0;
        private int index;
        private HttpAsyncClient client;

        public TestThread(int i, HttpAsyncClient client) {
            this.index = i;
            this.setName("async-http-thread-" + index);
            this.client = client;
        }
        public void run() {
            while(true) {
                try {
                    id = totalCount.incrementAndGet();
                    if (id > msgCount) {
                        break;
                    }
                    TestCommand command = new TestCommand(client);
                    command.doQueue();
                } catch (Exception e) {
                    logger.info("exception caught", e);
                }
            }
        }
    }

    class TestCommand extends AbstractHttpAsyncCommand<String> {
        private HttpAsyncClient client;
        private final String url = "http://10.32.170.222:9090?param=" + msgLength;

        public TestCommand(HttpAsyncClient client) {
            this.client = client;
        }

        @Override public HttpAsyncClient getClient() {
            return client;
        }

        @Override public HttpRequestBase buildRequest() {
            return new HttpGet(url);
        }

        @Override public String extractFromEntity(HttpEntity httpEntity) {
            try {
                InputStream content = httpEntity.getContent();
                JsonParser jp = new JsonFactory().createParser(new InputStreamReader(content));
                jp.nextToken();
                for (JsonToken token; (token = jp.nextToken()) != JsonToken.END_OBJECT && token != null; ) {
                    String fieldName = jp.getCurrentName();
                    jp.nextToken();
                    if ("result".equals(fieldName)) {
                        return jp.getValueAsString();
                    } else {
                        jp.skipChildren();
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        public void doQueue() {
            final long before = System.currentTimeMillis();
            final ListenableFuture<String> future = this.queue();
            Futures.addCallback(future, new FutureCallback<String>() {
                @Override public void onSuccess(@Nullable String result) {
                    long after = System.currentTimeMillis();
                    long cost = after - before;
                    histogram.update(cost);
                    countDownLatch.countDown();
                }

                @Override public void onFailure(Throwable t) {
                    countDownLatch.countDown();
                }
            });
        }
    }
}

