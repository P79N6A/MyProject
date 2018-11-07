//
// Created by Chao Shu on 16/4/5.
//

#include <gtest/gtest.h>

#include <boost/bind.hpp>
#include <boost/make_shared.hpp>

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSocket.h>
#include <thrift/transport/TTransportUtils.h>

#include <muduo/base/AsyncLogging.h>
#include <muduo/base/Logging.h>
#include <muduo/base/TimeZone.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/InetAddress.h>

#include <cthrift/cthrift_svr.h>

#include "Echo.h"

using namespace std;

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace echo;
using namespace cthrift;

boost::shared_ptr<muduo::Thread> g_sp_thread_svr;
boost::shared_ptr<muduo::Thread> g_sp_thread_cli;

muduo::AsyncLogging *g_asyncLog = NULL;

void asyncOutput(const char *msg, int len) {
  g_asyncLog->append(msg, len);
}

void Quit(void) {
  g_sp_thread_svr.reset();
  g_sp_thread_cli.reset();

  LOG_INFO << "exit";

  exit(0);
}

void TestGetSvrList(const string &str_svr_appkey, const int32_t &i32_loop_num,
                    muduo::net::EventLoop *p_event_loop) {
  boost::shared_ptr<CthriftTransport> transport =
      boost::make_shared<CthriftTransport>(str_svr_appkey, 500, "com.sankuai"
          ".inf.newct.client");

  boost::shared_ptr<CthriftTBinaryProtocol>
      protocol(new CthriftTBinaryProtocol(transport));
  
  EchoClient client(protocol);

  transport->open();

  string str_ret;
  string str_req;
  string str_num;

  try {
    for (int i = 0; i < i32_loop_num; i++) {
      try{
        str_num = boost::lexical_cast<std::string>(i);
      } catch(boost::bad_lexical_cast & e) {

        cerr << "boost::bad_lexical_cast :" << e.what()
             << "i : " << i;
        continue;
      }

      client.echo(str_ret, "test" + str_num);

      EXPECT_EQ(str_ret, "test" + str_num);
    }
  } catch (TException &tx) {
    LOG_ERROR << "echo test" + str_num + " error: " << tx.what();
    cerr << "echo test" + str_num + " error: " << tx.what() << endl;
  }

  transport->close();

  LOG_INFO << "Test OK";

  p_event_loop->runInLoop(boost::bind(&Quit));
}

class EchoHandler: virtual public EchoIf {
 public:
  EchoHandler() {
  }

  void echo(std::string &str, const std::string &s) {
    LOG_DEBUG << "EchoHandler::echo:" << s;
    str = s;
  }
};


void TestRegSvr(const string &str_svr_appkey,
                const uint16_t &u16_port,
                muduo::net::EventLoop *p_event_loop) {
  boost::shared_ptr<EchoHandler> handler(new EchoHandler());
  boost::shared_ptr<TProcessor> processor(new EchoProcessor(handler));

  try {
    CthriftSvr svr(str_svr_appkey, processor, u16_port);
    svr.serve();
  } catch (TException &tx) {
    EXPECT_EQ(tx.what(), "str_app_key empty, reason ");
    LOG_ERROR << "CthriftSvr failed: " << tx.what();
    cerr << "CthriftSvr failed: " << tx.what() << endl;
    FAIL();

    p_event_loop->runInLoop(boost::bind(&Quit));
  }
}

int main(int argc, char **argv) {
  muduo::AsyncLogging log("feature_test", 500 * 1024 * 1024);
  log.start();
  g_asyncLog = &log;

  muduo::Logger::setLogLevel(muduo::Logger::DEBUG);

  muduo::Logger::setTimeZone(muduo::TimeZone(8 * 3600, "CST"));
  muduo::Logger::setOutput(asyncOutput);

  if (CTHRIFT_UNLIKELY(4 < argc)) {
    LOG_ERROR
        << "prog <appkey,default \"com.sankuai.inf.newct\"> <echo loop time, default 100> <port, defalut 6666>";
    cerr
        << "prog <appkey,default \"com.sankuai.inf.newct\"> <echo loop time, default 100> <port, defalut 6666>"
        << endl;
    exit(-1);
  }

  string str_svr_appkey("com.sankuai.inf.newct");
  int32_t i32_echo_time = 100;
  uint16_t u16_port = 6666;

  if (2 <= argc) {
    str_svr_appkey.assign(argv[1]);
    if (3 <= argc) {
      i32_echo_time = atoi(argv[2]);
    } else {
      u16_port = static_cast<uint16_t>(atoi(argv[3]));
    }
  }

  LOG_DEBUG << "str_svr_appkey: " << str_svr_appkey << " i32_echo_time: " <<
        i32_echo_time << " u16_port: " << u16_port;

  muduo::net::EventLoop event_loop;

  g_sp_thread_svr =
      boost::make_shared<muduo::Thread>(boost::bind(&TestRegSvr, str_svr_appkey,
                                                    u16_port, &event_loop));
  g_sp_thread_svr->start();

  muduo::CurrentThread::sleepUsec(5000 * 1000); //wait 5s for reg svr

  g_sp_thread_cli =
      boost::make_shared<muduo::Thread>(boost::bind(&TestGetSvrList,
                                                    str_svr_appkey,
                                                    i32_echo_time,
                                                    &event_loop));
  g_sp_thread_cli->start();

  event_loop.runAfter(10.0, boost::bind(&Quit));

  event_loop.loop();
  LOG_INFO << "EXIT loop";
  return 0;
}