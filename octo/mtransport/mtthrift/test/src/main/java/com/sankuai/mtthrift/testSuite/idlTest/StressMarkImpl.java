package com.sankuai.mtthrift.testSuite.idlTest;

import com.meituan.mtrace.TraceParam;
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
 * Date: 16/12/5
 * Time: 上午11:17
 */
public class StressMarkImpl implements Twitter.Iface {
    @Override
    public boolean testBool(boolean b) throws TException {
        boolean a = Tracer.isTest();
        System.out.println(a);
        assert (a == true);
        return b;
    }

    @Override
    public byte testByte(byte b) throws TException {
        boolean a = Tracer.isTest();
        System.out.println(a);
        assert (a == false);
        return b;
    }

    @Override
    public short testI16(short i) throws TException {
        return i;
    }

    @Override
    public int testI32(int i) throws TException {
        Map<String, String> mtraceOneStepContext = Tracer.getServerSpan().getRemoteOneStepContext();
        Map<String, String> mtraceForeverContext = Tracer.getServerSpan().getForeverContext();
        System.out.println(mtraceForeverContext.get("foreverContext"));
        System.out.println(mtraceOneStepContext.get("oneStepContext"));
        assert (mtraceForeverContext.get("foreverContext") != null);
        assert (mtraceOneStepContext.get("oneStepContext") != null);
        return i;
    }

    @Override
    public long testI64(long i) throws TException {
        Map<String, String> mtraceOneStepContext = Tracer.getServerSpan().getRemoteOneStepContext();
        Map<String, String> mtraceForeverContext = Tracer.getServerSpan().getForeverContext();

        assert (mtraceOneStepContext.get("oneStepContext") != null);
        assert (mtraceForeverContext.get("foreverContext") != null);

        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("testSuite/stressMarkTest/another-client.xml");
        ClassPathXmlApplicationContext server =
                new ClassPathXmlApplicationContext("testSuite/stressMarkTest/another-server.xml");
        Twitter.Iface client = applicationContext
                .getBean("clientProxy", Twitter.Iface.class);

        try {
            Thread.sleep(20000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result = client.testString("");
        return i;
    }

    @Override
    public double testDouble(double d) throws TException {
        Map<String, String> mtraceOneStepContext = Tracer.getServerSpan().getRemoteOneStepContext();
        Map<String, String> mtraceForeverContext = Tracer.getServerSpan().getForeverContext();
        assert (mtraceForeverContext.get("foreverContext") != null);
        assert (mtraceOneStepContext.get("oneStepContext") != null);
        return d;
    }

    @Override
    public ByteBuffer testBinary(ByteBuffer b) throws TException {
        return b;
    }

    @Override
    public String testString(String s) throws TException {
        Map<String, String> mtraceOneStepContext = Tracer.getServerSpan().getRemoteOneStepContext();
        Map<String, String> mtraceForeverContext = Tracer.getServerSpan().getForeverContext();

        assert (mtraceOneStepContext.get("oneStepContext") != null);
        assert (mtraceForeverContext.get("foreverContext") != null);

        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("testSuite/stressMarkTest/another-client.xml");
        ClassPathXmlApplicationContext server =
                new ClassPathXmlApplicationContext("testSuite/stressMarkTest/another-server.xml");
        Twitter.Iface client = applicationContext
                .getBean("clientProxy", Twitter.Iface.class);

        try {
            Thread.sleep(30000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result = client.testString(s);

        return result;
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
    public Map<String, String> testMap(Map<String, String> m)
            throws TException {
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
    public TweetSearchResult testStruct(String query)
            throws TException {
        TweetSearchResult result = new TweetSearchResult();
        result.addToTweets(new Tweet(1, "1", "1"));
        result.addToTweets(new Tweet(2, "2", "2"));
        result.addToTweets(new Tweet(3, "3", "3"));

        return result;
    }

    @Override
    public String testException(Tweet tweet)
            throws TwitterUnavailable, TException {
        throw new TwitterUnavailable("exception");
    }
}
