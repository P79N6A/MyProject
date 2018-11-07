package com.sankuai.meituan.config.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class BootInit extends PropertyPlaceholderConfigurer{
    private static Logger LOG = LoggerFactory.getLogger(BootInit.class);
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (StringUtils.isEmpty(System.getProperty("app.key"))) {
            // not use plus to start the mnsc. e.g. mac. init the jetty.appkey = com.sankuai.inf.mnsc
            LOG.warn("set the app.key = com.sankuai.cos.mtconfig");
            System.setProperty("app.key", "com.sankuai.cos.mtconfig");
        }
        super.postProcessBeanFactory(beanFactory);
    }
}
