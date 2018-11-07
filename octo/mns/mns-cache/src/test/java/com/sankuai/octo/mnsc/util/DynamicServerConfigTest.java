package com.sankuai.octo.mnsc.util;

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.octo.mnsc.SpringBaseTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicServerConfigTest extends SpringBaseTest {
    private static Logger LOG = LoggerFactory.getLogger(DynamicServerConfigTest.class);

    @Test
    public void testDynamicServerConfig(){
        String jettyAppkey = System.getProperty("jetty.appkey");
        LOG.info("jetty.appkey = {}", System.getProperty("jetty.appkey"));

        Assert.assertNotNull(jettyAppkey);
        String hostName = ProcessInfoUtil.getHostNameInfoByIp();
        if(StringUtils.contains(hostName, "colosseomnsc")){
            Assert.assertTrue(jettyAppkey.equals("com.sankuai.inf.octo.colosseomnsc"));
        }else{
            Assert.assertTrue(jettyAppkey.equals("com.sankuai.inf.mnsc"));
        }
    }
}
