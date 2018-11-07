// =====================================================================================
//
//      Filename:  sgagent_stat.h
//
//      Description:   sg_agent统计模块
//
//      Version:  1.0
//      Created:  2015-03-27 15时47分50秒
//      Revision:  none
//
//
// =====================================================================================

#ifndef __SGAGENT_STAT__H__
#define __SGAGENT_STAT__H__

#include <muduo/base/Mutex.h>
#include "comm/inc_comm.h"
#include "sg_agent_def.h"

// 默认触发间隔60s
namespace sg_agent {

class InvokeStat {
 public:
  InvokeStat(int timeGap = DEFAULT_TRIGGERTIME);
  /**
   * 获取统计信息并返回是否切换标记
   * timeGap: 触发事件间隔， 默认为m_timeGap， 单位为s
   */
  static bool GetInvokeStatInfo(InvokeStat &statInfo, int &count);

  void SetTimeGap(int timeGap);

  int m_lastSecond;
  int m_count;
  int m_timeGap;
  muduo::MutexLock statMutexLock_;
};

} // namespace

#endif
