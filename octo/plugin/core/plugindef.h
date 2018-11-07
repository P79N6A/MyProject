#ifndef PLUGINDEF_H_
#define PLUGINDEF_H_

#include <string>
#include <vector>

#include "define/port.h"

typedef void* (*CreateFunc)();
typedef void (*DestroyFunc)(void*);

typedef struct RegisterParams {
  CreateFunc create_func;
  DestroyFunc destroy_func;
  std::string library_name;
} RegisterParams;

typedef bool (*RegisterFunc)(const std::string& type, const RegisterParams& params);
typedef int (*InvokeServiceFunc)(int service_name, void* service_params);

typedef struct HostServices {
  RegisterFunc register_func;
  InvokeServiceFunc invoke_service_func;
  void* context;
} HostServices;

typedef struct PreloadInfo{
  std::string name;
  std::string library_name;
  std::string hash;
  HANDLE handle;
}PreloadInfo;

typedef struct HostInfo {
  std::string name;
  std::string library_name;
  std::string hash;
}HostInfo;

typedef struct PluginInfo {
  std::string name;
  std::string library_name;
  std::string hash;
  int hot_update;
}PluginInfo;

typedef struct ConfigInfo {
  std::string link_name;
  std::string config_name;
  std::string hash;
  int hot_update;
}ConfigInfo;

/**
* @Brief sg_agent_worker 时间变量 单位为毫秒
* @Date 2015-12-01
*/

const int CONTROL_SERVER_TIMEOUT = 100;
const int SG_AEGNT_TIMEOUT = 30;



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

#define  kMaxPendingTasks  50
#define  kOperatorTimeOut  10000

typedef enum PluginStatu{
    PLUGIN_STATU_ORIGIN,
    PLUGIN_STATU_RUNING,
    PLUGIN_STATU_UPDATING,
    PLUGIN_STATU_STOPED
}PluginStatu;

typedef enum OPERATION_TYPE{
    OPERATION_TYPE_START,
    OPERATION_TYPE_REMOVE,
    OPERATION_TYPE_STOP,
    OPERATION_TYPE_RESTART,
    OPERATION_TYPE_UPGREAD,
    OPERATION_TYPE_ROLLBACK,
    OPERATION_TYPE_STARTNEW,
    OPERATION_TYPE_GETINFO,
    OPERATION_TYPE_KEEPALIVE
}OPERATION_TYPE;

typedef struct PluginNode{
    std::vector<PreloadInfo> preload_infos_;
    HostInfo host_info_;
    std::vector<PluginInfo> plugin_infos_;
    std::vector<ConfigInfo> config_infos_;

    pid_t plugin_pid_;
    int push_fd_;
    std::string now_version_;
    PluginStatu statu_;
    bool  is_ok_;
}PluginNode, p_PluginNode;

typedef struct IDCInfo{
    std::string host_name_;
    std::string local_ip_;
    std::string idc_;
    std::string center_name_;
    std::string region_;
    bool online_;
    std::string env_;
    std::string octo_env_;
    std::string host_;
    std::string os_version_;
    std::string str_sentinel_host_url_;
}IDCInfo, *p_IDCInfo;

typedef enum RPC_ERROR{
    ERR_OK = 0,
    ERR_DOWNLOADING_ERROR = -2,
    ERR_INVALID_UPDATING = -3,
    ERR_RUNNING_ERROR = -4,
    ERR_SOCKET_ERROR = -5,
    ERR_KILLING_ERROR = -6,
    ERR_INVALID_PID = -7,
    ERR_SOCKET_TIMEOUT = -8,
    ERR_CPLUGIN_EMPTY_CONFIG = -9,
    ERR_CPLUGIN_ERROR_CONFIG = -10,
    ERR_CPLUGIN_PLUGIN_ALREADY_START = -11,
    ERR_CPLUGIN_PLUGIN_ALREADY_VERSION = -12,
    ERR_CPLUGIN_PLUGIN_NOT_START = -13,
    ERR_CPLUGIN_PLUGIN_BUSY = -15,
    ERR_CPLUGIN_PLUGIN_TIMEOUT = -16,
    ERR_CPLUGIN_PLUGIN_CONFIG_ERR = -17,
    ERR_CPLUGIN_PLUGIN_NO_SUPPORT = -18,
    ERR_CPLUGIN_PLUGIN_INTERNAL = -19,
    ERR_CPLUGIN_INVILAD_PARAMS = -20,
    ERR_IDC_FILE_FALIED = -21,
    ERR_UNKNOW = -216
}RPC_ERROR;

typedef enum UPDATE_TYPE{
    UPDATE_TYPE_ALL = 0,
    UPDATE_TYPE_PLUGIN= 1,
    UPDATE_TYPE_UNKNOW =2,
    UPDATE_TYPE_CONFIG =3,
}UPDATE_TYPE;



#define CONFIG_FOR_NORMAL "config.xml"
#endif // PLUGINDEF_H_
