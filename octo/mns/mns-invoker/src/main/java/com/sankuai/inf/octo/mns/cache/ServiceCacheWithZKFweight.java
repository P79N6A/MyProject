package com.sankuai.inf.octo.mns.cache;

import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.ProtocolResponse;
import com.sankuai.sgagent.thrift.model.SGAgent;
import org.apache.thrift.TException;

/**
 * Created by lhmily on 05/04/2017.
 */
 class ServiceCacheWithZKFweight extends ServiceCache {
    @Override
    protected ProtocolResponse doGetServiceList(SGAgent.Iface client, ProtocolRequest req) throws TException {
        return client.getServiceListWithZKFweight(req);
    }
}
