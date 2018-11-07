package com.sankuai.meituan.config.util;

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;

/**
 * Created by lhmily on 11/24/2016.
 */
public class CommonConfigTest {

    public static String getZkUrl() {
        return ProcessInfoUtil.isLocalHostOnline()?"cos-zk.vip.sankuai.com:2181":"sgconfig-zk.sankuai.com:9331";
    }
}
