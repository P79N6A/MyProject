package com.sankuai.inf.octo.mns;

import com.sankuai.sgagent.thrift.model.ConfigStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by lhmily on 12/29/2016.
 */
public class HttpServerPublisherTests {
    ConfigStatus configStatus;
    @Test
    public void testConfigStatus(){
        Assert.assertNull(configStatus);
        configStatus = new ConfigStatus();
        Assert.assertNull(configStatus.initStatus);
//        System.out.println(configStatus.initStatus);
    }
}
