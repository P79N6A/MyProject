package com.meituan.service.mobile.mtthrift;

import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.auth.DefaultSignHandler;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-20
 * Time: 下午2:23
 */
public class TestThriftClient {

    public static void main(String[] args) throws Exception {

        Tracer.serverRecv(new TraceParam("test"));
        ThriftClientProxy proxy = new ThriftClientProxy();
        proxy.setServiceInterface(Class.forName("com.meituan.service.mobile.mtthrift.HelloService"));
        //proxy.setLocalServerPort(10001);
        proxy.setRemoteAppkey("com.sankuai.inf.mtthrift.testServer");
        proxy.setRemoteServerPort(10002);
        proxy.setAppKey("com.sankuai.inf.mtthrift.testClient");
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("com.sankuai.inf.mtthrift.testClient", "123456");
//        proxy.setSignHandler(new DefaultSignHandler(map));
        proxy.setTimeout(1000000);
        proxy.afterPropertiesSet();
        HelloService.Iface client = (HelloService.Iface) proxy.getObject();
        Thread.sleep(5000);
        for (int i = 0; i < 10; i++) {
            try {
                System.out.println(client.sayBye(i + ":old protocol"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        proxy.destroy();
        Tracer.serverSend();

    }

}
