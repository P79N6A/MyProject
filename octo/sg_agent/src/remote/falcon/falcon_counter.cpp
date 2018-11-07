//
// Created by Lhmily on 08/25/2017.
//

#include "falcon_counter.h"
namespace sg_agent {

std::string FalconCounter::GetMetric() {
  return m_metric;
}
void FalconCounter::SetMetric(const std::string &metric) {
  m_metric = metric;
}

std::string FalconCounter::GetTags() {
  return m_tags;
}
void FalconCounter::SetTags(const std::string &tags) {
  m_tags = tags;
}

unsigned long FalconCounter::GetTime() {
  return m_time;
}
void FalconCounter::SetTime(const unsigned long &time) {
  m_time = time;
}

std::string FalconCounter::GetValue() {
  return m_value;
}
void FalconCounter::SetValue(const std::string &value) {
  m_value = value;
}
}
