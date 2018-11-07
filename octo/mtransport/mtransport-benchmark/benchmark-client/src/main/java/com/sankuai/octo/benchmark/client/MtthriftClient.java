package com.sankuai.octo.benchmark.client;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.sankuai.octo.benchmark.thrift.EchoService;
import com.sankuai.octo.benchmark.thrift.Message;
import com.sankuai.octo.benchmark.utils.ThriftMsgGenerator;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-5
 * Time: 下午12:39
 */
public class MtthriftClient {

    private final static Logger logger = LoggerFactory.getLogger(MtthriftClient.class);

    public int threadNum;
    public long msgCount;
    public int msgLength;
    public String msgType;

    private AtomicLong totalCount = new AtomicLong(0);
    private AtomicLong errorCount = new AtomicLong(0);

    private static Histogram histogram;
    private static List<Message> msgList;
    private static String str;
    private static ByteBuffer buffer;

    private static EchoService.Iface client;
    private static ClassPathXmlApplicationContext clientBeanFactory;


    public static void main(String[] args) throws InterruptedException {

        int threadNum = 400;
        long msgCount = 10000000;
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

        MtthriftClient mtthriftClient = new MtthriftClient(threadNum, msgCount, msgLength, msgType);
        mtthriftClient.test();
        System.exit(0);
    }

    public MtthriftClient(int threadNum, long msgCount, int msgLength, String msgType) {
        this.threadNum = threadNum;
        this.msgCount = msgCount;
        this.msgLength = msgLength;
        this.msgType = msgType;
    }

    public void test() {

        clientBeanFactory = new ClassPathXmlApplicationContext("mtthrift/client.xml");
        client = (EchoService.Iface) clientBeanFactory.getBean("clientProxy");
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

        logger.info("*****mtthrift:ThreadNum:" + threadNum + " MsgCount:" + msgCount + " MsgLength:" + msgLength + " MsgType:" + msgType + "*****");
        logger.info("TPS:" + msgCount * 1000 / (end - start));
        logger.info("99th:" + histogram.getSnapshot().get99thPercentile());
        logger.info("95th:" + histogram.getSnapshot().get95thPercentile());
        logger.info("Mean:" + histogram.getSnapshot().getMean());
        logger.info("Median:" + histogram.getSnapshot().getMedian());
        logger.info("Max:" + histogram.getSnapshot().getMax());
        logger.info("Error Rate:" + errorCount.get() * 1.f / histogram.getCount() * 100 + "%");
        logger.info("check histogram count:" + histogram.getCount());

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


            while (true) {
                id = totalCount.incrementAndGet();
                if (id > msgCount) {
                    break;
                }
                long before = System.currentTimeMillis();
                try {
                    if ("pojo".equals(msgType)) {
                        List<Message> res = client.sendPojo(msgList);
                    } else if ("string".equals(msgType)) {
                        String res = client.sendString(str);
                    } else if ("byte".equals(msgType)) {
                        ByteBuffer res = client.sendBytes(buffer);
                    }
                } catch (TException e) {
                    errorCount.incrementAndGet();
                }
                long after = System.currentTimeMillis();
                long cost = after - before;
                histogram.update(cost);
//                try {
//                    System.out.println(client.sendString(str));
//                } catch (TException e) {
//                    e.printStackTrace();
//                }
            }

        }
    }
}
