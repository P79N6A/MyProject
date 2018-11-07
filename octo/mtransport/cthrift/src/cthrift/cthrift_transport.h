//
// Created by Chao Shu on 16/2/23.
//

#ifndef CTHRIFT_TRANSPORT_H
#define CTHRIFT_TRANSPORT_H

#include <thrift/async/TAsyncChannel.h>
#include "cthrift_common.h"

namespace cthrift {
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

class CthriftClientWorker;
class CthriftSgagent;

struct SharedBetweenWorkerTransport {
  apache::thrift::async::TAsyncChannel::VoidCallback cob_;
  std::string str_id;  //write by transport
  muduo::MutexLock *p_mutexlock_conn_ready;
  muduo::Condition *p_cond_ready_read;

  boost::weak_ptr <muduo::MutexLock> wp_mutexlock_read_buf;
  TMemBufSharedPtr *p_sp_read_tmembuf;
  //写锁在超时场景下存在idl-client销毁风险，用weak_ptr监控对象生命周期
  boost::weak_ptr <muduo::MutexLock> wp_mutexlock_write_buf;
  TMemBufSharedPtr *p_sp_write_tmembuf;

  int32_t i32_timeout_ms;
  muduo::Timestamp timestamp_start;
  muduo::Timestamp timestamp_cliworker_send;  //use ONLY by clientworker
  boost::weak_ptr <muduo::net::TcpConnection>
      wp_send_conn;  //use ONLY by clientworker

  boost::weak_ptr<bool> wp_b_timeout;   //use to note client_worker timeout

  //async
  bool async_flag;
  TMemoryBuffer *p_recv_tmembuf;
  TMemBufSharedPtr sp_send_tmembuf;

  std::string str_upper_spanid_;
  std::string str_upper_traceid_;

  SharedBetweenWorkerTransport()
      : p_mutexlock_conn_ready(0), p_cond_ready_read(0),
        p_sp_read_tmembuf(0), p_sp_write_tmembuf(0),
        i32_timeout_ms(0),async_flag(false), p_recv_tmembuf(0),
        timestamp_start(muduo::Timestamp::now()) { ; }

  ~SharedBetweenWorkerTransport() {
    p_sp_read_tmembuf = NULL;
    p_sp_write_tmembuf = NULL;
  }

  SharedBetweenWorkerTransport(muduo::MutexLock *p_mutexlock_conn_ready_tmp,
                               muduo::Condition *p_cond_ready_read_tmp,
                               boost::weak_ptr <muduo::MutexLock> wp_mutexlock_read_buf_tmp,
                               TMemBufSharedPtr *p_sp_read_tmembuf_tmp,
                               boost::weak_ptr <muduo::MutexLock>  wp_mutexlock_write_buf_tmp,
                               TMemBufSharedPtr *p_sp_write_tmembuf_tmp,
                               const int32_t &i32_timeout_tmp)
      : p_mutexlock_conn_ready(p_mutexlock_conn_ready_tmp),
        p_cond_ready_read(p_cond_ready_read_tmp),
        wp_mutexlock_read_buf(wp_mutexlock_read_buf_tmp),
        p_sp_read_tmembuf(p_sp_read_tmembuf_tmp),
        wp_mutexlock_write_buf(wp_mutexlock_write_buf_tmp),
        p_sp_write_tmembuf(p_sp_write_tmembuf_tmp),
        i32_timeout_ms(i32_timeout_tmp),async_flag(false),
        timestamp_start(muduo::Timestamp::now()){
  }

  SharedBetweenWorkerTransport(const SharedBetweenWorkerTransport &shared) {
    str_id.assign(shared.str_id);

    async_flag = shared.async_flag;
    p_mutexlock_conn_ready = shared.p_mutexlock_conn_ready;
    p_cond_ready_read = shared.p_cond_ready_read;
    wp_mutexlock_read_buf = shared.wp_mutexlock_read_buf;
    p_sp_read_tmembuf = shared.p_sp_read_tmembuf;
    wp_mutexlock_write_buf = shared.wp_mutexlock_write_buf;
    p_sp_write_tmembuf = shared.p_sp_write_tmembuf;

    i32_timeout_ms = shared.i32_timeout_ms;

    timestamp_start = shared.timestamp_start;
    timestamp_cliworker_send = shared.timestamp_cliworker_send;

    wp_b_timeout = shared.wp_b_timeout;
    p_recv_tmembuf = shared.p_recv_tmembuf;
    sp_send_tmembuf = shared.sp_send_tmembuf;

    str_upper_spanid_ = shared.str_upper_spanid_;
    str_upper_traceid_ = shared.str_upper_traceid_;
  }

