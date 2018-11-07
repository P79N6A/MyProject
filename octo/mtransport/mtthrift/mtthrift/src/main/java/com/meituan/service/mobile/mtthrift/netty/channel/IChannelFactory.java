package com.meituan.service.mobile.mtthrift.netty.channel;


/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/6
 * Time: 13:51
 */
public interface IChannelFactory {

    IChannel createChannel();

}
