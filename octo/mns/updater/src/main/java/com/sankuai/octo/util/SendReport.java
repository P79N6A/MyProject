package com.sankuai.octo.util;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.octo.scanner.model.report.ScannerReport;
import com.sankuai.octo.updater.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SendReport {
    private final static Logger logger = LoggerFactory.getLogger(SendReport.class);

    public static final String offlineMsgpHost = "http://octo.test.sankuai.info";
    public static final String onlineMsgpHost = "http://octo.sankuai.com";
    public static final String postReportApi = "/api/scanner/report?";
    public static final String msgpHost = Common.isOnline() ? onlineMsgpHost : offlineMsgpHost;

    public static void send(ScannerReport scannerReport) {
        JSONObject jsonReport = (JSONObject) JSONObject.toJSON(scannerReport);
        HttpUtils.postJsonAsync(msgpHost + postReportApi, jsonReport, "utf-8");
    }
}
