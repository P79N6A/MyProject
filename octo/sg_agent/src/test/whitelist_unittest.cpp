#include <gtest/gtest.h>
#include "util/gen_zk_path_operation.h"
#include "util/global_def.h"

extern GlobalVar* g_global_var;
class SGAgentWhiteList : public testing::Test {
 public:
  void SetUp() {
    appkey = "com.sankuai.inf.chenxin11";
    appkey_castle = "com.sankuai.inf.mafka.castleocto";
    appkey_cellar = "com.sankuai.cellar.config.waimai";

    EXPECT_EQ(0, operation_.Init(g_global_var->gEnvStr));
  }

 protected:
  std::string appkey;
  std::string appkey_castle;
  std::string appkey_cellar;
  SGAgentZkPath operation_;
};

TEST_F(SGAgentWhiteList, isInWhiteList) {
  //not in the whitelist
  EXPECT_FALSE(operation_.IsInWhiteList(appkey));

  EXPECT_TRUE(operation_.IsInWhiteList(appkey_castle));

  EXPECT_TRUE(operation_.IsInWhiteList(appkey_cellar));
}

