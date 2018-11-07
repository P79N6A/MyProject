//
// Created by Chao Shu on 16/11/12.
//

#ifndef OCTO_DEVELOP_CTHRIFT_CLIENT_H
#define OCTO_DEVELOP_CTHRIFT_CLIENT_H

#include "cthrift_common.h"
#include "cthrift_tbinary_protocol.h"
#include "cthrift_transport.h"

namespace cthrift {
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

enum InitCthriftCode {
    SUCCESS = 0,
    ERR_CON_NOT_READY,     //底层连接池未成功建立到server的连接，需要等待一段时间
    ERR_PARA_INVALID       //入参数错误
};

class CthriftClient {
 private:
  static const int32_t kI32DefaultTimeoutMS;

  std::string str_svr_appkey_;
  std::string str_cli_appkey_;
  std::string str_serviceName_filter_;
  int32_t i32_port_filter_;
  int32_t i32_timeout_ms_;   //TODO specify timeout differ by interface
  bool    b_parallel_;
  int32_t i32_async_pending_threshold_;
  bool    b_async_;

  bool    b_auth_;
  std::string   str_auth_token_;

  boost::shared_ptr <CthriftClientWorker> sp_cthrift_client_worker_;

  static muduo::MutexLock work_resource_lock_;
  typedef boost::weak_ptr<CthriftClientWorker> WorkerWeakPtr;
  static boost::unordered_map<string, WorkerWeakPtr> map_appkey_worker_;


  static __thread boost::shared_ptr <std::string> *sp_p_str_current_traceid_ ;
  static __thread boost::shared_ptr <std::string> *sp_p_str_current_spanid_ ;

  int InitWorker(bool async);
 public:
  CthriftClient(const std::string &str_svr_appkey,
                const int32_t &i32_timeout);

  CthriftClient(const std::string &str_svr_appkey,
                const std::string &str_cli_appkey,
                const int32_t &i32_timeout);

  ~CthriftClient(void) {
    //可以正常释放资源
    //delete will cause memory issue, cthriftclient should keepalive during
    // thread life-time, so two pointers leak acceptable

    /*if (CTHRIFT_LIKELY(p_sp_cthrift_tbinary_protocol_)) {
      delete p_sp_cthrift_tbinary_protocol_;
    }

    if (CTHRIFT_LIKELY(p_sp_cthrift_transport_)) {
      delete p_sp_cthrift_transport_;
    }*/
  }

  boost::shared_ptr <TProtocol> GetCthriftProtocol(void);

  int SetClientAppkey(const std::string &str_appkey);

  int SetFilterPort(const unsigned int &i32_port);

  int SetFilterService(const std::string &str_serviceName);

  int SetAuthInfo(const bool &b_auth, const std::string& token="");

  inline void SetParallel(const bool &b_par) {
      b_parallel_ = b_par;
  }

  inline void SetThreshold(const int &threshold) {
    i32_async_pending_threshold_ = threshold;
  }

  inline void SetAsync(const bool &b_async) {
    b_async_ = b_async;
  }

  inline int32_t GetThreshold() const {
    return i32_async_pending_threshold_;
  }

  int Init(void);

  int32_t GetTimeout() const {
    return i32_timeout_ms_;
  }

  boost::shared_ptr <CthriftClientWorker> GetClientWorker() {
    return sp_cthrift_client_worker_;
  }

  std::string GetEnvInfo(void) const{
    return cthrift::CthriftSgagent::str_env_;
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

    static void SetCurrentTraceId(const std::string& traceid) {

      if (CTHRIFT_LIKELY(sp_p_str_current_traceid_)) {
        (*(*sp_p_str_current_traceid_)).assign(traceid);
      }
      else{
          sp_p_str_current_traceid_ = new boost::shared_ptr<std::string>
                  (boost::make_shared<std::string>());

          (*(*sp_p_str_current_traceid_)).assign(traceid);
      }
    }

    static  void SetCurrentSpanId(const std::string& spanid) {
      if (CTHRIFT_LIKELY(sp_p_str_current_spanid_)) {
        (*(*sp_p_str_current_spanid_)).assign(spanid);
      }
      else{
        sp_p_str_current_spanid_ = new boost::shared_ptr<std::string>
                (boost::make_shared<std::string>());

        (*(*sp_p_str_current_spanid_)).assign(spanid);
      }
    }



};

}

#endif //OCTO_DEVELOP_CTHRIFT_CLIENT_H
