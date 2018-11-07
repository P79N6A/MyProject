#ifndef CHRIFT_CTHRIFTSVR_H_
#define CHRIFT_CTHRIFTSVR_H_

#include <map>

#include <muduo/base/Mutex.h>
#include <muduo/base/ThreadPool.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/TcpServer.h>

#include <thrift/Thrift.h>
#include <thrift/server/TServer.h>

#include <octoidl/sgagent_common_types.h>
#include <octoidl/sgagent_service_types.h>

#include "cthrift_common.h"
#include "cthrift_client.h"
#include "cthrift_tbinary_protocol.h"
#include "cthrift_transport.h"
#include "cthrift_sgagent.h"
#include "cthrift_uniform_protocol.h"

namespace cthrift {
using apache::thrift::server::TServer;
using apache::thrift::TProcessor;
using apache::thrift::TProcessorFactory;
using apache::thrift::protocol::TProtocolFactory;
using apache::thrift::transport::TTransportFactory;

typedef boost::weak_ptr <muduo::net::TcpConnection> TcpConnWeakPtr;

struct ConnEntry: public muduo::copyable {
  TcpConnWeakPtr wp_conn_;

  explicit ConnEntry(const TcpConnWeakPtr &wp_conn)
      : wp_conn_(wp_conn) {}

  ~ConnEntry(void) {
    muduo::net::TcpConnectionPtr sp_conn = wp_conn_.lock();

    if (sp_conn && sp_conn->connected()) {
      LOG_INFO << "conn " << (sp_conn->peerAddress()).toIpPort()
               << " timeout";
      sp_conn->shutdown();
    }
  }
};

typedef boost::weak_ptr <ConnEntry> ConnEntryWeakPtr;

struct ConnContext {
 public:
  enum cthrift::State enum_state;
  int32_t i32_want_size;
  time_t t_conn;
  time_t t_last_active;
  ConnEntryWeakPtr wp_conn_entry;

