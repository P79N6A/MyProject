package com.sankuai.octo.test.thrift.service.impl;

import com.meituan.service.mobile.mtthrift.server.MTIface;
import com.sankuai.octo.test.thrift.model.SGLog;
import com.sankuai.octo.test.thrift.model.SGModuleInvokeInfo;
import com.sankuai.octo.test.thrift.service.LogCollectorService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCollectorServiceImpl extends MTIface implements LogCollectorService.Iface {
    private final static Logger LOG = LoggerFactory.getLogger(LogCollectorServiceImpl.class);

    @Override
    public int uploadLog(SGLog oLog) throws TException {
//        LOG.info(oLog.toString());
//        sgLog.insertSgLog(oLog);
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return oLog.level;
    }

    @Override
    public int uploadModuleInvoke(SGModuleInvokeInfo info) throws TException {
//        LOG.info(info.toString());
//        if (StringUtils.isEmpty(info.getLocalAppKey())) {
//            info.setLocalAppKey("unknownService");
//        }
//        if (StringUtils.isEmpty(info.getLocalHost())) {
//            info.setLocalHost("unknownHost");
//        }
//        if (StringUtils.isEmpty(info.getRemoteAppKey())) {
//            info.setRemoteAppKey("unknownService");
//        }
//        if (StringUtils.isEmpty(info.getRemoteHost())) {
//            info.setRemoteHost("unknownHost");
//        }
//        if (StringUtils.isEmpty(info.getSpanName())) {
//            info.setSpanName("unknownMethod");
//        }
//        // first log for debug
//        try {
//            if (info.getDebug() == 1 && !"FacebookService.getStatus".equalsIgnoreCase(info.getSpanName())) {
//                trace.insert(info);
//            }
//        } catch (Exception e) {
//            LOG.warn("insert with exception...", e);
//        }
//        // then update field for perf, ignore getStatus
//        if (!"FacebookService.getStatus".equalsIgnoreCase(info.getSpanName())) {
//            info.setLocalHost(perf.filterIp(info.getLocalHost()));
//            info.setRemoteHost(perf.filterIp(info.getRemoteHost()));
//            perf.process(info);
//        }
        return info.getCount();
    }
}
