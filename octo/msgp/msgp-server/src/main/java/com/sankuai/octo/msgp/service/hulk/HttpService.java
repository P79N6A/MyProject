package com.sankuai.octo.msgp.service.hulk;

import com.amazonaws.util.StringUtils;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.meituan.mtrace.http.client.DefaultHttpClient;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.octo.msgp.model.reqpolicy.PolicyConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by songjianjian on 2018/8/1.
 */
public class HttpService {

    private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);

    public static String executeRequestImage(String url) {
        String result = potGetImageRequest(url);
        LOG.info("execute get image url {} request result {}", url, result);
        return result;
    }

    //调用BannerApi的通用接口(http接口)
    public static String executeRequestBannerApi(String url) {
        String result = potGetApiRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.BANNERAPI_TOKEN);
        LOG.info("execute get request result {}", result);
        return result;
    }

    public static String executePostRequestBannerApi(String url, String data) {
        String result = potPostApiRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.BANNERAPI_TOKEN, data);
        LOG.info("execute post request result {}", result);
        return result;
    }

    public static String getResponseNeedJsonStr(String url, String needKeyStr) {
        String resultInfo = potGetApiRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.BANNERAPI_TOKEN);
        LOG.info("request url {} and response need Json {}", url, resultInfo);
        if (!StringUtils.isNullOrEmpty(resultInfo)) {
            try {
                JSONObject jsonObject = new JSONObject(resultInfo);
                LOG.error("request result", jsonObject.toString());
                return jsonObject.getString(needKeyStr);
            } catch (JSONException e) {
                LOG.error("json make error ", e);
            }
        }
        return null;
    }

    public static String getRequestToKApi(String url) {
        String resultInfo = potGetApiRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.KAPI_SERVER_TOKEN);
        return resultInfo;
    }

    public static String postRequestToKApi(String url, String data) {
        String resultInfo = potPostApiRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.KAPI_SERVER_TOKEN, data);
        return resultInfo;
    }

    public static String getRequestToBannerApi(String url) {
        String resultInfo = potGetApiRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.BANNERAPI_TOKEN);
        return resultInfo;
    }

    public static String postRequestToBannerApi(String url, String data) {
        String resultInfo = potPostApiRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.BANNERAPI_TOKEN, data);
        return resultInfo;
    }

    public static String getRequestToOps(String url) {
        String resultInfo = potGetOpsRequest(url, PolicyConfig.CONTENT_TYPE, PolicyConfig.AUTHORIZATION_TOKEN);
        return resultInfo;
    }

    private static String makeOpsGetRequest(String url, String contentType, String authorizationToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", contentType);
        //Authorization
        header.put("Authorization", authorizationToken);
        String resultInfo = HttpUtil.httpGetRequest(url, header, null);
        return resultInfo;
    }

    private static String makeGetRequest(String url, String contentType, String token) {
        LOG.info("request url {} contentType {} token {}", url, contentType, token);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", contentType);
        //auth-token
        header.put("auth-token", token);
        String resultInfo = HttpUtil.httpGetRequest(url, header, null);
        LOG.info("httpUtil get request result {}", resultInfo);
        return resultInfo;
    }

    private static String makePostRequest(String url, String contentType, String token, String data) {
        String result = null;
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", contentType);
        header.put("auth-token", token);
        try {
            result = HttpUtil.httpPostRequest(url, header, data);
        } catch (UnsupportedEncodingException e) {
            LOG.error("HttpUtil post error ", e);
        }
        return result;
    }

    private static String potGetApiRequest(String url, String contentType, String token) {
        CloseableHttpResponse backResponse;
        String result = "";
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter("http.connection.timeout", PolicyConfig.CONNECT_TIMEOUT);
        client.getParams().setParameter("http.socket.timeout", PolicyConfig.SOCKET_TIMEOUT);
        HttpGet request = new HttpGet(url);
        request.setHeader("Content-Type", contentType);
        request.setHeader("auth-token", token);
        try {
            backResponse = client.execute(request);
            result = EntityUtils.toString(backResponse.getEntity());
        } catch (IOException e) {
            LOG.error("io error ", e);
        }
        return result;
    }

    private static String potPostApiRequest(String url, String contentType, String token, String data) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-type", contentType);
        post.setHeader("auth-token", token);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(data)) {
            StringEntity entity = new StringEntity(data, Charset.forName("UTF-8"));
            post.setEntity(entity);
        }
        String result = "";
        try {
            HttpResponse response = httpClient.execute(post);
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String potGetOpsRequest(String url, String contentType, String token) {
        CloseableHttpResponse backResponse;
        String result = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        request.setHeader("Content-Type", contentType);
        request.setHeader("Authorization", token);
        try {
            backResponse = client.execute(request);
            result = EntityUtils.toString(backResponse.getEntity());
        } catch (IOException e) {
            LOG.error("io error ", e);
        }
        return result;
    }

    private static String potGetImageRequest(String url) {
        CloseableHttpResponse backResponse;
        String result = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        try {
            backResponse = client.execute(request);
            result = EntityUtils.toString(backResponse.getEntity());
        } catch (IOException e) {
            LOG.error("io error ", e);
        }
        return result;
    }
}
