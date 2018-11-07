#include "event_loop_thread_proxy.h"
#include <unistd.h>

namespace muduo {
namespace net {

static const int kSleepTime = 200 * 1000; // 200ms

EventLoopThreadProxy::EventLoopThreadProxy() : event_loop_proxy_(NULL) {
}

EventLoopThreadProxy::~EventLoopThreadProxy() {
  if (event_loop_proxy_) {
    event_loop_proxy_->quit();
    // sleep for avoiding race condition 
    usleep(kSleepTime);
    delete event_loop_proxy_;
    event_loop_proxy_ = NULL;
  }
}

IEventLoopProxy *EventLoopThreadProxy::startLoop() {
  EventLoop *loop = event_loop_thread_.startLoop();
  event_loop_proxy_ = new EventLoopProxy(loop);
  return event_loop_proxy_;
}

IEventLoopThreadProxy *EventLoopThreadProxyMaker() {
  return new EventLoopThreadProxy();
}

void EventLoopThreadProxyDestroyer(IEventLoopThreadProxy *p) {
  if (p) {
    delete p;
    p = NULL;
  }
}

} // namespace net
} // namespace muduo

