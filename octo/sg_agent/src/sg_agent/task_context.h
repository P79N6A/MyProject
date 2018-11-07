#ifndef TASK_CONTEXT_H_
#define TASK_CONTEXT_H_

#include <unistd.h>
#include <stddef.h>

namespace sg_agent {

static const long kSleepInterval = 10 * 1000; // us

template<class Request, class Response>
class TaskContext {
 public:
  explicit TaskContext(const Request &req)
      : req_(req), result_ready_(false) {}
  ~TaskContext() {}

  // 单位us
  void WaitResult(long timeout) {
    long time_spent = 0;

    while (true) {
      if (result_ready_ || time_spent > timeout) {
        break;
      }
      time_spent += kSleepInterval;
      usleep(kSleepInterval);
    }
  }

  void set_response(const Response &rsp) {
      rsp_ = rsp;
      result_ready_ = true;
  }

  const Request *get_request() const {

    return &req_;
  }

  const Response *get_response() const {
    if (result_ready_) {
      return &rsp_;
    }
    return NULL;
  }
 private:
  Request req_;
  Response rsp_;
  volatile bool result_ready_;
};

} // namespace sg_agent

#endif // TASK_CONTEXT_H_
