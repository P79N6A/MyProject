//
// Created by Lhmily on 08/25/2017.
//

#ifndef SG_AGENT_COUNTER_H
#define SG_AGENT_COUNTER_H
#include <string>
using namespace __gnu_cxx;

namespace sg_agent {
class FalconCounter {
 public:
  FalconCounter() : m_time(0l) {}
  ~FalconCounter() {}
  std::string GetMetric();
  void SetMetric(const std::string &metric);

  std::string GetTags();
  void SetTags(const std::string &tags);

  unsigned long GetTime();
  void SetTime(const unsigned long &time);

  std::string GetValue();
  void SetValue(const std::string &value);

 private:
  std::string m_metric;
  std::string m_tags;
  unsigned long m_time;
  std::string m_value;
};
}

#endif //SG_AGENT_COUNTER_H
