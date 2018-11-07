package com.sankuai.meituan.config.util;

import com.sankuai.meituan.config.exception.MtConfigException;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;

/**
 * 注解支持相关工具类
 * <p/>
 * Created by oulong on 14-5-21.
 */
public class AnnotationUtil {

    /**
     * 支持6种基本类型
     */
    public static Object transferValueType(Field field, String value) throws MtConfigException {
        Object result;
        String fieldName = field.getDeclaringClass().getName() + "." + field.getName();
        String typeName = field.getType().getSimpleName();
        try {
            if (StringUtils.equals(typeName, "String")) {
                result = value;
            } else if (StringUtils.equals(typeName, "int") || StringUtils.equals(typeName, "Integer")) {
                result = Integer.valueOf(value);
            } else if (StringUtils.equals(typeName, "long") || StringUtils.equals(typeName, "Long")) {
                result = Long.valueOf(value);
            } else if (StringUtils.equals(typeName, "float") || StringUtils.equals(typeName, "Float")) {
                result = Float.valueOf(value);
            } else if (StringUtils.equals(typeName, "double") || StringUtils.equals(typeName, "Double")) {
                result = Double.valueOf(value);
            } else if (StringUtils.equals(typeName, "boolean") || StringUtils.equals(typeName, "Boolean")) {
                result = Boolean.valueOf(value);
            } else {
                throw new MtConfigException("UNEXPECTED FIELD (" + fieldName  + ") TYPE : TYPE IS " + typeName);
            }
        } catch (Exception e) {
            throw new MtConfigException("transfer value for " + fieldName + " failed", e);
        }
        return result;
    }
}
