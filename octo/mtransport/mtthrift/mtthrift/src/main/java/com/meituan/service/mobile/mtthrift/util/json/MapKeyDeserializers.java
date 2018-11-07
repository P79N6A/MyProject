package com.meituan.service.mobile.mtthrift.util.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;

/**
 * Author: qi.yin
 */
public class MapKeyDeserializers extends SimpleKeyDeserializers {
    private static final KeyDeserializer DEFAULT = new MapKeyDeserializer();

    @Override
    public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
        KeyDeserializer deserializer = super.findKeyDeserializer(type, config, beanDesc);

        return deserializer != null ? deserializer : DEFAULT;
    }
}
