package com.sankuai.mtthrift.testSuite.annotationTest;

import com.facebook.swift.codec.ThriftEnumValue;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-18
 * Time: 下午4:21
 */
public enum TweetType {

    TWEET(0),
    RETWEET(2),
    DM(10),
    REPLY(11);

    private final int value;

    TweetType(int value) {
        this.value = value;
    }

    @ThriftEnumValue
    public int getValue() {
        return value;
    }


}
