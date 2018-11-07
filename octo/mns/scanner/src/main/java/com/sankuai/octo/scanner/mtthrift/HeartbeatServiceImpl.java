package com.sankuai.octo.scanner.mtthrift;

import org.apache.thrift.TException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-4-26
 * Time: 下午3:12
 */
public class HeartbeatServiceImpl implements HeartbeatService.Iface {

    public String getStatus() throws TException {
        return "ALIVE";
    }

}
