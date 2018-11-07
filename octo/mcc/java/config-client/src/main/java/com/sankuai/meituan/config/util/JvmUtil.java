package com.sankuai.meituan.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-9
 */
@Deprecated
public class JvmUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JvmUtil.class);

    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            LOG.warn("failed to get pid.", e);
            return -1;
        }
    }
}
