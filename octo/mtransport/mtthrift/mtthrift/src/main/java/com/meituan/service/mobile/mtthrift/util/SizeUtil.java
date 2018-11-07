package com.meituan.service.mobile.mtthrift.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-7-25
 * Time: 下午8:06
 */
public class SizeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SizeUtil.class);
    private static final String sizeRangeConfig = "1,2,4,8,16,32,64,128,256,512,1024";
    private static final long sizeMin = 0;
    private static int[] sizeRangeArray = initRangeArray(sizeRangeConfig);

    public static int[] initRangeArray(String rangeConfig) {
        String[] range = rangeConfig.split(",");
        int end = Integer.valueOf(range[range.length - 1]);
        int[] rangeArray = new int[end];
        int rangeIndex = 0;
        for (int i = 0; i < end; i++) {
            if (range.length > rangeIndex) {
                int value = Integer.valueOf(range[rangeIndex]);
                if (i >= value) {
                    rangeIndex++;
                }
                rangeArray[i] = value;
            }
        }
        return rangeArray;
    }

    public static String getLogSize(int size) {
        if (size > sizeMin) {
            try {
                return getLogSize(size, sizeRangeArray);
            } catch (Exception e) {
                LOG.warn("error while logging size:" + e.getMessage());
            }
        }
        return null;
    }

    public static String getLogSize(int size, int[] rangeArray) {
        if (size > 0 && rangeArray != null && rangeArray.length > 0) {
            String value = ">" + rangeArray[rangeArray.length - 1] + "k";
            int sizeK = (int) Math.ceil(size * 1d / 1024);
            if (rangeArray.length > sizeK) {
                value = "<" + rangeArray[sizeK] + "k";
            }
            return value;
        }
        return null;
    }

}
