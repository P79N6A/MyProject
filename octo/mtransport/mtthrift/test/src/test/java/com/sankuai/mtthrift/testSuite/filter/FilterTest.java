package com.sankuai.mtthrift.testSuite.filter;

import com.meituan.dorado.common.RpcRole;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.dorado.rpc.handler.filter.FilterException;
import com.meituan.dorado.rpc.handler.filter.FilterHandler;
import com.meituan.dorado.rpc.handler.filter.InvokeChainBuilder;
import com.meituan.dorado.rpc.meta.RpcInvocation;
import com.meituan.dorado.rpc.meta.RpcResult;
import com.sankuai.mtthrift.testSuite.idlTest.Tweet;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.mtthrift.testSuite.idlTest.TwitterUnavailable;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/3/6
 */
public class FilterTest {
    private static Logger logger = LoggerFactory.getLogger(FilterTest.class);

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;

    public static StringBuilder invokeChainStr = new StringBuilder();
    public static StringBuilder exceptionInfoStr = new StringBuilder();

    private static StringBuilder expectClientInvokeChainStr = new StringBuilder();
    private static StringBuilder expectAllInvokeChainStr = new StringBuilder();
    private static StringBuilder expectExceptionInfoStr = new StringBuilder();

    private String testStr = "I am Emma";

    @BeforeClass
    public static void start() {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/filter/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/filter/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");

        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new TraceFilter());
        buildExpectInvokeChainStr();
    }

    @Before
    public void beforeTest() {
        ClientQpsLimitFilter.count.set(0);
        ServerQpsLimitFilter.count.set(0);
    }

    @Test
    public void testInvokeChain() throws Throwable {
        invokeChainStr = new StringBuilder();
        FilterHandler handler = new FilterHandler() {
            @Override
            public RpcResult handle(RpcInvocation invokeContext) throws Throwable {
                logger.info("This is actual invoke");
                return new RpcResult(invokeContext.getArguments()[0]);
            }
        };
        FilterHandler handlerChain = InvokeChainBuilder.buildInvokeChain(handler, RpcRole.INVOKER, new ArrayList<Filter>());

        Class<?>[] classes = new Class[1];
        classes[0] = String.class;
        Method method = client.getClass().getMethod("testString", classes);

        Object[] param = new Object[1];
        param[0] = testStr;
        RpcInvocation context = new RpcInvocation(Twitter.class, method, param);
        RpcResult result = handlerChain.handle(context);
        Assert.assertEquals(param[0], result.getReturnVal());
        Assert.assertEquals(expectClientInvokeChainStr.toString(), invokeChainStr.toString());
    }

    @Test
    public void testClientInvoke() throws TException {
        invokeChainStr = new StringBuilder();
        List<String> param = new ArrayList<String>();
        List<String> result = client.testList(param);
        Assert.assertEquals(param, result);
        Assert.assertEquals(expectAllInvokeChainStr.toString(), invokeChainStr.toString());
    }

    @Test
    public void testClientFilterException() throws TException {
        ClientQpsLimitFilter.enable();
        ServerQpsLimitFilter.disable();
        Map<String, String> param = new HashMap<String, String>();
        for (int i = 0; i < 3; i++) {
            Map<String, String> result = client.testMap(param);
            Assert.assertEquals(param, result);
        }

        try {
            String result = client.testString(testStr);
        } catch (Exception e) {
            Assert.assertEquals(FilterException.class, e.getClass());
            Assert.assertEquals("QpsLimited", e.getMessage());
        }
    }

    @Test
    public void testServerFilterException() throws TException {
        ClientQpsLimitFilter.disable();
        ServerQpsLimitFilter.enable();
        byte param = 'a';
        for (int i = 0; i < 5; i++) {
            byte result = client.testByte(param);
            Assert.assertEquals(param, result);
        }

        try {
            String result = client.testString(testStr);
        } catch (Exception e) {
            Assert.assertEquals(TException.class, e.getClass());
            Assert.assertEquals(TApplicationException.class, e.getCause().getClass());
            String exceptionMessage = "com.meituan.dorado.rpc.handler.filter.FilterException:QpsLimited";
            Assert.assertEquals(true, e.getCause().getMessage().contains(exceptionMessage));
        }
    }

    @Test
    public void testException() throws NoSuchMethodException, TException {
        exceptionInfoStr = new StringBuilder();
        try {
            String result = client.testException(new Tweet(1, "a", "b"));
        } catch (TwitterUnavailable twitterUnavailable) {
            Assert.assertEquals(expectExceptionInfoStr.toString(), exceptionInfoStr.toString());
        }
    }

    @AfterClass
    public static void stop() throws InterruptedException {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    private static void buildExpectInvokeChainStr() {
        expectClientInvokeChainStr.append(TraceFilter.class.getSimpleName())
                .append(InvokerFilter.class.getSimpleName())
                .append(ClientQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName());

        expectAllInvokeChainStr.append(TraceFilter.class.getSimpleName())
                .append(InvokerFilter.class.getSimpleName())
                .append(ClientQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(CustomFilter2.class.getSimpleName())
                .append(CustomFilter1.class.getSimpleName())
                .append(TraceFilter.class.getSimpleName())
                .append(ServerQpsLimitFilter.class.getSimpleName())
                .append(AccessLogFilter.class.getSimpleName())
                .append(ProviderFilter.class.getSimpleName())
                .append(CustomFilter2.class.getSimpleName())
                .append(CustomFilter1.class.getSimpleName());

        expectExceptionInfoStr.append("ProviderFilter end have Exception").append("InvokerFilter end have Exception");
    }
}
