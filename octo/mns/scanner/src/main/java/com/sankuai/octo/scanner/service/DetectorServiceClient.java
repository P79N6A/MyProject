package com.sankuai.octo.scanner.service;

import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.sankuai.octo.service.DetectorService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-6-12
 * Time: 下午2:27
 */
public class DetectorServiceClient implements FactoryBean<Object>, ApplicationContextAware, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(DetectorServiceClient.class);

    private Object clientProxy;
    private static DetectorService.AsyncIface client;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    }

    @Override
    public Object getObject() throws Exception {
        return this;
    }

    @Override
    public Class<?> getObjectType() {
        return this.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initInstance();
    }

    public Object getClientProxy() {
        return clientProxy;
    }

    public void setClientProxy(Object clientProxy) {
        this.clientProxy = clientProxy;
    }

    private void initInstance() {
        client = (DetectorService.AsyncIface) clientProxy;
    }

    public static void check(String env, String appkey, String path, List<String> providers, AtomicInteger scanRoundCounter) {
        try {
            client.check(env, appkey, path, providers, scanRoundCounter.get(), System.currentTimeMillis(), new OctoThriftCallback());
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void userDefinedHttpCheck(String env, String appkey, String path, List<String> providers,  String checkUrl, AtomicInteger scanRoundCounter) {
        try {
            client.userDefinedHttpCheck(env, appkey, path, providers, checkUrl, scanRoundCounter.get(), System.currentTimeMillis(), new OctoThriftCallback());
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

