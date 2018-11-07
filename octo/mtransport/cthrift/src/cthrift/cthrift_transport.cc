//
// Created by Chao Shu on 16/2/23.
//
#include "cthrift_client_worker.h"

using namespace apache::thrift::transport;
using namespace cthrift;

//muduo::MutexLock g_mutexlock_uuid;


CthriftTransport::CthriftTransport(const std::string &str_svr_appkey,
                 const int32_t &i32_timeout,
                 const std::string &str_cli_appkey,
                 const boost::shared_ptr <CthriftClientWorker> &sp_cthrift_client_worker)
        : str_svr_appkey_(str_svr_appkey),
          i32_timeout_ms_(i32_timeout),  //cthrift_client already check i32_timeout
          str_cli_appkey_(str_cli_appkey),
          sp_cthrift_client_worker_(sp_cthrift_client_worker),
          cond_ready_read(mutexlock_conn_ready){
  sp_mutexlock_read_buf = boost::make_shared<muduo::MutexLock>();
  sp_mutexlock_write_buf = boost::make_shared<muduo::MutexLock>();

  sp_read_tmembuf_ = boost::make_shared<TMemoryBuffer>();
  sp_write_tmembuf_ = boost::make_shared<TMemoryBuffer>();

  sp_shared_worker_transport_ = boost::make_shared<
          SharedBetweenWorkerTransport>(&mutexlock_conn_ready,
                                        &cond_ready_read,
                                        sp_mutexlock_read_buf,
                                        &sp_read_tmembuf_,
                                        sp_mutexlock_write_buf,
                                        &sp_write_tmembuf_,
                                        i32_timeout_ms_);
}

/*uint32_t CthriftTransport::readEnd() {
  LOG_DEBUG << "readEnd for appkey " << str_svr_appkey_ << " seqid " << sp_shared_worker_transport_->str_id << "clear write buf";

  ResetWriteBuf();
  return 0;
}*/

uint32_t CthriftTransport::read_virt(uint8_t *buf,
                                     uint32_t len) throw(TTransportException) {
  bool b_timeout;
  double d_left_secs = 0.0;

  while (1) {
    if (0 == ReadBufAvaliableReadSize()) {
      CLOG_STR_DEBUG("wait for read buf");
    } else {
      //LOG_DEBUG << "get read buf for appkey " << str_svr_appkey_ << " id " <<
      //        sp_shared_worker_transport_->str_id;
      return ReadBufRead(buf, len);
    }

    if (!CheckOverTime(sp_shared_worker_transport_->timestamp_start,
                       static_cast<double>(
                           sp_shared_worker_transport_->i32_timeout_ms)
                           / 1000, &d_left_secs)) {
      {
        muduo::MutexLockGuard lock(mutexlock_conn_ready);
        b_timeout =
            sp_shared_worker_transport_->p_cond_ready_read->waitForSeconds(
                d_left_secs);
      }

      if (b_timeout) {
        if (CTHRIFT_UNLIKELY(ReadBufAvaliableReadSize())) {
          CLOG_STR_DEBUG("miss notify, but buf already get");
          return ReadBufRead(buf, len);
        }

        break;
      }
    } else if (ReadBufAvaliableReadSize()) {  //check again for safe
      CLOG_STR_DEBUG("get read buf for appkey " << str_svr_appkey_ << " id " <<
                sp_shared_worker_transport_->str_id);
      //TODO: 正常读取消息的场景下不用删除map中的context？
      return ReadBufRead(buf, len);
    } else {
      break;
    }
  }

  CLOG_STR_WARN("wait appkey " << str_svr_appkey_ << " id " <<
           sp_shared_worker_transport_->str_id
           << " already " << sp_shared_worker_transport_->i32_timeout_ms
           << " ms for readbuf, timeout"
           << " upper_spanid " << sp_shared_worker_transport_->str_upper_spanid_
           << " upper_traceid " << sp_shared_worker_transport_->str_upper_traceid_);

  sp_bool_timeout_.reset(); //clientworker will use weak_ptr to check timeout

  sp_cthrift_client_worker_->getP_event_loop_()->runInLoop(boost::bind(&CthriftClientWorker::DelContextMapByID,
                                                                       sp_cthrift_client_worker_.get(),
                                                                       sp_shared_worker_transport_->str_id)); //if req NOT return, should del map here

  //worker no need to take the older task
  ResetWriteBuf();

  throw TTransportException(TTransportException::TIMED_OUT,
                            "wait for read buf timeout, maybe  server busy");
}

/*
uint32_t CthriftTransport::readEnd(void) {
  return ReadBufAvaliableReadSize();
}*/

void CthriftTransport::write_virt(const uint8_t *buf, uint32_t len) {
  AppendWriteBuf(buf, len);
}

