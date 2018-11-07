package com.sankuai.octo.statistic.service;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import com.sankuai.octo.statistic.domain.Instance3;
import com.sankuai.octo.statistic.domain.SimpleCountHistogram3;
import org.apache.thrift.TException;

/**
 * 接受数据半加工好的数据，二次合并计算
 *  https://wiki.sankuai.com/pages/viewpage.action?pageId=847503165
 */
@ThriftService
public interface LogExportService {

    /**
     *
     * @param name key的名字
     * @param histogram 参数
     * @throws TException
     */
    @ThriftMethod
    void sendDailyData(String appkey,String name,SimpleCountHistogram3 histogram) throws TException;

    @ThriftMethod
    void sendGroupRangeData(Instance3 instance) throws TException;
}
