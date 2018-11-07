package com.sankuai.inf.octo.mns;

import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.List;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/1/4
 * Description:
 */
public class TestServerList {
    @Test
    public void getServerList() {
        String remoteAppkey = "com.sankuai.octo.mtthrift.demo.benchmark";
        List<SGService> list = MnsInvoker.getSGServiceList("", remoteAppkey, false);
        System.out.println(list);
    }

    @Test
    public void testRequestisvalid() {
        ProtocolRequest req = new ProtocolRequest();
        req.setServiceName("sayHello");
        if (StringUtils.isNotBlank(req.getRemoteAppkey())) {
            req.setRemoteAppkey(req.getRemoteAppkey().trim());
        }
        if (StringUtils.isNotBlank(req.getServiceName())) {
            req.setServiceName(req.getServiceName().trim());
        }
    }
}
