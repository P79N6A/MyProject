namespace java com.sankuai.sgagent.thrift.model
namespace cpp com.sankuai.cmtrace

include  "./aggregator_common.thrift"
include  "./sgagent_common.thrift"
include  "./quota_common.thrift"
include  "./config_common.thrift"

typedef aggregator_common.SGModuleInvokeInfo SGModuleInvokeInfo
typedef aggregator_common.CommonLog CommonLog
typedef aggregator_common.SGLog SGLog
typedef sgagent_common.SGService SGService
typedef sgagent_common.CRouteData CRouteData
typedef quota_common.DegradeAction DegradeAction

typedef sgagent_common.HttpProperties HttpProperties

enum RegistCmd {
    REGIST = 0,
    UNREGIST = 1
}

struct properties_res_param_t
{
    1:i32 businessLineCode;
    2:map<string, HttpProperties> httpProperties;
    3:string version;
    4:i32 errCode;
}

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
    4:string protocol;
}

struct getservice_res_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
    4:list<SGService> serviceList;
    5:string protocol;
}

struct getservicename_req_param_t
{
    1:string localAppkey;
    2:string servicename;
    3:string version;
    4:string protocol;
}

struct getservicename_res_param_t
{
    1:string localAppkey;
    2:string servicename;
    3:string version;
    4:set<string> appkeys;
    5:string protocol;
}

struct getroute_req_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
    4:string protocol;
}

struct getroute_res_param_t
{
    1:string localAppkey;
    2:string remoteAppkey;
    3:string version;
    4:list<CRouteData> routeList;
    5:string protocol;
}

struct regist_req_param_t
{
    1:i32 retry_times = 0;
    2:SGService sgservice;
    3:i32 uptCmd;
    4:RegistCmd regCmd;
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
