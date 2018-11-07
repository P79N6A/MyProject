package com.codahale.metrics;

import com.sankuai.octo.statistic.metrics.TimeBucketUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zava on 15/9/24.
 */
public class TimeBucketUtilTest {


    @Test
    public void testCompress(){
        Assert.assertEquals(0, TimeBucketUtil.compress(0));
        Assert.assertEquals(1,TimeBucketUtil.compress(1));
        Assert.assertEquals(2,TimeBucketUtil.compress(2));
        Assert.assertEquals(3,TimeBucketUtil.compress(3));
        Assert.assertEquals(1999,TimeBucketUtil.compress(1999));

        Assert.assertEquals(2000,TimeBucketUtil.compress(2000));
        Assert.assertEquals(2000,TimeBucketUtil.compress(2001));
        Assert.assertEquals(2000,TimeBucketUtil.compress(2005));
        Assert.assertEquals(2000,TimeBucketUtil.compress(2006));
        Assert.assertEquals(2010,TimeBucketUtil.compress(2010));

        Assert.assertEquals(49990,TimeBucketUtil.compress(49990));
        Assert.assertEquals(49990,TimeBucketUtil.compress(49991));



        Assert.assertEquals(50000,TimeBucketUtil.compress(50000));
        Assert.assertEquals(50000,TimeBucketUtil.compress(50001));
        Assert.assertEquals(70000,TimeBucketUtil.compress(70000));
        Assert.assertEquals(70000,TimeBucketUtil.compress(70001));
        Assert.assertEquals(70000,TimeBucketUtil.compress(70101));
    }

}
