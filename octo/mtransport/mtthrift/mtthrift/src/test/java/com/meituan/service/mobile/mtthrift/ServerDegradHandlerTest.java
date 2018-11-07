package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.degrage.ServerDegradHandler;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import org.junit.Test;

import java.util.List;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/4/6
 * Description:
 */
public class ServerDegradHandlerTest {
    //private String appkey = "com.sankuai.octo.testMTthrift";
    private String appkey = "com.sankuai.inf.msgp";

    @Test
    public void getDegradeAction() throws InterruptedException {
        ServerDegradHandler handler = new ServerDegradHandler("", appkey);

        for(int i = 0; i < 20; i++) {
            handler.getDegradeActionsByAgent(appkey);
            List<DegradeAction> actions = handler.getDegradeActions();
            for(DegradeAction action : actions){
                System.out.println(action);
            }
            System.out.println("----------");
            Thread.sleep(5000);
        }

    }
}
