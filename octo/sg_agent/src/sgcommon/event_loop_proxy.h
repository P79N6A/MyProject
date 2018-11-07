#ifndef MUDUO_NET_EVENTLOOPPROXY_H
#define MUDUO_NET_EVENTLOOPPROXY_H

#include <muduo/net/EventLoop.h>
#include "port.h"
#include "common_interface.h"

namespace muduo {
namespace net {

class EventLoopProxy : public IEventLoopProxy {
 public:
  EventLoopProxy(EventLoop *loop);
  virtual ~EventLoopProxy();

  virtual void loop();

  virtual void quit();

  virtual void runInLoop(const boost::shared_ptr <Task> &task);

  virtual size_t queueSize() const;

  virtual void runEvery(double interval, const boost::shared_ptr <Task> &task);

 private:
  EventLoop *loop_;
};

OUTAPI IEventLoopProxy *IEventLoopProxyMaker(EventLoop *loop);

} // namespace net
} // namespace muduo

#endif  // MUDUO_NET_EVENTLOOPPROXY_H
