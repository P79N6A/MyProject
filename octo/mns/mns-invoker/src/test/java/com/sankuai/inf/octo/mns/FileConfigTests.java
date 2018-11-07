package com.sankuai.inf.octo.mns;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/9/22
 * Description:
 */
public class FileConfigTests {
    @Test
    public void testGetFile() throws TException, InterruptedException {
        byte[] content = MnsInvoker.getFileConfig("com.sankuai.octo.tmy", "settings.xml");
        Assert.assertNotNull(content);
        Thread.sleep(5 * 1000);
    }

}
