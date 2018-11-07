package com.sankuai.mtthrift.testSuite;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.octo.benchmark.thrift.EchoService;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/10/21
 * Description:
 */
public class FirstCallTimeTest {
    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;

    private static EchoService.Iface client;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/firstCall/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/firstCall/client.xml");
        Thread.sleep(30000);
        client = (EchoService.Iface) clientBeanFactory.getBean("clientProxy");
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    @Test
    public void baseTypeTest() throws InterruptedException {

        for(int i = 0; i < 10; i++) {
            try {
                boolean b = true;
                long start = System.currentTimeMillis();
                String result = client.sendString("beijing/shanghai");
                System.out.println(result);
                System.out.println(System.currentTimeMillis() - start);
                Thread.sleep(10);
                assert (result.equals("beijing/shanghai"));
            } catch (TException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void mnsInvokerTest() {

        for(int i = 0; i < 10; i++) {
            try {
                long start = System.currentTimeMillis();
                MnsInvoker.getSGServiceList(
                        "com.sankuai.inf.mtthrift.testClient", "com.sankuai.octo.mtthrift.demo.benchmark");
                System.out.println(System.currentTimeMillis() - start);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void catNewTransactionPerformance() {
        for(int i = 0; i < 10; i++) {
            try {
                long start = System.currentTimeMillis();
                Transaction transaction = Cat.newTransaction("OctoCall" + i, "method" + i);
                System.out.println(System.currentTimeMillis() - start);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

}
