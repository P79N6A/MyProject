package com.meituan.mtrace.collector;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangzhitong
 * @created 1/22/16
 */
public class AbstractCollectorTest {
    Logger logger = LoggerFactory.getLogger(AbstractCollectorTest.class);

    @Before
    public void setUp() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

    }


    /**
     * TestCollector 每5ms上报一次,队列大小为1000
     */
    private static class TestCollector extends AbstractCollector<String> {
        public AtomicInteger total = new AtomicInteger(0);
        private Random random = new Random();

        public TestCollector() {
            super("Test Collector");
            setInterval(5);
            setSize(1000);
            start();
        }

        @Override
        protected boolean upload(List<String> strings) {
            int rnd = random.nextInt();
            if (rnd % 10 != 0) {
                total.addAndGet(strings.size());
            } else {
                return false;
            }
            return true;
        }
    }

    @Test
    public void test() throws InterruptedException {
        TestCollector collector = new TestCollector();
        for (int i = 0; i < 500; ++i) {
            collector.collect("item " + i);
            Thread.sleep(5);
        }
        Thread.sleep(5);
        logger.info("AbstractCollectorTest collect " + collector.total.get());

    }
}
