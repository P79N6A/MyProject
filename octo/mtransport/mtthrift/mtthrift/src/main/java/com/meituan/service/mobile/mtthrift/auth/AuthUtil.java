package com.meituan.service.mobile.mtthrift.auth;

import com.meituan.mtrace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;


public class AuthUtil {

    private final static Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    public static final String APPKEY = "auth-appkey";
    public static final String SIGNATURE = "auth-signature";
    public static final String HMAC_SHA1 = "HmacSHA1";
    public static final String INF_RPC_AUTH = "INF_AUTH";

    public static void setRequestContext(SignMetaData signMetaData) {
        Tracer.getClientTracer().putRemoteOneStepContext(APPKEY, signMetaData.getAppkey());
        Tracer.getClientTracer().putRemoteOneStepContext(SIGNATURE, signMetaData.getSignature());
    }

    public static void setUniformSignContext(String signInfo) {
        Tracer.getClientTracer().putRemoteOneStepContext(INF_RPC_AUTH, signInfo);
    }

    public static String hmacSHA1(String token, String data) {
        String signature = "";
        try {
            SecretKeySpec secretKey = new SecretKeySpec(token.getBytes(), HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(data.getBytes("utf-8"));
            signature = byteToHexString(bytes);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
        return signature;
    }

    public static String byteToHexString(byte[] bytes) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexString.append(hex.toUpperCase());
        }
        return hexString.toString();
    }
}
