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
public class ClientTest {

    @Autowired
    @Qualifier("testClient")
    private LogCollectorService.Iface testClient;
    @Autowired
    @Qualifier("testClient1")
    private LogCollectorService.Iface testClient1;
    @Autowired
    @Qualifier("testClient2")
    private LogCollectorService.Iface testClient2;
    @Autowired
    @Qualifier("testClient3")
    private LogCollectorService.Iface testClient3;
    @Autowired
    @Qualifier("testClient4")
    private LogCollectorService.Iface testClient4;
    @Autowired
    @Qualifier("testClient5")
    private LogCollectorService.Iface testClient5;
    @Autowired
    @Qualifier("testClient6")
    private LogCollectorService.Iface testClient6;
    @Autowired
    @Qualifier("testClient7")
    private LogCollectorService.Iface testClient7;
    @Autowired
    @Qualifier("testClient8")
    private LogCollectorService.Iface testClient8;
    @Autowired
    @Qualifier("testClient9")
    private LogCollectorService.Iface testClient9;
    @Autowired
    @Qualifier("testClient10")
    private LogCollectorService.Iface testClient10;
    @Autowired
    @Qualifier("testClient11")
    private LogCollectorService.Iface testClient11;
    @Autowired
    @Qualifier("testClient12")
    private LogCollectorService.Iface testClient12;
    @Autowired
    @Qualifier("testClient13")
    private LogCollectorService.Iface testClient13;
    @Autowired
    @Qualifier("testClient14")
    private LogCollectorService.Iface testClient14;
    @Autowired
    @Qualifier("testClient15")
    private LogCollectorService.Iface testClient15;
    @Autowired
    @Qualifier("testClient16")
    private LogCollectorService.Iface testClient16;
    @Autowired
    @Qualifier("testClient17")
    private LogCollectorService.Iface testClient17;
    @Autowired
    @Qualifier("testClient18")
    private LogCollectorService.Iface testClient18;
    @Autowired
    @Qualifier("testClient19")
    private LogCollectorService.Iface testClient19;
    @Autowired
    @Qualifier("testClient20")
    private LogCollectorService.Iface testClient20;
    @Autowired
    @Qualifier("testClient21")
    private LogCollectorService.Iface testClient21;
    @Autowired
    @Qualifier("testClient22")
    private LogCollectorService.Iface testClient22;
    @Autowired
    @Qualifier("testClient23")
    private LogCollectorService.Iface testClient23;
    @Autowired
    @Qualifier("testClient24")
    private LogCollectorService.Iface testClient24;
    @Autowired
    @Qualifier("testClient25")
    private LogCollectorService.Iface testClient25;
    @Autowired
    @Qualifier("testClient26")
    private LogCollectorService.Iface testClient26;
    @Autowired
    @Qualifier("testClient27")
    private LogCollectorService.Iface testClient27;
    @Autowired
    @Qualifier("testClient28")
    private LogCollectorService.Iface testClient28;
    @Autowired
    @Qualifier("testClient29")
    private LogCollectorService.Iface testClient29;
    @Autowired
    @Qualifier("testClient30")
    private LogCollectorService.Iface testClient30;
    @Autowired
    @Qualifier("AtestClient31")
    private LogCollectorService.Iface testClient31;

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
                            testClient1.uploadModuleInvoke(randomInvokeInfo(count + 1));
                            testClient2.uploadModuleInvoke(randomInvokeInfo(count + 2));
                            testClient3.uploadModuleInvoke(randomInvokeInfo(count + 3));
                            testClient4.uploadModuleInvoke(randomInvokeInfo(count + 4));
                            testClient5.uploadModuleInvoke(randomInvokeInfo(count + 5));
                            testClient6.uploadModuleInvoke(randomInvokeInfo(count + 6));
                            testClient7.uploadModuleInvoke(randomInvokeInfo(count + 7));
                            testClient8.uploadModuleInvoke(randomInvokeInfo(count + 8));
                            testClient9.uploadModuleInvoke(randomInvokeInfo(count + 9));
                            testClient10.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient11.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient12.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient13.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient14.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient15.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient16.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient17.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient18.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient19.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient20.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient21.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient22.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient23.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient24.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient25.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient26.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient27.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient28.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient29.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient30.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient31.uploadModuleInvoke(randomInvokeInfo(count + 10));
                            testClient.uploadLog(randomLog(count));
                            testClient1.uploadLog(randomLog(count + 1));
                            testClient2.uploadLog(randomLog(count + 2));
                            testClient3.uploadLog(randomLog(count + 3));
                            testClient4.uploadLog(randomLog(count + 4));
                            testClient5.uploadLog(randomLog(count + 5));
                            testClient6.uploadLog(randomLog(count + 6));
                            testClient7.uploadLog(randomLog(count + 7));
                            testClient8.uploadLog(randomLog(count + 8));
                            testClient9.uploadLog(randomLog(count + 9));
                            testClient10.uploadLog(randomLog(count + 10));
                            testClient11.uploadLog(randomLog(count + 10));
                            testClient12.uploadLog(randomLog(count + 10));
                            testClient13.uploadLog(randomLog(count + 10));
                            testClient14.uploadLog(randomLog(count + 10));
                            testClient15.uploadLog(randomLog(count + 10));
                            testClient16.uploadLog(randomLog(count + 10));
                            testClient17.uploadLog(randomLog(count + 10));
                            testClient18.uploadLog(randomLog(count + 10));
                            testClient19.uploadLog(randomLog(count + 10));
                            testClient20.uploadLog(randomLog(count + 10));
                            testClient21.uploadLog(randomLog(count + 10));
                            testClient22.uploadLog(randomLog(count + 10));
                            testClient23.uploadLog(randomLog(count + 10));
                            testClient24.uploadLog(randomLog(count + 10));
                            testClient25.uploadLog(randomLog(count + 10));
                            testClient26.uploadLog(randomLog(count + 10));
                            testClient27.uploadLog(randomLog(count + 10));
                            testClient28.uploadLog(randomLog(count + 10));
                            testClient29.uploadLog(randomLog(count + 10));
                            testClient30.uploadLog(randomLog(count + 10));
                            testClient31.uploadLog(randomLog(count + 10));
                            //System.out.println(count);
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
