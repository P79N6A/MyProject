package com.meituan.service.mobile.mtthrift.monitor.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: YangXuehua
 * Date: 13-8-22
 * Time: 下午7:19
 */
@Deprecated
public abstract class Slot {
    private static final Logger LOG = LoggerFactory.getLogger(Slot.class);
    protected final static int secoundsPerPeriod = 60;
    private final static LinkedList<Slot> _instances = new LinkedList<Slot>();
    private static ScheduledExecutorService scheduExec = null;

    protected Slot() {
        synchronized (_instances) {
            _instances.addFirst(this);
        }
    }

    public void destroy() {
        synchronized (_instances) {
            _instances.remove(this);
        }
    }

    static {
        if(scheduExec==null){
            scheduExec = Executors.newScheduledThreadPool(1);
            scheduExec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (_instances.size() > 0) {
                            List<Slot> all = new ArrayList<Slot>();
                            all.addAll(_instances);
                            for (Slot _instance : all) {
                                _instance.pollCheck();
                            }
                        }
                    } catch (Exception e) {
                        LOG.debug("", e);
                    }
                }
            }, secoundsPerPeriod, secoundsPerPeriod, TimeUnit.SECONDS);
        }
    }

    protected abstract void pollCheck();

}
