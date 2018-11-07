package com.sankuai.octo.aggregator.swift;

import com.sankuai.octo.aggregator.thrift.LogCollectorServiceImpl;
import com.sankuai.octo.aggregator.thrift.model.CommonLog;
import com.sankuai.octo.aggregator.thrift.model.SGModuleInvokeInfo;
import org.apache.thrift.TException;

public class AggregatorServiceImpl implements AggregatorService {

    private LogCollectorServiceImpl impl = new LogCollectorServiceImpl();

    @Override
    public int uploadLog(SwiftSGLog log) throws TException {
        return 0;
    }

    @Override
    public int uploadModuleInvoke(SwiftSGModuleInvokeInfo log) throws TException {
        SGModuleInvokeInfo info = new SGModuleInvokeInfo();
        info.setTraceId(log.traceId);
        info.setSpanId(log.spanId);
        info.setSpanName(log.spanName);
        info.setLocalAppKey(log.localAppKey);
        info.setLocalHost(log.localHost);
        info.setLocalPort(log.localPort);
        info.setRemoteAppKey(log.remoteAppKey);
        info.setRemoteHost(log.remoteHost);
        info.setRemotePort(log.remotePort);
        info.setStart(log.start);
        info.setCost(log.cost);
        info.setType(log.type);
        info.setStatus(log.status);
        info.setCount(log.count);
        info.setDebug(log.debug);
        info.setExtend(log.extend);
        return impl.uploadModuleInvoke(info);
    }

    @Override
    public int uploadCommonLog(SwiftCommonLog log) throws TException {
        CommonLog commonLog = new CommonLog();
        commonLog.setCmd(log.cmd);
        commonLog.setContent(log.content);
        commonLog.setExtend(log.extend);
        return impl.uploadCommonLog(commonLog);
    }
}
