package com.sankuai.octo.oswatch.task;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sankuai.octo.oswatch.model.OctoEnv;
import com.sankuai.octo.oswatch.service.LogCollectorService;
import com.sankuai.octo.oswatch.service.PerfService;
import com.sankuai.octo.oswatch.thrift.data.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenxi on 6/10/15.
 */
public class QuotaWatchTaskTest {
    ProviderQuota quota = new ProviderQuota();
    ConsumerQuota consumerA = new ConsumerQuota();
    ConsumerQuota consumerB = new ConsumerQuota();
    ConsumerQuota consumerC = new ConsumerQuota();

    @Before
    public void init() {
        List<ConsumerQuota> consumerList = new ArrayList<ConsumerQuota>();

        consumerList.add(consumerA);
        consumerA.setConsumerAppkey("com.octo.consumerA.key")
                .setDegradeRedirect("providerBackA")
                .setDegradeStrategy(DegradeStrategy.findByValue(2))
                .setQPSRatio(0.3);

        consumerList.add(consumerB);
        consumerB.setConsumerAppkey("com.octo.consumerB.key")
                .setDegradeRedirect("http://www.meituan.com")
                .setDegradeStrategy(DegradeStrategy.findByValue(1))
                .setQPSRatio(0.2);

        consumerList.add(consumerC);
        consumerC.setConsumerAppkey("com.octo.consumerC.key")
                .setDegradeRedirect(null)
                .setDegradeStrategy(DegradeStrategy.findByValue(0))
                .setQPSRatio(0.5);

        quota.setConsumerList(consumerList)
                .setCreateTime(System.currentTimeMillis())
                .setEnv(1)
                .setId("providerId")
                .setMethod("all")
                .setName("test")
                .setProviderAppkey("com.octo.provider.key")
                .setQPSCapacity(70000)
                .setStatus(DegradeStatus.ENABLE)
                .setUpdateTime(System.currentTimeMillis())
                .setWatchPeriodInSeconds(3);
    }


