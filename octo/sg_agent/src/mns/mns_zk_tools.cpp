// =====================================================================================
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时43分26秒
//       Revision:  none
// 
// =====================================================================================

#include "comm/log4cplus.h"
#include "mns_zk_tools.h"
#include "util/global_def.h"
extern GlobalVar *g_mns_global_var;

// 判断一个appkey是否需要加watcher
WhiteListMgr MnsZkTools::whiteListMgr;
//前端
SGAgentZkPath MnsZkTools::operation_;

int MnsZkTools::Init() {

  int ret = operation_.Init(g_mns_global_var->gEnvStr);
  if (0 != ret) {
    LOG_ERROR("zk_client load whitelist fail! ret = " << ret);
    return ret;
  }

  ret = whiteListMgr.Init();
  if (0 != ret) {
    LOG_ERROR("zk_client load nowatcherwhitelist fail! ret = "
                  << ret);
    return ret;
  }
  return 0;

}

