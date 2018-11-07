package com.sankuai.octo.detector;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.octo.Common;
import com.sankuai.octo.falcon.FalconItem;
import com.sankuai.octo.falcon.Item;
import com.sankuai.octo.falcon.ReportUtils;
import com.sankuai.octo.util.ScheduleTaskFactory;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ScanServiceImpl {

    private final static Logger logger = LoggerFactory.getLogger(ScanServiceImpl.class);

    public static volatile boolean slowStart = true;
    public static volatile boolean emergencySwitch = false;
    public static volatile boolean fusing = false;
    public static volatile double FUSINGNUM = 95.0;
    public static volatile boolean useScannerHeartbeat = false;

    private static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1, new ScheduleTaskFactory("Falcon-Schedule"));

    static {
        scheduExec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getScannerConfig();
                    double rate = 100.0;
                    if (FalconItem.providerTotalNum.get() != 0)
                        rate = (FalconItem.providerTotalNum.get() - FalconItem.providerFailNum.get()) / (double) FalconItem.providerTotalNum.get() * 100;
                    List<Item> list = new ArrayList<Item>();
                    list.add(new Item("scanner.provider.successRate", String.valueOf(rate), System.currentTimeMillis()));
                    list.add(new Item("scanner.provider.totalNum", String.valueOf(FalconItem.providerTotalNum.get()), System.currentTimeMillis()));
                    list.add(new Item("scanner.provider.failNum", String.valueOf(FalconItem.providerFailNum.get()), System.currentTimeMillis()));
                    ReportUtils.doIOWrite(list, 60);

                    if (Double.compare(rate, FUSINGNUM) < 0)
                        fusing = true;

                    if (fusing && Double.compare(rate, 100.0) == 0)
                        fusing = false;

                    FalconItem.providerTotalNum = new AtomicInteger(0);
                    FalconItem.providerFailNum = new AtomicInteger(0);
                } catch (Exception e) {
                    logger.error("scheduleAtFixedRate exception", e);
                }
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public static void getScannerConfig() {
        String result = null;
        try {
            result = MnsInvoker.getConfig(Common.appkey);
        } catch (TException e) {
            logger.error("exception while MnsInvoker.getConfig by appkey: {}", Common.appkey, e);
        }
        //result: {"ret":0,"msg":"success","data":{"excludedAppkeys":"appkey1,appkey2,appkey3"},"version":"1181116538095"}
        JSONObject jo = JSON.parseObject(result);
        if (jo != null && jo.getJSONObject("data") != null) {

            String slowStartStr = (String) jo.getJSONObject("data").get("slowStart");
            if (slowStartStr != null && slowStartStr.equals("true"))
                slowStart = true;
            else if (slowStartStr != null && slowStartStr.equals("false"))
                slowStart = false;

            String emergencySwitchStr = (String) jo.getJSONObject("data").get("emergencySwitch");
            if (emergencySwitchStr != null && emergencySwitchStr.equals("true"))
                emergencySwitch = true;
            else if (emergencySwitchStr != null && emergencySwitchStr.equals("false"))
                emergencySwitch = false;

            String useScannerHeartbeatStr = (String) jo.getJSONObject("data").get("useScannerHeartbeat");
            if (useScannerHeartbeatStr != null && useScannerHeartbeatStr.equals("true"))
                useScannerHeartbeat = true;
            else
                useScannerHeartbeat = false;

        } else {
            slowStart = true;
            emergencySwitch = false;
            useScannerHeartbeat = false;
        }

    }

}
