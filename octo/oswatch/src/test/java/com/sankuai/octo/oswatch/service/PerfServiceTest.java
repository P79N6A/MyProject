package com.sankuai.octo.oswatch.service;

/**
 * Created by chenxi on 6/9/15.
 */

import static org.junit.Assert.*;

import com.sankuai.octo.oswatch.model.OctoEnv;
import com.sankuai.octo.oswatch.model.PerfQPSResult;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class PerfServiceTest {
    PerfService perfServiceService = new PerfService("http://perf.sankuai.com");

    @Test
    public void TimeFormatTest() {
        long timestamp = 1433843027792L;
        DateTime dt = new DateTime(timestamp);
        assertEquals("2015-06-09T17:43:47.792+08:00", dt.toString());
        assertEquals("2015/06/09-17:43:00", dt.toString(perfServiceService.TIME_FORMAT));
    }

    @Test
    public void getHttpResult() {
        String url = "/api/query?m=sum:1m-sum:counters.com.sankuai.inf.logcollector.serverCount%7Bspanname=all,remoteApp=*%7D&start=2015/06/09-13:27:00&end=2015/06/09-13:37:00";
        List<PerfQPSResult> qps = perfServiceService.getHttpContentList(url, PerfQPSResult.class);
        assertEquals(11, qps.get(0).getDps().size());
    }

    @Test
    public void getCurrentQPSTest () {
        long timestamp = 1433843027792L;
        Map<String, Double> rv;
        rv = perfServiceService.getCurrentQPS("com.sankuai.inf.logcollector", OctoEnv.getEnv(1), "all", timestamp, 5);
        assertTrue(rv.size() > 0);

        rv = perfServiceService.getCurrentQPS("com.sankuai.inf.logcollector", OctoEnv.getEnv(1), "uploadModuleInvoke", timestamp, 5);
        assertTrue(rv.size() > 0);
    }
}
