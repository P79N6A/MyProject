#include <muduo/base/TimeZone.h>
#include <muduo/net/EventLoop.h>
#include <cthrift/cthrift_svr.h>

#include "ControlServer.h"

using namespace std;
using namespace muduo;
using namespace muduo::net;
using namespace cthrift;

muduo::AsyncLogging *g_asyncLog = NULL;
void asyncOutput(const char *msg, int len) {
  g_asyncLog->append(msg, len);
}

int
main(int argc, char **argv) {
  //init muduo log thread
  muduo::AsyncLogging log("/opt/logs/controlserver", 500 * 1024 * 1024);
  log.start();
  g_asyncLog = &log;
  muduo::Logger::setLogLevel(muduo::Logger::WARN);//线上运行推荐用WARN级别日志
  muduo::Logger::setTimeZone(muduo::TimeZone(8 * 3600, "CST"));
  muduo::Logger::setOutput(asyncOutput);

  string str_svr_appkey("com.sankuai.inf.octo.cpluginserver"); //服务端的appkey
  uint16_t u16_port = 5299;
  bool b_single_thread = false;  //当时单线程运行时，worker thread num 只能是1
  int32_t i32_timeout_ms = 30;
  int32_t i32_max_conn_num = 20000;
  int16_t i16_worker_thread_num = 3;
  switch (argc) {
    case 1:
      LOG_INFO << "no input arg, use defalut";
      break;
    case 7:
      str_svr_appkey.assign(argv[1]);
      u16_port = static_cast<uint16_t>(atoi(argv[2]));
      b_single_thread = (1 == atoi(argv[3])) ? true : false;
      i32_timeout_ms = atoi(argv[4]);
      i32_max_conn_num = atoi(argv[5]);
      i16_worker_thread_num = static_cast<uint16_t>(atoi(argv[6]));
      break;
    default:
      LOG_ERROR << "prog <svr appkey> <port> <single thread ? 1:true, 0:false><timeout ms> <max connection num> <worker thread num for every IO thread: suggest NOT more than CPU core num> but argc " << argc;
      exit(-1);
  }
  LOG_INFO << "svr appkey " << str_svr_appkey;
  LOG_INFO << "port " << u16_port;
  LOG_INFO << "single thread? " << b_single_thread;
  LOG_INFO << "timeout ms " << i32_timeout_ms;
  LOG_INFO << "timeout ms " << i32_max_conn_num;
  LOG_INFO << "timeout ms " << i16_worker_thread_num;


  try {
    boost::shared_ptr<Controller::ControllerServer> handler(new Controller::ControllerServer());
    boost::shared_ptr <TProcessor> processor(new Controller::ControllerServiceProcessor(handler));
   
    CthriftSvr server(str_svr_appkey, processor, u16_port,
                      b_single_thread, //业务逻辑是否只能单线程运行
                      i32_timeout_ms, //毫秒, 服务端超时时间, 只用作日志告警输出
                      i32_max_conn_num //服务最大链接数
        , i16_worker_thread_num); //自定义工作线程数，如前面设置业务只能单线程运行，这里只能填1
    

    server.serve();
  } catch (TException &tx) {
    LOG_ERROR << tx.what();
    return -1;
  } catch (int e) {
    return e;
  }

  return 0;
}
