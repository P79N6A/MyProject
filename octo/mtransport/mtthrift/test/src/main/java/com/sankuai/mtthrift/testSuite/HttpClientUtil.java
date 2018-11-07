package com.sankuai.mtthrift.testSuite;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/2/13
 */
public class HttpClientUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static CloseableHttpClient httpclient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(10);
        httpclient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public static String doGet(String url, Map<String, String> params) throws Exception {
        return doGet(url, params, null);
    }

    public static String doGet(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        HttpGet httpget = null;
        if (params != null && params.size() > 0) {
            URIBuilder uri = new URIBuilder(url);
            for (Entry<String, String> param : params.entrySet()) {
                uri.addParameter(param.getKey(), param.getValue());
            }
            httpget = new HttpGet(uri.build());
        } else {
            httpget = new HttpGet(url);
        }
        if (headers != null) {
            for (Entry<String, String> header : headers.entrySet()) {
                httpget.setHeader(header.getKey(), header.getValue());
            }
        }
        return httpclient.execute(httpget, getResponseHandler());
    }

    public static String doPost(String url, Map<String, String> pairsMap) throws Exception {
        HttpPost httppost = new HttpPost(url);
        if (pairsMap != null && pairsMap.size() > 0) {
            List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
            for (String key : pairsMap.keySet()) {
                pairs.add(new BasicNameValuePair(key, pairsMap.get(key)));
            }
            httppost.setEntity(new UrlEncodedFormEntity(pairs));
        }
        return httpclient.execute(httppost, getResponseHandler());
    }

    public static String doPost(String url, String paramStr) throws IOException {
        return doPost(url, paramStr, null);
    }

    public static String doPost(String url, String paramStr, Map<String, String> headers) throws IOException {
        HttpPost httppost = new HttpPost(url);
        StringEntity str = new StringEntity(paramStr, ContentType.APPLICATION_JSON);
        httppost.setEntity(str);
        if (headers != null) {
            for (Entry<String, String> header : headers.entrySet()) {
                httppost.setHeader(header.getKey(), header.getValue());
            }
        }
        return httpclient.execute(httppost, getResponseHandler());
    }

    private static ResponseHandler<String> getResponseHandler() {
        return new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
    }

    public static void shutDown() {
        try {
            httpclient.close();
        } catch (Exception e) {
            logger.error("Http client close fail", e);
        }
    }
}
