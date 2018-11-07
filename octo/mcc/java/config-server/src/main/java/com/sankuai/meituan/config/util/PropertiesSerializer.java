/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 * 
 */
package com.sankuai.meituan.config.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author liuxu<liuxu04@meituan.com>
 */
public class PropertiesSerializer {
    private final static Logger LOG = LoggerFactory.getLogger(PropertiesSerializer.class);

    public static PropertiesConfiguration deSerializePropertiesConfiguration(byte[] data){
        PropertiesConfiguration p = new PropertiesConfiguration();
        p.setDelimiterParsingDisabled(true);
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            p.load(inputStream, "utf8");
            return p;
        } catch (ConfigurationException | IOException e) {
            LOG.error("反序列化失败", e);
        }
        return null;
    }

    public static byte[] serializePropertiesConfiguration(PropertiesConfiguration properties){
        try( ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            properties.save(outputStream, "utf8");
            return outputStream.toByteArray();
        } catch (ConfigurationException | IOException e) {
            LOG.error("序列化失败", e);
        }
        return null;
    }

    public static Properties deSerialize(byte[] data){
        Properties p = new Properties();
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            p.load(inputStream);
            return p;
        } catch (IOException e) {
            LOG.error("反序列化失败", e);
        }
        return null;
    }

    public static byte[] serialize(Properties properties){
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            properties.store(outputStream, null);
            byte[] ret = outputStream.toByteArray();
            return ret;
        } catch (IOException e) {
            LOG.error("序列化失败", e);
        }
        return null;
    }

    public static boolean isEquals(Properties properties, PropertiesConfiguration propertiesConfiguration){
        for(Map.Entry<Object, Object> entry : properties.entrySet()){

            boolean eq = Objects.equals(entry.getValue(), propertiesConfiguration.getProperty((String) entry.getKey()));
            if(!eq){
                return false;
            }
        }
        Iterator<String> ite = propertiesConfiguration.getKeys();
        while (ite.hasNext()){
            String key = ite.next();
            boolean eq = Objects.equals(properties.getProperty(key), propertiesConfiguration.getProperty(key));
            if(!eq){
                return false;
            }
        }
        return true;
    }
}
