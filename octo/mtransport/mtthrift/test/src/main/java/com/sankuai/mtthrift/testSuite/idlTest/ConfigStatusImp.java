package com.sankuai.mtthrift.testSuite.idlTest;

import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 16/12/13
 * Time: 17:00
 */
public class ConfigStatusImp implements Twitter.Iface{
    @Override public boolean testBool(boolean b) throws TException {
        return b;
    }

    @Override public byte testByte(byte b) throws TException {
        return b;
    }

    @Override public short testI16(short i) throws TException {
        return i;
    }

    @Override public int testI32(int i) throws TException {
        return i;
    }

    @Override public long testI64(long i) throws TException {
        return i;
    }

    @Override public double testDouble(double d) throws TException {
        return d;
    }

    @Override public ByteBuffer testBinary(ByteBuffer b) throws TException {
        return b;
    }

    @Override public String testString(String s) throws TException {
        return s;
    }

    @Override public List<String> testList(List<String> l) throws TException {
        return l;
    }

    @Override public Set<String> testSet(Set<String> s) throws TException {
        return s;
    }

    @Override public Map<String, String> testMap(Map<String, String> m) throws TException {
        return m;
    }

    @Override public void testVoid() throws TException {

    }

    @Override public String testReturnNull() throws TException {
        return null;
    }

    @Override
    public TweetSearchResult testStruct(String query) throws TException {
        List<Tweet> tweets = new ArrayList<Tweet>();
        tweets.add(new Tweet(1, "1", "1"));
        tweets.add(new Tweet(2, "2", "2"));
        tweets.add(new Tweet(3, "3", "3"));
        TweetSearchResult result = new TweetSearchResult(tweets);
        return result;
    }

    @Override
    public String testException(Tweet tweet) throws TwitterUnavailable, TException {
        throw new TwitterUnavailable("exception");
    }
}
