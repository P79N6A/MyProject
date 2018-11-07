package com.sankuai.octo.msgp.utils;

import com.sankuai.msgp.common.config.MtConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Component
public class CustomizedPropertyConfigurer extends PropertyPlaceholderConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(CustomizedPropertyConfigurer.class);

    @Override
    protected void loadProperties(Properties props) throws IOException {
        super.loadProperties(props);

        String user = MtConfigBean.rabbitmqUser;
        String password = MtConfigBean.rabbitmqPassword;
        String appkey = MtConfigBean.appkey;
        if (user.equals("cosguest") && password.equals("cosguest") && appkey != null) {
            props.put("rabbitmq.user", appkey);
            props.put("rabbitmq.password", appkey);
        } else {
            props.put("rabbitmq.user", user);
            props.put("rabbitmq.password", password);
        }
        String cluster = MtConfigBean.rabbitmqCluster;
        if (cluster == null) {
            LOG.error("can't find rabbit cluster");
        } else {
            props.put("rabbitmq.cluster", cluster);
        }
        props.put("rabbitmq.log.cluster", MtConfigBean.rabbitmqLogCluster);
        props.put("rabbitmq.log.vhost", MtConfigBean.rabbitmqLogVhost);
        props.put("rabbitmq.mtrace.cluster", MtConfigBean.rabbitmqMtraceCluster);
        props.put("rabbitmq.mtrace.vhost", MtConfigBean.rabbitmqMtraceVhost);
        props.put("rabbitmq.mtrace.user", MtConfigBean.rabbitmqMtraceUser);
        props.put("rabbitmq.mtrace.password", MtConfigBean.rabbitmqMtracePassword);

        String vhost = MtConfigBean.rabbitmqVhost;
        props.put("rabbitmq.vhost", vhost);
        props.put("rabbitmq.transacted", MtConfigBean.rabbitmqTransacted);

        props.put("mtsg.secret", MtConfigBean.mtsgSecret);
        props.put("mtmq.host", MtConfigBean.mtmqHost);

        LOG.info(props.toString());

    }
}
