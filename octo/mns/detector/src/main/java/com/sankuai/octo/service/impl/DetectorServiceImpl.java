package com.sankuai.octo.service.impl;

import com.sankuai.octo.detector.actors.http.HttpProviderActorManager;
import com.sankuai.octo.detector.actors.providerActorManager;
import com.sankuai.octo.service.DetectorService;
import org.apache.thrift.TException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-6-24
 * Time: 下午5:57
 */
public class DetectorServiceImpl implements DetectorService.Iface {

    private static final String baseDir = "/mns/sankuai";

    @Override
    public void check(String env, String appkey, String path, List<String> providers, int scanRoundCounter, long timestamp) throws TException {
        if (providers == null || providers.size() <= 0)
            return;
        String zkPathAppkey = baseDir + "/" + env + "/" + appkey;
        //path:provider、providers、provider-http
        String providersDir = zkPathAppkey + "/" + path;
        for (String provider : providers) {
            String providerPath = providersDir + "/" + provider;
            providerActorManager.check(env, appkey, providerPath, providersDir, new AtomicInteger(scanRoundCounter));
        }
    }

    @Override
    public void userDefinedHttpCheck(String env, String appkey, String path, List<String> providers, String checkUrl, int scanRoundCounter, long timestamp) throws TException {
        if (providers == null || providers.size() <= 0)
            return;
        String zkPathAppkey = baseDir + "/" + env + "/" + appkey;
        //path:provider-http
        String providersDir = zkPathAppkey + "/" + path;
        for (String provider : providers) {
            String providerPath = providersDir + "/" + provider;
            HttpProviderActorManager.check(env, appkey, providerPath, providersDir, checkUrl, new AtomicInteger(scanRoundCounter));
        }
    }
}
