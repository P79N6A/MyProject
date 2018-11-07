package com.sankuai.inf.octo.mns.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by lhmily on 03/25/2017.
 */
public class VersionUtil {
    private static Logger LOG = LoggerFactory.getLogger(VersionUtil.class);
    private static String version = null;


    private static Object versionInitLock = new Object();

    private VersionUtil() {
    }

    public static String getVersion() {


        if (null == version) {
            synchronized (versionInitLock) {
                if (null == version) {
                    String fileName = "application.properties";
                    Properties props = new Properties();
                    InputStream in = null;
                    try {
                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        if (null != cl) {
                            LOG.debug("Trying to find [{}] using class loader {}.", fileName, cl);
                            in = cl.getResourceAsStream(fileName);
                        }

                        if (null == in) {
                            cl = VersionUtil.class.getClassLoader();
                            if (null != cl) {
                                LOG.debug("Trying to find [{}] using class loader {}.", fileName, cl);
                                in = cl.getResourceAsStream(fileName);
                            }
                        }
                        if (null != in) {
                            props.load(in);
                            version = props.getProperty("version");
                        }

                        if (null == version) {
                            version = "";
                        }
                    } catch (FileNotFoundException e) {
                        LOG.debug(fileName + " does not exist", e);
                        version = "";
                    } catch (Exception e) {
                        LOG.debug("Failed to load config from " + fileName, e);
                        version = "";
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Exception e) {
                                LOG.debug("Failed to close " + fileName, e);
                            }
                        }
                    }
                }
            }
        }
        return version;
    }
}
