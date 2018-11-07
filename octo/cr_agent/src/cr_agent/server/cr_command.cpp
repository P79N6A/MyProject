//
// Created by smartlife on 2017/8/7.
//
#include "cr_command.h"
#include "../../util/cr_common.h"

CCommand::CCommand():m_taskName(""),m_jobUniqueCode(""),m_traceId(""),m_Node(""),
                     m_IpAndPort(""),m_Command(""),m_cmdRmFlag(false),m_cmdType(-1),
                     m_callBackAddr(-1),m_runState(-1),m_CPidNum(0),m_RunResult(-1){

  m_cmdToString.insert(std::make_pair(SCHEDULE_SEND_TASK, "ScheduleSendTask"));
  m_cmdToString.insert(std::make_pair(SCHEDULE_RECEIVE_RESULT, "ScheduleReceiveResult"));
  m_cmdToString.insert(std::make_pair(ATTEMPT_KILL_SEND_TASK, "AttemptKillSendTask"));
  m_cmdToString.insert(std::make_pair(ATTEMPT_KILL_RECEIVE_RESULT, "AttemptKillReceiveResult"));
  m_cmdToString.insert(std::make_pair(ATTEMPT_RUNNING_TASK, "AttemptRunningTask"));
  m_cmdToString.insert(std::make_pair(ATTEMPT_RUNNING_RESULT, "AttemptRunningResult"));
  m_CPid = 0;

}
void CCommand::SetCallbackAddr(int index, char *inString) {

  if (index > MAX_IP_NUM - 1) return;

  snprintf(m_callBackAddrStr[index], MAX_IP_LEN, "%s", inString);

}
void CCommand::SetWaitCmdInfo(PidTrace_Info& pidInfo){

  m_WaitPidInfo.nPid=pidInfo.nPid;
  m_WaitPidInfo.strTraceId.assign(pidInfo.strTraceId);
}
const char* CCommand::GetWaitCmdTraceId(){

  return m_WaitPidInfo.strTraceId.c_str();

}
int CCommand::GetWaitCmdPid(){

  return m_WaitPidInfo.nPid;

}
void CCommand::SetCmdType(char *inString) {
  if (NULL == inString) return;

  if (!strcmp(inString, "ScheduleSendTask")) {
    m_cmdType = SCHEDULE_SEND_TASK;
  } else if (!strcmp(inString, "ScheduleReceiveResult")) {
    m_cmdType = SCHEDULE_RECEIVE_RESULT;
  } else if (!strcmp(inString, "AttemptKillSendTask")) {
    m_cmdType = ATTEMPT_KILL_SEND_TASK;
  } else if (!strcmp(inString, "AttemptKillReceiveResult")) {
    m_cmdType = ATTEMPT_KILL_RECEIVE_RESULT;
  } else if (!strcmp(inString, "AttemptRunningSendTask ")) {
    m_cmdType = ATTEMPT_RUNNING_TASK;
  } else if (!strcmp(inString, "AttemptRunningResult")) {
    m_cmdType = ATTEMPT_RUNNING_RESULT;
  } else {
    m_cmdType = INVLID_CMD_TYPE;
  }
}

bool CCommand::GetCurrentRunningState() {
  if (m_runState == RUNNING_FINISHING_STATE || m_runState == RUNNING_PID_STATE) {
    return true;
  }
  return false;
}
