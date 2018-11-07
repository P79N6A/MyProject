package com.meituan.mtrace.http;

import com.meituan.mtrace.Endpoint;
import com.meituan.mtrace.Tracer;
import com.meituan.mtrace.http.client.PoolingNoCookieHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangxi
 * @created 14-1-1
 */
@Ignore
public class HttpClientPoolTest {

    @Test
    public void test() throws IOException {
        final Endpoint localEndpoint = new Endpoint(System.getProperty("app.key", "test"),
                System.getProperty("app.host", "127.0.0.1"), Integer.valueOf(System.getProperty("app.port", "8330")));
        System.setProperty("mtrace.collector","log");

        final CountDownLatch countDownLatch = new CountDownLatch(20);
        final HttpClient httpClient = createHttpClient();
        for(int i=1;i<=20;i++) {
            final int num = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Tracer.getServerTracer().setServerReceived(localEndpoint);
                    Tracer.getServerTracer().setCurrentTrace("test"+num, localEndpoint);
                    for(int i=0;i<50;i++) {
                        try {
                            HttpGet request = new HttpGet("http://api.upm-in.sankuai.com/api/roles/" + (7655 + num) + "/mtcrm");
                            HttpResponse response = httpClient.execute(request);
                            String json = EntityUtils.toString(response.getEntity());
                            System.out.println(json);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Tracer.getServerTracer().setServerSend(200);
                    countDownLatch.countDown();
                }
            });
            thread.start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private HttpClient createHttpClient() {
        HttpClient httpClient = new PoolingNoCookieHttpClient();
        return httpClient;
    }
}
