// =====================================================================================
//
//    Version:  1.0
//    Revision:  none
//    Des:       sg_agent涉及到的错误码, 全部为负数
//    01xxxxx:sg_agent公共部分的错误码
//      0100xxx:buffer_mgr
//      0101xxx:MQ
//    02xxxxx:sg_agent前端错误码
//      0220xxx:sg_agent前端不分类的错误码
//      0201xxx:config错误码
//      0202xxx:SDK部分错误码
//
// =====================================================================================

#ifndef __SGAGENT_ERR_H__
#define __SGAGENT_ERR_H__

#define SUCCESS 0
#define FAILURE -1

/**
 * buffer_mgr: -01 00 xxx
 */
#define ERR_BUFFERMGR_EMPTYUPDATEFUNC -100001
#define ERR_BUFFERMGR_EMPTYKEY -100002
#define ERR_BUFFERMGR_EMPTYVALUE -100003
#define ERR_BUFFERMGR_BUFHEAD_NULL -100004
/**
 * mqservice: -01 01 xxx
 */
#define ERR_MQSVC_FIALEDTORCVMSG -101001
#define ERR_MQ_FULL -101008
#define ERR_DISK_WRITE -101009 //磁盘写出错

#define ERR_DISK_WRITE -101009 //磁盘写出错


/**
 * env: -0102xxx
 */
#define ERR_NOTGETIP -102001
#define ERR_OFFLINE -102002
#define ERR_NOTGETHOST -102003
#define ERR_NEEDNOT2STAGE -102004
#define ERR_FAILDTOSAVEXML -102005
#define ERR_FAILDTOLOADXML -102006
#define ERR_NOTMNSPATH -102007
#define ERR_NOSGAGENTMUTABLECONF -102008
#define ERR_NOSGAGENTCONF -102009
#define ERR_INVALID_ENV -102010
#define ERR_FAILDMKDIRWORKER -102011

/**
 * comm: -0103xxx
 */
#define ERR_COMM_NOTGETIP -103001

/**
 * sg_agent: 不分类错误码, -02 20 xxx
 */
#define ERR_CREATEPTHREAD_FAILED -220001
#define ERR_LOCCONF_MQ_RES_ERR -220002
#define ERR_GETROUTELIST_FROMWORKER_NULL -220003
#define ERR_GETSERVICELIST_FROMWORKER_NULL -220004
#define ERR_SETMSG -220005
#define ERR_SERVICE_REQMQ_INIT -220006
#define ERR_SERVICE_RESMQ_INIT -220007
#define ERR_ROUTE_REQMQ_INIT -220008
#define ERR_ROUTE_RESMQ_INIT -220009
#define ERR_LOCCONF_REQMQ_INIT -220010
#define ERR_LOCCONF_RESMQ_INIT -220011
#define ERR_REGIST_REQMQ_INIT -220012
#define ERR_LOGCOLLECTOR_INIT -220013
#define ERR_INVOKECOLLECTOR_INIT -220014
#define ERR_REGIST_REQMQ_NULL -220015
#define ERR_LOGCOLLECTOR_REQMQ_NULL -220016
#define ERR_INVOKECOLLECTOR_REQMQ_NULL -220017
#define ERR_LOCCONF_REQMQ_NULL -220018
#define ERR_LOCCONF_RESMQ_NULL -220019
#define ERR_ROUTE_REQMQ_NULL -220020
#define ERR_ROUTE_RESMQ_NULL -220021
#define ERR_SERVICE_REQMQ_NULL -220022
#define ERR_SERVICE_RESMQ_NULL -220023
#define ERR_LOCCONF_BUFFER_NULL -220024
#define ERR_ROUTE_BUFFER_NULL -220025
#define ERR_SERVICE_BUFFER_NULL -220026
#define ERR_LOGSERIALIZATION -220027
#define ERR_INVOKESERIALIZATION -220028
#define ERR_REGISTSERIALIZATION -220029
#define ERR_LOCCONFSERIALIZATION -220030
#define ERR_ROUTESERIALIZATION -220031
#define ERR_SERVICESERIALIZATION -220032
#define ERR_LOCCONFDESERIALIZATION -220033
#define ERR_ROUTEDESERIALIZATION -220034
#define ERR_SERVICEDESERIALIZATION -220035
#define ERR_LOCCONF_KEYSET_NULL -220036
#define ERR_SERVICE_KEYSET_NULL -220037
#define ERR_ROUTE_KEYSET_NULL -220038
#define ERR_MQCONTENT_TOOLONG -220039
#define ERR_INVALIDAPPKEY -220040
#define ERR_EMPTY_APPKEY -220041
#define ERR_FAILEDGETLOCALIP -220042
#define ERR_INVALID_WEIGHT -220043
#define ERR_FAILEDSENDMSG -220044
#define ERR_PARAMNOTCOMPLETE -220045
#define ERR_COMMONLOGCOLLECTOR_REQMQ_NULL -220046
#define ERR_GETMQ_TIMEOUT -220047
#define ERR_INVALID_PORT -220048
#define ERR_SERVICENAME_REQMQ_INIT -220049
#define ERR_SERVICENAME_RESMQ_INIT -220050
#define ERR_SERVICENAME_BUFFER_NULL -220051
#define ERR_SERVICENAME_REQMQ_NULL -220052
#define ERR_SERVICENAME_RESMQ_NULL -220053
#define ERR_SERVICENAME_KEYSET_NULL -220054
#define ERR_ILLEGAL_REGISTE -220055
#define ERR_INVALID_PROTOCOL -220056

