namespace java com.sankuai.sgagent.thrift.model
include  "./aggregator_common.thrift"
include  "./sgagent_common.thrift"
include  "./quota_common.thrift"
include  "./config_common.thrift"

typedef aggregator_common.SGModuleInvokeInfo SGModuleInvokeInfo
typedef aggregator_common.CommonLog CommonLog
typedef aggregator_common.SGLog SGLog
typedef sgagent_common.SGService SGService
typedef sgagent_common.ConfigUpdateRequest ConfigUpdateRequest
typedef sgagent_common.proc_conf_param_t proc_conf_param_t
typedef sgagent_common.ZabbixSelfCheck ZabbixSelfCheck
/**
 * Common status reporting mechanism across all services
 */
typedef sgagent_common.fb_status sg_fb_status
typedef sgagent_common.HttpProperties HttpProperties
typedef quota_common.DegradeAction DegradeAction
typedef config_common.file_param_t file_param_t

service SGAgent
{
    /*
     *根据自己的appkey和要调用服务的appkey获取对方的服务列表
     */
    list<SGService> getServiceList(1:string localAppkey, 2:string remoteAppkey);

    /*
     *获取HTTP服务列表
     */
    list<SGService> getHttpServiceList(1:string localAppkey, 2:string remoteAppkey);

    list<string> getAppKeyListByBusinessLine(1: i32 businessLineCode);

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
    i32 updateConfig(1:ConfigUpdateRequest request);

    /**
     * 获取Config内容
     */
    string getConfig(1:proc_conf_param_t node);

    /**
     * 设置Config内容
     */
    i32 setConfig(1:proc_conf_param_t conf);

    /**
     * 获取文件配置
     */
    file_param_t getFileConfig(1:file_param_t file);

    /*
     * notify下发文件接口
     */
    i32 notifyFileConfigIssued(1:file_param_t files);

    /*
     * notify命令生效接口
     */
    i32 notifyFileConfigWork(1:file_param_t files);

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
     * 获得降级行为
     */
    list<DegradeAction> getDegradeActions(1:required string localAppkey, 2:required string remoteAppkey);

    /*
     *自检上报给zabbix
     */
    ZabbixSelfCheck getZabbixInfo();

    /*
     * 获取消费者白名单列表
     */
    string getAuthorizedConsumers(1:string targetAppkey);

    /*
     * 获取provider白名单列表
     */
    string getAuthorizedProviders(1:string targetAppkey);

    /*
     * 根据业务线获取http健康度状况
     */
    map<string, HttpProperties> getHttpPropertiesByBusinessLine(1: i32 bizCode);

    /*
     * 根据appkey获取http健康度状况
     */
    map<string, HttpProperties> getHttpPropertiesByAppkey(1:string appkey);

    /*
     * convert the env of this sgagent to the specified "env"
     */
    bool switchEnv(1: string env, 2: string verifyCode);

    /**
     * Returns a descriptive name of the service
     */
    string getName(),

   /**
    * Returns the version of the service
    */
   string getVersion(),

   /**
    * Returns the env of sg_agent
    */
    i32 getEnv(),

   /**
    * Gets the status of this service
    */
   sg_fb_status getStatus(),

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

