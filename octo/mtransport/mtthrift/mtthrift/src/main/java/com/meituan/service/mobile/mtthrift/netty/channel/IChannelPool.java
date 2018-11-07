package com.meituan.service.mobile.mtthrift.netty.channel;

import com.meituan.service.mobile.mtthrift.netty.exception.ChannelPoolException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/6
 * Time: 11:22
 */
public interface IChannelPool {

    int getSize();

    IChannel selectChannel() throws ChannelPoolException;

    boolean isClosed();

    void close();
}
