package com.sankuai.octo.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-12-30
 * Time: 下午7:30
 */
public class MtthriftServer {

    public static final Logger logger = LoggerFactory.getLogger(MtthriftServer.class);

    public static void main(String[] args) {
        BeanFactory serverBeanFactory = new ClassPathXmlApplicationContext("server.xml");
        logger.info("Mtthrift Server start!");
    }
}
