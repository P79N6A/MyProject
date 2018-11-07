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

class CthriftClient {
 private:
  static const int32_t kI32DefaultTimeoutMS;

  std::string str_svr_appkey_;
  std::string str_cli_appkey_;
  int32_t i32_timeout_ms_;   //TODO specify timeout differ by interface

  static __thread boost::shared_ptr <CthriftTBinaryProtoWithCthriftTrans>
      *p_sp_cthrift_tbinary_protocol_;

  static __thread boost::shared_ptr <CthriftTransport> *p_sp_cthrift_transport_;

  boost::shared_ptr <CthriftClientWorker> sp_cthrift_client_worker_;

 public:
  CthriftClient(const std::string &str_svr_appkey,
                const std::string &str_cli_appkey,
                const int32_t &i32_timeout);

  ~CthriftClient(void) {
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

  string GetEnvInfo(void) const{
    return cthrift::CthriftSgagent::str_env_;
  }
};

}

#endif //OCTO_DEVELOP_CTHRIFT_CLIENT_H
