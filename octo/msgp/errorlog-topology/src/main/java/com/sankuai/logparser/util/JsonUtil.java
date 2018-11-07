package com.sankuai.logparser.util;

import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: niyong@meituan.com
 * Date: 13-9-25
 * Time: 下午7:31
 */
public final class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private JsonUtil() {
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 对象转换成json字符串
     *
     * @param o 对象
     * @return
     * @author zhaolei
     * @created 2011-5-9
     */
    public static String toJsonString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonGenerationException e) {
            log.error(e.getMessage(), e);
        } catch (JsonMappingException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将json串转换成对象
     *
     * @param <T>
     * @param json
     * @param clazz
     * @return
     * @author lichengwu
     * @created 2011-8-17
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Throwable e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 将json串转换成map
     *
     * @param json
     * @return
     * @author lichengwu
     * @created 2011-8-17
     */
    public static Map<?, ?> toMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Throwable e) {
            log.error("", e);
            return null;
        }
    }

    public static List<?> toList(String json) {
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Throwable e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 将json串转换成数组
     *
     * @param <T>
     * @param json
     * @param array
     * @return
     * @author lichengwu
     * @created 2011-8-17
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(String json, T[] array) {
        try {
            array = (T[]) objectMapper.readValue(json, Object[].class);
            return array;
        } catch (Throwable e) {
            log.error("", e);
            return null;
        }
    }
}
