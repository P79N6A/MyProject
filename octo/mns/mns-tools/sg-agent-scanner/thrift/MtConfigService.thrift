namespace java com.sankuai.octo.config.service

include "MtConfig.thrift"

service MtConfigService{
    /**
     * 获取合并后的数据
     * @param version 范围0~2^31,如果版本一样则直接返回304,如果客户端版本较小则返回最新内容,如果客户端版本较大则抛出异常
     * @return {@link MtConfig.ConfigDataResponse}
     **/
    MtConfig.ConfigDataResponse getMergeData(1 : MtConfig.GetMergeDataRequest request);
    /**
     * 设置配置
     * @param version 当前客户端的配置,防止脏数据,如果与服务器上的数据version不一致则抛出异常
     * @param data 要变更的数据,支持批量操作
     * @return 异常错误码,具体含义参考 MtConfigExceptionType
     **/
    i32 setData(1 : string appkey, 2 : string env, 3 : string path, 4 : i64 version, 5 : string jsonData);
    /**
     * 同步sg_agent使用的节点关系,以便定时去除已经实现的节点与sg_notify的对应关系
     * @param nodeListJson 使用的节点列表的json值,数据结构参考ConfigNode
     * @return 异常错误码,具体含义参考 MtConfigExceptionType
     **/
    i32 syncRelation(1 : list<MtConfig.ConfigNode> usedNodes, 2 : string requestIp);
    /**
     * 获取默认配置,包括'sg_agent关系上报时间间隔','client的轮询间隔'等
     * @return {@link MtConfig.DefaultConfigResponse}
     **/
    MtConfig.DefaultConfigResponse getDefaultConfig();

    /*
     * 写入配置文件内容
     * @return {@link MtConfig.file_param_t} 里面暂时只有err
     */
    MtConfig.file_param_t setFileConfig(1:MtConfig.file_param_t files);

    /*
     * 获取配置文件内容
     * @param files 里面的configFiles放的是需要内容的文件,暂时只请求一个
     */
    MtConfig.file_param_t getFileConfig(1:MtConfig.file_param_t files);

    /*
     * 获取配置文件列表
     * @param files 里面的configFiles不用放东西
     * @return {@link MtConfig.file_param_t} 里面的configFiles的filecontent不放内容
     */
    MtConfig.file_param_t getFileList(1:MtConfig.file_param_t files)

    /*
     * 下发配置文件
     * @param request 里面的files的configFiles不用放内容
     */
    MtConfig.ConfigFileResponse distributeConfigFile(1:MtConfig.ConfigFileRequest request);

    /*
     * 配置文件生效
     */
    MtConfig.ConfigFileResponse enableConfigFile(1:MtConfig.ConfigFileRequest request);
}

