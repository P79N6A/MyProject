#ifndef CHRIFT_CTHRIFTSVR_H_
#define CHRIFT_CTHRIFTSVR_H_

#include <map>

#include <muduo/base/Mutex.h>
#include <muduo/base/ThreadPool.h>
#include <muduo/base/Exception.h>
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
#include "cthrift_kms.h"

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
      CLOG_STR_INFO("conn " << (sp_conn->peerAddress()).toIpPort()
               << " timeout");
      sp_conn->shutdown();
    }
  }
};

typedef boost::weak_ptr <ConnEntry> ConnEntryWeakPtr;

struct ConnContext {
 public:
  enum cthrift::State enum_state;
  int32_t i32_want_size;
  //time_t t_conn;
  time_t t_last_active;
  ConnEntryWeakPtr wp_conn_entry;
  bool b_is_auth;
  ConnContext(void)
      : enum_state(kExpectFrameSize), i32_want_size(0),
        t_last_active(0),b_is_auth(false) {}
};

typedef boost::shared_ptr <ConnContext> ConnContextSharedPtr;
typedef boost::weak_ptr <ConnContext> ConnContextWeakPtr;

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
  static __thread boost::shared_ptr <std::string> *sp_p_str_current_spanid_ ;
  static __thread boost::shared_ptr <std::map<std::string, std::string> > *sp_p_str_current_traceid_tag_map_ ;
  static __thread boost::shared_ptr <std::map<std::string, std::string> > *sp_p_str_user_tag_map_ ;

  static __thread boost::shared_ptr <TProcessor> *sp_p_processor_;

  std::string str_svr_appkey_;
  uint16_t u16_svr_port_;

  int32_t i32_max_conn_num_;
  int32_t i32_svr_overtime_ms_;
  int16_t i16_conn_thread_num_;
  int16_t i16_worker_thread_num_;
  int8_t  i8_heartbeat_status_;
  bool b_auth_;
  bool b_grayRelease_;
  AuthSize auth_size_;
  std::string token_;
  CthriftKmsTools tools;
  double con_collection_interval_;


  static __thread int32_t i32_curr_conn_num_;

  muduo::net::EventLoop event_loop_;  //guarantee init before server_ !!
  boost::shared_ptr <muduo::net::TcpServer> sp_server_;

  muduo::AtomicInt64 atom_i64_worker_thread_pos_;
  std::vector<muduo::net::EventLoop *> vec_worker_event_loop_;

  muduo::net::EventLoop * p_event_loop_;

  SGService sg_service_;
  boost::shared_ptr <CthriftClient> sp_cthrift_client_;
  boost::shared_ptr <SGAgentClient> sp_sgagent_client_;

  boost::shared_ptr <muduo::net::TimerId>
      sp_timerid_regsvr_; //use to control getsvrlist

  muduo::AtomicInt64 atom_i64_recv_msg_per_min_;

  static __thread boost::shared_ptr <StrStrMap> *sp_p_str_str_appkeyMap_ ;
  static __thread boost::shared_ptr <StrStrMap> *sp_p_str_str_whiteMap_ ;
  static __thread boost::shared_ptr <StrSMMap> *sp_p_str_map_methodMap_ ;
  static __thread boost::shared_ptr <std::string> *sp_p_str_local_token ;

  void UpdateAuthInfo();
  void UpdateAuthInfoForWorkThread( StrStrMap& appkeyMap,   StrStrMap& whiteMap ,
                                    StrSMMap&  methodMap ,  std::string&  local);

  void OnConn(const muduo::net::TcpConnectionPtr &conn);
  void OnMsg(const muduo::net::TcpConnectionPtr &conn,
             muduo::net::Buffer *buffer,
             muduo::Timestamp receiveTime);
  void OnWriteComplete(const muduo::net::TcpConnectionPtr &conn);

  void WorkerThreadInit(muduo::CountDownLatch *p_countdown_workthread_init);

  void Process(const boost::shared_ptr <muduo::net::Buffer> &sp_buf,
               boost::weak_ptr <ConnContext> wp_conn_context,
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
                      bool &b_is_auth,
                      boost::weak_ptr <muduo::net::TcpConnection> wp_tcp_conn,
                      muduo::Timestamp timestam);

  void ProcessUniformNormal( CthriftUniformRequest& request,
                        const int32_t &i32_req_size,
                        const uint8_t *p_u8_req_buf,
                        bool &b_is_auth,
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


  void HandleAuthFailed(CthriftUniformRequest& request,
                               const int32_t &i32_req_size,
                               uint8_t *p_ui8_req_buf,
                               boost::weak_ptr<muduo::net::TcpConnection> wp_tcp_conn,
                               muduo::Timestamp timestamp_from_recv);

  void TimewheelKick(void);

  void ConnThreadInit(muduo::CountDownLatch *p_countdown_connthread_init);

  void RegSvr(void);

  int8_t ArgumentCheck(const std::string &str_app_key,
                       const uint16_t &u16_port,
                       const int32_t &i32_svr_overtime_ms,
                       const int32_t &i32_max_conn_num,
                       const int8_t &i8_check_type,   //0: ONLY check
      // str_app_key & port  1: full check
                       std::string *p_str_reason) const;

  void InitStaticThreadLocalMember(void);

 public:
  void Init(void);

  //simple construct
  template<typename Processor>
  CthriftSvr(const std::string &str_app_key,
             const uint16_t &u16_port,
             const boost::shared_ptr <Processor> &processor,
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
          i8_heartbeat_status_(2),
          b_auth_(false),
          b_grayRelease_(false),
          auth_size_(AuthAppkey),
          token_("") ,
          tools(str_app_key),
          con_collection_interval_(0.0),
          p_event_loop_(NULL){
    try {
      sp_server_ = boost::make_shared<muduo::net::TcpServer>(&event_loop_, muduo::net::InetAddress(u16_port),
                                                             str_app_key + "_" + "cthrift_svr");
    } catch (const muduo::Exception& ex) {
      CLOG_STR_ERROR("port: " << u16_port << " has been occupied by other process.");
      //init muduo server failed, exit process
      usleep(400*1000);
      exit(1);
    }
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

    cthrift::CthriftSgagent::str_svr_name_ =  "";
  }


  template<typename Processor>
  CthriftSvr(const std::string &str_app_key,
             const boost::shared_ptr <Processor> &processor,
             const uint16_t &u16_port,
             const std::string str_svr_name = "",
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
      i8_heartbeat_status_(2),
      b_auth_(false),
      b_grayRelease_(false),
      auth_size_(AuthAppkey),
      token_("") ,
      tools(str_app_key),
      con_collection_interval_(0.0),
      p_event_loop_(NULL){
    try {
      sp_server_ = boost::make_shared<muduo::net::TcpServer>(&event_loop_, muduo::net::InetAddress(u16_port),
                                                             str_app_key + "_" + "cthrift_svr");
    } catch (const muduo::Exception& ex) {
      CLOG_STR_ERROR("port: " << u16_port << " has been occupied by other process.");
      //init muduo server failed, exit process
      usleep(400*1000);
      exit(1);
    }
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
             const std::string str_svr_name = "",
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
      i8_heartbeat_status_(2),
      b_auth_(false),
      b_grayRelease_(false),
      auth_size_(AuthAppkey),
      token_(""),
      tools(str_app_key),
      con_collection_interval_(0.0),
      p_event_loop_(NULL){
    try {
      sp_server_ = boost::make_shared<muduo::net::TcpServer>(&event_loop_, muduo::net::InetAddress(u16_port),
                                                             str_app_key + "_" + "cthrift_svr");
    } catch (const muduo::Exception& ex) {
      CLOG_STR_ERROR("port: " << u16_port << " has been occupied by other process.");
      //init muduo server failed, exit process
      usleep(400*1000);
      exit(1);
    }

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
             const std::string str_svr_name = "",
             THRIFT_OVERLOAD_IF(Processor, TProcessor)
  ) throw(TException)
      :
      TServer(processor),
      str_svr_appkey_(str_app_key),
      u16_svr_port_(u16_port),
      i32_max_conn_num_(i32_max_conn_num),
      i32_svr_overtime_ms_(i32_svr_overtime_ms),
      i16_conn_thread_num_(kI16CpuNum),
      i8_heartbeat_status_(2),
      b_auth_(false),
      b_grayRelease_(false),
      auth_size_(AuthAppkey),
      token_(""),
      tools(str_app_key),
      con_collection_interval_(0.0),
      p_event_loop_(NULL){
    try {
      sp_server_ = boost::make_shared<muduo::net::TcpServer>(&event_loop_, muduo::net::InetAddress(u16_port),
                                                             str_app_key + "_" + "cthrift_svr");
    } catch (const muduo::Exception& ex) {
      CLOG_STR_ERROR("port: " << u16_port << " has been occupied by other process.");
      //init muduo server failed, exit process
      usleep(400*1000);
      exit(1);
    }

    sp_cthrift_client_ = boost::make_shared<CthriftClient>(
        CthriftSgagent::kStrSgagentAppkey,
        str_app_key,
        kI32DefultSgagentTimeoutMS);

    std::string str_reason;

    if (true == b_single_thread) {
      if (CTHRIFT_UNLIKELY(1 < i16_worker_thread_num)) {
        CLOG_STR_WARN("single mode, CANNOT set worker thread "
                 << i16_worker_thread_num << " so reset to be 1");
      }

      i16_worker_thread_num_ = 1;
    } else if (0 >= i16_worker_thread_num) {

      std::string str_worker_thread_num;

      try{
        str_worker_thread_num = boost::lexical_cast<std::string>(i16_worker_thread_num);
      } catch(boost::bad_lexical_cast & e) {

        CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                  << "i16_worker_thread_num " << i16_worker_thread_num);
      }

      str_reason.assign("i16_worker_thread_num " + str_worker_thread_num);
      CLOG_STR_ERROR(str_reason);
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

  void SetAuthInfo(const bool &b_auth, const AuthSize &auth_size=AuthAppkey, const std::string& token=""){
       b_auth_ = b_auth;
       auth_size_ = auth_size;
       token_ = token;
  }

  void SetGrayRelease(const bool &b_grayRelease){
      b_grayRelease_ = b_grayRelease;
  }

  static const std::string GetCurrentConnId() {

      if (CTHRIFT_LIKELY(sp_p_str_current_connid_)) {
          return  *(*sp_p_str_current_connid_);
      }

     return "";
  }

  static const std::string GetCurrentTraceId() {

      if (CTHRIFT_LIKELY(sp_p_str_current_traceid_)) {
          return  *(*sp_p_str_current_traceid_);
      }

      return "";
  }

  static const std::string GetCurrentSpanId() {

    if (CTHRIFT_LIKELY(sp_p_str_current_spanid_)) {
      return  *(*sp_p_str_current_spanid_);
    }

    return "";
  }


  static  void SetUserTagMap(const std::string& key, const std::string& value);

  void StatMsgNumPerMin(void) {
    CLOG_STR_INFO(atom_i64_recv_msg_per_min_.getAndSet(0) / 60
             << " msg per second");
  }

  void
  InitWorkerThreadPos(void) {  //init start pos for avoid big-number-mod performance issue
    if (CTHRIFT_LIKELY(1 < i16_worker_thread_num_)) {
      CLOG_STR_DEBUG(atom_i64_worker_thread_pos_.getAndSet(0)
                << " msg per 5 mins");
    }
  }

  std::string GetEnvInfo(void){
    return cthrift::CthriftSgagent::str_octo_env_;
  }

  void SetServiceNameForUniform(const std::string& str_svr_name){
       cthrift::CthriftSgagent::str_svr_name_ =  str_svr_name;
  }

  //清理空闲连接的间隔时间单位：分钟
  //清理周期至少5min以上
  void SetConnGCInterval(const double &min);

  void SetMaxConNum(const int &i_num) {
      i32_max_conn_num_ = i_num;
  }

  void SetOverTime(const int &overtime_ms) {
      i32_svr_overtime_ms_ = overtime_ms;
  }

  void SetWorkerThreadNum(const int16_t &thread_num) {
      i16_worker_thread_num_ = thread_num;
  }

  void SetServiceName(const std::string& service_name) {
      cthrift::CthriftSgagent::str_svr_name_ =  service_name;
  }

  int Drain();

  ~CthriftSvr(void);

  void serve();

  void stop();

  const muduo::string &name() const {
    return sp_server_->name();
  }
}; // CthriftSvr
} // cthrift

#endif
