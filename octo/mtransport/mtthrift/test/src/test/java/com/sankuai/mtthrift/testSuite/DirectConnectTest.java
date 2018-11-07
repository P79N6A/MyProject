package com.sankuai.mtthrift.testSuite;

import com.sankuai.mtthrift.testSuite.idlTest.Tweet;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.mtthrift.testSuite.idlTest.TwitterUnavailable;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DirectConnectTest {
    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/direct/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/direct/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    @Test
    public void test() throws TException {
        Assert.assertEquals(client.testString("hello"), "hello");
        try {
            client.testException(new Tweet(1, "a", "b"));
        } catch (TwitterUnavailable twitterUnavailable) {
            return;
        }
        Assert.fail();
    }
}
