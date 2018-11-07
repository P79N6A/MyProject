/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 * 
 */
package com.sankuai.meituan.config.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sankuai.meituan.config.model.PropertyValue;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.SimpleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

/**
 * @author liuxu<liuxu04@meituan.com>
 * @desc Map jsonStr to PropertyValue and also do re-mapping things
 */
@Service
public class PropertyValueJsonMapService {

    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperService.class);
    private ObjectMapper jsonMapper = new ObjectMapper();

    @PostConstruct
    private void postConstruct() {
        jsonMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    public List<PropertyValue> mapJsonStr2PropertyValueList(String jsonStr) {
            if (Strings.isNullOrEmpty(jsonStr)) {
                return null;
            }
            List<PropertyValue> nodeData = null;
            try {
                nodeData = jsonMapper.readValue(jsonStr, CollectionType.construct(List.class, SimpleType.construct(PropertyValue.class)));
            } catch (IOException e) {
                LOG.error("Fail to parse Object from zookeeper data with {}", e);
            }
            return nodeData;
        }

    public String mapPropertyValueList2JsonStr(List<PropertyValue> datas) {
            String str = null;
            try {
                str = jsonMapper.writeValueAsString(datas);
            } catch (IOException e) {
                LOG.error("fail to serialize ConfigNode to zookeeper node with exception:{}", e);
            }
            return str;
        }
}
