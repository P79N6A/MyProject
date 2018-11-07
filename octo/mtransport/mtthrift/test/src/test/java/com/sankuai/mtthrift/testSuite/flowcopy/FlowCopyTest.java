package com.sankuai.mtthrift.testSuite.flowcopy;

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import com.meituan.service.mobile.mtthrift.server.flow.FlowCopyTask;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import com.sankuai.mtthrift.testSuite.idl.TestServiceImpl;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/25
 */
public class FlowCopyTest {

    private String appkey = "com.sankuai.inf.mtthrift.testClient";
    ThriftServerPublisher serverPublisher;
    ThriftClientProxy clientProxy;

    @Before
    public void init() throws Exception {
        serverPublisher = new ThriftServerPublisher();
        serverPublisher.setAppKey(appkey);
        serverPublisher.setPort(9001);
        serverPublisher.setServiceInterface(TestService.class);
        serverPublisher.setServiceImpl(new TestServiceImpl());
        serverPublisher.afterPropertiesSet();

        clientProxy = new ThriftClientProxy();
        clientProxy.setAppKey(appkey);
        clientProxy.setServiceInterface(TestService.class);
        clientProxy.setLocalServerPort(9001);
        clientProxy.setRemoteUniProto(true);
        clientProxy.afterPropertiesSet();
    }

    @Test
    public void normalTest() throws Exception {
        for (int i = 0; i < 100; i++) {
            try {
                ((TestService.Iface) clientProxy.getObject()).testMock("aa");
            } catch (TException e) {
                e.printStackTrace();
                Assert.fail("Should not throw exception");
            }
        }
    }

    @After
    public void finish() {
        FlowCopyTask.stop();
        clientProxy.destroy();
        serverPublisher.destroy();
    }
}
