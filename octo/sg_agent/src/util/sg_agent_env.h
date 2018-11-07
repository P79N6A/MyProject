// =====================================================================================
// 
//       Filename:  sg_agent_env.h
// 
//       Description:  环境
// 
//       Version:  1.0
//       Created:  2015-11-26
//       Author: yangjie17
//       Revision:  none
// 
// 
//  =====================================================================================

#ifndef __sgagent_env_H__
#define __sgagent_env_H__

#include <string>
#include "comm/log4cplus.h"

namespace sg_agent {

class SGAgentEnv {
 public:
  // 检测env是否属于agent内部定义的环境
  static bool checkEnv(const std::string &env);
  // 环境切换
  static bool switchEnv(const std::string &env,
                        const int currentEnv, const std::string &verifyCode);

  // 初始化sg_agent环境
  static int InitEnv();
  static int GetIpFromIntranet();
 private:
  // init gAppenv
  static int InitAppenv();

  static int getEnvFromSgAgentEnv();
  static int getEnvFromPath(const std::string &path);
  static int GenSgAgentEnv(std::string mnsPath);
  static bool IsOnlineTestHost(int &err);

};

} //namespace
#endif
