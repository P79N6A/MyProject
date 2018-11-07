package com.meituan.mtrace.collector;

import com.meituan.mtrace.Convert;
import com.meituan.mtrace.MLog;
import com.meituan.mtrace.Span;
import com.meituan.mtrace.octo.OctoCollector;
import com.meituan.mtrace.thrift.model.ThriftSpan;
import com.meituan.mtrace.thrift.model.ThriftSpanList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * trace log collect and upload
 */
public final class TraceCollector extends AbstractCollector<Span> implements ISample {
    private final static Logger logger = LoggerFactory.getLogger(TraceCollector.class);
    private OctoCollector octoCollector;
    public final static int MAX_INTERVAL = 8 * 1024;
    public final static int MIN_INTERVAL = 2;
    private volatile int uploadSize = 0;
    private Random random = new Random();

    public TraceCollector() {
        super(TraceCollector.class.getSimpleName());
        octoCollector = new OctoCollector();
        setInterval(MIN_INTERVAL);
        start();
    }

    /**
     * 上报逻辑, 根据每次上报队列中取出的数据大小, 动态变更上报的周期
     *
     * @param spans 上传列表
     * @return 上报是否success
     */
    @Override
    protected boolean upload(List<Span> spans) {
        boolean status = true;
        uploadSize = spans.size();
        if (!spans.isEmpty()) {
            ThriftSpanList thriftSpanList = new ThriftSpanList();
            for (Span span : spans) {
                ThriftSpan thriftSpan = Convert.spanToThrift(span);
                if (thriftSpan != null) {
                    thriftSpanList.addToSpans(thriftSpan);
                    MLog.info(thriftSpan);
                }
            }
            status = octoCollector.sendTraceLogs(thriftSpanList);
        }
        int uploadSize = spans.size();
        if (uploadSize >= UPLOAD_SIZE && interval > MIN_INTERVAL) {
            interval = interval / 2;
        } else if (uploadSize < UPLOAD_SIZE && interval < MAX_INTERVAL) {
            interval = interval * 2;
        }
        return status;
    }

    public boolean isSample() {
        if (!this.isActive) {
            return false;
        }
        if (uploadSize < 64) {
            return true;
        } else if (uploadSize < UPLOAD_SIZE) {
            return random.nextDouble() < (double) 64 / uploadSize;
        } else {
            return random.nextDouble() < ((double) 64 / uploadSize * interval / MAX_INTERVAL);
        }
    }

}
