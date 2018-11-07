namespace java com.sankuai.octo.mnsc.idl.thrift.service

include "mnsc_data.thrift"
include  "../../../../idl-common/src/main/thrift/sandbox_common.thrift"

typedef mnsc_data.MNSResponse MNSResponse
typedef mnsc_data.MNSBatchResponse MNSBatchResponse
typedef mnsc_data.AppKeyListResponse AppKeyListResponse
typedef mnsc_data.HttpPropertiesResponse HttpPropertiesResponse
typedef sandbox_common.SandboxResponse SandboxResponse
typedef sandbox_common.SandboxConfig SandboxConfig
typedef mnsc_data.HttpGroupResponse HttpGroupResponse
typedef mnsc_data.AllHttpGroupsResponse AllHttpGroupsResponse
typedef mnsc_data.MnsRequest MnsRequest
typedef mnsc_data.RegisterResponse RegisterResponse
typedef mnsc_data.SGService SGService
typedef mnsc_data.AppkeyDescResponse AppkeyDescResponse

/**
 * @octo.appkey com.sankuai.inf.mnsc
 * @version 1.1.0-SNAPSHOT
 * @permission 公开
 * @staus 可用
 */
service MNSCacheService {

    /**
     * @param appkey 服务标识
     * @param version 版本
     * @param env 环境
     * @return 服务的节点列表
     * @name 获取服务的节点列表
     */
    MNSResponse getMNSCache(1:string appkey, 2:string version, 3:string env);

    /**
     * @param appkey 服务标识
     * @param version 版本
     * @param env 环境
     * @return 服务的HLB节点列表
     * @name 获取服务的HLB节点列表
     */
    MNSResponse getMNSCache4HLB(1:string appkey, 2:string version, 3:string env);

    /**
     * @param req 请求结构体
     * @name 获取服务的节点列表; 获取时, 会将缓存中的version与zk上的version进行比较,如果不相等,则重新更新缓存后,再返回数据。
     */
     MNSResponse getMNSCacheWithVersionCheck(1:MnsRequest req);

    /**
     * @param appkeys 请求的appkey列表
     * @param protocol
     */
    MNSBatchResponse getMNSCacheByAppkeys(1:list<string> appkeys, 2:string protocol);

    /**
     * @param bizCode 业务线标识
     * @param env 环境
     * @return 服务列表
     * @name 根据业务线获取服务列表
     */
    AppKeyListResponse getAppKeyListByBusinessLine(1:i32 bizCode, 2:string env);

    /**
     * @param bizCode 业务线标识
     * @param env 环境
     * @return 服务列表
     * @name 根据业务线获取支持set化的服务列表
     */
    AppKeyListResponse getCellAppKeysByBusinessLine(1:i32 bizCode, 2:string env);

    /**
     * @param bizCode 业务线标识
     * @param env 环境
     * @return map<appkey, map<propertyName, propertyValue>>, 若某appKey下未设置http服务配置则value为空map
     * @name 根据业务线获取http服务配置信息
     * @desc 根据业务线bizCode查询http服务配置信息，包括健康检查方案、负载均衡方案等
     */
    HttpPropertiesResponse getHttpPropertiesByBusinessLine(1:i32 bizCode, 2:string env);

    /**
     * @param appkey 服务标识
     * @param env 环境
     * @return map<appkey, map<propertyName, propertyValue>>, 若某appKey下未设置http服务配置则value为空map
     * @name 根据appkey获取http服务配置信息
     * @desc 根据appkey查询该服务http服务配置信息，包括健康检查方案、负载均衡方案等
     */
    HttpPropertiesResponse getHttpPropertiesByAppkey(1:string appkey, 2:string env);

    /**
     *param env 环境
     *param appkey 服务标识
     */
    HttpGroupResponse getGroupsByAppkey(1:string appkey, 2:string env);
    AllHttpGroupsResponse getAllGroups(1:string env);


    /**
     * @param appkey 服务标识
     * @param env 环境
     * @return 服务沙箱配置
     * @name 根据appkey、env获取服务沙箱配置
     */
    SandboxResponse getSandbox(1:string appkey, 2:string env);

    /**
     * @param id 服务沙箱的id
     * @param env 环境
     * @param data 服务沙箱的json数据
     * @return 是否成功
     * @name 根据id、env,设置 指定 环境的服务沙箱配置
     */
    bool saveSandbox(1:string id,2:string env,3:string data);

    /**
    * @param id 服务沙箱的id
    * @param env 环境
    * @return 是否成功
    * @name 根据id、env,删除 指定 环境的服务沙箱配置
    */
    bool deleteSandbox(1:string id,2:string env);

    /**
    * @param id 服务沙箱的id
    * @param env 环境
    * @return 是否成功
    * @name 根据id、env,删除 指定 环境的服务沙箱配置
    */
    list<SandboxConfig> getSandboxConfig(1:list<string> appkeys, 2:string env);

    /**
    * 根据IP返回与该IP相关的服务节点
    * @param ip IP 地址
    */
    MNSResponse getProvidersByIP(1:string ip);

    /**
    *
    * @param appkey 服务标识
    * @param env OCTO环境
    * @param serverType thrift or http
    * @param ip IP地址
    * @param port  端口
    */
    bool delProvider(1:string appkey,  2:i32 env, 3:i32 serverType, 4:string ip, 5:i32 port);


    /**
    *
    * @param ip
    * @return appkey列表
    */
    AppKeyListResponse getAppkeyListByIP(1:string ip);
 
    /**
    *
    * @param appkey
    * @return AppkeyDescResponse结构体
    */
    AppkeyDescResponse getDescByAppkey(1:string appkey);

    /**
    *
    * 查询注册时的双框架信息
    * @param {@link SGService}
    * @return {@link RegisterResponse}
    */
    RegisterResponse registerService(1:SGService registerInfo);
}
