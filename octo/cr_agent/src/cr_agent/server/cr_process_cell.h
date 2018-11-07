#ifndef CRANE_SRC_PCELL_H
#define CRANE_SRC_PCELL_H

/************************************************************************/
/* 1.Class Name:PCell
   2.Class Function:
   3.The trace can map multiple pids
*/
/************************************************************************/
#include <string.h>
#include <iostream>
#include <vector>
#include <iostream>
#include <map>
#include <queue>
#include <unistd.h>
#include <errno.h>
#include <sys/wait.h>

typedef enum {

  INVALID_PID_STATE=0,
  RUNNING_FINISHING_STATE,   //表示运行正常
  RUNNING_FAILED_STATE,      //表示运行失败
  RUNNING_PID_STATE,         //表示正在运行
  RUNNING_UNKOWN_STATE,      //不确定状态
  ERROR_PID_STATE,

}CURRENT_PID_STATE;

class PCell {
 public:
  PCell();
  ~PCell() {};
  void SetRunTime(const char *rTime) { m_runTime = rTime; }
  void SetCPid(pid_t pID) { m_cPid = pID; }
  pid_t GetCPid() { return m_cPid; }
  const char *GetRunTime() { return m_runTime.c_str(); }

  void SetCPidState(CURRENT_PID_STATE pidState){ m_cPidState=pidState;}
  CURRENT_PID_STATE GetCPidState(){return (CURRENT_PID_STATE)m_cPidState;}
 private:
  pid_t m_cPid;
  int m_cPidState;
  std::string m_runTime;

 protected:

};

#endif
