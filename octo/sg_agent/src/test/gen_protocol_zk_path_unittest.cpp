#include <gtest/gtest.h>
#include "util/gen_zk_path_operation.h"
#include "util/global_def.h"

extern GlobalVar *g_global_var;

class SGAgentGenZkPath : public testing::Test {
 public:
  void SetUp() {
    appkey = "com.sankuai.inf.chenxin11";
    m_root_path = "/mns/sankuai/" + g_global_var->gEnvStr;

    operation_.Init(g_global_var->gEnvStr);
  }

 protected:
  std::string m_root_path;
  std::string appkey;

  SGAgentZkPath operation_;
};

TEST_F(SGAgentGenZkPath, thrift_provider) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "provider";
  EXPECT_EQ(0, operation_.GenProtocolZkPath(zkPath, appkey, "thrift", nodeType));

  EXPECT_STREQ("provider", nodeType.c_str());
  std::string result = m_root_path + "/" + appkey + "/" + "provider";
  EXPECT_STREQ(result.c_str(), zkPath);
}

TEST_F(SGAgentGenZkPath, thrift_route) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "route";
  EXPECT_EQ(0, operation_.GenProtocolZkPath(zkPath, appkey, "thrift", nodeType));

  EXPECT_STREQ("route", nodeType.c_str());
  std::string result = m_root_path + "/" + appkey + "/" + "route";
  EXPECT_STREQ(result.c_str(), zkPath);
}

TEST_F(SGAgentGenZkPath, http_provider) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "provider";
  EXPECT_EQ(0, operation_.GenProtocolZkPath(zkPath, appkey, "http", nodeType));

  EXPECT_STREQ("provider-http", nodeType.c_str());
  std::string result = m_root_path + "/" + appkey + "/" + "provider-http";
  EXPECT_STREQ(result.c_str(), zkPath);
}

TEST_F(SGAgentGenZkPath, http_route) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "route";
  EXPECT_EQ(0, operation_.GenProtocolZkPath(zkPath, appkey, "http", nodeType));

  EXPECT_STREQ("route-http", nodeType.c_str());
  std::string result = m_root_path + "/" + appkey + "/" + "route-http";
  EXPECT_STREQ(result.c_str(), zkPath);
}

TEST_F(SGAgentGenZkPath, tair_provider) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "provider";
  EXPECT_EQ(0, operation_.GenProtocolZkPath(zkPath, appkey, "tair", nodeType));

  EXPECT_STREQ("providers/tair", nodeType.c_str());
  std::string result = m_root_path + "/" + appkey + "/" + "providers/tair";
  EXPECT_STREQ(result.c_str(), zkPath);
}

TEST_F(SGAgentGenZkPath, tair_route) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "route";
  EXPECT_EQ(0, operation_.GenProtocolZkPath(zkPath, appkey, "tair", nodeType));

  EXPECT_STREQ("routes/tair", nodeType.c_str());
  std::string result = m_root_path + "/" + appkey + "/" + "routes/tair";
  EXPECT_STREQ(result.c_str(), zkPath);
}

TEST_F(SGAgentGenZkPath, protocol_empty) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "tair";
  EXPECT_EQ(-1, operation_.GenProtocolZkPath(zkPath, appkey, "", nodeType));
}

TEST_F(SGAgentGenZkPath, ServiceNamePath) {
  char zkPath[MAX_BUF_SIZE] = {0};
  char zkPathExpect[MAX_BUF_SIZE] = {0};
  std::string serviceName
      = "com.meituan.mtthrift.test.TestService";
  std::string appkey = "com.sankuai.inf.sg_agent";
  snprintf(zkPathExpect, sizeof(zkPathExpect),
           "/mns/service/%s/%s", "prod", serviceName.c_str());
  EXPECT_EQ(0,
            operation_.genServiceNameZkPath(zkPath,
                                            serviceName));
  EXPECT_STREQ(zkPathExpect, zkPath);

  EXPECT_EQ(0,
            operation_.genServiceNameZkPath(zkPath,
                                            serviceName, appkey));
  EXPECT_STREQ(zkPathExpect, zkPath);
}
TEST_F(SGAgentGenZkPath, ServiceNameHttpPath) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string serviceName = "http://service.dianping.com/memberOSSService/utmService_1.0.0";
  std::string appkey = "com.sankuai.inf.sg_agent";

  EXPECT_EQ(0, operation_.genServiceNameZkPath(zkPath, serviceName, appkey));
  EXPECT_STREQ(zkPath, "/mns/service/prod/http:^^service.dianping.com^memberOSSService^utmService_1.0.0");
}

TEST_F(SGAgentGenZkPath, ReplaceHttpServiceName) {
  std::string http_servicename = "http://service.dianping.com/weixinService/weiXinMessageService_1.0.0";
  std::string expect_http_servicename = "http:^^service.dianping.com^weixinService^weiXinMessageService_1.0.0";

  EXPECT_STREQ(expect_http_servicename.c_str(), operation_.ReplaceHttpServiceName(http_servicename).c_str());
}
