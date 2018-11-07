// =====================================================================================
// 
// 
// 
// 
// =====================================================================================

#ifndef MNS_ZK_CLIENT_H_
#define MNS_ZK_CLIENT_H_
#include <stdio.h>

#include "sgagent_service_types.h"
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/base/CountDownLatch.h>
#include "util/zk_version_check.h"
#include "sgagent_worker_service_types.h"
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
//#include "mns_impl.h"

extern "C" {
#include <zookeeper/zookeeper.h>
#include "comm/cJSON.h"
}

namespace sg_agent {
class MnsImpl;

class ServiceZkClient {
 public:
  ServiceZkClient(MnsImpl *p);
  ~ServiceZkClient();

  int GetAppkeyByServiceName(std::set<std::string> &appkeys,
                             const std::string &localAppkey,
                             const std::string &serviceName,
                             std::string &version,
                             const std::string &protocol);

  int GetSrvListByProtocol(std::vector<SGService> &srvlist,
                           const std::string &localAppkey,
                           const std::string &appKey,
                           std::string &cur_cache_version,
                           const std::string &protocol,
                           bool is_watcher_callback = false);

  int getRouteListByProtocol(std::vector<CRouteData> &routeList,
                             const std::string &localAppkey,
                             const std::string &appKey,
                             std::string &version,
                             const std::string &protocol);

  int GetAppkeyDesc(AppkeyDescResponse &desc_res,const std::string &appkey);

  /// TODO:All service change watcher function
  static void ServiceByProtocolWatcher(zhandle_t *zh, int type,
                                       int state, const char *path, void *watcherCtx);


 private:
  ZkVersionCheck zk_version_check_;
  ZkVersionCheck zk_version_check_route_;
  MnsImpl *plugin;
	static muduo::net::EventLoopThread s_watcher_thread;
  static muduo::net::EventLoop *s_watcher_loop;
};
}

#endif

