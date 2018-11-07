//
// Created by smartlife on 2017/8/7.
//
#ifndef WORKSPACE_CSERVER_H
#define WORKSPACE_CSERVER_H
#include "../init/cr_interface.h"
#include <boost/shared_ptr.hpp>
#include <boost/make_shared.hpp>
#include <boost/scoped_ptr.hpp>
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
#include "../../comm/cJSON.h"
#include "../../comm/inc_comm.h"
#include "cr_process_cell.h"
#include "../init/tcp_server.h"
#include "../../comm/log4cplus.h"
#include "cr_command.h"
#define SAFE_FREE(p) { if(p) { free(p); (p)=NULL; } }
#define MAX_THREADS_NUM 4
#define MAX_SERVICE_NUM 20
#define MAX_WAIT_SERVICE_NUM 10

struct service_task{

  pthread_t m_Tid[MAX_THREADS_NUM];
  int m_TaskState;
};

class CAgentService {
 public:
  ~CAgentService();
  //对外服务接口
  rt_code_type CServiceOnMessage(int fd, char *data);
  rt_code_type CDeployOnMessage(int fd, char *data);
  rt_code_type CLogOnMessage(int fd, char *data);
  rt_code_type CDefaultMessage(int fd, char *data);


  int CServiceMonitor();
  static CAgentService* GetCSInstance();
  static void CraneSignalHandler(int signo);
  inline int GetWaitServiceLen(){return m_WaitCmdService.size();};
	inline int GetSchTimeOut(){return m_MaxSchTime;};
	int m_fd;
  int m_CurrentPid;

 private:
	boost::shared_ptr<CCommand> m_CurrentCmd;
	inline void SetCurrentCmd(boost::shared_ptr<CCommand> cmd){
	//	pthread_mutex_lock(&m_SMutex);
		m_CurrentCmd=cmd;
	//	pthread_mutex_unlock(&m_SMutex);
	}
	boost::shared_ptr<CCommand> GetCurrentCmd(){
		return m_CurrentCmd;
	}
  CAgentService();
  CAgentService(const CAgentService&);
  CAgentService& operator=(const CAgentService&);

	static void* RunHandler(void* args);
	int RunCmd(boost::shared_ptr<CCommand> cmd);
  inline void InitPara();
  inline void SetInputTime(unsigned long inPutTime){m_InputTime=inPutTime;};
  inline unsigned long GetInputTime(){return m_InputTime;};
  inline int SetCurrentServiceTrace(const char* cTraceId){m_CurrentServiceTrace=cTraceId;};
  inline const char* GetCurrentServiceTrace(){return m_CurrentServiceTrace.c_str();};
  void FindCurrTraceByCmd(int callBackPid,std::string& tmpTrace);
  inline int GetCmdServiceLen(){return m_CmdService.size();};
  const char* FindCurrTrace(int callBackPid);
  int RemoveCmdCellByFlag(const char* traceId);
  int RemoveCmdCell(const char* traceId);
  int RemoveWaitCmdCell(int rmWaitPid, const char* rmTraceId);
	int RemovePidCell(const char* traceId);
  int RunCallBackTask(int callBackPid);
  int RunningTask( boost::shared_ptr<CCommand> cmdCell);
  int AttemptRunningTask(const char *inData,boost::shared_ptr<CCommand> cmd);
  int WrapperRunCmd(boost::shared_ptr<CCommand> cmd,const char *cmdString);
  void printfBuffer(unsigned char * nmaMsg, int msgLen);
  int ScheduleSendTask(boost::shared_ptr<CCommand> cmd);
  int AttemptKillSendTask(boost::shared_ptr<CCommand> cmd);
  int CallBackTask(boost::shared_ptr<CCommand> cmd, int type);

