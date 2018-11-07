package com.sankuai.mtthrift.testSuite;

import com.sankuai.mtthrift.testSuite.idlTest.Tweet;
import com.sankuai.mtthrift.testSuite.idlTest.TweetSearchResult;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.mtthrift.testSuite.idlTest.TwitterUnavailable;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-22
 * Time: 上午10:17
 */
public class WaimaiClientTest {

    private static ClassPathXmlApplicationContext waimaiThriftServiceBeanFactory;
    private static long start = System.currentTimeMillis();

    private static Twitter.Iface wmOrgCityThriftService;
    private static Twitter.Iface wmContactPointThriftService;
    private static Twitter.Iface wmAorThriftService;
    private static Twitter.Iface wmEmployThriftService;
    private static Twitter.Iface wmDistrictThriftService;
    private static Twitter.Iface wmBigDistrictThriftService;
    private static Twitter.Iface wmOpenCityThriftService;
    private static Twitter.Iface wmBuildingThriftService;
    private static Twitter.Iface wmCtrlAreaThriftService;
    private static Twitter.Iface wmBizPointThriftService;
    private static Twitter.Iface wmCityEntrancePageThriftService;
    private static Twitter.Iface wmVirtualOrgThriftService;
    private static Twitter.Iface operationThriftService;
    private static Twitter.Iface wmOrgThriftService;
    private static Twitter.Iface wmUserOperationRecordService;
    private static Twitter.Iface wmContractThriftService;
    private static Twitter.Iface wmContractAsyncThriftService;
    private static Twitter.Iface wmQuaThriftService;
    private static Twitter.Iface wmAgentContractThriftService;
    private static Twitter.Iface wmContractPoiThriftService;
    private static Twitter.Iface wmContractPoiAsyncThriftService;
    private static Twitter.Iface wmContractPoiAuditedThriftService;
    private static Twitter.Iface wmContractPoiAuditedAsyncThriftService;
    private static Twitter.Iface wmSettleThriftService;
    private static Twitter.Iface wmSettleAsyncThriftService;
    private static Twitter.Iface wmSettleAuditedThriftService;
    private static Twitter.Iface wmSettleAuditedAsyncThriftService;
    private static Twitter.Iface wmContractAuditedThriftService;
    private static Twitter.Iface wmContractAuditedAsyncThriftService;
    private static Twitter.Iface wmPoiSettleThriftService;
    private static Twitter.Iface wmPoiSettleAsyncThriftService;
    private static Twitter.Iface wmPoiSettleAuditedThriftService;
    private static Twitter.Iface wmPoiSettleAuditedAsyncThriftService;
    private static Twitter.Iface wmContractLogThriftService;
    private static Twitter.Iface wmContractLogAsyncThriftService;
    private static Twitter.Iface wmContractVersionThriftService;
    private static Twitter.Iface wmContractSignerThriftService;
    private static Twitter.Iface wmContractSignerAuditedThriftService;
    private static Twitter.Iface wmContractVersionDealThriftService;
    private static Twitter.Iface wmUnviewRecordThriftService;
    private static Twitter.Iface wmContractPlatformfeeDiscountThriftService;
    private static Twitter.Iface wmContractQualificationThriftService;
    private static Twitter.Iface wmContractQualificationAsyncThriftService;
    private static Twitter.Iface wmContractQualificationAuditedThriftService;
    private static Twitter.Iface wmContractQualificationAuditedAsyncThriftService;
    private static Twitter.Iface wmPoiPaybillService;
    private static Twitter.Iface wmPoiPaybillExecutionContextService;
    private static Twitter.Iface wmPoiPayTaskService;
    private static Twitter.Iface wmOrderSettleExtraService;
    private static Twitter.Iface wmAccountManagerService;
    private static Twitter.Iface wmPartnerSettleBillService;
    private static Twitter.Iface wmPoiTakebackService;
    private static Twitter.Iface wmPoiPaySelfService;
    private static Twitter.Iface wmPaymentRecordService;
    private static Twitter.Iface wmMoneyDelayFireService;
    private static Twitter.Iface wmSettleCardPayTaskService;
    private static Twitter.Iface wmSettleCardService;
    private static Twitter.Iface wmPoiSettleCardService;
    private static Twitter.Iface wmPoiGroupSettleCardService;
    private static Twitter.Iface wmSettleCardExecutionContextService;
    private static Twitter.Iface wmOrderPoiFoodShareService;
    private static Twitter.Iface wmPoiLogisticsFoodShareThriftService;
    private static Twitter.Iface wmPoiLogisticsShareThriftService;
    private static Twitter.Iface wmSettlePoolService;
    private static Twitter.Iface mtPaymentThriftService;
    private static Twitter.Iface wmPoiPaySettingThriftService;
    private static Twitter.Iface wmSettleContractSyncService;
    private static Twitter.Iface wmPoiShareService;
    private static Twitter.Iface wmActOrderChargeService;
    private static Twitter.Iface wmSettleService;
    private static Twitter.Iface wmSettleFlowService;
    private static Twitter.Iface wmAccountWithDrawService;
    private static Twitter.Iface wmSettle2AccountHisService;
    private static Twitter.Iface wmThirdPoiSettlementService;
    private static Twitter.Iface wmSupermarketsSettleService;
    private static Twitter.Iface wmDepositService;
    private static Twitter.Iface wmAccountWithdrawRecordFailedQueueService;
    private static Twitter.Iface wmCrowdSourcingDepositDebitService;
    private static Twitter.Iface wmCrowdSourcingDebitService;
    private static Twitter.Iface wmMoneyMonitorService;
    private static Twitter.Iface wmContractSettlePoiService;
    private static Twitter.Iface wmBillPeriodService;
    private static Twitter.Iface wmContractSettleSettingService;
    private static Twitter.Iface wmPoiTransferAccountService;
    private static Twitter.Iface wmSettleBillExtraService;
    private static Twitter.Iface wmAdDepositService;
    private static Twitter.Iface wmMealLossService;
    private static Twitter.Iface wmSettleQueryForBusinessService;
    private static Twitter.Iface wmAdjustmentService;
    private static Twitter.Iface WmAdCpcDepositService;
    private static Twitter.Iface wmActOrderSwitchService;
    private static Twitter.Iface wmActivityOrderService;
    private static Twitter.Iface wmReplicatorService;
    private static Twitter.Iface wmSettleTestService;
    private static Twitter.Iface wmAgentMoneyService;
    private static Twitter.Iface wmActOrderChargeServiceTest;
    private static Twitter.Iface wmTaskPoiThriftSubscribe;
    private static Twitter.Iface wmPoiSpThriftSubscribe;
    private static Twitter.Iface wmPoiThriftService;
    private static Twitter.Iface wmPoiTagThriftService;
    private static Twitter.Iface wmPoiAppThriftService;
    private static Twitter.Iface wmPoiStatDataThriftService;
    private static Twitter.Iface wmPoiContactThriftService;
    private static Twitter.Iface wmThirdSpAreaThriftService;
    private static Twitter.Iface wmAcctContactThriftService;
    private static Twitter.Iface wmPoiCommitmentThriftService;
    private static Twitter.Iface wmPoiQualificationInfoThriftService;
    private static Twitter.Iface wmAuditPoiThriftService;
    private static Twitter.Iface wmAppThriftService;
    private static Twitter.Iface wmThirdPoiThriftService;
    private static Twitter.Iface wmPoiThirdSpThriftService;
    private static Twitter.Iface wmPoiOplogThriftService;
    private static Twitter.Iface wmPoiQualificationThriftService;
    private static Twitter.Iface wmLogisticsThriftService;
    private static Twitter.Iface wmPoiGroupBrandThriftService;
    private static Twitter.Iface wmPoiProfileThriftService;
    private static Twitter.Iface wmPoiLogoThriftService;
    private static Twitter.Iface wmPoiAuditThriftService;
    private static Twitter.Iface wmPoiLabelThriftService;

