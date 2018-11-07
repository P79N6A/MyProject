package com.sankuai.octo;

import com.sankuai.meituan.borp.http.AuthUtil;
import com.sankuai.meituan.borp.http.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by zava on 16/5/16.
 */
public class MsgpApiAuthFilterTest {
    private static final Logger LOG = LoggerFactory.getLogger(MsgpApiAuthFilterTest.class);
    DefaultHttpClient httpClient;
    Integer defaultRetryCount = 3;
    Integer connectTimeout = 10000;
    Integer soTimeout = 10000;
    @Before
    public void setUp() {

        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount,
                                        HttpContext context) {
                if (executionCount > defaultRetryCount) {
                    return false;
                } else {
                    LOG.info("borp retry " + executionCount + " " + context.toString() + " -> " +
                            exception.getMessage());
                }
                return true;
            }
        };

        PoolingClientConnectionManager poolingClientConnectionManager = new PoolingClientConnectionManager();
        poolingClientConnectionManager.setDefaultMaxPerRoute(5);
        poolingClientConnectionManager.setMaxTotal(50);
        httpClient = new DefaultHttpClient(poolingClientConnectionManager);

        httpClient.setHttpRequestRetryHandler(myRetryHandler);
        httpClient.getParams()
                .setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
    }

    @Test
    public void testApi() {
        try {
            String url = "http://localhost:8080/api/report/kpi?appkey=com.sankuai.inf.logCollector&day=2016-05-15";
            HttpGet request = new HttpGet(url);
            String date = getAuthDate();
            String authorization = AuthUtil.getAuthorization(request.getURI().getPath(), request.getMethod(), date, "com.sankuai.inf.msgp", "bd7eaaab277f52464883e028d5ca4de1");
            request.setHeader("Date", date);
            request.setHeader("Authorization", authorization);
            System.out.println(date);
            System.out.println(authorization);
            HttpResponse httpResponse = httpClient.execute(request);

            HttpEntity entity = httpResponse.getEntity();
            String content = entity != null ? EntityUtils.toString(httpResponse.getEntity(), "UTF-8") : null;
            System.out.println(content);
        } catch (Exception e) {
            LOG.info("exce",e);
        }
    }

    @Test
    public void testAuthDateAppkey() {
        String date = getAuthDate();
        String authorization = getAuthorization("api/report/kpi", "GET", date, "com.sankuai.inf.msgp", "bd7eaaab277f52464883e028d5ca4de1");
        System.out.println(date);
        System.out.println(authorization);
    }


    private static String getAuthDate() {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = df.format(new Date());
        return dateString;
    }

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String getAuthorization(String uri, String method, String date, String clientId, String secret) {
        String stringToSign = method + " " + uri + "\n" + date;
        String signature = getSignature(stringToSign, secret);
        return "MWS" + " " + clientId + ":" + signature;
    }


    public static String getSignature(String data, String secret) {
        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = Base64.encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }
}

