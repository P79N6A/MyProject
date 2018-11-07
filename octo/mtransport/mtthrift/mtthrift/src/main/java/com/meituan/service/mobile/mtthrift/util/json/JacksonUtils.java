package com.meituan.service.mobile.mtthrift.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.meituan.service.mobile.mtthrift.server.http.meta.HttpInvokeParam;
import com.meituan.service.mobile.mtthrift.server.http.meta.MethodParameter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

public class JacksonUtils {
    public static final String CLASS_KEY = "@class";
    public static final String BASE_VALUE_KEY = "\"value\"";
    public static final String BASE_VALUE_NODE_KEY = "value";
    private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final ObjectMapper simpleMapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        mapper.enableDefaultTypingAsProperty(NON_FINAL, CLASS_KEY);
        module.setKeyDeserializers(new MapKeyDeserializers());
        module.addKeyDeserializer(Object.class, new MapKeyDeserializer());
        module.addKeySerializer(Object.class, new MapKeySerializer());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(module);

        simpleMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static String serialize(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        return "";
    }

    public static <T> T deserialize(String jsonString, Class<T> clazz) {
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        return null;
    }

    public static String simpleSerialize(Object obj) {
        try {
            return simpleMapper.writeValueAsString(obj);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        return "";
    }

    public static <T> T simpleDeserialize(String jsonString, Class<T> clazz) {
        try {
            return simpleMapper.readValue(jsonString, clazz);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        return null;
    }

    public static JsonNode readNode(String jsonString) {
        try {
            return mapper.readTree(jsonString);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        return null;
    }


    /**
     * 根据传入的服务信息生成http请求的json参数
     * 自己写httpClient 使用这个方法
     *
     * @param httpInvokeParam
     * @param methodName
     * @param paramTypes
     * @param params
     * @return
     * @throws JsonProcessingException
     */
    public static String genHttpInvokeParam(HttpInvokeParam httpInvokeParam, String methodName, Class<?>[] paramTypes, Object[] params) throws JsonProcessingException {
        if (httpInvokeParam == null) {
            throw new IllegalArgumentException("httpInvokeParam has not been initialized");
        }
        if (StringUtils.isBlank(httpInvokeParam.getServiceName()) || StringUtils.isBlank(methodName)) {
            throw new IllegalArgumentException("serviceName and methodName cannot be empty");
        }
        List<MethodParameter> methodParameters;
        if (paramTypes == null && params == null) {
            methodParameters = null;
        } else if (paramTypes != null && params != null && paramTypes.length == params.length) {
            methodParameters = new ArrayList<MethodParameter>();
            for (int i = 0; i < paramTypes.length; i++) {
                String paramTypeStr = paramTypes[i].getName();
                Object arg = params[i];
                MethodParameter parameter = new MethodParameter(paramTypeStr, serialize(arg));
                methodParameters.add(parameter);
            }
        } else {
            throw new IllegalArgumentException("method paramTypes and params not match");
        }
        httpInvokeParam.setMethodName(methodName);
        httpInvokeParam.setParameters(methodParameters);

        String mapperJson = simpleMapper.writeValueAsString(httpInvokeParam);
        return mapperJson;
    }
}