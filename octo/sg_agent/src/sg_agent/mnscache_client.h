// =====================================================================================
// 
//       Filename:  mnscache_client.h
// 
//    Description: sg_agent通过thrift协议访问MNSCache服务，快速获取ZK数据 
//                  by xuzhangjian 
//
//        Version:  1.0
// 
// =====================================================================================

#ifndef __MNSCACHE_CLIENT_H__
#define __MNSCACHE_CLIENT_H__
#include "sgagent_service_types.h"
#include "thrift_client_handler.h"
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include "sgcommon/common_interface.h"
#include <muduo/base/Mutex.h>
#include "count_request.h"
//#include <muduo/base/Mutex.h>
/* 
 *  定义mnscache业务逻辑处理类
 *
 * */
class MnsCacheCollector {
 public:

  MnsCacheCollector();

  static MnsCacheCollector *getInstance();
  static void Destroy();

  int getMNSCache(
      std::vector<SGService> &serviceList,
      const std::string &appkey,
      const std::string &version,
      const std::string &env,
      const std::string &protocol);

  int getServiceList(
      std::vector<SGService> &serviceList,
      const int &providerSize,
      const std::string &appKey,
      const std::string &version,
      const std::string &env,
      const std::string &protocol);

  int registerService(RegisterResponse& res,
                      const SGService& req);

 private:
  //获取服务列表，每隔10s检查一次服务列表是
  int _getServiceList();


  ThriftClientHandler *_getOneCollector();

 private:


  std::string m_appkey;
  int m_lastCheckTime;
  std::string localIp;
  std::vector<SGService> m_serviceList;
  static pthread_mutex_t m_CMutex;
  static MnsCacheCollector *mnsCacheCollector;
};

#endif

