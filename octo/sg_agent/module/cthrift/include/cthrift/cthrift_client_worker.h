//
// Created by Chao Shu on 16/3/6.
//

#ifndef CTHRIFT_CTHRIFT_CLIENT_WORKER_H
#define CTHRIFT_CTHRIFT_CLIENT_WORKER_H

#include "cthrift_common.h"
#include "cthrift_sgagent.h"
#include "cthrift_client.h"
#include "cthrift_transport.h"

namespace cthrift {
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

struct WeightSort {
  bool operator()(const double &a, const double &b) const {
    return a > b;
  }
};

class ConnInfo {
 private:
  SGService sgservice_;
  TcpClientSharedPtr sp_tcpclient_;

  std::multimap<double,
      TcpClientWeakPtr,
      WeightSort> *p_map_weight_tcpclientwp_;

//for relate to weight index, if not be deleted, safe
  std::multimap<double, TcpClientWeakPtr>::iterator
      it_map_weight_tcpclientwp_index_;

 public:
  ConnInfo(const SGService &sgservice_tmp,
           std::multimap<double,
               TcpClientWeakPtr,
               WeightSort> *
           p_map_weight_tcpclientwp) : sgservice_(sgservice_tmp),
                                       p_map_weight_tcpclientwp_(
                                           p_map_weight_tcpclientwp) {}

  ~ConnInfo(void) {
    if (CTHRIFT_UNLIKELY(!p_map_weight_tcpclientwp_)) {
      LOG_ERROR << "p_map_weight_tcpclientwp_ NULL";
    } else {
      LOG_INFO << "delete appkey " << sgservice_.appkey << " ip: "
               << sgservice_.ip << " port: " << sgservice_.port
               << " from weight pool";
      p_map_weight_tcpclientwp_->erase(it_map_weight_tcpclientwp_index_);
    }
  }

  const SGService &GetSgservice(void) const {
    return sgservice_;
  }

  bool CheckConnHealthy(void) const;

  void UptSgservice(const SGService &sgservice);

  void setSp_tcpclient_(const TcpClientSharedPtr &sp_tcpclient);

  TcpClientSharedPtr &getSp_tcpclient_() {
    return sp_tcpclient_;
  }

  /*int8_t FetchTcpConnSP(muduo::net::TcpConnectionPtr *p_tcpconn_sp) {
    if (sp_tcpclient_.get() && sp_tcpclient_->connection()) {
      p_tcpconn_sp->reset((sp_tcpclient_->connection()).get());
      return 0;
    }

    LOG_INFO << "sp_tcpclient_ NOT init";
    return -1;
  }*/
};

typedef boost::shared_ptr <ConnInfo> ConnInfoSharedPtr;
typedef boost::weak_ptr <ConnInfo> ConnInfoWeakPtr;

struct ConnContext4Worker {
 public:
  State enum_state;
  int32_t i32_want_size;

  time_t t_last_conn_time_;
  time_t t_last_recv_time_;
  time_t t_last_send_time_;
  bool b_highwater;
  bool b_occupied;

  ConnInfoWeakPtr wp_conn_info;
  //std::queue <std::string> queue_send;

  ConnContext4Worker(const ConnInfoSharedPtr &sp_conn_info)
      : enum_state(kExpectFrameSize), i32_want_size(0), b_highwater(false),
        b_occupied(false), wp_conn_info(sp_conn_info) {}
};

typedef boost::shared_ptr <ConnContext4Worker> Context4WorkerSharedPtr;

class CthriftClientWorker {
 private:
  typedef boost::unordered_map<string, ConnInfoSharedPtr>::iterator
      UnorderedMapStr2SpConnInfoIter;

  static const int32_t kI32HighWaterSize; //64K

  boost::shared_ptr <muduo::net::TcpClient> sp_tcpclient_sentinel_;

  muduo::MutexLock mutexlock_avaliable_conn_ready_;
  muduo::Condition cond_avaliable_conn_ready_;

  muduo::AtomicInt32
      atomic_avaliable_conn_num_;  //exclude disconn/highwater/occupied

  std::string str_svr_appkey_;
  std::string str_client_appkey_;

  char *unzip_buf_;

  boost::unordered_map <string, SGService>
      map_ipport_sgservice_; //use compare and update svrlist

  //boost::shared_ptr<CthriftTransport> sp_cthrift_transport_sgagent_;
  boost::shared_ptr <CthriftClient> sp_cthrift_client_;
  boost::shared_ptr <SGAgentClient> sp_sgagent_client_;

  //for get seqid from raw buffer
  boost::shared_ptr <TMemoryBuffer>
      *sp_p_tmemorybuffer_;
  boost::shared_ptr <CthriftTBinaryProtocolWithTMemoryBuf>
      *sp_p_cthrift_tbinary_protocol_;