//-201xxx预留给CONFIG
#define ERR_FAILEDTOINITMEMMGR -201001
#define ERR_CONFIG_REQMQ_NULL -201002
#define ERR_CONFIGDESERIALIZATION -201003
#define ERR_SET_NOTGETRESFROMMQ -201004 // MQ中未获取结果， 超时或者worker处理失败
#define ERR_GET_NOTGETRESFROMMQ -201005 // MQ中未获取结果， 超时或者worker处理失败
#define ERR_UPDATE_NOTGETRESFROMMQ -201006 // MQ中未获取结果， 超时或者worker处理失败
#define ERR_FAILED_SENDMQ -201007 // 发送消息到MQ失败
#define ERR_CANNOTSET -201008 // 由于buffer没有对应的conf值， 不能做setConfig操作
#define ERR_SGAGENT_UNKNOW_ERROR -201009
#define ERR_GETCONFIG_TIMEOUT -201010 // 获取结果超时
#define ERR_CREATECONNECTION -201011 // 建立连接失败
#define ERR_FAILEDTOGETCONFSERVLIST -201012 // 获取serviceList失败
#define ERR_CONFSERVLIST_EMPTY -201013 // 建立serviceList为空
#define ERR_CONFSERV_CONNFAILED -201014 // 建立连接失败
#define ERR_GETMERGEDATA_TEXCEPTION -201015 // 获取conf失败
#define ERR_GETFILECONF_TEXCEPTION -201016 // 获取文件配置失败
#define ERR_SYNCRELATION_TEXCEPTION -201017 // 同步配置主机失败
#define ERR_SETDATA_TEXCEPTION -201018 // 设置配置失败
#define ERR_CONFIG_FROMFILE -201119
#define ERR_CONFIG_EMPTYPATH -201120
#define ERR_CONFIG_EMPTYFILE -201121
#define ERR_CONFIG_OPENFILE_FAILED -201122
#define ERR_CONFIG_OPENFUNC_NOT -201123
#define ERR_CONFIG_MKDIR -201124
#define ERR_CONFIG_PARAM_APPKEYMISS -201125
#define ERR_CONFIG_PARAM_ENVMISS -201126
#define ERR_CONFIG_PARAM_PATHMISS -201127
#define ERR_CONFIG_PATH_EMPTY -201128
#define ERR_CONFIG_FILE_EMPTY -201129
#define ERR_CONFIG_LOAD_CONF_FAIL -201130
#define ERR_CONFIG_TASKSIZE_OVERLOAD -201132
#define ERR_CONFIG_INVILIDCMD -201133

// -2011xx for file config
#define ERR_FILECONFIG_REQMQ_INIT -201100
#define ERR_FILECONFIG_RESMQ_INIT -201101
#define ERR_FILECONFIG_REQMQ_NULL -201102
#define ERR_FILECONFIG_MQ_RES_ERR -201103
#define ERR_FILECONFIG_BUFFER_NULL -201104
#define ERR_FILECONFIG_SERIALIZATION -201105
#define ERR_FILECONFIG_DESERIALIZATION -201106
#define ERR_FILECONFIG_KEYSET_NULL -201107
#define ERR_FILECONFIG_MD5_WRONG -201108
#define ERR_FILECONFIG_MD5_SAME -201108
#define ERR_FILECONFIG_FROMFILE -201109

