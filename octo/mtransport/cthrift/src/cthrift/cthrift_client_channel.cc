//
// Created by ZhangXiang on 2018/3/16.
//
#include <boost/shared_ptr.hpp>
#include "cthrift_client_worker.h"
#include "cthrift_tbinary_protocol.h"
#include "cthrift_client_channel.h"

using namespace apache::thrift::transport;
using namespace apache::thrift::async;

using namespace cthrift;

CthriftClientChannel::CthriftClientChannel(boost::shared_ptr <CthriftClient> sp_cthrift_cli)
        : cond_ready_read(mutexlock_conn_ready),
          sp_cthrift_client_(sp_cthrift_cli),
          sp_cthrift_client_worker_(sp_cthrift_client_->GetClientWorker()){
    sp_mutexlock_write_buf = boost::make_shared<muduo::MutexLock>();
    sp_write_buf = boost::make_shared<TMemoryBuffer>();
    sp_read_buf = boost::make_shared<TMemoryBuffer>();
}

CthriftClientChannel::~CthriftClientChannel() {
}

bool CthriftClientChannel::good() const { return true; }
bool CthriftClientChannel::error() const { return false; }
bool CthriftClientChannel::timedOut() const { return false; }

void CthriftClientChannel::sendAndRecvMessage(const TAsyncChannel::VoidCallback& cob,
                                              TMemoryBuffer* sendBuf,
                                              TMemoryBuffer* recvBuf) {
    //sendBuf不具备持久性，copy一份sendBuf内容
    uint8_t *src_buf;
    uint32_t sz = 0;
    sendBuf->getBuffer(&src_buf, &sz);
    TMemBufSharedPtr sp_send_tmembuf_ = boost::make_shared<TMemoryBuffer>();
    sp_send_tmembuf_->resetBuffer(src_buf, sz, TMemoryBuffer::COPY);

    //获取seqid，rBase_指针移动到正式消息的头部，避免buffer copy开销。
    CthriftTBinaryProtocolWithTMemoryBuf tmp_protocol(sp_send_tmembuf_);
    int seqid = tmp_protocol.GetSeqID();

    string str_id;
    try{
        str_id = boost::lexical_cast<std::string>(seqid);
    } catch(boost::bad_lexical_cast & e) {

        CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
                       << "seqid " << seqid);

        return;
    }
    CLOG_STR_DEBUG("async sp_send_tmembuf_ message id: " << str_id << " & size: " << sz);
    sendBuf->resetBuffer();

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

    //构建上下文内容
    SharedContSharedPtr sp_shared_worker_transport_ = boost::make_shared<
            SharedBetweenWorkerTransport>(&mutexlock_conn_ready,
                                          &cond_ready_read,
                                          sp_mutexlock_read_buf,
                                          &sp_read_buf,
                                          sp_mutexlock_write_buf,
                                          &sp_write_buf,
                                          sp_cthrift_client_->GetTimeout()
    );

    sp_shared_worker_transport_->SetSpanID(str_spanid);
    sp_shared_worker_transport_->SetTraceID(str_traceid);

    sp_shared_worker_transport_->async_flag = true;
    sp_shared_worker_transport_->str_id = str_id;
    sp_shared_worker_transport_->cob_ = cob;
    //传递copy的send_buf，避免异步处理出现的串写问题
    sp_shared_worker_transport_->sp_send_tmembuf = sp_send_tmembuf_;
    //raw point传递收到的消息，底层只有一个异步线程顺序处理无须lock保护；未使用shared_ptr避免造成内存重复释放
    sp_shared_worker_transport_->p_recv_tmembuf = recvBuf;
    sp_shared_worker_transport_->timestamp_start = Timestamp::now();

    sp_shared_worker_transport_->wp_b_timeout = boost::make_shared<bool>();

    //protocol already call SetID2Transport to set id

    CLOG_STR_INFO(" seqid " << sp_shared_worker_transport_->str_id
                            << " upper_spanid " << sp_shared_worker_transport_->str_upper_spanid_
                            << " upper_traceid " << sp_shared_worker_transport_->str_upper_traceid_);

    muduo::Condition
            &cond = sp_cthrift_client_worker_->getCond_avaliable_conn_ready_();
    muduo::MutexLock &mutexlock =
            sp_cthrift_client_worker_->getMutexlock_avaliable_conn_ready_();

    bool b_timeout;
    double d_wait_secs = 0.0;
    const double d_timeout_secs = static_cast<double>(sp_cthrift_client_->GetTimeout()) / 1000;

    while (0 >= sp_cthrift_client_worker_
            ->getAtomic_avaliable_conn_num_()) {//while, NOT if
        CLOG_STR_WARN("No good conn from client worker, waiting");

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
                CLOG_STR_WARN(sp_cthrift_client_->GetTimeout()
                                      << "ms countdown to 0, but no good conn ready, maybe server busy"
                                      << " upper_spanid " << sp_shared_worker_transport_->str_upper_spanid_
                                      << " upper_traceid " << sp_shared_worker_transport_->str_upper_traceid_);

                throw TTransportException(TTransportException::TIMED_OUT,
                                          "wait for good conn timeout, maybe conn all be occupied or server list empty");
            }
        }
    }


    size_t sz_queue_size =
            sp_cthrift_client_worker_->getP_event_loop_()->queueSize();

    if (CTHRIFT_UNLIKELY(sp_cthrift_client_->GetThreshold() <= sz_queue_size)) {
        CLOG_STR_ERROR("async worker queue size： " << sz_queue_size << " more than the threshold, drop request."
                               << " upper_spanid " << sp_shared_worker_transport_->str_upper_spanid_
                               << " upper_traceid " << sp_shared_worker_transport_->str_upper_traceid_);
        //这里最好是抛出thrift异常，用户快速感知，进行降速处理；但是thrift本身异常类型无匹配项
        throw TTransportException(TTransportException::INTERNAL_ERROR,
                                  "RPC send too fast, async queue size already more than the threshold. "
                                  "This request will be dropped, please slow down and retry");
        /* sp_cthrift_client_worker_->getP_async_event_loop_()->runInLoop(boost::bind(&CthriftClientWorker::AsyncBadCallback,
                                                                             sp_cthrift_client_worker_.get(),
                                                                             sp_shared_worker_transport_));*/
    } else {
        sp_cthrift_client_worker_->getP_event_loop_()->runInLoop(boost::bind(&CthriftClientWorker::AsyncSendReq,
                                                                             sp_cthrift_client_worker_.get(),
                                                                             sp_shared_worker_transport_));    //NOT sp_shared_worker_transport_ itself
    }
}

void CthriftClientChannel::sendMessage(
        const VoidCallback& cob, apache::thrift::transport::TMemoryBuffer* message) {
    (void) cob;
    (void) message;
    throw TProtocolException(TProtocolException::NOT_IMPLEMENTED,
                             "Unexpected call to TEvhttpClientChannel::sendMessage");
}


void CthriftClientChannel::recvMessage(
        const VoidCallback& cob, apache::thrift::transport::TMemoryBuffer* message) {
    (void) cob;
    (void) message;
    throw TProtocolException(TProtocolException::NOT_IMPLEMENTED,
                             "Unexpected call to TEvhttpClientChannel::recvMessage");
}
