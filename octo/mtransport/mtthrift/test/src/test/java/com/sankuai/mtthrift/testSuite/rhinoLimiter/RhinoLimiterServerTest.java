package com.sankuai.mtthrift.testSuite.rhinoLimiter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2018 Meituan
 * All rights reserved
 * Description：
 * User: wuxinyu
 * Date: Created in 2018/5/27 下午7:07
 * Copyright: Copyright (c) 2018
 */
public class RhinoLimiterServerTest {

    private static Logger logger = LoggerFactory.getLogger(RhinoLimiterServerTest.class);

    private static ClassPathXmlApplicationContext serverBeanFactory;


    @BeforeClass
    public static void start() {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/rhinoLimiter/server.xml");

    }

    @AfterClass
    public static void stop() {
        serverBeanFactory.destroy();
    }

    @Test
    public void startServer() throws InterruptedException {
        Thread.sleep(10000000);
    }

}
