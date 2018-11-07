package com.sankuai.octo.oswatch.service;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenxi on 6/30/15.
 */
public abstract class QPSReader {
    public Logger logger = LoggerFactory.getLogger(QPSReader.class);
    public HttpClient httpClient = HttpClientBuilder.create().build();
    public static int ONE_SECOND_IN_MS = 1000;
    public String queryURL;

    public abstract Map<String, Double> getCurrentQPS(String appkey, String method, String env, long timestamp, int periodInMinutes);

    public abstract String mergePerfQuery(String appkey, String spanname, String env, String start, String end);

    public <T> List<T> getHttpContentList(String URL, Class<T> clazz) {
        List<T> list = new ArrayList<T>();

        try {
            HttpGet httpget=new HttpGet(queryURL+URL);
            HttpResponse response = httpClient.execute(httpget);

            if(response.getStatusLine().getStatusCode()==200) {
                String result = EntityUtils.toString(response.getEntity());
                list = JSON.parseArray(result, clazz);
            }
        } catch (IOException ioe) {
            logger.error("http get error", ioe);
        }

        return list;
    }

    public <T> T getHttpContent(String URL, Class<T> clazz) {
        T t = null;

        try {
            HttpGet httpget=new HttpGet(queryURL+URL);
            System.out.println(queryURL+URL);
            HttpResponse response = httpClient.execute(httpget);

            if(response.getStatusLine().getStatusCode()==200) {
                String result = EntityUtils.toString(response.getEntity());
                t = JSON.parseObject(result, clazz);
            }
        } catch (IOException ioe) {
            logger.error("http get error", ioe);
        }

        return t;
    }
}
