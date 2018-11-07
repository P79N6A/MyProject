package com.sankuai.octo.scanner.service;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.octo.service.httpPost;
import com.sankuai.octo.scanner.Common;
import com.sankuai.octo.scanner.model.report.ScannerReport;


public class SendReport {

    public static void send(ScannerReport scannerReport) {
        JSONObject jsonReport = (JSONObject) JSONObject.toJSON(scannerReport);
        httpPost.post(Common.msgpHost + Common.postReportApi, jsonReport, Common.deaultEncoding);
    }
}
