// =====================================================================================
//
//      Filename:  sgagent_filter.h
//
//      Description:  对serviceList进行route过滤和backup过滤
//
//      Version:  1.0
//      Created:  2015-05-25
//      Revision:  none
//
//
// =====================================================================================

#ifndef SGAGENT_CONF_H_
#define SGAGENT_CONF_H_

#include <string>
#include <vector>
#include <set>
#include "sgagent_service_types.h"
#include <pthread.h>

namespace sg_agent {

int LoadConfig();

} // namespace sg_agent


#endif // SGAGENT_CONF_H_

