/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 * 
 */
package com.sankuai.meituan.config.service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.config.util.PropertiesSerializer;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author liuxu<liuxu04@meituan.com>
 * @desc Serialize and Deserialize PropertyValue to byte array which will be persistented in zookeeper
 */


@Service
public class PropertySerializeService {

    private static final Logger LOG = LoggerFactory.getLogger(PropertySerializeService.class);
    /**
     * Without comment
     * @param data
     * @return
     */
    public Map<String, String> deSerializePropertyValueAsMap(byte[] data) {
        List<PropertyValue> values = deSerializePropertyValueAsList(data);
        Map<String, String> nodeData = Maps.newHashMap();
        for(PropertyValue v : values){
            nodeData.put(v.getKey(), v.getValue());
        }
        return nodeData;
    }

    public List<PropertyValue> deSerializePropertyValueAsList(byte[] data) {
        if (ArrayUtils.isEmpty(data)) {
            return Lists.newArrayList();
        }
        PropertiesConfiguration propertiesConfiguration = PropertiesSerializer.deSerializePropertiesConfiguration(data);
        Iterator<String> iterator = propertiesConfiguration.getKeys();
        if(iterator == null){
            return Lists.newArrayList();
        }
        List<PropertyValue> pvs = Lists.newArrayList();
        while(iterator.hasNext()){
            String key = iterator.next();
            PropertyValue v = new PropertyValue();
            v.setKey(key);
            v.setComment(propertiesConfiguration.getLayout().getComment(key));
            Object value = propertiesConfiguration.getProperty(key);
            if(value instanceof List){
                v.setValue(Joiner.on(',').join((List) value));
            }else if(value instanceof String){
                v.setValue((String) value);
            }else{
                // toto
                LOG.error("fail to parse {} to string", value);
            }
            pvs.add(v);
        }
        return pvs;
    }


    public byte[] serializePropertyValue(Collection<? extends PropertyValue> values) {

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setDelimiterParsingDisabled(true);

        for(PropertyValue v : values){

            propertiesConfiguration.setProperty(v.getKey(), v.getValue());
            propertiesConfiguration.getLayout().setComment(v.getKey(), v.getComment());
        }
        return PropertiesSerializer.serializePropertiesConfiguration(propertiesConfiguration);
    }
}
