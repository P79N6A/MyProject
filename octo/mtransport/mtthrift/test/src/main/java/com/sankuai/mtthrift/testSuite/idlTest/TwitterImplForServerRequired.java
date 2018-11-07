package com.sankuai.mtthrift.testSuite.idlTest;

import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 2017/5/8
 * Time: 上午11:24
 */
public class TwitterImplForServerRequired implements Twitter.Iface {


    @Override
    public boolean testBool(boolean b) throws TException {
        return b;
    }

    @Override
    public byte testByte(byte b) throws TException {
        return b;
    }

    @Override
    public short testI16(short i) throws TException {
        return i;
    }

    @Override
    public int testI32(int i) throws TException {
        return i;
    }

    @Override
    public long testI64(long i) throws TException {
        return i;
    }

    @Override
    public double testDouble(double d) throws TException {
        return d;
    }

    @Override
    public ByteBuffer testBinary(ByteBuffer b) throws TException {
        return b;
    }

    @Override
    public String testString(String s) throws TException {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    public List<String> testList(List<String> l) throws TException {
        return l;
    }

    @Override
    public Set<String> testSet(Set<String> s) throws TException {
        return s;
    }

    @Override
    public Map<String, String> testMap(Map<String, String> m) throws TException {
        return m;
    }

    @Override
    public void testVoid() throws TException {

    }

    @Override
    public String testReturnNull() throws TException {
        return null;
    }

    @Override
    public TweetSearchResult testStruct(String query) throws TException {
        Tweet tweet = new Tweet();
        List<Tweet> tweets = new ArrayList<Tweet>();
        tweets.add(tweet);
        TweetSearchResult result = new TweetSearchResult(tweets);
        return result;
    }

    @Override
    public String testException(com.sankuai.mtthrift.testSuite.idlTest.Tweet tweet) throws com.sankuai.mtthrift.testSuite.idlTest.TwitterUnavailable, TException {
        throw new TwitterUnavailable("exception");
    }
}
