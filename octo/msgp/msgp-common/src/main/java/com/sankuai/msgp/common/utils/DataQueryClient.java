package com.sankuai.msgp.common.utils;

import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.sankuai.octo.statistic.model.*;
import com.sankuai.octo.statistic.service.LogQueryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yves on 17/6/20.
 * thrift client of data query
 */

public class DataQueryClient {

    private static LogQueryService queryService;

    private static Logger LOGGER = LoggerFactory.getLogger(DataQueryClient.class);

    static {
        try {
            ThriftClientProxy thriftClientProxy = new ThriftClientProxy();
            thriftClientProxy.setAppKey("com.sankuai.inf.msgp");
            thriftClientProxy.setRemoteAppkey("com.sankuai.inf.data.query");
            thriftClientProxy.setServiceInterface(LogQueryService.class);
            thriftClientProxy.setRemoteServerPort(8961);
            thriftClientProxy.setTimeout(20000);
            thriftClientProxy.setMaxResponseMessageBytes(32768000);
            //thriftClientProxy.setClusterManager("octo");
            thriftClientProxy.afterPropertiesSet();
            queryService = (LogQueryService) thriftClientProxy.getObject();
        } catch (Exception e) {
            LOGGER.error("init statistic query client failed.", e);
        }
    }

    public static List<DataRecordMerged> queryHistoryDataMerged(String appkey, int start, int end, String protocolType, String role,
                                                                String dataType, String env, String unit, String spanname, String localhost,
                                                                String remoteAppkey, String remoteHost, String sortKey, String dataSource) {
        StatQueryParam statQueryParam = new StatQueryParam(appkey, start, end,
                protocolType, role, dataType, env, unit, spanname, localhost, remoteAppkey, remoteHost, sortKey, dataSource);
        try {
            StatMergedQueryResult statMergedQueryResult = queryService.queryHistoryDataMerged(statQueryParam);
            return statMergedQueryResult.getRecordList();
        } catch (TException e) {
            LOGGER.error("queryHistoryDataMerged failed. StatQueryParam: " + statQueryParam.toString(), e);
            return null;
        }
    }


    public static List<DataRecord> queryHistoryData(String appkey, int start, int end, String protocolType, String role,
                                                    String dataType, String env, String unit, String spanname, String localhost,
                                                    String remoteAppkey, String remoteHost, String sortKey, String dataSource) {

        StatQueryParam statQueryParam = new StatQueryParam(appkey, start, end,
                protocolType, role, dataType, env, unit, spanname, localhost, remoteAppkey, remoteHost, sortKey, dataSource);
        try {
            StatQueryResult statQueryResult = queryService.queryHistoryData(statQueryParam);
            return statQueryResult.getRecordList();
        } catch (TException e) {
            LOGGER.error("queryHistoryData failed. StatQueryParam: " + statQueryParam.toString(), e);
            return null;
        }
    }

    public static TagQueryResult queryTag(String appkey, int start, int end, String env, String role) {
        TagQueryParam queryParam = new TagQueryParam(appkey, start, end, env, role);

        try {
            return queryService.queryTag(queryParam);
        } catch (TException e) {
            LOGGER.error("queryTag failed. TagQueryParam: " + queryParam.toString(), e);
            return null;
        }
    }

    public static StatDailyQueryResult queryDailyData(String appkey, int ts, String env, String role) {
        StatDailyQueryParam queryParam = new StatDailyQueryParam(appkey, ts, env, role);
        try {
            return queryService.queryDailyData(queryParam);
        } catch (TException e) {
            LOGGER.error("queryDaily failed. StatDailyQueryParam: " + queryParam.toString(), e);
            return null;
        }
    }


    public static SecondLevelData querySecondLevelData(String appkey, int start, int end, String env, String ip) {
        QuerySecondLevelParam queryParam = new QuerySecondLevelParam(appkey, start, end, env, ip);
        try {
            return queryService.querySecondLevelData(queryParam);
        } catch (TException e) {
            LOGGER.error("querySecondLevelData failed. QuerySecondLevelParam: " + queryParam.toString(), e);
            return null;
        }
    }
}
