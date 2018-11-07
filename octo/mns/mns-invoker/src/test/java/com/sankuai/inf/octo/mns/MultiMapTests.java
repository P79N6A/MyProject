package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.cache.MultiMap;
import com.sankuai.sgagent.thrift.model.SGService;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MultiMapTests {
    @Test
    public void testMap() {
        MultiMap map = new MultiMap<String, String, SGService>();
        Assert.assertTrue(map.rows().isEmpty());
        Assert.assertTrue(map.columns(null).isEmpty());
        Assert.assertTrue(map.columns("test").isEmpty());
        Assert.assertNull(map.get(null));
        Assert.assertNull(map.get("row"));
        Assert.assertNull(map.get(null,null));
        Assert.assertNull(map.get("row",null));
        Assert.assertNull(map.get(null,"column"));
        Assert.assertNull(map.getOrCreate(null));
        Assert.assertTrue(map.getOrCreate("row").isEmpty());
        Assert.assertFalse(map.rows().isEmpty());

        Assert.assertTrue(map.columns(null).isEmpty());
        Assert.assertTrue(map.columns("test").isEmpty());

        map.put(null, null, null);
        Assert.assertNull(map.get(null, null));

        map.put("row", null, null);
        Assert.assertNull(map.get("row", null));

        map.put(null, "column", null);
        Assert.assertNull(map.get(null, "column"));
        map.put("row", "column", null);
        Assert.assertNull(map.get("row", "column"));
        map.put("row", "column", "value");
        Assert.assertEquals("value",map.get("row", "column"));

        map.put("row", "column", null);
        Assert.assertEquals("value", map.get("row", "column"));
        Assert.assertFalse(map.rows().isEmpty());
        Assert.assertTrue(map.columns(null).isEmpty());
        Assert.assertTrue(map.columns("test").isEmpty());

        Map<String, String> hashMap = new HashMap<String, String>();
        String value = hashMap.get(null);
        Assert.assertNull(value);
    }
}
