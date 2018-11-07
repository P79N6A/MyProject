// =====================================================================================
// 
//       Filename:  zk_client.h
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时12分19秒
//       Revision:  none
// 
// 
// =====================================================================================

#ifndef UTIL_ZK_VERSION_CHECK_H_
#define UTIL_ZK_VERSION_CHECK_H_

#include <string.h>
#include "comm/buffer_mgr.h"

namespace sg_agent {
class ZkVersionCheck {
 public:
  ZkVersionCheck();
  ~ZkVersionCheck();

  int CheckZkVersion(const std::string &appkey, const std::string &zk_version, std::string &version);

 private:
  boost::shared_ptr <BufferMgr<int> > key_buffer_;

  int zk_update_time_;
  int zk_update_rand_time_;
};
}

#endif

