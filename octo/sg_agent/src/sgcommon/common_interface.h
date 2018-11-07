#ifndef COMMON_INTERFACE_H
#define COMMON_INTERFACE_H

namespace muduo {
namespace net {

class Task {
 public:
  Task() {}
  virtual ~Task() {}
  virtual void Run() = 0;
};

class IEventLoopProxy {
 public:
  virtual ~IEventLoopProxy() {}
  virtual void loop() = 0;

  virtual void runInLoop(const boost::shared_ptr <Task> &task) = 0;

  virtual size_t queueSize() const = 0;

  virtual void runEvery(double interval, const boost::shared_ptr <Task> &task) = 0;
};

class IEventLoopThreadProxy {
 public:
  virtual ~IEventLoopThreadProxy() {}
  virtual IEventLoopProxy *startLoop() = 0;
};

} // namespace net
} // namespace muduo

#endif // COMMON_INTERFACE_H_ 
