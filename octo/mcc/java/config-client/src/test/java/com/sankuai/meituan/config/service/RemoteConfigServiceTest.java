package com.sankuai.meituan.config.service;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-29
 */
public class RemoteConfigServiceTest {
    //    private RemoteConfigService remoteConfigService = new RemoteConfigService("http://master.config.test.sankuai.info");
//    private RemoteConfigService remoteConfigService = new RemoteConfigService("http://develop.config.test.sankuai.info");
    private RemoteConfigService remoteConfigService = new RemoteConfigService();
//    private RemoteConfigService remoteConfigService = new RemoteConfigService("http://config.sankuai.com");

    @Test
    public void getZkServerListTest() {
        System.out.println(remoteConfigService.getZkServerList());
    }

    @Test
    public void propertiesTest() throws IOException {
        Properties properties = new Properties();
        String prop = "key=v,v2,v3\nkey1=12";
        InputStream inputStream = new ByteArrayInputStream(prop.getBytes());
        properties.load(inputStream);
        // 以,分隔的不会被解析成Array
        // 数字解析出来的类型也是 String
        for (String key : properties.stringPropertyNames()) {
            Object v = properties.get(key);
            System.out.println(properties.get(key));
        }
    }


    @Test
    public void setValueBatchTest() {
        String spaceName = "test";
        String nodeName = "test.test";
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("key", "value");
        data.put("key1", "valu3\nvalue");
        data.put("newKey", "newKey");
        System.out.println(data.toString());

        System.out.println(remoteConfigService.setValue(spaceName, nodeName, data, null));
    }
}