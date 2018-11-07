//
// Created by smartlife on 2017/8/7.
//
#include "cr_service.h"
#include <stdlib.h>
#define MAX_HEADER_LENGTH 4
#define MAX_INT 2147483647 

pthread_mutex_t CAgentService::m_SInsMutex = PTHREAD_MUTEX_INITIALIZER;
pthread_rwlock_t CAgentService::m_CmdRwlock = PTHREAD_RWLOCK_INITIALIZER;
pthread_mutex_t CAgentService::m_SMutex = PTHREAD_MUTEX_INITIALIZER;
pthread_rwlock_t CAgentService::m_WCmdRwlock = PTHREAD_RWLOCK_INITIALIZER;
pthread_rwlock_t CAgentService::m_MCmdRwlock = PTHREAD_RWLOCK_INITIALIZER;

static boost::scoped_ptr<CAgentService> m_Instance;
CAgentService *CAgentService::m_Instance = NULL;
void CAgentService::InitTasks() {

}

CAgentService::CAgentService() {

  pthread_mutex_init(&m_SInsMutex, NULL);
  pthread_rwlock_init(&m_CmdRwlock, NULL);
  pthread_rwlock_init(&m_WCmdRwlock, NULL);
  memset(m_SendBuffer, 0, MAX_SEND_BUFFER);
  memset(m_RcvBuffer, 0, MAX_SEND_BUFFER);
  memset(m_LocalIp, 0, MAX_LOCAL_IP);
  memset(m_LocalMask, 0, MAX_LOCAL_MASK);

  m_SendLen = 0;
  m_localIp = 0;
  InitPara();

}
CAgentService::~CAgentService() {

  pthread_mutex_destroy(&m_SInsMutex);
  pthread_rwlock_destroy(&m_CmdRwlock);
  pthread_rwlock_destroy(&m_WCmdRwlock);
}
CAgentService *CAgentService::GetCSInstance() {

  if (NULL == m_Instance) {

    pthread_mutex_lock(&m_SInsMutex);
    if (NULL == m_Instance) {
      m_Instance = new CAgentService();
    }
    pthread_mutex_unlock(&m_SInsMutex);
  }
  return m_Instance;
}
void CAgentService::SetTraceAndCmd(char* traceId,boost::shared_ptr<CCommand> cmd){

  pthread_rwlock_wrlock(&m_MCmdRwlock);  
	m_Trace2Cmd.insert(std::map<std::string,boost::shared_ptr<CCommand> >::value_type(traceId,cmd));	
  pthread_rwlock_unlock(&m_MCmdRwlock);  
}
bool CAgentService::FindCmdInMap(char* traceId,boost::shared_ptr<CCommand>& tmpCmd ){

  pthread_rwlock_rdlock(&m_MCmdRwlock);  
	std::map<std::string,boost::shared_ptr<CCommand> >::iterator itor;
	if(m_Trace2Cmd.size()>0){
		itor=m_Trace2Cmd.find(traceId);
		if(itor==m_Trace2Cmd.end()){
			LOG_INFO("can not find cmd in map");
		}else {
			tmpCmd=itor->second;
      pthread_rwlock_unlock(&m_MCmdRwlock);  
			return true;
		}
	}
	pthread_rwlock_unlock(&m_MCmdRwlock);  
	return false;
}
bool CAgentService::CmdSafetyCheck(const char *cmdstring) {
	if (NULL == cmdstring) {
		return false;
	}
	char bannerCommand[MAX_BANNER_COMMAND][MAX_COMMAND_LEN] = {

		"rm -rf ", "rm -r *", "rm -rf /",
		"rm -rf .", ":(){:|:&};:", "> /dev/sda",
		"mv * /dev/null", "wget http://malicious_source -O- | sh",
		"mkfs.ext3 /dev/sda", "> *file", "^foo^bar",
		"dd if=/dev/random of=/dev/sda", "systemctl reboot"
	};

	for (int i = 0; i < MAX_BANNER_COMMAND; ++i) {

		std::string::size_type position;
		std::string srcStr = cmdstring;
		position = srcStr.find(bannerCommand[i]);
		if (position != srcStr.npos)
		{
			return true;
		} else {
			return false;
		}
	}
	return false;
}
const char* CCommand::GetStrCmd(int cmdType) {

  if (cmdType >= SCHEDULE_SEND_TASK && cmdType <= ATTEMPT_RUNNING_RESULT) {
    return m_cmdToString.at(cmdType).c_str();
  } else {
    return NULL;
  }
  return NULL;
}
void CAgentService::InitPara() {

  if (getIntranet(m_LocalIp, m_LocalMask) < 0) {
    LOG_ERROR("getIntranet failed" << m_LocalIp);
    return;
  } else {
    LOG_INFO("getIntranet ip and mask are " << m_LocalIp << "," << m_LocalMask);
  }
	if(NULL != CCInterface::GetCCInstance()) {
		CCInterface::GetCCInstance()->GetConfigIns()->LoadConf();
		m_MaxThreadNum=CCInterface::GetCCInstance()->GetConfigIns()->GetMaxThreadNum();
		m_MaxAliveTime=CCInterface::GetCCInstance()->GetConfigIns()->GetMaxAliveTime();
	  m_MaxSchTime=CCInterface::GetCCInstance()->GetConfigIns()->GetSchTime();
	  m_MaxTaskNum=CCInterface::GetCCInstance()->GetConfigIns()->GetMaxTaskNum();
		LOG_INFO("ThreadNum: "<<m_MaxThreadNum<<",AliveTime: "<<m_MaxAliveTime<<"SchTime: "<<m_MaxSchTime<<"TaskNum: "<<m_MaxTaskNum);
	}
}
void CAgentService::GetTmpTraceId(const char *netMsg, std::string &netCmd) {
  if (NULL == netMsg) return;

  cJSON *cmd = cJSON_Parse(netMsg);

  if (NULL == cmd) {
    return;
  }
  netCmd = (char *) cJSON_GetObjectItem(cmd, "traceId")->valuestring;

  cJSON_Delete(cmd);
  cmd = NULL;

}
rt_code_type CAgentService::SetCommandValue(const char *netCmd, boost::shared_ptr<CCommand> cmdCell) {

  if (NULL == netCmd) return INVALID_RUN_CMD;
  cJSON *cmd, *callBackArray;
  if (NULL == cmdCell) {
    return INVALID_RUN_CMD;
  }
  cmd = cJSON_Parse(netCmd);

  if (NULL == cmd) {
    return INVALID_RUN_CMD;
  }
  cJSON *pItem = cJSON_GetObjectItem(cmd, "type");
  if (pItem) {
    cmdCell->SetCmdType(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(cmd, "taskName");
  if (pItem) {
    cmdCell->SetTaskName(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(cmd, "jobUniqueCode");
  if (pItem) {
    cmdCell->SetJobCode(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(cmd, "traceId");
  if (pItem) {
    cmdCell->SetTraceId(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(cmd, "runState");
  if (pItem) {
    cmdCell->SetRunState(pItem->valueint);
  }

  switch (cmdCell->GetCmdType()) {
    case SCHEDULE_SEND_TASK:
    case ATTEMPT_KILL_SEND_TASK: {
      pItem = cJSON_GetObjectItem(cmd, "runCommond");
      if (pItem) {
        cmdCell->SetRunCmd(pItem->valuestring);
      }
      callBackArray = cJSON_GetObjectItem(cmd, "callbackAddress");

      if (NULL != callBackArray) {
        if (callBackArray->type != cJSON_Array) {
          return ERROR_CODE_MISPATCH;

        }
        int ipNum = cJSON_GetArraySize(cmd);
        for (int iCnt = 0; iCnt < ipNum; iCnt++) {
          cJSON *pSub = callBackArray->child;
          if (NULL == pSub) {
            continue;
          }
          if (NULL != pSub) {
            cmdCell->SetCallbackAddr(iCnt, pSub->valuestring);
          }
        }
      }
      break;
    }
    case SCHEDULE_RECEIVE_RESULT:
    case ATTEMPT_KILL_RECEIVE_RESULT: {
      pItem = cJSON_GetObjectItem(cmd, "node");
      if (pItem) {
        cmdCell->SetNode(pItem->valuestring);
      }
      pItem = cJSON_GetObjectItem(cmd, "ipAndPort");
      if (pItem) {
        cmdCell->SetIpAndPort(pItem->valuestring);
      }
      break;
    }
    case ATTEMPT_RUNNING_TASK: {

      break;
    }

    default: {
      break;
    }
  }
//  LOG_INFO("cmd" << cmdCell->GetCmd()<< ",node" << cmdCell->GetNode()
//                 << ",jobcode" << cmdCell->GetJobCode()<< ",traceId" << cmdCell->GetTraceId());

  cJSON_Delete(cmd);
  return SUCESS_CODE;
}

int CAgentService::_Command2Json(int cmdType, boost::shared_ptr<CCommand> cmd, std::string &cmdJson) {
  cJSON *root;
  char *cmdOut;

  root = cJSON_CreateObject();
  if (!root) {
    return -1;
  } else {

    cJSON_AddItemToObject(root, "type", cJSON_CreateString(m_CCmd.GetTaskByType(cmdType).c_str()));
    cJSON_AddItemToObject(root, "taskName", cJSON_CreateString(cmd->GetTaskName()));
    cJSON_AddItemToObject(root, "jobUniqueCode", cJSON_CreateString(cmd->GetJobCode()));
    cJSON_AddItemToObject(root, "traceId", cJSON_CreateString(cmd->GetTraceId()));
    cJSON_AddItemToObject(root, "runState", cJSON_CreateNumber(cmd->GetRunState()));
    switch (cmdType) {
      case SCHEDULE_RECEIVE_RESULT: {

        cJSON_AddItemToObject(root, "node", cJSON_CreateString(cmd->GetCallbackAddr()));
        cJSON_AddItemToObject(root, "ipAndPort", cJSON_CreateString(GetLocalIpAndPort()));
        break;
      }
      case ATTEMPT_RUNNING_RESULT: {
        cJSON_AddItemToObject(root, "running", cJSON_CreateBool(cmd->GetCurrentRunningState()));
        cJSON_AddItemToObject(root, "ip", cJSON_CreateString(cmd->GetCallbackAddr()));
        break;
      }
      case ATTEMPT_KILL_RECEIVE_RESULT: {

        cJSON_AddItemToObject(root, "node", cJSON_CreateString(cmd->GetCallbackAddr()));
        cJSON_AddItemToObject(root, "ipAndPort", cJSON_CreateString(GetLocalIpAndPort()));
        break;
      }
      default:break;

    }
    cmdOut = cJSON_Print(root);

    cmdJson = cmdOut;
    SAFE_FREE(cmdOut);
    cJSON_Delete(root);
    return SUCESS_CODE;
  }
}
//read, nolock
bool CAgentService::FindCmd(std::string traceId,boost::shared_ptr<CCommand>& findCmd) {

  if (NULL == traceId.c_str()) {
    LOG_INFO("Error the traceId is null");
    return boost::shared_ptr<CCommand>();
  }

	pthread_rwlock_rdlock(&m_CmdRwlock);
  for (int i = 0; i < m_CmdService.size(); i++) {
    if (NULL != m_CmdService[i]->GetTraceId()) {
      if (!strcmp(m_CmdService[i]->GetTraceId(), traceId.c_str())) {
	      findCmd=m_CmdService[i]; 
	 			pthread_rwlock_unlock(&m_CmdRwlock);
	 			return true;
      }
    } else {
      LOG_ERROR("the cmd service traceId is null");
    }
  }
	pthread_rwlock_unlock(&m_CmdRwlock);
  return false;

}
void CAgentService::InPutPCell(boost::shared_ptr<CCommand> cmd) {

  boost::shared_ptr<PCell> pCellTmp = boost::make_shared<PCell>();
  pCellTmp->SetCPid(cmd->m_CPid);
  pCellTmp->SetCPidState((CURRENT_PID_STATE) cmd->GetRunState());
  cmd->m_pCell.push_back(pCellTmp);

}
void CAgentService::FindCurrTraceByCmd(int callBackPid, std::string&  tmpTrace){

  pthread_rwlock_rdlock(&m_CmdRwlock);
	for (int i = 0; i < m_CmdService.size(); i++) {
    if (NULL != m_CmdService[i]->GetTraceId()) {
      if (m_CmdService[i]->GetWaitCmdPid()==callBackPid) {
				tmpTrace=m_CmdService[i]->GetTraceId();
        pthread_rwlock_unlock(&m_CmdRwlock);
				return;
      }
    } else {
      LOG_ERROR("the cmd service traceId is null");
    }
  }
  pthread_rwlock_unlock(&m_CmdRwlock);

	return;

}
const char *CAgentService::FindCurrTrace(int callBackPid) {

  if (callBackPid <= 0) {

    return NULL;
  }
  const char *currTraceID;
  if (m_WaitCmdService.size() <= 0) {
    LOG_INFO("m_WaitCmdService len is null");
    return NULL;
  } else {
    std::vector<PidTrace_Info>::const_iterator cItor;   //进程ID<-->traceID的映射结构
    pthread_rwlock_rdlock(&m_WCmdRwlock);
    for (cItor = m_WaitCmdService.begin(); cItor != m_WaitCmdService.end(); cItor++) {

      if ((*cItor).strTraceId.empty()) {
        continue;
      }
      if ((*cItor).nPid == callBackPid) {

        LOG_INFO("m_WaitCmdService map has id=" << (*cItor).strTraceId.c_str());
	      pthread_rwlock_unlock(&m_WCmdRwlock);
        return (*cItor).strTraceId.c_str();

      }
      if (cItor == m_WaitCmdService.end()) {
        break;
      }

    }
  }
	 pthread_rwlock_unlock(&m_WCmdRwlock);
  return NULL;
}
 /*
 * 如果一个traceID无多个进程对应，此时发送
 * 响应报文后将存放的cmd cell和pid cell进行
 * 释放，不进行相关等待操作
 * 1.相同traceID进行调度时，只给server端回一次
 */

int CAgentService::RunCallBackTask(int callBackPid) {

  if (GetCmdServiceLen() <= 0 || callBackPid <= 0) {
    LOG_INFO("The callback is null or the pid is invalid");
    return -1;
  } else {
		std::string tmpTraceId="";
    FindCurrTraceByCmd(callBackPid,tmpTraceId);
    if (NULL == tmpTraceId.c_str()) {
      return -1;

    } else {
      boost::shared_ptr<CCommand> rCallBack=boost::make_shared<CCommand>();
      bool find = FindCmd(tmpTraceId.c_str(),rCallBack);
      if (find) {
        rCallBack->SetRunState(RUNNING_FINISHING_STATE);
        if (CallBackTask(rCallBack, SCHEDULE_RECEIVE_RESULT) < 0) {
          LOG_INFO("CallBackTask msg failed");
        }
        if (RemoveCmdCell(tmpTraceId.c_str()) > 0) {
          LOG_INFO("RemoveCmdCell sucess,size=" << m_CmdService.size());
        } else {
          LOG_INFO("RemoveCmdCell failed,size=" << m_CmdService.size());
        }
				if (RemoveCmdInMap(tmpTraceId.c_str())>=0){
          LOG_INFO("Remove map cmd sucess,size=" << m_Trace2Cmd.size());
				}else{
          LOG_INFO("Remove map cmd sucess,size=" << m_Trace2Cmd.size());
				}
      } else {
        LOG_INFO("can not find want to sche traceId"<<tmpTraceId.c_str());
        return -1;
      }
      return 0;
    }
  }
}

int CAgentService::RemoveWaitCmdCell(int rmWaitPid, const char *rmTraceId) {

  if (rmWaitPid < 0 || NULL == rmTraceId) {

    LOG_ERROR("The invalid rm m_WaitCmdService condition");
  }
  int rmFlag = -1;
  std::vector<PidTrace_Info>::iterator mItor;
	pthread_rwlock_wrlock(&m_WCmdRwlock);
  for(std::vector<PidTrace_Info>::iterator itor=m_WaitCmdService.begin(); itor!=m_WaitCmdService.end(); ){

    if (!((*itor).strTraceId.empty())) {
		   if (((*itor).nPid == rmWaitPid) && (!strcmp((*itor).strTraceId.c_str(), rmTraceId))) {
         mItor=itor;
			   itor=m_WaitCmdService.erase(mItor);
         rmFlag = 1;
         break;
       }
		}else{
			 itor++;
		}
	}	
	pthread_rwlock_unlock(&m_WCmdRwlock);
	return rmFlag;
}

//需查看当前是否有正在执行的进程，无此时可remove
int CAgentService::RemoveCmdCell(const char *traceId) {
  if (NULL == traceId) {
    LOG_ERROR("traceId is null=" << traceId);
    return -1;
  }

  int rmFlag = -1;
  std::vector<boost::shared_ptr < CCommand> > ::iterator
  cItor;
  pthread_rwlock_wrlock(&m_CmdRwlock);	

  for (cItor = m_CmdService.begin(); cItor != m_CmdService.end();) {
    if (!strcmp((*cItor)->GetTraceId(), traceId)) {
      cItor=m_CmdService.erase(cItor);
      rmFlag = 1;
    } else {
      cItor++;
    }
  }
  pthread_rwlock_unlock(&m_CmdRwlock);	

  return rmFlag;
}
int CAgentService::RemoveCmdCellByFlag(const char *traceId) {

  if (NULL == traceId) {
    LOG_ERROR("traceId is null=" << traceId);
    return -1;
  }
  int rmFlag = -1;
  std::vector<boost::shared_ptr < CCommand> > ::iterator
  cItor;
  pthread_rwlock_wrlock(&m_CmdRwlock);	
  for (cItor = m_CmdService.begin(); cItor != m_CmdService.end();) {
    LOG_INFO("the rm falg"<<(*cItor)->GetCmdRmFlag());
		if (((*cItor)->GetCmdRmFlag())) {
      LOG_INFO("m_CmdService has the rm traceId=" << traceId);
      cItor=m_CmdService.erase(cItor);
      rmFlag = 1;
    } else {
      cItor++;
      LOG_INFO("m_CmdService is forbidden to delete=" << traceId);
    }
  }
  LOG_INFO("after kill rm the cmd of vector size: " << m_CmdService.size());
  pthread_rwlock_unlock(&m_CmdRwlock);	
  return rmFlag;
}

int CAgentService::RemovePidCell(const char *traceID) {
  if (NULL == traceID) {
    LOG_INFO("traceId is null");
    return SUCESESS_RETURN;
  } else {
    return ERROR_RETURN;
  }
}
int CAgentService::CallBackTask(boost::shared_ptr<CCommand> cmd, int type) {

  bool HasCallBackFd = false;
  std::string strJson;

  _Command2Json(type, cmd, strJson);

  if (HasCallBackFd)  //has the callback socketfd
  {
    if (ERROR_RETURN == send(m_fd, strJson.c_str(), strlen(strJson.c_str()) + 1, 0)) {
      LOG_ERROR("fail to send");
    	return ERROR_RETURN;
		}
  } else {
    if (ERROR_RETURN == CallBackMsgSend(cmd, strJson)) {
      LOG_ERROR("fail to CallBackMsgSend");
      return ERROR_RETURN;
    } else {
    // LOG_INFO("sucess to CallBackMsgSend");
    }
  }
  return 1;
}
const char *CAgentService::GetCallBackIp(char *callBackList) {
  if (NULL == callBackList) {
    return NULL;
  }
  const int buffLen = 64;
  char buff[buffLen] = {'0'};

  if (callBackList != NULL) {
    int gap;
    char *pos = strchr(callBackList, ',');
    if (NULL == pos && callBackList != NULL) {

      strncpy(buff, callBackList, strlen(callBackList));
      return (char *) buff;
		}else {
      if (gap > BIT_INT) {
        return NULL;
      }
      gap = pos - callBackList;
      if (gap < 0 || gap > buffLen){
        strncpy(buff, callBackList, gap);
        return (char *) buff;
			}
    }
  }
}
void CAgentService::printfBuffer(unsigned char *nmaMsg, int msgLen) {
  for (int iter = 0; iter < msgLen; iter++) {
    if (iter % 16 == 0) {
      LOG_INFO("" << "\n");
    }
    LOG_INFO("" << *(nmaMsg + iter));
  }
  LOG_INFO("" << "\n");
}

int CAgentService::CallBackMsgSend(boost::shared_ptr<CCommand> cmd, std::string &strJson) {
  if (NULL == strJson.c_str() || NULL == cmd) {
    LOG_ERROR("callbackmsg send para is error");
    return ERROR_RETURN;
  }
  static int send_num=0;
	if(send_num>MAX_INT){
		send_num=0;
	}
  char *serverIP = (char *) cmd->GetCallbackAddr();
  if (NULL == serverIP) {
    LOG_ERROR("callback addr error");
    return ERROR_RETURN;
  }
  const int buffLen=1024;
 	char rcvBuff[buffLen];
  memset(rcvBuff, 0, buffLen);
	char sendBuff[buffLen];
  memset(sendBuff, 0, buffLen);
  int sendToLen=0;
	std::string sendStr="";
  sendToLen = strlen(strJson.c_str());
  sendStr = strJson.c_str();
  char *ptr = rcvBuff;
  SetUInt32(ptr, sendToLen);
  SetChars(ptr, sendStr.c_str(), strlen(sendStr.c_str()));
  //LOG_INFO("callback msg" << sendStr.c_str());
  int sendLen = ToBytes((char *) sendBuff, MAX_SEND_BUFFER,sendStr,sendToLen);

  struct sockaddr_in cServerSin;
  int s_fd, flags = -2, ret = -2;

  bzero(&cServerSin, sizeof(cServerSin));
  cServerSin.sin_family = AF_INET;
  inet_pton(AF_INET, serverIP, (void *) &cServerSin.sin_addr);
  cServerSin.sin_port = htons(CRANE_SERVER_PORT);

  if ((s_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
    LOG_ERROR("fail to create socket");
    return -1;
  }

  if (ret = connect(s_fd, (struct sockaddr *) &cServerSin, sizeof(cServerSin)) < 0) {
    LOG_ERROR("fail to connect socket" << errno);
    PrintConnectState(errno);
    close(s_fd);
  } else {
   }
  if (-1 == send(s_fd, sendBuff, sendLen, 0)) {
    LOG_ERROR("fail to send");
    close(s_fd);
    return -1;
  } else {
    send_num++;
    LOG_INFO("callback msg len=" << sendLen<<",send num"<<send_num);
  }
  if (close(s_fd) == ERROR_RETURN) {
    return ERROR_RETURN;
  }
  return SUCESESS_RETURN;
}
void CAgentService::PrintConnectState(int erron) {

  switch (erron) {
    case EISCONN: {
      LOG_INFO("connect sucess");
      break;
    }
    case EALREADY: {
      LOG_INFO("connect error");
      break;
    }
    case EINPROGRESS: {
      LOG_INFO("connect is doning");
      break;
    }
    default: {
      LOG_INFO("connect fialed");
      break;
    }
  }
}
int CAgentService::ScheduleSendTask(boost::shared_ptr<CCommand> cmd) {
  int rResult = -1;
  rResult = ExecuteScheduleTheScript(cmd);

  switch (rResult) {

    case UNFINISHED_STATE: { //任务未执行完，此时不用回复

      InPutPCell(cmd);
      cmd->SetPidNumPlus();
      break;
    }
    case FINISHED_STATE: { //执行完成，发送响应报文
      cmd->SetRunResult(FINISH_SCH);
      CallBackTask(cmd, SCHEDULE_RECEIVE_RESULT);
      break;

    }
    case FAILED_STATE: { //执行失败，发送响应报文
      cmd->SetRunResult(FAILED_SCH);   //执行失败
      CallBackTask(cmd, SCHEDULE_RECEIVE_RESULT);
      break;
		}
		default:{
      break;
   		}
    }
  return SUCESESS_RETURN;
}
int CAgentService::AttemptKillSendTask(boost::shared_ptr<CCommand> cmd) {
  if (NULL == cmd) {
    return ERROR_RETURN;
  }
  int rResult = -1;

  rResult = ExecuteKillTheScript(cmd);
  LOG_INFO("ExecuteKillTheScript result:" << rResult);

  if (rResult == SUCESS_KILL_TASK)   //1 bitmask sucess
  {
    InPutPCell(cmd);
    cmd->SetRunResult(SUCESS_KILL_TASK);
    cmd->SetRunState(SUCESS_KILL_TASK);
    cmd->SetPidNumPlus();
  } else {
    cmd->SetRunResult(ERROR_KILL_TASK);
    cmd->SetRunState(ERROR_KILL_TASK);
  }
  if (CallBackTask(cmd, ATTEMPT_KILL_RECEIVE_RESULT) < 0) {

    LOG_ERROR("CallBackTask send failed");

    return ERROR_RETURN;
  }
  return SUCESESS_RETURN;
}
int CAgentService::RunCmdTask(const char *inData, boost::shared_ptr<CCommand> cmd) {
  if (NULL == cmd) {
    return ERROR_RETURN;
  }
  switch (cmd->GetCmdType()) {
    case SCHEDULE_SEND_TASK: {
      return ScheduleSendTask(cmd);
    }
    case ATTEMPT_KILL_SEND_TASK: {
      return AttemptKillSendTask(cmd);
    }
    case ATTEMPT_RUNNING_TASK: {
      break;
    }
    default: {
      return ERROR_RETURN;
    }
  }
  return SUCESESS_RETURN;
}
int CAgentService::AttemptRunningTask(const char *inData, boost::shared_ptr<CCommand> cmd) {

  if (NULL == cmd) {
    LOG_ERROR("AttemptRunningTask Error");
    return ERROR_RETURN;
  }
  cmd->SetRunState(false);

  if (CallBackTask(cmd, ATTEMPT_RUNNING_RESULT) < 0) {
    LOG_ERROR("AttemptRunningTask::CallBackTask failed Error");
  }
  return ERROR_RETURN;
}

int CAgentService::RunNewTask(const char *inData, boost::shared_ptr<CCommand> cmdCell) {

  struct timeval sTime;
  gettimeofday(&sTime, NULL);
  LOG_INFO("before enter the cmd enqueue time: " << sTime.tv_sec);
  cmdCell->SetInputTime(sTime.tv_sec);
  RunCmdTask(inData, cmdCell);

  return SUCESESS_RETURN;

}
int CAgentService::UpdateSendTask(const char *netCmd, boost::shared_ptr<CCommand> cmdCell) {

  if (NULL == netCmd){
	  	return ERROR_RETURN;
	}

  cJSON *cmd = cJSON_Parse(netCmd);

  if (NULL == cmd) {
    return ERROR_RETURN;
  }
  std::string tmpCmd;
  cJSON *pItem = cJSON_GetObjectItem(cmd, "runCommond");

  if (NULL != pItem) {
    tmpCmd = pItem->valuestring;
    if (!strcmp(tmpCmd.c_str(), cmdCell->GetCmd())) {
      LOG_INFO("It's the repeatedly cmd,disgards");
      return SUCESESS_RETURN;
    }
  } else {
    LOG_INFO("Build the process in the same traceId");
    ScheduleSendTask(cmdCell);
    return SUCESESS_RETURN;
  }
  cJSON_Delete(cmd);
  cmd = NULL;
  return SUCESESS_RETURN;
}
int CAgentService::KillExistTask(boost::shared_ptr<CCommand> cmdCell) {

  int kResult = KillChild(cmdCell->GetTraceId());
  cmdCell->SetRunState(kResult);

  if (CallBackTask(cmdCell, ATTEMPT_KILL_RECEIVE_RESULT) < 0) {
    LOG_ERROR(" KillExistTask::CallBackTask msg error");
    return ERROR_RETURN;
  }
  return 1;

}
int CAgentService::RunningTask(boost::shared_ptr<CCommand> cmdCell) {

  if (NULL == cmdCell) return ERROR_RETURN;

  if (CallBackTask(cmdCell, ATTEMPT_RUNNING_RESULT) < 0) {
    LOG_ERROR("CallBackTask msg error");

  }
  return 1;

}
int CAgentService::RunExistTask(const char *inData, boost::shared_ptr<CCommand> cmdCell) {

  LOG_INFO("==RunExistTask==")
  SetCommandValue(inData, cmdCell);
  switch (cmdCell->GetCmdType()) {
    case SCHEDULE_SEND_TASK: {
      return UpdateSendTask(inData, cmdCell);
    }
    case ATTEMPT_KILL_SEND_TASK: {
      return KillExistTask(cmdCell);
    }
    case ATTEMPT_RUNNING_RESULT: {
      return RunningTask(cmdCell);
    }
    default: {
      return INVALID_RUN_CMD;
    }
      return SUCESESS_RETURN;
  }
}
void CAgentService::SetIpAndPort(const char *localIp) {

  if (NULL != localIp) {
    m_LocalAndPort = localIp;
    m_LocalAndPort.append(":");
    m_LocalAndPort.append("5267");
  } else {
    LOG_ERROR("==local ip string is null==");
  }
  return;
}
rt_code_type CAgentService::CServiceOnMessage(int fd,char *data) {

  if (NULL == data) {
    LOG_ERROR("==CServiceOnMessage msg is null==");
    return ERROR_CODE_NULL;
  }
	static int rcv_num=0;
	if(rcv_num>MAX_INT){
		rcv_num=0;
	}
	LOG_INFO("receive the crane msg num: "<<rcv_num++);

  m_fd = fd;
  LOG_INFO("crane msg \r\n"<<data);
  SetIpAndPort(m_LocalIp);

  if (NULL != data) {
    LOG_INFO("==m_CmdService size==" << m_CmdService.size())
    if ((!m_CmdService.empty())) {        //根据到来的traceId在服务队列中查找
      std::string tmpTraceID;
      GetTmpTraceId(data, tmpTraceID);
      boost::shared_ptr<CCommand> cmdCell=boost::make_shared<CCommand>();
      bool find = FindCmd(tmpTraceID.c_str(),cmdCell);
      if (find) {
        LOG_INFO("==m_ServiceQueue has the traceID" << tmpTraceID.c_str());
        RunExistTask(data, cmdCell);

      } else {
        LOG_INFO("==m_ServiceQueue has not the traceID,build new cell" << tmpTraceID.c_str());
        boost::shared_ptr<CCommand> cmdCell = boost::make_shared<CCommand>();
        SetCommandValue(data, cmdCell);
        RunNewTask(data, cmdCell);
      }
    } else //服务队列为空，按照新的流程处理
    {
      boost::shared_ptr<CCommand> cmdCell = boost::make_shared<CCommand>();
      SetCommandValue(data, cmdCell);
      RunNewTask(data, cmdCell);
    }
  } else {
    LOG_ERROR("==cmd body value is empty==");
    return ERROR_CODE_NULL;
  }
}

void CAgentService::CraneSignalHandler(int signo) {

  LOG_INFO("==signo" << signo);
  switch(signo){
		case 3:{
      LOG_INFO("kill -9"<<signo);
         break;
		}
		case 9:{
      LOG_INFO("kill -9"<<signo);
         break;
		}
		case 11:{
      LOG_INFO("signal number"<<signo);
         break;
		}
		case 15:{
      LOG_INFO("signal number"<<signo);
         break;
		}
		default:{
      LOG_INFO("signal number"<<signo);
		}
	}
  return;
}
int CAgentService::RunCmd(boost::shared_ptr<CCommand> cmd){

    pid_t pid;
    int status;
    const char* cmdString=cmd->GetCmd();

		LOG_INFO("the runcmd string "<<cmdString);
    if (cmdString == NULL) {
      return -1;
    }
    if ((pid = fork()) < 0) {
      status = -1;
    } else if (pid == 0) {
      execl("/bin/sh", "sh", "-c", cmdString, (char *) 0);
      _exit(127); //子进程正常执行则不会执行此语句
    } else {

      PidTrace_Info pidTraceInfo;
      cmd->m_CPid=pid;
      pidTraceInfo.nPid = cmd->m_CPid;
      pidTraceInfo.strTraceId.assign(cmd->GetTraceId());
      cmd->SetWaitCmdInfo(pidTraceInfo);
			pthread_rwlock_wrlock(&m_CmdRwlock);	
      InPutPCell(cmd);
      m_CmdService.push_back(cmd);
			pthread_rwlock_unlock(&m_CmdRwlock);	

			while (waitpid(pid, &status, 0) < 0) {
        if (errno != 4) {
					LOG_ERROR("wait child failed"<<pid<<"cmdString"<<cmdString);
          status = -1;
          break;
        }
      }
		LOG_INFO("the runcmd pid: "<<pid);
		RunCallBackTask(pid);
    }
    return status;
}
void* CAgentService::RunHandler(void* args){
		
   	pthread_detach(pthread_self());
		
		 if(NULL==args){
				return NULL;
		 }
		 std::string tmpTrace=(char*)args;
		 CAgentService* curService=CAgentService::GetCSInstance();
     if(NULL==curService){
				return NULL;
		 }else{
        boost::shared_ptr<CCommand> tmpCmd=boost::make_shared<CCommand>();
			 curService->FindCmdInMap((char*)tmpTrace.c_str(),tmpCmd);
			 if(curService->RunCmd(tmpCmd)<0){
		    LOG_INFO("run cmd failed");
		   }			
		}
		 return NULL;
}
void CAgentService::BindSignalHandler(){

    signal(SIGCHLD, CraneSignalHandler);
    signal(SIGINT, CraneSignalHandler);
    signal(SIGQUIT, CraneSignalHandler);
    signal(SIGSEGV, CraneSignalHandler);

		return;
}
/*
 *1.大于最大服务队列长度，小于最大排队队列长度，此时排队
 *2.在配置中限制最大的排队队长和线程数
 *3.超过处理能力按照失败处理，支持重发和kill销毁 
 * */
int CAgentService::WrapperRunCmd(boost::shared_ptr<CCommand> cmd, const char *cmdString) {

  if (cmdString == NULL || CmdSafetyCheck(cmdString)) {
    return ERROR_RETURN;
  }
  if(NULL==cmd){
    return ERROR_RETURN;
	}

	if (m_CmdService.size() > GetMaxTaskNum()) {
     if(cmd->GetCmdType()==SCHEDULE_SEND_TASK){
   		cmd->SetRunState(RUNNING_FAILED_STATE); 
	    CallBackTask(cmd, SCHEDULE_RECEIVE_RESULT);
			LOG_INFO("cmd queue full,schedule callback");
    } else {
      LOG_ERROR("error schedule msg");
    }
  } else {
    SetTraceAndCmd((char*)cmd->GetTraceId(),cmd); 
	 	pthread_t tid;
	  int ret=pthread_create(&tid, NULL, CAgentService::RunHandler,(void*)cmd->GetTraceId());
    LOG_INFO("pthread is finished"); 
	}
}

int CAgentService::KillChild(const char *traceId) {
  if (NULL == traceId) {
    return ERROR_RETURN;
  }
  bool findFlag = false;
  std::list<boost::shared_ptr < PCell> > ::iterator
  itor;
  int retval, status, killResult = ERROR_KILL_TASK;
  pthread_rwlock_rdlock(&m_CmdRwlock);
 	for (int i = 0; i < m_CmdService.size(); i++) {
    if (NULL == m_CmdService[i]->GetTraceId()) {
      continue;
    }
		if (!strcmp(m_CmdService[i]->GetTraceId(), traceId)) {
      m_CmdService[i]->SetKilledFlag(true);
      findFlag = true;
      std::list<boost::shared_ptr < PCell> > cellTemp = m_CmdService[i]->GetCurrentPcell();
      itor = cellTemp.begin();
      while (itor != cellTemp.end()) {
				LOG_INFO("want to kill pid"<<(*itor)->GetCPid());
        retval = kill((*itor)->GetCPid(), SIGKILL); //在kill之前应该检查是否是主进程
        if (!retval) {
          LOG_INFO("==kill sucess pid=" << (*itor)->GetCPid() << "==");
          m_CmdService[i]->SetCmdRmFlag(true);
          killResult = SUCESS_KILL_TASK;
       
			 	} else {
          killResult = SUCESS_KILL_TASK; //crane do
					waitpid((*itor)->GetCPid(), &status,0);
          LOG_ERROR("kill falied: " << (*itor)->GetCPid()<<"delete the /proc/pid"); //unsafty todo
        }
        itor++;
      }

    } else {
      continue;
    }
  }
  pthread_rwlock_unlock(&m_CmdRwlock);
  RemoveCmdCellByFlag(traceId);
	RemoveCmdInMap(traceId);
  return killResult;
}

int CAgentService::CServiceMonitor() {

  struct timeval checkTime;
  memset(&checkTime, 0, sizeof(timeval));
  gettimeofday(&checkTime, 0);
	int rmFlag=-1;
  LOG_INFO("the helthy check time is " << (checkTime.tv_sec) << "cmdservice length: " << m_CmdService.size());
  int aliveTime=GetMaxAliveTime();
  if (m_CmdService.size() <= 0) {
    LOG_INFO("CServiceMonitor,nothing to check");
    return -1;
  } else {
    LOG_INFO("CServiceMonitor,the cmd length is,time " << m_CmdService.size()<<"time: "<<aliveTime);
    int rmFlag = -1;
    std::vector<boost::shared_ptr < CCommand> > ::iterator
    cItor;
    pthread_rwlock_wrlock(&m_CmdRwlock);
    for (cItor = m_CmdService.begin(); cItor != m_CmdService.end();) {
      LOG_INFO("the canche time "<<checkTime.tv_sec - (*cItor)->GetInputTime());
			if ((checkTime.tv_sec - (*cItor)->GetInputTime()) >=aliveTime) {
        LOG_INFO("m_CmdService has dead cmd,rm length is " << m_CmdService.size());
				if((*cItor)->GetCmdType()==SCHEDULE_SEND_TASK){
					LOG_INFO("monitor to remove the cmd");
					(*cItor)->SetRunState(RUNNING_FAILED_STATE);
					CallBackTask((*cItor), SCHEDULE_RECEIVE_RESULT);
					LOG_INFO("cmd queue has dead cmd,schedule callback");
				} else if((*cItor)->GetCmdType()==ATTEMPT_KILL_SEND_TASK){
					(*cItor)->SetRunState(ERROR_KILL_TASK);
					CallBackTask((*cItor), ATTEMPT_KILL_RECEIVE_RESULT);
					LOG_INFO("error crane msg");
				   }
				cItor=m_CmdService.erase(cItor);
				LOG_INFO("m_CmdService has dead cmd,after rm length is " << m_CmdService.size());
				rmFlag = 1;
         }else{
          cItor++;
          LOG_INFO("m_CmdService has not dead cmd");
				 }
        }
    }
		pthread_rwlock_unlock(&m_CmdRwlock);
    return rmFlag;
  }

int CAgentService::RemoveCmdInMap(const char* traceId){

  if(NULL==traceId){
		return -1;
	}
	int rmFlag=-1;
	std::map<std::string,boost::shared_ptr<CCommand> > ::iterator mItor ;
  pthread_rwlock_wrlock(&m_MCmdRwlock);  
	for (mItor = m_Trace2Cmd.begin(); mItor != m_Trace2Cmd.end();){
		if (!strcmp(mItor->first.c_str(), traceId))
		{    
			rmFlag++;
			m_Trace2Cmd.erase(mItor++) ; // erase之后，令当前迭代器指向其后续 
		}    
		else 
		{    
			++mItor;
		}    
	}
	LOG_INFO("remove map after "<<m_Trace2Cmd.size());
	pthread_rwlock_unlock(&m_MCmdRwlock);  
	return rmFlag;
}

int CAgentService::WrapperKillChild(const char *traceId) {

  if (NULL == traceId) return ERROR_KILL_TASK;

  return KillChild(traceId);

}
//to get the run result:include 3 bitmasks
int CAgentService::ExecuteScheduleTheScript(boost::shared_ptr<CCommand> cmd) {

  if (NULL == cmd) {
    return ERROR_RETURN;
  }
  std::string cmdTmp = cmd->GetCmd();
  int result;
  result = WrapperRunCmd(cmd, cmdTmp.c_str());
  LOG_INFO("run the schedule script result" << result);

  return result;
}

int CAgentService::ExecuteKillTheScript(boost::shared_ptr<CCommand> cmd) {
  if (NULL == cmd) {
    return ERROR_RETURN;
  }
  int flag = -1;

  if (cmd->GetPidNum() > 0) {
    pid_t status;
    long int cmdPid;
    std::string cmdTmp = cmd->GetCmd();

    return WrapperKillChild(cmd->GetTraceId());

  }
}
//TODO:move to the tools
inline void CAgentService::SetChars(char *&pDes, const char *pSour, int nSetNum) {

  if (NULL == *pSour || nSetNum == 0 || nSetNum >= 1024) {
    LOG_INFO("the pSouur is null,or nSetNum is less 0");
    return;
  }
  memcpy(pDes, pSour, nSetNum);
  pDes += nSetNum;
}

inline void CAgentService::SetUInt32(char *&pchIODes, int u32In) {
  u32In = htonl(u32In);
  memcpy(pchIODes, &u32In, 4);
  pchIODes += 4;
}
int CAgentService::ToBytes(char *pBuffer, const int bufLen,std::string& jsonData,int sendLen) {
  char *ptr = pBuffer;

  if (NULL == jsonData.c_str() || bufLen <= 0) {
    LOG_INFO("the json data is null or bufLen is less 0");
    return 0;
  }
  SetUInt32(ptr, sendLen);
  SetChars(ptr, jsonData.c_str(), strlen(jsonData.c_str()));
  if (ptr - pBuffer > bufLen) {
    LOG_INFO("To bytes error overflow" << ptr - pBuffer);
    return -1;
  }
  return ptr - pBuffer;
}
inline void CAgentService::GetString(char *&pchInSour, char *pOut, int nNum) {
  if (NULL == *&pchInSour || nNum <= 0 || nNum >= 1024) {
    LOG_INFO("the pSour is null or nNum is less 0");
    return;
  }
  memcpy(pOut, pchInSour, nNum);
  pchInSour += nNum;
}

inline int CAgentService::GetUInt32(char *&pchInSour) {
  int nRtn;
  memcpy(&nRtn, pchInSour, 4);
  pchInSour += 4;
  nRtn = ntohl(nRtn);
  return nRtn;
}

