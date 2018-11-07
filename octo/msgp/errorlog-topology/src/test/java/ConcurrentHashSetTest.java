import com.sankuai.logparser.util.ConcurrentHashSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/31
 */
public class ConcurrentHashSetTest {

    private String blackListStr = "mobile.abtest,com.sankuai.wpt.groupapi.groupapi.pirlo,mtgis,mobile-hotel,gm-bonus-exposure-mapi-web,com.sankuai.mobile.recsys.recommend.homepage,com.sankuai.recsys.recsys.theme,com.sankuai.inf.mtsi,com.sankuai.ia.delay.queue,com.sankuai.pic,mtpoiop,baymax-bidding-service,com.sankuai.waimai.c.cbasememory,com.sankuai.train.train.agent,com.meituan.pic.imageproc.start,com.sankuai.waimai.d.cpcdc";
    @Test
    public void test() {
        Set set1 = new ConcurrentHashSet();
        set1.addAll(Arrays.asList(blackListStr.replaceAll("\\s+", "").split(",")));

        HashSet set2 = new HashSet();
        set2.addAll(Arrays.asList(blackListStr.replaceAll("\\s+", "").split(",")));

        Assert.assertEquals(set1.size(), set2.size());
    }
}
