package com.sankuai.mtthrift.testSuite.httpinvoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.service.mobile.mtthrift.server.http.meta.HttpInvokeParam;
import com.meituan.service.mobile.mtthrift.server.http.meta.MethodParameter;
import com.meituan.service.mobile.mtthrift.util.json.JacksonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class HttpInvokeParamMapperTest {

    private ObjectMapper mapper = new ObjectMapper();
    private String serviceName = "com.sankuai.mtthrift.TestService";
    private String methodName = "testFunction";

    @Test
    public void test() throws IOException {
        Class<?>[] paramTypes = new Class<?>[1];
        Object[] params = new Object[1];
        paramTypes[0] = String.class;
        params[0] = "haha";
        HttpInvokeParam httpInvokeParam = new HttpInvokeParam("com.sankuai.inf.mtthrift.testClient", "com.sankuai.inf.mtthrift.testServer", serviceName);

        String mapperJson = JacksonUtils.genHttpInvokeParam(httpInvokeParam, methodName, paramTypes, params);

//        String expectedStr = "{\"serviceName\":\"com.sankuai.mtthrift.TestService\",\"methodName\":\"testFunction\",\"parameters\":[{\"type\":\"java.lang.String\",\"paramStr\":\"\\\"haha\\\"\"}],\"test\":false}";
        String expectedStr = "{\"clientAppkey\":\"com.sankuai.inf.mtthrift.testClient\",\"serverAppkey\":\"com.sankuai.inf.mtthrift.testServer\",\"serviceName\":\"com.sankuai.mtthrift.TestService\",\"methodName\":\"testFunction\",\"parameters\":[{\"typeStr\":\"java.lang.String\",\"argStr\":\"\\\"haha\\\"\"}],\"test\":true}";
        Assert.assertEquals(expectedStr, mapperJson);
        HttpInvokeParam invokeParam = mapper.readValue(mapperJson, HttpInvokeParam.class);

        Assert.assertEquals(serviceName, invokeParam.getServiceName());
        Assert.assertEquals(methodName, invokeParam.getMethodName());
        Assert.assertEquals(true, invokeParam.isTest());

        List<MethodParameter> methodParameters = invokeParam.getParameters();
        Assert.assertEquals(1, methodParameters.size());
        for (int i = 0; i < methodParameters.size(); i++) {
            MethodParameter parameter = methodParameters.get(i);
            Assert.assertEquals("java.lang.String", parameter.getTypeStr());
            Assert.assertEquals(params[0], JacksonUtils.deserialize(parameter.getArgStr(), String.class));
        }
    }
}
