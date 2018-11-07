namespace java com.sankuai.inf.hmc.idl.thrift.model

struct AppKeyListResponse {
    //状态码，200: 正常返回；400: 访问MNS请求超时；
    1 : required i32 code = 200; 

    //服务appkey列表
    2 : optional list<string> appKeyList;
}

typedef map<string,string> HttpProperties
struct HttpPropertiesResponse {
    //状态码，200: 正常返回；400: 访问MNS请求超时；
    1 : required i32 code = 200;

    //Http属性，key为appKey、value为HttpProperties
    2 : optional map<string, HttpProperties> propertiesMap; 
}

//upstream server 节点定义
struct HlbcService {
    1 : string          ip;
    2 : i32             port;
    3 : i32             weight;
    4 : i32             status;
    5 : optional i32    failTimeout;
    6 : optional i32    maxFails;
    7 : optional i32    slowStart;
}
//upstream 健康检查结构定义
struct HlbcHealthCheck {
    1 : string          healthCheckType;
    2 : optional string checkHttpSend;
    3 : optional string legacyCheckCmd;
}
//upstream 数据结构定义
struct HttpUpstream {
    1 : string                        upstreamName;
    2 : i32                           business;
    3 : i32                           isGrey;
    4 : optional map<string, string>  scheduleStrategy;
    5 : list<HlbcService>             server;
    6 : optional HlbcHealthCheck      checkStrategy;
}
struct UpstreamDataResponse {
    1 : required i32 code = 200;
    2 : map<string, HttpUpstream> upstreams;
}

struct UpstreamResponse {
    //状态码，200: 正常返回；500: MNSC正在进行cache更新
    1 : required i32 code = 200;

    //key为upstream_name，value为upstream json串内容
    2 : map<string, string> upstreams;

    //扩展字段
    3 : string ext;
}

//location块的数据结构定义
struct HlbcLocation {
    1 : string          locationPath;
    2 : optional string locationBlockCmd;
    3 : optional string proxyPass;
}
//server块数据结构定义
struct HlbcServer {
    1 : string             serverName;
    2 : i32                business;
    3 : optional string    serverBlockCmd;
    4 : list<HlbcLocation> locationList;
    5 : optional i32       grayRelease;
    6 : optional list< string >  deployHosts;
    7 : optional string    serverBlock;
}
struct ServerDataResponse {
    1 : required i32 code = 200;
    2 : map<string, HlbcServer> serverBlocks;
}
struct ServerBlockResponse {
    //状态码，200: 正常返回；500: MNSC正在进行cache更新
    1 : required i32 code = 200;

    //key为upstream_name，value为server_block json串内容
    2 : map<string, string> serverBlocks;

    //扩展字段
    3 : string ext;
}

//环境的名字
const string PROD = "prod";
const string STAGE = "stage";
const string TEST = "test";

/**
* 异常错误定义
**/

//成功
const i32 SUCCESS = 200;

//HMC正在更新时，返回500
const i32 HMC_UPDATING = 500;

// 超时异常
const i32 TIMEOUT_ERROR = 504;

// 入口参数不合法
const i32 BAD_REQUEST = 400;
