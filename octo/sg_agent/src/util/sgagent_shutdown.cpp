#include "sgagent_shutdown.h"
#include "comm/inc_comm.h"
#include "comm/log4cplus.h"
#include "sgservice_misc.h"

namespace sg_agent {
bool SGAgentShutdown::shutdownSelf(const std::string &verifyCode) {
  LOG_INFO("Self shutdowning.");
  if (!SGServiceMisc::CheckVerifyCode(verifyCode)) {
    return false;
  }
  exit(0);
}
} //namespace
