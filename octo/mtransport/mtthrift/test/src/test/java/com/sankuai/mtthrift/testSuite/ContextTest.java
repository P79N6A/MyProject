package com.sankuai.mtthrift.testSuite;

import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.util.ContextUtil;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ContextTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;
    private static Twitter.AsyncIface asyncClient;

    private static Twitter.Iface nettyClient;
    private static Twitter.AsyncIface nettyAsyncClient;

    private static Twitter.Iface client1;
    private static Twitter.AsyncIface asyncClient1;

    private static Twitter.Iface nettyClient1;
    private static Twitter.AsyncIface nettyAsyncClient1;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/contextTest/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/contextTest/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        asyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncClientProxy");

        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");
        nettyAsyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("nettyAsyncClientProxy");

        client1 = (Twitter.Iface) clientBeanFactory.getBean("clientProxy1");
        asyncClient1 = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncClientProxy1");

        nettyClient1 = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy1");
        nettyAsyncClient1 = (Twitter.AsyncIface) clientBeanFactory.getBean("nettyAsyncClientProxy1");
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
    public void baseTypeTest() throws InterruptedException, ExecutionException {

        String s = "test123456";

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                String result = client.testString(s);
                System.out.println(j);
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                OctoThriftCallback callback = new OctoThriftCallback();
                asyncClient.testString(s, callback);
                System.out.println(j);
                String result = (String) callback.getFuture().get();
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testNetty() throws ExecutionException, InterruptedException {
        String s = "test123456";

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                String result = nettyClient.testString(s);
                System.out.println(j);
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                OctoThriftCallback callback = new OctoThriftCallback();
                nettyAsyncClient.testString(s, callback);
                System.out.println(j);
                String result = (String) callback.getFuture().get();
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }

//        @Test
    public void containersTest(){
        try {
            List<String> l = new ArrayList<String>();
            l.add("a");
            l.add("b");
            l.add("c");
            List<String> result = client.testList(l);
            assert (l.equals(result));
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            Set<String> s = new HashSet<String>();
            s.add("a");
            s.add("b");
            s.add("c");
            Set<String> result = client.testSet(s);
            assert (s.equals(result));
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            Map<String, String> m = new HashMap<String, String>();
            m.put("1", "a");
            m.put("2", "b");
            m.put("3", "c");
            Map<String, String> result = client.testMap(m);
            assert (m.equals(result));
        } catch (TException e){
            e.printStackTrace();
        }
    }

//        @Test
    public void otherTest(){
        try {
            client.testVoid();
        } catch (TException e){
            e.printStackTrace();
        }

//        try {
//            String result = client.testReturnNull();
//            assert (result == null);
//        } catch (TException e){
//            e.printStackTrace();
//        }

        try {
            List<Tweet> tweets = new ArrayList<Tweet>();
            tweets.add(new Tweet(1, "1", "1"));
            tweets.add(new Tweet(2, "2", "2"));
            tweets.add(new Tweet(3, "3", "3"));
            TweetSearchResult tweetSearchResult = new TweetSearchResult(tweets);
            TweetSearchResult result = client.testStruct("test");
            assert (tweetSearchResult.getTweets().equals(result.getTweets()));
        } catch (TException e){
            e.printStackTrace();
        }

        try {
            /**
             * 当返回值为bool、int等基本类型时，thrift 0.8 不会抛出自定义异常
             */
            client.testException(new Tweet(1, "1", "1"));
        } catch (TwitterUnavailable twitterUnavailable) {
            assert (twitterUnavailable.getMessage().endsWith("exception"));
        } catch (TException e){
            e.printStackTrace();
        }
    }


    @Test
    public void baseTypeTest1() throws InterruptedException, ExecutionException {
        String s = "test123456";

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                String result = client1.testString(s);
                System.out.println(j);
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                OctoThriftCallback callback = new OctoThriftCallback();
                asyncClient1.testString(s, callback);
                System.out.println(j);
                String result = (String) callback.getFuture().get();
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testNetty1() throws ExecutionException, InterruptedException {
        String s = "test123456";

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                String result = nettyClient1.testString(s);
                System.out.println(j);
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }

        try {
            for(int j = 0; j < 1; j++) {
                ContextUtil.setLocalContext(new HashMap<String, String>() {
                    {
                        put("local-1", "context-1");
                        put("local-2", "context-2");
                    }
                });
                Tracer.serverRecv(new TraceParam("test service"));
                OctoThriftCallback callback = new OctoThriftCallback();
                nettyAsyncClient1.testString(s, callback);
                System.out.println(j);
                String result = (String) callback.getFuture().get();
                System.out.println(result);
                assert (s.equals(result));
                Tracer.serverSend();
            }
        } catch (TException e){
            Assert.fail(e.getMessage());
        }
    }

}
