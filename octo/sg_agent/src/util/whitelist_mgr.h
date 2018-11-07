#ifndef _whitelist_mgr_H__
#define _whitelist_mgr_H__

#include <set>
#include <string>
#include <muduo/base/Mutex.h>

#include "sgagent_common_types.h"
#include "sg_agent_def.h"

class WhiteListMgr {
 public:
  int Init();
  bool IsAppkeyInWhitList(
      const std::string &appkey);
  bool IsAppkeyInRegistUnlimitWhitList(
      const std::string &appkey);
  void UpdateWhiteList(std::set<std::string>& list);
  void UpdateRegisteList(std::set<std::string>& list);
 private:
  std::set <std::string> whiteList;
  std::set <std::string> registe_unlimit_whitelist_;
};

#endif
