package com.sankuai.msgp.common.utils;

import com.sankuai.msgp.common.model.FalconItem;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AlarmSender {
    private static final Logger logger = LoggerFactory.getLogger(AlarmSender.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(20);

    private static final String FALCON_STR_ALARM_URL = "http://proxy.transfer.falcon.vip.sankuai.com:6061/api/push";
    // 测试URL "http://yf-inf-falcon-judge13:6061/api/push";

    private static AtomicInteger count = new AtomicInteger(0);

    public static void send(String alarmNode, String metric, String alarmMsg) {
        FalconItem falconItem = new FalconItem(alarmNode, metric, System.currentTimeMillis() / 1000,
                alarmMsg, "location=beijing,service=octo");
        executor.submit(new SenderTask(falconItem));
    }

    private static class SenderTask implements Callable<SenderTask> {
        private FalconItem falconItem;

        public SenderTask(FalconItem falconItem) {
            this.falconItem = falconItem;
        }

        // send monitor string to falcon
        public static void falconSend(List<FalconItem> falconItem) {
            try {
                logger.debug("send to falcon " + falconItem.get(0).getEndpoint() + " " + falconItem.get(0).getValue());
                int id = count.incrementAndGet();
                String jsonParam = JsonHelper.jsonStr(falconItem);
                if (id % 1000 == 0) {
                    logger.info("params " + JsonHelper.jsonStr(falconItem));
                }
                String result = HttpUtil.httpPostRequest(FALCON_STR_ALARM_URL, jsonParam);
                if (StringUtils.isBlank(result)) {
                    logger.error("falcon send failed {}", jsonParam);
                } else {
                    if (!result.contains("\"msg\":\"success\"")) {
                        logger.error("falcon send failed: {} {}", result, jsonParam);
                    } else {
                        logger.debug("falcon send success: {}", jsonParam);
                    }
                }
            } catch (Exception ex) {
                logger.warn("falcon send data failed", ex);
            }
        }

        @Override
        public SenderTask call() throws Exception {
            logger.debug("begin send to zabbix and falcon");
            List<FalconItem> list = new ArrayList<>();
            list.add(falconItem);
            falconSend(list);
            return this;
        }
    }

    // test Falcon alarm
    public static void main(String[] args) throws Exception {
        FalconItem item = new FalconItem("com.sankuai.inf.octo.errorlog", "sg.custom.error.status",System.currentTimeMillis() / 1000,
                "报警测试", "location=beijing,service=octo");
        List<FalconItem> list = new ArrayList<>();
        list.add(item);
        SenderTask.falconSend(list);
    }
}
