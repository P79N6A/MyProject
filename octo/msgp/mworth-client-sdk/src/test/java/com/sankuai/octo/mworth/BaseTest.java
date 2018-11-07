package com.sankuai.octo.mworth;

import com.sankuai.octo.mworth.common.model.OperationSourceType;
import com.sankuai.octo.mworth.common.model.WorthEvent;
import com.sankuai.octo.mworth.service.WorthEventSevice;
import com.sankuai.octo.mworth.service.impl.WorthEventServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuexiaojun on 14/10/30.
 */
public class BaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    protected static WorthEventSevice worthEventSevice;
    protected static String host = "http://localhost:8080";
    protected static String appkey = "test";
    protected static String secret = "test";


    @BeforeClass
    public static void init() {
        LOG.info("init");
        worthEventSevice = new WorthEventServiceImpl(host, appkey, secret);
    }

    @Test
    public void test() {

        for (int i = 0; i < 100; i++) {
            WorthEvent worthEvent = newWorthEvent(i);
            worthEventSevice.saveAsyn(worthEvent);
            worthEventSevice.save(worthEvent);
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public static WorthEvent newWorthEvent(int i) {
        WorthEvent event = new WorthEvent();
        event.setProject("test");
        event.setModel("test");
        event.setFunctionName("test");
        event.setEndTime(System.currentTimeMillis());
        event.setStartTime(System.currentTimeMillis() - 1000 * i);
        event.setOperationSource("127.0.0." + i);
        event.setOperationSourceType(OperationSourceType.HUMAN);
        event.setTargetAppkey("test");
        event.setSignid("");
        return event;
    }


}
