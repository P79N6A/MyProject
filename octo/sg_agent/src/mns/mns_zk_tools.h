// =====================================================================================
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时12分19秒
//       Revision:  none
// 
// 
// =====================================================================================

#ifndef MNS_ZK_TOOLS_H_
#define MNS_ZK_TOOLS_H_

#include <string.h>

#include "util/gen_zk_path_operation.h"
#include "util/whitelist_mgr.h"

class MnsZkTools {
 public:
  static int Init();
  // 判断一个appkey是否需要加watcher
  static WhiteListMgr whiteListMgr;
  //前端
  static SGAgentZkPath operation_;
};

#endif

