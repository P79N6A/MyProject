package com.sankuai.octo.scanner.falcon;

import com.meituan.jmonitor.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-11-26
 * Time: 上午10:23
 */
public class ReportUtils {

    private final static Log LOG = LogFactory.getLog(ReportUtils.class);

    private static final int STEP = 300;
    private static final String URL = "http://127.0.0.1:1988/v1/push";
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int SOCKET_TIMEOUT = 1000;
    private static String localhostname;

    private static List<Item> list = new ArrayList<Item>();

    public static boolean doIOWrite(List<Item> list, int step) {
        URL url;
        PrintWriter out = null;
        boolean r = true;
        HttpURLConnection conn = null;
        try {
            url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(SOCKET_TIMEOUT);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.connect();
            out = new PrintWriter(conn.getOutputStream());
            String body = initPostParam(list, step);
            out.write(body);
            out.flush();

            int code = conn.getResponseCode();
            if (code > 300) {
                LOG.error("Do http agent error! return code:" + code);
            }
        } catch (Exception e) {
            LOG.debug("Do http agent error!");
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

    public static String initPostParam(List<Item> list, int step) {
        StringBuilder data = new StringBuilder();
        String tag = "";
        data.append("[");
        for (Item item : list) {
            String key = item.key;
            if (Utils.isBlank(key)) {
                continue;
            }
            data.append("{");
            data.append("\"endpoint\":\"").append(getLocalHostName()).append("\",");
            data.append("\"metric\":\"").append(key).append("\",");
            data.append("\"value\":\"").append(item.value).append("\",");
            data.append("\"timestamp\":").append(item.mills / 1000).append(",");
            data.append("\"step\":").append(step).append(",");
            data.append("\"counterType\":\"").append("GAUGE").append("\",");
            data.append("\"tags\":\"").append(tag).append("\"");
            data.append("},");
        }
        data.deleteCharAt(data.length() - 1);
        data.append("]\n");
        return data.toString();
    }

    private static String getLocalHostName() {
        if (null != localhostname) {
            return localhostname;
        }
        try {
            localhostname = InetAddress.getLocalHost().getHostName();
            if (!Utils.isBlank(localhostname) && localhostname.contains(".")) {
                localhostname = localhostname.substring(0,
                        localhostname.indexOf("."));
            }
        } catch (final UnknownHostException ex) {
            LOG.error("Jmonitor http agent unkown local host name,use 'unkown'.", ex);
            localhostname = "unkown";
        }
        return localhostname;
    }

    public static void addItem(String k, String v) {
        list.add(new Item(k, v, System.currentTimeMillis()));
    }

    public static void report() {
        doIOWrite(list, STEP);
        list.clear();
    }

}
