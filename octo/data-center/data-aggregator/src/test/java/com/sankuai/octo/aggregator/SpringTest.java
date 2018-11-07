package com.sankuai.octo.aggregator;

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo;
import com.sankuai.octo.aggregator.thrift.service.LogCollectorService;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.thrift.TException;
import org.junit.Test;

public class SpringTest {

    @Test
    public void test2() throws Exception {
        ThriftClientProxy proxy = new ThriftClientProxy();
        proxy.setServiceInterface(LogCollectorService.class);
//        proxy.setServerIpPorts("192.168.12.164:8920");
        proxy.setAppKey("com.sankuai.inf.newct");
        proxy.setRemoteAppkey("com.sankuai.inf.logCollector");
        proxy.setTimeout(3000);
        proxy.setServerIpPorts("172.18.176.91:8920");
        //proxy.setImplFacebookService(false)
//        proxy.setStrAgentUrl("192.168.22.196:5266");
        proxy.afterPropertiesSet();
        LogCollectorService.Iface a = (LogCollectorService.Iface) proxy.getObject();

//        SGLog log = new SGLog();
//        int ret = a.uploadLog(log);
//        System.out.println(ret);
        int count = 10;
        int c = 0;
        int t_c = 0;
        while (count-- > 0) {

            try {
                SGModuleInvokeInfo info = randomInvokeInfo();
                a.uploadModuleInvoke(info);
                c++;
                t_c+=info.count;
                System.out.println(c+","+t_c);
            } catch (TException e) {
                e.printStackTrace();
            }
            Thread.sleep(10);
        }
    }

    private SGModuleInvokeInfo randomInvokeInfo() {
        SGModuleInvokeInfo info = new SGModuleInvokeInfo();
        info.setSpanName("TestController.test");
        //info.setLocalAppKey("com.sankuai.inf.sgnotify");
        info.setLocalAppKey("com.sankuai.inf.newct");
        //info.setLocalAppKey("testthriftserver");
        //info.setLocalAppKey("octotestserver");
        info.setLocalHost("testhost");
        info.setRemoteAppKey("test");
        info.setRemoteHost("192.168.2.2");
        info.setStatus(0);
        info.setCount(RandomUtils.nextInt(100));
        info.setType(1);
        info.setCost(RandomUtils.nextInt(20) + 20);
        return info;
    }
}
