#ifndef SG_AGENT_SGCOMMON_INVOKER_H
#define SG_AGENT_SGCOMMON_INVOKER_H
#include <boost/shared_ptr.hpp>
#include "sgcommon/common_interface.h"

namespace sg_agent {
muduo::net::IEventLoopThreadProxy *EventLoopThreadProxyMaker();

void EventLoopThreadProxyDestroyer(muduo::net::IEventLoopThreadProxy *loop_thread);

}

#endif
