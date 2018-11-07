#ifndef MUDUO_NET_EVENTLOOPTHREADPROXY_H
#define MUDUO_NET_EVENTLOOPTHREADPROXY_H

#include <muduo/net/EventLoopThread.h>
#include "port.h"
#include "event_loop_proxy.h"
#include "common_interface.h"

namespace muduo {
namespace net {

class EventLoopThreadProxy : public IEventLoopThreadProxy {
 public:
  EventLoopThreadProxy();
  virtual ~EventLoopThreadProxy();
  virtual IEventLoopProxy *startLoop();

 private:
  EventLoopThread event_loop_thread_;
  EventLoopProxy *event_loop_proxy_;
};

OUTAPI IEventLoopThreadProxy *EventLoopThreadProxyMaker();
OUTAPI void EventLoopThreadProxyDestroyer(IEventLoopThreadProxy *p);

} // namespace net
} // namespace muduo

#endif  // MUDUO_NET_EVENTLOOPTHREADPROXY_H

