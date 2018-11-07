//
// Created by smartlife on 2017/8/7.
//
#ifndef DEBUG_HOME_TCPSERVER_H
#define DEBUG_HOME_TCPSERVER_H

#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<sys/socket.h>
#include <boost/noncopyable.hpp>
#include <muduo/base/CurrentThread.h>
#include <boost/bind.hpp>
#include <muduo/base/Thread.h>
#include<sys/types.h>
#include<sys/select.h>
#include<netinet/in.h>
#include <boost/shared_ptr.hpp>
#include <muduo/net/EventLoop.h>
#include <boost/make_shared.hpp>
#include <muduo/base/Mutex.h>
#include <muduo/base/ThreadPool.h>
#include <muduo/net/TcpServer.h>
#include <muduo/base/AsyncLogging.h>
#include <muduo/base/FileUtil.h>
#include <muduo/base/Mutex.h>
#include <muduo/base/ThreadLocalSingleton.h>
#include <muduo/base/ThreadPool.h>
#include <muduo/base/TimeZone.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/InetAddress.h>
#include <muduo/net/http/HttpContext.h>
#include <muduo/net/http/HttpResponse.h>
#include <muduo/net/TcpClient.h>
#include <muduo/net/TcpConnection.h>
#include <muduo/net/TcpServer.h>
#include <muduo/net/TimerId.h>
#include <boost/unordered_set.hpp>
#include <muduo/base/Exception.h>


#define MAX_CIRCUL_BUFFER 4
#define MAX_FD_SIZE 1024
#define MAX_AGENT_FD 1024

extern int m_AgentListenPort;
typedef boost::weak_ptr <muduo::net::TcpConnection> TcpConnWeakPtr;
struct ConnEntry: public muduo::copyable {

	TcpConnWeakPtr wp_conn_;

	explicit ConnEntry(const TcpConnWeakPtr &wp_conn)
		: wp_conn_(wp_conn) {}

	~ConnEntry(void) {
		muduo::net::TcpConnectionPtr sp_conn = wp_conn_.lock();

		if (sp_conn && sp_conn->connected()) {
			sp_conn->shutdown();
		}
	}
};

typedef boost::weak_ptr <ConnEntry> ConnEntryWeakPtr;


class TcpServer{

	public:

		  TcpServer(int listenPort){
			  try{
				  CraneTcpServer = boost::make_shared<muduo::net::TcpServer>(&event_loop_,
							            muduo::net::InetAddress(listenPort),"tcp server");
			  } catch(const muduo::Exception& ex){
				  //to catch the bind failed, exit process
				  //TODO:调整初始化顺序后，此处增加日志打印
				  sleep(2);
				  exit(1);
			  }
			assert(LocalSingConnEntryCirculBuf::pointer() == NULL);
			LocalSingConnEntryCirculBuf::instance();
			assert(LocalSingConnEntryCirculBuf::pointer() != NULL);
			(LocalSingConnEntryCirculBuf::instance()).resize(MAX_CIRCUL_BUFFER);

			  CraneTcpServer->setConnectionCallback(boost::bind(&TcpServer::OnConn,
						this,
						_1));
			  CraneTcpServer->setMessageCallback(boost::bind(&TcpServer::OnMsg,
						this,
						_1,
						_2,
						_3));
			  CraneTcpServer->start();
			sp_event_thread_ = boost::make_shared<muduo::net::EventLoopThread>();
			p_event_loop = sp_event_thread_->startLoop();
		};

		muduo::net::EventLoop *p_event_loop;
		boost::shared_ptr<muduo::net::EventLoopThread> sp_event_thread_;
		void StartServer(){
			event_loop_.loop();
		}
		~TcpServer(){};
		int StartServer(int serverType);
		void SetLogServer();
		int SetServerPara();
		void OnConn(const muduo::net::TcpConnectionPtr &conn);
		void OnMsg(const muduo::net::TcpConnectionPtr &conn,
				muduo::net::Buffer *buffer,
				muduo::Timestamp receiveTime);

	private:
		muduo::net::EventLoop event_loop_;
  		boost::shared_ptr <muduo::net::TcpServer> CraneTcpServer;
		typedef boost::shared_ptr <ConnEntry> ConnEntrySharedPtr;
		typedef boost::unordered_set <ConnEntrySharedPtr>
			ConnEntryBucket;
		typedef boost::circular_buffer <ConnEntryBucket> ConnEntryBucketCirculBuf;
		typedef muduo::ThreadLocalSingleton <ConnEntryBucketCirculBuf>
			LocalSingConnEntryCirculBuf;

		int16_t u16_svr_port_;
		static __thread int32_t i32_curr_conn_num_;
		int32_t i32_max_conn_num_;
		int32_t i32_svr_overtime_ms_;
		int16_t i16_conn_thread_num_;
		int16_t i16_worker_thread_num_;
		int m_ListenPort;

	protected:

};
#endif //DEBUG_HOME_TCPSERVER_H
