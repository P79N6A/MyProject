package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.mtthrift.testSuite.idlTest.TwitterImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class MultiClientTest {

    private static Logger logger = LoggerFactory.getLogger(MultiClientTest.class);

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client1;
    private static Twitter.Iface client2;
    private static TestService.Iface client3;
    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter multiServiceAnnotationClient;
    private static Twitter.Iface multiServiceIdlClient;

    private static StringBuilder client1ChainStr1 = new StringBuilder();
    private static StringBuilder client2ChainStr1 = new StringBuilder();
    private static StringBuilder client3ChainStr1 = new StringBuilder();

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/filter/multiServer.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/filter/multiClient.xml");
        client1 = (Twitter.Iface) clientBeanFactory.getBean("clientProxy1");
        client2 = (Twitter.Iface) clientBeanFactory.getBean("clientProxy2");
        client3 = (TestService.Iface) clientBeanFactory.getBean("clientProxy3");
        multiServiceAnnotationClient = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("annotationClientProxy");
        multiServiceIdlClient = (Twitter.Iface) clientBeanFactory.getBean("idlClientProxy");

        buildExpectInvokeChainStr();
    }

    @Test
    public void testList() throws Exception {
        List<String> param = new ArrayList<String>();
        param.add("Test");
        FilterTest.invokeChainStr = new StringBuilder();
        List<String> result = client1.testList(param);
        Assert.assertEquals(param, result);
        Assert.assertEquals(client1ChainStr1.toString(), FilterTest.invokeChainStr.toString());

        FilterTest.invokeChainStr = new StringBuilder();
        result = client2.testList(param);
        Assert.assertEquals(param, result);
        Assert.assertEquals(client2ChainStr1.toString(), FilterTest.invokeChainStr.toString());

        FilterTest.invokeChainStr = new StringBuilder();
        String client3Ret = client3.testMock("Test");
        Assert.assertEquals("Test", client3Ret);
        Assert.assertEquals(client3ChainStr1.toString(), FilterTest.invokeChainStr.toString());
    }

    @Test
    public void testMultiService() throws Exception {
        List<String> param = new ArrayList<String>();
        param.add("Test");
        FilterTest.invokeChainStr = new StringBuilder();
        List<String> result = multiServiceAnnotationClient.testList(param);
        Assert.assertEquals(param, result);
        Assert.assertEquals(client1ChainStr1.toString(), FilterTest.invokeChainStr.toString());

        FilterTest.invokeChainStr = new StringBuilder();
        result = multiServiceIdlClient.testList(param);
        Assert.assertEquals(param, result);
        Assert.assertEquals(client1ChainStr1.toString(), FilterTest.invokeChainStr.toString());
    }

    private ThriftServerPublisher apiWayServer() throws Exception {
        ThriftServerPublisher serverPublisher = new ThriftServerPublisher();
        serverPublisher.setAppKey("com.sankuai.inf.mtthrift.testServer");
        serverPublisher.setPort(9008);
        serverPublisher.setServiceInterface(Twitter.class);
        serverPublisher.setServiceImpl(new TwitterImpl());
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new AccessLogFilter());
        filters.add(new SpecificFilter());
        serverPublisher.setFilters(filters);
        serverPublisher.afterPropertiesSet();
        return serverPublisher;
    }

    private ThriftClientProxy apiWayClient() throws Exception {
        ThriftClientProxy clientProxy = new ThriftClientProxy();
        clientProxy.setAppKey("com.sankuai.inf.mtthrift.testServer");
        clientProxy.setRemoteAppkey("com.sankuai.inf.mtthrift.testServer");
        clientProxy.setServiceInterface(TestService.class);
        clientProxy.setServerIpPorts("localhost:9008");
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new SpecificFilter());
        clientProxy.setFilters(filters);
        clientProxy.afterPropertiesSet();
        return clientProxy;
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    private static void buildExpectInvokeChainStr() {
        client1ChainStr1.append(TraceFilter.class.getSimpleName())
                .append(InvokerFilter.class.getSimpleName())
                .append(SpecificFilter.class.getSimpleName())
                .append(ClientQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(CustomFilter2.class.getSimpleName())
                .append(TraceFilter.class.getSimpleName())
                .append(ServerQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(ProviderFilter.class.getSimpleName())
                .append(CustomFilter2.class.getSimpleName())
                .append(CustomFilter1.class.getSimpleName());
        client2ChainStr1.append(TraceFilter.class.getSimpleName())
                .append(InvokerFilter.class.getSimpleName())
                .append(ClientQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(CustomFilter1.class.getSimpleName())
                .append(TraceFilter.class.getSimpleName())
                .append(ServerQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(ProviderFilter.class.getSimpleName())
                .append(CustomFilter2.class.getSimpleName())
                .append(CustomFilter1.class.getSimpleName());
        client3ChainStr1.append(TraceFilter.class.getSimpleName())
                .append(InvokerFilter.class.getSimpleName())
                .append(ClientQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(CustomFilter2.class.getSimpleName())
                .append(CustomFilter1.class.getSimpleName())
                .append(TraceFilter.class.getSimpleName())
                .append(SpecificFilter.class.getSimpleName())
                .append(ServerQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(ProviderFilter.class.getSimpleName())
                .append(CustomFilter2.class.getSimpleName())
                .append(CustomFilter1.class.getSimpleName());
    }
}
