package com.sankuai.octo.mworth.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.octo.mworth.model.Page;
import com.sankuai.octo.mworth.model.WorthResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import com.meituan.mtrace.http.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class HttpUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    private static PoolingClientConnectionManager poolingClientConnectionManager;

    private static DefaultHttpClient httpClient;

    private static Integer defaultRetryCount = 3;

    private static Integer connectTimeout = 10000;

    private static Integer soTimeout = 10000;


    static {
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount,
                                        HttpContext context) {
                if (executionCount > defaultRetryCount) {
                    return false;
                } else {
                    LOG.info("retry " + executionCount + " " + context.toString() + " -> " +
                            exception.getMessage());
                }
                return true;
            }
        };

        poolingClientConnectionManager = new PoolingClientConnectionManager();
        poolingClientConnectionManager.setDefaultMaxPerRoute(5);
        poolingClientConnectionManager.setMaxTotal(50);
        httpClient = new DefaultHttpClient(poolingClientConnectionManager);

        httpClient.setHttpRequestRetryHandler(myRetryHandler);
        httpClient.getParams()
                .setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
    }



    public static void post(String appkey, String secret, String url, Object data, String... headers) {
        post(appkey, secret, url, data, null, headers);
    }
    public static <T> WorthResponse<T> post(String appkey, String secret, String url, Object data, Class<T> returnType, String... headers) {
        try {
            HttpPost put = new HttpPost(url);
            String contentString = JSON.toJSONString(data);
            StringEntity body = new StringEntity(contentString, "utf-8");
            body.setContentType("Content-Type: application/json; charset=utf-8");
            put.setEntity(body);
            put.setHeader("Content-Type", "application/json; charset=utf-8");
            return execute(appkey, secret, put, returnType, headers);
        } catch (UnsupportedEncodingException e) {
            throw new HttpException(506, "UnsupportedEncodingException(utf-8)", e);
        }
    }
    public static <T> WorthResponse<T> execute(String appkey, String secret, HttpUriRequest request, Class<T> returnType, String... headers) {

        try {
            // 插入basic验证等基础逻辑
            for (int i = 0; i < headers.length; i += 2) {
                request.addHeader(headers[i], headers[i + 1]);
            }

            String date = getAuthDate();
            String authorization = AuthUtil.getAuthorization(request.getURI().getPath(), request.getMethod(), date, appkey, secret);
            request.setHeader("Date", date);
            request.setHeader("Authorization", authorization);

            HttpResponse httpResponse = httpClient.execute(request);

            HttpEntity entity = httpResponse.getEntity();
            String content = entity != null ? EntityUtils.toString(httpResponse.getEntity(), "UTF-8") : null;
            if (httpResponse.getStatusLine().getStatusCode() >= 200
                    && httpResponse.getStatusLine().getStatusCode() < 300) {
                JSONObject json = JSON.parseObject(content);
                if (json.get("error") == null) {

                    //无返回类型表示无返回值
                    if (returnType == null) {
                        return null;
                    }

                    if (json.get("data") == null) {
                        return null;
                    }

                    JSONObject responseJson = JSON.parseObject(json.getString("data"));
                    WorthResponse worthResponse = new WorthResponse();

                    Page page = responseJson.getObject("page", Page.class);
                    worthResponse.setPage(page);

                    if (responseJson.get("result") != null) {
                        List<T> result = JSON.parseArray(responseJson.getJSONArray("result").toJSONString(), returnType);
                        worthResponse.setResult(result);
                    } else {
                        worthResponse.setResult(Collections.emptyList());
                    }

                    return worthResponse;
                } else {
                    MtError error = JSON.parseObject(json.getJSONObject("error").toJSONString(), MtError.class);
                    throw new HttpException(error.code, error.type, error.message);
                }
            } else {
                throw new HttpException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase()
                        , content);
            }
        } catch (IOException e) {
            throw new HttpException(505, "IOException", e);
        }
    }


    public static class MtError {
        private int code;
        private String type;
        private String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

    private static String getAuthDate() {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = df.format(new Date());
        return dateString;
    }
}
