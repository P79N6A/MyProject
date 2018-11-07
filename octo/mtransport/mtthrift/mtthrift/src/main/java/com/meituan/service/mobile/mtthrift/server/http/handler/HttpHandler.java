package com.meituan.service.mobile.mtthrift.server.http.handler;

import com.meituan.service.mobile.mtthrift.server.http.NettyHttpSender;
import com.meituan.dorado.common.RpcRole;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/2
 */
public interface HttpHandler {

    void handle(NettyHttpSender httpSender, String uri, byte[] content);

    void setRole(RpcRole role);

    RpcRole getRole();
}
