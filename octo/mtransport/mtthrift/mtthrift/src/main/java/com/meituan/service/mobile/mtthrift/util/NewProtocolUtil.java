package com.meituan.service.mobile.mtthrift.util;

import org.xerial.snappy.Snappy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-5-31
 * Time: 下午6:40
 */
public class NewProtocolUtil {
    public static byte[] getChecksum(byte[] bytes) {
        Adler32 a32 = new Adler32();
        a32.update(bytes, 0, bytes.length);
        int sum = (int) a32.getValue();
        byte[] checksum = new byte[4];
        checksum[0] = (byte) (sum >> 24);
        checksum[1] = (byte) (sum >> 16);
        checksum[2] = (byte) (sum >> 8);
        checksum[3] = (byte) (sum);
        return checksum;
    }

    public static boolean compareChecksum(final long compareSum, final byte[]
            bytes) {
        Adler32 a32 = new Adler32();
        a32.update(bytes, 0, bytes.length);
        return (compareSum == a32.getValue());
    }

    public static boolean bytesEquals(byte[] bytes1, byte[] bytes2) {
        boolean equals = true;
        if (null == bytes1 && null == bytes2) {
        } else if(null == bytes1 || null == bytes2) {
            equals = false;
        } else if(bytes1.length != bytes2.length) {
            equals = false;
        } else {
            int len = bytes1.length;
            for(int i = 0; i < len; i++) {
                if(bytes1[i] != bytes2[i]) {
                    equals = false;
                    break;
                }
            }
        }

        return equals;
    }

    public static byte[] gZip(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        try {
            gos.write(data);
            gos.finish();
            gos.flush();
            byte[] output = bos.toByteArray();
            return output;
        } finally {
            gos.close();
            bos.close();
        }
    }

    public static byte[] unGZip(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPInputStream gis = new GZIPInputStream(bis);

        int count;
        byte[] buf = new byte[1024];
        try {
            while ((count = gis.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, count);
            }
            bos.flush();
            byte[] result = bos.toByteArray();
            return result;
        } finally {
            gis.close();
            bos.close();
            bis.close();

        }
    }

    public static byte[]  compressSnappy(byte[] buf) throws IOException {
        if (buf == null) {
            return null;
        }
        return Snappy.compress(buf);
    }

    public static byte[] unCompressSnappy(byte[] buf) throws IOException {
        if (buf == null) {
            return null;
        }
        return Snappy.uncompress(buf);
    }
}

