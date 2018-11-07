package com.meituan.mtrace.octo;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.Validate;
import com.sankuai.sgagent.thrift.model.DropRequest;
import com.sankuai.sgagent.thrift.model.DropRequestList;
import com.sankuai.sgagent.thrift.model.TraceThresholdLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class AsyncOctoCollector {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncOctoCollector.class);
    public static final int DEFAULT_BUGGER_SIZE = 512;
    private final OctoCollector collector;
    private final Queue<Span> buffer;
    private Span discard;
    private Thread dispatcher;
    private int bufferSize = DEFAULT_BUGGER_SIZE;
    private final ExecutorService executor;
    private LinkedBlockingQueue<TraceThresholdLog> slowQueue = new LinkedBlockingQueue<TraceThresholdLog>(1000);

    private AsyncOctoCollector() {
        collector = new OctoCollector();
        buffer = new LinkedList<Span>();
        bufferSize = getIntProperty("mtrace.bufferSize", DEFAULT_BUGGER_SIZE);

        executor = Executors.newFixedThreadPool(1, new ThreadFactory() {
            final ThreadFactory delegate = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread result = delegate.newThread(r);
                result.setName("Octo Collector" + result.getName());
                result.setDaemon(true);
                return result;
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                LinkedList<TraceThresholdLog> logList = new LinkedList<TraceThresholdLog>();
                while (!executor.isShutdown() || slowQueue.size() > 0) {
                    try {
                        long start = System.currentTimeMillis();
                        boolean wait = true;
                        while (wait) {
                            TraceThresholdLog log = slowQueue.poll(100, TimeUnit.MILLISECONDS);
                            if (log != null) {
                                logList.add(log);
                            }
                            long end = System.currentTimeMillis();
                            wait = logList.size() < 100 && (end - start) < 100;
                        }
                        if (!logList.isEmpty()) {
                            // 同步发送，序列化时复用了logList
                            collector.sendSlowQuerys(logList);
                            logList.clear();
                        }
                    } catch (Exception e) {
                        // TODO exception hander, can't log
                    }
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    LOG.info("shutdown executor with queue size " + slowQueue.size());
                    executor.shutdown();
                    executor.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    LOG.warn("shutdown executor catch exception", e);
                }
            }
        });
        dispatcher = new Thread(new Dispatcher(), "Octo Dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    public void collectSlowQuery(final TraceThresholdLog log) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("collect TraceThresholdLog : {}", log);
        }
        slowQueue.offer(log);
    }

    public void collectDropRequest(List<DropRequest> list) {
        // direct sync send
        if (list != null && !list.isEmpty()) {
            collector.sendDropRequest(new DropRequestList(list));
        }
    }

    private static class AsyncOctoCollectorHolder {
        private static final AsyncOctoCollector instance = new AsyncOctoCollector();
    }

    public static AsyncOctoCollector getInstance() {
        return AsyncOctoCollectorHolder.instance;
    }

    private int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void collect(final Span span) {
        Validate.notNull(span);
        if (dispatcher == null || !dispatcher.isAlive() || bufferSize <= 0) {
            collector.collect(span);
            return;
        }
        synchronized (buffer) {
            int previousSize = buffer.size();
            if (previousSize < bufferSize) {
                buffer.offer(span);
                if (previousSize == 0) {
                    buffer.notifyAll();
                }
                return;
            }
            discard = buffer.poll();
            buffer.offer(span);
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private class Dispatcher implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Span[] spans = null;
                    Span tmpDiscard = null;
                    synchronized (buffer) {
                        int bufferSize = buffer.size();
                        while (bufferSize == 0) {
                            buffer.wait();
                            bufferSize = buffer.size();
                        }
                        if (bufferSize > 0) {
                            spans = new Span[bufferSize];
                            buffer.toArray(spans);
                            tmpDiscard = discard;
                            buffer.clear();
                            discard = null;
                            buffer.notifyAll();
                        }
                    }


                    if (spans != null) {
                        for (Span span : spans) {
                            collector.collect(span);
                        }
                        if (tmpDiscard != null && LOG.isDebugEnabled()) {
                            LOG.debug("buffer is full: " + tmpDiscard);
                        }
                    }
                } catch (Exception e) {
                    if (collector != null && collector.isPrintExceptionStack()) {
                        LOG.info(e.getMessage(), e);
                    } else {
                        LOG.info(e.getMessage());
                    }
                }
            }
        }
    }
}
