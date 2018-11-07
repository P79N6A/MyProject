#include <gtest/gtest.h>
#include <string>
#include "muduo/net/EventLoop.h"
#include "muduo/net/EventLoopThread.h"
#include "comm/buffer_mgr.h"
#include "boost/bind.hpp"
using namespace sg_agent;
class BufferMgrTest : public testing::Test {
	public:
			virtual void SetUp(){
			loop_1 = thread_1.startLoop();
			loop_2 = thread_2.startLoop();
			loop_3 = thread_3.startLoop();
		  loop_4 = thread_4.startLoop();
		}
		void Insert(){
			m_buffer.insert("test", "test-mt");
		}
		void GetKeyList(){
			std::vector<std::string> key_list;
			int ret = m_buffer.GetKeyList(key_list);
//			EXPECT_NE(ERR_BUFFERMGR_BUFHEAD_NULL, ret);
		}
		void Delete(){
			m_buffer.del("test");
		}
		const char*  Get(){
			std::string value;
			int ret = m_buffer.get("test",value);
			EXPECT_NE(ERR_BUFFERMGR_BUFHEAD_NULL, ret);
		}
	public:

		muduo::net::EventLoopThread thread_1;
		muduo::net::EventLoopThread thread_2;
		muduo::net::EventLoopThread thread_3;
    muduo::net::EventLoopThread thread_4;
		muduo::net::EventLoop* loop_1;
		muduo::net::EventLoop* loop_2;
		muduo::net::EventLoop* loop_3;
		muduo::net::EventLoop* loop_4;
		BufferMgr<std::string> m_buffer;
};

TEST_F(BufferMgrTest, multiThread_run){

	loop_1->runEvery(0.001, boost::bind(&BufferMgrTest::Insert, this));
	loop_2->runEvery(0.001, boost::bind(&BufferMgrTest::GetKeyList, this));
	loop_3->runEvery(0.001, boost::bind(&BufferMgrTest::Delete, this));
	loop_4->runEvery(0.001, boost::bind(&BufferMgrTest::Get, this));
	std::cout<<"run buffer_mgr in multi-thread, please wait 60s." << std::endl;
	sleep(60);
}
