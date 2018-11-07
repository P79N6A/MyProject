package com.meituan.mtrace.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author zhangzhitong
 * @created 3/7/16
 */
public class Log4jTest {
    private Logger logger = Logger.getLogger(Log4jTest.class);
    @Test
    public void testLog4j() {
        logger.addAppender(new MtraceAppender());
        logger.setLevel(Level.DEBUG);
        logger.debug("test");
        try {
            throw new RuntimeException();
        } catch (Exception e) {
            logger.warn("no" + e, e);
        }

    }
    @Test
    public void testString() {
        String s = "this one = wrong \t, throw exception \n";
        String result = s.replaceAll("\\n|\\t", ": &lt;br/&gt;").replaceAll("=", ":");
        logger.info(result);
        System.out.println(result);
    }

}
