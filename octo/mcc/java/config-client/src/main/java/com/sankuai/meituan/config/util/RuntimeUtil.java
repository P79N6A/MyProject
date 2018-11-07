package com.sankuai.meituan.config.util;

import com.google.common.base.Throwables;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.*;

public class RuntimeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeUtil.class);
    @Deprecated
    public static final String ONLINE_IP_PREFIX_SET = "10.";

    @Deprecated
    public static boolean isMacOS() {
        String osName = System.getProperty("os.name").toUpperCase();
        return "MAC OS X".equals(osName);
    }

    public static boolean isOnlineIp() {
        return ProcessInfoUtil.isLocalHostOnline();
    }

    @Deprecated
    public static String getLocalIpV4(){
        return ProcessInfoUtil.getLocalIpV4();
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
