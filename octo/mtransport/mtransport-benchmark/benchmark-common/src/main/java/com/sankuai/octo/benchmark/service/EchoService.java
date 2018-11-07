package com.sankuai.octo.benchmark.service;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-12-30
 * Time: 上午10:18
 */
public interface EchoService {

    public String sendString(String str);

    public ByteBuffer sendBytes(ByteBuffer bytes);

    public List<Message> sendPojo(List<Message> msgList);
}
