namespace java com.meituan.mafka.thrift.castle

enum ErrorCode {
    OK                              = 0,    //request、response正常，且状态有变更
    NO_CHANGE                       = 1,    //request、response正常，状态未变更
    ILLEGAL_ROLE                    = 2,    //客户端角色设置非法
    ILLEGAL_APPKEY                  = 3,    //APPKEY不合法
    ILLEGAL_TOPIC                   = 4,    //TOPIC不合法
    ILLEGAL_GROUP                   = 5,    //消费组不合法
    REGISTER_FAIL                   = 6,    //客户端注册失败
    ILLEGAL_PARAM                   = 7,    //客户端hearbeat参数不合法
    ILLEGAL_CLIENT_INFO             = 8,    //客户端上报信息不合法
    ILLEGAL_CLIENT_CONFIG           = 9,    //客户端未配置
    ILLEGAL_PRODUCER_CONFIG         = 10,   //生产端未配置
    ILLEGAL_CONSUMER_CONFIG         = 11,   //消费端未配置
    NO_VERSION_FOUND                = 12,   //无法计算出当前heartbeat版本信息，可能原因：客户端配置信息缺失、broker缺失、partition缺失
    NO_TOPIC_CONFIG_FOUND           = 13,   //无法根据appkey、topic（group）找到对应的配置信息
    NO_BROKER_FOUND                 = 14,   //Broker全部宕机，或者网络闪断
    NO_CLUSTER_FOUND                = 15,   //当前客户端配置的集群不属于castle集群的管控中，castle集群在mcc配置管理的broker集群
    NO_PARTITION_FOUND              = 16,   //当前topic下没有发现有partition，这种情况是zk上信息被误删
    NO_PARTITION_ASSIGN             = 17,   //request、response正常, 原因：partition数<消费者数或者castle计算过慢，导致新上的消费者尚未进行分配
    OTHER_ERROR                     = 100
}

enum SendType {
    SYNC                              = 0,
    ASYN                              = 1
}

enum RequestType {
    ALL                              = 0,
    CONFIG                           = 1,
    MONITOR                          = 2
}

//------------------------mafka client端接口-----------------------//
enum ClientRole {
    PRODUCER = 0,
    CONSUMER = 1
}

struct ClientInfo {
    1: string version, //客户端代码发布版本号
    2: string hostname,
    3: string ip,
}

struct ProducerConfig {
    1: string appkey,
    2: string topic,
    3: string producerId
}

struct ConsumerConfig {
    1: string appkey,
    2: string topic,
    3: string groupName,
    4: string consumerId
}

struct ClientConfig {
    1: ProducerConfig producerConfig,
    2: ConsumerConfig consumerConfig
}

struct BrokerInfo
{
    1: i32 id,
    2: string host,
    3: i32 port
}

struct ProducerClusterInfo {
    1: string clusterName,
    2: list<BrokerInfo> brokerInfos,
    3: list<i32> partitionList
}

struct ProducerResponse { //topic粒度
    1: map<string, string> kvPair, //producer在zk上的配置信息
    2: map<string, ProducerClusterInfo> clusterInfoPair //目前只会返回一个集群
}

struct PartitionAssign {
    1: i32 generationId,
    2: list<i32> partitionList  //consumer分配到的partition
}

struct ConsumerClusterInfo {
    1: string clusterName,
    2: list<BrokerInfo> brokerInfos,
    3: PartitionAssign partitionAssign
}

struct ConsumerResponse { //group粒度
    1: map<string, string> kvPair, //consumer在zk上的配置信息
    2: map<string, ConsumerClusterInfo> clusterInfoPair //跨集群消费问题
}

struct ClientResponse { 
    1: ProducerResponse producerResponse,
    2: ConsumerResponse consumerResponse
}

//获取consumer Assignment接口信息
struct HeartBeatRequest {
    1: i32 version,
    2: ClientRole clientRole,
    3: i32 heartbeatTime, //客户端上报心跳时间，服务端根据这个心跳时间进行判断
    4: ClientInfo clientInfo,
    5: ClientConfig clientConfig
} 

struct HeartBeatResponse {
    1: ErrorCode errorCode,
    2: i32 version,
    3: ClientResponse clientResponse
}

//------------------------mq管理平台端接口-----------------------//

//获取客户端主机相关信息
struct ClientInfoRequest {
    1: ClientRole clientRole 
    2: string cluster,
    3: string topic,
    4: string group,
    5: optional string appkey
}

