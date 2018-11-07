//
// Created by Xiang Zhang on 2017/10/13.
//

#ifndef CONTROLSERVER_THREAD_POOL_H
#define CONTROLSERVER_THREAD_POOL_H

#include <string>
#include <vector>

#include <boost/function.hpp>
#include <boost/noncopyable.hpp>
#include <boost/ptr_container/ptr_vector.hpp>

#include <muduo/base/Atomic.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>

namespace Controller {
class thread_pool : boost::noncopyable {
public:
    thread_pool(muduo::net::EventLoop *baseLoop, const std::string& nameArg = "control server thread pool");
    ~thread_pool();
    void start(int numThreads);

    muduo::net::EventLoop* getNextLoop();

    std::vector<muduo::net::EventLoop*> getAllLoops();

    bool started() const {
        return started_;
    }

private:
    muduo::net::EventLoop *p_base_loop_;
    bool started_;
    int num_threads_;
    std::string name_;
    muduo::AtomicInt64 count_;
    boost::ptr_vector<muduo::net::EventLoopThread> threads_;
    std::vector<muduo::net::EventLoop*> loops_;
};
}

#endif //CONTROLSERVER_THREAD_POOL_H
