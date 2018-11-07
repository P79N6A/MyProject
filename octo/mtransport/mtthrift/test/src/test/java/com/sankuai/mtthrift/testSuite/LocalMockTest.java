package com.sankuai.mtthrift.testSuite;


import com.sankuai.mtthrift.testSuite.idl.TestService;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-19
 * Time: 上午11:20
 */
public class LocalMockTest {

    @Test
    public void testMock(){
        String param = "hello";
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("testSuite/localmock/client.xml");
        TestService.Iface client = (TestService.Iface) beanFactory.getBean("clientProxy");
        TestService.Iface nettyClient = (TestService.Iface) beanFactory.getBean("nettyClientProxy");
        String result = null;
        try {
            result = client.testMock(param);
            System.out.println("result:" + result);
            Assert.assertEquals(param, result);

            result = nettyClient.testMock(param);
            System.out.println("result:" + result);
            Assert.assertEquals(param, result);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }
}
