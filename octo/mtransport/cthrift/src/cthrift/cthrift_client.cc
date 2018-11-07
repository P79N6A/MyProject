//
// Created by Chao Shu on 16/11/12.
//
#include <boost/algorithm/string.hpp>

#include "cthrift_client_worker.h"

using namespace cthrift;

const int32_t CthriftClient::kI32DefaultTimeoutMS = 5000;  //5s
muduo::MutexLock CthriftClient::work_resource_lock_;
boost::unordered_map<string, CthriftClient::WorkerWeakPtr> CthriftClient::map_appkey_worker_;

__thread boost::shared_ptr <std::string> * CthriftClient::sp_p_str_current_traceid_ = NULL;
__thread boost::shared_ptr <std::string> * CthriftClient::sp_p_str_current_spanid_  = NULL;

CthriftClient::CthriftClient(const std::string &str_svr_appkey,
              const std::string &str_cli_appkey,
              const int32_t &i32_timeout_ms)
        : str_svr_appkey_(str_svr_appkey),
          str_cli_appkey_(str_cli_appkey),
          i32_timeout_ms_(
                  0 > i32_timeout_ms ? kI32DefaultTimeoutMS : i32_timeout_ms),
          str_serviceName_filter_(""),
          i32_port_filter_(-1),
          b_parallel_(false),
          i32_async_pending_threshold_(30),
          b_async_(false),
          b_auth_(false),
          str_auth_token_(""){
  //旧的构造函数，对齐以前实现逻辑；不作修改。
  if (SUCCESS == InitWorker(false)) {
    CLOG_STR_INFO("Init cthrift client success, begin to provide normal service.");
  }
}


CthriftClient::CthriftClient(const std::string &str_svr_appkey,
                             const int32_t &i32_timeout_ms)
    : str_svr_appkey_(str_svr_appkey),
      str_cli_appkey_(""),
      i32_timeout_ms_(
          0 > i32_timeout_ms ? kI32DefaultTimeoutMS : i32_timeout_ms),
      str_serviceName_filter_(""),
      i32_port_filter_(-1),
      b_parallel_(false),
      i32_async_pending_threshold_(30),
      b_async_(false),
      b_auth_(false),
      str_auth_token_(""){
}

int CthriftClient::InitWorker(bool async) {
    //key需要将过滤开关考虑进去
    string map_key = str_svr_appkey_ + str_serviceName_filter_ + boost::lexical_cast<std::string>(i32_port_filter_);
    //async模式下，必须开启并发模式；因为异步场景下thrift本身的sendBuf无法保证线程安全
    if (async) {
        b_parallel_ = true;
    }
    do {
        muduo::MutexLockGuard work_lock(work_resource_lock_);
        if (!b_parallel_ && map_appkey_worker_.find(map_key) != map_appkey_worker_.end()
            && !map_appkey_worker_[map_key].expired()) {
            sp_cthrift_client_worker_ = map_appkey_worker_[map_key].lock();
        } else {
            sp_cthrift_client_worker_ = boost::make_shared<CthriftClientWorker>(
                    str_svr_appkey_,
                    str_cli_appkey_,
                    str_serviceName_filter_,
                    i32_port_filter_,
                    i32_timeout_ms_,
                    b_auth_,
                    str_auth_token_);

            if (async) {
                //startup async thread
                sp_cthrift_client_worker_->EnableAsync(i32_timeout_ms_);
            }

            map_appkey_worker_[map_key] = sp_cthrift_client_worker_;
        }
    } while(0);

    Timestamp timestamp_start = Timestamp::now();

    muduo::Condition
            &cond = sp_cthrift_client_worker_->getCond_avaliable_conn_ready_();
    muduo::MutexLock &mutexlock =
            sp_cthrift_client_worker_->getMutexlock_avaliable_conn_ready_();

    const double
            d_default_timeout_secs = static_cast<double>(kI32DefaultTimeoutMS) /
                                     1000;
    double d_left_time_sec = 0.0;
    bool b_timeout = false;

    while (0 >= sp_cthrift_client_worker_
            ->getAtomic_avaliable_conn_num_()) {//while, NOT if
        CLOG_STR_WARN("No good conn for appkey " << str_svr_appkey_
                                                 << " from worker, wait");

        if (!CheckOverTime(timestamp_start,
                           d_default_timeout_secs,
                           &d_left_time_sec)) {
            do {
                muduo::MutexLockGuard lock(mutexlock);
                b_timeout = cond.waitForSeconds(d_left_time_sec);
            } while(0);

            if (b_timeout) {
                if (CTHRIFT_UNLIKELY(0 < sp_cthrift_client_worker_
                        ->getAtomic_avaliable_conn_num_())) {
                    CLOG_STR_DEBUG("miss notify, but already get");
                } else {
                    CLOG_STR_WARN("wait " << d_left_time_sec
                                          << " secs for good conn for appkey " << str_svr_appkey_ << " timeout, maybe need more time");
                }
                return SUCCESS;
                //return ERR_CON_NOT_READY;
            }

            if (CTHRIFT_UNLIKELY(CheckOverTime(timestamp_start,
                                               d_default_timeout_secs,
                                               0))) {
                CLOG_STR_WARN(d_default_timeout_secs
                                      << "secs countdown to 0, but no good conn ready, maybe need more time");
                return SUCCESS;
                //return ERR_CON_NOT_READY;
            }
        }
    }

    CLOG_STR_DEBUG("wait done, avaliable conn num " << sp_cthrift_client_worker_
            ->getAtomic_avaliable_conn_num_());

//CLIENT_INIT(str_cli_appkey_, str_svr_appkey_); //init cmtrace
    return SUCCESS;
}

