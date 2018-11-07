package com.meituan.mtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLog {
    private MLog() {}
    private static Logger logger = LoggerFactory.getLogger("com.meituan.mtrace.MLog");
    public static void info(String s) {
        if (MtraceConfig.getInstance().isMLog()) {
            logger.info(s);
        }
    }
    public static void info(Object s) {
        if (MtraceConfig.getInstance().isMLog()) {
            logger.info(s.toString());
        }
    }

}
