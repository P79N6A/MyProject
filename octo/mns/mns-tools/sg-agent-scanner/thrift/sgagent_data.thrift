include "MtConfig.thrift"

/**
 * Common status reporting mechanism across all services
 */
enum fb_status {
    DEAD = 0,
    STARTING = 1,
    ALIVE = 2,
    STOPPING = 3,
    STOPPED = 4,
    WARNING = 5,
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
    6:i32 status;  //正常状态，重启状态，下线状态，故障状态
    7:i32 role;    //backup角色，当其他服务无法服务的时候，启用backup状态
    8:i32 envir;        //运行环境，prod（线上环境），stag（线上测试环境），test（测试环境）
    9:i32 lastUpdateTime;  //最后修改时间
    10:string extend;   //扩展
    11:double fweight;   //浮点型权重
    12:i32 serverType;  //用于区分http和thrift
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
 * 日志格式定义，定位是agent和MTransport的日志（异常，错误，重连等）
 */
struct SGLog 
{
    1:string appkey;
    2:i64 time;
    3:i32 level;
    4:string content;
}

struct LogList 
{
    1: required list<SGLog> logs;
}

/*
 * 通用的日志格式
 */
const i32 MTRACE_LOG = 1;
const i32 ERROR_LOG = 2;
struct CommonLog 
{
    1:required i32 cmd;          // 命令字
    2:required binary content;   // 所有的内容
    3:optional string extend;    // 扩展字段
}

struct SGModuleInvokeInfo 
{
    1:string traceId;
    2:string spanId;
    3:string spanName;
    4:string localAppKey;
    5:string localHost;
    6:i32 localPort;
    7:string remoteAppKey;
    8:string remoteHost;
    9:i32 remotePort;
    10:i64 start;  //开始时间戳
    11:i32 cost;    //耗时ms
    12:i32 type;    //调用类型（0为client，1为server）
    13:i32 status;
    14:i32 count;
    15:i32 debug;
    16:string extend;
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
    8: optional string key; // 某次请求的唯一标示,用于匹配Set请求应答对
    9: optional list<MtConfig.ConfigNode> configNodeList; // 当前sg_agent机器内存中配置节点信息，用与同步ZK
}

struct ConfigUpdateRequest {
  1: required list<MtConfig.ConfigNode> nodes;
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
}

/*
* 鉴权消费者结构体
*/
struct AuthorizedConsumer
{
    1:string ip;
    2:string appkey;
}

/*
* 鉴权提供者结构体
*/
struct AuthorizedProvider
{
    1:string ip;
    2:string appkey;
}
