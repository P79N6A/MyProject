package com.sankuai.meituan.config.util;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lhmily on 11/24/2016.
 */
public class ZkTest {
    private static final Logger LOG =  LoggerFactory.getLogger(ZkTest.class);

    CuratorFramework client = null;

    @Before
    public void init() {
        client = CuratorFrameworkFactory.builder()
                .connectString(CommonConfigTest.getZkUrl())
                .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE)).build();
        client.start();
    }

    @Test(expected = KeeperException.BadVersionException.class)
    public void testSetDataWithVersion() throws Exception{
            client.setData().withVersion(0).forPath("/config/com.sankuai.octo.tmy/test", new byte[0]);
    }
}
