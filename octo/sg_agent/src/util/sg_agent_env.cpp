#include <regex.h>
//#include <string>
#include <fstream>
#include "sg_agent_env.h"
#include "sg_agent_def.h"
#include "comm/tinyxml2.h"
#include "comm/inc_comm.h"
#include "global_def.h"
#include "boost/algorithm/string.hpp"

using namespace std;

extern GlobalVar *g_global_var;

namespace sg_agent {
bool SGAgentEnv::checkEnv(const string &env) {
  for (int i = 0; i < ENVNAME_LEN; ++i) {
    if (env == ENVNAME[i]) {
      return true;
    }
  }
  return false;
}

bool SGAgentEnv::switchEnv(const string &env,
                           const int currentEnv,
                           const string &verifyCode) {

  LOG_INFO("RUN switchEnv. current enviroment: " << currentEnv
                                                 << ", change env: " << env
                                                 << ", verifyCode: "
                                                 << verifyCode);

  if ("agent.octo.sankuai.com" != verifyCode) {
    LOG_ERROR("the verifyCode is error.");
    return false;
  }

  if (!checkEnv(env)) {
    LOG_ERROR("invalid new env = " << env);
    return false;
  }

  if (0 == currentEnv) {
    LOG_ERROR("the currentEnv is undefined, "
                  << "currentEvn = " << currentEnv);
    return false;
  }
  string old_env = ENVNAME[currentEnv - 1];
  if (old_env == env) {
    return true;
  }

  tinyxml2::XMLDocument agent_xml;

  tinyxml2::XMLError operateRet = agent_xml.LoadFile(SG_AGENT_CONF.c_str());
  if (tinyxml2::XML_NO_ERROR != operateRet) {
    LOG_ERROR("tinyxml2 return error code: "
                  << operateRet
                  << ", can't load the sg_agent_env.xml of sg_agent.");
    return false;
  }

  tinyxml2::XMLElement *agent_mns_path
      = agent_xml.FirstChildElement("SGAgentConf")
          ->FirstChildElement("MnsPath");

  if (NULL == agent_mns_path) {
    LOG_ERROR("can't find MnsPath in sg_agent_env.xml of sg_agent.");
    return false;
  }

  string new_path = "/mns/sankuai/" + env;
  agent_mns_path->SetText(new_path.c_str());

  operateRet = agent_xml.SaveFile(SG_AGENT_CONF.c_str());
  if (tinyxml2::XML_NO_ERROR != operateRet) {
    LOG_ERROR("tinyxml2 return error code: "
                  << operateRet
                  << ", can't save the sg_agent_env.xml of sg_agent.");
    return false;
  }
  exit(0);
}

int SGAgentEnv::InitAppenv() {
  string deployenv_str;
  string env_str;
  std::ifstream appenv_fin;

  try {
	appenv_fin.open(sg_agent::APPENV_FILE.c_str(), std::ios::in);
	if (!appenv_fin.is_open()) {
	  LOG_ERROR("failed to init gAppenv, there is not" << sg_agent::APPENV_FILE);
	  return ERR_INVALID_ENV;
	} else {
	  std::string buffer_str;
	  while (std::getline(appenv_fin,buffer_str) && (env_str.empty() || deployenv_str.empty())) {
		std::size_t pos = buffer_str.find_first_of("=");
		if (std::string::npos != pos) {
		  std::string key = buffer_str.substr(0, pos);
		  std::string value = buffer_str.substr(pos + 1);
		  boost::trim(key);
		  boost::trim(value);
		  if ("env" == key) {
			env_str = value;
			LOG_INFO("parsing env: " << buffer_str);
		  } else if ("deployenv" == key) {
			deployenv_str = value;
			LOG_INFO("parsing deployenv: " << buffer_str);
		  }
		}
		buffer_str.clear();
	  }
	}
	appenv_fin.close();
  } catch (std::exception &e) {
	appenv_fin.close();
    LOG_ERROR("fail to load " << sg_agent::APPENV_FILE
                              << "OR fetch deployenv/appenv failed, reason: " << e.what());
    return ERR_INVALID_ENV;
  }

  transform(deployenv_str.begin(), deployenv_str.end(), deployenv_str.begin(), ::tolower);
  transform(env_str.begin(), env_str.end(), env_str.begin(), ::tolower);

  LOG_INFO("get env = " << env_str << 
					 ", deployenv = " << deployenv_str << 
					 " from " << sg_agent::APPENV_FILE);

  return g_global_var->InitEnv(env_str, deployenv_str);
}

/**
 * 初始化环境, 以appenv为准；
 * 如果能正确解释appenv文件时:
 * 1:若sg_agent_env.xml不存在并且非线上的test机器，则生成。
 * 2:若发现sg_agent_env.xml的环境跟appenv不符，则纠正。
 **/
int SGAgentEnv::InitEnv() {
  int ret = InitAppenv(); //official env file
  if (unlikely(SUCCESS != ret)) {
    LOG_ERROR("fail to init gAppenv, ret = " << ret);
    return ret;
  }

  // 对于sg_agent_env.xml存在进行判断， 已有则使用其中的Env
  const int sg_agent_env_int = getEnvFromSgAgentEnv();

  if (sg_agent_env_int != g_global_var->gEnv) {
    LOG_INFO("there is a conflict between "
                 << sg_agent::SG_AGENT_CONF << " and "
                 << sg_agent::APPENV_FILE
                 << "; now re-generate " << sg_agent::SG_AGENT_CONF
                 << " based on " << sg_agent::APPENV_FILE);
    string mns_path = "/mns/sankuai/" + g_global_var->gEnvStr;
    ret = GenSgAgentEnv(mns_path);
    if (SUCCESS != ret) {
      LOG_ERROR("fail to re-generate " << mns_path << ", ret = " << ret);
    }
  }
  return SUCCESS;
}

int SGAgentEnv::getEnvFromSgAgentEnv() {
  tinyxml2::XMLDocument sg_agent_env_xml;
  tinyxml2::XMLError sg_agent_env_xml_ret = sg_agent_env_xml.LoadFile(SG_AGENT_CONF.c_str());
  if (tinyxml2::XML_SUCCESS != sg_agent_env_xml_ret) {
    LOG_ERROR("fail to load " << sg_agent::SG_AGENT_CONF);
    return FAILURE;
  } else {
    const char *env_ch = sg_agent_env_xml.FirstChildElement("SGAgentConf")
        ->FirstChildElement("MnsPath")->GetText();
    string env_path(env_ch);
    LOG_INFO("the env of " << sg_agent::SG_AGENT_CONF << " is " << env_path);
    return getEnvFromPath(env_path);
  }
}

int SGAgentEnv::getEnvFromPath(const string &path) {
  if (strstr(path.c_str(), "prod") != NULL) {
    return 3;
  } else if (strstr(path.c_str(), "stage") != NULL) {
    return 2;
  } else if (strstr(path.c_str(), "test") != NULL) {
    return 1;
  }
//  (g_global_var->gEnvStr).assign(sg_agent::ENVNAME[g_global_var->gEnv - 1]);
  return ERR_INVALID_ENV;
}

int SGAgentEnv::GenSgAgentEnv(string mnsPath) {
  // create new sg_agent_env.xml
  tinyxml2::XMLDocument agentXml;
  tinyxml2::XMLNode *sgAgentConf = agentXml.NewElement("SGAgentConf");
  agentXml.InsertFirstChild(sgAgentConf);

  tinyxml2::XMLElement *ele = agentXml.NewElement("MnsPath");
  ele->SetText(mnsPath.c_str());
  sgAgentConf->InsertEndChild(ele);

  tinyxml2::XMLError eResult = agentXml.SaveFile(SG_AGENT_CONF.c_str());
  if (tinyxml2::XML_SUCCESS != eResult) {
    LOG_ERROR("failed to save " << SG_AGENT_CONF);
    return eResult;
  } else {
    LOG_INFO("succeed to create " << sg_agent::SG_AGENT_CONF
                                  << ", mnspath = " << mnsPath);
  }

  return SUCCESS;
}

int SGAgentEnv::GetIpFromIntranet() {
  int ret;
  char ip[INET_ADDRSTRLEN];
  char mask[INET_ADDRSTRLEN];
  ret = getIntranet(ip, mask);
  if (SUCCESS != ret) {
	LOG_ERROR("failed to get IP, Mask  by getIntranet, ret = " << ret);
	// 使用host方式获取IP
	ret = getHost(g_global_var->gIp);
	if (SUCCESS != ret) {
	  LOG_ERROR("failed to get IP by getHost, ret = " << ret);
	} else {
	  LOG_INFO("get local IP = " << g_global_var->gIp);
	}
	// mask default = 255.255.0.0
  } else {
	g_global_var->gIp = std::string(ip);
	g_global_var->gMask = std::string(mask);
	LOG_INFO("get local IP = " << g_global_var->gIp
							   << ", Mask = " << g_global_var->gMask);
  }
  return ret;
}

bool SGAgentEnv::IsOnlineTestHost(int &err) {
  char ip[INET_ADDRSTRLEN];
  char mask[INET_ADDRSTRLEN];
  int ret = getIntranet(ip, mask);
  if (SUCCESS != ret) {
    LOG_ERROR("can't get ip with getIntranet");
    err = ERR_NOTGETIP;
    return false;
  }

  char host[256] = {0};
  getHostInfo(host, ip);
  LOG_INFO("host = " << host);

  err = SUCCESS;
  if(strstr(host, ".corp.sankuai.com") > 0){
    return false;
  }

  char pattern_test[] = "-test[0-9]*\\.[A-Za-z]+\\.sankuai.com";
  const size_t nmatch = 1;
  regmatch_t pm[1];
  regex_t reg_test;
  regcomp(&reg_test, pattern_test, REG_EXTENDED | REG_NOSUB);
  ret = regexec(&reg_test, host, nmatch, pm, REG_NOTBOL);
  return REG_NOMATCH != ret;
}

} //namespace
