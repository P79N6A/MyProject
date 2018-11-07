package com.meituan.service.mobile.mtthrift.netty.channel;

import java.net.SocketAddress;
import java.util.concurrent.Future;
/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/6
 * Time: 12:02
 */
public interface IChannel {

    boolean isAvailable();

    boolean isWritable();

    void disConnect();

    void connect();

    SocketAddress getRemoteAddress();

    Future write(byte[] bytes);
}
