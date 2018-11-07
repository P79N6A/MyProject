//
// Created by smartlife on 2017/8/7.
//

#ifndef CRANE_AGENT_CR_COMMAND_H
#define CRANE_AGENT_CR_COMMAND_H
#include <boost/shared_ptr.hpp>
#include <boost/make_shared.hpp>
#include <stdio.h>
#include <signal.h>
#include <fcntl.h>
#include <sstream>
#include <memory>
#include <string.h>
#include <vector>
#include <iostream>
#include <map>
#include <queue>
#include <unistd.h>
#include <errno.h>
#include <signal.h>
#include <sys/wait.h>
#include <list>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/uio.h>
#include <netinet/tcp.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <net/if_arp.h>
#include "../protocol/CmdTypes.h"
#include "../../util/cr_common.h"
#include "../../comm/cJSON.h"
#include "cr_process_cell.h"
#include "../init/tcp_server.h"
#include "../../comm/log4cplus.h"

class CCommand {
 public:
  CCommand();
  ~CCommand() {};

  std::list<boost::shared_ptr<PCell> > m_pCell;
  int m_RunState;
  void SetCmdType(char *inString);
  void SetTaskName(char *inString) { m_taskName = inString; };
  void SetJobCode(char *inString) { m_jobUniqueCode = inString; };
  void SetTraceId(char *inString) { m_traceId = inString; };
  void SetRunState(int runState) { m_runState = runState; };
  void SetCallbackAddr(int index, char *inString);
  void SetRunCmd(char *inString) { m_Command = inString; };
  void SetNode(char *inString) { m_Node = inString; };
  void SetIpAndPort(char *inString) { m_IpAndPort = inString; };
  void SetKilledFlag(bool flag) { m_KilledFlag = flag; };
  void SetCmdRmFlag(bool rmFlag){m_cmdRmFlag=rmFlag;};
  void SetWaitCmdInfo(PidTrace_Info& pidInfo);

  inline void SetInputTime(unsigned long inPutTime){m_InputTime=inPutTime;};
  inline unsigned long GetInputTime(){return m_InputTime;}; 
	bool GetCmdRmFlag(){return m_cmdRmFlag;};
  inline int GetCmdType() { return m_cmdType; };
  inline int GetRunState() { return m_runState; };
  inline char*  GetCallbackAddr() { return m_callBackAddrStr[0]; };  //当前取第一个IP地址为callback地址
  inline const char* GetTaskName() { return m_taskName.c_str(); };
  inline const char* GetJobCode() { return m_jobUniqueCode.c_str(); };
  inline const char* GetTraceId() { return m_traceId.c_str(); };
  inline const char* GetNode() { return m_Node.c_str(); };
  inline const char* GetIpAndPort() { return m_IpAndPort.c_str(); };
  inline const char* GetCmd() { return m_Command.c_str(); };
  const char* GetStrCmd(int cmdType);
  int GetCPid() { return m_CPid; }
  int GetWaitCmdPid();
  const char* GetWaitCmdTraceId();

  int CS_Command2Json(char *strJson);
  void CInit();
  void SetTaskName();
  std::string CGetTaskName(int cmdType);
  void SetValueByIndex(int index, char *inString);
  unsigned int BkdrHash(const char *key);

  int _CommandCheck(int type, std::string taskName);
  int _CommandDispatch();
  int _RunCommand();
  std::string GetSchSendTask();
  std::string GetSchRcvTask();
  std::string GetKillSendTask();
  std::string GetKillRcvResult();


  void SetPidNumPlus(){m_CPidNum++;};
  int GetPidNum(){return m_CPidNum;};
  void SetRunResult(int runResult) { m_RunResult = runResult; }
  int GetRunResult() { return m_RunResult; }
  bool GetCurrentRunningState();
  int m_CPid;

  inline std::string GetTaskByType(int cmdType) { return m_cmdToString.at(cmdType); };
  std::list<boost::shared_ptr<PCell> > GetCurrentPcell() { return m_pCell; }

 private:

  std::string m_taskName;
  std::string m_jobUniqueCode;
  std::string m_traceId;
  std::string m_Node;
  std::string m_IpAndPort;
  std::string m_Command;
  bool m_cmdRmFlag;
  bool m_KilledFlag;
  int m_cmdType;
  int m_callBackAddr;
  int m_runState;
  int m_CPidNum;
  int m_RunResult;
  int m_InputTime;
  char m_callBackAddrStr[MAX_IP_NUM][MAX_IP_LEN];
  std::map<int, std::string> m_cmdToString;
  PidTrace_Info m_WaitPidInfo;

 protected:

};

#endif //CRANE_AGENT_CR_COMMAND_H
