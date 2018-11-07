package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.util.ScheduleTaskFactory;
import com.sankuai.octo.config.model.ConfigFile;
import com.sankuai.octo.config.model.file_param_t;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGAgent;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import sun.security.krb5.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhangzhitong
 * @created 1/5/16
 */
public class InvokeProxyTests {

    @Test
    public void testProxy() {
        InvokeProxy invoker = new InvokeProxy(SGAgentClient.ClientType.temp);
        try {
            SGAgent.Iface proxy = invoker.getProxy();
            List<SGService> serviceList = proxy.getServiceList("", "com.sankuai.inf.logCollector");
            System.out.println(serviceList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // please use jprofiler to run this test case. be careful the memory of the byte[] and full gc.
    @Test
    public void multiThreadProxy() throws Exception {
        final SGAgent.Iface multiProtocolClient = new InvokeProxy(SGAgentClient.ClientType.multiProto).getProxy();
        for (int i = 0; i < 5; ++i) {
            Thread th1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    file_param_t param = new file_param_t();
                    param.setAppkey("com.sankuai.octo.tmy");
                    List<ConfigFile> files = new ArrayList<ConfigFile>();
                    ConfigFile configFile = new ConfigFile();
                    configFile.setFilename("settings.xml");
                    param.setConfigFiles(files);
                    while (true) {
                        try {
                            multiProtocolClient.getFileConfig(param);

                            Thread.sleep(10);
                        } catch (Exception e) {
                            Assert.assertTrue(false);
                        }

                    }


                }
            });

            Thread th2 = new Thread(new Runnable() {
                @Override
                public void run() {

                    ProtocolRequest req = new ProtocolRequest();
                    req.setProtocol("thrift")
                            .setRemoteAppkey("com.sankuai.inf.mnsc");
                    while (true) {
                        try {
                            multiProtocolClient.getServiceListByProtocol(req);
                            Thread.sleep(10);
                        } catch (Exception e) {
                            Assert.assertTrue(false);
                        }

                    }


                }
            });
            th1.start();
            th2.start();
        }

        Thread.sleep(1000 * 120);
    }
}
