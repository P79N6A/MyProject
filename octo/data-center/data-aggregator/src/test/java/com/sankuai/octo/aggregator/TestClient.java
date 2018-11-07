package com.sankuai.octo.aggregator;

import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo;
import com.sankuai.octo.aggregator.thrift.service.LogCollectorService;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class TestClient {
    @Autowired
    private LogCollectorService.Iface logCollectorService;

    private int nThreadNum = 40;
    private int readCount = 100000;
    private long allStart;
    private long allEnd;
    private long threadEnd;


    private AtomicInteger averTime = new AtomicInteger(0);
    private AtomicInteger totalCount = new AtomicInteger(-1);
    private SGModuleInvokeInfo[] infos = new SGModuleInvokeInfo[readCount];
    @Test
    public void testLogCollector() {
        int ThreadNum = 1;
        final int count = 1000;
        System.err.println("start test!");
        final long start = System.currentTimeMillis();
        for (int i = 0; i < ThreadNum; i++) {
            new Thread(new Runnable() {
                int c = count;

                @Override
                public void run() {
                    while (c-- > 0) {
                        try {
                            logCollectorService.uploadModuleInvoke(randomInvokeInfo());
                        } catch (TException e) {
                            e.printStackTrace();
                        }
                    }
                    //System.err.println(Thread.currentThread().getId() + ", time: " + (System.currentTimeMillis() - start));
                }
            }).start();
        }
        while (true) ;
    }

    @Test
    public void test() throws Exception {

        System.err.println("Total thread number: " + nThreadNum);

        allStart = System.currentTimeMillis();
        Thread[] threads = new Thread[nThreadNum];

        for (int i = 0; i < nThreadNum; i++) {
            threads[i] = new TestThread(i);
            threads[i].start();
        }
        for (int i = 0; i < nThreadNum; i++) {
            threads[i].join();
        }
        allEnd = System.currentTimeMillis();
        System.err.println("Average Time per Thread: " + averTime.intValue()
                / nThreadNum + " ms.");
        System.err.println("Eplased time: " + (allEnd - allStart)
                + " ms.");

        System.out.println("count:"+atomicCount.get());
    }

    private SGModuleInvokeInfo randomInvokeInfo() {
        SGModuleInvokeInfo info = new SGModuleInvokeInfo();
        info.setSpanName("TestController.test");
        info.setLocalAppKey("com.sankuai.inf.test.logCollector");
        info.setLocalHost("testhost");
        info.setRemoteAppKey("test");
        info.setStatus(0);
        info.setType(1);
        info.setStart(System.currentTimeMillis());
        info.setCount(RandomUtils.nextInt(10) + 1000);
        info.setCost(RandomUtils.nextInt(20) + 20);
        return info;
    }
    AtomicInteger atomicCount = new AtomicInteger();
    class TestThread extends Thread {
        private int index;
        int id = 0;

        public TestThread(int threadIndex) {
            index = threadIndex;
        }

        public void run() {
            System.out.println("thread Index: " + index + " start ...");
            while (true) {
                id = totalCount.incrementAndGet();
                if (id > readCount) {
                    break;
                }
                try {
                    SGModuleInvokeInfo sGModuleInvokeInfo = randomInvokeInfo();
                    infos[id] = sGModuleInvokeInfo;
                    logCollectorService.uploadModuleInvoke(sGModuleInvokeInfo);
                    atomicCount.addAndGet(sGModuleInvokeInfo.getCount());
                    TimeUnit.MILLISECONDS.sleep(10l);
                } catch (Exception e) {
                    System.err.println("=====" + e.getMessage() + "======");
                }

            }
            System.out.println("thread Index: " + index + " finish ...");

            threadEnd = System.currentTimeMillis();
            int time = (int) (threadEnd - allStart);
            averTime.addAndGet(time);
        }
    }

}
