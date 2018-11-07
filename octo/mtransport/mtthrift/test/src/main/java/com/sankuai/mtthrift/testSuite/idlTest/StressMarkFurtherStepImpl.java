package com.sankuai.mtthrift.testSuite.idlTest;

import com.meituan.mtrace.Tracer;
import org.apache.thrift.TException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 16/12/7
 * Time: 10:10
 */
public class StressMarkFurtherStepImpl implements Twitter.Iface {
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
        Map<String, String> mtraceOneStepContext = Tracer.getServerSpan().getRemoteOneStepContext();
        Map<String, String> mtraceForeverContext = Tracer.getServerSpan().getForeverContext();

        assert (mtraceOneStepContext.get("oneStepContext") == null);
        assert (mtraceForeverContext.get("foreverContext") != null);

        return s;
    }

    @Override public List<String> testList(List<String> l) throws TException {
        return l;
    }

    @Override public Set<String> testSet(Set<String> s) throws TException {
        return s;
    }

    @Override public Map<String, String> testMap(Map<String, String> m)
            throws TException {
        return m;
    }

    @Override public void testVoid() throws TException {

    }

    @Override public String testReturnNull() throws TException {
        return null;
    }

    @Override public TweetSearchResult testStruct(String query)
            throws TException {
        TweetSearchResult result = new TweetSearchResult();
        result.addToTweets(new Tweet(1, "1", "1"));
        result.addToTweets(new Tweet(2, "2", "2"));
        result.addToTweets(new Tweet(3, "3", "3"));

        return result;
    }

    @Override public String testException(Tweet tweet)
            throws TwitterUnavailable, TException {
        throw new TwitterUnavailable("exception");
    }
}