  int RunCmdTask(const char *inData,boost::shared_ptr<CCommand> cmd);
  int ExecuteScheduleTheScript(boost::shared_ptr<CCommand> cmd);
  int ExecuteKillTheScript(boost::shared_ptr<CCommand> cmd);
  int RunNewTask(const char *inData, boost::shared_ptr<CCommand> cmdCell);
  int RunExistTask(const char *inData, boost::shared_ptr<CCommand> cmdCell);
  int CallBackMsgSend(boost::shared_ptr<CCommand> cmd, std::string &strJson);
  const char *GetCallBackIp(char *callBackList);
  const char *GetLocalIpAndPort() { return m_LocalAndPort.c_str(); }
  rt_code_type SetCommandValue(const char *netCmd, boost::shared_ptr<CCommand> cmdCell);
  int _Command2Json(int cmdType, boost::shared_ptr<CCommand> cmd, std::string &cmdJson);
  bool FindCmd(std::string traceId,boost::shared_ptr<CCommand>& findCmd);
  CCommand *FindCmdByTraceId() {};
  void InPutPCell(boost::shared_ptr<CCommand> cmd);
  int WrapperKillChild(const char *traceId);
  int KillChild(const char *traceId);
  int UpdateSendTask(const char *netCmd,boost::shared_ptr<CCommand> cmdCell);
  int KillExistTask(boost::shared_ptr<CCommand> cmdCell);
  //tools
  inline void SetChars(char *&pDes, const char *pSour, int nSetNum);
  inline void SetUInt32(char *&pchIODes, int u32In);
  inline void GetString(char *&pchInSour, char *pOut, int nNum);
  inline int GetUInt32(char *&pchInSour);
  inline int GetMaxAliveTime(){return m_MaxAliveTime;};
	inline int GetMaxThreadNum(){return m_MaxThreadNum;};
	inline int GetMaxTaskNum(){return m_MaxTaskNum;};
	int ToBytes(char *pBuffer, const int bufLen,std::string& jsonData,int sendLen);
  unsigned int GetLocalBoardIp(unsigned int fd);
  void SetIpAndPort(const char* localIp);
  void GetTmpTraceId(const char *netMsg, std::string &netCmd);
  bool CmdSafetyCheck(const char *cmdstring);
  void PrintConnectState(int erron);
  void InitTasks();
  void SetTraceAndCmd(char* traceId,boost::shared_ptr<CCommand> cmd);
	bool  FindCmdInMap(char* traceId,boost::shared_ptr<CCommand>& tmpCmd);
	int RemoveCmdInMap(const char* traceId);
  void BindSignalHandler();
	//Paras
  std::vector<boost::shared_ptr<CCommand> > m_CmdService;       //当前的命令服务数据结构
  std::vector<PidTrace_Info> m_WaitCmdService;   //进程ID<-->traceID的映射结构
	 std::map<std::string,boost::shared_ptr<CCommand> > m_Trace2Cmd;
  char m_SendBuffer[MAX_SEND_BUFFER];
  char m_RcvBuffer[MAX_SEND_BUFFER];
  char m_LocalIp[MAX_LOCAL_IP];
  char m_LocalMask[MAX_LOCAL_MASK];
  std::string m_JsonData;
  std::string m_LocalAndPort;
  std::string m_inNetCmd;
  CCommand m_CCmd;
  static pthread_mutex_t m_SMutex;
  static pthread_mutex_t m_SInsMutex;
  static pthread_rwlock_t m_CmdRwlock;
  static pthread_rwlock_t m_WCmdRwlock;
  static pthread_rwlock_t m_MCmdRwlock;
	int m_SendLen;
  unsigned long m_InputTime; //精确到秒即可
  unsigned int m_localIp;
  int m_MaxThreadNum;
	int m_MaxAliveTime;
	int m_MaxSchTime;
	int m_MaxTaskNum;
	std::string m_CurrentServiceTrace;
  static CAgentService* m_Instance;
  int m_PipFd[2];


 protected:

};

#endif //WORKSPACE_CSERVER_H
