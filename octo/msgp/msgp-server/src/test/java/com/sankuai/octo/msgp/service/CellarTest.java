package com.sankuai.octo.msgp.service;

import com.alibaba.fastjson.JSON;
import com.sankuai.inf.utils.cache.CellarCache;
import com.sankuai.inf.utils.cache.CellarCacheFactory;
import com.sankuai.inf.utils.common.PagingList;
import com.taobao.tair3.client.error.TairException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by songjianjian on 2018/7/12.
 */
public class CellarTest {
    @Test
    public void test() throws TairException, IOException, InterruptedException {
        String localAppKey = "com.sankuai.inf.hulk.bannerapi";
        String remoteAppkey = "com.sankuai.tair.qa.function";
        int area = 125;
        CellarCache cache = CellarCacheFactory.newCache(localAppKey, remoteAppkey, area);

        //普通存储测试ok
        List<String> list = new ArrayList<>();
        list.add("songjianjian");
        list.add("弹性测试");
        cache.putValue("cellar.octo.test01", PagingList.create(list));
        PagingList result = cache.getValue("cellar.octo.test01", PagingList.class);
        System.out.println(result);

        //加锁测试
        String key = "cellar.octo.test01";

        boolean a = cache.lock(key);
        assertTrue(a);

        a = cache.lock(key);
        assertFalse(a);

        Thread.sleep(5000);
        a = cache.unlock(key);
        assertTrue(a);

        a = cache.lock(key);
        assertTrue(a);

        cache.put("cellar.hulk.octo.manual.com.sankuai.inf.hulk.robotserverbj", "empty");
        System.out.println("DDDD" + cache.get("cellar.hulk.octo.manual.com.sankuai.inf.hulk.robotserverbj"));
    }
}
