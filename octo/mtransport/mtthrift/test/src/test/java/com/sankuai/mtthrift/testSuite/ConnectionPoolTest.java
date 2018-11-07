package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/8/23
 * Description:
 */
public class ConnectionPoolTest {

    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static final int port = 9016;
    private static ThriftClientProxy thriftClientProxy;
    private static TestService.Iface iface;
    private static int timeout = 3000;

    @BeforeClass
    public static void start() throws InterruptedException, TTransportException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/connectionPool/server.xml");
        Thread.sleep(10000);
        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();

    }

    private static void initTimeoutClient() {

        MTThriftPoolConfig mtThriftPoolConfig = new MTThriftPoolConfig();
        mtThriftPoolConfig.setMaxActive(8);
        mtThriftPoolConfig.setMaxIdle(4);
        mtThriftPoolConfig.setMinIdle(4);
        mtThriftPoolConfig.setMaxWait(timeout);
        mtThriftPoolConfig.setTestOnBorrow(false);

        thriftClientProxy = new ThriftClientProxy();
        thriftClientProxy.setMtThriftPoolConfig(mtThriftPoolConfig);
        thriftClientProxy.setServiceInterface(com.sankuai.mtthrift.testSuite.idl.TestService.class);
        thriftClientProxy.setTimeout(timeout);
        thriftClientProxy.setLocalServerPort(port);
        thriftClientProxy.setAppKey("appkey");
        thriftClientProxy.setNettyIO(false);
        try {
            thriftClientProxy.afterPropertiesSet();
            iface = (TestService.Iface) thriftClientProxy.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void initTestOnBorrowClient() {

        MTThriftPoolConfig mtThriftPoolConfig = new MTThriftPoolConfig();
        mtThriftPoolConfig.setMaxActive(8);
        mtThriftPoolConfig.setMaxIdle(4);
        mtThriftPoolConfig.setMinIdle(4);
        mtThriftPoolConfig.setMaxWait(timeout);
        mtThriftPoolConfig.setTestOnBorrow(true);

        thriftClientProxy = new ThriftClientProxy();
        thriftClientProxy.setMtThriftPoolConfig(mtThriftPoolConfig);
        thriftClientProxy.setServiceInterface(com.sankuai.mtthrift.testSuite.idl.TestService.class);
        thriftClientProxy.setTimeout(timeout);
        thriftClientProxy.setLocalServerPort(port);
        thriftClientProxy.setAppKey("appkey");
        thriftClientProxy.setNettyIO(false);
        try {
            thriftClientProxy.afterPropertiesSet();
            iface = (TestService.Iface) thriftClientProxy.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void initTimeBetweenEvictionRuns() {

        MTThriftPoolConfig mtThriftPoolConfig = new MTThriftPoolConfig();
        mtThriftPoolConfig.setMaxActive(8);
        mtThriftPoolConfig.setMaxIdle(4);
        mtThriftPoolConfig.setMinIdle(0);
        mtThriftPoolConfig.setMaxWait(timeout);
        mtThriftPoolConfig.setTestOnBorrow(false);

        thriftClientProxy = new ThriftClientProxy();
        thriftClientProxy.setMtThriftPoolConfig(mtThriftPoolConfig);
        thriftClientProxy.setServiceInterface(com.sankuai.mtthrift.testSuite.idl.TestService.class);
        thriftClientProxy.setTimeout(timeout);
        thriftClientProxy.setLocalServerPort(port);
        thriftClientProxy.setAppKey("appkey");
        thriftClientProxy.setNettyIO(false);
        try {
            thriftClientProxy.afterPropertiesSet();
            iface = (TestService.Iface) thriftClientProxy.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @AfterClass
    public static void stop() {
        serverBeanFactory.destroy();
        if (!ContextStore.getRequestMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    @Test
    public void testTimeout() {
        initTimeoutClient();
        for(int i = 0; i < 5; i++) {
            long now = System.currentTimeMillis();
            try {
                iface.testTimeout();
            } catch (TException e) {
                assert (e.getMessage().contains("timeout"));
                long timeElapsed = System.currentTimeMillis() - now;
                System.out.println("timeElapsed: " + timeElapsed);
                System.out.println("timeout: " + timeout);
                System.out.println(((float) timeElapsed) / timeout);
                if(i >= 1)
                 assert (timeElapsed / timeout < 1.01);
            }
        }
        thriftClientProxy.destroy();
    }

    @Test
    public void testMaxwait() {

    }

    @Test
    public void testOnBorrow() throws TException {
        initTestOnBorrowClient();
        assert (thriftClientProxy.getMtThriftPoolConfig().isTestOnBorrow() );
        iface.testLong(123L);
        thriftClientProxy.destroy();

    }

//    @Test
    public void testTimeBetweenEvictionRuns() throws TException, InterruptedException {
        initTimeBetweenEvictionRuns();
        assert (thriftClientProxy.getMtThriftPoolConfig().getMinIdle() == 0 );
        assert (thriftClientProxy.getMtThriftPoolConfig().getTimeBetweenEvictionRunsMillis() == 30000 );
        iface.testLong(123L);
        Thread.sleep(1000000);
        thriftClientProxy.destroy();


    }
}
