namespace java com.sankuai.octo.mnsc.idl.thrift.service

include "mnsc_data.thrift"
include  "./sandbox_common.thrift"

typedef mnsc_data.MNSResponse MNSResponse
typedef mnsc_data.AppKeyListResponse AppKeyListResponse
typedef mnsc_data.HttpPropertiesResponse HttpPropertiesResponse
typedef mnsc_data.UpstreamResponse UpstreamResponse
typedef sandbox_common.SandboxResponse SandboxResponse
typedef sandbox_common.SandboxConfig SandboxConfig

service MNSCacheService {

    MNSResponse getMNSCache(1:string appkey, 2:string version, 3:string env);

    MNSResponse getMNSCache4HLB(1:string appkey, 2:string version, 3:string env);

    AppKeyListResponse getAppKeyListByBusinessLine(1:i32 bizCode, 2:string env);

    HttpPropertiesResponse getHttpPropertiesByBusinessLine(1:i32 bizCode, 2:string env);

    HttpPropertiesResponse getHttpPropertiesByAppkey(1:string appkey, 2:string env);

    UpstreamResponse getHlbUpstream(1:string nginx_type, 2:string idc_type, 3:string env);

    SandboxResponse getSandbox(1:string appkey, 2:string env);

    bool saveSandbox(1:string id,2:string env,3:string data);

    bool deleteSandbox(1:string id,2:string env);

    list<SandboxConfig> getSandboxConfig(1:list<string> appkeys, 2:string env);

    MNSResponse getProvidersByIP(1:string ip);

    bool delProvider(1:string appkey,  2:i32 env, 3:i32 serverType, 4:string ip, 5:i32 port);
}
