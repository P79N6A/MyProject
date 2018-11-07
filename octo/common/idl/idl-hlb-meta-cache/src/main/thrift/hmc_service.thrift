namespace java com.sankuai.inf.hmc.idl.thrift.service

include "hmc_data.thrift"

typedef hmc_data.AppKeyListResponse AppKeyListResponse
typedef hmc_data.HttpPropertiesResponse HttpPropertiesResponse
typedef hmc_data.UpstreamResponse UpstreamResponse
typedef hmc_data.ServerBlockResponse ServerBlockResponse
typedef hmc_data.UpstreamDataResponse UpstreamDataResponse
typedef hmc_data.ServerDataResponse ServerDataResponse
typedef hmc_data.HttpUpstream HttpUpstream
typedef hmc_data.HlbcServer HlbcServer

service HlbMetaCacheService {

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
     * @param duyp_name dyup名字
     * @param env 环境
     * @return upstream配置, key为dyup_name，value为dyup的json串
     * @name 根据dyup_name env获取upstream配置
     */
    UpstreamResponse getHLBCDyup( 1:string duyp_name, 2:string env);
    UpstreamDataResponse getHLBCDyupData(1:string dyup_name, 2:string env);

    /**
     * @param env 环境
     * @return upstream配置, key为dyup_name，value为dyup的json串
     * @name 根据env获取upstream配置
     */
    UpstreamResponse getHLBCDyupByEnv( 1:string env);
    UpstreamDataResponse getHLBCDyupDataByEnv(1:string env);

    /**
     * @param server_name 域名
     * @param env 环境
     * @return server block配置, key为server_name，value为server block的json串
     * @name 根据域名 and env获取server block配置
     */
    ServerBlockResponse getHLBCServerBlockByDomainName( 1:string serverName, 2:string env);
    ServerDataResponse getHLBCServerDataByDomainName(1:string serverName, 2:string env);

    /**
     * @param bizCode 业务线标识
     * @param env 环境
     * @return server block配置, key为server_name，value为server block的json串
     * @name 根据业务群编号及env 获取server block配置
     */
    ServerBlockResponse getHLBCServerBlockByBusinessLine( 1:i32 bizCode, 2:string env);
    ServerDataResponse getHLBCServerDataByBusinessLine(1:i32 bizCode, 2:string env);

    /**
     * @param ip 机器ip
     * @return upstream name list
     * @name 查询ip归属于哪些upstream
     */
    list<string> getHLBCDyupListByIp( 1:string ip);
    list<HttpUpstream> getHLBCDyupDataListByIp(1:string ip);

    /**
     * @param dyupName  upstream名字
     * @param env 环境
     * @return server name list
     * @name 查询upstream归属于哪些域名
     */
    list<string> getHLBCServerBlockListByDyupName( 1:string dyupName, 2:string env);
    list<HlbcServer> getHLBCServerDataListByDyupName(1:string dyupName, 2:string env);
}
