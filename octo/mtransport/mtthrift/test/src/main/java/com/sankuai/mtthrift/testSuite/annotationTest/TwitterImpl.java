package com.sankuai.mtthrift.testSuite.annotationTest;



import com.meituan.mtrace.Tracer;
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
 * Date: 16-2-22
 * Time: 上午10:43
 */
public class TwitterImpl implements Twitter{

    public boolean testBool(boolean b) throws TException {
        return b;
    }

    public byte testByte(byte b) throws TException {
        return b;
    }

    public short testI16(short i) throws TException {
        return i;
    }

    public int testI32(int i) throws TException {
        return i;
    }

    public long testI64(long i) throws TException {
        return i;
    }

    public double testDouble(double d) throws TException {
        return d;
    }

    public ByteBuffer testBinary(ByteBuffer b) throws TException {
        return b;
    }

    public String testString(String s) throws TException {
        return s;
    }

    public List<String> testList(List<String> l) throws TException {
        return l;
    }

    public Set<String> testSet(Set<String> s) throws TException {
        return s;
    }

    public Map<String, String> testMap(Map<String, String> m) throws TException {
        return m;
    }

    public void testVoid() throws TException {

    }

    public String testReturnNull() throws TException {
        return null;
    }

    public TweetSearchResult testStruct(String query) throws TException {
        List<Tweet> tweets = new ArrayList<Tweet>();
        tweets.add(new Tweet(1, "1", "1"));
        tweets.add(new Tweet(2, "2", "2"));
        tweets.add(new Tweet(3, "3", "3"));
        TweetSearchResult result = new TweetSearchResult(tweets);
        return result;
    }

    public String testException(Tweet tweet) throws TwitterUnavailable, TException {
        throw new TwitterUnavailable("exception");
    }
}
