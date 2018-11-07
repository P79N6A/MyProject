//
// Created by smartlife on 2017/8/7.
//
#include "cr_agent_conf.h"

/*
 * 加载配置文件
 * 1.端口
 * 2.超时时间
 * 3.重试次数
 * */

CraneAgentConf::CraneAgentConf():m_AgentListenPort(0),m_PeerServerPort(0),
m_RetryNum(0),m_RetryTime(0),m_SchTime(0),m_MaxTaskNum(0),m_MaxThreadNum(0),m_MaxAliveTime(),m_LogConfigPath(""),m_AgentConfigPath(""){

}

bool CraneAgentConf::IsNum(std::string str){
	if(NULL==str.c_str()){
		return false;
	}
	std::stringstream sin(str);  
  double inDigit;;  
	char c;  
	if(!(sin >> inDigit)) {
		 return false;
     }
	if (sin >> c) {
		 return false;
		}  
return true;  
}
int CraneAgentConf::LoadConf() {

     tinyxml2::XMLDocument crAgent;
     tinyxml2::XMLError crAgentUpload = crAgent.LoadFile(CR_AGENT_CONF.c_str());

    if (tinyxml2::XML_SUCCESS != crAgentUpload) {
      LOG_ERROR("agent can't load " << CR_AGENT_CONF);
      return -1;
    }

    tinyxml2::XMLElement *agentConf = crAgent.FirstChildElement("CRAgentConf");
    if (NULL == agentConf) {
      LOG_ERROR("can't find CRAgentConf in " << CR_AGENT_CONF);
      return -1;
    }
  //读取监听端口
    const char* clientPort = agentConf->FirstChildElement("ClientPort")->GetText();

    if(NULL!=clientPort){
      if(!IsNum(clientPort)){
				return -1;
			}
      m_AgentListenPort = atoi(clientPort);
      LOG_INFO("crane_agent listen port " << m_AgentListenPort);
    }else{
      LOG_ERROR("cr can't find listen port " << CR_AGENT_CONF);
    }
    const char* peerServerPort=agentConf->FirstChildElement("CraneServerPort")->GetText();

    if(NULL!=peerServerPort){
      if(!IsNum(clientPort)){
				return -1;
			}
      m_PeerServerPort = atoi(peerServerPort);
      LOG_INFO("crane_agent peerServer port " << m_PeerServerPort);
    }else{
      LOG_ERROR("cr can't find peerServer port" << CR_AGENT_CONF);
    }

    const char* timeout=agentConf-> FirstChildElement("Timeout")->GetText();

    if(NULL!=timeout){
      if(!IsNum(timeout)){
				return -1;
			}
      m_RetryTime = atoi(timeout);
      LOG_INFO("crane_agent RetryTime " << m_RetryTime);
    }else{
      LOG_ERROR("cr can't find m_RetruTime" << CR_AGENT_CONF);
    }

    const char* retryNum=agentConf-> FirstChildElement("Retry")->GetText();

    if(NULL!=retryNum){
      if(!IsNum(retryNum)){
				return -1;
			}
      m_RetryNum = atoi(retryNum);
      LOG_INFO("crane_agent RetryNum " << m_RetryNum);
    }else{
      LOG_ERROR("cr can't find RetryNum" << CR_AGENT_CONF);
    }
    const char* schTimeout=agentConf-> FirstChildElement("MonitorTimeOut")->GetText();

    if(NULL!=schTimeout){
      if(!IsNum(schTimeout)){
				return -1;
			}
      m_SchTime = atoi(schTimeout);
      LOG_INFO("crane_agent Schedule time(seconds) " << m_SchTime);
    }else{
      LOG_ERROR("cr can't find RetryNum" << CR_AGENT_CONF);
    }
    const char* maxTaskNum=agentConf-> FirstChildElement("MaxTaskNum")->GetText();

    if(NULL!=maxTaskNum){
      if(!IsNum(maxTaskNum)){
				return -1;
			}
      m_MaxTaskNum = atoi(maxTaskNum);
      LOG_INFO("crane_agent maxTasknum " << m_MaxTaskNum);
    }else{
      LOG_ERROR("cr can't find MaxTaskNum" << CR_AGENT_CONF);
    }

    const char* aliveTime=agentConf-> FirstChildElement("MaxAliveTime")->GetText();

    if(NULL!=aliveTime){
      if(!IsNum(aliveTime)){
				return -1;
			}
      m_MaxAliveTime = atoi(aliveTime);
      LOG_INFO("crane_agent alive time(seconds) " << m_MaxAliveTime);
    }else{
      LOG_ERROR("cr can't find MaxAliveTime" << CR_AGENT_CONF);
    }
    const char* maxThread=agentConf-> FirstChildElement("MaxThreadNum")->GetText();
    if(NULL!=maxThread){

      if(!IsNum(maxThread)){
				return -1;
			}
      m_MaxThreadNum = atoi(maxThread);
      LOG_INFO("crane_agent maxThread " << m_MaxThreadNum);
    }else{
      LOG_ERROR("cr can't find MaxthreadNum" << CR_AGENT_CONF);
    }
    return 0;
}

