package com.sankuai.octo.mnsc.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class DynamicServerConfig extends PropertyPlaceholderConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicServerConfig.class);
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (StringUtils.isEmpty(System.getProperty("jetty.appkey"))) {
            // not use plus to start the mnsc. e.g. mac. init the app.key = com.sankuai.inf.mnsc
            LOG.warn("set the jetty.appkey = com.sankuai.inf.mnsc");
            System.setProperty("jetty.appkey", "com.sankuai.inf.mnsc");
        }
        super.postProcessBeanFactory(beanFactory);
    }
}
