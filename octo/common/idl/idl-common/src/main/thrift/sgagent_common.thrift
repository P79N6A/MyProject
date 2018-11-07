namespace java com.sankuai.sgagent.thrift.model
include "config_common.thrift"

typedef config_common.ConfigNode ConfigNode

/**
 * HttpProperties
 */
typedef map<string,string> HttpProperties

/**
 * Common status reporting mechanism across all services
 */
enum fb_status {
    DEAD = 0,//宕机，故障
    STARTING = 1,//启动中
    ALIVE = 2,//正常
    STOPPING = 3,//正在下线
    STOPPED = 4,//禁用
    WARNING = 5,//警告
}

enum HeartbeatSupportType {
    NoSupport = 0,
    P2POnly = 1,
    ScannerOnly = 2,
    BothSupport = 3,
}

struct ServiceDetail {
    1:bool unifiedProto;  //默认为false，true代表支持统一协议
}

/*
 * 美团服务定义
 */
struct SGService
{
    1:string appkey;
    2:string version;
    3:string ip;
    4:i32 port;
    5:i32 weight;
    6:i32 status;  //正常状态，重启状态，下线状态，故障状态,具体值参考fb_status
    7:i32 role;    //backup角色，当其他服务无法服务的时候，启用backup状态;0(主）1（备）
    8:i32 envir;        //运行环境，prod（线上环境3），stag（线上测试环境2），test（测试环境1）
    9:i32 lastUpdateTime;  //最后修改时间
    10:string extend;   //扩展
    11:double fweight;   //浮点型权重
    12:i32 serverType;  //用于区分http(1)和thrift(0)
    13:string protocol;    //支持tair, sql等其他协议的服务
    14:map<string, ServiceDetail> serviceInfo; //serviceName 到 servcieDetail的映射
    15:byte heartbeatSupport; //0:不支持心跳， 1:仅支持端对端心跳   2:仅支持scanner心跳  3:两种心跳都支持
    16:optional string swimlane;
    17:optional string hostname;
    18:optional string cell; //外卖服务set化标志位
    19:optional string groupInfo;   //获取http分组信息
}

/*
 * 服务节点, 主要储存服务->appkey映射
 */
struct ServiceNode
{
    1:string serviceName;
    2:set<string> appkeys; //serviceName对应的appkey
    3:i32 lastUpdateTime;  //最后修改时间
}

/*
 * 服务分组,consumer定义
 */
struct Consumer
{
    1:list<string> ips;
    2:list<string> appkeys;
}

/*
 * 服务分组定义
 */
struct CRouteData
{
    1:string id;
    2:string name;
    3:string appkey;
    4:i32 env;
    5:i32 category;
    6:i32 priority;
    7:i32 status;  //服务分组，0：禁用，1：启用
    8:Consumer consumer;
    9:list<string> provider;
    10:i32 updateTime;  //最后修改时间
    11:i32 createTime;  //最后修改时间
    12:string reserved;   //扩展 k1:v1|k2:v2...
}

/*
 * 路由分组中provider节点信息 
 */
struct CProviderNode
{
    1:string appkey;
    2:i64 lastModifiedTime;
    3:i64 mtime;
    4:i64 cversion;
    5:i64 version;
} 

/*
 * 路由分组中route根节点结构 
 */
struct CRouteNode 
{
    1:string appkey;
    2:i64 lastModifiedTime;
    3:i64 mtime;
    4:i64 cversion;
    5:i64 version;
}

/*
 * sg_notify下发配置传给sg_agent参数定义 
 */
struct ParamMCC 
{
    1:string appkey;
    2:string zkNode;    //需要更新的配置文件zk节点路径
    3:string md5;       //zk中配置信息的md5值，用来校验
    4:string fileName;  //配置文件名
    5:string path;      //配置文件存放路径
    6:i64 lastUpdateTime;   //节点最新更新时间
    7:i32 needToLocal;   //配置文件是否需要落地到本机，0:不需要，1:需要
    8:i64 createTime;   //节点创建时间
    9:string privilege; //生成配置文件的权限
    10:string reserved; //扩展字段
    11:string fileType; //文件类型
} 

/**
 * 处理conf相关参数定义
 */
struct proc_conf_param_t
{
    1: required string appkey;
    2: required string env;
    3: required string path;
    4: optional i64 version;
    5: optional string conf;
    6: optional i32 cmd; // MtConfigCmdType消息类型
    7: optional i32 err;
    8: optional string key; // 某次请求的唯一标示,用于匹配请求应答对
    9: optional list<ConfigNode> configNodeList; // 当前sg_agent机器内存中配置节点信息，用与同步ZK
    10: optional string swimlane; //泳道标识
    11: optional string token; //修改鉴权token
    12: optional string cell;  //外卖set标识
}

struct ConfigUpdateRequest {
  1: required list<ConfigNode> nodes;
}

struct ZabbixSelfCheck
{
    1:map<i32, i64> msgQueueBytes; //有多个队列和共享内存值
    2:i32 agent_vmRss;
    3:i32 worker_vmRss;
    4:double agent_cpu;
    5:double worker_cpu;
    6:i32 zkConnections;
    7:i32 mtConfigConnections;
    8:i32 logCollectorConnections;
    9:i32 bufferKeyNum;
    10:i32 missBuffNum;   
    11:string extend;
    12:map<i32, i32> bufferSize; // 缓存中数据条数
    13:map<i32, double> reqStat; // 获取服务列表请求数据统计
    14:map<i32, double> registeStat; // 注册请求数据统计
    15:map<i32, double> unregisteStat; // 注册请求数据统计
}

struct ProtocolRequest {
    1:string localAppkey;
    2:string remoteAppkey;
    3:string protocol;
    4:string serviceName; //对应上海侧接口概念
    5:optional string swimlane;
    6:optional bool enableSwimlane2; // 开启swimlane2.0与否
    7:optional bool enableCell; // 是否过滤cell的服务节点，true: 不过滤，false：过滤。默认过滤
}

struct ProtocolResponse {
    1:i32 errcode;
    2:list<SGService> servicelist;
}

struct SwitchRequest {
    1:i32 key;
    2:bool value;
    3:string verifyCode;
    4:string switchName;
}

struct SwitchResponse {
    1:i32 errcode;
    2:string msg;
}

enum UptCmd {
    RESET = 0,
    ADD = 1,
    DELETE = 2,
}

enum CustomizedStatus{
     DEAD = 0, //default value
     ALIVE = 2,
     STOPPED = 4,
}

struct ConfigStatus {
    1:CustomizedStatus initStatus, //初始化注册到OCTO的状态为未启动，默认未启动
    2:CustomizedStatus runtimeStatus //运行时的服务状态为正常，默认正常
}

