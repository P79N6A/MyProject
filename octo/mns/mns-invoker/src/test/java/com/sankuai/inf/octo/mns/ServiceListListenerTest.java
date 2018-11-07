package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.inf.octo.mns.util.HttpUtilTests;
import com.sankuai.inf.octo.mns.util.SGServiceUtilTests;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by lhmily on 05/27/2016.
 */
public class ServiceListListenerTest {
    private static Logger LOG = LoggerFactory.getLogger(ServiceListListenerTest.class);
    String remoteAppkey = "com.sankuai.octo.tmy";
    String localAppkey = "com.sankuai.octo.yangjie";
    int port = 9010;
    String localIP = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();
    boolean addCallback = false, deletedCallback = false;

    private enum ServiceType {
        PROTOCOL, ZKFWEIGHT, ORIGIN
    }


    private class MyListener implements IServiceListChangeListener {
        @Override
        public void changed(ProtocolRequest req, List<SGService> oldList, List<SGService> newList, List<SGService> addList, List<SGService> deletedList, List<SGService> modifiedList) {
            System.out.println("req protocol: " + req.getProtocol());
            if (!addList.isEmpty()) {
                addCallback = true;
                print("addList:", addList);
            }

            if (!deletedList.isEmpty()) {
                deletedCallback = true;
                print("deletedList:", deletedList);
            }

        }

        private void print(String msg, List<SGService> list) {
            System.out.println(msg);
            for (SGService service : list) {
                System.out.println(service);
            }
        }
    }

    @Test
    public void serviceListListenerTest() throws TException, InterruptedException {

        testListener(ServiceType.ORIGIN);
        testListener(ServiceType.PROTOCOL);
        testListener(ServiceType.ZKFWEIGHT);
    }

    private void testListener(ServiceType type) throws TException, InterruptedException {

        ProtocolRequest redisReq = new ProtocolRequest()
                .setLocalAppkey(localAppkey)
                .setRemoteAppkey(remoteAppkey)
                .setProtocol("cellar");
        ProtocolRequest httpReq = new ProtocolRequest()
                .setLocalAppkey(localAppkey)
                .setRemoteAppkey(remoteAppkey)
                .setProtocol("http");
        ProtocolRequest thriftReq = new ProtocolRequest()
                .setLocalAppkey(localAppkey)
                .setRemoteAppkey(remoteAppkey)
                .setProtocol("thrift");

        IServiceListChangeListener redisListener = new MyListener();
        IServiceListChangeListener thriftListener = new MyListener();
        IServiceListChangeListener httpListener = new MyListener();
        delete();

        addCallback = false;
        deletedCallback = false;
        switch (type) {
            case ZKFWEIGHT:
                Assert.assertEquals(0, MnsInvoker.addServiceListListenerWithZKFweight(redisReq, redisListener));
                Assert.assertEquals(0, MnsInvoker.addServiceListListenerWithZKFweight(thriftReq, thriftListener));
                Assert.assertEquals(0, MnsInvoker.addServiceListListenerWithZKFweight(httpReq, httpListener));
                break;
            case PROTOCOL:
                Assert.assertEquals(0, MnsInvoker.addServiceListener(redisReq, redisListener));
                Assert.assertEquals(0, MnsInvoker.addServiceListener(thriftReq, thriftListener));
                Assert.assertEquals(0, MnsInvoker.addServiceListener(httpReq, httpListener));
                break;
            case ORIGIN:
                Assert.assertEquals(0, MnsInvoker.addOriginServiceListener(redisReq, redisListener));
                Assert.assertEquals(0, MnsInvoker.addOriginServiceListener(thriftReq, thriftListener));
                Assert.assertEquals(0, MnsInvoker.addOriginServiceListener(httpReq, httpListener));
                break;
        }


        LOG.info("Now start to test listener");
        register();
        Assert.assertTrue(addCallback);
        delete();
        Assert.assertTrue(deletedCallback);
        LOG.info("Now start to test remove listener");

        switch (type) {
            case ZKFWEIGHT:
                Assert.assertEquals(0, MnsInvoker.removeServiceListListenerWithZKFweight(redisReq, redisListener));
                Assert.assertEquals(0, MnsInvoker.removeServiceListListenerWithZKFweight(thriftReq, thriftListener));
                Assert.assertEquals(0, MnsInvoker.removeServiceListListenerWithZKFweight(httpReq, httpListener));
                break;
            case PROTOCOL:
                Assert.assertEquals(0, MnsInvoker.removeServiceListener(redisReq, redisListener));
                Assert.assertEquals(0, MnsInvoker.removeServiceListener(thriftReq, thriftListener));
                Assert.assertEquals(0, MnsInvoker.removeServiceListener(httpReq, httpListener));
                break;
            case ORIGIN:
                Assert.assertEquals(0, MnsInvoker.removeOriginServiceListener(redisReq, redisListener));
                Assert.assertEquals(0, MnsInvoker.removeOriginServiceListener(thriftReq, thriftListener));
                Assert.assertEquals(0, MnsInvoker.removeOriginServiceListener(httpReq, httpListener));
                break;
        }

        addCallback = false;
        deletedCallback = false;
        register();
        delete();
        Assert.assertFalse(addCallback || deletedCallback);
    }


    private void delete() throws InterruptedException {
        HttpUtilTests.delete(remoteAppkey, "thrift", localIP, 10000);
        HttpUtilTests.delete(remoteAppkey, "thrift", localIP, 10001);

        HttpUtilTests.delete(remoteAppkey, "http", localIP, 10000);
        HttpUtilTests.delete(remoteAppkey, "http", localIP, 10001);
        HttpUtilTests.delete(remoteAppkey, "cellar", localIP, 10000);
        HttpUtilTests.delete(remoteAppkey, "cellar", localIP, 10001);
        Thread.sleep(10000);

    }

    private void register() throws TException, InterruptedException {
        int weigth = 10;
        registerHandle("thrift", localIP, 10000, weigth);
        registerHandle("thrift", localIP, 10001, weigth);

        registerHandle("http", localIP, 10000, weigth);
        registerHandle("http", localIP, 10001, weigth);

        registerHandle("cellar", localIP, 10000, weigth);
        registerHandle("cellar", localIP, 10001, weigth);
        Thread.sleep(10000);

    }

    private void registerHandle(String protocol, String ip, int port, int weigth) throws TException, InterruptedException {
        SGService service = SGServiceUtilTests.getDefaultSGService(remoteAppkey, port, true);
        service.setIp(ip);
        service.setProtocol(protocol);
        service.setWeight(weigth).setFweight(weigth);
        MnsInvoker.registerService(service);
    }

    @After
    public void destroy() throws InterruptedException {
        delete();
    }
}
