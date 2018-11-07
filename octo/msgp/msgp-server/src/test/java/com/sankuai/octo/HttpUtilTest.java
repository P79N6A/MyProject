package com.sankuai.octo;

import com.meituan.mtrace.http.HttpClients;
import com.sankuai.msgp.common.utils.HttpUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpUtilTest {
    @Test
    public void testHttpGet() {
        String urlStr = "http://data.octo.vip.sankuai.com/api/tags";
        Map map = new HashMap<String, Object>() {
            {
                put("appkey", "waimai_api");

                put("start", "1480323225");

                put("end", "1480928025");

                put("env", "prod");

                put("source", "");
            }
        };

        String result = null;
        try {
            result = HttpUtil.httpGetRequest(urlStr, map);
            System.out.println(result);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHttpGetHead(String url) {
        String urlStr = "http://octo.test.sankuai.com/service/filter?business=-1&type=4&pageNo=1&pageSize=20000";
        String result = "";
        try {
            HttpGet httpget = new HttpGet(urlStr);
            CookieStore cookieStore = new BasicCookieStore();
            BasicClientCookie cookie = new BasicClientCookie("ssoid", "faee5ebce6ca4140a4d4*07dd6e4fd4e");
            cookie.setVersion(0);
            URI uri = httpget.getURI();

            cookie.setDomain(uri.getHost());   //设置范围
            cookie.setPath("/");
            cookieStore.addCookie(cookie);

            CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();


            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(20000)
                    .setConnectTimeout(20000).setCookieSpec(CookieSpecs.STANDARD).build();
            System.out.println("Executing request " + httpget.getRequestLine());
            httpget.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {

                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    result = EntityUtils.toString(response.getEntity());
                }
            } finally {
                response.close();
                httpclient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);

    }
}
