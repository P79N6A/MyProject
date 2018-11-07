#include <gtest/gtest.h>
#include "remote/falcon/falcon_collector.h"
#include <boost/bind/bind.hpp>
#include "muduo/base/CountDownLatch.h"
#include "muduo/net/EventLoopThreadPool.h"
#include "muduo/net/EventLoopThread.h"
#include "muduo/net/EventLoop.h"

namespace sg_agent{
	bool is_run_falcon_task = false;
}

using namespace sg_agent;
class FalconTest : public testing::Test{
	public:
		FalconTest(): m_latch(1){}
		void SetUp(){
			m_loop = m_thread_pool.startLoop();	
			m_loop_1 = m_thread_1.startLoop();
			m_loop_2 = m_thread_2.startLoop();
			m_metric = "sg_agent.test.falcon";
			m_tags = "";
		}
	void GetAndResetTest(){
		while(true){
			if(is_break){
				break;
			}
			EXPECT_TRUE(NULL != FalconCollector::GetAndReset());
		}
	}
	public:
	muduo::net::EventLoop* m_loop;
	muduo::net::EventLoop* m_loop_1;
	muduo::net::EventLoop* m_loop_2;
	muduo::net::EventLoopThread m_thread_pool;
	muduo::net::EventLoopThread m_thread_1;
	muduo::net::EventLoopThread m_thread_2;
	std::string m_metric;
	std::string m_tags;
	bool is_break;
	muduo::CountDownLatch m_latch;
};

TEST_F(FalconTest, shared_ptr_swap) {
	// not run upload falcon
	is_run_falcon_task = false;
	is_break = false;

	m_loop->runEvery(0.001, boost::bind(FalconCollector::GetAndReset));
	m_loop_1->runEvery(0.001, boost::bind(FalconCollector::SetRate,"sg_agent.test", "", true));
	m_loop_2->runEvery(0.001, boost::bind(FalconCollector::SetValue, "sg_agent.setValue", "", "10"));
	
	std::cout<< "test falcon in multi-thread, please wait 60s." << std::endl;
	sleep(60);
	is_run_falcon_task = true;
}