void CthriftTransport::flush(void) throw(TTransportException) {
  sp_shared_worker_transport_->timestamp_start = Timestamp::now();
  sp_shared_worker_transport_->i32_timeout_ms =
      i32_timeout_ms_;  //in case change timeout after init, so need match these two

  sp_bool_timeout_ = boost::make_shared<bool>();
  sp_shared_worker_transport_->wp_b_timeout = sp_bool_timeout_;

  //protocol already call SetID2Transport to set id

  CLOG_STR_DEBUG("appkey " << str_svr_appkey_ << " seqid "
            << sp_shared_worker_transport_->str_id);

  muduo::Condition
      &cond = sp_cthrift_client_worker_->getCond_avaliable_conn_ready_();
  muduo::MutexLock &mutexlock =
      sp_cthrift_client_worker_->getMutexlock_avaliable_conn_ready_();

  bool b_timeout;
  double d_wait_secs = 0.0;
  const double d_timeout_secs = static_cast<double>(i32_timeout_ms_) / 1000;

  while (0 >= sp_cthrift_client_worker_
      ->getAtomic_avaliable_conn_num_()) {//while, NOT if
    CLOG_STR_WARN("No good conn for appkey " << str_svr_appkey_
             << " from worker, wait");

    if (!CheckOverTime(sp_shared_worker_transport_->timestamp_start,
                       d_timeout_secs,
                       &d_wait_secs)) {
      do {
        muduo::MutexLockGuard lock(mutexlock);
        b_timeout = cond.waitForSeconds(d_wait_secs);
      } while(0);

      if (b_timeout) {
        if (CTHRIFT_UNLIKELY(0 < sp_cthrift_client_worker_
            ->getAtomic_avaliable_conn_num_())) {
          CLOG_STR_DEBUG("miss notify, but already get avaliable conn");
        } else {
          CLOG_STR_ERROR("wait " << d_wait_secs
                    << " secs for good conn timeout");

          throw TTransportException(TTransportException::TIMED_OUT,
                                    "wait for good conn timeout, maybe conn all be occupied or server list empty");
        }
      }

      if (CTHRIFT_UNLIKELY(CheckOverTime(sp_shared_worker_transport_->timestamp_start,
                                         d_timeout_secs, 0))) {
        CLOG_STR_WARN(i32_timeout_ms_
            << "ms countdown to 0, but no good conn ready, maybe server busy");

        throw TTransportException(TTransportException::TIMED_OUT,
                                  "wait for good conn timeout, maybe conn all be occupied or server list empty");
      }
    }
  }

  ResetReadBuf();   //in case transport return, then worker fill readbuf before erase id, for safe

  /*boost::shared_ptr<muduo::net::EventLoopThread>
      sp_worker_thread = sp_cthrift_client_worker_->GetWorkerThreadSP();*/

  size_t sz_queue_size =
      sp_cthrift_client_worker_->getP_event_loop_()->queueSize();

  if (CTHRIFT_UNLIKELY(10 <= sz_queue_size)) {
    CLOG_STR_WARN("worker queue size " << sz_queue_size);
  } else {
    CLOG_STR_DEBUG("worker queue size " << sz_queue_size);
  }

  SharedContSharedPtr sp_shared = boost::make_shared<
      SharedBetweenWorkerTransport>(
      *sp_shared_worker_transport_);

  std::string str_traceid = CthriftClient::GetCurrentTraceId();
  std::string str_spanid  = CthriftClient::GetCurrentSpanId();
  if(str_traceid.empty() || str_spanid.empty()){
    RemoteProcessCall * pServerRpc = TraceNameSpace::getServerRpc();
    if(NULL == pServerRpc || ((RemoteProcessCall*)ILLEGAL_PRT) == pServerRpc){
      RemoteProcessCall rpc(sp_cthrift_client_worker_->GetServerAppkey());
      str_spanid = rpc.getRpcId();
      str_traceid = rpc.getTraceId();
    }else{
      str_spanid = pServerRpc->getNextRpcId();
      str_traceid = pServerRpc->getTraceId();
    }
  }
  else{
    RemoteProcessCall rpc(str_traceid, str_spanid, sp_cthrift_client_worker_->GetServerAppkey());
    str_spanid = rpc.getNextRpcId();
    str_traceid = rpc.getTraceId();
  }

  sp_shared->SetSpanID(str_spanid);
  sp_shared->SetTraceID(str_traceid);

  sp_cthrift_client_worker_->getP_event_loop_()->runInLoop(boost::bind(&CthriftClientWorker::SendTransportReq,
                                                                       sp_cthrift_client_worker_.get(),
                                                                       sp_shared));    //NOT sp_shared_worker_transport_ itself
}

