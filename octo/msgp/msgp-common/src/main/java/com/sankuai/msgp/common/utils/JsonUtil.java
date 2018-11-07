package com.sankuai.msgp.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yves on 2018/1/9.
 */
public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    private JsonUtil() {
    }

    /**
     * 对象转换成json字符串
     */
    public static String toString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static byte[] toByte(Object o) {
        try {
            return objectMapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public static JsonNode parse(String json) {
        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return rootNode;
    }

    /**
     * 将json串转换成对象
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            LOGGER.error("json转换失败" + json, e);
            return null;
        }
    }


    /**
     * 将json串转换成对象
     */
    public static <T> T toObject(String json, JavaType javaType) {
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            LOGGER.error("json转换失败" + json, e);
            return null;
        }
    }


    /**
     * 将json串转换成map
     */
    public static Map<?, ?> toMap(String json) {
        Map<?, ?> map = new HashMap();
        try {
            map = objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            LOGGER.error("json转换失败" + json, e);
        }
        return map;
    }

    /**
     * 将json串转换成key为String类型的map
     */
    public static Map<String, Object> toStringMap(String json) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            LOGGER.error("json转换失败" + json, e);
        }
        return map;
    }

    public static List<?> toList(String json) {
        List data = null;
        try {
            data = objectMapper.readValue(json, List.class);
        } catch (IOException e) {
            LOGGER.error("json转换失败" + json, e);
            data = null;
        }
        return data;
    }

    /**
     * 将json串转换成数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(String json, T[] array) {
        try {
            array = (T[]) objectMapper.readValue(json, Object[].class);
        } catch (IOException e) {
            LOGGER.error("json转换失败" + json, e);
            array = null;
        }
        return array;
    }

    public static List converToList(String data, Class<?> elementClasses) throws IOException{
        JavaType type = getCollectionType(ArrayList.class,elementClasses);
        return objectMapper.readValue(data,type);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}