int CthriftClient::Init(void) {
  boost::trim(str_svr_appkey_);
  if (str_svr_appkey_.empty()) {
    CLOG_STR_ERROR("Fail to init cthrift client, the server appkey can't be empty.");
    return ERR_PARA_INVALID;
  }

  return InitWorker(b_async_);
}

int CthriftClient::SetFilterPort(const unsigned int &i32_port) {
  if (!ValidatePort(i32_port)) {
    CLOG_STR_ERROR("Fail to set the filter port, it can't be < 1 || > 65535. i32_port =" << i32_port);
    return ERR_PARA_INVALID;
  }
  i32_port_filter_ = i32_port;
  return SUCCESS;
}

int CthriftClient::SetFilterService(const std::string &str_serviceName) {
  string tmp = str_serviceName;
  boost::trim(tmp);
  if (tmp.empty()) {
    CLOG_STR_ERROR("The servicename can't be empty");
    return ERR_PARA_INVALID;
  }
  str_serviceName_filter_ = tmp;
  return SUCCESS;
}

int CthriftClient::SetAuthInfo(const bool &b_auth, const std::string& token){
    string tmp = token;
    CLOG_STR_INFO("User SetAuth Info  b_auth:" << b_auth << "  token:" << token);
    boost::trim(tmp);
    if (tmp.empty()) {
        CLOG_STR_INFO("The User Set use default kms auth");
    }
    b_auth_ = b_auth;
    str_auth_token_ = tmp;
}

int CthriftClient::SetClientAppkey(const std::string &str_appkey) {
  string tmp = str_appkey;
  boost::trim(tmp);
  if (tmp.empty()) {
    CLOG_STR_ERROR("The client appkey shouldn't be empty");
    return ERR_PARA_INVALID;
  } else {
    str_cli_appkey_ = tmp;
  }
  return SUCCESS;
}

boost::shared_ptr <TProtocol>
CthriftClient::GetCthriftProtocol(void) {

  CLOG_STR_INFO("cthrift transport init, thread info: "
                           << CurrentThread::tid());
    boost::shared_ptr<CthriftTransport> sp_cthrift_transport_ = boost::make_shared<
            CthriftTransport>(str_svr_appkey_,
                              i32_timeout_ms_,
                              str_cli_appkey_,
                              sp_cthrift_client_worker_);

    boost::shared_ptr<CthriftTBinaryProtoWithCthriftTrans> sp_cthrift_tbinary_protocol_ =
            boost::make_shared<
                    CthriftTBinaryProtoWithCthriftTrans>(sp_cthrift_transport_,
                                                         CTHRIFT_CLIENT);

  return sp_cthrift_tbinary_protocol_;
}
