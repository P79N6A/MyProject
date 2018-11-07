//
// Created on 2018/3/19.
//

#ifndef SG_AGENT_COUNT_REQUEST_H
#define SG_AGENT_COUNT_REQUEST_H
#include <string>
#include <boost/unordered_map.hpp>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Mutex.h>


namespace sg_agent {


class CountRequest {
 public:

  void CountConfigReq(bool is_success);
  void CountFConfigReq(bool is_success);
  void CountMnscReq(bool is_success);
  void GetReqData(boost::unordered_map<std::string, int> &monitor_data);
  static CountRequest* GetInstance();

 private:
  CountRequest();
  ~CountRequest();


  unsigned int m_config_count;
  unsigned int m_fconfig_count;
  unsigned int m_allfconfig_count;
  unsigned int m_allconfig_count;
  unsigned int m_mnsc_count;
  unsigned int m_allmnsc_count;
  static CountRequest *m_count_client;
  static muduo::MutexLock m_cache_lock;
  static muduo::MutexLock m_instance_lock;
};
}

#endif //SG_AGENT_COUNT_REQUEST_H
