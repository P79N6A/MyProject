package com.sankuai.mtthrift.testSuite.rhinoLimiter;

import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2018 Meituan
 * All rights reserved
 * Description：
 * User: wuxinyu
 * Date: Created in 2018/5/27 下午7:07
 * Copyright: Copyright (c) 2018
 */
public class RhinoLimiterClientTest {

    private static Logger logger = LoggerFactory.getLogger(RhinoLimiterClientTest.class);

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static Twitter.Iface clientProxy;
    private static Twitter.Iface clientProxy2;
    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter clientProxy3;


    @BeforeClass
    public static void start() {
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/rhinoLimiter/client.xml");
        clientProxy = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        clientProxy2 = (Twitter.Iface) clientBeanFactory.getBean("clientProxyWithFilter");
        clientProxy3= (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("annotationClientProxy");
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
    }

    /**
     * 调用端单机限流，服务端不限流
     * 使用ClientProxy2
     */
    @Test
    public void testClientLimit() throws InterruptedException {

        ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool
                (5, new MTDefaultThreadFactory("RhinoLimiter"));
        for(int i = 1; i <= 5; i++) {
            scheduExec.execute(new RhinoTask2(new AtomicInteger(0),
                    new AtomicInteger(0),"clientProxyWithFilter",
                    String.valueOf(i),500));
        }
        scheduExec.awaitTermination(65, TimeUnit.SECONDS);
    }


    static class RhinoTask2 implements Runnable {

        AtomicInteger successCount;
        AtomicInteger failCount;
        String beanName;
        String taskName;
        int sleepTime;

        public RhinoTask2(AtomicInteger successCount, AtomicInteger failCount, String beanName, String taskName, int sleepTime) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.beanName = beanName;
            this.taskName = taskName;
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            Twitter.Iface clientProxy = (Twitter.Iface) clientBeanFactory.getBean(beanName);
            for(int i = 0; i < 60; i++) {
                try {
                    Double result = clientProxy.testDouble(2.0d);
                    System.out.println(result);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    //e.printStackTrace();
                    failCount.incrementAndGet();
                } finally {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(taskName != null) {
                synchronized(RhinoTask2.class) {
                    System.out.println("###" + taskName + "###");
                    System.out.println("successCount: " + successCount.get());
                    System.out.println("failCount: " + failCount.get());
                }
            }
        }
    }


    @Test
    public void testClientFilter() throws InterruptedException {
        int failCount = 0;
        int successCount = 0;
        // 第一种情况 entrance 和 params 都为空
        for (int i = 0; i < 10; i++) {
            try {
                Double result = clientProxy2.testDouble(2.0d);
                System.out.println(result);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
            Thread.sleep(500);
        }
        System.out.println("successCount: " + successCount);
        System.out.println("failCount: " + failCount);
    }


    /**
     * 调用端ClientRhinoLimiterFilter过滤器
     * 单机限流，服务端不限流
     * 使用clientProxy
     */
    @Test
    public void testClientSingleQPS() throws InterruptedException {
        int successCount = 0;
        int failCount = 0;
        for(int i = 0; i < 30; i++) {
            try {
                boolean result = clientProxy.testBool(false);
                System.out.println(result);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
            Thread.sleep(1000);
        }
        System.out.println("successCount: " + successCount);
        System.out.println("failCount: " + failCount);
    }


    /**
     * 调用端ClientRhinoLimiterFilter过滤器
     * 多个调用端单机限流，服务端不限流
     * 使用clientProxy
     */
    @Test
    public void testClientSingleQPS2() throws InterruptedException {
        ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool
                (5, new MTDefaultThreadFactory("client"));
        for (int i = 0; i < 5; i++) {
            scheduExec.execute(new RhinoTask(new AtomicInteger(0), new AtomicInteger(0),
                    "clientProxy", "task-" + i));
        }
        scheduExec.awaitTermination(60, TimeUnit.SECONDS);
        Thread.sleep(1000 * 65);
    }

    /**
     * 调用端ClientRhinoLimiterFilter过滤器
     * 集群限流，服务端不限流
     * 使用clientProxy
     */
    @Test
    public void testClientcluster() throws InterruptedException {

        ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool
                (5, new MTDefaultThreadFactory("RhinoLimiter"));
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        Random r = new Random();

        for(int i = 0; i < 20; i++) {
            scheduExec.execute(new RhinoTask(successCount,failCount,"clientProxy",
                    null,r.nextInt(100) + 30));
        }
        scheduExec.awaitTermination(600, TimeUnit.SECONDS);
        System.out.println("successCount: " + successCount.get());
        System.out.println("failCount: " + failCount.get());

    }

    static class RhinoTask implements Runnable {

        AtomicInteger successCount;
        AtomicInteger failCount;
        String beanName;
        String taskName;
        Integer times = 60;

        public RhinoTask(AtomicInteger successCount, AtomicInteger failCount, String beanName) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.beanName = beanName;
        }

        public RhinoTask(AtomicInteger successCount, AtomicInteger failCount, String beanName, String taskName) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.beanName = beanName;
            this.taskName = taskName;
        }

        public RhinoTask(AtomicInteger successCount, AtomicInteger failCount, String beanName, String taskName, Integer times) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.beanName = beanName;
            this.taskName = taskName;
            this.times = times;
        }

        @Override
        public void run() {
            clientProxy3 = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("annotationClientProxy");
            //Twitter.Iface clientProxy = (Twitter.Iface) clientBeanFactory.getBean(beanName);
            for(int i = 0; i < times; i++) {
                try {
                    //boolean result = clientProxy.testBool(false);
                    //int result = clientProxy.testI32(50);
                    boolean result = clientProxy3.testBool(false);
                    System.out.println(result);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                    failCount.incrementAndGet();
                } finally {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(taskName != null) {
                System.out.println("successCount: " + successCount.get());
                System.out.println("failCount: " + failCount.get());
            }
        }
    }

    /**
     * 调用端不限流
     * 服务端限流
     * 使用clientProxy3
     */
    @Test
    public void testServerLimit() throws InterruptedException {
        ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool
                (5, new MTDefaultThreadFactory("client"));
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        for (int i = 0; i < 1; i++) {
            scheduExec.execute(new RhinoTask(successCount, failCount, "annotationClientProxy"));
        }
        scheduExec.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("successCount: " + successCount.get());
        System.out.println("failCount: " + failCount.get());
    }

    /**
     * 调用端ClientRhinoLimiterFilter过滤器
     * 服务端限流
     * 使用clientProxy5
     * 测试正则的入口规则
     */
    @Test
    public void testRex() throws InterruptedException {
        int successCount = 0;
        int failCount = 0;
        for (int i = 0; i < 60; i++) {
            try {
                int result = clientProxy2.testI32(100);
                System.out.println(result);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
            Thread.sleep(500);
        }
        System.out.println("successCount: " + successCount);
        System.out.println("failCount: " + failCount);
    }


    /**
     * 测试白名单配置
     */
    @Test
    public void testWhiteList() throws InterruptedException {
        int successCount = 0;
        int failCount = 0;
        for(int i = 0; i < 30; i++) {
            try {
                //RhinoLimitContext context = new RhinoLimitContext("com.sankuai.mtthrift.testSuite.idlTest.TwitterImpl.testList", null);
                //RhinoLimitContextManager.putContext("clientProxy2",context);
                List<String> result = clientProxy2.testList(Arrays.asList("a","b","c"));
                System.out.println(result);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
            Thread.sleep(1000);
        }
        System.out.println("successCount: " + successCount);
        System.out.println("failCount: " + failCount);
    }


}
