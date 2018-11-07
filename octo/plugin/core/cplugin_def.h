#ifndef CPLUGINDEF_H_
#define CPLUGINDEF_H_
#include "plugindef.h"

typedef enum CPLUGIN_OPERATION_TYPE{
    CPLUGIN_OPERATION_TYPE_START,
    CPLUGIN_OPERATION_TYPE_REMOVE,
    CPLUGIN_OPERATION_TYPE_STOP,
    CPLUGIN_OPERATION_TYPE_RESTART,
    CPLUGIN_OPERATION_TYPE_UPGREAD,
    CPLUGIN_OPERATION_TYPE_ROLLBACK,
    CPLUGIN_OPERATION_TYPE_STARTNEW,
    CPLUGIN_OPERATION_TYPE_GETINFO,
    CPLUGIN_OPERATION_TYPE_KEEPALIVE,
    CPLUGIN_OPERATION_TYPE_UPDATEFILE,
    CPLUGIN_OPERATION_TYPE_STOPALL,
    CPLUGIN_OPERATION_TYPE_MONITOR
}CPLUGIN_OPERATION_TYPE;

typedef enum CPLUGIN_RPC_ERROR{
    CPLUGIN_ERR_OK = 0,
    CPLUGIN_ERR_DOWNLOADING_ERROR = -2,
    CPLUGIN_ERR_INVALID_UPDATING = -3,
    CPLUGIN_ERR_RUNNING_ERROR = -4,
    CPLUGIN_ERR_SOCKET_ERROR = -5,
    CPLUGIN_ERR_KILLING_ERROR = -6,
    CPLUGIN_ERR_INVALID_PID = -7,
    CPLUGIN_ERR_SOCKET_TIMEOUT = -8,
    CPLUGIN_ERR_CPLUGIN_EMPTY_CONFIG = -9,
    CPLUGIN_ERR_CPLUGIN_ERROR_CONFIG = -10,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_ALREADY_START = -11,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_ALREADY_VERSION = -12,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_NOT_START = -13,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_BUSY = -15,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_TIMEOUT = -16,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_CONFIG_ERR = -17,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_NO_SUPPORT = -18,
    CPLUGIN_ERR_CPLUGIN_PLUGIN_INTERNAL = -19,
    CPLUGIN_ERR_CPLUGIN_INVILAD_PARAMS = -20,
    CPLUGIN_ERR_IDC_FILE_FALIED = -21,
    CPLUGIN_ERR_PATH_ERROR = -22,
    CPLUGIN_ERR_STOP_FAILED = -23,
    CPLUGIN_ERR_FILE_INTEGRITY = -24,
    CPLUGIN_ERR_DISK_FULL = -25,
    CPLUGIN_ERR_UNKNOW = -216
}CPLUGIN_RPC_ERROR;

typedef enum MONITER_STATUS {
    MONITER_STATUS_RUNNING = 200,
    MONITER_STATUS_NOT_FOUND = 401,
    MONITER_STATUS_INTERNAL_ERROR = 501,
    MONITER_STATUS_STOP = 503,
    MONITER_STATUS_TIMEWOUT = 504
}MONITER_STATUS;

typedef struct CPluginNode{
    std::vector<PreloadInfo> preload_infos_;
    HostInfo host_info_;
    std::vector<PluginInfo> plugin_infos_;
    std::vector<ConfigInfo> config_infos_;

    pid_t plugin_pid_;
    int push_fd_;
    std::string now_version_;
    PluginStatu statu_;
    bool  is_ok_;
    bool  download_;

    muduo::Timestamp last_time_download_failed;
    int  last_download_failed_inernal;
    int  last_download_failed_times;

}CPluginNode, p_CPluginNode;

#define  CPlugin_kOperatorTimeOut  5000000

#endif // PLUGINDEF_H_
