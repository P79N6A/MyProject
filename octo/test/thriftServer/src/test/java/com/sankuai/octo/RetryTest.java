package com.sankuai.octo;

import com.sankuai.octo.test.thrift.model.SGLog;
import com.sankuai.octo.test.thrift.model.SGModuleInvokeInfo;
import com.sankuai.octo.test.thrift.service.LogCollectorService;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client.xml")
public class RetryTest {

    @Autowired
    @Qualifier("testClient")
    private LogCollectorService.Iface testClient;

    @Test
    public void test() throws InterruptedException {
        System.out.println("init......");
        int thread = 1;
        for (int i = 0; i < thread; i++) {
            new Thread(new Runnable() {
                public void run() {
                    int count = 10000000;
                    while (count-- > 0) {
                        try {
                            testClient.uploadModuleInvoke(randomInvokeInfo(count));
                            testClient.uploadLog(randomLog(count));
//                            System.out.println(count);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }
            }).start();
        }
        Thread.sleep(10000000);
    }

    private SGModuleInvokeInfo randomInvokeInfo(int count) {
        SGModuleInvokeInfo info = new SGModuleInvokeInfo();
        info.setSpanName("TestController.test");
        info.setLocalAppKey("com.sankuai.inf.test.client");
        info.setLocalHost("testhost");
        info.setRemoteAppKey("com.sankuai.inf.test");
        info.setRemoteHost("192.168.2.2");
        info.setStatus(0);
        info.setCount(count);
        info.setType(1);
        info.setCost(RandomUtils.nextInt(20) + 20);
        return info;
    }

    private SGLog randomLog(int count) {
        SGLog log = new SGLog();
        log.setAppkey("com.sankuai.inf.test.client");
        log.setLevel(count);
        log.setTime(System.currentTimeMillis());
        log.setContent("test" + count);
        return log;
    }
}
