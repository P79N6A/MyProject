package com.sankuai.mtthrift.testSuite.annotationTest;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-18
 * Time: 下午4:45
 */
@ThriftStruct
public class TweetSearchResult {

    private List<Tweet> tweets;

    @ThriftConstructor
    public TweetSearchResult(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    @ThriftField(1)
    public List<Tweet> getTweets() {
        return tweets;
    }

    @ThriftField
    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }
}
