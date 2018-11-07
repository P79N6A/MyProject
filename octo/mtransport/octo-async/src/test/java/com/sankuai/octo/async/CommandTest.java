package com.sankuai.octo.async;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CommandTest {
    private static HttpDemoServer server;
    private static DefaultHttpAsyncClientFactory factory;
    private static HttpAsyncClient globalClient;

    @BeforeClass
    public static void init() throws IOReactorException, ExecutionException, InterruptedException {
        server = new HttpDemoServer();
        server.start();
        factory = new DefaultHttpAsyncClientFactory();
        globalClient = factory.getClient();

        // warnup
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < TestConsts.count; i++) {
//            TestCommand command = new TestCommand(globalClient, TestConsts.url + TestConsts.ms);
//            command.queue().get();
//        }
//        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testCommand() throws ExecutionException, InterruptedException {
        TestCommand command = new TestCommand(globalClient, TestConsts.url + TestConsts.ms);
        System.out.println(command.queue().get());
    }

    @Test
    public void testNullCommand() throws ExecutionException, InterruptedException {
        NullRequestCommand command = new NullRequestCommand();
        System.out.println(command.queue().get());
    }

    @Test
    public void testIllegalUrlCommand() throws ExecutionException, InterruptedException {
        TestCommand command = new TestCommand(globalClient, "http://localhost:8080/none_url");
        System.out.println(command.queue().get());
    }

    @Test
    public void testNullUrlCommand() throws ExecutionException, InterruptedException {
        TestCommand command = new TestCommand(globalClient, null);
        System.out.println(command.queue().get());
    }

    @Test
    public void testCancelledCommand() throws ExecutionException, InterruptedException {
        TestCommand command = new TestCommand(globalClient, TestConsts.url + TestConsts.ms);
        ListenableFuture<Long> future = command.queue();
        future.cancel(true);
        System.out.println(future.get());
    }

    @Test
    public void testBatchSync() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < TestConsts.count; i++) {
            TestCommand command = new TestCommand(globalClient, TestConsts.url + TestConsts.ms);
            command.queue().get();
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testBatchSyncThread() throws ExecutionException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(TestConsts.count);
        Executor executor = Executors.newFixedThreadPool(50);
        // init thread pool

        long start = System.currentTimeMillis();
        for (int i = 0; i < TestConsts.count; i++) {
            final long s1 = System.currentTimeMillis();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TestCommand command = new TestCommand(globalClient, TestConsts.url + TestConsts.ms);
                    try {
                        command.queue().get();
                        long s2 = System.currentTimeMillis();
                        System.out.println("actual wait cost: " + (s2 - s1));
                        latch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        long s2 = System.currentTimeMillis();
        System.out.println("submit: " + (s2 - start));
        latch.await();
        System.out.println("cost: " + (System.currentTimeMillis() - s2));
        System.out.println(Thread.activeCount());
    }

    @Test
    public void testBatchAsync() throws ExecutionException, InterruptedException, IOReactorException {
        final CountDownLatch latch = new CountDownLatch(TestConsts.count);

//        factory.setConnReqTimeout(10000);
//        factory.setConnTimout(10000);
//        factory.setConnSocketTimeout(10000);
//        factory.setReactorSoTimeout(10000);
//        factory.setReactorConnTimeout(10000);
        globalClient = factory.getClient();
        long start = System.currentTimeMillis();
        final List<Future<Long>> futureList = new ArrayList<Future<Long>>();
        for (int i = 0; i < TestConsts.count; i++) {
            final long s1 = System.currentTimeMillis();
            TestCommand command = new TestCommand(globalClient, TestConsts.url + TestConsts.ms);
            command.setCallTree("appkey1", "appkey2");
            ListenableFuture<Long> future = command.queue();
            if (future == null) {
                System.out.println(command);
            } else {
                futureList.add(future);
            }
            long s2 = System.currentTimeMillis();
            System.out.println("actual add cost: " + (s2 - s1) + ", " + (s2 - start));
        }
        long s1 = System.currentTimeMillis();
        System.out.println("submit command: " + (s1 - start));

        long s2 = System.currentTimeMillis();
        for (Future<Long> future : futureList) {
            future.get();
            latch.countDown();
        }
        latch.await();
        System.out.println("submit command: " + (s1 - start));
        System.out.println("cost: " + (System.currentTimeMillis() - s2));
        System.out.println("threads:" + Thread.activeCount());
    }


    @Test
    public void testBatchAsyncThread() throws ExecutionException, InterruptedException, IOReactorException {
        final CountDownLatch addLatch = new CountDownLatch(TestConsts.count);
        final CountDownLatch latch = new CountDownLatch(TestConsts.count);
        Executor executor = Executors.newFixedThreadPool(100);

        factory.setConnReqTimeout(10000);
        factory.setConnTimout(10000);
        factory.setConnSocketTimeout(10000);
        factory.setReactorSoTimeout(10000);
        factory.setReactorConnTimeout(10000);
        globalClient = factory.getClient();
        final long start = System.currentTimeMillis();
        final List<Future<Long>> futureList = new ArrayList<Future<Long>>();
        for (int i = 0; i < TestConsts.count; i++) {
            final long s1 = System.currentTimeMillis();
//            if (i == TestConsts.count / 2) {
//                globalClient = factory.getClient();
//            }

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final long s = System.currentTimeMillis();
                    TestCommand command = new TestCommand(globalClient, TestConsts.url + TestConsts.ms);
//                    TestCommand command = new BaiduCommand(globalClient, TestConsts.baiduUrl + TestConsts.ms);
                    command.setCallTree("appkey1", "appkey2");
                    ListenableFuture<Long> future = command.queue();
                    if (future == null) {
                        System.out.println(command);
                    } else {
                        futureList.add(future);
                    }
                    long s2 = System.currentTimeMillis();
                    System.out.println("actual add cost: " + (s2 - s) + "," + (s2 - s1) + ", " + (s2 - start));
                    addLatch.countDown();
                }
            });
        }
        long s1 = System.currentTimeMillis();
        System.out.println("submit command: " + (s1 - start));
        addLatch.await();
        System.out.println("submit command execute cost: " + (System.currentTimeMillis() - start));

        long s2 = System.currentTimeMillis();
        for (Future<Long> future : futureList) {
            if (future != null) {
                future.get();
            }
            latch.countDown();
        }
        latch.await();
        System.out.println("submit command: " + (s1 - start));
        System.out.println("cost: " + (System.currentTimeMillis() - s2));
        System.out.println("threads:" + Thread.activeCount());
    }

    static class TestCommand extends AbstractHttpAsyncCommand<Long> {
        private HttpAsyncClient client;
        private String url;

        public TestCommand(HttpAsyncClient client, String url) {
            this.client = client;
            this.url = url;
        }

        @Override
        public HttpAsyncClient getClient() {
            return client;
        }

        @Override
        public HttpRequestBase buildRequest() {
            HttpGet request = new HttpGet(url);
            return request;
        }

        @Override
        public Long extractFromEntity(HttpEntity entity) {
            try {
                InputStream content = entity.getContent();
                JsonParser jp = new JsonFactory().createParser(new InputStreamReader(content));
                jp.nextToken();
                for (JsonToken token; (token = jp.nextToken()) != JsonToken.END_OBJECT && token != null; ) {
                    String fieldName = jp.getCurrentName();
                    jp.nextToken();
                    if ("result".equals(fieldName)) {
                        return jp.getLongValue();
                    } else {
                        jp.skipChildren();
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1L;
        }

        // 自定义fallback
        @Override
        public void getFallback(SettableFuture<Long> future, Exception e) {
            // ignore exception
            // future.setException(e);
            System.out.println(e.getMessage());
            future.set(100L);
        }
    }

    class NullRequestCommand extends TestCommand {

        public NullRequestCommand() {
            super(globalClient, null);
        }

        @Override
        public HttpRequestBase buildRequest() {
            return null;
        }
    }

    class BaiduCommand extends TestCommand {

        public BaiduCommand(HttpAsyncClient client, String url) {
            super(client, url);
        }

        @Override
        public Long extractFromEntity(HttpEntity entity) {
            try {
                InputStream content = entity.getContent();
                return content.available() * 1L;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1L;
        }
    }
}
