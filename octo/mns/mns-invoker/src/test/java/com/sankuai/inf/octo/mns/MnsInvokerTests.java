package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.inf.octo.mns.util.HttpUtilTests;
import com.sankuai.inf.octo.mns.util.SGServiceUtilTests;
import com.sankuai.octo.appkey.model.AppkeyDescResponse;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.ProtocolResponse;
import com.sankuai.sgagent.thrift.model.SGService;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/3/10
 * Description:
 */
public class MnsInvokerTests {
    final String consumerAppkey = "com.sankuai.octo.yangjie";
    final String remoteAppkey = "com.sankuai.cos.mtconfig";


    @Test
    public void testMnsInvoker() {
        AppkeyDescResponse ret = MnsInvoker.getAppkeyDesc("com.sankuai.octo.tmy");
        Assert.assertTrue(null != ret);
        Assert.assertTrue(0 == ret.getErrCode());
        Assert.assertTrue(null != ret.getDesc());
        Assert.assertTrue("true".equalsIgnoreCase(ret.getDesc().getCell()));
    }


    @Test
    public void testFirstTime() {
        ProtocolRequest req = new ProtocolRequest();
        req.setProtocol("thrift")
                .setRemoteAppkey("sdfadsfdsafjjdsoijsodfjsioadfjsi");
        long startTime = System.currentTimeMillis();
        MnsInvoker.getServiceList(req);

        long endTime = System.currentTimeMillis();
        Assert.assertTrue(endTime - startTime <= 500);
    }

    @Test
    public void getServiceList() throws InterruptedException {
        System.out.println("【testGetServercieList】 cunsumerAppkey =" + consumerAppkey + " and remoteAppkey = " + remoteAppkey);
        for (int i = 1; i < 1000; i++) {
            List<SGService> list = MnsInvoker.getSGServiceList(consumerAppkey, remoteAppkey);
            Assert.assertNotNull(list);
            Assert.assertFalse(list.isEmpty());
        }
    }

    @Test
    public void testGetOriginServiceList() throws Exception {
        String ip1 = "10.4.245.3";
        String ip2 = "10.21.128.175";
        int port = 10000;
        double fweight = 20.0;
        int weight = 20;
        HttpUtilTests.delete(consumerAppkey, "thrift", ip1, port);
        HttpUtilTests.delete(consumerAppkey, "thrift", ip2, port);

        Thread.sleep(5000);
        SGService service = SGServiceUtilTests.getDefaultSGService(consumerAppkey, port, true);
        service.setIp(ip1)
                .setFweight(fweight)
                .setWeight(weight);
        MnsInvoker.registerService(service);
        SGService service1 = SGServiceUtilTests.getDefaultSGService(consumerAppkey, port, true);
        service1.setIp(ip2)
                .setFweight(fweight)
                .setWeight(weight);
        MnsInvoker.registerService(service1);

        long startTime = System.currentTimeMillis();
        boolean isExceptWeight = false;
        long nowTime = startTime;

        do {
            ProtocolRequest req = new ProtocolRequest();
            req.setProtocol("thrift")
                    .setRemoteAppkey(consumerAppkey);
            List<SGService> list = MnsInvoker.getOriginServiceList(req);
            for (SGService node : list) {
                if (ip1.equals(node.getIp()) && port == node.getPort()) {
                    isExceptWeight = (node.getFweight() - fweight <= 0.0001);
                } else if (ip2.equals(node.getIp()) && port == node.getPort()) {
                    isExceptWeight = (node.getFweight() - fweight <= 0.0001);
                }
            }
            nowTime = System.currentTimeMillis();
            Thread.sleep(1000);
        } while ((!isExceptWeight) && (nowTime - startTime <= 10000));
        Assert.assertTrue(isExceptWeight);
        HttpUtilTests.delete(consumerAppkey, "thrift", ip1, port);
        HttpUtilTests.delete(consumerAppkey, "thrift", ip2, port);

        Thread.sleep(5000);
    }

}
