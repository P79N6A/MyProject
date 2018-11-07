package com.sankuai.inf.octo.mns.util;

import com.sankuai.sgagent.thrift.model.SGService;

/**
 * Created by lhmily on 06/22/2016.
 */
public class SGServiceUtilTests {
    private static String localIp = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();

    public static SGService getDefaultSGService(final String appkey, final int port, final boolean isThrift) {
        String protocol = isThrift ? "thrift" : "http";
        SGService service = new SGService();
        service.setAppkey(appkey);
        service.setPort(port);
        service.setVersion(isThrift ? "original" : "HLB");
        service.setIp(localIp);
        service.setLastUpdateTime((int) (System.currentTimeMillis() / 1000));
        service.setServerType(isThrift ? 0 : 1);
        service.setWeight(10);
        service.setFweight(10.d);
        String extend = "OCTO|slowStartSeconds:180";
        service.setExtend(extend);
        service.setProtocol(protocol);
        return service;
    }
}