    @BeforeClass
    public static void start() throws InterruptedException {
        waimaiThriftServiceBeanFactory = new ClassPathXmlApplicationContext("testSuite/waimaiTest/waimaiClient.xml");

        wmOrgCityThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmOrgCityThriftService");
        wmContactPointThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContactPointThriftService");
        wmAorThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAorThriftService");
        wmEmployThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmEmployThriftService");
        wmDistrictThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmDistrictThriftService");
        wmBigDistrictThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmBigDistrictThriftService");
        wmOpenCityThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmOpenCityThriftService");
        wmBuildingThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmBuildingThriftService");
        wmCtrlAreaThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmCtrlAreaThriftService");
        wmBizPointThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmBizPointThriftService");
        wmCityEntrancePageThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmCityEntrancePageThriftService");
        wmVirtualOrgThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmVirtualOrgThriftService");
        operationThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("operationThriftService");
        wmOrgThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmOrgThriftService");
        wmUserOperationRecordService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmUserOperationRecordService");
        wmContractThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractThriftService");
//        wmContractAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractAsyncThriftService");
        wmQuaThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmQuaThriftService");
        wmAgentContractThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAgentContractThriftService");
        wmContractPoiThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractPoiThriftService");
//        wmContractPoiAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractPoiAsyncThriftService");
        wmContractPoiAuditedThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractPoiAuditedThriftService");
//        wmContractPoiAuditedAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractPoiAuditedAsyncThriftService");
        wmSettleThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleThriftService");
//        wmSettleAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleAsyncThriftService");
        wmSettleAuditedThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleAuditedThriftService");
//        wmSettleAuditedAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleAuditedAsyncThriftService");
        wmContractAuditedThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractAuditedThriftService");
//        wmContractAuditedAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractAuditedAsyncThriftService");
        wmPoiSettleThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiSettleThriftService");
//        wmPoiSettleAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiSettleAsyncThriftService");
        wmPoiSettleAuditedThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiSettleAuditedThriftService");
//        wmPoiSettleAuditedAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiSettleAuditedAsyncThriftService");
        wmContractLogThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractLogThriftService");
//        wmContractLogAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractLogAsyncThriftService");
        wmContractVersionThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractVersionThriftService");
        wmContractSignerThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractSignerThriftService");
        wmContractSignerAuditedThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractSignerAuditedThriftService");
        wmContractVersionDealThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractVersionDealThriftService");
        wmUnviewRecordThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmUnviewRecordThriftService");
        wmContractPlatformfeeDiscountThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractPlatformfeeDiscountThriftService");
        wmContractQualificationThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractQualificationThriftService");
//        wmContractQualificationAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractQualificationAsyncThriftService");
        wmContractQualificationAuditedThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractQualificationAuditedThriftService");
//        wmContractQualificationAuditedAsyncThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractQualificationAuditedAsyncThriftService");
        wmPoiPaybillService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiPaybillService");
        wmPoiPaybillExecutionContextService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiPaybillExecutionContextService");
        wmPoiPayTaskService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiPayTaskService");
        wmOrderSettleExtraService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmOrderSettleExtraService");
        wmAccountManagerService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAccountManagerService");
        wmPartnerSettleBillService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPartnerSettleBillService");
        wmPoiTakebackService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiTakebackService");
        wmPoiPaySelfService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiPaySelfService");
        wmPaymentRecordService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPaymentRecordService");
        wmMoneyDelayFireService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmMoneyDelayFireService");
        wmSettleCardPayTaskService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleCardPayTaskService");
        wmSettleCardService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleCardService");
        wmPoiSettleCardService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiSettleCardService");
        wmPoiGroupSettleCardService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiGroupSettleCardService");
        wmSettleCardExecutionContextService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleCardExecutionContextService");
        wmOrderPoiFoodShareService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmOrderPoiFoodShareService");
        wmPoiLogisticsFoodShareThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiLogisticsFoodShareThriftService");
        wmPoiLogisticsShareThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiLogisticsShareThriftService");
        wmSettlePoolService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettlePoolService");
        mtPaymentThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("mtPaymentThriftService");
        wmPoiPaySettingThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiPaySettingThriftService");
        wmSettleContractSyncService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleContractSyncService");
        wmPoiShareService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiShareService");
        wmActOrderChargeService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmActOrderChargeService");
        wmSettleService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleService");
        wmSettleFlowService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleFlowService");
        wmAccountWithDrawService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAccountWithDrawService");
        wmSettle2AccountHisService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettle2AccountHisService");
        wmThirdPoiSettlementService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmThirdPoiSettlementService");
        wmSupermarketsSettleService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSupermarketsSettleService");
        wmDepositService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmDepositService");
        wmAccountWithdrawRecordFailedQueueService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAccountWithdrawRecordFailedQueueService");
        wmCrowdSourcingDepositDebitService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmCrowdSourcingDepositDebitService");
        wmCrowdSourcingDebitService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmCrowdSourcingDebitService");
        wmMoneyMonitorService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmMoneyMonitorService");
        wmContractSettlePoiService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractSettlePoiService");
        wmBillPeriodService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmBillPeriodService");
        wmContractSettleSettingService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmContractSettleSettingService");
        wmPoiTransferAccountService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiTransferAccountService");
        wmSettleBillExtraService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleBillExtraService");
        wmAdDepositService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAdDepositService");
        wmMealLossService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmMealLossService");
        wmSettleQueryForBusinessService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleQueryForBusinessService");
        wmAdjustmentService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAdjustmentService");
        WmAdCpcDepositService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("WmAdCpcDepositService");
        wmActOrderSwitchService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmActOrderSwitchService");
        wmActivityOrderService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmActivityOrderService");
        wmReplicatorService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmReplicatorService");
        wmSettleTestService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmSettleTestService");
        wmAgentMoneyService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAgentMoneyService");
        wmActOrderChargeServiceTest = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmActOrderChargeServiceTest");
        wmTaskPoiThriftSubscribe = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmTaskPoiThriftSubscribe");
        wmPoiSpThriftSubscribe = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiSpThriftSubscribe");
        wmPoiThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiThriftService");
        wmPoiTagThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiTagThriftService");
        wmPoiAppThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiAppThriftService");
        wmPoiStatDataThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiStatDataThriftService");
        wmPoiContactThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiContactThriftService");
        wmThirdSpAreaThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmThirdSpAreaThriftService");
        wmAcctContactThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAcctContactThriftService");
        wmPoiCommitmentThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiCommitmentThriftService");
        wmPoiQualificationInfoThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiQualificationInfoThriftService");
        wmAuditPoiThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAuditPoiThriftService");
        wmAppThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmAppThriftService");
        wmThirdPoiThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmThirdPoiThriftService");
        wmPoiThirdSpThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiThirdSpThriftService");
        wmPoiOplogThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiOplogThriftService");
        wmPoiQualificationThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiQualificationThriftService");
        wmLogisticsThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmLogisticsThriftService");
        wmPoiGroupBrandThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiGroupBrandThriftService");
        wmPoiProfileThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiProfileThriftService");
        wmPoiLogoThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiLogoThriftService");
        wmPoiAuditThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiAuditThriftService");
        wmPoiLabelThriftService = (Twitter.Iface) waimaiThriftServiceBeanFactory.getBean("wmPoiLabelThriftService");
        Thread.sleep(15000);
    }

