// =====================================================================================
//
//       Filename:  quota_client.h
//
//    Description:
//
//        Created:  2015-07-20
//       Revision:  none
//
//
// =====================================================================================

#ifndef __quota_client__H__
#define __quota_client__H__

#include <string>
#include <vector>
#include <set>
#include "sgagent_service_types.h"
#include "sgagent_worker_service_types.h"
#include "comm/buffer_mgr.h"
#include "quota_common_types.h"
#include "util/sg_agent_def.h"
#include "sgcommon/common_interface.h"
#include <pthread.h>
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Mutex.h>

#include "quota_zk_client.h"

namespace sg_agent {

class QuotaClient {
 public:
  typedef boost::shared_ptr<quota_res_param_t> QuotaPtr;

  class ZkHandler : public muduo::net::Task {
   public:
    ZkHandler(QuotaClient *client, QuotaPtr quota)
        : client_(client), quota_(quota) {}
    virtual void Run() {
      client_->BackendZkHandler(quota_);
    }
   private:
    QuotaPtr quota_;
    QuotaClient *client_;
  };

  class UpdateHandler : public muduo::net::Task {
   public:
    UpdateHandler(QuotaClient *client) : client_(client) {}
    virtual void Run() {
      client_->UpdateTimer();
    }
   private:
    QuotaClient *client_;
  };

  static QuotaClient *getInstance();
  static void Destroy();

  int init(int timeout = DEFAULT_QUOTA_TIMEOUT);

  int getActions(std::vector<DegradeAction> &actions,
                 const std::string &localAppkey,
                 const std::string &appKey);

  int SendTaskToWorker(const std::string &localAppkey, const std::string &remoteAppkey, const std::string &version);

  //定时更新buffer里的key
  void UpdateTimer();

  //worker thread handle zk
  void BackendZkHandler(QuotaPtr quota);
 private:
  QuotaClient();
  ~QuotaClient();

  inline std::string _genKey(const std::string &localAppkey, const std::string &remoteAppkey) {
    return remoteAppkey;
  }

  int timeout_;

  QuotaZkClient zkClient_;

  static QuotaClient *mQuotaClient;

  muduo::net::IEventLoopThreadProxy *loop_thread_;

  muduo::net::IEventLoopProxy *loop_;

  static BufferMgr<quota_res_param_t> *mBufferMgr;
  /**
   * 用于记录quota的所有key信息
   */
  boost::shared_ptr<std::set<std::string> > keySet_;
  muduo::MutexLock quotaMutexLock_;
};
} //namespace

#endif

