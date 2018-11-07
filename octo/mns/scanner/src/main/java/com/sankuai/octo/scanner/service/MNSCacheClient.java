package com.sankuai.octo.scanner.service;

import com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-9-22
 * Time: 下午5:55
 */
public class MNSCacheClient implements FactoryBean<Object>, ApplicationContextAware, InitializingBean {

    private Object clientProxy;
    private static MNSCacheService.Iface client;


    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
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
        client = (MNSCacheService.Iface) clientProxy;
    }

    public static MNSCacheService.Iface getInstance() {
        return client;
    }
}

