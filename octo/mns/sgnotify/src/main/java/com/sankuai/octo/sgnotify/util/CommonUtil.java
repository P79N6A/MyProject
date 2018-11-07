package com.sankuai.octo.sgnotify.util;


import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;

/**
 * Created by lhmily on 06/18/2017.
 */
public class CommonUtil {
    public static int longTimeOutInMills = ProcessInfoUtil.isLocalHostOnline() ? 50 : 150;
}
