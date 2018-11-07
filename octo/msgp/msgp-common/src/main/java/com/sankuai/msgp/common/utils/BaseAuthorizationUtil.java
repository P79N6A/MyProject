package com.sankuai.msgp.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by yves on 17/2/9.
 */
public class BaseAuthorizationUtil {
    private static final Log serviceLog = LogFactory.getLog(BaseAuthorizationUtil.class);

    public static final String HTTP_HEADER_DATE = "Date";
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    public static final String MEITUAN_AUTH_METHOD = "MWS";
    public static final String HTTP_HEADER_TIME_ZONE = "GMT";
    public static final String HTTP_HEADER_DATE_FORMAT = "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'z";
    public static final String ALGORITHM_HMAC_SHA1 = "HmacSHA1";

    /**
     * * 生成请求头认证和date信息<br>
     * request在调用本方法之前必须已经设置了uri
     *
     * @param request
     * @param client
     * @param secret
     */
    public static void generateAuthAndDateHeader(HttpRequestBase request, String client, String secret) {
        Date sysdate = new Date();
        SimpleDateFormat df = new SimpleDateFormat(HTTP_HEADER_DATE_FORMAT, Locale.US);
        df.setTimeZone(TimeZone.getTimeZone(HTTP_HEADER_TIME_ZONE));
        String date = df.format(sysdate);
        String string_to_sign = request.getMethod().toUpperCase() + " " + request.getURI().getPath() + "\n" + date;
        String sig = secret;
        String encoding = "";
        try {
            encoding = getSignature(string_to_sign.getBytes(), sig.getBytes());
        } catch (Exception e1) {
            serviceLog.error("获取签名数据异常", e1);
            return;
        }
        String authorization = MEITUAN_AUTH_METHOD + " " + client + ":" + encoding;
        request.addHeader(HTTP_HEADER_AUTHORIZATION, authorization);
        request.addHeader(HTTP_HEADER_DATE, date);
    }

    public static String getSignature(byte[] data, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        SecretKeySpec signingKey = new SecretKeySpec(key, ALGORITHM_HMAC_SHA1);
        Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA1);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data);
        return new String(Base64.encodeBase64(rawHmac));
    }

}