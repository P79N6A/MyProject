package com.sankuai.octo.oswatch.service;

import com.sankuai.octo.oswatch.model.PerfQPSResult;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chenxi on 6/4/15.
 */

public class PerfService extends QPSReader {
    public static String TIME_FORMAT =  "yyyy/MM/dd-HH:mm:ss";

    public PerfService(String perfURL) {
        this.queryURL = perfURL;
    }

    public Map<String, Double> getCurrentQPS(String appkey, String method, String env, long timestamp, int periodInSeconds) {
        DateTime start, end;
        String spanname;
        Map<String, Double> rv = new HashMap<String, Double>();

        if(method == null) spanname = "all";
        else spanname = method;

        end = new DateTime(timestamp);
        start = end.minusSeconds(periodInSeconds);

        String request = mergePerfQuery(appkey, spanname, env, start.toString(TIME_FORMAT), end.toString(TIME_FORMAT));
        List<PerfQPSResult> qpsObjList = getHttpContentList(request, PerfQPSResult.class);

        if (qpsObjList.isEmpty()) return rv;

        for (PerfQPSResult qpsResult: qpsObjList) rv.put(qpsResult.getTags().getRemoteApp(), getMeanQPS(qpsResult.getDps()));
        return rv;
    }

    /*
    http://perf.sankuai.com/api/query?m=sum:1m-sum:counters.com.sankuai.waimai.money.serverCount%7Bspanname=all,remoteApp=*%7D&start=2015/06/12-00:00:00&end=2015/06/13-00:10:00
     */

    public String mergePerfQuery(String appkey, String spanname, String env,  String start, String end) {
        return "/api/query?m=sum:1m-sum:counters." +
                appkey +
                ".serverCount%7Bspanname=" +
                spanname +
                ",remoteApp=*%7D&start=" +
                start +
                "&end=" +
                end;
    }

    private Double getMeanQPS(Map<String, Double> dpsMap) {
        int qpmSum = 0;

        for (Map.Entry<String, Double> entry: dpsMap.entrySet()) qpmSum += entry.getValue();

        return (double)qpmSum / dpsMap.size();
    }
}