  SharedBetweenWorkerTransport &
  operator=(const SharedBetweenWorkerTransport &shared) {
    if (this != &shared) {
      str_id.assign(shared.str_id);

      async_flag = shared.async_flag;
      p_mutexlock_conn_ready = shared.p_mutexlock_conn_ready;
      p_cond_ready_read = shared.p_cond_ready_read;
      wp_mutexlock_read_buf = shared.wp_mutexlock_read_buf;
      p_sp_read_tmembuf = shared.p_sp_read_tmembuf;
      wp_mutexlock_write_buf = shared.wp_mutexlock_write_buf;
      p_sp_write_tmembuf = shared.p_sp_write_tmembuf;

      i32_timeout_ms = shared.i32_timeout_ms;

      timestamp_start = shared.timestamp_start;
      timestamp_cliworker_send = shared.timestamp_cliworker_send;

      wp_b_timeout = shared.wp_b_timeout;
      p_recv_tmembuf = shared.p_recv_tmembuf;
      sp_send_tmembuf = shared.sp_send_tmembuf;

      str_upper_spanid_ = shared.str_upper_spanid_;
      str_upper_traceid_ = shared.str_upper_traceid_;
    }

    return *this;
  }

  bool IsTimeout(void) {
    return wp_b_timeout.expired();
  }

  void EnableTimeout(const boost::shared_ptr<bool> &sp_b_timeout) {
    wp_b_timeout = sp_b_timeout;
  }

    void SetSpanID(const std::string& str_span_id) {
      str_upper_spanid_ = str_span_id;
    }

    void SetTraceID(const std::string& str_trace_id) {
      str_upper_traceid_ = str_trace_id;
    }

  int8_t GetAsyncWriteBuf(muduo::net::Buffer *p_buf) {
    if (CTHRIFT_UNLIKELY(!p_buf || !sp_send_tmembuf)) {
      CLOG_STR_ERROR("async p_buf OR sp_send_tmembuf NULL");
      return -1;
    }

    uint8_t *buf = 0;
    uint32_t u32_len = 0;

    sp_send_tmembuf->getBuffer(&buf, &u32_len);
    CLOG_STR_DEBUG("u32_len " << u32_len);

    if (0 == u32_len) {
      CLOG_STR_WARN("async 0 == u32_len");
      return -1;
    }

    p_buf->appendInt32(u32_len);    //Head size
    p_buf->append(buf, u32_len);

    return 0;
  }

  int8_t GetWriteBuf(muduo::net::Buffer *p_buf) {
    boost::shared_ptr<muduo::MutexLock> sp_mutexlock_write_buf = wp_mutexlock_write_buf.lock();

    if (CTHRIFT_UNLIKELY(!sp_mutexlock_write_buf || !p_sp_write_tmembuf)) {
      CLOG_STR_ERROR("p_sp_write_tmembuf OR p_mutexlock_write_buf NULL");
      return -1;
    }

    TMemBufSharedPtr tmem_tmp;
    {
      muduo::MutexLockGuard lock(*sp_mutexlock_write_buf);
      tmem_tmp = *p_sp_write_tmembuf;
    }

    uint8_t *buf = 0;
    uint32_t u32_len = 0;

    tmem_tmp->getBuffer(&buf, &u32_len);
    CLOG_STR_DEBUG("u32_len " << u32_len);

    if (0 == u32_len) {
      CLOG_STR_WARN("0 == u32_len");
      return -1;
    }

    p_buf->appendInt32(u32_len);    //Head size
    p_buf->append(buf, u32_len);

    return 0;
  }

  void ResetReadBuf(uint8_t *p_buf, uint32_t u32_len) {
    boost::shared_ptr<muduo::MutexLock> sp_mutexlock_read_buf = wp_mutexlock_read_buf.lock();
    if (CTHRIFT_UNLIKELY(!sp_mutexlock_read_buf || !p_sp_read_tmembuf)) {
      CLOG_STR_ERROR("p_sp_read_tmembuf OR p_mutexlock_read_buf NULL");
      return;
    }

    muduo::MutexLockGuard lock(*sp_mutexlock_read_buf);
    if (!(p_sp_read_tmembuf->unique())) {
      *p_sp_read_tmembuf =
          boost::make_shared<TMemoryBuffer>();  //No need copy original buf since reset
    }

    (*p_sp_read_tmembuf)->resetBuffer(p_buf, u32_len, TMemoryBuffer::COPY);
  }

  void ResetRecvBuf(uint8_t *p_buf, uint32_t u32_len) {
    if (CTHRIFT_UNLIKELY(!p_buf || u32_len <= 0)) {
      CLOG_STR_WARN("p_buf invalid OR u32_len <= 0, maybe task timeout befor network transport");
      return;
    }
    //避免重复buffer拷贝，将传入的内存生命周期交给TMemoryBuffer对象管理
    p_recv_tmembuf->resetBuffer(p_buf, u32_len, TMemoryBuffer::TAKE_OWNERSHIP);
  }

