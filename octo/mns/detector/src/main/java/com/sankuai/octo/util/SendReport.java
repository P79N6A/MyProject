package com.sankuai.octo.util;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.octo.Common;
import com.sankuai.octo.detector.actors.httpPost;
import com.sankuai.octo.model.report.ScannerReport;


public class SendReport {

    public static void send(ScannerReport scannerReport) {
        JSONObject jsonReport = (JSONObject)JSONObject.toJSON(scannerReport);
        httpPost.post(Common.msgpHost + Common.postReportApi, jsonReport, Common.deaultEncoding);
    }
}
