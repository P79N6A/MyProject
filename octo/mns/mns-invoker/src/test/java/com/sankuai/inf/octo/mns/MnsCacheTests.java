package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.cache.CacheLoader;
import com.sankuai.inf.octo.mns.cache.MnsCache;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.sgagent.thrift.model.SGService;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MnsCacheTests {

    @Test
    public void test() {
        MnsCache cache = new MnsCache<String, String, List<SGService>>(
                new CacheLoader<String, String, List<SGService>>() {
                    @Override
                    public List<SGService> reload(String row, String column) {
                        return null;
                    }
                });
        System.out.println(cache.get("local", "remote"));
        cache.put("local", "remote", Arrays.asList(randService()));
        System.out.println(cache.get("local", "remote"));
        cache.updateAll();
        cache.put("local", "remote", Collections.emptyList());
        System.out.println(cache.get("local", "remote"));
        cache.put("", "test", "test");
        System.out.println(cache.get("", "test"));
        cache.updateAll();
    }

    private SGService randService() {
        SGService sgService = new SGService();
        sgService.setAppkey("test");
        sgService.setIp(ProcessInfoUtil.getLocalIpV4());
        sgService.setPort(111);
        sgService.setLastUpdateTime((int) (System.currentTimeMillis() / 1000));
        sgService.setVersion("original");
        sgService.setWeight(10);
        sgService.setFweight(10.d);
        String extend = "OCTO|slowStartSeconds:180";
        sgService.setExtend(extend);
        return sgService;
    }

    @Test
    public void testUpdateAll() {
        MnsCache cache = new MnsCache<String, String, String>(
                new CacheLoader<String, String, String>() {
                    @Override
                    public String reload(String row, String column) {
                        return null;
                    }
                });
        cache.put("R1", "C1", "V1");
        cache.put("R2", "C2", "V2");
        cache.put("R3", "C3", "V3");
        //        for (String row : (Set<String>)cache.cache.rows()) {
        //            for (String column : (Set<String>)(cache.cache.columns(row))) {
        //                System.out.println(row + "|" + column + "|" + cache.get(row, column));
        //            }
        //        }
    }
}
