//
// Created by hawk on 2017/9/29.
//

#ifndef CONTROLSERVER_CONDITION_LATCH_H
#define CONTROLSERVER_CONDITION_LATCH_H

#include <muduo/base/Condition.h>
#include <muduo/base/Mutex.h>

#include <boost/noncopyable.hpp>

namespace Controller {

class condition_latch : boost::noncopyable{
public:
    condition_latch();

    void wait();

    bool wait(double seconds);

    void notify();

private:
    mutable muduo::MutexLock mutex_;
    muduo::Condition condition_;
};

}


#endif //CONTROLSERVER_CONDITION_LATCH_H
