package com.sankuai.octo.oswatch;

import com.meituan.service.mobile.mtthrift.server.MTIface;
import com.sankuai.octo.oswatch.controller.QuotaController;
import com.sankuai.octo.oswatch.thrift.data.ProviderQuota;
import com.sankuai.octo.oswatch.thrift.service.OSWatchService;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by chenxi on 6/5/15.
 */

public class OSWatchServiceImpl extends MTIface implements OSWatchService.Iface {
    @Autowired
    QuotaController quotaController;

    public void notifyProviderQuota(List<ProviderQuota> quotaList)  throws TException {
        quotaController.notifyProviderQuota(quotaList);
    }

    public void setQuotaProvider(QuotaController quotaController){
        this.quotaController = quotaController;
    }
}
