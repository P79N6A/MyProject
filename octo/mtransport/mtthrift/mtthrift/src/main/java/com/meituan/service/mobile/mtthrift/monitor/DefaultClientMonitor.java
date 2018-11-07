package com.meituan.service.mobile.mtthrift.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-12-18
 * Time: 上午11:38
 * 客户端缺省监控
 */
public class DefaultClientMonitor implements IClientMonitor {
    private final static Logger logger = LoggerFactory.getLogger(DefaultClientMonitor.class);
    private int invokeLimitMillSec2log = 1000;
    private int borrowConnLimitMillSec2log = 100;

    public void setInvokeLimitMillSec2log(int invokeLimitMillSec2log) {
        this.invokeLimitMillSec2log = invokeLimitMillSec2log;
    }

    public void setBorrowConnLimitMillSec2log(int borrowConnLimitMillSec2log) {
        this.borrowConnLimitMillSec2log = borrowConnLimitMillSec2log;
    }

    @Override
    public void noticeInvoke(String serviceName, String methodName, String serverIpPort, long takesMills) {
        if (takesMills > invokeLimitMillSec2log)
            logger.warn(serviceName + "." + methodName + " (rpc:" + serverIpPort + ") take " + takesMills);
    }

    @Override
    public void noticeGetConnect(String serviceName, long takesMills) {
        if (takesMills > borrowConnLimitMillSec2log)
            logger.warn(serviceName + " get thrift remote-Connection take " + takesMills);
    }

    @Override
    public void noticeException(String serviceName, String methodName, String serverIpPort, String exceptionMessage, Throwable e) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }
}
