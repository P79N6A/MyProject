package com.sankuai.octo.detector;

import com.sankuai.octo.updater.thrift.ProviderStatus;
import com.sankuai.octo.updater.thrift.UpdaterService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-6-12
 * Time: 下午2:27
 */
public class UpdaterServiceClient implements FactoryBean<Object>, ApplicationContextAware, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(UpdaterServiceClient.class);

    private Object clientProxy;
    private static UpdaterService.Iface client;


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
        client = (UpdaterService.Iface) clientProxy;
    }

    public static UpdaterService.Iface getInstance() {
        return client;
    }

    public static void doubleCheck(String providerPath, ProviderStatus status) {
        logger.info("need double check:" + providerPath + " " + status.toString());
        try {
            client.doubleCheck(providerPath, status);
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static void userDefinedHttpDoubleCheck(String providerPath, ProviderStatus status, String checkUrl) {
        logger.info("need http double check:" + providerPath + " " + status.toString());
        try {
            client.userDefinedHttpDoubleCheck(providerPath, status, checkUrl);
        } catch (TException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

