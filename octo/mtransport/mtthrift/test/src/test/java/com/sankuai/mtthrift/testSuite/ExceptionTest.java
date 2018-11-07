package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.mtthrift.testSuite.idl.MyException;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import com.sankuai.sgagent.thrift.model.ProtocolResponse;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExceptionTest {

    /**
     * Mock Server List Info
     * {
     "appkey":	"com.sankuai.inf.mtthrift.testServer",
     "version":	"mtthrift-v1.7.2-NightlyBuild-SNAPSHOT",
     "ip":	"172.18.189.117",
     "port":	9011,
     "weight":	10,
     "status":	0,
     "role":	0,
     "env":	3,
     "lastUpdateTime":	1473064477,
     "extend":	"OCTO|slowStartSeconds:180",
     "fweight":	10,
     "serverType":	0,
     "heartbeatSupport":	3,
     "protocol":	"thrift",
     "serviceInfo":	{
     "com.sankuai.mtthrift.testSuite.idl.TestService":	{
     "unifiedProto":	1
     }
     }
     }
     */

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static TestService.Iface client;
    private static TestService.Iface nettyClient;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/idl/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/idl/syncClient.xml");
        client = (TestService.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (TestService.Iface) clientBeanFactory.getBean("nettyClientProxy");

//        Map<String, ServiceDetail> serviceDetailMap =  new HashMap<String, ServiceDetail>(){
//            {
//                put("com.sankuai.mtthrift.testSuite.idl.TestService", new ServiceDetail(true));
//            }
//        };
//        SGService sgService = new SGService();
//        sgService.setAppkey("com.sankuai.inf.mtthrift.testServer")
//                .setVersion("")
//                .setIp(ProcessInfoUtil.getLocalIpV4())
//                .setPort(9011).setWeight(10).setStatus(2).setRole(0).setFweight(10.d)
//                .setHeartbeatSupport((byte)(0x03)).setProtocol("thrift").setServiceInfo(serviceDetailMap);
//
//
//        List<SGService > sgServiceList = new ArrayList<SGService>();
//        sgServiceList.add(sgService);
//        ProtocolResponse response = new ProtocolResponse();
//        response.setErrcode(0);
//        response.setServicelist(sgServiceList);
//        InvokeProxy.setIsMock(true);
//        InvokeProxy.setMockValue(sgServiceList);

        Thread.sleep(30000);
        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void stop() {
        InvokeProxy.setIsMock(false);
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
    public void nettyTestMock() {
        try {
            System.out.println("testMock");
            String result = client.testMock("mock");
            assert (result.equals("mock"));
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testProtocolMisMatch() {
        try {
            System.out.println("testProtocolMisMatch");
            client.testProtocolMisMatch();
        } catch (TException e) {
//            assert (e.getCause() instanceof TTransportException);
            assert (e.getMessage().contains("Exception:org.apache.thrift.protocol.TProtocolException"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testTransportException() {
        try {
            System.out.println("testTransportException");
            client.testTransportException();
        } catch (TException e) {
            assert (e.getCause() instanceof TTransportException);
            return;
        }
        Assert.fail();
    }

    @Test
    public void testTimeout() {
        try {
            client.testTimeout();
        } catch (TException e) {
            assert (e.getMessage().contains("timeout"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void testException() {
        try {
            System.out.println(client.testException());
            Assert.fail();
        } catch (MyException e) {
            assert (e instanceof MyException);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            System.out.println(client.testBaseTypeException());
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (MyException e) {
            Assert.fail();
        }
    }

    @Test
    public void testNull() {
        try {
            client.testNull();
        } catch (TException e) {
            assert (e.getCause() instanceof TApplicationException);
            assert (e.getMessage().contains("NullPointerException"));// .getCause() instanceof NullPointerException);
            return;
        }
        Assert.fail();
    }


    @Test
    public void testReturnNull() {
        try {
            String result = client.testReturnNull();
            assert (null == result);
        } catch (TException e) {
            assert (e.getCause() instanceof TApplicationException);
            assert (e.getMessage().contains("NullPointerException"));// .getCause() instanceof NullPointerException);
        }
    }

    @Test
    public void testMock() {
        try {
            System.out.println("testMock");
            String result = nettyClient.testMock("mock");
            assert (result.equals("mock"));
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyTestProtocolMisMatch() {
        try {
            System.out.println("testProtocolMisMatch");
            nettyClient.testProtocolMisMatch();
        } catch (TException e) {
            assert (e.getMessage().contains("Exception:org.apache.thrift.protocol.TProtocolException"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void nettyTestTransportException() {
        try {
            System.out.println("testTransportException");
            nettyClient.testTransportException();
        } catch (TException e) {
            assert (e.getCause() instanceof TTransportException);
            return;
        }
        Assert.fail();
    }

    @Test
    public void nettyTestTimeout() {
        try {
            nettyClient.testTimeout();
        } catch (TException e) {
            assert (e.getMessage().contains("timeout"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void nettyTestException() {
        try {
            System.out.println(nettyClient.testException());
            Assert.fail();
        } catch (MyException e) {
            assert (e instanceof MyException);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyTestNull() {
        try {
            nettyClient.testNull();
        } catch (TException e) {
            assert (e.getCause() instanceof TApplicationException);
            assert (e.getMessage().contains("NullPointerException"));// .getCause() instanceof NullPointerException);
            return;
        }
        Assert.fail();
    }


    @Test
    public void nettyTestReturnNull() {
        try {
            String result = nettyClient.testReturnNull();
            assert (null == result);
        } catch (TException e) {
            assert (e.getCause() instanceof TApplicationException);
            assert (e.getMessage().contains("NullPointerException"));// .getCause() instanceof NullPointerException);
        }
    }
}
