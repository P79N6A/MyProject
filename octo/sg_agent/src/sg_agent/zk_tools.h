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

#ifndef __zk_tools__H__
#define __zk_tools__H__

#include <string.h>

#include "comm/tinyxml2.h"
#include "util/gen_zk_path_operation.h"
#include "util/whitelist_mgr.h"

static const int DefaultRetry = 5;

class ZkTools {
 public:

  static int Init(
      tinyxml2::XMLDocument &conf,
      const std::string &root_path);

  // 判断一个appkey是否需要加watcher
  static WhiteListMgr whiteListMgr;
  //前端
  static SGAgentZkPath operation_;

  static int InitZk(tinyxml2::XMLDocument &conf);

  static std::string get_root_path();

 private:
  static std::string root_path_;
};

#endif

