package com.sankuai.octo.benchmark.utils;


import com.sankuai.octo.benchmark.thrift.Message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-12-30
 * Time: 下午7:25
 */
public class ThriftMsgGenerator {

    public static final String base = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static ByteBuffer getRandomBuffer(int length) {
        return ByteBuffer.wrap(getRandomString(length).getBytes());
    }

    public static String getRandomString(int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static List<Message> getRandomMessageList(int length) {
        List<Message> msgList = new ArrayList<Message>();
        String str = getRandomString(1);
        for (int i = 0; i < length; i++) {
            Message message = new Message();
            message.setId(i);
            message.setContent(str);
            msgList.add(message);
        }
        return msgList;
    }
}
