package com.sankuai.mtthrift.testSuite;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class MultiServerTest {

    private static ClassPathXmlApplicationContext serverBeanFactory;

    @BeforeClass
    public static void start() throws InterruptedException, TTransportException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/multiServer.xml");
    }


    @AfterClass
    public static void stop() {
        serverBeanFactory.destroy();
    }

    @Test
    public void test() throws TException, InterruptedException, IOException {
        Thread.sleep(10 * 1000);
    }
}
