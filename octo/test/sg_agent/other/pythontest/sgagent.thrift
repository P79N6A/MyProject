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

/*
 * 通用的日志格式
 */
const i32 MTRACE_LOG = 1;
const i32 ERROR_LOG = 2;
struct CommonLog
{
    1:required i32 cmd;            // 命令字
    2:required binary content;     // 所有的内容
    3:optional string extend;      // 扩展字段
}

/*
 * 模块调用信息定义，字段换和mtrace的字段保持一致：http://wiki.sankuai.com/pages/viewpage.action?pageId=73895420
 */
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

struct ZabbixSelfCheck
{
    1:map<i32, i64> msgQueueSize; //有多个队列和共享内存值
    2:map<i32, i32> shmKeyNums;
    3:i32 bufferKeyNum;
    4:i32 missBuffNum;   
    5:i32 vmRss;
    6:i32 cpu;
    7:i32 zkConnections;
    8:i32 mtConfigConnections;
    9:i32 logCollectorConnections;
    10:string extend
}

service SGAgent 
{
    /*
     *根据自己的appkey和要调用服务的appkey获取对方的服务列表
     */
    list<SGService> getServiceList(1:string localAppkey, 2:string remoteAppkey);

    /*
     *根据自己的appkey和要调用服务的appkey根据负载均衡的策略获取服务，
     *此接口一期不实现
     */
    SGService getService(1:string localAppkey, 2:string remoteAppkey, 3:i32 strategy);

    /*
     *服务注册
     */
    i32 registService(1:SGService oService);

    /*
     * sg_agent回调配置中心接口,从ZK更新配置文件到本地
     */
    string callBackMCC(1:i32 cmdType, 2:string sData);

    /*
     *根据自己的appkey和IP获取配置信息
     *
     */
    string getLocalConfig(1:string localAppkey, 2:string ip);

    /*
     *上报日志
     */
    i32 uploadLog(1:SGLog oLog);

    /*
     *上报通用日志
     */
    i32 uploadCommonLog(1:CommonLog oCommonLog);

    /*
     *上报调用信息
     */
    i32 uploadModuleInvoke(1:SGModuleInvokeInfo oInfo);

    /*
     *自检上报给zabbix
     */
    ZabbixSelfCheck getZabbixInfo();

    /**
     * Returns a descriptive name of the service
     */
    string getName(),

   /**
    * Returns the version of the service
    */
   string getVersion(),

   /**
    * Gets the status of this service
    */
   fb_status getStatus(),

   /**
    * User friendly description of status, such as why the service is in
    * the dead or warning state, or what is being started or stopped.
    */
   string getStatusDetails(),

   /**
    * Gets the counters for this service
    */
   map<string, i64> getCounters(),

   /**
    * Gets the value of a single counter
    */
   i64 getCounter(1: string key),

   /**
    * Sets an option
    */
   void setOption(1: string key, 2: string value),

   /**
    * Gets an option
    */
   string getOption(1: string key),

   /**
    * Gets all options
    */
   map<string, string> getOptions(),

   /**
    * Returns a CPU profile over the given time interval (client and server
    * must agree on the profile format).
    */
   string getCpuProfile(1: i32 profileDurationInSec),

   /**
    * Returns the unix time that the server has been running since
    */
   i64 aliveSince(),

   /**
    * Tell the server to reload its configuration, reopen log files, etc
    */
   oneway void reinitialize(),

   /**
    * Suggest a shutdown to the server
    */
   oneway void shutdown(),

}

