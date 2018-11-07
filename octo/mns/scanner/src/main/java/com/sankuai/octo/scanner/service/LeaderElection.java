package com.sankuai.octo.scanner.service;


import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import com.sankuai.octo.scanner.Common;
import com.sankuai.octo.scanner.util.ScanUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-6-16
 * Time: 下午10:12
 */
public class LeaderElection {

    private static final Logger logger = LoggerFactory.getLogger(LeaderElection.class);
    private static final String PATH = "/scanner/";
    //zk连接地址
    private static final String offlineAddress = "10.4.245.244:2181,10.4.245.245:2181,10.4.245.246:2181";
    private static final String onlineAddress = "dx-inf-mns-zk04:2181,dx-inf-mns-zk05:2181,dx-inf-mns-zk06:2181";
    private static CuratorFramework client = null;
    private static LeaderLatch example = null;

    public static volatile boolean isMaster = false;

    public static void init() {
        try {
            //注意超时时间设置（10000：连接超时时间，10000：Session超时时间）
            String connectString = Common.isOnline ? onlineAddress : offlineAddress;
            String path = "leader";
            if (Common.isOnline) {
                if (ScanUtils.hostIpPrefix.startsWith("10.32")) {
                    path = PATH + "onlineDX";
                } else if (ScanUtils.hostIpPrefix.startsWith("10.4")) {
                    path = PATH + "onlineYF";
                } else if (ScanUtils.hostIpPrefix.startsWith("10.12")) {
                    path = PATH + "onlineCQ";
                } else if (ScanUtils.hostIpPrefix.startsWith("10.69")) {
                    path = PATH + "onlineGQ";
                }
            } else {
                path = PATH + "offline";
            }
            client = createWithOptions(connectString, new ExponentialBackoffRetry(1000, 3), 10000, 10000);
            example = new LeaderLatch(client, path, ProcessInfoUtil.getLocalIpV4());
            client.start();
            example.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    isMaster = true;
                    logger.warn("I am leader, id:" + example.getId());
                }

                @Override
                public void notLeader() {
                    isMaster = false;
                    logger.warn("I am not leader, id:" + example.getId());
                }
            });
            example.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder().connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

}
