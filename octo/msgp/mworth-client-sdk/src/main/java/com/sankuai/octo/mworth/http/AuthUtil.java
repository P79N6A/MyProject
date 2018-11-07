package com.sankuai.octo.mworth.http;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AuthUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AuthUtil.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private AuthUtil() {
    }

    /**
     * @param uri
     * @param method
     * @param date     时间格式"EEE, d MMM yyyy HH:mm:ss z"
     * @param clientId
     * @param secret
     * @return
     */
    public static String getAuthorization(String uri, String method, String date, String clientId, String secret) {
        String stringToSign = method + " " + uri + "\n" + date;
        String signature = getSignature(stringToSign, secret);
        return "MWS" + " " + clientId + ":" + signature;
    }

    public static String getSignature(String data, String secret) {
        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = Base64.encodeToString(rawHmac);
        } catch (Exception e) {
            LOG.warn("", e);
            throw new IllegalStateException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }
}