struct ProducerInfo {
    1: ClientInfo clientInfo,
    2: i64 createTime,
    3: optional i64 modifyedTime
}

struct ConsumerInfo {
    1: ClientInfo clientInfo,
    2: i64 createTime,
    3: i32 partition,
    4: optional i64 modifyedTime
}

//客户端的信息
struct ClientPartitionInfo {
    1: ClientInfo clientInfo,
    2: i32 partition,  //生产者的时候,这个字段可不填写.
    3: i64 createDTime,
    4: i64 modifyedTime,
    5: ClientRole clientRole,
    6: i32 threadId,
    7: string cluster
}

//主题配置
struct TopicConfig {
    1: string topicName,  //topic 是唯一的
    2: string appkey,
    3: list<string> cluster,
    4: i32  partition,
    5: i32  replica,
    6: SendType  sendType,
    7: i32  ack,
    8: optional bool sendSwitchEnable,
    9: optional i32 requestTimeOutMs,
    10: optional i32 queueMaxMs,
    11: optional i32 queueMaxMessages,
    12: optional i32 batchNumMessages,
    13: optional i32 messagSendFailMax,
    14: optional string clusterName,
    15: optional string idcSwitch,
    16: optional string mafkaConfigSource,
    17: optional i32 sendMessageVersion
}

//消费组配置
struct ConsumerGroupConfig {
    1: string topic,
    2: string appkey,
    3: string group,
    4: list<string> cluster,
    5: optional string topicAppkey,
    6: optional string clusterName,
    7: optional i32 consumerThreadNum,
    8: optional bool autoCommitEnable,
    9: optional i32 autoCommitIntervalMs,
    10: optional i32 rebalanceMaxRetries,
    11: optional i32 fetchMinBytes,
    12: optional i32 fetchWaitMaxMs,
    13: optional i32 zookeeperSyncTimeMs,
    14: optional i32 rebalanceBackOffMs,
    15: optional string autoOffsetReset,
    16: optional string idcSwitch,
    17: optional string mafkaIdcSwitch,
    18: optional string mafkaConfigSource,
    19: optional i32 consumerParallarNum,
    20: optional i32 consumeMessageDelay

}


struct ClientInfoResponse {
    1: ErrorCode errorCode,
    2: list<ProducerInfo> producerInfoList,
    3: list<ConsumerInfo> consumerInfoList,
    4: optional list<ClientPartitionInfo> mqClientInfoList,
}

struct TopicStatistic {
    1: i64 messageCount,
    2: i64 produceRate
}

struct GroupStatistic {
    1: i64 accumulation,
    2: i64 offset,
    3: i64 consumeRate,
    4: i64 logSize
}

struct PartitionInfo {
    1: i32 partitionId,
    2: TopicStatistic topicPartitionInfo,
    3: GroupStatistic groupPartitionInfo
}

//主题监控信息
struct TopicMonitorInfo {
    1: list<PartitionInfo> topicPartitionInfoList,
    2: TopicStatistic topicStatic
}

//消费组监控信息
struct ConsumerGroupMonitorInfo {
    1: list<PartitionInfo> partitionInfoList,
    2: GroupStatistic groupStatistic
}

//主题所有信息
struct TopicInfo {
    1: TopicConfig topicConfig,
    2: TopicMonitorInfo topicMonitorInfo
}

//消费组所有信息
struct ConsumerGroupInfo {
    1: ConsumerGroupConfig consumerGroupConfig,
    2: ConsumerGroupMonitorInfo consumerGroupMonitorInfo
}


//请求以及返回的封装
struct TopicConfigRequest {
    1: TopicConfig topicConfig,
    2: RequestType requestType
}

struct TopicActionResponse {
    1: ErrorCode errocode
}

struct TopicConfigResponse {
    1: ErrorCode errocode,
    2: TopicConfig topicConfig
}

struct TopicInfoResponse {
    1: ErrorCode errocode,
    2: TopicInfo topicInfo
}

struct ConsumerGroupConfigRequest {
    1: ConsumerGroupConfig consumerGroupConfig,
    2: RequestType requestType
}

struct ConsumerGroupInfoResponse {
    1: ErrorCode errocode,
    2: ConsumerGroupInfo consumerGroupInfo
}

struct TopicInfoListResponse {
    1: ErrorCode errocode,
    2: list<TopicInfo> topicInfoList
}

struct ConsumerGroupListResponse {
    1: ErrorCode errocode,
    2: list<ConsumerGroupInfo> consumerGroupInfoList
}

struct ClusterNameRequest {
    1: string cluster
}



