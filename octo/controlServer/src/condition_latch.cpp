//
// Created by Xiang Zhang on 2017/9/29.
//

#include "condition_latch.h"

using namespace muduo;
using namespace Controller;

condition_latch::condition_latch()
        : mutex_(),
          condition_(mutex_){
}

void condition_latch::wait() {
    MutexLockGuard lock(mutex_);
    condition_.wait();
}

// returns true if time out, false otherwise.
bool condition_latch::wait(double seconds) {
    MutexLockGuard lock(mutex_);
    bool ret = condition_.waitForSeconds(seconds);
    return ret;
}

void condition_latch::notify() {
    MutexLockGuard lock(mutex_);
    condition_.notify();
}