    @AfterClass
    public static void stop() {
        waimaiThriftServiceBeanFactory.destroy();
    }

    @Test
    public void waimaiClientInit() throws InterruptedException {
        System.out.println("waimai client init time: " + (System.currentTimeMillis() - start) + " ms!");
        Thread.sleep(60 * 1000);
//        Thread.sleep(3600 * 1000);
    }

    //    @Test
    public void baseTypeTest() throws InterruptedException {
        Thread.sleep(15000);

        try {
            boolean b = true;
            boolean result = wmOrgCityThriftService.testBool(b);
            System.out.println("result:" + result);
            assert (result == b);

        } catch (TException e) {
            e.printStackTrace();
        }

        try {
            byte b = 10;
            byte result = wmOrgCityThriftService.testByte(b);
            System.out.println(result);
            assert (result == b);
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            short s = 100;
            short result = wmOrgCityThriftService.testI16(s);
            System.out.println(result);
            assert (result == s);
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            int i = 1234;
            int result = wmOrgCityThriftService.testI32(i);
            System.out.println(result);
            assert (result == i);
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            long l = 123456;
            long result = wmOrgCityThriftService.testI64(l);
            System.out.println(result);
            assert (result == l);
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            double d = 123456.789;
            double result = wmOrgCityThriftService.testDouble(d);
            System.out.println(result);
            assert (result == d);
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            ByteBuffer b = ByteBuffer.wrap("test".getBytes());
            ByteBuffer result = wmOrgCityThriftService.testBinary(b);
            assert (b.equals(result));
        } catch (TException e){
            e.printStackTrace();
        }


        String s = "test123456";
        for (int i = 0; i < 100; i++) {
            s += s;
            if (s.length() > 10240)
                break;
        }

        try {
            for(int j = 0; j < 20; j++) {
                String result = wmOrgCityThriftService.testString(s);
                System.out.println(j);
                assert (s.equals(result));
            }
        } catch (TException e){
            e.printStackTrace();
        }
    }

