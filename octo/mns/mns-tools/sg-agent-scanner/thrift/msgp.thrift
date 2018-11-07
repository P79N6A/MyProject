namespace java com.sankuai.octo.msgp.thrift.service

include "quota_data.thrift"


typedef quota_data.ProviderQuota ProviderQuota
typedef quota_data.DegradeAction DegradeAction

service MSGPService {
    i32 countProviderQuota();
    list<ProviderQuota> getProviderQuota(1:i32 limit, 2:i32 skip)

    void updateDegradeAction(1: i32 env, 2: string providerAppkey, 3: string method, 4: list<DegradeAction> actions);
    void removeDegradeNode(1: i32 env, 2: string providerAppkey, 3: string method);
}
