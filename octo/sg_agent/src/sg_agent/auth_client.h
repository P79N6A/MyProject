// =====================================================================================
//
//       Filename:  auth_client.h
//
//    Description:
//
//        Created:  2015-07-20
//       Revision:  none
//
//
// =====================================================================================

#ifndef SGAGENT_AUTH_CLIENT_H_
#define SGAGENT_AUTH_CLIENT_H_

#include <string>
#include <vector>
#include <set>
#include "util/sg_agent_def.h"
#include "sgagent_service_types.h"
#include "sgagent_worker_service_types.h"

extern "C" {
#include <zookeeper/zookeeper.h>
#include "comm/cJSON.h"
}
#include "zk_client.h"
#include "sgcommon/common_interface.h"

#include "comm/buffer_mgr.h"
#include <pthread.h>
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Mutex.h>

namespace sg_agent {

class AuthClient {
 public:
  typedef boost::shared_ptr<getauth_res_param_t> AuthPtr;

  class AuthTask : public muduo::net::Task {
   public:
    AuthTask(AuthClient *client, AuthPtr auth)
        : client_(client), auth_(auth) {}
    virtual void Run() {
      client_->BackendZkHandler(auth_);
    }
   private:
    AuthPtr auth_;
    AuthClient *client_;
  };

  class AuthTimer : public muduo::net::Task {
   public:
    AuthTimer(AuthClient *client) : client_(client) {}
    virtual void Run() {
      client_->UpdateTimer();
    }
   private:
    AuthClient *client_;
  };

  static AuthClient *getInstance();
  static void Destroy();

  int init(int timeout = DEFAULT_AUTH_TIMEOUT);

  int getAuthorized(int role, std::string &_return, const std::string &targetAppkey);

  int SendTaskToWorker(int role, const std::string &targetAppkey, const std::string &version);

  //定时更新buffer里的key
  void UpdateTimer();

  //worker thread handle zk
  void BackendZkHandler(AuthPtr auth);
 private:
  AuthClient();
  ~AuthClient();

  //生成buffer key
  inline std::string _genKey(int role, const std::string &targetAppkey) {
    std::string name = "";
    if (AUTH_PROVIDER == role) {
      name = "provider";
    } else if (AUTH_CONSUMER == role) {
      name = "consumer";
    } else {
      LOG_ERROR("Auth role is not correct; role = " << role);
    }

    return name + "_" + targetAppkey;
  }

  int GetFromZk(AuthPtr auth);
 private:
  int timeout_;

  static AuthClient *mAuthClient;

  muduo::net::IEventLoopThreadProxy *loop_thread_;

  muduo::net::IEventLoopProxy *loop_;

  static BufferMgr<getauth_res_param_t> *mBufferMgr;
  /**
   * 用于记录auth的所有key信息
   */
  boost::shared_ptr<std::set<std::string> > keySet_;
  muduo::MutexLock authMutexLock_;
};
} //namespace


#endif

