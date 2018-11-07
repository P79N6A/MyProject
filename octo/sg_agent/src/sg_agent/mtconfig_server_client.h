// =====================================================================================
// 
//       Filename:  mtconfig_server_client.h
// 
//    Description: sg_agent通过thrift协议访问mtconfig-server，实现动态配置管理相关功能 
//                  by xuzhangjian 
//
//        Version:  1.0
// 
// =====================================================================================

#ifndef __MTCONFIG_SERVER_CLIENT_H__
#define __MTCONFIG_SERVER_CLIENT_H__
#include "MtConfigService.h"
#include "config_service_types.h"
#include "sgagent_service_types.h"
#include "comm/inc_comm.h"
#include "util/SGAgentErr.h"
#include "thrift_client_handler.h"
#include "sgcommon_invoker.h"
#include <boost/unordered_map.hpp>
#include <string>
#include "count_request.h"
/*
 *  定义处理handler结构体
 * */
struct MtconfigCollectorInfo {
  ThriftClientHandler *m_pHandler;
  std::string m_host;
  int m_port;
  bool m_isavailable;
};

/*
 *  定义config_server业务逻辑处理类
 *
 * */
class MtConfigCollector {
 public:
  MtConfigCollector();
  int init();

  //与mtconfig_server交互：同步sg_agent使用的节点关系,以便mtconfig_server定时删除已经失效的节点
  int syncRelation(proc_conf_param_t &param);
  int syncRelationToMtconfig(proc_conf_param_t &param);

  //主动周期同步配置信息
  int SyncFileConf(FileConfigSyncResponse &resparam, FileConfigSyncRequest &oparam);
  //提供给SDK接口：从共享内存获取配置内容，返回给sdk
  int getConfigData(proc_conf_param_t &oparam);
  int getConfig(proc_conf_param_t &oparam);

  //获取静态配置文件
  int getFileConfig(file_param_t &files);
  int getFileConfigData(file_param_t &files);

  //提供给SDK接口：发送配置内容到mtconfig_server
  int setConfigData(proc_conf_param_t &oparam);
  int setConfig(proc_conf_param_t &oparam);

  int deleteCollector(const std::string &host, int port);


 private:
  //获取服务列表，每隔10s检查一次mtconfig_server列表是否正常
  int getServiceList(std::string& req_appkey, std::string& mcc_appkey);

  ThriftClientHandler *getOneCollector(int &err,std::string& req_appkey);

  int SyncFileConfToMtconfig(FileConfigSyncResponse &resparam, FileConfigSyncRequest &oparam);
  int SyncFileConfPeriodicToMtconfig(FileConfigSyncResponse &resparam, FileConfigSyncRequest &oparam);

 private:

  int LoadMccCfg();
  int GetMccClusterAppkey(std::string& req_appkey,std::string& mcc_appkey);


  std::string m_appkey;
  std::vector<SGService> m_serviceList;

  std::map<std::string, ThriftClientHandler *> m_collectList;
  std::vector<MtconfigCollectorInfo *> m_collectorHandlerList;
  static boost::unordered_map<std::string,std::string >m_bgAppkeyList;

  bool is_inited_;
  static bool is_mcc_cluster_init;



};

#endif

