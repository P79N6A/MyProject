// =====================================================================================
// 
//       Filename:  sgagent_shutdown.h
// 
//       Description:  环境
// 
//       Version:  1.0
//       Created:  2016-07-07
//       Author: yangjie17
//       Revision:  none
// 
// 
//  =====================================================================================

#ifndef __sgagent_shutdown_H__
#define __sgagent_shutdown_H__

#include <string>

using namespace __gnu_cxx;

namespace sg_agent {

class SGAgentShutdown {
 public:
  // shutdown self
  static bool shutdownSelf(const std::string &verifyCode);
};

} //namespace
#endif
