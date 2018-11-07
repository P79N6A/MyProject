namespace java com.sankuai.octo.config.service

include  "../../../../idl-common/src/main/thrift/config_common.thrift"
include  "config_data.thrift"

typedef config_common.ConfigDataResponse ConfigDataResponse
typedef config_common.GetMergeDataRequest GetMergeDataRequest
typedef config_common.ConfigNode ConfigNode
typedef config_common.DefaultConfigResponse DefaultConfigResponse
typedef config_common.file_param_t file_param_t
typedef config_common.ConfigFileRequest ConfigFileRequest
typedef config_common.ConfigFileResponse ConfigFileResponse
typedef config_common.ConfigGroupResponse ConfigGroupResponse
typedef config_common.ConfigGroupsResponse ConfigGroupsResponse
typedef config_data.PullRequest PullRequest
typedef config_data.PRDetail PRDetail
typedef config_data.Review Review
typedef config_data.UpdateGroupRequest UpdateGroupRequest
typedef config_data.FilelogRequest FilelogRequest
typedef config_data.DeleteFileRequest DeleteFileRequest
typedef config_data.FileConfigSyncRequest FileConfigSyncRequest
typedef config_data.FileConfigSyncResponse FileConfigSyncResponse
typedef config_data.SetConfigRequest SetConfigRequest
typedef config_data.SetConfigResponse SetConfigResponse
typedef config_data.AddGroupRequest AddGroupRequest
typedef config_data.DeleteGroupRequest DeleteGroupRequest
typedef config_data.MergeRequest MergeRequest

service MtConfigService{
    /**
     * 获取合并后的数据
     * @param version 范围0~2^31,如果版本一样则直接返回304,如果客户端版本较小则返回最新内容,如果客户端版本较大则抛出异常
     * @return {@link ConfigDataResponse}
     **/
    ConfigDataResponse getMergeData(1 : GetMergeDataRequest request);
    /**
     * 设置配置
     * @param version 当前客户端的配置,防止脏数据,如果与服务器上的数据version不一致则抛出异常
     * @param data 要变更的数据,支持批量操作
     * @return 异常错误码,具体含义参考 MtConfigExceptionType
     **/
    i32 setData(1 : string appkey, 2 : string env, 3 : string path, 4 : i64 version, 5 : string jsonData);

     /**
     * 设置配置
     * @param SetDataRequest
     * @return SetConfigResponse
     **/
     SetConfigResponse setConfig(1 : SetConfigRequest request);
    /**
     * 同步sg_agent使用的节点关系,以便定时去除已经实现的节点与sg_notify的对应关系
     * @param nodeListJson 使用的节点列表的json值,数据结构参考ConfigNode
     * @return 异常错误码,具体含义参考 MtConfigExceptionType
     **/
    i32 syncRelation(1 : list<ConfigNode> usedNodes, 2 : string requestIp);
    /**
     * 同步sg_agent使用的文件配置，添加ip至对应分组
     * @param {@link FileConfigSyncRequest}
     * @return {@link FileConfigSyncResponse}
     **/
    FileConfigSyncResponse syncFileConfig(1 : FileConfigSyncRequest request);
    /**
     * 获取默认配置,包括'sg_agent关系上报时间间隔','client的轮询间隔'等
     * @return {@link DefaultConfigResponse}
     **/
    DefaultConfigResponse getDefaultConfig();

    /*
     * 写入配置文件内容
     * @return {@link file_param_t} 里面暂时只有err
     */
    file_param_t setFileConfig(1:file_param_t files);

    /*
     * 删除配置文件
     */
     i32 deleteFileConfig(1:DeleteFileRequest request);

    /*
     * 获取配置文件内容
     * @param files 里面的configFiles放的是需要内容的文件,暂时只请求一个
     */
    file_param_t getFileConfig(1:file_param_t files);

    /*
     * 获取配置文件列表
     * @param files 里面的configFiles不用放东西
     * @return {@link file_param_t} 里面的configFiles的filecontent不放内容
     */
    file_param_t getFileList(1:file_param_t files);

    /*
     * 下发配置文件
     * @param request 里面的files的configFiles不用放内容
     */
    ConfigFileResponse distributeConfigFile(1:ConfigFileRequest request);

    /*
     * 配置文件生效
     */
    ConfigFileResponse enableConfigFile(1:ConfigFileRequest request);

    /*
     * 保存操作记录
     */
    bool saveFilelog(1:FilelogRequest filelogRequest);

    /*
     * 根据groupID获取分组信息
    */
    ConfigGroupResponse getGroupInfo(1:string appkey, 2:string env, 3:string groupID);

    /*
     * 获取某个appkey下的分组信息
    */
    ConfigGroupsResponse getGroups(1:string appkey, 2:string env);

    /*
     * 新增分组
     */
    ConfigGroupResponse addGroup(1:string appkey, 2:string env, 3:string groupName, 4:list<string> ips);

    ConfigGroupResponse addFileGroup(1:AddGroupRequest request);

    /*
     * 更新分组
     */
    ConfigGroupResponse updateGroup(1:string appkey, 2:string env, 3:string groupId, 4:list<string> ips);

     /*
      * 更新分组
      */
    ConfigGroupResponse updateFileGroup(1:UpdateGroupRequest request);

    /*
     * 删除分组
     */
    i32 deleteGroup(1:string appkey, 2:string env, 3:string groupId);

    i32 deleteFileGroup(1:DeleteGroupRequest request);

    /*
     * 根据IP获取分组ID
     */
    string getGroupID(1:string appkey, 2:string env, 3:string ip);

    /*
     *  创建PR
     */
    bool createPR(1:PullRequest pr, 2:list<PRDetail> detailList);

    /*
     * 删除PR
     */
    bool detelePR(1:i64 prID);

    /*
     * 更新PR
     */
    bool updatePR(1:PullRequest pr);

    /*
     * 获取PR列表
     */
    list<PullRequest> getPullRequest(1:string appkey, 2:i32 env, 3:i32 status);

    /*
     * 获取指定PR
     */
    PullRequest getPR(1:i64 prID);

     /*
      * merge PR
      */
    bool mergePR(1:i64 prID);

     /*
      * merge PullRequest
      */
    bool mergePullRequest(1:MergeRequest request);

    /*
     * 更新PR详情
     */
    bool updatePRDetail(1:i64 prID ,2:list<PRDetail> detailList);

    /*
     * 获取PR详情
     */
    list<PRDetail> getPRDetail(1:i64 prID);

    /*
     * 获取Review详情
     */
    list<Review> getReview(1:i64 prID);

     /*
      * 创建reveiw
      */
    bool createReview(1:Review review);
}

