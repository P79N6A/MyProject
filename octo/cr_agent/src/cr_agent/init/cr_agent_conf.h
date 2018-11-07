//
// Created by smartlife on 2017/8/7.
//

#ifndef DEBUG_HOME_CR_AGENT_CONF_H
#define DEBUG_HOME_CR_AGENT_CONF_H
#include "../../comm/log4cplus.h"
#include "cr_agent_def.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <sstream> 
#include "../../comm/tinyxml2.h"
#include "../../comm/log4cplus.h"
class CraneAgentConf{

 public:
  CraneAgentConf();
  ~CraneAgentConf(){};

	int LoadConf();
	inline int GetListenPort(){return m_AgentListenPort;};
	inline int GetPeerServerPort(){return m_PeerServerPort;};
	inline int GetRetryNum(){return m_RetryNum;};
	inline int GetRetryTime(){return m_RetryTime;};
	inline int GetSchTime(){ return  m_SchTime;};
	inline int GetMaxTaskNum(){return m_MaxTaskNum;};
	inline int GetMaxAliveTime(){return m_MaxAliveTime;}
	inline int GetMaxThreadNum(){return m_MaxThreadNum;};
	std::string GetLogConfigPath(){return m_LogConfigPath;};
	std::string GetAgentConfigPath(){return m_AgentConfigPath;};
 private:
  bool IsNum(std::string str);
	int m_AgentListenPort;
	int m_PeerServerPort;
	int m_RetryNum;
	int m_RetryTime;
	int m_SchTime;
	int m_MaxTaskNum;
	int m_MaxThreadNum;
	int m_MaxAliveTime;
	std::string m_LogConfigPath;
	std::string m_AgentConfigPath;

};


#endif //DEBUG_HOME_CR_AGENT_CONF_H
