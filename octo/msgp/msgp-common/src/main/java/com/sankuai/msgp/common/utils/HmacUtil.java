package com.sankuai.msgp.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by yves on 17/1/3.
 */

@SuppressWarnings("unchecked")
public class HmacUtil {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String getSignature(String data, String key) {
        String result;

        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("utf-8"), HMAC_SHA1_ALGORITHM);

            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(data.getBytes("utf-8"));

            Base64 base64 = new Base64();
            result = base64.encodeToString(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC : " + e.getMessage());
        }

        return result;
    }
}
