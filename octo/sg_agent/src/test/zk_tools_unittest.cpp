#include <gtest/gtest.h>
#include "sg_agent/zk_tools.h"
#include "sg_agent/zk_client.h"
#include "util/global_def.h"
#include "util/idc_util.h"

extern GlobalVar *g_global_var;

class ZkToolsTest : public testing::Test {
 public:
  void SetUp() {
  }
 public:
  tinyxml2::XMLDocument conf_mutable;
  std::string zkPath;
};

TEST_F(ZkToolsTest, file_cfg_whitelist){
  std::string not_appkey = "com.sankuai.octo.tmy";
  std::string yes_appkey = "com.sankuai.tair.qa.function";
  std::string not_appkey1 = "com.sankuai.tairqa.function";
  EXPECT_TRUE(ZkTools::operation_.IsAllEnvFileCfgAppkeys(yes_appkey));
  EXPECT_FALSE(ZkTools::operation_.IsAllEnvFileCfgAppkeys(not_appkey));
  EXPECT_FALSE(ZkTools::operation_.IsAllEnvFileCfgAppkeys(not_appkey1));
}

TEST_F(ZkToolsTest, isInWhiteList) {
  std::string appkey = "com.sankuai.inf.chenxin11";
  std::string appkey_log = "com.sankuai.inf.logCollector";
  std::string appkey_mtconfig = "com.sankuai.cos.mtconfig";
  std::string appkey_mnsc = "com.sankuai.inf.mnsc";

  //not in the whitelist
  EXPECT_FALSE(ZkTools::whiteListMgr.IsAppkeyInWhitList(appkey));

  EXPECT_TRUE(ZkTools::whiteListMgr.IsAppkeyInWhitList(appkey_log));

  EXPECT_TRUE(ZkTools::whiteListMgr.IsAppkeyInWhitList(appkey_mtconfig));
  EXPECT_TRUE(ZkTools::whiteListMgr.IsAppkeyInWhitList(appkey_mnsc));
}

TEST_F(ZkToolsTest, getRootPath) {
  std::string root_path = "/mns/sankuai/prod";

  EXPECT_EQ(root_path, ZkTools::get_root_path());
}

TEST_F(ZkToolsTest, thrift_provider) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "provider";
  std::string appkey = "com.sankuai.inf.chenxin11";
  EXPECT_EQ(0, ZkTools::operation_.GenProtocolZkPath(zkPath, appkey, "thrift", nodeType));

  EXPECT_STREQ("provider", nodeType.c_str());
  std::string result = "/mns/sankuai/" + g_global_var->gEnvStr + "/" + appkey + "/" + "provider";
  EXPECT_STREQ(result.c_str(), zkPath);
}

TEST_F(ZkToolsTest, idc) {
  char host[] =
      "10.4.245.244:2181,10.4.245.245:2181,10.32.245.246:2181,10.1.234.205:2181,10.5.233.208:2181,10.5.232.155:2181";
  std::string zk_host_str = IdcUtil::GetSameIdcZk(host, "10.4.244.156");
  std::string eq_str = "10.4.245.244:2181,10.4.245.245:2181,10.5.233.208:2181,10.5.232.155:2181";
  std::cout << "zk_host_str: " << zk_host_str << std::endl;
  EXPECT_STREQ(eq_str.c_str(), zk_host_str.c_str());

  zk_host_str = IdcUtil::GetSameIdcZk(host, "10.32.244.156");
  eq_str = "10.32.245.246:2181";
  std::cout << "zk_host_str: " << zk_host_str << std::endl;
  EXPECT_STREQ(eq_str.c_str(), zk_host_str.c_str());

  zk_host_str = IdcUtil::GetSameIdcZk(host, "10.12.245.244");
  eq_str =
      "10.4.245.244:2181,10.4.245.245:2181,10.32.245.246:2181,10.1.234.205:2181,10.5.233.208:2181,10.5.232.155:2181";
  std::cout << "zk_host_str: " << zk_host_str << std::endl;
  EXPECT_STREQ(eq_str.c_str(), zk_host_str.c_str());
}
