package com.sankuai.meituan.config.util;

import com.google.common.base.Throwables;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {
    public static String getMd5(byte[] data) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(data);
            byte digestBytes[] = md5.digest();
            StringBuilder result = new StringBuilder("");
            for (int offset = 0; offset < digestBytes.length; offset++) {
                int i = digestBytes[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    result.append("0");
                result.append(Integer.toHexString(i));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw Throwables.propagate(e);
        }
    }
}
