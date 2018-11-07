package com.meituan.mtrace.octo;

import com.sankuai.sgagent.thrift.model.SGModuleInvokeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by IntelliJ IDEA.
 * User: mansari
 * Date: 4/12/11
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */

public class InvokeInfoQueue {
    private static final Logger LOG = LoggerFactory.getLogger(InvokeInfoQueue.class);

    public static final int DEFAULT_SIZE = 10000;
    private int size = DEFAULT_SIZE;

    public static final int DEFAULT_MAX_DISCARDED_SIZE = 1024;
    private int maxDiscardedSize = DEFAULT_MAX_DISCARDED_SIZE;
    private int discardedSize = 1;
    private int discardedNum = 0;

    public void setSize(int size) {
        if (size > 0) {
            this.size = size;
        } else {
            this.size = DEFAULT_SIZE;
        }
    }

    public void setMaxDiscardedSize(int maxDiscardedSize) {
        if (maxDiscardedSize > 0) {
            this.maxDiscardedSize = maxDiscardedSize;
        } else {
            this.maxDiscardedSize = DEFAULT_MAX_DISCARDED_SIZE;
        }
    }

    private Queue<SGModuleInvokeInfo> queue = new ConcurrentLinkedQueue<SGModuleInvokeInfo>();

    public synchronized void put(SGModuleInvokeInfo info) {
        queue.add(info);
        if (queue.size() > this.size) {
            queue.poll();
            discardLogEntry();
        }
    }

    public synchronized SGModuleInvokeInfo get() {
        return queue.poll();
    }

    private void discardLogEntry() {
        discardedNum++;
        if (discardedNum >= discardedSize) {
            LOG.info("{} are discarded", discardedSize);
            discardedNum = 0;
            if (discardedSize < maxDiscardedSize) {
                discardedSize *= 2;
                if (discardedSize > maxDiscardedSize) {
                    discardedSize = maxDiscardedSize;
                }
            }
        }
    }
}
