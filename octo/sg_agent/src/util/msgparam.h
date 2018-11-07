/**
 * @Brief 消息队列涉及到的各类参数数据结构
 */
#ifndef __MSGPARAM_H__
#define __MSGPARAM_H__
#include <string>
#include "sgagent_service_types.h"

//单个消息体最大限制：1.5M - 100B
#define DEFAULT_BUF_LEN 1572764
//单个消息体扩展字段最大限制：64B
#define DEFAULT_EXT_LEN 64

//定义消息类型
enum MQType{
    //sg_agent添加的消息，type = 1
    MQ_SG_AGENT = 1,
    //sg_agent_worker处理完后，添加到MQ的消息，type = 2
    MQ_SG_AGENT_WORKER,
    //sg_agent_worker处理完setConf后，添加到MQ的消息，type = 3
    MQ_SG_AGENT_WORKER_SET_CONF,
    //sg_agent_worker处理完updateConf后，添加到MQ的消息，type = 4
    MQ_SG_AGENT_WORKER_UPDATE_CONF,
    //sg_agent_worker返回轮询buffer内容，添加到MQ的消息，type = 5
    MQ_SG_AGENT_WORKER_SCAN_CONF,
    //sg_agent添加的http消息
    MQ_SG_AGENT_HTTP,
    //sg_agent_worker处理完后，添加到MQ的http消息
    MQ_SG_AGENT_WORKER_HTTP,
};

//定义消息队列key,唯一标示不同MQ
enum MQKey{
    SERVICE_KEY = 10101,
    ROUTE_KEY,
    CONFIG_KEY,
    REGISTER_KEY,
    SEND_LOG_KEY,
    SEND_MODULE_KEY,
    SEND_COMMON_LOG_KEY,
    PROC_CONF_KEY,
    QUOTA_KEY,
    PROC_FILE_CONF_KEY,
    HLB_KEY,
    APPKEY_KEY,
    AUTH_KEY,
    PROPERTIES_KEY,
    SERVICE_BY_PROTOCOL_KEY,
    ROUTE_BY_PROTOCOL_KEY,
    SERVICENAME_BY_PROTOCOL_KEY,
    SERVICE_BY_PROTOCOL_RES_KEY,
    ROUTE_BY_PROTOCOL_RES_KEY,
};

//定义配置管理中，不同接口对应消息类型
enum MtConfCmdType{
    //获取配置消息,对应getConf
    MQ_GET_CONF = 1,
    //添加配置消息, 对应setConf
    MQ_SET_CONF,
    //更新配置消息, 对应updateConf
    MQ_UPDATE_CONF,
    //同步配置节点信息, 对应syncRelation
    MQ_SYNC_CONF,
    //轮询配置节点信息, 对应syncRelation
    MQ_SCAN_CONF
};

// msg结构
struct sg_msgbuf{
    long mtype;       /*  message type, must be > 0 */
    char mtext[DEFAULT_BUF_LEN];    /* message data */
};


#endif
