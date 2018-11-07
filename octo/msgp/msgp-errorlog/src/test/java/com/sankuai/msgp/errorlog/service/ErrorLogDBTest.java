package com.sankuai.msgp.errorlog.service;


import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.errorlog.ApplicationTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


public class ErrorLogDBTest extends ApplicationTest {
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogDBTest.class);

    @Autowired
    private ErrorLogFilterService errorLogFilterService;

    @Test
    public void testMybatis() throws Exception {
        ParsedLog parsedLog = new ParsedLog();
        parsedLog.setUniqueKey("errorlog_test_"+System.currentTimeMillis()/1000);
        parsedLog.setLogTime(new Date());
        parsedLog.setHost("test-host");
        parsedLog.setAppkey("com.sankuai.inf.octo.errorlog");
        parsedLog.setLocation("com.sankuai.octo.statistic.util.HBaseClient$");
        parsedLog.setMessage(" Could not initialize class org.apache.hadoop.hbase.client.HConnectionManager$HConnectionKey\n" +
                "Caused by:\n" +
                "\n" +
                "java.lang.NoClassDefFoundError: Could not initialize class org.apache.hadoop.hbase.client.HConnectionManager$HConnectionKey\n" +
                "\tat org.apache.hadoop.hbase.client.HConnectionManager.getConnection(HConnectionManager.java:182)\n" +
                "\tat org.apache.hadoop.hbase.client.HTable.<init>(HTable.java:194)\n" +
                "\tat org.apache.hadoop.hbase.client.HTableFactory.createHTableInterface(HTableFactory.java:36)\n" +
                "\tat org.apache.hadoop.hbase.client.HTablePool.createHTable(HTablePool.java:268)\n" +
                "\tat org.apache.hadoop.hbase.client.HTablePool.findOrCreateTable(HTablePool.java:198)\n" +
                "\tat org.apache.hadoop.hbase.client.HTablePool.getTable(HTablePool.java:173)\n" +
                "\tat org.apache.hadoop.hbase.client.HTablePool.getTable(HTablePool.java:216)\n" +
                "\tat com.sankuai.octo.msgp.service.LogService.getLogFromHBase(LogService.java:192)");

        parsedLog.setException("测试测试 get \"com.meituan.tair.piegon.message\" failed");
        parsedLog.setTraceId("5892248530995899108");
        parsedLog.setHostSet("set1");
        parsedLog.setEnv("test");

        for (int i = 0; i < 10; i++) {
            errorLogFilterService.handleLog(parsedLog);
        }
    }

    @Test
    public void testFilter() throws Exception{
        ParsedLog parsedLog = new ParsedLog();
        String appkey = "com.sankuai.wpt.op.entryop";
        String message = "Fails to get waimai isFilterTopic, do fallback function";
        parsedLog.setMessage(message);
        parsedLog.setException("");
        parsedLog.setAppkey(appkey);
        parsedLog.setLogTime(new Date());
        errorLogFilterService.handleLog(parsedLog);
        while (true) {
            Thread.sleep(10000);
        }
    }
}
