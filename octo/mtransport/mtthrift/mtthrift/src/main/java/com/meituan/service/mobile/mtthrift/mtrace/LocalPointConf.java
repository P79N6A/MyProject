package com.meituan.service.mobile.mtthrift.mtrace;

import com.meituan.mtrace.Endpoint;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: YangXuehua
 * Date: 13-12-24
 * Time: 下午2:59
 */
public class LocalPointConf {
    private final static Logger LOG = LoggerFactory.getLogger(LocalPointConf.class);
    private static volatile Endpoint defaultEndpoint = null;
    private static volatile Map<Integer, Endpoint> appport2Endpoint = new ConcurrentHashMap<Integer, Endpoint>();

    private static String appKey;
    private static String appHost;
    private static String appIp;
    private static int appPort = -1;
    private static Boolean traceLog = null;

    public static String getAppKey() {
        if (StringUtils.isBlank(appKey)) {
            appKey = System.getProperty("app.key", "");
        }
        return appKey;
    }

    public static String getAppHost() {
        if (appHost == null) {
            appHost = System.getProperty("app.host");
            if (appHost == null || appHost.length() == 0) {
                appHost = ProcessInfoUtil.getHostNameInfoByIp();
            }
            if (appHost == null)
                appHost = "";
        }
        return appHost;
    }

    public static String getAppIp() {
        if (appIp == null) {
            appIp = System.getProperty("app.ip");
            if (appIp == null || appIp.length() == 0) {
                appIp = ProcessInfoUtil.getLocalIpV4();
            }
        }
        return appIp;
    }

    /**
     * 避免使用该值，同时存在多个ThriftServerPublisher的情况下会出现覆盖
     * @return
     */
    public static int getAppPort() {
        if (appPort < 0) {
            String appPortStr = System.getProperty("app.port");
            if (appPortStr != null && appPortStr.length() > 0) {
                try {
                    appPort = Integer.parseInt(appPortStr);
                } catch (NumberFormatException e) {
                    LOG.info("NumberFormatException...", e.getMessage());
                }
            }
            if (appPort < 0)
                appPort = 0;
        }
        return appPort;
    }

    public static void setPort(int port) {
        appPort = port;
    }

    public static boolean isTraceLog() {
        // 已废弃逻辑，待彻底删除
        traceLog = false;
        return traceLog;
    }

    public static Endpoint getLocalEndpoint() {
        if (defaultEndpoint == null) {
            defaultEndpoint = new Endpoint(LocalPointConf.getAppKey(), LocalPointConf.getAppIp(), LocalPointConf.getAppPort());
        }
        return defaultEndpoint;
    }

    public static Endpoint getLocalEndpoint(int appPort) {
        Endpoint localEndPoint = appport2Endpoint.get(appPort);
        if (localEndPoint == null) {
            localEndPoint = new Endpoint(LocalPointConf.getAppKey(), LocalPointConf.getAppIp(), appPort);
            appport2Endpoint.put(appPort, localEndPoint);
        }
        return localEndPoint;
    }
}
