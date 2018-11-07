// =====================================================================================
//
//      Filename:  log_collector.h
//
//      Description: Send log info to server.
//
//      Version:  1.0
//      Created:  2015-04-18
//      Revision:  none
//
//
// =====================================================================================

#ifndef __COMMON_LOGCOLLECTOR_H__
#define __COMMON_LOGCOLLECTOR_H__
#include "sgagent_service_types.h"
#include "util/sg_agent_def.h"
#include "remote_log_collector.h"
#include "sgcommon/common_interface.h"

namespace sg_agent {
class CommonLogCollector {
 public:

  class CommonLogHandler : public muduo::net::Task {
   public:
    CommonLogHandler(CommonLogCollector *client, CommonLog content)
        : client_(client), content_(content) {}
    virtual void Run() {
      client_->CommonLogBackendHandler(content_);
    }
   private:
    CommonLog content_;
    CommonLogCollector *client_;
  };

  class ModuleInvokerHandler : public muduo::net::Task {
   public:
    ModuleInvokerHandler(CommonLogCollector *client, SGModuleInvokeInfo info)
        : client_(client), info_(info) {}
    virtual void Run() {
      client_->ModuleInvokerBackendHandler(info_);
    }
   private:
    SGModuleInvokeInfo info_;
    CommonLogCollector *client_;
  };

  /**
   * @brief:  To init MQ, retry_times. Need be called before sendLogs.
   * @return: 0 is OK, other is ErrorCode.
   */
  int init(int retry = 3);

  /**
   * @brief:  To send Logs to Server.
   * @return: 0 is OK( which means succeed to insert message into MQ).
   *          Other is ErrorCode.
   */
  int sendCommonLogs(const CommonLog &oCommonLog);
  /**
   * @brief:  To send ModuleInvokeInfo to Server.
   * @return: 0 is OK( which means succeed to insert message into MQ).
   *          Other is ErrorCode.
   */
  int sendModuleInvokeInfo(const SGModuleInvokeInfo &oInfo);

  void CommonLogBackendHandler(CommonLog oCommonLog);

  void ModuleInvokerBackendHandler(SGModuleInvokeInfo oInfo);

  /**
   * @brief:  To get Collector's Singleton
   * @return: NULL means it's failed to get Singleton, maybe you need do
   *          init function firstly.
   */
  static CommonLogCollector *getInstance();

  static void Destroy();
 private:
  CommonLogCollector();
  ~CommonLogCollector();

 private:
  int retry_;
  static CommonLogCollector *mCommonLogCollector;

  muduo::net::IEventLoopThreadProxy *loop_thread_;
  muduo::net::IEventLoopProxy *loop_;

  RemoteLogCollector logCollector_;
};

} // namespace

#endif
