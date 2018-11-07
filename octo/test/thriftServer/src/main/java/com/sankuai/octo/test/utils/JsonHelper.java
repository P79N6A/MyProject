package com.sankuai.octo.test.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

import java.util.ArrayList;
import java.util.List;

public class JsonHelper {

    private JsonHelper() {
    }

    public static String write(Object data, Class<?> clazz, String... properties) {
        return Writer().object(data).filter(clazz, properties).build();
    }

    public static String write(Object data) {
        return Writer().object(data).build();
    }

    public static <T> T read(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    public static <T> T read(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }

    public static <T> T read(String text, TypeReference<T> typeReference) {
        return JSON.parseObject(text, typeReference);
    }

    public static Writer Writer() {
        return new Writer();
    }

    public static class Writer {
        private Object object;
        private List<SimplePropertyPreFilter> filters = new ArrayList<SimplePropertyPreFilter>();

        public Writer object(Object object) {
            this.object = object;
            return this;
        }

        public Writer filter(Class<?> clazz, String... properties) {
            SimplePropertyPreFilter filter = new SimplePropertyPreFilter(clazz, properties);
            filters.add(filter);
            return this;
        }

        public String build() {
            SerializeWriter out = new SerializeWriter();
            try {
                JSONSerializer serializer = new JSONSerializer(out);
                serializer.config(SerializerFeature.WriteDateUseDateFormat, true);
                for (SimplePropertyPreFilter filter : filters) {
                    serializer.getPropertyPreFilters().add(filter);
                }
                serializer.write(object);
                return out.toString();
            } finally {
                out.close();
            }
        }
    }
}