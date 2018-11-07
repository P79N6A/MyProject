package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

import static com.sankuai.sgagent.thrift.model.CustomizedStatus.ALIVE;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 16/12/22
 * Time: 18:37
 */
public class ConfigStatusTest {
    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;

    private static Twitter.Iface client;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/configStatus/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/configStatus/client.xml");
        client = clientBeanFactory.getBean("clientProxy", Twitter.Iface.class);
        Thread.sleep(30000);
        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();

        if (!ContextStore.getRequestMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    @Test
    public void test() throws TException, IOException, InterruptedException {
        try {
            System.out.println(client.testString("hell0"));
        } catch (Exception e) {
            assert (e.getMessage().contains("connection list is empty"));
        }
        ThriftServerPublisher serverPublisher = serverBeanFactory.getBean("serverPublisher", ThriftServerPublisher.class);
        serverPublisher.getConfigStatus().setRuntimeStatus(ALIVE);
        Thread.sleep(20000);
        assert ("w0rld".equals(client.testString("w0rld")));
    }
}
