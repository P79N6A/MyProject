package com.sankuai.mtthrift.testSuite.hardCode;

import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import com.sankuai.mtthrift.testSuite.idl.TestService;

/**
 * Created by jiguang on 15/7/15.
 */
public class HardCodeServer {
    private ThriftServerPublisher serverPublisher;


    public HardCodeServer(Object serviceImpl) {
        this(serviceImpl, 12345);
    }


    public HardCodeServer(Object serviceImpl, int port) {
        this(serviceImpl, "serverAppkey", port);

    }

    public HardCodeServer(Object serviceImpl, String appkey, int port) {
        serverPublisher = new ThriftServerPublisher();
        serverPublisher.setAppKey(appkey);
        serverPublisher.setServiceImpl(serviceImpl);
        serverPublisher.setServiceInterface(TestService.class);
        serverPublisher.setPort(port);
    }

    public void run() {
        try {
            serverPublisher.publish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void destroy() {
        long start = System.currentTimeMillis();
        try {
            serverPublisher.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        assert(finish - start > 3000);

        System.out.println(System.currentTimeMillis() - start + " ms");
    }

}
