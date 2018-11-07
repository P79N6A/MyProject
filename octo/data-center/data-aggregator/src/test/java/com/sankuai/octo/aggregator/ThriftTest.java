package com.sankuai.octo.aggregator;

import com.sankuai.octo.aggregator.thrift.model.CommonLog;
import com.sankuai.octo.aggregator.thrift.model.DropRequest;
import com.sankuai.octo.aggregator.thrift.model.DropRequestList;
import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo;
import com.sankuai.octo.aggregator.thrift.service.LogCollectorService;
import com.sankuai.octo.statistic.constant.Constants;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThriftTest {
    private TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testLog() throws InterruptedException {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final AtomicLong time = new AtomicLong(0L);
        int max = 100000;
        final CountDownLatch countDownLatch = new CountDownLatch(max);
        final long start = System.currentTimeMillis();

        ExecutorService service = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < max; i++) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    int count = 1000;
                    logger.info("start test!");
                    LogCollectorService.Client client = init();
                    while (count-- > 0) {
                        try {
                            int ret = client.uploadModuleInvoke(randomInvokeInfo());
                            logger.info("ret:" + ret);
                            atomicInteger.incrementAndGet();
                            Thread.sleep(1);
                        } catch (TException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (count % 100000 == 0) {
                            time.set(System.currentTimeMillis() - start);
                            logger.info("count:" + atomicInteger.get());
                            logger.info("time:" + time.get());
                        }
                    }
                    countDownLatch.countDown();
                }
            };
            service.submit(r);
        }
        time.set(System.currentTimeMillis() - start);
        countDownLatch.await(1000, TimeUnit.SECONDS);
        logger.info("count:" + atomicInteger.get());
        logger.info("time:" + time.get());
    }

    @Test
    public void testClient() throws TException {
        LogCollectorService.Client client = init();
        client.uploadModuleInvoke(randomInvokeInfo());
        client.uploadCommonLog(randomCommonLog());
    }

    private CommonLog randomCommonLog() throws TException {
        CommonLog log = new CommonLog();
        log.setCmd(Constants.DROP_REQUEST_LOG);
        DropRequest request = new DropRequest();
        request.setAppkey("mtupm");
        request.setCount(5L);
        request.setHost("192.168.1.111");
        request.setRemoteAppkey("mtupm.test22");
        request.setSpanname("TestController.test2");
        request.setStart(System.currentTimeMillis());
        request.setType(1);
        DropRequestList list = new DropRequestList();
        list.addToRequests(request);
        byte[] bytes = serializer.serialize(list);
        log.setContent(bytes);
        return log;
    }

    private LogCollectorService.Client init() {
        try {
            TTransport transport = new TFramedTransport(new TSocket("10.4.245.181", 8920), 16384000);
            //TTransport transport = new TFramedTransport(new TSocket("192.168.12.164", 8920, 10000), 16384000);
            //TTransport transport = new TFramedTransport(new TSocket("192.168.11.90", 52999));
            TProtocol protocol = new TBinaryProtocol(transport);
            LogCollectorService.Client client = new LogCollectorService.Client(protocol);
            transport.open();
            return client;
        } catch (TTransportException e) {
            logger.error("init", e);
            throw new RuntimeException("init fail", e);
        }

    }

    private SGModuleInvokeInfo randomInvokeInfo() {
        SGModuleInvokeInfo info = new SGModuleInvokeInfo();
        info.setSpanName("TestController.test2");
        info.setLocalAppKey("mtupm");
        info.setLocalHost("192.168.1.111");
        info.setRemoteAppKey("mtupm.test22");
        info.setRemoteHost("192.168.2.222");
        info.setType(1);
        info.setStatus(0);
        info.setCount(1);
        info.setCost(RandomUtils.nextInt(20) + 20);
        return info;
    }
}
