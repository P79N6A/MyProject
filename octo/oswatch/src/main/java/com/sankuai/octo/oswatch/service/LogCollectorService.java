package com.sankuai.octo.oswatch.service;

import com.sankuai.octo.oswatch.model.ConsumerResult;
import com.sankuai.octo.oswatch.model.LogCollectorResult;
import com.sankuai.octo.oswatch.model.PerfQPSResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenxi on 6/30/15.
 */

public class LogCollectorService extends QPSReader {
    public Logger logger = LoggerFactory.getLogger(LogCollectorService.class);

    public LogCollectorService(String queryURL) {
        this.queryURL = queryURL;
    }

    public Map<String, Double> getCurrentQPS(String appkey, String method, String env, long timestamp, int periodInSeconds) {
        long start, end;
        String spanname;
        Map<String, Double> rv = new HashMap<String, Double>();

        if(method == null) spanname = "all";
        else spanname = method;

        end = timestamp / ONE_SECOND_IN_MS;
        start = end - periodInSeconds;

        String request = mergePerfQuery(appkey, spanname, env, String.valueOf(start), String.valueOf(end));
        logger.info("request",request);
        LogCollectorResult qpsObj = getHttpContent(request, LogCollectorResult.class);

        if (qpsObj == null) return rv;

        for (ConsumerResult cResult: qpsObj.getConsumer2QpsList()){
            logger.info("consumer:"+cResult.getConsumerAppKey());
            logger.info("qps:"+ Double.valueOf(cResult.getQpsAvg()));
            rv.put(cResult.getConsumerAppKey(), Double.valueOf(cResult.getQpsAvg()));
        }

        return rv;
    }

    public String mergePerfQuery(String appkey, String spanname, String env, String start, String end) {
        return "/api/query?provider=" +
                appkey +
                "&spanname=" +
                spanname +
                "&env=" +
                env +
                "&start=" +
                start +
                "&end=" +
                end;
    }

//    public static void main(String[] args) {
//        LogCollectorService s = new LogCollectorService("http://192.168.3.163:8930");
//        long x = System.currentTimeMillis();
//        while (true) {
//            Map<String, Double> rv = s.getCurrentQPS("com.sankuai.chenxi.test_provider_a", "HelloInterface.hi", x , 5);
//            try{Thread.sleep(1000);} catch (Exception e) {}
//            System.out.println(rv);
//        }
//    }
}
