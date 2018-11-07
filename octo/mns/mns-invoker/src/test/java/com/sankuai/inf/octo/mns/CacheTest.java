package com.sankuai.inf.octo.mns;

import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.ProtocolResponse;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhmily on 08/26/2016.
 */
public class CacheTest {
    private String localAppkey = "com.sankuai.octo.yangjie";
    private String remoteAppkey = "com.sankuai.octo.tmy";

    @Test
    public void testCount() {
        String key1 = "com.sankuai.waimai.contract";
        String key2 = "com.sankuai.waimai.audit";
        ProtocolRequest req = new ProtocolRequest();
        req.setProtocol("thrift")
                .setLocalAppkey(key1)
                .setRemoteAppkey(key2);
        req.setServiceName("com.sankuai.meituan.waimai.thrift.service.WmAuditTaskDataService");
        List<SGService> list = MnsInvoker.getSGServiceList(key1, key2);
        System.out.println(list.size());

        List<SGService> list2 = MnsInvoker.getServiceList(req);
        System.out.println(list2.size());

        //System.out.println(req);
        //req.unsetRemoteAppkey();
        List<SGService> list3 = MnsInvoker.getServiceList(req);
        System.out.println(list3.size());
        MnsInvoker.getServerList(key1, key2);
    }

    @Test
    public void getServiceListByProtocol() throws TException, InterruptedException {
//        InvokeProxy.setIsMock(true);
//        ProtocolRequest req = new ProtocolRequest();
//        req.setProtocol("thrift")
//                .setLocalAppkey(localAppkey)
//                .setRemoteAppkey(remoteAppkey);

//        testNull(req, 0);
//        testEmpty(req, 0);
//        testNotEmpty(req, 0, 1);
//        //模拟sg_agent获取异常
//        testNull(req, 1);
//        testNotEmpty(req, 0, 1);
//        testNotEmpty(req, -1, 1);
//        testEmpty(req, 0);
//        testNotEmpty(req, -1, 0);

//        InvokeProxy.setIsMock(false);
    }

    private void testNotEmpty(ProtocolRequest req, int errorCode, int size) throws InterruptedException {
        List<SGService> list = new ArrayList<SGService>();
        ProtocolResponse resp = new ProtocolResponse();
        SGService serviceItem = new SGService();
        serviceItem.setAppkey(remoteAppkey);
        list.add(serviceItem);
        resp.setErrcode(errorCode);
        resp.setServicelist(list);
        InvokeProxy.setMockValue(resp);
        Thread.sleep(6000);
        List<SGService> services = MnsInvoker.getServiceList(req);
        Assert.assertTrue(services.size() == size);
    }

    private void testEmpty(ProtocolRequest req, int errorCode) throws InterruptedException {
        List<SGService> list = new ArrayList<SGService>();
        ProtocolResponse resp = new ProtocolResponse();
        resp.setErrcode(errorCode);
        resp.setServicelist(list);
        InvokeProxy.setMockValue(resp);
        Thread.sleep(6000);
        Assert.assertTrue(MnsInvoker.getServiceList(req).isEmpty());
    }

    private void testNull(ProtocolRequest req, int size) throws InterruptedException {
        InvokeProxy.setMockValue(null);
        Thread.sleep(6000);
        Assert.assertTrue(MnsInvoker.getServiceList(req).size() == size);
    }

}
