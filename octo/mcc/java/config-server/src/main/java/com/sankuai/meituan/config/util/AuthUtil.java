package com.sankuai.meituan.config.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by liangchen on 2017/11/21.
 */
public class AuthUtil {
    private final static Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    public static final String HMAC_SHA1 = "HmacSHA1";

    public static String hmacSHA1(String token, String data) {
        String signature = "";
        try {
            SecretKeySpec secretKey = new SecretKeySpec(token.getBytes(), HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(data.getBytes("utf-8"));
            signature = byteToHexString(bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
