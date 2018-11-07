package com.meituan.control.zookeeper.util;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.*;
import java.util.Enumeration;

public class RuntimeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeUtil.class);

    public static boolean isMacOS() {
        String osName = System.getProperty("os.name").toUpperCase();
        return "MAC OS X".equals(osName);
    }

    public static String getRootResourcePath() {
        URL resource = RuntimeUtil.class.getResource("/");
        if (resource != null) {
            return resource.getFile();
        }else {
            try {
                return new File(RuntimeUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
            } catch (URISyntaxException e) {
                throw Throwables.propagate(e);
            }
        }
    }
}
