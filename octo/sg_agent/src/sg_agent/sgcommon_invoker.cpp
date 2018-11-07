#include <dlfcn.h>
#include "sgcommon_invoker.h"
#include "comm/log4cplus.h"
#include "sgcommon/event_loop_thread_proxy.h"

extern bool g_is_test;
extern void *g_handle;
using namespace muduo::net;
namespace sg_agent {
IEventLoopThreadProxy *EventLoopThreadProxyMaker() {
  if (g_is_test) {
#ifdef SG_AGENT_TEST
    return muduo::net::EventLoopThreadProxyMaker();
#else
    LOG_ERROR("g_is_test = " << g_is_test << ", but SG_AGENT_TEST = false");
    return NULL;
#endif
  } else {
    typedef IEventLoopThreadProxy *(*CF)();
    CF cf = reinterpret_cast<CF>(dlsym(g_handle, "EventLoopThreadProxyMaker"));

    if (!cf) {
      LOG_ERROR("cannot find EventLoopThreadProxyMaker");
      return NULL;
    } else {
      return cf();
    }
  }
}

void EventLoopThreadProxyDestroyer(IEventLoopThreadProxy *loop_thread) {
  if (g_is_test) {
#ifdef SG_AGENT_TEST
    muduo::net::EventLoopThreadProxyDestroyer(loop_thread);
#endif
  } else {
    typedef void (*CF)(IEventLoopThreadProxy *);
    CF cf = reinterpret_cast<CF>(dlsym(g_handle, "EventLoopThreadProxyDestroyer"));

    if (!cf) {
      LOG_ERROR("cannot find EventLoopThreadProxyDestroyer");
    } else {
      cf(loop_thread);
    }
  }
}
}
