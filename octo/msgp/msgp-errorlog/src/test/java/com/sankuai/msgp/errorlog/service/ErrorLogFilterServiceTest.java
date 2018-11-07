package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.errorlog.ApplicationTest;
import com.sankuai.msgp.errorlog.domain.ErrorLogParsedFilter;
import net.sf.ehcache.Ehcache;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

@RunWith(SpringJUnit4ClassRunner.class)

public class ErrorLogFilterServiceTest extends ApplicationTest {
    @Autowired
    private ErrorLogFilterService errorLogFilterService;
    @Resource
    private CacheManager cacheManager;

    @Test
    public void testSelectEnableCache() throws Exception {
        for (int i = 0; i < 10000; i++) {
            String appkey = "com.sankuai.inf.msgp";
            List<ErrorLogParsedFilter> data = errorLogFilterService.getParsedFiltersByAppkey(appkey);
            Thread.sleep(200);
            System.out.println(data.size());
            System.out.println(((Ehcache)cacheManager.getCache("LRUCache-1m").getNativeCache()).getKeys());

            appkey = "com.sankuai.inf.octo.errorlog.mock";
            data = errorLogFilterService.getParsedFiltersByAppkey(appkey);
            System.out.println(data.size());
        }
        Thread.sleep(50000);
    }

    @Test
    public void filterMatch() {
        String rule = "ErrorLog";
        Pattern pattern = Pattern.compile(rule);
        String str = "hohoho ErrorLog hihihi";
        Assert.assertEquals(errorLogFilterService.match(str, pattern), true);
    }

    @Test
    public void testSubString() throws Exception {
        String test = "我爱中国人";
        String str = ErrorLogFilterService.subStrByByte(test,3);
        Assert.assertEquals("我", str);
    }
}
