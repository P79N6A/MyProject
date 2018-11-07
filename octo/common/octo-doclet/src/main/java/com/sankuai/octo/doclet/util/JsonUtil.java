package com.sankuai.octo.doclet.util;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    private JsonUtil() {
    }

    public static String toJson(Object data, Class<?> clazz, String... properties) {
        return Writer().object(data).filter(clazz, properties).build();
    }

    public static String toJson(Object data) {
        return Writer().object(data).build();
    }

    public static Writer Writer() {
        return new Writer();
    }

    public static class Writer {
        private Object object;
        private List<SimplePropertyPreFilter> filters = new ArrayList<SimplePropertyPreFilter>();

        /**
         * 序列化目标对象
         *
         * @param object
         * @return
         */
        public Writer object(Object object) {
            this.object = object;
            return this;
        }

        /**
         * 针对特定类型的属性进行过滤
         *
         * @param clazz
         * @param properties
         * @return
         */
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
                serializer.config(SerializerFeature.DisableCircularReferenceDetect, true);
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
