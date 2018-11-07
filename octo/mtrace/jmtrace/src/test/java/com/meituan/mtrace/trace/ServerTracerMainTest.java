package com.meituan.mtrace.trace;

import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;

/**
 * @author zhangzhitong
 * @created 1/28/16
 */
public class ServerTracerMainTest {
    static class TraceThread extends Thread {
        private String name;

        public TraceThread(String name) {
            this.name = name;
        }

        public void run() {
            for (int i = 0; i < 100; i++) {
                TraceParam param = new TraceParam(name + " " + i);
                param.setLocal("com.meituan.mtrace.trace", "127.0.0.1", 1234);
                param.setRemote("com.meituan.mtrace.trace", "127.0.0.2", 8080);
                param.setInfraName("http");
                Tracer.serverRecv(param);
                for (int j = 0; j < 3; ++j) {
                    Tracer.clientSend(new TraceParam(name + " client " + j));
                    Tracer.clientRecv();
                }
                Tracer.serverSend();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; ++i) {
            Thread t = new TraceThread("ClientTracerMainTest" + i);
            t.start();
            t.join();
        }

    }
}
