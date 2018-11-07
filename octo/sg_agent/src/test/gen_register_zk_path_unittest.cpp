#include <gtest/gtest.h>
#include "util/gen_zk_path_operation.h"
#include "util/global_def.h"

extern GlobalVar *g_global_var;
class SGAgentGenRegisterZkPath : public testing::Test {
 public:
  void SetUp() {
    m_root_path = "/mns/sankuai/test";
    appkey = "com.sankuai.inf.chenxin11";

    provider = m_root_path + "/" + appkey + "/provider";
    http = m_root_path + "/" + appkey + "/provider-http";
    tair = m_root_path + "/" + appkey + "/providers/tair";
    other = m_root_path + "/" + appkey + "/providers/other";

    operation_.Init(g_global_var->gEnvStr);
    m_SGAgentZkPath_test_env.Init("test");
  }

 protected:
  std::string m_root_path;
  std::string appkey;

  std::string provider;
  std::string http;
  std::string tair;
  std::string other;

  SGAgentZkPath operation_;
  SGAgentZkPath m_SGAgentZkPath_test_env;
};

TEST_F(SGAgentGenRegisterZkPath, test_genDescZkPath_env) {
  char zk_path[MAX_BUF_SIZE] = {0};
	std::string castle_appkey("com.sankuai.inf.mafka.castleocto");
  int ret =
      m_SGAgentZkPath_test_env.genDescZkPath(zk_path, castle_appkey);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str(zk_path);

  EXPECT_FALSE(zk_path_str == "/mns/sankuai/test/com.sankuai.inf.mafka.castleocto/desc");

  memset(zk_path, 0, MAX_BUF_SIZE);
	std::string octo_tmy_appkey("com.sankuai.octo.tmy");
  ret = m_SGAgentZkPath_test_env.genDescZkPath(zk_path, octo_tmy_appkey);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str1(zk_path);
  EXPECT_TRUE(zk_path_str1 == "/mns/sankuai/prod/com.sankuai.octo.tmy/desc");
}
TEST_F(SGAgentGenRegisterZkPath, test_genServiceNameZkPath_env) {
  char zk_path[MAX_BUF_SIZE] = {0};
	std::string castle_appkey("com.sankuai.inf.mafka.castleocto");
	std::string servicename("test_service_name");
	std::string protocol("thrift");
  int ret =
      m_SGAgentZkPath_test_env.genServiceNameZkPathNode(zk_path, servicename, protocol, castle_appkey);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str(zk_path);

  EXPECT_TRUE(zk_path_str == "/mns/service/prod/test_service_name/thrift");

  memset(zk_path, 0, MAX_BUF_SIZE);
	std::string octo_tmy_appkey("com.sankuai.octo.tmy");
  ret = m_SGAgentZkPath_test_env.genServiceNameZkPathNode(zk_path, servicename, protocol, octo_tmy_appkey);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str1(zk_path);
	std::cout << zk_path_str1 << std::endl;
  EXPECT_TRUE(zk_path_str1 == "/mns/service/test/test_service_name/thrift");
}
TEST_F(SGAgentGenRegisterZkPath, test_genRegisterZkPath_env) {
  char zk_path[MAX_BUF_SIZE] = {0};
	std::string castle_appkey("com.sankuai.inf.mafka.castleocto");
	std::string protocol("thrift");
	int server_type = 1;
  int ret =
      m_SGAgentZkPath_test_env.genRegisterZkPath(zk_path, castle_appkey, protocol, server_type);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str(zk_path);

  EXPECT_FALSE(zk_path_str == "/mns/sankuai/test/com.sankuai.inf.mafka.castleocto/provider");

  memset(zk_path, 0, MAX_BUF_SIZE);
	std::string octo_tmy_appkey("com.sankuai.octo.tmy");
  ret = m_SGAgentZkPath_test_env.genRegisterZkPath(zk_path, octo_tmy_appkey, protocol, server_type);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str1(zk_path);
  EXPECT_TRUE(zk_path_str1 == "/mns/sankuai/test/com.sankuai.octo.tmy/provider");
}
TEST_F(SGAgentGenRegisterZkPath, test_GenProtocolZkPath_env) {
  char zk_path[MAX_BUF_SIZE] = {0};
	std::string castle_appkey("com.sankuai.inf.mafka.castleocto");
	std::string protocol("thrift");
	std::string provider("provider");
  int ret =
      m_SGAgentZkPath_test_env.GenProtocolZkPath(zk_path, castle_appkey, protocol, provider);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str(zk_path);

  EXPECT_FALSE(zk_path_str == "/mns/sankuai/test/com.sankuai.inf.mafka.castleocto/provider");

  memset(zk_path, 0, MAX_BUF_SIZE);
	std::string octo_tmy_appkey("com.sankuai.octo.tmy");
  ret = m_SGAgentZkPath_test_env.GenProtocolZkPath(zk_path, octo_tmy_appkey, protocol, provider);
  EXPECT_EQ(SUCCESS, ret);

  std::string zk_path_str1(zk_path);
  EXPECT_TRUE(zk_path_str1 == "/mns/sankuai/test/com.sankuai.octo.tmy/provider");
}

