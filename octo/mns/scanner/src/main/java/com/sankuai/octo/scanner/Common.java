package com.sankuai.octo.scanner;

import com.meituan.service.mobile.zkclient.MtZookeeperClient;
import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import com.sankuai.octo.scanner.model.report.ScannerReport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jiguang on 14-11-23.
 */
public class Common {
    public static final Boolean isOnline = ProcessInfoUtil.isLocalHostOnline();
    public static final String localIp = ProcessInfoUtil.getLocalIpV4();
    public static final int timeOutInMills = isOnline? 20 : 200;
    public static final int longTimeOutInMills = isOnline? 50 : 150;
    public static final String appkey = "com.sankuai.inf.octo.scannermaster";
    public static final String vbarAsRead = "\\|";
    public static final String vbar = "|";
    public static final String colon = ":";
    public static final String agentAppkey = "com.sankuai.inf.sg_agent";
    public static final String msgpAppkey = "com.sankuai.inf.msgp";
    public static final String kmsAgentAppkey = "com.sankuai.inf.kms_agent";

    public static final long unReachableTimeBeforeDelete = isOnline? 12 * 60 * 60 * 1000L : 60 * 60 * 1000L;
    public static final String postReportApi = "/api/scanner/report?";
    public static final String deaultEncoding = "utf-8";
    public static String msgpHost = "";
    public static final String checkStatus = "checkStatus";
    public static boolean allowUpdateZKData = false;
    public static MtZookeeperClient zkClient;
    public static String gqZkObserverAddress = "gq-inf-mns-zk01.gq.sankuai.com:2181,gq-inf-mns-zk02.gq.sankuai.com:2181,gq-inf-mns-zk03.gq.sankuai.com:2181";
    public static String gqIpPrefix = "10.69.";
    public static String dxIpPrefix = "10.32.";
    public static String yfIpPrefix = "10.4.";
    public static String cqIpPrefix = "10.12.";

    public static Set<String> beijingIpPrefixSet = new HashSet<>();

    static {
        //LF
        beijingIpPrefixSet.add("10.64.");
        beijingIpPrefixSet.add("10.65.");
        //DX
        beijingIpPrefixSet.add("10.32.");
        beijingIpPrefixSet.add("10.33.");
        //YF
        beijingIpPrefixSet.add("10.4.");
        beijingIpPrefixSet.add("10.5.");
        beijingIpPrefixSet.add("10.99.");
        //CQ
        beijingIpPrefixSet.add("10.12.");
        beijingIpPrefixSet.add("10.13.");
        //RZ
        beijingIpPrefixSet.add("10.16.");
        beijingIpPrefixSet.add("10.17.");
        //WJ、DBA
        beijingIpPrefixSet.add("10.8.");
        beijingIpPrefixSet.add("10.10.");
        //DBC
        beijingIpPrefixSet.add("10.96.");
        beijingIpPrefixSet.add("10.97.");
    }

}
