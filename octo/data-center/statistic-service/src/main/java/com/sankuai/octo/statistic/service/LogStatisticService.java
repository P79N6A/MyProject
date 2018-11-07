package com.sankuai.octo.statistic.service;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import com.sankuai.octo.statistic.model.Metric;
import org.apache.thrift.TException;

import java.util.List;

/**
 * 该接口中每个rpc方法第一个参数是providerAppKey,用以一致性hash根据它计算节点位置
 *
 * @octo.appkey com.sankuai.inf.data.statistic
 * @permission 公开
 * @status 在线
 * @link http://wiki.sankuai.com/x/a5VWDg
 * @see com.sankuai.octo.statistic.ConsistentHashLoadBalancer
 */
@ThriftService
public interface LogStatisticService {

    /**
     * @param metrics 上报的metric集合
     * @name 上报metrics
     */
    @ThriftMethod
    void sendMetrics(List<Metric> metrics) throws TException;
}
