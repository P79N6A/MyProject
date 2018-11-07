package com.meituan.service.mobile.mtthrift.degrage;

import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-3-30
 * Time: 下午8:03
 */
public class ServerDegradeUtil {

    public static boolean isDegrade() {
        return MtraceServerTBinaryProtocol.requestDegraded.get();
    }
}