    @Test
    public void checkWithTimeInvalid() {
        QuotaWatchTask task = new QuotaWatchTask(quota, "", null, System.currentTimeMillis(), System.currentTimeMillis());
        try {
            List<DegradeAction> actions = task.call().getActions();
            assertTrue(actions.isEmpty());
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    @Test
    public void checkWithQPSCapacity() {
        long current = System.currentTimeMillis();
        long last = current - quota.watchPeriodInSeconds * PerfService.ONE_SECOND_IN_MS;

        LogCollectorService perfServiceClient = mock(LogCollectorService.class);

        Map<String, Double> qpsMap =  new HashMap<String, Double>();
        qpsMap.put(consumerA.getConsumerAppkey(), 15000.0);
        qpsMap.put(consumerB.getConsumerAppkey(), 20000.0);
        qpsMap.put(consumerC.getConsumerAppkey(), 25000.0);
        when(perfServiceClient.getCurrentQPS( quota.providerAppkey, quota.getMethod(), OctoEnv.getEnv(1), current, quota.watchPeriodInSeconds)).thenReturn(qpsMap);

        QuotaWatchTask task = new QuotaWatchTask(quota, "", null, current, last);
        task.setLogCollectorService(perfServiceClient);
        try {
            List<DegradeAction> actions = task.call().getActions();
            assertTrue(actions.isEmpty());
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    @Test
    public void triggerDegradeWithOneConsumer() {
        long current = System.currentTimeMillis();
        long last = current - quota.watchPeriodInSeconds * PerfService.ONE_SECOND_IN_MS;

        LogCollectorService perfServiceClient = mock(LogCollectorService.class);

        Map<String, Double> qpsMap =  new HashMap<String, Double>();
        qpsMap.put(consumerA.getConsumerAppkey(), 45000.0);
        qpsMap.put(consumerB.getConsumerAppkey(), 10000.0);
        qpsMap.put(consumerC.getConsumerAppkey(), 25000.0);
        when(perfServiceClient.getCurrentQPS(quota.providerAppkey, quota.getMethod(), OctoEnv.getEnv(1), current, quota.watchPeriodInSeconds)).thenReturn(qpsMap);

        QuotaWatchTask task = new QuotaWatchTask(quota, "", null, current, last);
        task.setLogCollectorService(perfServiceClient);
        try {
            List<DegradeAction> actions = task.call().getActions();
            assertEquals(actions.size(), 1);

            DegradeAction action = actions.get(0);
            double consumerADegradeRation = (45000 - consumerA.getQPSRatio() * quota.QPSCapacity) / 45000;

            assertEquals(action.getConsumerAppkey(), consumerA.getConsumerAppkey());
            assertEquals(action.getDegradeRedirect(), consumerA.getDegradeRedirect());
            assertEquals(action.getDegradeStrategy(), consumerA.getDegradeStrategy());
            assertEquals(action.getEnv(), quota.getEnv());
            assertEquals(action.getId(), consumerA.getConsumerAppkey()+'/'+quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getProviderAppkey(), quota.getProviderAppkey());
            assertEquals(action.getDegradeRatio(), consumerADegradeRation, 0.0);
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    @Test
    public void triggerDegradeWithMoreConsumer() {
        long current = System.currentTimeMillis();
        long last = current - quota.watchPeriodInSeconds * PerfService.ONE_SECOND_IN_MS;

        LogCollectorService perfServiceClient = mock(LogCollectorService.class);
        Map<String, Double> qpsMap =  new HashMap<String, Double>();
        qpsMap.put(consumerA.getConsumerAppkey(), 45000.0);
        qpsMap.put(consumerB.getConsumerAppkey(), 50000.0);
        qpsMap.put(consumerC.getConsumerAppkey(), 60000.0);

        when(perfServiceClient.getCurrentQPS(quota.getProviderAppkey(), quota.getMethod(), OctoEnv.getEnv(1), current, quota.watchPeriodInSeconds)).thenReturn(qpsMap);

        QuotaWatchTask task = new QuotaWatchTask(quota, "", null, current, last);
        task.setLogCollectorService(perfServiceClient);
        try {
            List<DegradeAction> actions = task.call().getActions();
            assertEquals(actions.size(), 3);

            double consumerADegradeRation = (45000 - consumerA.getQPSRatio() * quota.QPSCapacity) / 45000;
            double consumerBDegradeRation = (50000 - consumerB.getQPSRatio() * quota.QPSCapacity) / 50000;
            double consumerCDegradeRation = (60000 - consumerC.getQPSRatio() * quota.QPSCapacity) / 60000;

            DegradeAction action = actions.get(0);
            assertEquals(action.getConsumerAppkey(), consumerA.getConsumerAppkey());
            assertEquals(action.getDegradeRedirect(), consumerA.getDegradeRedirect());
            assertEquals(action.getDegradeStrategy(), consumerA.getDegradeStrategy());
            assertEquals(action.getEnv(), quota.getEnv());
            assertEquals(action.getId(), consumerA.getConsumerAppkey()+'/'+quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getProviderAppkey(), quota.getProviderAppkey());
            assertEquals(action.getDegradeRatio(), consumerADegradeRation, 0.0);

            action = actions.get(1);
            assertEquals(action.getConsumerAppkey(), consumerB.getConsumerAppkey());
            assertEquals(action.getDegradeRedirect(), consumerB.getDegradeRedirect());
            assertEquals(action.getDegradeStrategy(), consumerB.getDegradeStrategy());
            assertEquals(action.getEnv(), quota.getEnv());
            assertEquals(action.getId(), consumerB.getConsumerAppkey()+'/'+quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getProviderAppkey(), quota.getProviderAppkey());
            assertEquals(action.getDegradeRatio(), consumerBDegradeRation, 0.0);

            action = actions.get(2);
            assertEquals(action.getConsumerAppkey(), consumerC.getConsumerAppkey());
            assertEquals(action.getDegradeRedirect(), consumerC.getDegradeRedirect());
            assertEquals(action.getDegradeStrategy(), consumerC.getDegradeStrategy());
            assertEquals(action.getEnv(), quota.getEnv());
            assertEquals(action.getId(), consumerC.getConsumerAppkey()+'/'+quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getProviderAppkey(), quota.getProviderAppkey());
            assertEquals(action.getDegradeRatio(), consumerCDegradeRation, 0.0);

        } catch (Exception e) {
            System.out.println("error");
        }
    }

    @Test
    public void ConfigFewConsumerQPSQuota() {
        long current = System.currentTimeMillis();
        long last = current - quota.watchPeriodInSeconds * PerfService.ONE_SECOND_IN_MS;

        consumerA.setQPSRatio(0.1);
        consumerB.setQPSRatio(0.2);
        consumerC.setQPSRatio(0.1);

        quota.setQPSCapacity(1000);
        LogCollectorService perfServiceClient = mock(LogCollectorService.class);
        Map<String, Double> qpsMap =  new HashMap<String, Double>();
        qpsMap.put(QuotaWatchTask.ALL_QPS_KEY, 2200.0);
        qpsMap.put(consumerA.getConsumerAppkey(), 400.0);
        qpsMap.put(consumerB.getConsumerAppkey(), 300.0);
        qpsMap.put(consumerC.getConsumerAppkey(), 500.0);
        qpsMap.put("consumerD", 200.0);
        qpsMap.put("consumerE", 200.0);
        qpsMap.put("consumerF", 200.0);
        qpsMap.put("consumerG", 200.0);
        qpsMap.put("consumerH", 200.0);

        when(perfServiceClient.getCurrentQPS(quota.getProviderAppkey(), quota.getMethod(), OctoEnv.getEnv(1), current, quota.watchPeriodInSeconds)).thenReturn(qpsMap);

        QuotaWatchTask task = new QuotaWatchTask(quota, "", null, current, last);
        task.setLogCollectorService(perfServiceClient);
        try {
            List<DegradeAction> actions = task.call().getActions();
            assertEquals(actions.size(), 4);

            double consumerADegradeRation = (400 - consumerA.getQPSRatio() * quota.QPSCapacity) / 400;
            double consumerBDegradeRation = (300 - consumerB.getQPSRatio() * quota.QPSCapacity) / 300;
            double consumerCDegradeRation = (500 - consumerC.getQPSRatio() * quota.QPSCapacity) / 500;
            double consumerXDegradeRation = (1000 - 600) / 1000.0;

            DegradeAction action = actions.get(0);
            assertEquals(action.getConsumerAppkey(), consumerA.getConsumerAppkey());
            assertEquals(action.getId(), consumerA.getConsumerAppkey()+'/'+quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getDegradeRatio(), consumerADegradeRation, 0.0);

            action = actions.get(1);
            assertEquals(action.getConsumerAppkey(), consumerB.getConsumerAppkey());
            assertEquals(action.getId(), consumerB.getConsumerAppkey()+'/'+quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getDegradeRatio(), consumerBDegradeRation, 0.0);

            action = actions.get(2);
            assertEquals(action.getConsumerAppkey(), consumerC.getConsumerAppkey());
            assertEquals(action.getId(), consumerC.getConsumerAppkey()+'/'+quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getDegradeRatio(), consumerCDegradeRation, 0.0);

            action = actions.get(3);
            assertEquals(action.getConsumerAppkey(), QuotaWatchTask.DEFAULT_CONSUMER_KEY);
            assertEquals(action.getId(), QuotaWatchTask.DEFAULT_CONSUMER_KEY + '/' + quota.getMethod()+'/'+quota.getId());
            assertEquals(action.getMethod(), quota.getMethod());
            assertEquals(action.getDegradeRatio(), consumerXDegradeRation, 0.0);
        } catch (Exception e) {
            System.out.println("error");
        }
    }
}
