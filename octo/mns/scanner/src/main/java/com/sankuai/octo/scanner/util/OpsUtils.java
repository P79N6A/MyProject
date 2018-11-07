package com.sankuai.octo.scanner.util;

import com.sankuai.meituan.common.security.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-11-24
 * Time: 下午4:41
 */
public class OpsUtils {

    private final static Log log = LogFactory.getLog(OpsUtils.class);

    /**
     * http://wiki.sankuai.com/pages/viewpage.action?pageId=96384377#id-发布系统RelengRestAPI-添加发布项的发布主机
     */

    private static String addHostUrl = "http://ops.sankuai.com/api/releng/deploy_app/953/";
    private static String restartUrl = "http://ops.sankuai.com/api/releng/release_record/?app_name=sg_agent";
    private static String token = "Basic " + Base64.encodeToString("gaosheng:mt123".getBytes());

    public static boolean restart(Map<String, String> paramsHashMap, String configCharset) {

        List<NameValuePair> nameValuePairArrayList = new ArrayList<NameValuePair>();
        if (paramsHashMap != null && !paramsHashMap.isEmpty()) {
            for (Map.Entry<String, String> entry : paramsHashMap.entrySet()) {
                nameValuePairArrayList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(nameValuePairArrayList, configCharset);
            HttpPost httpPost = new HttpPost(restartUrl);
            httpPost.setHeader("Authorization", token);
            httpPost.setEntity(entity);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(httpEntity.getContent(), configCharset), 8 * 1024);
                    StringBuilder entityStringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        entityStringBuilder.append(line + "\n");
                    }
                    log.info(entityStringBuilder.toString());
                    if (entityStringBuilder.toString().contains("200"))
                        return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }


    public static boolean addHost(Map<String, String> paramsHashMap, String configCharset) {

        List<NameValuePair> nameValuePairArrayList = new ArrayList<NameValuePair>();
        if (paramsHashMap != null && !paramsHashMap.isEmpty()) {
            for (Map.Entry<String, String> entry : paramsHashMap.entrySet()) {
                nameValuePairArrayList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(nameValuePairArrayList, configCharset);
            HttpPut httpPut = new HttpPut(addHostUrl);
            httpPut.setHeader("Authorization", token);
            httpPut.setEntity(entity);

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpPut);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(httpEntity.getContent(), configCharset), 8 * 1024);
                    StringBuilder entityStringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        entityStringBuilder.append(line + "\n");
                    }
                    log.info(entityStringBuilder.toString());
                    if (entityStringBuilder.toString().contains("200"))
                        return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public static boolean reboot(String host) {
        log.warn("reboot " + host);

        Map<String, String> addHostMap = new HashMap<String, String>();
        addHostMap.put("host", host);
        addHost(addHostMap, "UTF-8");

        Map<String, String> restartMap = new HashMap<String, String>();
        restartMap.put("hosts", host);
        restartMap.put("action", "restart");
        return restart(restartMap, "UTF-8");

    }
}
