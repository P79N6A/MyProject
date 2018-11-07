#include "event_loop_proxy.h"

#include <iostream>
#include <boost/bind.hpp>
#include <boost/function.hpp>

using namespace std;

namespace muduo {
namespace net {

void RunTask(boost::shared_ptr <Task> task) {
  task->Run();
}

EventLoopProxy::EventLoopProxy(EventLoop *loop) : loop_(loop) {}

EventLoopProxy::~EventLoopProxy() {}

void EventLoopProxy::loop() {
  loop_->loop();
}

void EventLoopProxy::quit() {
  loop_->quit();
}

void EventLoopProxy::runInLoop(const boost::shared_ptr <Task> &task) {
  loop_->runInLoop(boost::bind(&RunTask, task));
}

size_t EventLoopProxy::queueSize() const {
  return loop_->queueSize();
}

void EventLoopProxy::runEvery(double interval, const boost::shared_ptr <Task> &task) {
  loop_->runEvery(interval, boost::bind(&RunTask, task));
}

IEventLoopProxy *IEventLoopProxyMaker(EventLoop *loop) {
  return new EventLoopProxy(loop);
}

} // namespace net
} // namespace muduo
