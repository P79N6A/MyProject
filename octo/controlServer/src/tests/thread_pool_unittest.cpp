//
// Created by Xiang Zhang on 2017/10/16.
//

#include "../thread_pool.h"
#include <muduo/net/EventLoop.h>
#include <muduo/base/Thread.h>

#include <boost/bind.hpp>

#include <gtest/gtest.h>

using namespace muduo;
using namespace muduo::net;
using namespace Controller;
using testing::Types;

void print(EventLoop* p = NULL)
{
    printf("main(): pid = %d, tid = %d, loop = %p\n",
           getpid(), CurrentThread::tid(), p);
}

void init(EventLoop* p)
{
    printf("init(): pid = %d, tid = %d, loop = %p\n",
           getpid(), CurrentThread::tid(), p);
}

TEST(ThreadPoolTest, HandleTrueReturn)
{
    print();

    EventLoop loop;
    loop.runAfter(11, boost::bind(&EventLoop::quit, &loop));

    {
        printf("Single thread %p:\n", &loop);
        thread_pool model(&loop, "single");
        model.start(0);
        EXPECT_TRUE(model.getNextLoop() == &loop);
        EXPECT_TRUE(model.getNextLoop() == &loop);
        EXPECT_TRUE(model.getNextLoop() == &loop);
    }

    {
        printf("Another thread:\n");
        thread_pool model(&loop, "another");
        model.start(1);
        EventLoop* nextLoop = model.getNextLoop();
        nextLoop->runAfter(2, boost::bind(print, nextLoop));
        EXPECT_TRUE(nextLoop != &loop);
        EXPECT_TRUE(nextLoop == model.getNextLoop());
        EXPECT_TRUE(nextLoop == model.getNextLoop());
        ::sleep(3);
    }

    {
        printf("Three threads:\n");
        thread_pool model(&loop, "three");
        model.start(3);
        EventLoop* nextLoop = model.getNextLoop();
        nextLoop->runInLoop(boost::bind(print, nextLoop));
        EXPECT_TRUE(nextLoop != &loop);
        EXPECT_TRUE(nextLoop != model.getNextLoop());
        EXPECT_TRUE(nextLoop != model.getNextLoop());
        EXPECT_TRUE(nextLoop == model.getNextLoop());
    }

    loop.loop();
}

