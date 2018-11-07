package com.sankuai.octo.msgp.service;

import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.octo.ServiceCutFlowSuite;
import com.sankuai.octo.msgp.dao.service.ServiceCutFlowDAO;
import com.sankuai.octo.msgp.service.cutFlow.CutFlowService;
import com.sankuai.octo.msgp.utils.client.ZkClient;
import com.sankuai.octo.statistic.model.DataRecord;
import com.sankuai.octo.statistic.model.Point;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2018 Meituan
 * All rights reserved
 * Description：
 * User: wuxinyu
 * Date: Created in 2018/4/26 上午11:52
 * Copyright: Copyright (c) 2018
 */
public class CutFlowServiceTest extends CutFlowService{

    private static final Logger LOGGER = LoggerFactory.getLogger(CutFlowServiceTest.class);


    private static ServiceCutFlowSuite serviceCutFlowSuite = new ServiceCutFlowSuite();

    @Test
    public void testCutFlow() throws InterruptedException {
        CutFlowService.start();
        Thread.sleep(1000000);
    }

    @Test
    public void testCutFlowService(){
        monitorCutFlow();
    }

    @Test
    public void testGetQuota() {
        long result = CutFlowService.getQuota("com.sankuai.inf.msgp",3,2000,200);
        System.out.println(result);
    }

    @Test
    public void testupdateCutFlowQuotas() {
        String json = "{\"appkey\":\"com.sankuai.inf.msgp\",\"id\":536,\"name\":\"sp\",\"method\":\"CutFlowController.doCutAck\"," +
                "\"alarmStatus\":1,\"degradeStatus\":1,\"degradeend\":0,\"env\":3,\"qpsCapacity\":0,\"watchPeriod\":0," +
                "\"ctime\":1525262994,\"utime\":1525262994,\"hostQpsCapacity\":100,\"clusterQpsCapacity\":0,\"testStatus\":0," +
                "\"consumers\":[]}";
        ServiceCutFlowDAO.updateCutFlowQuotas(json);
    }

    @Test
    public void testZKPath() {
        String path = "/mns/sankuai/prod/com.sankuai.travel.dsg.gisplus/quota/DomesticCityService.selectOpenCityAll  ";
        boolean result = ZkClient.exist(path.trim());
        System.out.println(result);
    }

    @Test
    public void testMessage() {
        Tables.AppQuotaRow appQuotaRow =  serviceCutFlowSuite.getAppQuotaRow();
        List<ServiceCutFlowDAO.CutFlowRatio> ratioStrategies = serviceCutFlowSuite.getJsonConsumerRatioHistoryRowList();
        List<String> consumerAppkeys = new ArrayList<>();
        consumerAppkeys.add("unknownService");consumerAppkeys.add("others");

        String message = CutFlowService.getAlarmMessage(appQuotaRow,ratioStrategies,consumerAppkeys);
        System.out.println(message);

        consumerAppkeys.remove("unknownService");
        message = CutFlowService.getAlarmMessage(appQuotaRow,ratioStrategies,consumerAppkeys);
        System.out.println(message);

        message = CutFlowService.getNotifyMessage(appQuotaRow,"策略");
        System.out.println(message);

        ServiceCutFlowDAO.QuotaWarning quotaWarning = serviceCutFlowSuite.getQuotaWarning();
        message = CutFlowService.getWarningMessage(appQuotaRow,quotaWarning);
        System.out.println(message);
    }

