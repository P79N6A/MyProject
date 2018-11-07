namespace java com.meituan.mafka.thrift.castle

include 'castle_data.thrift'

typedef castle_data.HeartBeatRequest HeartBeatRequest
typedef castle_data.HeartBeatResponse HeartBeatResponse
typedef castle_data.ClientInfoRequest ClientInfoRequest
typedef castle_data.ClientInfoResponse ClientInfoResponse

typedef castle_data.TopicConfig TopicConfig
typedef castle_data.TopicMonitorInfo TopicMonitorInfo
typedef castle_data.ConsumerGroupConfig ConsumerGroupConfig
typedef castle_data.ConsumerGroupMonitorInfo ConsumerGroupMonitorInfo
typedef castle_data.TopicInfo TopicInfo
typedef castle_data.ConsumerGroupInfo ConsumerGroupInfo
typedef castle_data.TopicConfigRequest TopicConfigRequest
typedef castle_data.TopicActionResponse TopicActionResponse
typedef castle_data.TopicConfigResponse TopicConfigResponse
typedef castle_data.TopicInfoResponse TopicInfoResponse
typedef castle_data.ConsumerGroupConfigRequest ConsumerGroupConfigRequest
typedef castle_data.TopicInfoListResponse TopicInfoListResponse
typedef castle_data.ConsumerGroupListResponse ConsumerGroupListResponse
typedef castle_data.ClusterNameRequest ClusterNameRequest
typedef castle_data.ConsumerGroupInfoResponse ConsumerGroupInfoResponse

//-----------------------------接口设置-----------------------------------------//
service CastleService {
    //-----------------------------mafka client接口设置-----------------------------------------//
    HeartBeatResponse getHeartBeat(1:HeartBeatRequest heartBeatRequest);

    //-----------------------------mq接口设置-----------------------------------------//
    ClientInfoResponse getClientInfo(1:ClientInfoRequest clientInfoRequest);

    //主题设置相关操作
    TopicActionResponse createTopic(1:TopicConfigRequest topicConfigRequest);
    TopicActionResponse updateTopic(1:TopicConfigRequest topicConfigRequest);
    TopicActionResponse deleteTopic(1:TopicConfigRequest topicConfigRequest);

    //主题监控
    TopicInfoResponse getTopicInfo(1:TopicConfigRequest topicConfigRequest);

    //消费组相关操作
    ConsumerGroupInfoResponse createConsumerGroup(1:ConsumerGroupConfigRequest consumerGroupConfigRequest);
    ConsumerGroupInfoResponse updateConsumerGroup(1:ConsumerGroupConfigRequest consumerGroupConfigRequest);
    ConsumerGroupInfoResponse deleteConsumerGroup(1:ConsumerGroupConfigRequest consumerGroupConfigRequest);

    //消费组监控
    ConsumerGroupInfoResponse getConsumerGroupInfo(1:ConsumerGroupConfigRequest consumerGroupConfigRequest);

    //集群所有主题和消费组的信息
    TopicInfoListResponse getClusterTopicAllInfo(1:ClusterNameRequest clusterNameRequest);
    ConsumerGroupListResponse getClusterGroupAllInfo(1:ClusterNameRequest clusterNameRequest);


}
