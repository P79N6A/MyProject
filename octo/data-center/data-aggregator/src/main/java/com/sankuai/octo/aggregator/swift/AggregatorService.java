package com.sankuai.octo.aggregator.swift;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import org.apache.thrift.TException;

/**
 * @octo.appkey com.sankuai.inf.logCollector
 * @permission 公开
 * @status 在线
 * @link http://wiki.sankuai.com/x/a5VWDg
 */
@ThriftService
public interface AggregatorService {

    /**
     * @param log 异常日志
     * @return
     * @throws TException
     * @name 上传异常日志
     * @status 待下线
     */
    @ThriftMethod
    int uploadLog(SwiftSGLog log) throws TException;

    /**
     * @param log 调用日志
     * @return
     * @throws TException
     * @name 上传调用日志
     * @status 待下线
     */
    @ThriftMethod
    int uploadModuleInvoke(SwiftSGModuleInvokeInfo log) throws TException;

    /**
     * @param log 通用日志
     * @return
     * @throws TException
     * @name 上传通用日志
     */
    @ThriftMethod
    int uploadCommonLog(SwiftCommonLog log) throws TException;
}
