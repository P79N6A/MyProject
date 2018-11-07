package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.mtthrift.testSuite.idlTest.Tweet;
import com.sankuai.mtthrift.testSuite.idlTest.TweetSearchResult;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.mtthrift.testSuite.idlTest.TwitterUnavailable;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-22
 * Time: 上午10:17
 */
public class IdlInterfaceTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;
    private static Twitter.Iface nettyClient;

    @BeforeClass
    public static void start() throws InterruptedException {
        Thread.sleep(5000);
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/idlTest/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/idlTest/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");
        Thread.sleep(30000);

        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
        if (!ContextStore.getRequestMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    @Test
    public void baseTypeTest() throws InterruptedException {

        try {
            boolean b = true;
            boolean result = client.testBool(b);
            System.out.println("result:" + result);
            assert (result == b);

        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            byte b = 10;
            byte result = client.testByte(b);
            System.out.println(result);
            assert (result == b);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            short s = 100;
            short result = client.testI16(s);
            System.out.println(result);
            assert (result == s);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            int i = 1234;
            int result = client.testI32(i);
            System.out.println(result);
            assert (result == i);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            long l = 123456;
            long result = client.testI64(l);
            System.out.println(result);
            assert (result == l);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            double d = 123456.789;
            double result = client.testDouble(d);
            System.out.println(result);
            assert (result == d);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            ByteBuffer b = ByteBuffer.wrap("test".getBytes());
            ByteBuffer result = client.testBinary(b);
            assert (b.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }


        String s = "test123456";
        for (int i = 0; i < 100; i++) {
            s += s;
            if (s.length() > 10240)
                break;
        }

        try {
            for(int j = 0; j < 20; j++) {
                String result = client.testString(s);
                System.out.println(j);
                assert (s.equals(result));
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void containersTest(){
        try {
            List<String> l = new ArrayList<String>();
            l.add("a");
            l.add("b");
            l.add("c");
            List<String> result = client.testList(l);
            assert (l.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            Set<String> s = new HashSet<String>();
            s.add("a");
            s.add("b");
            s.add("c");
            Set<String> result = client.testSet(s);
            assert (s.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            Map<String, String> m = new HashMap<String, String>();
            m.put("1", "a");
            m.put("2", "b");
            m.put("3", "c");
            Map<String, String> result = client.testMap(m);
            assert (m.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void otherTest(){
        try {
            client.testVoid();
        } catch (TException e){
            Assert.fail(e.getMessage());
        }


        try {
            List<Tweet> tweets = new ArrayList<Tweet>();
            tweets.add(new Tweet(1, "1", "1"));
            tweets.add(new Tweet(2, "2", "2"));
            tweets.add(new Tweet(3, "3", "3"));
            TweetSearchResult tweetSearchResult = new TweetSearchResult(tweets);
            TweetSearchResult result = client.testStruct("test");
            assert (tweetSearchResult.getTweets().equals(result.getTweets()));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            /**
             * 当返回值为bool、int等基本类型时，thrift 0.8 不会抛出自定义异常
             */
            client.testException(new Tweet(1, "1", "1"));
        } catch (TwitterUnavailable twitterUnavailable) {

        } catch (TException e){
            e.printStackTrace();
            Assert.fail();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nettyBaseTypeTest() throws InterruptedException {
        Thread.sleep(15000);

        try {
            boolean b = true;
            boolean result = nettyClient.testBool(b);
            System.out.println("result:" + result);
            assert (result == b);

        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            byte b = 10;
            byte result = nettyClient.testByte(b);
            System.out.println(result);
            assert (result == b);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            short s = 100;
            short result = nettyClient.testI16(s);
            System.out.println(result);
            assert (result == s);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            int i = 1234;
            int result = nettyClient.testI32(i);
            System.out.println(result);
            assert (result == i);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            long l = 123456;
            long result = nettyClient.testI64(l);
            System.out.println(result);
            assert (result == l);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            double d = 123456.789;
            double result = nettyClient.testDouble(d);
            System.out.println(result);
            assert (result == d);
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            ByteBuffer b = ByteBuffer.wrap("test".getBytes());
            ByteBuffer result = nettyClient.testBinary(b);
            assert (b.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }


        String s = "test123456";
        for (int i = 0; i < 100; i++) {
            s += s;
            if (s.length() > 10240)
                break;
        }

        try {
            for(int j = 0; j < 20; j++) {
                String result = nettyClient.testString(s);
                System.out.println(j);
                assert (s.equals(result));
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyContainersTest(){
        try {
            List<String> l = new ArrayList<String>();
            l.add("a");
            l.add("b");
            l.add("c");
            List<String> result = nettyClient.testList(l);
            assert (l.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            Set<String> s = new HashSet<String>();
            s.add("a");
            s.add("b");
            s.add("c");
            Set<String> result = nettyClient.testSet(s);
            assert (s.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            Map<String, String> m = new HashMap<String, String>();
            m.put("1", "a");
            m.put("2", "b");
            m.put("3", "c");
            Map<String, String> result = nettyClient.testMap(m);
            assert (m.equals(result));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyOtherTest(){
        try {
            nettyClient.testVoid();
        } catch (TException e){
            Assert.fail(e.getMessage());
        }


        try {
            List<Tweet> tweets = new ArrayList<Tweet>();
            tweets.add(new Tweet(1, "1", "1"));
            tweets.add(new Tweet(2, "2", "2"));
            tweets.add(new Tweet(3, "3", "3"));
            TweetSearchResult tweetSearchResult = new TweetSearchResult(tweets);
            TweetSearchResult result = nettyClient.testStruct("test");
            assert (tweetSearchResult.getTweets().equals(result.getTweets()));
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            /**
             * 当返回值为bool、int等基本类型时，thrift 0.8 不会抛出自定义异常
             */
            nettyClient.testException(new Tweet(1, "1", "1"));
        } catch (TwitterUnavailable twitterUnavailable) {

        } catch (TException e){
            e.printStackTrace();
            Assert.fail();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
