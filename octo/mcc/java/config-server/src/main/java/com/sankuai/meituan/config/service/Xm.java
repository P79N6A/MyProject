package com.sankuai.meituan.config.service;

import com.sankuai.xm.api.push.PushUtil;

import java.util.List;

/**
 * Created by lhmily on 08/23/2016.
 */
public class Xm {
    private static final String XM_KEY = "octo_notice";
    private static final String XM_SECRET = "0c8ecbc5d8b826124cb2e975e8309319";
    private static final String XM_SENDER = "octo_subscribe@meituan.com";
    private static final String XM_API = "http://xm-in.sankuai.com/api";

    static {
        PushUtil.init(XM_KEY, XM_SECRET, XM_SENDER, XM_API);
    }
    public static void send(List<String> users, String msg) {
        PushUtil.push(msg, users);
    }
}
