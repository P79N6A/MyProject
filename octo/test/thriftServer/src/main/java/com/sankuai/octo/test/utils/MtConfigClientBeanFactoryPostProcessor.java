package com.sankuai.octo.test.utils;

import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.exception.MtConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

@Component
public class MtConfigClientBeanFactoryPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {
    private static final Logger LOG = LoggerFactory.getLogger(MtConfigClientBeanFactoryPostProcessor.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        MtConfigClient client = new MtConfigClient();
        client.setNodeName("thriftServer");
        String configServerHost = NetHelper.getLocalIp().startsWith("10.") ? "http://config.sankuai.com" : "http://master.config.test.sankuai.info";
        client.setConfigServerHost(configServerHost);
        client.setPullPeriod(100);
        client.setScanBasePackage("com.sankuai");
        try {
            client.init();
        } catch (MtConfigException e) {
            LOG.error("init MtConfigClient (" + client.getNodeName() + ") failed", e);
        }
        beanFactory.registerSingleton("mtConfigClient", client);
        LOG.info("MtConfigClient ({}) init", client.getNodeName());
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
