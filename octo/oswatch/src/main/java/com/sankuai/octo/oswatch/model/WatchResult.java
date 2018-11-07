package com.sankuai.octo.oswatch.model;

import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.sankuai.octo.oswatch.thrift.data.ProviderQuota;

import java.util.List;

/**
 * Created by chenxi on 7/1/15.
 */

public class WatchResult {
    ProviderQuota providerQuota;
    List<DegradeAction> actions;
    WatchResultType resultType;
    int nodeAlive;

    public WatchResult(ProviderQuota providerQuota, List<DegradeAction> actions, WatchResultType resultType, int nodeAlive) {
        this.providerQuota = providerQuota;
        this.actions = actions;
        this.resultType = resultType;
        this.nodeAlive = nodeAlive;
    }

    public ProviderQuota getProviderQuota() {
        return providerQuota;
    }

    public List<DegradeAction> getActions() {
        return actions;
    }

    public WatchResultType getResultType() {
        return resultType;
    }

    public int getNodeAlive() {
        return nodeAlive;
    }
}
