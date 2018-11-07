package com.meituan.control.zookeeper.http;

import net.sf.json.JSONObject;

import java.util.HashMap;

/**
 * User: jinmengzhe
 * Date: 2015-05-25
 */
public class RequestTest {
    private static final String endPoint = "http://192.168.60.199:8082";

    public static void testGetZkList() {
        MtHttpRequest request = new MtHttpRequest(endPoint, "post");
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("reqtype", "get_zk_list");
        request.setParameters(parameters);
        try {
            MtHttpResponse response = MtHttpClientUtil.executeHttpRequest(request);
            MtHttpClientUtil.showResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testReportClientInfo() {
        MtHttpRequest request = new MtHttpRequest(endPoint, "post");
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("reqtype", "report_zk_client_info");
        request.setParameters(parameters);
        JSONObject postData = new JSONObject();
        postData.put("clientInfo", "xxxxx");
        request.setContentData(postData.toString().getBytes());
        try {
            MtHttpResponse response = MtHttpClientUtil.executeHttpRequest(request);
            MtHttpClientUtil.showResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        //testGetZkList();
        testReportClientInfo();
    }
}
