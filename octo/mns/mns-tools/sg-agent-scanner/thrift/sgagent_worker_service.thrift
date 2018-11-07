namespace java com.sankuai.sgagent.thrift.model
include  "../../../../idl-common/src/main/thrift/aggregator_common.thrift"
include  "../../../../idl-common/src/main/thrift/sgagent_common.thrift"
include  "../../../../idl-common/src/main/thrift/quota_common.thrift"
include  "../../../../idl-common/src/main/thrift/config_common.thrift"

typedef aggregator_common.SGModuleInvokeInfo SGModuleInvokeInfo
typedef aggregator_common.CommonLog CommonLog
typedef aggregator_common.SGLog SGLog
typedef sgagent_common.SGService SGService
typedef sgagent_common.CRouteData CRouteData
typedef quota_common.DegradeAction DegradeAction

struct appkey_req_param_t
{
    1:i32 businessLineCode;
    2:string version;
}

struct appkey_res_param_t
{
    1:i32 businessLineCode;
    2:i32 errCode;
    3:string version;
    4:list<string> appKeyList;
}

struct quota_req_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
}

struct quota_res_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
    4:list<DegradeAction> actions;
}

struct locconf_req_param_t
{
    1:string localAppkey;
    2:string ip;
}

struct locconf_res_param_t
{
    1:string localAppkey;
    2:string ip;
    3:string conf;
}

struct getservice_req_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
}

struct getservice_res_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
    4:list<SGService> serviceList;
}

struct getroute_req_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
}

struct getroute_res_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
    4:list<CRouteData> routeList;
}

struct regist_req_param_t
{
    1:i32 retry_times = 0;
    2:SGService sgservice;
}

struct log_req_param_t
{
    1:i32 retry_times = 0;
    2:SGLog log;
}

struct common_log_req_param_t
{
    1:i32 retry_times = 0;
    2:CommonLog commonlog;
}

struct invoke_req_param_t
{
    1:i32 retry_times = 0;
    2:SGModuleInvokeInfo info;
}

struct getauth_req_param_t
{
    1:string targetAppkey;
    2:string version;
    3:i32 role;
}

struct getauth_res_param_t
{
    1:string targetAppkey;
    2:string version;
    3:i32 role;
    4:string content;
}

/*
struct proc_conf_param_t
{
    1: required string appkey;
    2: required string env;
    3: required string path;
    4: optional i64 version;
    5: optional string conf;
    6: required i32 cmd; // MtConfigCmdType消息类型
    7: optional i32 err;
}
*/
