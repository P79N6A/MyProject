package com.meituan.mtrace.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractCollector<T> implements ICollector<T> {
    private final static Logger logger = LoggerFactory.getLogger(AbstractCollector.class);
    protected final String name;
    protected int size;
    protected int uploadSize;
    protected int interval;
    protected final AtomicInteger failCount = new AtomicInteger(0);
    public final static int MAX_QUEUE_SIZE = 1024 * 16;
    public final static int DEFAULT_INTERVAL = 1024;
    public final static int UPLOAD_SIZE = 1024 * 2;
    protected BlockingQueue<T> queue;
    protected ArrayList<T> retryList;
    public volatile boolean isActive = true;
    protected Thread t;

    public AbstractCollector(String name) {
        this.name = name;
        this.interval = DEFAULT_INTERVAL;
        this.size = MAX_QUEUE_SIZE;
        this.uploadSize = UPLOAD_SIZE;
        addShutdownHook();
    }

    protected void start() {
        queue = new LinkedBlockingQueue<T>(size);
        retryList = new ArrayList<T>(uploadSize);
        t = new CollectorThreadFactory(this.name).newThread(new Dispatcher());
        t.start();
        logger.debug("Collector " + name + " start size " + size + " interval " + interval);
    }

    public void collect(final T t) {
        if (t != null && this.isActive) {
            if (!queue.offer(t)) {
                failCount.incrementAndGet();
            }
        }
    }


    /**
     * 上报函数, 批量上报接口, 由Dispatcher线程调用
     *
     * @param ts 上报数据
     * @return 是否上报成功
     */
    protected abstract boolean upload(List<T> ts);

    protected void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                isActive = false;
                List<T> ts = new ArrayList<T>(size);
                // TODO 有待优化,极端情况下queue会很大, 一次性上报数据过多
                queue.drainTo(ts,uploadSize);
                logger.debug("Collector " + name + " before shutdown upload " + ts.size() +
                        " retry " + retryList.size() +
                        " fail " + failCount.getAndSet(0));
                if (retryList.size() > 0) {
                    ts.addAll(retryList);
                    retryList.clear();
                }
                if (ts.size() > 0) {
                    upload(ts);
                }
            }
        });
    }

    private static class CollectorThreadFactory implements ThreadFactory {

        private final String name;

        public CollectorThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(name);
            thread.setDaemon(true);
            return thread;
        }
    }

    private class Dispatcher implements Runnable {

        @Override
        public void run() {
            while (isActive) {
                List<T> total = new ArrayList<T>(uploadSize * 2);
                List<T> ts = new ArrayList<T>(uploadSize);
                queue.drainTo(ts, uploadSize);
                logger.debug("Collector " + name + " dispatcher loop interval " + interval +
                        " upload " + ts.size() +
                        " retry " + retryList.size() +
                        " fail " + failCount.getAndSet(0));
                if (retryList.size() > 0) {
                    total.addAll(retryList);
                    retryList.clear();
                }
                total.addAll(ts);
                if (!upload(total)) {
                    retryList.addAll(ts);
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    isActive = false;
                }
            }
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getUploadSize() {
        return uploadSize;
    }

    public void setUploadSize(int uploadSize) {
        this.uploadSize = uploadSize;
    }
}

