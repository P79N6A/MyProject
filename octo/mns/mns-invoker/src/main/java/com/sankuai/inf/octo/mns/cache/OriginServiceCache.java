package com.sankuai.inf.octo.mns.cache;

import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.ProtocolResponse;
import com.sankuai.sgagent.thrift.model.SGAgent;
import org.apache.thrift.TException;

/**
 * not public class, visible within package.
 */
class OriginServiceCache extends ServiceCache {
    @Override
    protected ProtocolResponse doGetServiceList(SGAgent.Iface client, ProtocolRequest req) throws TException {
        return client.getOriginServiceList(req);
    }
}
