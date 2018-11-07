package com.meituan.service.mobile.mtthrift.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-8-11
 * Time: 下午4:06
 */
public class ConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    private static final String DEFAULT_APPKEY = null;
    private static final String APPKEY_CONFIG_FILE_NAME = "META-INF/app.properties";
    private static final String APPKEY_OCTO_PROP = "app.name";
    private static final String APPKEY_SYSTEM_PROP_NAME = "app.key";

    private static final String APPKEY;

    static {
        Properties props = new Properties();
        ClassLoader cl;
        InputStream is = null;

        cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            logger.debug("Trying to find [{}] using class loader {}.", APPKEY_CONFIG_FILE_NAME, cl);
            is = cl.getResourceAsStream(APPKEY_CONFIG_FILE_NAME);
        }

        if (is == null) {
            cl = ConfigUtil.class.getClassLoader();
            if (cl != null) {
                logger.debug("Trying to find [{}] using class loader {}.", APPKEY_CONFIG_FILE_NAME, cl);
                is = cl.getResourceAsStream(APPKEY_CONFIG_FILE_NAME);
            }
        }

        if (is != null) {
            try {
                props.load(is);
            } catch (IOException ignored) {
                logger.debug("props.load ERROR {}", ignored);
            }
        }
        String appkey = props.getProperty(APPKEY_OCTO_PROP, DEFAULT_APPKEY);
        logger.debug("props.getProperty {} {}", APPKEY_OCTO_PROP, appkey);
        APPKEY = (appkey == DEFAULT_APPKEY) ? System.getProperty(APPKEY_SYSTEM_PROP_NAME, DEFAULT_APPKEY) : appkey;

    }

    public static String getAPPKEY() {
        return APPKEY;
    }
}
