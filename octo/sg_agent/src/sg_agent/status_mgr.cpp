#include <string>
#include "status_mgr.h"
#include "comm/inc_comm.h"

namespace sg_agent {
int StatusMgr::SwitchStatus(SGService &service) {
  if (!NeedToSwitch(service) || fb_status::STOPPED == service.status) {
    return 0;
  }

  if (isPortOpen(service.ip, service.port)) {
    LOG_INFO("set reg status to ALIVE, old status is "
                 << service.status);
    service.status = fb_status::ALIVE;
  } else {
    LOG_INFO("set reg status to DEAD, old status is "
                 << service.status);
    service.status = fb_status::DEAD;
  }

  return 0;
}

bool StatusMgr::NeedToSwitch(const SGService &service) {
  if (service.version.compare(MTthriftSwitchMinVersion) >= 0
      && service.version.compare(MTthriftSwitchMaxVersion) <= 0) {
    return true;
  }

  return false;
}
}
