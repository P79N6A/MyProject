package com.sankuai.octo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import com.sankuai.octo.Common;
import com.sankuai.octo.model.Provider;
import com.sankuai.octo.model.report.ScannerReport;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;


public class ScanUtils {
    private final static Logger logger = LoggerFactory.getLogger(ScanUtils.class);
    public static String hostIpPrefix = "";

    static {
        if (StringUtils.isBlank(hostIpPrefix))
            hostIpPrefix = getLocalIpV4Prefix();
    }

    public static String getLocalIpV4Prefix() {
        String localAddress = ProcessInfoUtil.getLocalIpV4();
        String[] ipSegs = localAddress.split("\\.");
        String prefix = ipSegs[0] + "." + ipSegs[1] + ".";
        return prefix;

    }

    public static JSONObject bytes2JO(byte[] bytes) {
        if (null == bytes)
            return null;
        String serverInfo = "";
        JSONObject jo = null;
        try {
            serverInfo = new String(bytes, "utf-8");
            jo = JSON.parseObject(serverInfo);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            return jo;
        }
    }

    public static boolean isReachable(String ip, int timeout) {
        boolean result = true;
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            result = inetAddress.isReachable(timeout);
        } catch (Exception e) {
            result = false;
        } finally {
            return result;
        }
    }


    public static JSONObject getProviderJsonInZK(String serverPath) {
        return ScanUtils.bytes2JO(Common.zkClient.get(serverPath));
    }

    public static void deleteUnReachableProvider(Provider provider) {
        for (int i = 0; i < 3; i++) {
            if (isReachable(provider.getIp(), Common.getTimeOutInMills(provider.getIp())))
                return;
        }

        logger.warn("Delete unReachable provider! " + provider.getIdentifierString());
        try {
            Common.zkClient.delete(provider.getServerPath());
            updateClusterTime(provider.getProvidersDir());
        } catch (KeeperException e) {
            logger.warn("deleteUnReachableProvider Exception" + e.getCause());
        } finally {
            SendReport.send(new ScannerReport(1, "DeleteProvider",
                    "Delete unReachable provider!", provider.getIdentifierString()));
        }
    }


    public static void updateClusterTime(String providersDir) {
        JSONObject clusterJO = ScanUtils.bytes2JO(Common.zkClient.get(providersDir));
        long lastUpdateTime = System.currentTimeMillis() / 1000;
        clusterJO.put("lastUpdateTime", lastUpdateTime);
        Common.zkClient.update(providersDir, JSON.toJSONBytes(clusterJO), false);
        logger.info("cluster status changed " + providersDir + " lastUpdateTime:" + lastUpdateTime);

    }

}
