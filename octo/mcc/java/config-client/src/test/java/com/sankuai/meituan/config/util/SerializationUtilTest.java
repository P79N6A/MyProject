package com.sankuai.meituan.config.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-22
 */
public class SerializationUtilTest {
    @Test
    public void test() {
        OObject o = new OObject();
        o.setStr("中文\n\r,\t");
        o.setI(12345);
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("key中文", "value中文");
        o.setMap(map);

        String ss = SerializationUtils.toBase64String(o);

        System.out.println(ss);

        OObject o1 = (OObject) SerializationUtils.fromBase64String(ss);
        System.out.println(o1);

        Assert.assertEquals(o, o1);


    }

}
