package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.util.*;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import junit.framework.Assert;
import org.apache.thrift.TException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhmily on 02/16/2017.
 */
public class SwimlaneTests {
    private static final Logger LOG = LoggerFactory.getLogger(SwimlaneTests.class);
    private static String localAppkey = "com.sankuai.octo.yangjie";
    private static String remoteAppkey = "com.sankuai.octo.tmy";
    private static int port = 10000;
    private static String localIP = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();

    @Test
    public void testOsName() {
        String osName = System.getProperty("os.name");
        LOG.info("os.name = {}", osName);
        LOG.info("{}", CommonUtil.containsIgnoreCase(osName, "mac os"));
    }

    @Test
    public void testRegister() throws InterruptedException, TException {
        List<String> ipPorts = new ArrayList<String>();
        ipPorts.add(ProcessInfoUtil.getLocalIpV4() + ":" + port);
        delete("thrift", ipPorts);
        String swimlane = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getSwimlane();
        MnsInvoker.registerThriftService(remoteAppkey, port);
        Thread.sleep(10000);
        ProtocolRequest request = new ProtocolRequest();
        request.setProtocol("thrift")
                .setRemoteAppkey(remoteAppkey)
                .setLocalAppkey(localAppkey);
        List<SGService> list = MnsInvoker.getServiceList(request);
        boolean isValid = false;
        for (SGService service : list) {
            if (ProcessInfoUtil.getLocalIpV4().equals(service.getIp()) && port == service.getPort()) {
                isValid = (CommonUtil.isBlankString(swimlane) && CommonUtil.isBlankString(service.getSwimlane()))
                        || swimlane.equals(service.getSwimlane());
            }
        }
        Assert.assertTrue(isValid);
        delete("thrift", ipPorts);
    }


    private void delete(String protocol, List<String> ipPorts) throws InterruptedException {
        String url = "http://mns.test.sankuai.info/api/providers/delete";
        String data = HttpUtilTests.getDeletedProviderJson(remoteAppkey, protocol, ipPorts, 3);
        HttpUtilTests.delete(url, data);
        Thread.sleep(10000);
    }

    @Test
    public void getService() throws InterruptedException, TException {
        List<String> ipPorts = new ArrayList<String>();
        ipPorts.add(ProcessInfoUtil.getLocalIpV4() + ":" + port);
        delete("thrift", ipPorts);
        String swimlane = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getSwimlane();
        MnsInvoker.registerThriftService(remoteAppkey, port);
        Thread.sleep(10000);
        ProtocolRequest request = new ProtocolRequest();
        request.setProtocol("thrift")
                .setRemoteAppkey(remoteAppkey)
                .setLocalAppkey(localAppkey);
        List<SGService> list = MnsInvoker.getServiceList(request);
        for (SGService service : list) {
            if (!CommonUtil.isBlankString(swimlane)) {
                Assert.assertTrue(swimlane.equals(service.getSwimlane()));

            }
        }

        if (CommonUtil.isBlankString(swimlane)) {
            for (SGService service : list) {
                Assert.assertTrue(CommonUtil.isBlankString(service.getSwimlane()));
            }
        }
    }


//    @Test
//    public void test1() throws InterruptedException, TException {
//        CustomizedManager.setCustomizedSGAgents("10.4.244.115:5266");
//
//        SGService service =SGServiceUtilTests.getDefaultSGService("com.sankuai.flow.diversion.test1",8022,false);
//        service.setIp("10.5.235.54");
//        MnsInvoker.registerService(service);
//        Thread.sleep(10000);
//        service.setAppkey("com.sankuai.flow.diversion.test2")
//                .setPort(8024);
//        MnsInvoker.registerService(service);
//        Thread.sleep(10000);
//    }

//    only for jprofiler tests the number of SGService
//    @Test
//    public void test2() throws InterruptedException, TException {
//        CustomizedManager.setCustomizedSGAgents("10.4.244.115:5266");
//        //use jprofiler to test the number of SGService
//        List<String> serviceNames = new ArrayList<String>();
//        for (int i = 0; i < 1000; ++i) {
//            serviceNames.add(String.valueOf(System.currentTimeMillis()));
//        }
//        ProtocolRequest request = new ProtocolRequest();

//        while (true) {
//            for (String serviceName : serviceNames) {
//                request.setLocalAppkey(localAppkey)
//                        .setRemoteAppkey(remoteAppkey)
//                        .setServiceName(serviceName)
//                        .setProtocol("thrift");
//                List<SGService> list = MnsInvoker.getServiceList(request);
//                System.out.println("withservicename " + list.size());
//                request.unsetServiceName();
//                list = MnsInvoker.getServiceList(request);
//                System.out.println("all " + list.size());
//            }
//            Thread.sleep(3000);
//        }
//    }

}
