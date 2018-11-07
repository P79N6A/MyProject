//
// Created on 2018/3/19.
//
#include <zconf.h>
#include "count_request.h"

using namespace sg_agent;


CountRequest *CountRequest::m_count_client = NULL;
muduo::MutexLock CountRequest::m_cache_lock;
muduo::MutexLock CountRequest::m_instance_lock;


CountRequest::CountRequest():m_mnsc_count(0),
                             m_config_count(0),
                             m_fconfig_count(0),
                             m_allmnsc_count(0),
                             m_allconfig_count(0),
                             m_allfconfig_count(0) {}

CountRequest::~CountRequest() {}


CountRequest* CountRequest::GetInstance() {
  if (NULL == m_count_client) {
    muduo::MutexLockGuard lock(m_instance_lock);
    if (NULL == m_count_client) {
      m_count_client = new CountRequest();
    }
  }
  return m_count_client;
}

void CountRequest::CountConfigReq(bool is_success) {
  muduo::MutexLockGuard lock(m_cache_lock);
  ++m_allconfig_count;
  if (is_success) {
    ++m_config_count;
  }
}

void CountRequest::CountFConfigReq(bool is_success) {
  muduo::MutexLockGuard lock(m_cache_lock);
  ++m_allfconfig_count;
  if (is_success) {
    ++m_fconfig_count;
  }
}

void CountRequest::CountMnscReq(bool is_success) {
  muduo::MutexLockGuard lock(m_cache_lock);
  ++m_allmnsc_count;
  if (is_success) {
    ++m_mnsc_count;
  }
}

void CountRequest::GetReqData(boost::unordered_map<std::string, int> &monitor_data) {

  muduo::MutexLockGuard lock(m_cache_lock);
  monitor_data["allconfig"] = m_allconfig_count;
  monitor_data["allfconfig"] = m_allfconfig_count;
  monitor_data["allmnsc"] = m_allmnsc_count;
  monitor_data["config"] = m_config_count;
  monitor_data["fconfig"] = m_fconfig_count;
  monitor_data["mnsc"] = m_mnsc_count;

  m_allconfig_count = 0;
  m_allfconfig_count = 0;
  m_allmnsc_count = 0;
  m_config_count = 0;
  m_fconfig_count = 0;
  m_mnsc_count = 0;
}