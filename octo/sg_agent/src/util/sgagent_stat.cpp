// =====================================================================================
//
//       Filename:  sgagent_stat.cpp
//
//    Description:  sg_agent,统计模块，用来统计各种性能数据
//
//        Version:  1.0
//        Created:  2015-03-27 15时46分52秒
//       Revision:  none
//
// =====================================================================================

#include "sgagent_stat.h"

namespace sg_agent {

bool InvokeStat::GetInvokeStatInfo(InvokeStat &statInfo, int &count) {
  muduo::MutexLockGuard lock(statInfo.statMutexLock_);
  //add Stat Info, added by tuyang
  bool isChange = false;
  int currentTime = time(0);
  //每分钟统计一次
  if (currentTime - statInfo.m_lastSecond >= statInfo.m_timeGap) {
    count = statInfo.m_count;
    isChange = true;

    //初始化
    statInfo.m_lastSecond = currentTime;
    statInfo.m_count = 1;
  } else {
    statInfo.m_count++;
  }
  return isChange;
}

InvokeStat::InvokeStat(int timeGap)
    : m_lastSecond(time(0)), m_count(0), m_timeGap(timeGap) {
};

void InvokeStat::SetTimeGap(int timeGap) {
  m_timeGap = timeGap;
}

} // namespace sg_agent
