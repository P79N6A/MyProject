package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.util.AuthUtil;
import com.sankuai.meituan.config.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

/**
 * Created by lhmily on 02/03/2016.
 */
public class HttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);
    public static final Integer CONNECTION_TIMEOUT = 10000;
    public static final Integer SO_TIMEOUT = 10000;

    //为了兼容性，暂时保留该方法。
    @Deprecated
    public MtHttpResponse executeRaw(final MtHttpRequest request) {
        LOG.warn("This method is belong to the HttpClient of MCC. It is not recommended to use this method, please fix your codes.");
        return execute(request);
    }

    public static MtHttpResponse execute(final MtHttpRequest request) {
        String url = getUrl(request);
        HttpURLConnection connection = null;
        MtHttpResponse response = new MtHttpResponse();
        String method = request.getMethod();

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(request.getMethod());
            setAuth(connection, request);
            setRequestProperty(connection, request);
            if ("POST".equals(method)) {
                writeContent(connection, map2Str(request.getEntitys()));
            }
            response.setEntity(readStream(connection, request));
            response.setStatusCode(connection.getResponseCode());
        } catch (Exception e) {
            String msg = "Failed to obtain data from  " + url + ". statusCode:" + response.getStatusCode();
            LOG.debug(msg, e);
        } finally {
            closeConnection(connection);
        }
        return response;
    }

    private static void setAuth(HttpURLConnection connection, final MtHttpRequest request) {
        if (request.needAuth()) {
            String clientKey = request.getClientKey();
            String secret = request.getSecret();
            String date = TimeUtil.getAuthDate(new Date());
            String authorization = AuthUtil.getAuthorization(request.getPath(), request.getMethod(), date, clientKey, secret);
            connection.setRequestProperty("Date", date);
            connection.setRequestProperty("Authorization", authorization);
        }
    }

    private static String getUrl(final MtHttpRequest request) {
        String url = request.getHost() + request.getPath();
        String param = map2Str(request.getParams());
        if (StringUtils.isNotEmpty(param)) {
            url += "?" + param;
        }
        return url;
    }

    private static void setRequestProperty(HttpURLConnection connection, final MtHttpRequest request) {
        Map<String, Object> headers = request.getHeaders();
        if (!headers.isEmpty()) {
            for (Map.Entry<String, Object> item : headers.entrySet()) {
                connection.setRequestProperty(item.getKey(), item.getValue().toString());
            }
        }
    }

    private static String map2Str(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> item : map.entrySet()) {
            sb.append("&");
            sb.append(item.getKey());
            sb.append("=");
            sb.append(item.getValue().toString());
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(0);
        }
        return sb.toString().trim();
    }

    private static void closeConnection(HttpURLConnection connection) {
        if (null != connection) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                String msg = "close connection " + connection.getURL() + " failed";
                LOG.debug(msg, e);
            }
        }
    }

    private static void writeContent(HttpURLConnection connection, String content) {
        OutputStreamWriter out = null;
        try {
            connection.setDoOutput(true);
            out = new OutputStreamWriter(connection.getOutputStream(), Charset.forName("utf-8"));
            out.write(content);
        } catch (Exception e) {
            String msg = "write content to " + connection.getURL() + " failed";
            LOG.debug(msg, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    String msg = "close connection " + connection.getURL() + " failed";
                    LOG.debug(msg, e);
                }
            }
        }
    }

    private static String readStream(HttpURLConnection connection, final MtHttpRequest request) throws IOException {
        int connectTimeout = request.getTimeout() != null ? request.getTimeout() : CONNECTION_TIMEOUT;
        int soTimeout = request.getTimeout() != null ? request.getTimeout() : SO_TIMEOUT;

        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(soTimeout);

        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;
        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            String msg = "read connection " + connection.getURL() + " failed";
            LOG.debug(msg, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    String msg = "close connection " + connection.getURL() + " failed";
                    LOG.debug(msg,e);
                }
            }
        }
        return result;
    }
}
