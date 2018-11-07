// =====================================================================================
// 
//       Filename:  remote_log_collector.h
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-09-22 17时16分34秒
//       Revision:  none
// 
// 
// =====================================================================================

#ifndef __REMOTE_LOG_COLLECTOR_H__
#define __REMOTE_LOG_COLLECTOR_H__
#include <pthread.h>

#include "sgagent_service_types.h"
#include "sgagent_worker_service_types.h"
#include "aggregator_service_types.h"
#include "util/sgservice_manager.h"

#include "thrift_client_handler.h"

struct CollectorHandlerInfo {
  ThriftClientHandler *m_pHandler;
  std::string m_host;
  int m_port;
  bool m_isavailable;
};

class RemoteLogCollector {
 public:
  RemoteLogCollector();
  ~RemoteLogCollector();
  int sendCommonLogs(const CommonLog &oCommonLog);
  int sendModuleInvokeInfo(const SGModuleInvokeInfo &oInfo);
  int deleteCollector(const std::string &host, int port);

  std::vector<CollectorHandlerInfo *> m_collectorHandlerList;
 private:
  //每隔30s对logCollector的连接池进行管理
  int _getServiceList();

  int getServiceListFromMNSC(std::vector<SGService> &servicelist);

  CollectorHandlerInfo *_getOneCollector();

  // 即针对增删情况， 权重修改等操作， 未处理
  void _addHandler(
      const std::vector<SGService> &vec_sgservice_add);
  void _deleteHandler(
      const std::vector<SGService> &vec_sgservice_delete);
  void _changeHandler(
      const std::vector<SGService> &vec_sgservice_change);

 private:
  std::string m_appkey;
  int m_lastCheckTime;
  SGServiceManager sgserviceMgr;
};

#endif

