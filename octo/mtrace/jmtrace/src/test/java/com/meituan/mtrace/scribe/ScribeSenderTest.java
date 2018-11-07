package com.meituan.mtrace.scribe;

import com.meituan.mtrace.collector.ScribeLogCollector;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ScribeSenderTest {
    private ScribeSender sender;
    @Test
    public void connect() {
        sender = new ScribeSender();
    }
    @Test
    public void testCollector() throws InterruptedException {
        ScribeLogCollector collector = new ScribeLogCollector();
        Thread.sleep(100000);
    }



}
