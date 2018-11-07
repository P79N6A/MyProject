package com.sankuai.inf.octo.mns.falcon;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.inf.octo.mns.util.IpUtil;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-29
 * Time: 下午2:56
 */
public class ReportUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReportUtil.class);

    private static final int STEP = 60;
    private static final String FALCON_URL = "http://127.0.0.1:1988/v1/push";
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int SOCKET_TIMEOUT = 3000;
    static final String UNKNOWN = "unkown";

    //default value is null.
    private static String localhostname = null;

    private ReportUtil() {

    }

    public static boolean doIOWrite(List<Counter> list) {
        PrintWriter out = null;
        boolean r = true;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(FALCON_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(SOCKET_TIMEOUT);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.connect();
            out = new PrintWriter(conn.getOutputStream());
            String body = initPostParam(list);
            out.write(body);
            out.flush();

            int code = conn.getResponseCode();
            if (code > 300) {
                LOG.debug("falcon report error! return code:{}", code);
            }
        } catch (Exception e) {
            //ignore the error of uploading data to falcon-agent, because local falcon-agent is maybe not installed.
            r = false;
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return r;
    }

    public static String initPostParam(List<Counter> list) {
        StringBuilder data = new StringBuilder();
        data.append("[");
        for (Counter counter : list) {
            String metric = counter.getMetric();
            if (CommonUtil.isBlankString(metric)) {
                continue;
            }
            data.append("{")
                    .append("\"ENDPOINT\":\"").append(getLocalHostName()).append("\",")
                    .append("\"metric\":\"").append(metric).append("\",")
                    .append("\"value\":\"").append(counter.getValue()).append("\",")
                    .append("\"timestamp\":").append(counter.getTime() / 1000).append(",")
                    .append("\"step\":").append(STEP).append(",")
                    .append("\"counterType\":\"").append("GAUGE").append("\",")
                    .append("\"tags\":\"").append(counter.getTags()).append("\"")
                    .append("},");
        }
        data.deleteCharAt(data.length() - 1);
        data.append("]\n");
        return data.toString();
    }

    static String getLocalHostName() {
        if (!CommonUtil.isBlankString(localhostname)) {
            return localhostname;
        }

        localhostname = ProcessInfoUtil.getHostNameInfoByIp();
        if (CommonUtil.isBlankString(localhostname)) {
            LOG.error("unknown local host name, use 'unknown'.");
            localhostname = UNKNOWN;
        } else if (localhostname.contains(".sankuai.com") || localhostname.contains(".office.mos")) {
            localhostname = localhostname.substring(0, localhostname.indexOf("."));
        }
        return localhostname;
    }

}
