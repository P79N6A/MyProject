// =====================================================================================
// 
// 
// 
// 
// =====================================================================================

#ifndef __QUOTA_ZK_Client__H__
#define __QUOTA_ZK_Client__H__

#include "sgagent_service_types.h"
#include "quota_common_types.h"

#include "util/zk_version_check.h"
#include "zk_client.h"

namespace sg_agent {
class QuotaZkClient {
 public:
  QuotaZkClient();
  ~QuotaZkClient();

  int getDegradeActions(std::vector<DegradeAction> &degradeActions, const std::string &localAppkey,
                        const std::string &appKey, std::string &version);

 public:
  /// quota node change watcher function
  static void quotaWatcher(zhandle_t *zh, int type,
                           int state, const char *path, void *watcherCtx);

 private:

  ZkVersionCheck zk_version_check_;
};
} //namespace

#endif

