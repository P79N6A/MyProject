package com.sankuai.inf.octo.mns.sentinel;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.inf.octo.mns.sentinel.SentinelManager;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SentinelTests {
    @Test
    public void getSentienlList() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String strSentinelList = SentinelManager.getSentinelAgentList().toString();
                    System.out.println(strSentinelList);
                    assertNotNull(strSentinelList);
                    assertTrue(strSentinelList.contains(Consts.sg_sentinelAppkey));
                }
            });
            thread.start();
        }
        Thread.sleep(1000);
    }
}
