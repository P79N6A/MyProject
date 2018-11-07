//
// Created by Lhmily on 08/25/2017.
//

#ifndef SG_AGENT_COLLECTOR_H
#define SG_AGENT_COLLECTOR_H
#include <string>
#include <map>
#include "muduo/net/EventLoopThread.h"
#include "muduo/net/EventLoop.h"
#include "falcon_counter.h"

namespace sg_agent {
class FalconItem;
typedef std::map<std::string, boost::shared_ptr<FalconItem> > FalconItemMap;
typedef boost::shared_ptr<FalconItemMap> FalconItemPtr;
class FalconCollector {
 public:
  ~FalconCollector() {}
  static muduo::net::EventLoopThread *GetCollectorThread();
  static void StartCollect();
  static int OnWriteData(void *buffer, size_t size, size_t nmemb, void *lpVoid);

  static void SetValue(const std::string &metric, const std::string &tags, const std::string &value);
  static void Count(const std::string &metric, const std::string &tags);
  static void RecordTime(const std::string &metric, const std::string &tags, unsigned long mills);
  static void SetRate(const std::string &metric,
                      const std::string &tags,
                      bool is_inc_rate_count = true);
  static FalconItemPtr GetAndReset();
 private:
  FalconCollector() {}
  static void DoCollect();
  static void Upload(std::vector<FalconCounter> &list);
  static bool SetEndPoint();
  static std::string GenCacheKey(const std::string &metric, const std::string &tags);
  static void MakeSureCacheExists(FalconItemPtr &ptr, const std::string &metric, const std::string &tags);
  static int HttpPost(const std::string &url, const std::string &post, std::string *response);

  static muduo::net::EventLoopThread s_collector_thread;
  static muduo::net::EventLoop *s_collecotr_loop;
  static muduo::MutexLock s_cache_check_lock;
  static double s_interval;
  static FalconItemPtr s_data_ptr;
  static std::string s_end_point;
  static std::string s_falcon_url;
};
}

#endif //SG_AGENT_COLLECTOR_H
