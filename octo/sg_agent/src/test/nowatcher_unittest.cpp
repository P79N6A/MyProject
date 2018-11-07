#include <gtest/gtest.h>
#include "util/whitelist_mgr.h"

class NoWatcherWhiteList : public testing::Test {
 public:
  void SetUp() {
    appkey = "com.sankuai.inf.chenxin11";
    appkey_log = "com.sankuai.inf.logCollector";
    appkey_mtconfig = "com.sankuai.cos.mtconfig";
    appkey_mnsc = "com.sankuai.inf.mnsc";

    EXPECT_EQ(0, operation_.Init());
  }

 protected:
  std::string appkey;
  std::string appkey_log;
  std::string appkey_mtconfig;
  std::string appkey_mnsc;
  WhiteListMgr operation_;
};

TEST_F(NoWatcherWhiteList, isInWhiteList) {
  //not in the whitelist
  EXPECT_FALSE(operation_.IsAppkeyInWhitList(appkey));

  EXPECT_TRUE(operation_.IsAppkeyInWhitList(appkey_log));

  EXPECT_TRUE(operation_.IsAppkeyInWhitList(appkey_mtconfig));
  EXPECT_TRUE(operation_.IsAppkeyInWhitList(appkey_mnsc));
}

