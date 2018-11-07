#include "util/zk_version_check.h"
#include <gtest/gtest.h>
using namespace sg_agent;

class ZKVersionCheck : public testing::Test {
 public:
  ZkVersionCheck base;
};

TEST_F(ZKVersionCheck, CheckZkVersion) {
  std::string appkey = "test1";
  std::string zk_version = "zk_01";
  std::string version = "01";

  EXPECT_EQ(0, base.CheckZkVersion(appkey, zk_version, version));
  usleep(100);
  EXPECT_EQ(ERR_ZK_LIST_SAME_BUFFER, base.CheckZkVersion(appkey, zk_version, version));

}

