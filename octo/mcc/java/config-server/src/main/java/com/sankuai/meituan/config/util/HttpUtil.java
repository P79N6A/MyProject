package com.sankuai.meituan.config.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.meituan.config.model.APIResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-9
 */
public class HttpUtil {
    public static final String UNKNOWN = "unknown";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    public static String getRealIp(HttpServletRequest request) {
        String ip = head(request, "X-Real-IP");
        if (ip != null && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = head(request, "X-Forwarded-For");
        if (ip != null) {
            int index = ip.indexOf(',');
            // 多次反向代理后会有多个IP值，第一个为真实IP。
            return (index != -1) ? ip.substring(0, index) : ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip == null ? "unkown" : ip;
    }

    public static String head(HttpServletRequest req, String key) {
        return req.getHeader(key);
    }

    public static String ipToHost(String ip) {
        return ProcessInfoUtil.getHostInfoByIp(ip);
    }

    public static void setJsonResponse(HttpServletResponse response, String msg, int httpStatusCode) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(httpStatusCode);
        Object errorResult = APIResponse.newResponse(false).withErrorMessage(msg);
        PrintWriter pw =null;
        try {
            String json = JSON.toJSONString(errorResult);

            pw = response.getWriter();
            pw.println(json);
            pw.flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }finally {
            if(null!=pw){
                pw.close();
            }
        }
    }

    public static String get(String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setConnectTimeout(1500);
        builder.setConnectionRequestTimeout(1500);
        builder.setSocketTimeout(1500);
        httpGet.setConfig(builder.build());
        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpGet);
           return  EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            LOGGER.debug("failed to get data.", e);
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (Exception e) {
                    LOGGER.debug("failed to close connection.", e);
                }
            }
        }
        return "";
    }

}