TEST_F(SGAgentGenRegisterZkPath, thrift_type_newInterface) {
  char zkPath[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath, appkey, "thrift", 0));
  //EXPECT_STREQ(provider.c_str(), zkPath);

  //验证serverType无关
  char zkPath_2[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath_2, appkey, "thrift", 1));
  //EXPECT_STREQ(provider.c_str(), zkPath_2);
}

TEST_F(SGAgentGenRegisterZkPath, thrift_type_oldInterface) {
  char zkPath[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath, appkey, "", 0));
}

TEST_F(SGAgentGenRegisterZkPath, http_type_newInterface) {
  //验证serverType无关
  char zkPath[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath, appkey, "http", 0));
  //EXPECT_STREQ(http.c_str(), zkPath);

  char zkPath_2[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath_2, appkey, "http", 1));
  //EXPECT_STREQ(http.c_str(), zkPath_2);
}

TEST_F(SGAgentGenRegisterZkPath, http_type_oldInterface) {
  char zkPath[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath, appkey, "", 1));
//  EXPECT_STREQ(http.c_str(), zkPath);
}

TEST_F(SGAgentGenRegisterZkPath, tair_protocol) {
  char zkPath[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath, appkey, "tair", 0));
 // EXPECT_STREQ(tair.c_str(), zkPath);

  char zkPath_2[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath_2, appkey, "tair", 1));
 // EXPECT_STREQ(tair.c_str(), zkPath_2);
}

TEST_F(SGAgentGenRegisterZkPath, other_protocol) {
  char zkPath[MAX_BUF_SIZE] = {0};
  EXPECT_LE(0, operation_.genRegisterZkPath(zkPath, appkey, "other", 2));
 // EXPECT_STREQ(other.c_str(), zkPath);
}

TEST_F(SGAgentGenRegisterZkPath, servicename_node) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string appkey = "com.sankuai.inf.sg_agent";
  std::string serviceName = "sayHello";

  EXPECT_LE(0, operation_.genServiceNameZkPathNode(zkPath,
                                                   serviceName,
                                                   "thrift",
                                                   appkey));
  std::cout << "zkPath = " << zkPath << std::endl;
  EXPECT_STREQ(zkPath, "/mns/service/prod/sayHello/thrift");

  EXPECT_LE(0, operation_.genServiceNameZkPathNode(zkPath,
                                                   serviceName,
                                                   "http",
                                                   appkey));
  EXPECT_STREQ(zkPath, "/mns/service/prod/sayHello/http");
}

TEST_F(SGAgentGenRegisterZkPath, servicename) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string appkey = "com.sankuai.inf.sg_agent";
  std::string serviceName = "sayHello";

  EXPECT_LE(0, operation_.genServiceNameZkPath(zkPath,
                                               serviceName,
                                               appkey));
  std::cout << "zkPath = " << zkPath << std::endl;
  EXPECT_STREQ(zkPath, "/mns/service/prod/sayHello");

  EXPECT_LE(0, operation_.genServiceNameZkPath(zkPath,
                                               serviceName,
                                               appkey));
  EXPECT_STREQ(zkPath, "/mns/service/prod/sayHello");
}
