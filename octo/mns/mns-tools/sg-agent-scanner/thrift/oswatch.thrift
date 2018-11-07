namespace java com.sankuai.octo.oswatch.thrift.service

include "quota_data.thrift"

typedef quota_data.ProviderQuota ProviderQuota

service OSWatchService {
    void notifyProviderQuota(1: list<ProviderQuota> quotaList)
}
