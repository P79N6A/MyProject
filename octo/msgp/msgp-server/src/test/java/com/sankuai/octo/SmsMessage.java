package com.sankuai.octo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.apache.commons.net.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class SmsMessage {

    private static void testIndustry(String urlString, String uriString) {
        try {

            URL url = new URL(urlString + uriString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String clientId = "com.sankuai.inf.msgp";
            String httpVerb =  "POST";

            String dateStr = DateTimeUtil.format(new Date(), "yy-MM-dd hh:mm:ss");
            String clientSecret = "AE8ECE065466EA418B1EB21422B4C2DA";
            String requestUri = uriString;
            String authorization = generateAuthorization(clientId, httpVerb, requestUri, dateStr, clientSecret);
            conn.setRequestProperty("Date", dateStr);
            conn.setRequestProperty("Authorization", authorization);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(1000 * 5);

            try {
                JSONObject data = new JSONObject();
                data.put("type","6656");
                JSONArray mobiles = new JSONArray();
                for (int i = 0; i < 1; i++) {
                    mobiles.add("13426429178");
                }
                data.put("mobiles", mobiles);
                JSONObject pairs = new JSONObject();
                pairs.put("message", "你好");
                data.put("pairs", pairs);
//                conn.getOutputStream().write(data.toString().getBytes("UTF-8"));
            } catch (JSONException e) {
                return;
            }

            conn.getOutputStream().flush();
            conn.getOutputStream().close();

            InputStream stream = conn.getInputStream();
            ByteArrayOutputStream responseParam = new ByteArrayOutputStream();

            if (stream != null) {
                byte[] buffer = new byte[1024];
                int bytesRead = -1;
                while ((bytesRead = stream.read(buffer)) > 0) {
                    responseParam.write(buffer, 0, bytesRead);
                }
            }
            System.out.println(responseParam.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String generateAuthorization(String clientId, String httpVerb, String requestUri, String reqDate, String clientSecret) throws Exception{
        String strToSign = httpVerb + " " + requestUri + "\n" + reqDate;
        String signature = new String(Base64.encodeBase64(hmacSHA1Encrypt(strToSign, clientSecret)));
        String authorization = "MWS" + " " + clientId + ":" + signature;
        return authorization;
    }
    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    public static byte[] hmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception{
        byte[] data=encryptKey.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = encryptText.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }


    public static void main(String args[]) throws Exception{
         testIndustry("http://bjsms.dp", "/send/industry");
    }

}
