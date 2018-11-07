package com.sankuai.octo.util;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.octo.updater.util.httpPost;


public class HttpUtils {

    public static void postJsonAsync(String targetUrl, JSONObject jsonObject, String configCharset) {
        httpPost.post(targetUrl, jsonObject, configCharset);
    }

}
