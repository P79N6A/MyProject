package com.sankuai.inf.octo.mns.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by lhmily on 03/25/2017.
 */
public class VersionUtilTests {
    private Logger LOG = LoggerFactory.getLogger(VersionUtilTests.class);

    @Test
    public void testVersion() throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; ++i) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String version = VersionUtil.getVersion();
                    LOG.info(version);
                    Assert.assertFalse(CommonUtil.isBlankString(version));
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
    }
}
