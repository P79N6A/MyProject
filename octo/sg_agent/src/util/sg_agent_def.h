/**
 * @Brief 消息队列涉及到的各类参数数据结构
 */
#include <stdint.h>

#include <string>

#ifndef __SGDEF_H__
#define __SGDEF_H__
namespace sg_agent {
const int MAX_BUF_SIZE = 1024;
const int kZkContentSize = 1024;
//协议类型
const int THRIFT_TYPE = 0;
const int HTTP_TYPE = 1;

const int REG_LIMIT_LEGAL = 1;

const int DEFAULT_SLEEPTIME = 10000;

const int DEFAULT_SPIN_NUM = 10;

/**
 * agent的配置文件
 */
const std::string SG_AGENT_CONF = "/opt/meituan/apps/sg_agent/sg_agent_env.xml";
const std::string SG_AGENT_MUTABLE_CONF = "/opt/meituan/apps/sg_agent/sg_agent_mutable.xml";
const std::string AGENT_SWITCH_FILE = "/opt/meituan/apps/sg_agent/agent_switch.xml";
const std::string CONFIG_SERVICELIST_FILE = "/opt/meituan/apps/sg_agent/sg_agent_servicelist.xml";
const std::string APPENV_FILE = "/data/webapps/appenv";
const std::string kStrIDCFileFullPath = "/opt/meituan/apps/sg_agent/idc.xml";

/**
 * agent环境
 */
const int ENVNAME_LEN = 3;
const std::string ENVNAME[ENVNAME_LEN] = {"test", "stage", "prod"};
const int SWITCH = 1;
const std::string ENVNEEDSWITCH = "0";
const std::string ENVNEEDNOTSWITCH = "1";

/**
 * 重试次数
 */
const int DEFAULT_LOCCONF_RETRY = 3;
const int DEFAULT_HLB_RETRY = 3;
const int DEFAULT_AUTH_RETRY = 3;
const int DEFAULT_QUOTA_RETRY = 3;
const int DEFAULT_ROUTE_RETRY = 3;
const int DEFAULT_SERVICE_RETRY = 3;
const int DEFAULT_SENDLOG_RETRY = 3;
const int DEFAULT_SENDCOMMONLOG_RETRY = 3;
const int DEFAULT_SENDINVOKE_RETRY = 3;
const int DEFAULT_REGIST_RETRY = 3;
const int DEFAULT_CONFIG_RETRY = 3;
const int DEFAULT_APPKEY_RETRY = 3;
const int DEFAULT_PROPERTIES_RETRY = 3;

/**
 * sg_agent时间变量，统一为us
 */
const int DEFAULT_LOCCONF_TIMEOUT = 100000;
const int DEFAULT_QUOTA_TIMEOUT = 500000;
const int DEFAULT_ROUTE_TIMEOUT = 100000;
const int DEFAULT_SERVICE_TIMEOUT = 200000;
const int DEFAULT_CONFIG_TIMEOUT = 500000;
const int DEFAULT_FILECONFIG_TIMEOUT = 500000;
const int DEFAULT_HLB_TIMEOUT = 200000;
const int DEFAULT_AUTH_TIMEOUT = 500000;
const int DEFAULT_APPKEY_TIMEOUT = 200000;
const int DEFAULT_PROPERTIES_TIMEOUT = 200000;

const int DEFAULT_LOCCONF_UPDATETIME = 10000;
const int DEFAULT_QUOTA_UPDATETIME = 10000;
const int DEFAULT_GETSERVICE_UPDATETIME = 10000;
const int DEFAULT_GETROUTE_UPDATETIME = 10000;
const int DEFAULT_GETCONFIG_UPDATETIME = 10000;
const int DEFAULT_HLB_UPDATETIME = 10000;
const int DEFAULT_AUTH_UPDATETIME = 10000;
const int DEFAULT_APPKEY_UPDATETIME = 10000;
const int DEFAULT_PROPERTIES_UPDATETIME = 10000;

// 轮训次数, 超过直接sleep
const int DEFAULT_MAXCOUNT = 10000;

/**
 * sg_agent统计时间变量，单位为s
 */
const int DEFAULT_TRIGGERTIME = 60;
const int DEFAULT_SCANTIME = 20;
const int DEFAULT_PROTOCOL_SCANTIME = 20;
const int DEFAULT_PROTOCOL_MAX_UPDATETIME = 60; // 1min
const int DEFAULT_SAVECONFIG_TIME = 60;
const int DEFAULT_CLEARCONFIG_TIME = 43200; // 12h
const int DEFAULT_SYNCCONFIG_TIME = 43200; // 12h

const int DEFAULT_LONGTIME_COUNT = 100; // 长sleep的阈值

const int16_t kHealthyCheckInitIntervalSecs = 1;
const int16_t kHealthyCheckMaxInterval = 60;

/**
* @Brief sg_agent_worker 时间变量 单位为毫秒
* @Date 2015-12-01
*/
const int LOGCOLLECTOR_TIMEOUT = 100;
const int LOGCOLLECTOR_SEND_TIMEOUT = 30;
const int MNSC_TIMEOUT = 600;
const int MTCONFIG_TIMEOUT = 1000;
const int ZK_CLIENT_TIMEOUT = 10000;
//retry times
const int MTCONFIG_RETRY = 3;
const int ZK_RETRY = 3;
const int MAFKA_CLIENT_RETRY = 3;

// config过期时间 5min
const int DEFAULT_EXTIME = 300000000;
// config过期检查触发时间 5min, 单位s
//const int DEFAULT_EXTIME_S = 43200;
// 临时修改为30days
const int DEFAULT_EXTIME_S = 7776000;

// config 同步时间 12h, 单位s
const int DEFAULT_SYNCTIME_S = 43200;
// mtconfig server的返回码与sg_agent的差值
const static int ERRCODE_DISC = -201000;
// mtconfig server return code
const int MTCONFIG_OK = 200;
const int MTCONFIG_NOT_CHANGE = 302;

// mnsc return code
const int MNSC_OK = 200;
const int MNSC_UPDATING = 500;
const int MNSC_RETRY_TIME = 3;

// auth role
const int AUTH_PROVIDER = 1;
const int AUTH_CONSUMER = 2;

// businessLine appkey 同步时间10分钟
const int APPKEY_SCANTIME = 600;

//properties by businessLine  
const int PROPERTIES_SCANTIME = 60;

// 地域过滤阈值
const double RegionThresHold = 0.0001;
// IDC过滤阈值
const double IdcThresHold = 0.1;

//监视配置文件间隔
const int CHECK_MONITOR_TIME = 300;

}
#endif
