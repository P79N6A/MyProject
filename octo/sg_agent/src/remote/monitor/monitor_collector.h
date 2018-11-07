#ifndef OCTO_SRC_MONITOR_COLLECTOR_H
#define OCTO_SRC_MONITOR_COLLECTOR_H
#include <assert.h>
#include <string>
#include <map>
#include <list>
#include <iterator>
#include <stdio.h>
#include <evhttp.h>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/shared_ptr.hpp>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/EventLoopThreadPool.h>
#include <boost/algorithm/string/trim.hpp>
#include "../../comm/cJSON.h"
#include "util/global_def.h"
#include "util/falcon_mgr.h"
#include "../falcon/falcon_collector.h"
#include "../../comm/inc_comm.h"
#include "../../sg_agent/config_client.h"
#include "../../sg_agent/file_config_client.h"
#include "../../sg_agent/mnscache_client.h"
#include "sg_agent/count_request.h"


namespace sg_agent {

typedef struct cpu_util{

  unsigned long total_cpu_delta;
  unsigned long proc_cpu_delta;
  cpu_util(){total_cpu_delta = 0.0;proc_cpu_delta = 0.0;};
}cpu_util_t;

class SgMonitorCollector {

 private:
  SgMonitorCollector() : m_has_init(false),
                         m_end_point(""),
                         m_agent_pid(""),
                         config_collector(NULL),
                         m_monitor_data(boost::unordered_map<std::string, int>()) {};
 public:
  ~SgMonitorCollector() {};
  static SgMonitorCollector *GetInstance();
  int DoInitMonitorInfo();
  int GetCollectorMonitorInfo(std::string &mInfo);
  int CollectorInfo2Json(cJSON *json, cJSON *json_arrary, int type);
  float CalcuProcCpuUtil(const int& pid);

 private:
  void SetValueByType(cJSON *root,int type);
  void GetEndPoint(std::string &value);
  int64_t GetTimeStamp();


  static SgMonitorCollector *s_instance;
  static muduo::MutexLock s_cmutex;
  std::map<int, std::string> m_metric_value;
  bool m_has_init;
  std::string m_end_point;
  std::string m_agent_pid;
  cpu_util_t s_cpuJiffValue;
  MtConfigCollector *config_collector;
  boost::unordered_map<std::string, int> m_monitor_data;
};

}
#endif //OCTO_SRC_MONITOR_COLLECTOR_H