  void OnConn4Sentinel(const muduo::net::TcpConnectionPtr &conn);
  void OnMsg4Sentinel(const muduo::net::TcpConnectionPtr &conn,
                      muduo::net::Buffer *buffer,
                      muduo::Timestamp receiveTime);

  //for common srvlist update
  boost::unordered_map <string, ConnInfoSharedPtr>
      map_ipport_spconninfo_;

  static __thread std::multimap<double, TcpClientWeakPtr, WeightSort>
      *  //ONLY used by conninfo
      p_multimap_weight_wptcpcli_;

  typedef boost::unordered_map<string, ConnInfoSharedPtr>
  ::iterator
      UnorderedMapIpPort2ConnInfoSP;

  typedef std::multimap<double, TcpClientWeakPtr, WeightSort>::iterator
      MultiMapIter;

  //MultiMapIter it_last_choose_conn;

  boost::shared_ptr <muduo::net::EventLoopThread> sp_event_thread_;

  boost::shared_ptr <muduo::net::EventLoopThread> sp_event_thread_sgagent_;

  muduo::net::EventLoop *p_event_loop_;
  muduo::net::EventLoop *p_event_loop_sgagent_;

  boost::unordered_map <string, SharedContSharedPtr>
      map_id_sharedcontextsp_;

  typedef boost::unordered_map<string, SharedContSharedPtr>::iterator
      MapID2SharedPointerIter;

  void AddSrv(const std::vector <SGService> &vec_add_sgservice);
  void DelSrv(const std::vector <SGService> &vec_add_sgservice);
  void ChgSrv(const std::vector <SGService> &vec_add_sgservice);

  void OnConn(const muduo::net::TcpConnectionPtr &conn);
  void OnMsg(const muduo::net::TcpConnectionPtr &conn,
             muduo::net::Buffer *buffer,
             muduo::Timestamp receiveTime);
  void OnWriteComplete(const muduo::net::TcpConnectionPtr &conn);
  void OnHighWaterMark(const muduo::net::TcpConnectionPtr &conn, size_t len);

  void UpdateSvrList
      (const std::vector <SGService> &vec_sgservice);

  void InitWorker(void);
  void InitSgagentHandlerThread(void);
  void InitSentinel(void);
  void UnInitSentinel(void);

  void CheckLocalSgagent();
  bool CheckLocalSgagentHealth(void);

  void GetSvrList(void);

  int8_t ChooseNextReadyConn(TcpClientWeakPtr *p_wp_tcpcli);
  static int8_t CheckRegion(const double &d_weight);

 public:
  CthriftClientWorker(const std::string &str_svr_appkey,
                      const std::string &str_cli_appkey);


  void ClearTcpClient(void) {
    map_ipport_spconninfo_.clear(); //clear tcpclient, OR may core when
    // multiple tcpclient quit
    sp_tcpclient_sentinel_.reset();
  }


  virtual ~CthriftClientWorker() {
    p_event_loop_->runInLoop(boost::bind(&CthriftClientWorker::ClearTcpClient,
                                         this));
    //muduo::CurrentThread::sleepUsec(10 * 1000);

    if (CTHRIFT_LIKELY(unzip_buf_)) {
      delete unzip_buf_;
    }

    //delete will cause memory issue, CthriftClientWorker should keepalive
    // during
    // thread life-time, so two pointers leak acceptable
    /*if (CTHRIFT_LIKELY(sp_p_tmemorybuffer_)) {
      delete sp_p_tmemorybuffer_;
    }

    if (CTHRIFT_LIKELY(sp_p_cthrift_tbinary_protocol_)) {
      delete sp_p_cthrift_tbinary_protocol_;
    }*/
  }

  muduo::net::EventLoop *getP_event_loop_(void) const {
    return p_event_loop_;
  }

  void DelContextMapByID(const std::string &str_id) {

    MapID2SharedPointerIter
            map_iter = map_id_sharedcontextsp_.find(str_id);
    if(map_iter != map_id_sharedcontextsp_.end()) {
      boost::shared_ptr <muduo::net::TcpConnection> sp_send_conn((map_iter->second->wp_send_conn).lock());
      if(sp_send_conn) {
        LOG_WARN << "del id from ipport :" << (sp_send_conn->peerAddress()).toIpPort();
      }
    }

    LOG_WARN << "del id " << str_id << " by transport for timeout";
    map_id_sharedcontextsp_.erase(str_id);
  }

  void SendTransportReq(SharedContSharedPtr sp_shared);

  int32_t getAtomic_avaliable_conn_num_(){
    return atomic_avaliable_conn_num_.get();
  }

  muduo::Condition &getCond_avaliable_conn_ready_() {
    return cond_avaliable_conn_ready_;
  }

  muduo::MutexLock &getMutexlock_avaliable_conn_ready_(void) {
    return mutexlock_avaliable_conn_ready_;
  }
};
}

#endif //CTHRIFT_CTHRIFT_CLIENT_WORKER_H
