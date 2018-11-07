package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.inf.octo.mns.util.HttpUtilTests;
import com.sankuai.inf.octo.mns.util.SGServiceUtilTests;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterTests {

    String providerAppkey = "com.sankuai.octo.tmy";
    String localAppkey = "com.sankuai.octo.yangjie";
    int port = 9002;
    private static String localIP = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();

    @Test
    public void register() throws TException, InterruptedException {
        SGService service = SGServiceUtilTests.getDefaultSGService(providerAppkey, 5198, true);
        service.setProtocol("cellar");
        MnsInvoker.registerService(service);
        Thread.sleep(5000);
        service.setProtocol("thrift");
        MnsInvoker.registerService(service);
        Thread.sleep(10000);
    }


    @Test
    public void testRegister() throws TException, InterruptedException, IOException {
        String localIp = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();
        final ServerSocket serverSocket = new ServerSocket(port);
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("read : " + socket.toString());
//                        socket.close();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
            }

        }.start();

        MnsInvoker.registerThriftService(providerAppkey, port);
        Thread.sleep(30000);
        List<SGService> providerList = MnsInvoker.getSGServiceList(localAppkey, providerAppkey);


        Assert.assertNotNull(providerList);
        boolean exist = false;
        int status = -1;
        for (SGService sgService : providerList) {
            if (sgService.getIp().equals(localIp) && sgService.getPort() == port) {
                exist = true;
                status = sgService.getStatus();
            }
        }

        Assert.assertTrue(exist);

        try {
            serverSocket.close();
        } catch (SocketException e) {

        }

        MnsInvoker.unRegisterThriftService(providerAppkey, port);
        Thread.sleep(15000);

        providerList = MnsInvoker.getSGServiceList(localAppkey, providerAppkey);
        Assert.assertNotNull(providerList);
        for (SGService sgService : providerList) {
            if (sgService.getIp().equals(localIp) && sgService.getPort() == port) {
                exist = true;
                status = sgService.getStatus();
            }
        }

        Assert.assertTrue(exist);
    }

    @Test
    public void testServiceName() throws InterruptedException, TException {
        int port = 10002;
        String serviceName = "http://mns.sankuai.com";
        HttpUtilTests.delete(providerAppkey, "thrift", localIP, port);
        SGService service = SGServiceUtilTests.getDefaultSGService("com.sankuai.octo.tmy", port, true);
        Map<String, ServiceDetail> serviceNames = new HashMap<String, ServiceDetail>();
        ServiceDetail detail = new ServiceDetail();
        detail.setUnifiedProto(true);
        serviceNames.put(serviceName, detail);
        service.setServiceInfo(serviceNames);
        MnsInvoker.registerService(service);
        Thread.sleep(10000);
        ProtocolRequest request = new ProtocolRequest();
        request.setProtocol("thrift")
                .setLocalAppkey(localAppkey)
                .setServiceName(serviceName);
        List<SGService> list = MnsInvoker.getServiceList(request);
        boolean isExist = false;
        for (SGService item : list) {
            if (localIP.equals(item.getIp()) && port == item.getPort() && item.getServiceInfo().containsKey(serviceName)) {
                isExist = true;
            }
        }
        Assert.assertTrue(isExist);
    }
}
