package com.sankuai.inf.octo.mns;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTests {
    private String appkey = "com.sankuai.octo.tmy";

    @Test
    public void testGetConfigSGAgentShutDownAndUp() throws InterruptedException, TException {
        System.out.println("【testGetConfigSGAgentShutDownAndUp】start");

        for (int i = 0; i < 10; i++) {
            String config = MnsInvoker.getConfig(appkey);
            System.out.println("data = " + config);
            Thread.sleep(100);
        }
    }

    @Test
    public void testGetConfig() throws InterruptedException, TException {
        System.out.println("【testGetConfigSGAgentShutDownAndUp】start");
        String fileName = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            byte[] nullByes = MnsInvoker.getFileConfig(appkey, fileName);
            Assert.assertNull(nullByes);
        }
    }

}
