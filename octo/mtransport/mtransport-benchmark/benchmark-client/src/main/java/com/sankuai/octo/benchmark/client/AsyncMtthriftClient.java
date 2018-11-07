package com.sankuai.octo.benchmark.client;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.sankuai.octo.benchmark.thrift.EchoService;
import com.sankuai.octo.benchmark.thrift.Message;
import com.sankuai.octo.benchmark.utils.ThriftMsgGenerator;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-27
 * Time: 上午10:13
 */
public class AsyncMtthriftClient {

    private final static Logger logger = LoggerFactory.getLogger(AsyncMtthriftClient.class);

    public int threadNum;
    public long msgCount;
    public int msgLength;
    public String msgType;

    private AtomicLong totalCount = new AtomicLong(0);

    private static Histogram histogram;
    private static List<Message> msgList;
    private static String str;
    private static ByteBuffer buffer;

    private static EchoService.AsyncIface client;
    private static ClassPathXmlApplicationContext clientBeanFactory;


    public static void main(String[] args) throws InterruptedException {

        int threadNum = 1;
        long msgCount = 1000000;
        int msgLength = 1000;
        String msgType = "string";

        if (null != args) {
            if (args.length >= 1) {
                threadNum = Integer.parseInt(args[0]);
            }
            if (args.length >= 2) {
                msgCount = Long.parseLong(args[1]);
            }
            if (args.length >= 3) {
                msgLength = Integer.parseInt(args[2]);
            }
            if (args.length >= 4) {
                msgType = args[3];
            }
        }

        AsyncMtthriftClient asyncMtthriftClient = new AsyncMtthriftClient(threadNum, msgCount, msgLength, msgType);
        asyncMtthriftClient.test();
        System.exit(0);
    }

    public AsyncMtthriftClient(int threadNum, long msgCount, int msgLength, String msgType) {
        this.threadNum = threadNum;
        this.msgCount = msgCount;
        this.msgLength = msgLength;
        this.msgType = msgType;
    }

    public void test() {

        clientBeanFactory = new ClassPathXmlApplicationContext("mtthrift/asyncClient.xml");
        client = (EchoService.AsyncIface) clientBeanFactory.getBean("clientProxy");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        initMsg();
        histogram = new Histogram(new UniformReservoir(10000));
        totalCount = new AtomicLong(0);

        Long start = System.currentTimeMillis();
        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new TestThread(i);
            threads[i].start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < threadNum; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Long end = System.currentTimeMillis();

        logger.info("*****AsyncMtthrift:ThreadNum:" + threadNum + " MsgCount:" + msgCount + " MsgLength:" + msgLength + " MsgType:" + msgType + "*****");
        logger.info("TPS:" + msgCount * 1000 / (end - start));
        logger.info("99th:" + histogram.getSnapshot().get99thPercentile());
        logger.info("95th:" + histogram.getSnapshot().get95thPercentile());
        logger.info("Mean:" + histogram.getSnapshot().getMean());
        logger.info("Median:" + histogram.getSnapshot().getMedian());
        logger.info("Max:" + histogram.getSnapshot().getMax());
        logger.info("check histogram count:" + histogram.getCount());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clientBeanFactory.destroy();
    }

    public void initMsg() {
        buffer = ThriftMsgGenerator.getRandomBuffer(msgLength);
        str = ThriftMsgGenerator.getRandomString(msgLength);
        msgList = ThriftMsgGenerator.getRandomMessageList(msgLength);
    }

    class TestThread extends Thread {

        long id = 0;
        private int index;

        public TestThread(int threadIndex) {
            index = threadIndex;
        }

        public void run() {

            try {
                while (true) {
                    id = totalCount.incrementAndGet();
                    if (id > msgCount) {
                        break;
                    }
                    long before = System.currentTimeMillis();
                    if ("pojo".equals(msgType)) {
                        client.sendPojo(msgList, new SendPojoCallback());
                    } else if ("string".equals(msgType)) {
                        client.sendString(str, new SendStringCallback());
                    } else if ("byte".equals(msgType)) {
                        client.sendBytes(buffer, new SendBytesCallback());
                    }
                    long after = System.currentTimeMillis();
                    long cost = after - before;
                    histogram.update(cost);
                }

            } catch (TTransportException e) {
                e.printStackTrace();
            } catch (TException e) {
                e.printStackTrace();
            }
        }
    }

    class SendPojoCallback implements AsyncMethodCallback<EchoService.AsyncClient.sendPojo_call> {


        public void onComplete(EchoService.AsyncClient.sendPojo_call sendPojo_call) {

        }

        public void onError(Exception e) {

        }
    }


    class SendStringCallback<T> implements AsyncMethodCallback<T> {


        public void onComplete(T t) {
//            try {
//                logger.info(sendString_call.getResult().toString());
//            } catch (TException e) {
//                e.printStackTrace();
//            }

//            try {
//                Method m = t.getClass().getMethod("getResult");
//                System.out.println(m.invoke(t));
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
        }

        public void onError(Exception e) {

        }
    }




    class SendBytesCallback implements AsyncMethodCallback<EchoService.AsyncClient.sendBytes_call> {


        public void onComplete(EchoService.AsyncClient.sendBytes_call sendBytes_call) {

        }

        public void onError(Exception e) {

        }
    }



}
