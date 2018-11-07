package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.sgagent.thrift.model.ProtocolResponse;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/2/15
 * Description:
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MockServerListTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;
    private static Twitter.Iface nettyClient;

    /**
     * public SGService(String appkey, String version, String ip, int port, int weight, int status, int role, int envir,
     *      int lastUpdateTime, String extend, double fweight, int serverType, String protocol,
     *      Map<String, ServiceDetail> serviceInfo, byte heartbeatSupport)
     * @throws InterruptedException
     */

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/mockServerList/server.xml");
        Thread.sleep(5000);
        InvokeProxy.setIsMock(true);
        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void stop() {
        InvokeProxy.setIsMock(false);
        serverBeanFactory.destroy();
        if (!ContextStore.getRequestMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    private void testI32() throws TException, InterruptedException {
        Thread.sleep(10000);
        int b = 123;
        int result = client.testI32(b);
        assert (result == b);
    }

    private void nettyTestI32() throws TException, InterruptedException {
        Thread.sleep(10000);
        int b = 123;
        int result = nettyClient.testI32(b);
        assert (result == b);
    }

    @Test
    public void emptyList() throws InterruptedException {

        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/mockServerList/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");

        List<SGService> serviceList = new ArrayList<SGService>();

        ProtocolResponse response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(serviceList);
        InvokeProxy.setMockValue(response);

        try {
            testI32();
        } catch (TException e) {
            assert (e.getMessage().contains("is empty"));
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            assert (e.getMessage().contains("is empty"));
        }

        clientBeanFactory.destroy();

    }


    @Test
    public void nullList() throws InterruptedException{

        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/mockServerList/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");

        // wrong port.
        List<SGService> serviceList = new ArrayList<SGService>();

        ProtocolResponse response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(null);
        InvokeProxy.setMockValue(response);

        try {
            testI32();
        } catch (TException e) {
            assert (e.getMessage().contains("is empty"));
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            assert (e.getMessage().contains("is empty"));
        }

        clientBeanFactory.destroy();

    }


    @Test
    public void correctServerList() throws InterruptedException {


        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/mockServerList/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");

        List<SGService> serviceList = (new ArrayList<SGService>() {
            {
                add(new SGService("com.sankuai.inf.mtthrift.testServer",
                        "", ProcessInfoUtil.getLocalIpV4(), 9020, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte)0x01));
            }
        });
        ProtocolResponse response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(serviceList);
        InvokeProxy.setMockValue(response);
        try {
            testI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        clientBeanFactory.destroy();
    }


    @Test
    public void wrongServerList() throws InterruptedException {

        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/mockServerList/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");

        // wrong port.
        List<SGService> serviceList = (new ArrayList<SGService>() {
            {
                add(new SGService("com.sankuai.inf.mtthrift.testServer",
                        "", ProcessInfoUtil.getLocalIpV4(), 9999, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte)0x01));
            }
        });

        ProtocolResponse response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(serviceList);
        InvokeProxy.setMockValue(response);

        try {
            testI32();
        } catch (TException e) {
            // assert exception message is not empty list
            assert (!e.getMessage().contains("is empty"));
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            assert (!e.getMessage().contains("is empty"));
        }

        clientBeanFactory.destroy();
    }


    @Test
    public void listChange2Null() throws InterruptedException {
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/mockServerList/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");

        List<SGService> serviceList = (new ArrayList<SGService>() {
            {
                add(new SGService("com.sankuai.inf.mtthrift.testServer",
                        "", ProcessInfoUtil.getLocalIpV4(), 9020, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte)0x01));
            }
        });
        ProtocolResponse response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(serviceList);
        InvokeProxy.setMockValue(response);
        try {
            testI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(null);
        InvokeProxy.setMockValue(response);

        try {
            testI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        clientBeanFactory.destroy();
    }



    @Test
    public void listChange2Empty() throws InterruptedException {
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/mockServerList/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");

        List<SGService> serviceList = (new ArrayList<SGService>() {
            {
                add(new SGService("com.sankuai.inf.mtthrift.testServer",
                        "", ProcessInfoUtil.getLocalIpV4(), 9020, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte)0x01));
            }
        });

        ProtocolResponse response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(serviceList);
        InvokeProxy.setMockValue(response);
        try {
            testI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        serviceList = new ArrayList<SGService>();

        response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(serviceList);
        InvokeProxy.setMockValue(response);

        try {
            testI32();
        } catch (TException e) {
            Assert.assertTrue(e.getMessage().contains("is empty"));
        }

        try {
            nettyTestI32();
        } catch (TException e) {
            Assert.assertTrue(e.getMessage().contains("is empty"));
        }

        clientBeanFactory.destroy();
    }


}
