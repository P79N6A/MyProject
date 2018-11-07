package com.meituan.service.mobile.mtthrift.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-4-26
 * Time: 下午2:25
 * 取得当前jar的版本号
 */
public class MtThriftManifest {

    private static final Logger logger = LoggerFactory.getLogger(MtThriftManifest.class);

    private static final String DEFAULT_VERSION = "1.7.2";
    private static final String PROPERTIES_NAME = "mtthrift-application.properties";
    private static final String MTTHRIFT_VERSION = "mtthrift.version";


    private static String version = null;

    static {
        version = genVersion();
    }

    private static String genVersion() {
        Properties props = new Properties();
        ClassLoader cl;
        InputStream is = null;

        cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            logger.debug("Trying to find [{}] using class loader {}.", PROPERTIES_NAME, cl);
            is = cl.getResourceAsStream(PROPERTIES_NAME);
        }

        if (is == null) {
            cl = MtThriftManifest.class.getClassLoader();
            if (cl != null) {
                logger.debug("Trying to find [{}] using class loader {}.", PROPERTIES_NAME, cl);
                is = cl.getResourceAsStream(PROPERTIES_NAME);
            }
        }

        if (is != null) {
            try {
                props.load(is);
            } catch (IOException ignored) {
                logger.debug("props.load ERROR {}", ignored);
            }
        }
        String version = props.getProperty(MTTHRIFT_VERSION, DEFAULT_VERSION);
        return version;
    }

    public static String getVersion() {
        return version;
    }

    public static void main(String... avgs) {
        System.out.println(getVersion());
    }
}