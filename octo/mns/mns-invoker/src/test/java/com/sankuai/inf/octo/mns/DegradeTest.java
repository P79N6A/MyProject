package com.sankuai.inf.octo.mns;

import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import org.junit.Test;

import java.util.List;

public class DegradeTest {

    String providerAppkey = "com.sankuai.octo.testMTthrift";

    @Test
    public void testDegradeActionListAtServer() throws InterruptedException {
        for (int i = 1; i < 5; i++) {
            List<DegradeAction> list = MnsInvoker.getDegradeActionListAtServer(providerAppkey);
            System.out.println(list);
            Thread.sleep(100);
        }
    }
}
