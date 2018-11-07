package com.sankuai.octo.benchmark;

import com.sankuai.octo.dorado.core.server.DefaultServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-18
 * Time: 下午1:41
 */
public class DoradoServer {

    public static final Logger logger = LoggerFactory.getLogger(DoradoServer.class);

    public static void main(String[] args) throws Exception {
        Map<Class<?>, Object> serviceMap = new HashMap(){{
            put(Class.forName("com.sankuai.octo.benchmark.thrift.EchoService"),
                    Class.forName("com.sankuai.octo.benchmark.thrift.EchoServiceImpl").newInstance());
        }};
        new DefaultServer(serviceMap).run();
        logger.info("Dorado server start!");
    }
}
