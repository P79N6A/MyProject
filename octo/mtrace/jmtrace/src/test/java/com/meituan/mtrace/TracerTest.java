package com.meituan.mtrace;

import com.sun.corba.se.spi.activation.Server;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangxi
 * @created 13-11-4
 */
@Ignore
public class TracerTest {

    Logger logger  = LoggerFactory.getLogger(ClientTracerTest.class);
    public static AtomicLong debug = new AtomicLong(0);
    public static AtomicLong count = new AtomicLong(0);
    class Service {
        String appKey = "com.meituan.mtrace.Test";
        String spanName = "Service.invoke";
        String ip = "127.0.0.1";
        int port = 80;
        String infraName = "server-1.1.0";

        public void service() {
            TraceParam param = new TraceParam(spanName);
            param.setLocal(appKey, ip, port);
            param.setInfraName(infraName);
            Tracer.serverRecv(param);

            TraceParam client1Param = new TraceParam("call1");
            client1Param.setRemote("com.mtrace.TestRemote1", "", 0);
            TraceParam client2Param = new TraceParam("call2");
            client2Param.setRemote("com.mtrace.TestRemote2", "", 0);
            Tracer.clientSend(client1Param);
            Span span1 = Tracer.clientRecv();
            Tracer.clientSend(client2Param);
            Span span2 = Tracer.clientRecv();
            Span serverSpan = Tracer.serverSend();
            //logger.info(span1.toString());
            //logger.info(span2.toString());
            if (serverSpan.isDebug()) {
                debug.incrementAndGet();
            }
            count.incrementAndGet();
            //logger.info(serverSpan.toString());

        }

    }

    @Test
    public void testLog() {
        DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.ENGLISH);
        String dateString = formater.format(new Date());
        System.out.println(dateString);
    }

    @Test
    public void testTrace() {
        Service service = new Service();
        service.service();
    }

    @Test
    public void testTraces() {
        int threadNum = 10;
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < threadNum; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Service s = new Service();
                    for (int i = 0; i < 1000; ++i) {
                        s.service();
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long t2 = System.currentTimeMillis();
        logger.info("debug : " + debug + ", count : " + count + ", duration : " + (t2 - t1));
    }
}
