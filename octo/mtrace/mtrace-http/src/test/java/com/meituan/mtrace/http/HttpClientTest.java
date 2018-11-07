package com.meituan.mtrace.http;

import com.meituan.mtrace.Endpoint;
import com.meituan.mtrace.Tracer;
import com.meituan.mtrace.http.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * @author zhangxi
 * @created 14-1-1
 */
public class HttpClientTest {

    @Test
    public void testDefaultClient() throws IOException {
        Endpoint localEndpoint = new Endpoint(System.getProperty("app.key", "test"),
                System.getProperty("app.host", "127.0.0.1"), Integer.valueOf(System.getProperty("app.port", "8330")));
        System.setProperty("mtrace.collector","log");
        Tracer.getServerTracer().setCurrentTrace("test", localEndpoint);

        DefaultHttpClient httpClient = createHttpClient();
        for (int i = 0; i < 1; i++) {
            HttpGet request = new HttpGet("http://api.upm-in.sankuai.com/api/roles/" + (7655 + i) + "/mtcrm");
            HttpResponse response = httpClient.execute(request);
            String json = EntityUtils.toString(response.getEntity());
            System.out.println(json);
        }
    }

    @Test
    public void testHttpClients() {
        HttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet("http://api.upm-in.sankuai.com/api/roles/7655/mtcrm");
            httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private DefaultHttpClient createHttpClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient("octotest");
        return httpClient;
    }
}
