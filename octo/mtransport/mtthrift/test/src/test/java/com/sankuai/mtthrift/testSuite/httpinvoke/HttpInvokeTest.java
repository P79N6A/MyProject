package com.sankuai.mtthrift.testSuite.httpinvoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.service.mobile.mtthrift.server.http.meta.HttpInvokeParam;
import com.meituan.service.mobile.mtthrift.util.json.JacksonUtils;
import com.sankuai.mtthrift.testSuite.HttpClientUtil;
import com.sankuai.mtthrift.testSuite.generic.Message;
import com.sankuai.mtthrift.testSuite.generic.SubMessage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpInvokeTest {

    private static ClassPathXmlApplicationContext serverBeanFactory;
    private ObjectMapper mapper = new ObjectMapper();
    private String url = "http://localhost:5080/invoke";
    private static String serviceName = "com.sankuai.mtthrift.testSuite.generic.Generic";
    private static HttpInvokeParam httpInvokeParam;

    @BeforeClass
    public static void init() {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/generic/server.xml");
        httpInvokeParam = new HttpInvokeParam("com.sankuai.inf.mtthrift.testClient", "com.sankuai.inf.mtthrift.testServer", serviceName);
    }

    @Test
    public void testEcho1() throws Exception {
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo1", null, null);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals("null", result);
    }

    @Test
    public void testEcho2() throws Exception {
        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = String.class;
        params[0] = "Hello Emma";

        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo2", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        String returnRet = JacksonUtils.deserialize(result, String.class);

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(params[0], returnRet);
    }

    @Test
    public void testEcho3() throws Exception {
        SubMessage subMessage = new SubMessage();
        subMessage.setId(100);
        subMessage.setValue("hello world");

        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = SubMessage.class;
        params[0] = subMessage;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo3", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        SubMessage returnRet = JacksonUtils.deserialize(result, SubMessage.class);

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(subMessage, returnRet);
    }

    @Test
    public void testEcho4() throws Exception {
        List<SubMessage> param = new ArrayList<SubMessage>();
        SubMessage subMessage = new SubMessage();
        subMessage.setId(100);
        subMessage.setValue("hello world");
        param.add(subMessage);
        SubMessage subMessage2 = new SubMessage();
        subMessage2.setId(101);
        subMessage2.setValue("hello kitty");
        param.add(subMessage2);

        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = List.class;
        params[0] = param;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo4", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        List<SubMessage> returnRet = JacksonUtils.deserialize(result, param.getClass());

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(param, returnRet);
    }

    @Test
    public void testEcho5() throws Exception {
        Map<SubMessage, SubMessage> param = new HashMap<SubMessage, SubMessage>();
        SubMessage key = new SubMessage();
        key.setId(100);
        key.setValue("hello world");
        param.put(key, key);

        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = Map.class;
        params[0] = param;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo5", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        Map<SubMessage, SubMessage> returnRet = JacksonUtils.deserialize(result, param.getClass());

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(param, returnRet);

    }

    @Test
    public void testEcho6() throws Exception {
        Message param = new Message();
        param.setId(100);
        param.setValue("hello world");
        List<SubMessage> subMessages = new ArrayList<SubMessage>();
        SubMessage subMessage = new SubMessage();
        subMessage.setId(101);
        subMessage.setValue("hello kitty");
        subMessages.add(subMessage);
        param.setSubMessages(subMessages);

        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = Message.class;
        params[0] = param;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo6", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        Message returnRet = JacksonUtils.deserialize(result, param.getClass());

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(param, returnRet);
    }

    @Test
    public void testEcho7() throws Exception {
        String param1 = "Hello Emma";
        SubMessage param2 = new SubMessage();
        param2.setId(101);
        param2.setValue("hello kitty");

        Class<?>[] paramTypes = new Class<?>[2];
        Object[] params = new Object[2];
        paramTypes[0] = String.class;
        params[0] = param1;
        paramTypes[1] = SubMessage.class;
        params[1] = param2;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo7", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        SubMessage returnRet = JacksonUtils.deserialize(result, param2.getClass());

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(param2, returnRet);
    }

    @Test
    public void testEcho8() throws Exception {
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo8", null, null);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();

        Assert.assertEquals(false, returnMessage.success);
        Assert.assertEquals(true, result.contains("GenericException"));
        Assert.assertEquals(true, result.contains("服务方法调用异常"));
    }

    @Test
    public void testEcho9() throws Exception {
        byte param1 = 'a';
        int param2 = 1000;
        long param3 = Long.MAX_VALUE;
        double param4 = Double.MAX_VALUE;

        Class<?>[] paramTypes = new Class<?>[4];
        Object[] params = new Object[4];
        paramTypes[0] = byte.class;
        params[0] = param1;
        paramTypes[1] = int.class;
        params[1] = param2;
        paramTypes[2] = long.class;
        params[2] = param3;
        paramTypes[3] = double.class;
        params[3] = param4;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo9", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        byte returnRet = JacksonUtils.deserialize(result, byte.class);

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(param1, returnRet);
    }

    @Test
    public void testLongParam() throws Exception {
        List<SubMessage> param = new ArrayList<SubMessage>();
        for (int i = 0; i < 100; i++) {
            SubMessage subMessage = new SubMessage();
            subMessage.setId(i);
            subMessage.setValue("hello world" + i);
            param.add(subMessage);
        }

        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = List.class;
        params[0] = param;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo4", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();
        List<SubMessage> returnRet = JacksonUtils.deserialize(result, param.getClass());

        Assert.assertEquals(true, returnMessage.success);
        Assert.assertEquals(param, returnRet);
    }

    @Test
    public void testNotExistMethod() throws Exception {
        SubMessage param = new SubMessage();
        param.setId(101);
        param.setValue("hello kitty");

        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = SubMessage.class;
        params[0] = param;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();

        Assert.assertEquals(false, returnMessage.success);
        Assert.assertEquals(true, result.contains("NoSuchMethodException"));
    }

    @Test
    public void testParamTypeError() throws Exception {
        SubMessage param = new SubMessage();
        param.setId(101);
        param.setValue("hello kitty");

        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = String.class;
        params[0] = param;
        String paramJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, "echo", paramTypes, params);
        ReturnMessage returnMessage = getResult(HttpClientUtil.doPost(url, paramJson));
        String result = returnMessage.getResult();

        Assert.assertEquals(false, returnMessage.success);
        Assert.assertEquals(true, result.contains("IllegalArgumentException"));
    }

    @Test
    public void testMultiThread() {
        final int threadNum = 10;
        final int loopNum = 1000;

        final AtomicInteger failCount = new AtomicInteger(0);
        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < loopNum; j++) {
                        try {
                            testLongParam();
                        } catch (Throwable e) {
                            failCount.incrementAndGet();
                        }
                    }
                }
            });
            threads[i] = thread;
            thread.start();
        }
        for (int i = 0; i < threadNum; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                failCount.incrementAndGet();
            }
        }
        Assert.assertEquals(0, failCount.get());
    }

    @AfterClass
    public static void destroy() throws InterruptedException {
        serverBeanFactory.destroy();
    }

    private ReturnMessage getResult(String message) throws IOException {
        ReturnMessage returnMessage = mapper.readValue(message, ReturnMessage.class);
        return returnMessage;
    }

    static class ReturnMessage {
        private Boolean success;
        private String result;

        public ReturnMessage() {
        }

        public ReturnMessage(Boolean success, String result) {
            this.success = success;
            this.result = result;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