    //        @Test
    public void containersTest(){
        try {
            List<String> l = new ArrayList<String>();
            l.add("a");
            l.add("b");
            l.add("c");
            List<String> result = wmOrgCityThriftService.testList(l);
            assert (l.equals(result));
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            Set<String> s = new HashSet<String>();
            s.add("a");
            s.add("b");
            s.add("c");
            Set<String> result = wmOrgCityThriftService.testSet(s);
            assert (s.equals(result));
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            Map<String, String> m = new HashMap<String, String>();
            m.put("1", "a");
            m.put("2", "b");
            m.put("3", "c");
            Map<String, String> result = wmOrgCityThriftService.testMap(m);
            assert (m.equals(result));
        } catch (TException e){
            e.printStackTrace();
        }
    }

    @Test
    public void otherTest(){
        try {
            wmOrgCityThriftService.testVoid();
        } catch (TException e){
            e.printStackTrace();
        }


        try {
            List<Tweet> tweets = new ArrayList<Tweet>();
            tweets.add(new Tweet(1, "1", "1"));
            tweets.add(new Tweet(2, "2", "2"));
            tweets.add(new Tweet(3, "3", "3"));
            TweetSearchResult tweetSearchResult = new TweetSearchResult(tweets);
            TweetSearchResult result = wmOrgCityThriftService.testStruct("test");
            assert (tweetSearchResult.getTweets().equals(result.getTweets()));
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            /**
             * 当返回值为bool、int等基本类型时，thrift 0.8 不会抛出自定义异常
             */
            wmOrgCityThriftService.testException(new Tweet(1, "1", "1"));
        } catch (TwitterUnavailable twitterUnavailable) {
            assert (twitterUnavailable.getMessage().endsWith("exception"));
        } catch (TException e){
            e.printStackTrace();
        }
    }

}
