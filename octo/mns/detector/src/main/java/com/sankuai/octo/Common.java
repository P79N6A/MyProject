package com.sankuai.octo;

import com.meituan.service.mobile.zkclient.MtZookeeperClient;
import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import com.sankuai.octo.model.report.ScannerReport;
import com.sankuai.octo.util.ScanUtils;

import java.util.concurrent.LinkedBlockingQueue;

public class Common {

    public static final Boolean isOnline = ProcessInfoUtil.isLocalHostOnline();
    public static final String appkey = "com.sankuai.inf.octo.scannerdetector";
    public static final String vbarAsRead = "\\|";
    public static final String vbar = "|";
    public static final String colon = ":";
    public static final long unReachableTimeBeforeDelete = isOnline ? 12 * 60 * 60 * 1000L : 60 * 60 * 1000L;
    public static final String postReportApi = "/api/scanner/report?";
    public static final String deaultEncoding = "utf-8";
    public static String msgpHost = isOnline ? "http://octo.sankuai.com" : "http://octo.test.sankuai.info";
    public static String zkUrl = isOnline ? "dx-inf-mns-zk04:2181,dx-inf-mns-zk05:2181,dx-inf-mns-zk06:2181" : "10.4.245.244:2181,10.4.245.245:2181,10.4.245.246:2181";
    public static MtZookeeperClient zkClient;
    public static String gqZkObserverAddress = "gq-inf-mns-zk01.gq.sankuai.com:2181,gq-inf-mns-zk02.gq.sankuai.com:2181,gq-inf-mns-zk03.gq.sankuai.com:2181";
    public static String gqIpPrefix = "10.69.";

    static {
        if (Common.isOnline && ScanUtils.hostIpPrefix.startsWith(Common.gqIpPrefix)) {
            zkUrl = Common.gqZkObserverAddress;
        }
        zkClient = new MtZookeeperClient(zkUrl, 30000, true);
    }

    public static int getTimeOutInMills(String ip) {
        if (isOnline) {
            if (ip.startsWith("10.4") || ip.startsWith("10.32") || ip.startsWith("10.12")) {
                return 40;
            }
            return 80;
        } else {
            return 120;
        }
    }
}