    @Test
    public void testGetQPSList() {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));
        calendar2.setTimeInMillis(System.currentTimeMillis() - 360 * 1000);
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));

        int endTime = Long.valueOf(System.currentTimeMillis() / 1000).intValue();
        int startTime = endTime - 360;

        Tables.AppQuotaRow appQuotaRow =  serviceCutFlowSuite.getAppQuotaRow();

        List<DataRecord> data = getQps(appQuotaRow,startTime,endTime);
        if(data.size() == 0)
            System.out.println("empty data");
        for(DataRecord dataRecord : data) {
            System.out.println(dataRecord.getTags().getRemoteApp());
            for(Point point : dataRecord.getQps()){
                System.out.println(point.getX() + "--" + point.getY());
            }
        }
    }

    @Test
    public void testDivideConsumerQuotaConfigs() {
        List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigRows = serviceCutFlowSuite.getConsumerQuotaConfigs();
        Map<ConsumerType, List<Tables.ConsumerQuotaConfigRow>> configMap = divideConsumerQuotaConfigs(consumerQuotaConfigRows);
        System.out.println(configMap);
    }

    @Test
    public void testDivideQpsDatas() {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));
        calendar2.setTimeInMillis(System.currentTimeMillis() - 360 * 1000);
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));

        int endTime = Long.valueOf(System.currentTimeMillis() / 1000).intValue();
        int startTime = endTime - 360;

        Tables.AppQuotaRow appQuotaRow =  serviceCutFlowSuite.getAppQuotaRow();
        List<DataRecord> data = getQps(appQuotaRow,startTime,endTime);
        List<String> consumerAppkeys = new ArrayList<>();
        consumerAppkeys.add("unknownService");
        consumerAppkeys.add("others");

        Map<QpsType, List<DataRecord>> qpsMap = divideQpsDatas(data,consumerAppkeys);
        System.out.println(qpsMap);

        consumerAppkeys = new ArrayList<>();
        qpsMap = divideQpsDatas(data,consumerAppkeys);
        System.out.println(qpsMap);
    }

    @Test
    public void testObjectToDoubleList() {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));
        calendar2.setTimeInMillis(System.currentTimeMillis() - 360 * 1000);
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));

        int endTime = Long.valueOf(System.currentTimeMillis() / 1000).intValue();
        int startTime = endTime - 360;

        Tables.AppQuotaRow appQuotaRow =  serviceCutFlowSuite.getAppQuotaRow();

        List<DataRecord> data = getQps(appQuotaRow,startTime,endTime);
        for(DataRecord record : data){
            List<Double> qpsList = ObjectToDoubleList(record);
            System.out.println(qpsList);
        }
    }

    @Test
    public void testQpsListToStr() {
        String result = qpsListToStr(new ArrayList<Double>());
        System.out.println(result);
        result = CutFlowService.qpsListToStr(Arrays.asList(2.0,3.0));
        System.out.println(result);
    }

    @Test
    public void testGetRatios() {
        Tables.AppQuotaRow appQuotaRow =  serviceCutFlowSuite.getAppQuotaRow();
        List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigRows = serviceCutFlowSuite.getConsumerQuotaConfigs();
        List<String> consumerAppkeys = new ArrayList<>();
        consumerAppkeys.add("unknownService");consumerAppkeys.add("others");

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));
        calendar2.setTimeInMillis(System.currentTimeMillis() - 360 * 1000);
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));

        int endTime = Long.valueOf(System.currentTimeMillis() / 1000).intValue();
        int startTime = endTime - 360;
        List<DataRecord> data = getQps(appQuotaRow,startTime,endTime);

        List<ServiceCutFlowDAO.CutFlowRatio> consumerRatioHistoryRows =
                getRatios(appQuotaRow,consumerQuotaConfigRows,consumerAppkeys,data,startTime,endTime);
        System.out.println(consumerRatioHistoryRows);
    }

    @Test
    public void testGetStrategy() {
        long quotaThreshold = 400;
        List<Double> qpsList = Arrays.asList(300.0,400.0,500.0,600.0);
        Strategy strategy = getStrategy(quotaThreshold,qpsList);
        System.out.println(strategy.toString());
        qpsList = Arrays.asList(300.0,400.0,500.0,600.0,402.0);
        strategy = getStrategy(quotaThreshold,qpsList);
        System.out.println(strategy.toString());
        qpsList = Arrays.asList(430.0,420.0,500.0,600.0,300.0);
        strategy = getStrategy(quotaThreshold,qpsList);
        System.out.println(strategy.toString());
    }

    @Test
    public void testHasEnoughQuota() {
        List<Tables.ConsumerQuotaConfigRow> consumerQuotaConfigRows = serviceCutFlowSuite.getConsumerQuotaConfigs();
        Tables.AppQuotaRow appQuotaRow =  serviceCutFlowSuite.getAppQuotaRow();

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));
        calendar2.setTimeInMillis(System.currentTimeMillis() - 360 * 1000);
        System.out.println(calendar2.get(Calendar.MINUTE) + ":" + calendar2.get(Calendar.SECOND));

        int endTime = Long.valueOf(System.currentTimeMillis() / 1000).intValue();
        int startTime = endTime - 360;
        List<DataRecord> data = getQps(appQuotaRow,startTime,endTime);
        List<ServiceCutFlowDAO.QuotaWarning> quotaWarnings = hasEnoughQuota(consumerQuotaConfigRows,data);
        for(ServiceCutFlowDAO.QuotaWarning quotaWarning : quotaWarnings) {
            System.out.println(quotaWarning);
        }
    }

    @Test
    public void testScheduledExecutorService() throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    long beginTime = System.currentTimeMillis();
                    Thread.sleep(30000);
                    long endTime = System.currentTimeMillis();
                    LOGGER.info("It costs {}s in one round of cutFlow", (endTime - beginTime) / 1000);
                } catch (Exception e) {
                    LOGGER.error("CutFlow checkAll failed", e);
                }
            }
        }, 0, 20, TimeUnit.SECONDS);
        Thread.currentThread().join();
    }

    @Test
    public void testRenewWarnAck() {
        ServiceCutFlowDAO.renewWarnAck(544L,Arrays.asList(""));
    }

    @Test
    public void testSubString() {
        System.out.println(",".substring(0,0));
    }
}
