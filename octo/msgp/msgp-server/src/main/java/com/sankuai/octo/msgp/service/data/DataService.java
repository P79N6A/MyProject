package com.sankuai.octo.msgp.service.data;

import com.sankuai.octo.msgp.serivce.data.DataQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConversions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chen.CD on 2018/7/20
 */
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    public static Map<String, Double> getAllIdcDataAsClient(String appkey, int start, String env) {
        return getServerOrClientData(appkey, start, env, "client");
    }


    public static Map<String, Double> getAllIdcData(String appkey, int start, String env) {
        return getServerOrClientData(appkey, start, env, "server");
    }

    private static Map<String, Double> getServerOrClientData(String appkey, int start, String env, String role) {
        //线下prod -> dev
        env = env.equals("dev") ? "prod" : env;

        // 返回机房下对应的ip 返回数据格式参见 http://octo.test.sankuai.com/data/idc_host?appkey=com.sankuai.inf.logCollector&start=2018-06-14%2016%3A33%3A00&end=2018-06-14%2017%3A33%3A00&env=prod
        Map<String, List<String>> idc2HostMap = DataQuery.getAppLocalhost(appkey, env, role, start, start).idcLocalHosts();

        Map<String, Double> resultMap = new HashMap<>();
        Double allIdcCount = 0.0;
        for (Map.Entry<String, List<String>> entry : idc2HostMap.entrySet()) {
            String idc = entry.getKey();
            if (!idc.equals("all") && !idc.equals("other")) {
                List<String> hostList = entry.getValue();
                if (hostList != null && !hostList.isEmpty()) {
                    StringBuilder sb = new StringBuilder();

                    for (String host : hostList) {
                        sb.append(host).append(",");
                    }
                    String idcLocalHosts = sb.toString().substring(0, sb.toString().length() - 1);

                    List<DataQuery.DataRecordMerged> recordList = JavaConversions.asJavaList(DataQuery.getHistoryStatisticMergedByHost(appkey, start, start, "", role, "", env, "", "spanLocalhost", "all",
                            "*", "", "", "", idc, idcLocalHosts));

                    Double allCount = 0.0;
                    if (!recordList.isEmpty()) {
                        for (DataQuery.DataRecordMerged data : recordList) {
                            allCount += data.count();
                        }
                    }
                    resultMap.put(idc, allCount);
                    allIdcCount += allCount;
                } else {
                    resultMap.put(idc, 0.0);
                }
            }
        }
        resultMap.put("all", allIdcCount);

        return resultMap;
    }


    public static Map<String, Double> getIdcData(String appkey, String idc, int start, String env) {
        //线下prod -> dev
        env = env.equals("dev") ? "prod" : env;
        List<String> hostList = DataQuery.getAppLocalhost(appkey, env, "server", start, start).idcLocalHosts().get(idc);

        Map<String, Double> resultMap = new HashMap<>();
        if (hostList != null && !hostList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hostList.size(); i++) {
                sb.append(hostList.get(i)).append(",");
            }
            String idcLocalHosts = sb.toString().substring(0, sb.toString().length() - 1);

            List<DataQuery.DataRecordMerged> recordList = JavaConversions.asJavaList(DataQuery.getHistoryStatisticMergedByHost(appkey, start, start, "", "server", "", env, "", "spanLocalhost", "all",
                    "*", "", "", "", idc, idcLocalHosts));

            if (!recordList.isEmpty()) {
                Double allCount = 0.0;
                for (DataQuery.DataRecordMerged data : recordList) {
                    String ip = data.tags().localhost().get();
                    Double count = data.count();
                    allCount += count;
                    resultMap.put(ip, count);
                }
                resultMap.put("all", allCount);
            }
        }
        return resultMap;
    }


}