  ConnContext(void)
      : enum_state(kExpectFrameSize), i32_want_size(0),
        t_last_active(0) {}
};

typedef boost::shared_ptr <ConnContext> ConnContextSharedPtr;

class CthriftSvr: boost::noncopyable,
                  public TServer {
 private:
  //time wheel
  typedef boost::shared_ptr <ConnEntry> ConnEntrySharedPtr;
  typedef boost::unordered_set <ConnEntrySharedPtr>
      ConnEntryBucket;    //more than one data entry in one grid in circule buffer
  typedef boost::circular_buffer <ConnEntryBucket> ConnEntryBucketCirculBuf;
  typedef muduo::ThreadLocalSingleton <ConnEntryBucketCirculBuf>
      LocalSingConnEntryCirculBuf;        //kick idle conn

  //static const double kDCheckConnIntervalSec;
  static const time_t kTMaxCliIdleTimeSec;
  static const int8_t kI8TimeWheelGridNum;

  static __thread boost::shared_ptr <TMemoryBuffer>
      *sp_p_input_tmemorybuffer_;
  static __thread boost::shared_ptr <TMemoryBuffer>
      *sp_p_output_tmemorybuffer_;

  static __thread boost::shared_ptr <TProtocol>
      *sp_p_input_tprotocol_;
  static __thread boost::shared_ptr <TProtocol>
      *sp_p_output_tprotocol_;

  static __thread boost::shared_ptr <std::string> *sp_p_str_current_connid_ ;
  static __thread boost::shared_ptr <std::string> *sp_p_str_current_traceid_ ;

  static __thread boost::shared_ptr <TProcessor> *sp_p_processor_;

  std::string str_svr_appkey_;
  uint16_t u16_svr_port_;

  int32_t i32_max_conn_num_;
  int32_t i32_svr_overtime_ms_;
  int16_t i16_conn_thread_num_;
  int16_t i16_worker_thread_num_;

  static __thread int32_t i32_curr_conn_num_;

  muduo::net::EventLoop event_loop_;  //guarantee init before server_ !!
  muduo::net::TcpServer server_;

  muduo::AtomicInt64 atom_i64_worker_thread_pos_;
  vector<muduo::net::EventLoop *> vec_worker_event_loop_;

  SGService sg_service_;
  boost::shared_ptr <CthriftClient> sp_cthrift_client_;
  boost::shared_ptr <SGAgentClient> sp_sgagent_client_;

  boost::shared_ptr <muduo::net::TimerId>
      sp_timerid_regsvr_; //use to control getsvrlist

  muduo::AtomicInt64 atom_i64_recv_msg_per_min_;

  void OnConn(const muduo::net::TcpConnectionPtr &conn);
  void OnMsg(const muduo::net::TcpConnectionPtr &conn,
             muduo::net::Buffer *buffer,
             muduo::Timestamp receiveTime);
  void OnWriteComplete(const muduo::net::TcpConnectionPtr &conn);

  void WorkerThreadInit(muduo::CountDownLatch *p_countdown_workthread_init);

  void Process(const boost::shared_ptr <muduo::net::Buffer> &sp_buf,
               boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
               Timestamp timestamp_from_recv);

  void ProcessThrift(const int32_t &i32_req_size,
                     uint8_t *p_u8_req_buf,
                     boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                     muduo::Timestamp timestam);

  void ProcessHessian(boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                      muduo::Timestamp timestam);

  void ProcessUniform(const int32_t &i32_req_size,
                      const uint8_t *p_u8_req_buf,
                      boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                      muduo::Timestamp timestam);

  void ProcessUniformNormal( CthriftUniformRequest& request,
                        const int32_t &i32_req_size,
                        const uint8_t *p_u8_req_buf,
                        boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                        muduo::Timestamp timestam);

  void ProcessUniformNormalHeartBeat( CthriftUniformRequest& request,
                              boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                              muduo::Timestamp timestam);

   void ProcessUniformScannerHeartBeat( CthriftUniformRequest& request,
                                       boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                                       muduo::Timestamp timestam);

  void Process( CthriftUniformRequest& request,
               const int32_t &i32_req_size,
               uint8_t *p_u8_req_buf,
               boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
               muduo::Timestamp timestamp);

    void ProcessHeartbeat(CthriftUniformRequest &request, boost::weak_ptr<muduo::net::TcpConnection> wp_tcp_conn,
                        muduo::Timestamp timestamp);

  void TimewheelKick(void);

  void ConnThreadInit(muduo::CountDownLatch *p_countdown_connthread_init);

  void RegSvr(void);

  int8_t ArgumentCheck(const string &str_app_key,
                       const uint16_t &u16_port,
                       const int32_t &i32_svr_overtime_ms,
                       const int32_t &i32_max_conn_num,
                       const int8_t &i8_check_type,   //0: ONLY check
      // str_app_key & port  1: full check
                       string *p_str_reason) const;

  void Init(void);
  void InitStaticThreadLocalMember(void);

 public:
  //simple construct
  template<typename Processor>
  CthriftSvr(const std::string &str_app_key,
             const boost::shared_ptr <Processor> &processor,
             const uint16_t &u16_port,
             const string str_svr_name = "",
             THRIFT_OVERLOAD_IF(Processor, TProcessor)
  ) throw(TException)
      :
      TServer(processor),
      str_svr_appkey_(str_app_key),
      u16_svr_port_(u16_port),
      i32_max_conn_num_(100000),  //default 10W conn
      i32_svr_overtime_ms_(0),    //default NO overtime
      i16_conn_thread_num_(kI16CpuNum),
      i16_worker_thread_num_(kI16CpuNum),
      server_(&event_loop_, muduo::net::InetAddress(u16_port), str_app_key +
          "_" + "cthrift_svr"){
    sp_cthrift_client_ = boost::make_shared<CthriftClient>(
        CthriftSgagent::kStrSgagentAppkey,
        str_app_key,
        kI32DefultSgagentTimeoutMS);

    std::string str_reason;
    if (ArgumentCheck(str_app_key,
                      u16_port,
                      0,
                      0,
                      0,
                      &str_reason)) {
      throw TException(str_reason); //safe
    }

    cthrift::CthriftSgagent::str_svr_name_ =  str_svr_name;
    Init();
  }


  //construct with over_time/max_conn_num set
  template<typename Processor>
  CthriftSvr(const std::string &str_app_key,
             const boost::shared_ptr <Processor> &processor,
             const uint16_t &u16_port,
             const bool &b_single_thread,
             const int32_t &i32_svr_overtime_ms,
             const int32_t &i32_max_conn_num,
             const string str_svr_name = "",
             THRIFT_OVERLOAD_IF(Processor, TProcessor)
  ) throw(TException)
      :
      TServer(processor),
      str_svr_appkey_(str_app_key),
      u16_svr_port_(u16_port),
      i32_max_conn_num_(i32_max_conn_num),
      i32_svr_overtime_ms_(i32_svr_overtime_ms),
      i16_conn_thread_num_(kI16CpuNum),
      i16_worker_thread_num_(true == b_single_thread ? 1 : kI16CpuNum),
      server_(&event_loop_, muduo::net::InetAddress(u16_port),
              str_app_key + " " + "cthrift_svr"){
    sp_cthrift_client_ = boost::make_shared<CthriftClient>(
        CthriftSgagent::kStrSgagentAppkey,
        str_app_key,
        kI32DefultSgagentTimeoutMS);

    std::string str_reason;
    if (ArgumentCheck(str_app_key,
                      u16_port,
                      i32_svr_overtime_ms,
                      i32_max_conn_num,
                      1,
                      &str_reason)) {
      throw TException(str_reason);  //safe
    }
    cthrift::CthriftSgagent::str_svr_name_ =  str_svr_name;
    Init();
  }

  //construct with over_time/max_conn_num set/worker_thread
  template<typename Processor>
  CthriftSvr(const std::string &str_app_key,
             const boost::shared_ptr <Processor> &processor,
             const uint16_t &u16_port,
             const bool &b_single_thread,
             const int32_t &i32_svr_overtime_ms,
             const int32_t &i32_max_conn_num,
             const int16_t &i16_worker_thread_num,
             const string str_svr_name = "",
             THRIFT_OVERLOAD_IF(Processor, TProcessor)
  ) throw(TException)
      :
      TServer(processor),
      str_svr_appkey_(str_app_key),
      u16_svr_port_(u16_port),
      i32_max_conn_num_(i32_max_conn_num),
      i32_svr_overtime_ms_(i32_svr_overtime_ms),
      i16_conn_thread_num_(kI16CpuNum),
      server_(&event_loop_, muduo::net::InetAddress(u16_port),
              str_app_key + " " + "cthrift_svr"){
    sp_cthrift_client_ = boost::make_shared<CthriftClient>(
        CthriftSgagent::kStrSgagentAppkey,
        str_app_key,
        kI32DefultSgagentTimeoutMS);

    std::string str_reason;

    if (true == b_single_thread) {
      if (CTHRIFT_UNLIKELY(1 < i16_worker_thread_num)) {
        LOG_WARN << "single mode, CANNOT set worker thread "
                 << i16_worker_thread_num << " so reset to be 1";
      }

      i16_worker_thread_num_ = 1;
    } else if (0 >= i16_worker_thread_num) {

      std::string str_worker_thread_num;

      try{
        str_worker_thread_num = boost::lexical_cast<std::string>(i16_worker_thread_num);
      } catch(boost::bad_lexical_cast & e) {

        LOG_ERROR << "boost::bad_lexical_cast :" << e.what()
                  << "i16_worker_thread_num " << i16_worker_thread_num;
      }

      str_reason.assign("i16_worker_thread_num " + str_worker_thread_num);
      LOG_ERROR << str_reason;
      throw TException(str_reason);
    } else {
      i16_worker_thread_num_ = i16_worker_thread_num;
    }

    if (ArgumentCheck(str_app_key,
                      u16_port,
                      i32_svr_overtime_ms,
                      i32_max_conn_num,
                      1,
                      &str_reason)) {
      throw TException(str_reason);  //safe
    }

    cthrift::CthriftSgagent::str_svr_name_ =  str_svr_name;
    Init();
  }

  static const std::string GetCurrentConnId() {
     return *(*sp_p_str_current_connid_);
  }

  static const std::string GetCurrentTraceId() {
      return *(*sp_p_str_current_traceid_);
  }

  void StatMsgNumPerMin(void) {
    LOG_INFO << atom_i64_recv_msg_per_min_.getAndSet(0) / 60
             << " msg per second";
  }

  void
  InitWorkerThreadPos(void) {  //init start pos for avoid big-number-mod performance issue
    if (CTHRIFT_LIKELY(1 < i16_worker_thread_num_)) {
      LOG_DEBUG << atom_i64_worker_thread_pos_.getAndSet(0)
                << " msg per 5 mins";
    }
  }

  string GetEnvInfo(void){
    return cthrift::CthriftSgagent::str_env_;
  }

  void SetServiceNameForUniform(const string& str_svr_name){
       cthrift::CthriftSgagent::str_svr_name_ =  str_svr_name;
  }

  ~CthriftSvr(void);

  void serve();

  void stop();

  const muduo::string &name() const {
    return server_.name();
  }
}; // CthriftSvr
} // cthrift

#endif
