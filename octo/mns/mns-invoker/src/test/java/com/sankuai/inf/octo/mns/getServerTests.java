package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.inf.octo.mns.util.*;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import com.sankuai.sgagent.thrift.model.UptCmd;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class getServerTests {
    private static Logger LOG = LoggerFactory.getLogger(ServiceListListenerTest.class);

    final String consumerAppkey = "com.sankuai.inf.yangjie";
    final String providerAppkey = "com.sankuai.inf.logCollector";
    final String remoteAppkey = "com.sankuai.octo.tmy";

    final int remotePort = 65500;
    final String localip = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();

    //observe the weight increment when svr start,please check MCC policy firstly in case for dead loop
    @Test
    public void testSrvSlowStart() throws Exception {
        /**
         *  change appkey and port according to test
         *  the current Jenkins host is mtthrift-test03 10.4.246.239
         */

//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    MnsInvoker.registerThriftService(remoteAppkey, remotePort);
//                    ServerSocket svrSocket = new ServerSocket(remotePort);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//
//        Thread.sleep(20000);    //wait for scanner to change the weight
//
//        int i_weight = -1;
//        List<SGService> list;
//        Iterator<SGService> iter;
//        while (true) {
//            list = MnsInvoker.getSGServiceList(consumerAppkey, remoteAppkey);
//
//            iter = list.iterator();
//            while (iter.hasNext()) {
//                SGService sgService = iter.next();
//
//                if (sgService.port == remotePort && i_weight != sgService.weight) {
//                    System.out.println("【testSrvSlowStart】 NOW weight is " + sgService.weight);
//
//                    if (1 > sgService.weight - i_weight) {
//                        System.out.println("sgService.weight " + sgService.weight
//                                + " should more than last one " + i_weight + " at least one count");
//                        throw new Exception();
//                    }
//
//                    i_weight = sgService.weight;
//                    if (10 <= sgService.weight) {
//                        break;
//                    }
//                }
//            }
//            if (10 <= i_weight) {
//                System.out.println("【testSrvSlowStart】 test PASS");
//                break;
//            }
//        }
    }


    @Test
    public void testProtocolServiceList() throws TException, InterruptedException {
        String remoteAppkey1 = "com.sankuai.inf.sg_sentinel";
        System.out.println("【testGetServiceListByProtocol】thrift http redis cunsumerAppkey =" + consumerAppkey + " and remoteAppkey = " + remoteAppkey);

        ProtocolRequest redisReq = new ProtocolRequest()
                .setLocalAppkey(consumerAppkey)
                .setRemoteAppkey(remoteAppkey)
                .setProtocol("cellar");
        ProtocolRequest httpReq = new ProtocolRequest()
                .setLocalAppkey(consumerAppkey)
                .setRemoteAppkey(remoteAppkey1)
                .setProtocol("http");
        ProtocolRequest thriftReq = new ProtocolRequest()
                .setLocalAppkey(consumerAppkey)
                .setRemoteAppkey(remoteAppkey1)
                .setProtocol("thrift");


        List<SGService> redisList = MnsInvoker.getServiceList(redisReq);
        List<SGService> httpList = MnsInvoker.getServiceList(httpReq);
        List<SGService> thriftList = MnsInvoker.getServiceList(thriftReq);
        Assert.assertNotNull(redisList);
        Assert.assertFalse(redisList.isEmpty());
        Assert.assertEquals(0, httpList.size());
        System.out.println("thrift size is " + thriftList.size());
//            Assert.assertEquals(5, thriftList.size());

    }


    @Test
    public void testServiceList() throws InterruptedException {
        System.out.println("【testGetServercieList】 cunsumerAppkey =" + consumerAppkey + " and remoteAppkey = " + remoteAppkey);
        for (int i = 1; i < 100; i++) {
            List<SGService> list = MnsInvoker.getSGServiceList(consumerAppkey, remoteAppkey);
            Assert.assertNotNull(list);
            Assert.assertFalse(list.isEmpty());
        }
    }

    @Test
    public void multiThread() throws InterruptedException {
        for (int i = 1; i < 3; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        try {
                            testProtocolServiceList();
                        } catch (TException e) {
                            Assert.assertTrue(false);
                        }
                        testServiceList();
                        testHttpServiceList();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        Thread.sleep(1000);
    }

    @Test
    public void testHttpServiceList() throws InterruptedException {
        System.out.println("【testHttpServiceList】 cunsumerAppkey =" + consumerAppkey + " and remoteAppkey = com.sankuai.cos.mtconfig");
        for (int i = 0; i < 3; i++) {
            List<SGService> list = MnsInvoker.getHttpServiceList(consumerAppkey, "com.sankuai.cos.mtconfig");
            Assert.assertNotNull(list);
            Assert.assertFalse(list.isEmpty());
        }
    }

//    @Test
//    public void testMultiServerList() throws InterruptedException {
//
//        List<SGService> list1 = MnsInvoker.getSGServiceList(consumerAppkey, providerAppkey);
//        Assert.assertNotNull(list1);
//        LOG.info(list1.toString());
//
//        Assert.assertTrue(list1.toString().contains(providerAppkey));
//
//        List<SGService> list2 = MnsInvoker.getSGServiceList(consumerAppkey, "com.sankuai.cos.mtconfig");
//
//        Assert.assertNotNull(list2);
//        Assert.assertTrue(list2.toString().contains("com.sankuai.cos.mtconfig"));
//    }

    @Test
    public void testGetServiceWithServiceName() throws InterruptedException, TException {
        //
        delete(remoteAppkey, "thrift", localip + ":5198");

        SGService service = SGServiceUtilTests.getDefaultSGService(remoteAppkey, 5198, true);
        Map<String, ServiceDetail> mapData = new HashMap<String, ServiceDetail>();
        mapData.put("tmy", new ServiceDetail(true));
        service.setServiceInfo(mapData);
        MnsInvoker.registServiceWithCmd(UptCmd.ADD.getValue(), service);
        Thread.sleep(5000);

//        ProtocolRequest serviceNameReq = new ProtocolRequest()
//                .setLocalAppkey(consumerAppkey)
//                .setServiceName("tmy")
//                .setProtocol("thrift");
//        List<SGService> res = MnsInvoker.getServiceList(serviceNameReq);
//        boolean isExist = false;
//        for (SGService item : res) {
//            if (item.getIp().equals(localip) && item.getPort() == 5198) {
//                isExist = true;
//                break;
//            }
//        }
//        Assert.assertTrue(isExist);
    }

    public static void delete(String remoteAppkey, String protocol, String ipPort) throws InterruptedException {
        List<String> ipPorts = new ArrayList<String>();
        ipPorts.add(ipPort);
        String url = "http://mns.test.sankuai.info/api/providers/delete";
        String data = HttpUtilTests.getDeletedProviderJson(remoteAppkey, protocol, ipPorts, 3);
        HttpUtilTests.delete(url, data);
        Thread.sleep(5000);
    }

    @Before
    public void setCustomizedSgagent() {
        CustomizedManager.setCustomizedSGAgents("10.4.243.121:5266");
    }
}
