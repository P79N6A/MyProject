package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.generic.GenericService;
import com.meituan.service.mobile.mtthrift.util.json.JacksonUtils;
import com.sankuai.mtthrift.testSuite.generic.Message;
import com.sankuai.mtthrift.testSuite.generic.SubMessage;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericServiceTest {
    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;

    private static GenericService client;

    @BeforeClass
    public static void init() {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/generic/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/generic/client.xml");
        client = clientBeanFactory.getBean(GenericService.class);
    }

    @AfterClass
    public static void destroy() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    private static void testEcho1(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();

        List<String> paramValues = new ArrayList<String>();

        String result = genericService.$invoke("echo1", paramTypes, paramValues);
        System.out.println(result);
    }

    private static void testEcho2(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();
        paramTypes.add("java.lang.String");

        List<String> paramValues = new ArrayList<String>();
        paramValues.add("\"hello world\"");

        String result = genericService.$invoke("echo2", paramTypes, paramValues);
        System.out.println(result);
    }

    private static void testEcho3(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();
        paramTypes.add("com.sankuai.mtthrift.testSuite.generic.SubMessage");

        List<String> paramValues = new ArrayList<String>();
        SubMessage subMessage = new SubMessage();
        subMessage.setId(1);
        subMessage.setValue("hello world");
        String expected = JacksonUtils.serialize(subMessage);
        paramValues.add(expected);

        String result = genericService.$invoke("echo3", paramTypes, paramValues);
        System.out.println(result);
        Assert.assertEquals(expected, result);
    }

    private static void testEcho4(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();
        paramTypes.add("java.util.List");
        List<SubMessage> subMessages = new ArrayList<SubMessage>();
        SubMessage subMessage = new SubMessage();
        subMessage.setId(2);
        subMessage.setValue("hello world");
        subMessages.add(subMessage);
        List<String> paramValues = new ArrayList<String>();
        String expected = JacksonUtils.serialize(subMessages);
        paramValues.add(expected);

        String result = genericService.$invoke("echo4", paramTypes, paramValues);
        System.out.println(result);
        Assert.assertEquals(expected, result);
    }

    private static void testEcho5(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();
        paramTypes.add("java.util.Map");
        Map<SubMessage, SubMessage> maps = new HashMap<SubMessage, SubMessage>();
        SubMessage key = new SubMessage();
        key.setId(1);
        key.setValue("hello world");
        maps.put(key, key);

        List<String> paramValues = new ArrayList<String>();
        String expected = JacksonUtils.serialize(maps);
        paramValues.add(expected);

        String result = genericService.$invoke("echo5", paramTypes, paramValues);
        System.out.println(result);
        Assert.assertEquals(expected, result);
    }

    private static void testEcho6(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();
        paramTypes.add("com.sankuai.mtthrift.testSuite.generic.Message");
        Message message =new Message();
        message.setId(1);
        message.setValue("hello world");
        List<SubMessage> subMessages=new ArrayList<SubMessage>();
        SubMessage subMessage=new SubMessage();
        subMessage.setId(1);
        subMessage.setValue("hello world");
        subMessages.add(subMessage);
        message.setSubMessages(subMessages);
        List<String> paramValues = new ArrayList<String>();
        String expected = JacksonUtils.serialize(message);
        paramValues.add(expected);

        String result = genericService.$invoke("echo6", paramTypes, paramValues);
        System.out.println(result);
        Assert.assertEquals(expected, result);
    }

    private static void testEcho7(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();
        paramTypes.add("java.lang.String");
        paramTypes.add("com.sankuai.mtthrift.testSuite.generic.SubMessage");

        List<String> paramValues = new ArrayList<String>();
        paramValues.add("\"hello world\"");
        SubMessage subMessage = new SubMessage();
        subMessage.setId(1);
        subMessage.setValue("hello world");
        String expected = JacksonUtils.serialize(subMessage);
        paramValues.add(expected);

        String result = genericService.$invoke("echo7", paramTypes, paramValues);
        System.out.println(result);
        Assert.assertEquals(expected, result);
    }

    private static void testEcho8(final GenericService genericService) throws TException {
        List<String> paramTypes = new ArrayList<String>();

        List<String> paramValues = new ArrayList<String>();

        try {
            String result = genericService.$invoke("echo8", paramTypes, paramValues);
            Assert.fail("should not be here");
        } catch (TException e) {
            Assert.assertTrue(e.getCause() instanceof TApplicationException);
            Assert.assertTrue(e.getCause().getMessage().equals("com.sankuai.mtthrift.testSuite.generic.GenericException:generic error"));
        }
    }

    @Test
    public void testException() {
        try {
            client.$invoke("testA", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
            Assert.fail("should not be here");
        } catch (TException e) {
            Assert.assertTrue(e.getCause() instanceof TApplicationException);
            Assert.assertTrue(e.getCause().getMessage().contains("generic service failed，can not find method with signature:")
                    && e.getCause().getMessage().contains("testA()"));
        }

        try {
            client.$invoke("testB", Collections.singletonList("java.lang.String"), Collections.singletonList(JacksonUtils.serialize("hello")));
            Assert.fail("should not be here");
        } catch (TException e) {
            Assert.assertTrue(e.getCause() instanceof TApplicationException);
            Assert.assertTrue(e.getCause().getMessage().contains("generic service failed，can not find method with signature:")
                    && e.getCause().getMessage().contains("testB(java.lang.String)"));
        }

        try {
            client.$invoke("testC", Arrays.asList("java.lang.String", "int"), Arrays.asList(JacksonUtils.serialize("hello"), JacksonUtils.serialize(234)));
            Assert.fail("should not be here");
        } catch (TException e) {
            Assert.assertTrue(e.getCause() instanceof TApplicationException);
            Assert.assertTrue(e.getCause().getMessage().contains("generic service failed，can not find method with signature:")
                    && e.getCause().getMessage().contains("testC(java.lang.String,int)"));
        }
    }

    @Test
    public void testAll() throws TException {
        testEcho1(client);
        testEcho2(client);
        testEcho3(client);
        testEcho4(client);
        testEcho5(client);
        testEcho6(client);
        testEcho7(client);
        testEcho8(client);
        testException();
    }
}