  /*void GeneteUuid(void) {
    uuid_t uuid;

    {
      muduo::MutexLockGuard lock(g_mutexlock_uuid);
      uuid_generate(uuid);  //NOT thread safe
    }

    char uuid_str[37] = {0};
    uuid_unparse(uuid, uuid_str);
    str_id.assign(uuid_str);

    LOG_DEBUG << "uuid " << str_id;
  }*/
};

typedef boost::shared_ptr <SharedBetweenWorkerTransport> SharedContSharedPtr;
typedef boost::weak_ptr <SharedBetweenWorkerTransport> WeakContSharedPtr;
}


namespace apache {
namespace thrift {
namespace transport {
using namespace cthrift;

class CthriftTransport: public TTransport {
 private:
  std::string str_cli_appkey_;
  std::string str_svr_appkey_;
  int32_t i32_timeout_ms_;

  muduo::MutexLock mutexlock_conn_ready;
  muduo::Condition cond_ready_read;

  boost::shared_ptr<muduo::MutexLock> sp_mutexlock_read_buf;
  TMemBufSharedPtr sp_read_tmembuf_;
  boost::shared_ptr<muduo::MutexLock> sp_mutexlock_write_buf;
  TMemBufSharedPtr sp_write_tmembuf_;

  SharedContSharedPtr sp_shared_worker_transport_;

  boost::shared_ptr <CthriftClientWorker> sp_cthrift_client_worker_;
  //muduo::EventLoop *p_client_worker_eventloop_;

  boost::shared_ptr<bool> sp_bool_timeout_; //for note timeout to
  // clientworker immediately

  void AppendReadBuf(const uint8_t *buf, uint32_t len) {
    muduo::MutexLockGuard lock(*sp_mutexlock_read_buf);
    if (!(sp_read_tmembuf_.unique())) {
      uint8_t *p_buf = 0;
      uint32_t u32_len = 0;

      sp_read_tmembuf_->getBuffer(&p_buf, &u32_len);
      sp_read_tmembuf_ =
          boost::make_shared<TMemoryBuffer>(p_buf, u32_len, TMemoryBuffer::COPY);
    }

    sp_read_tmembuf_->write(buf, len);
  }

  void AppendWriteBuf(const uint8_t *buf, uint32_t len) {
    muduo::MutexLockGuard lock(*sp_mutexlock_write_buf);
    if (!(sp_write_tmembuf_.unique())) {
      uint8_t *p_buf = 0;
      uint32_t u32_len = 0;

      sp_write_tmembuf_->getBuffer(&p_buf, &u32_len);
      sp_write_tmembuf_ =
          boost::make_shared<TMemoryBuffer>(p_buf, u32_len, TMemoryBuffer::COPY);
    }

    sp_write_tmembuf_->write(buf, len);
  }

  uint32_t ReadBufAvaliableReadSize(void) {
    TMemBufSharedPtr sp_read_buf;
    {
      muduo::MutexLockGuard lock(*sp_mutexlock_read_buf);
      sp_read_buf = sp_read_tmembuf_;
    }

    return sp_read_buf->available_read();
  }

  uint32_t ReadBufRead(uint8_t *buf, uint32_t len) {
    muduo::MutexLockGuard lock(*sp_mutexlock_read_buf);
    if (!(sp_read_tmembuf_.unique())) {
      uint8_t *p_buf = 0;
      uint32_t u32_len = 0;

      sp_read_tmembuf_->getBuffer(&p_buf, &u32_len);
      sp_read_tmembuf_ =
          boost::make_shared<TMemoryBuffer>(p_buf, u32_len, TMemoryBuffer::COPY);
    }

    return sp_read_tmembuf_->read(buf, len);
  }

  void ResetReadBuf(void) {
    muduo::MutexLockGuard lock(*sp_mutexlock_read_buf);
    if (!(sp_read_tmembuf_.unique())) {
      sp_read_tmembuf_ =
          boost::make_shared<TMemoryBuffer>(); //No need copy original buf since reset
    }

    sp_read_tmembuf_->resetBuffer();
  }

 public:
    CthriftTransport(const std::string &str_svr_appkey,
                     const int32_t &i32_timeout,
                     const std::string &str_cli_appkey,
                     const boost::shared_ptr <CthriftClientWorker> &sp_cthrift_client_worker);

  virtual uint32_t read_virt(uint8_t *buf, uint32_t len) throw
  (TTransportException);

  virtual void write_virt(const uint8_t *buf, uint32_t len);

  void flush(void) throw(TTransportException);

  void SetID2Transport(const std::string &str_id) {  //call by cthrift_protocol
    (sp_shared_worker_transport_->str_id).assign(str_id);
  }

  void ResetWriteBuf(void) {
    muduo::MutexLockGuard lock(*sp_mutexlock_write_buf);
    if (!(sp_write_tmembuf_.unique())) {
      sp_write_tmembuf_ =
          boost::make_shared<TMemoryBuffer>(); //No need copy original buf since reset
    }

    sp_write_tmembuf_->resetBuffer();
  }
};
}
}
}


#endif //CTHRIFT_TRANSPORT_H