//预留给MtConfig Server错误码
#define ERR_NO_CHANGE -201302
#define ERR_UNKNOE_ERROR  -201500
#define ERR_PARAM_ERROR  -201501
#define ERR_NODE_NOT_EXIST  -201502
#define ERR_NOT_EXIST_VERSION  -201503
#define ERR_DEPRECATED_VERSION  -201504
//-203xxx for HLB
#define ERR_HLB_MQ_RES_ERR -203050
#define ERR_HLB_REQMQ_INIT -203051
#define ERR_HLB_RESMQ_INIT -203052
#define ERR_HLB_REQMQ_NULL -203053
#define ERR_HLB_RESMQ_NULL -203055
#define ERR_HLB_BUFFER_NULL -203056
#define ERR_HLB_SERIALIZATION -203057
#define ERR_HLB_DESERIALIZATION -203058
#define ERR_HLB_KEYSET_NULL -203059
//-204xxx for appkey
#define ERR_APPKEY_MQ_RES_ERR -204050
#define ERR_APPKEY_REQMQ_INIT -204051
#define ERR_APPKEY_RESMQ_INIT -204052
#define ERR_APPKEY_REQMQ_NULL -204053
#define ERR_APPKEY_RESMQ_NULL -204055
#define ERR_APPKEY_BUFFER_NULL -204056
#define ERR_APPKEY_SERIALIZATION -204057
#define ERR_APPKEY_DESERIALIZATION -204058
#define ERR_APPKEY_KEYSET_NULL -204059
//-205xxx for AUTH
#define ERR_AUTH_MQ_RES_ERR -205050
#define ERR_AUTH_REQMQ_INIT -205051
#define ERR_AUTH_RESMQ_INIT -205052
#define ERR_AUTH_REQMQ_NULL -205053
#define ERR_AUTH_RESMQ_NULL -205054
#define ERR_AUTH_BUFFER_NULL -205055
#define ERR_AUTH_SERIALIZATION -205056
#define ERR_AUTH_DESERIALIZATION -205058
#define ERR_AUTH_KEYSET_NULL -205059
//-2 06 xxx for http-properties
#define ERR_PROPERTIES_MQ_RES_ERR -206050
#define ERR_PROPERTIES_REQMQ_INIT -206051
#define ERR_PROPERTIES_RESMQ_INIT -206052
#define ERR_PROPERTIES_REQMQ_NULL -206053
#define ERR_PROPERTIES_RESMQ_NULL -206054
#define ERR_PROPERTIES_BUFFER_NULL -206055
#define ERR_PROPERTIES_SERIALIZATION -206056
#define ERR_PROPERTIES_DESERIALIZATION -206058
#define ERR_PROPERTIES_KEYSET_NULL -206059
//-2 07 xxx for quota
#define ERR_QUOTA_MQ_RES_ERR -207050
#define ERR_QUOTA_REQMQ_INIT -207051
#define ERR_QUOTA_RESMQ_INIT -207052
#define ERR_QUOTA_REQMQ_NULL -207053
#define ERR_QUOTA_RESMQ_NULL -207054
#define ERR_QUOTA_BUFFER_NULL -207055
#define ERR_QUOTA_SERIALIZATION -207056
#define ERR_QUOTA_DESERIALIZATION -207057
#define ERR_QUOTA_KEYSET_NULL -207058
#define ERR_QUOTA_PARAM_INVALID -207059
#define ERR_QUOTA_NOTOPEN -207060


// -202xxx预留给client SDK使用
// -2020xx预留给不分类错误码
// -2021xs预留给MtConfig SDK
#define ERR_FAILEDOPENCONNECTION -202101
#define ERR_FAILEDCLOSECONNECTION -202102
#define ERR_FAILEDTOKOWNONLINEOFFLINE -202103
#define ERR_JSONPARSE -202104

/**
* @Brief: sg_agent_worker: -03 00 xxx
*/
//-3 00 xxx for 通用错误码
#define ERR_CHECK_CONNECTION -300001
#define ERR_CREATE_CONNECTION -300002
#define ERR_CLOSE_CONNECTION -300003
#define ERR_GET_HANDLER_INFO_FAIL -300004
#define ERR_GET_HANDLER_FAIL -300005
#define ERR_SERVICELIST_NULL -300006
#define ERR_SERVICELIST_FAIL -300007
#define ERR_REQMQ_INIT -300008
#define ERR_RESMQ_INIT -300009
#define ERR_WORKER_CREATEPTHREAD_FAILED -300010
#define ERR_APPKEY_INVALID -300011
#define ERR_JSON_TO_DATA_FAIL -300012
#define ERR_DATA_TO_JSON_FAIL -300013
#define ERR_MQ_CONTENT_TOO_LONG -300014
#define ERR_CONFIG_PARAM_MISS -300015

//-3 01 xxx for zk
#define ERR_ZK_LIST_SAME_BUFFER -301001
#define ERR_GET_ZK_HANDLER_FAIL -301002
#define ERR_ZK_CONNECTION_LOSS -301003
#define ERR_ZK_EVENTLOOP_TIMEOUT -301004

//-3 02 xxx for serviceList
#define ERR_REGIST_SERVICE_ZK_FAIL -302001
#define ERR_NODE_LOST -302002
//-3 03 xxx for mnsc
#define ERR_MNSC_GET_MNSCACHE -303001
#define ERR_MNSC_GET_APPKEYLIST -303002
//-3 04 xxx for appkeylist
//-3 05 xxx for AUTH
#define ERR_AUTH_ROLE_WRONG -305001
//-3 06 xxx for http-properties
//-3 07 xxx for quota
//-3 08 xxx for logCollector
#define ERR_SEND_MODULE_FAIL -308003
#define ERR_SEND_LOGS_FAIL -308004
#define ERR_SEND_COMMONLOGS_FAIL -308005
//-3 09 xxx for CONFIG

//http return code
#define HTTP_RESPONSE_OK 200
#define HTTP_PARAM_ERROR 400
#define HTTP_INNER_ERROR 501
#define HTTP_NOT_SUPPORT 404



// zookeeper 自身errorcode, 该错误码已经用于thrift的API。【NOTE】不再允许对该错误码做任何变更
#define ERR_NODE_NOTFIND -101

#define ERR_INVALID_PARAM 400
#endif
