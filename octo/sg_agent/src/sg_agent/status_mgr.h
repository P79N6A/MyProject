#ifndef __status_manager__H__
#define __status_manager__H__
#include "sgagent_service_types.h"

namespace sg_agent {

const std::string MTthriftSwitchMinVersion = "mtthrift-v1.5.3";
const std::string MTthriftSwitchMaxVersion = "mtthrift-v1.6.4-SNAPSHOT";

class StatusMgr {
 public:
  int SwitchStatus(SGService &service);
  bool NeedToSwitch(const SGService &service);
};

}

#endif
