
package com.sankuai.msgp.common.utils;

import com.meituan.mtrace.http.HttpClients;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 使用httpclient发送请求工具类
 */
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    public static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setSocketTimeout(5000)
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000).build();

    public enum RequestType {
        GET("GET"), POST("POST");
        private String type;

        RequestType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
    private static String EMPTY_STR = "";
    private static String UTF_8 = "UTF-8";


    private static ThreadLocal<DateFormat> formatLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
                    Locale.ENGLISH);
        }
    };

    private synchronized static void init() {
        if (poolingHttpClientConnectionManager == null) {
            synchronized (HttpUtil.class) {
                if (poolingHttpClientConnectionManager == null) {
                    poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
                    poolingHttpClientConnectionManager.setMaxTotal(50);// 整个连接池最大连接数
                    poolingHttpClientConnectionManager.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
                }
            }
        }
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */

    public static CloseableHttpClient getHttpClient() {
        init();
        return HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager).build();
    }

    public static String getResult(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(REQUEST_CONFIG);
        return getResult(httpGet);
    }

    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(response.getEntity(), "UTF-8");
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            log.info("request 请求失败，URI:" + request.getURI(), e);
        }
        return EMPTY_STR;
    }

    public static final String get(String url) {
        String result = "";
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            HttpGet httpget = new HttpGet("http://httpbin.org/get");

            System.out.println("Executing request " + httpget.getRequestLine());
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
            log.error("http get error:" + url, e);
        }
        return result;
    }


    /**
     * 通过http post发送请求
     */
    public static final HttpResponse postString(String surl, List<NameValuePair> params,
                                                String clientId, String secret) {
        HttpResponse response = null;
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(surl);
        try {
            Date date = new Date();
            URL url = new URL(surl);
            // 设置Authorization
            if (clientId != null) {
                String uri = url.getPath();
                String dateString = getDateString(date);
                String authorization = getAuthorization(uri, "POST", date, clientId, secret);
                post.setHeader("Date", dateString);
                post.setHeader("Authorization", authorization);
            }
            if (params != null && params.size() > 0) {
                UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(params, "UTF-8");
                post.setEntity(uefe);
                log.info("POST " + post.getURI());
            }
            response = httpclient.execute(post);
            return response;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response = null;
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
    }

    public static String httpGetRequest(String url, Map<String, String> params) throws URISyntaxException {
        URIBuilder uRIBuilder = new URIBuilder();
        uRIBuilder.setPath(url);

        if (params != null && !params.isEmpty()) {
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            uRIBuilder.setParameters(pairs);
        }

        HttpGet httpGet = new HttpGet(uRIBuilder.build());
        return getResult(httpGet);
    }

    public static String httpGetRequest(String url, Map<String, String> headers, Map<String, String> params) {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath(url);

        if (params != null && !params.isEmpty()) {
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            uriBuilder.setParameters(pairs);
        }
        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            log.error("httpGetRequest uri exception.", e);
            return "";
        }
        for (Map.Entry<String, String> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet);
    }

    /**
     * 支持
     *
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws URISyntaxException
     */
    public static String httpGetRequestWithBasicAuthorization(String url, Map<String, String> headers, Map<String, String> params,
                                                              String clientId, String secret) throws URISyntaxException {
        URIBuilder uRIBuilder = new URIBuilder();
        uRIBuilder.setPath(url);

        if (params != null && !params.isEmpty()) {
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            uRIBuilder.setParameters(pairs);
        }

        HttpGet httpGet = new HttpGet(uRIBuilder.build());
        httpGet.setConfig(REQUEST_CONFIG);
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> param : headers.entrySet()) {
                httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }
        }

        BaseAuthorizationUtil.generateAuthAndDateHeader(httpGet, clientId, secret);

        return getResult(httpGet);
    }

    public static String httpPostRequest(String url, Map<String, String> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(REQUEST_CONFIG);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    /**
     * @param url
     * @param json
     * @return post raw 格式的数据
     * @throws UnsupportedEncodingException
     */
    public static String httpPostRequest(String url, String json) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(REQUEST_CONFIG);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        StringEntity postingString = new StringEntity(json, "UTF-8");
        httpPost.setEntity(postingString);
        return getResult(httpPost);
    }

    public static String httpPostRequest(String url, Map<String, String> headers, String json) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(REQUEST_CONFIG);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            httpPost.addHeader(headerEntry.getKey(), String.valueOf(headerEntry.getValue()));
        }

        StringEntity postingString = new StringEntity(json, "UTF-8");
        httpPost.setEntity(postingString);
        return getResult(httpPost);
    }

    // octo一键扩缩容用，统一token
    public static String httpPostRequestForScaleOut(String url, String json) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(REQUEST_CONFIG);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.addHeader("auth-token", "appTokenFromOcto281931");
        StringEntity postingString = new StringEntity(json, "UTF-8");
        httpPost.setEntity(postingString);
        return getResult(httpPost);
    }

    public static String httpPostRequestForFalcon(String url ,String auth,String json){
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(REQUEST_CONFIG);
        httpPost.addHeader("Authorization", auth);
        StringEntity postingString = new StringEntity(json, "UTF-8");
        httpPost.setEntity(postingString);
        return getResult(httpPost);
    }


    public static String httpPostRequest(String url, Map<String, String> headers, Map<String, String> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, String> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return getResult(httpPost);
    }

    public static int httpPostRequestByBody(String url, String bodyJson) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(REQUEST_CONFIG);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        StringEntity postingString = new StringEntity(bodyJson);
        httpPost.setEntity(postingString);

        CloseableHttpClient httpClient = getHttpClient();
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            log.info("[httpPostRequestByBody]statusCode: " + response.getStatusLine().getStatusCode());
            log.info("[httpPostRequestByBody]response data: " + EntityUtils.toString(response.getEntity()));
            return response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            log.info("request 请求失败，URI:" + httpPost.getURI(), e);
        }
        return 201;
    }

    /**
     * 支持BA认证的POST请求
     *
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String httpPostRequestWithBasicAuthorization(String url, Map<String, String> headers, Map<String, String> params,
                                                               String clientId, String secret) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

        for (Map.Entry<String, String> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        BaseAuthorizationUtil.generateAuthAndDateHeader(httpPost, clientId, secret);

        return getResult(httpPost);
    }

    /**
     * 支持BA认证的POST请求(raw)
     *
     * @param url
     * @param json
     * @param clientId
     * @param secret
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String httpPostRequestWithBasicAuthorization(String url, String json, String clientId, String secret) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(REQUEST_CONFIG);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        StringEntity postingString = new StringEntity(json, "UTF-8");
        httpPost.setEntity(postingString);

        BaseAuthorizationUtil.generateAuthAndDateHeader(httpPost, clientId, secret);
        return getResult(httpPost);
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, String> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (null != param.getValue()) {
                pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
            }
        }
        return pairs;
    }

    public static String getDateString(Date date) {
        DateFormat df = formatLocal.get();
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        String dateString = df.format(date);

        return dateString;
    }

    public static String getAuthorization(String url, String method, Date date, String clientId,
                                          String secret) {

        String stringToSign = method + " " + url + "\n" + getDateString(date);

        String signature = HmacUtil.getSignature(stringToSign, secret);

        String authorization = "MWS" + " " + clientId + ":" + signature;

        return authorization;
    }

    public static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new HashMap<String, Object>(0);
        if (StringUtils.isBlank(param)) {
            return map;
        }
        String[] params = param.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 2) {
                try {
                    map.put(p[0], URLDecoder.decode(p[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    log.error("This method requires UTF-8 encoding support,param" + param, e);
                }
            }
        }
        return map;
    }

    public static int getUrlCode(String url) {
        HttpGet httpGet = new HttpGet(url);
        final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
                .setSocketTimeout(50000)
                .setConnectTimeout(5000)
                .setRedirectsEnabled(false)
                .setConnectionRequestTimeout(5000).build();

        httpGet.setConfig(REQUEST_CONFIG);
        CloseableHttpClient httpClient = getHttpClient();
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            return response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            log.info("request 请求失败，URI:" + httpGet.getURI(), e);
        }

        return 500;
    }

    public static Map<String, String> getBAAuthHeader(String clientID, String clientSecret, RequestType reqType, String uri) {
        Map<String, String> headers = new HashMap<>();
        String gmtTime = getDateString(new Date());
        String authorization = getAuthorization(uri, reqType, gmtTime, clientID, clientSecret);
        if (authorization == null) {
            return null;
        }
        headers.put("Date", gmtTime);
        headers.put("Authorization", authorization);
        return headers;
    }

    public static String getAuthorization(String uri, RequestType reqType, String gmtTime, String clientId,
                                          String secret) {
        String stringToSign = String.format("%s %s%n%s", reqType, uri, gmtTime);

        String signature = HmacUtil.getSignature(stringToSign, secret);

        String authorization = String.format("%s %s:%s", "MWS", clientId, signature);

        return authorization;
    }


}
