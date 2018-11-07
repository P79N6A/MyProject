/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 * 
 */
package com.sankuai.meituan.config.util;

import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author liuxu<liuxu04@meituan.com>
 */
public class PropertiesSerializerTest extends TestCase {

    private Properties properties;
    private PropertiesConfiguration propertiesConfiguration;
    private PropertiesConfiguration propertiesConfigurationWithComment;


    public void setUp() {
        properties = new Properties();
        properties.setProperty("a", "true");
        properties.setProperty("b", "123");
        properties.setProperty("jdbc", "jdbc://123.23.23.23:3232/db_name?");


        propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setDelimiterParsingDisabled(true);
        propertiesConfiguration.setProperty("a", "true");
        propertiesConfiguration.setProperty("b", "123");
        propertiesConfiguration.setProperty("jdbc", "jdbc://123.23.23.23:3232/db_name?");


        propertiesConfigurationWithComment = new PropertiesConfiguration();
        propertiesConfigurationWithComment.setDelimiterParsingDisabled(true);
        propertiesConfigurationWithComment.setProperty("a", "true");
        propertiesConfigurationWithComment.setProperty("b", "123");
        propertiesConfigurationWithComment.setProperty("jdbc", "jdbc://123.23.23.23:3232/db_name?");

        propertiesConfigurationWithComment.getLayout().setComment("jdbc", "comment for jdbc");
        propertiesConfigurationWithComment.getLayout().setComment("b", "comment for b");
        propertiesConfigurationWithComment.getLayout().setComment("a", "comment for a");

        propertiesConfigurationWithComment.getLayout().setComment("notexists", "comment for notexists");

    }

    @Test
    public void testSerialize() {
        Properties properties = this.properties;

        byte[] data = PropertiesSerializer.serialize(properties);

        Properties newProperties = PropertiesSerializer.deSerialize(data);

        for (Map.Entry<Object, Object> item : properties.entrySet()) {
            Assert.assertTrue(StringUtils.equals((String) item.getValue(), newProperties.getProperty((String) item.getKey())));
        }
    }

    @Test
    public void testSerializePropertiesConfiguration() throws ConfigurationException {
        PropertiesConfiguration oldProperties = this.propertiesConfigurationWithComment;

        byte[] data = PropertiesSerializer.serializePropertiesConfiguration(oldProperties);

        PropertiesConfiguration newProperties = PropertiesSerializer.deSerializePropertiesConfiguration(data);

        Iterator<String> iter = oldProperties.getKeys();
        while(iter.hasNext()){
            String key = iter.next();
            Assert.assertTrue(StringUtils.equals(oldProperties.getString(key),newProperties.getString(key)));
        }
    }


    @Test
    public void testConvertSerializePropertiesConfiguration() throws ConfigurationException {
        PropertiesConfiguration properties = this.propertiesConfiguration;

        byte[] data = PropertiesSerializer.serializePropertiesConfiguration(properties);

        Properties newProperties = PropertiesSerializer.deSerialize(data);

        assertTrue(PropertiesSerializer.isEquals(newProperties, properties));

        byte[] data2 = PropertiesSerializer.serialize(newProperties);

        PropertiesConfiguration newProperties2 = PropertiesSerializer.deSerializePropertiesConfiguration(data2);
        assertTrue(PropertiesSerializer.isEquals(newProperties, newProperties2));

    }

}
