//
// Created by Xiang Zhang on 2017/10/13.
//

#include "thread_pool.h"

#include <boost/bind.hpp>

#include <stdio.h>
namespace Controller {
    using namespace std;
    using namespace muduo;
    using namespace muduo::net;

    thread_pool::thread_pool(EventLoop* base_loop, const string& nameArg)
            : p_base_loop_(base_loop),
              started_(false),
              num_threads_(0),
              name_(nameArg){
    }

    thread_pool::~thread_pool() {

    }

    void thread_pool::start(int numThreads) {
        assert(!started_);

        num_threads_ = numThreads;
        started_ = true;

        typedef boost::function<void(EventLoop*) > ThreadInitCallback;

        for (int i = 0; i < num_threads_; ++i) {
            char buf[name_.size() + 32];
            snprintf(buf, sizeof buf, "%s%d", name_.c_str(), i);
            EventLoopThread* t = new EventLoopThread(ThreadInitCallback(), buf);
            threads_.push_back(t);
            loops_.push_back(t->startLoop());
        }
    }

    EventLoop* thread_pool::getNextLoop() {
        assert(started_);
        EventLoop* loop = p_base_loop_;

        if (!loops_.empty())
        {
            loop = loops_[count_.incrementAndGet() % loops_.size()];
        }
        return loop;
    }

    std::vector<muduo::net::EventLoop*> thread_pool::getAllLoops() {
        assert(started_);
        if (loops_.empty())
        {
            return std::vector<EventLoop*>(1, p_base_loop_);
        }
        else
        {
            return loops_;
        }
    }
}