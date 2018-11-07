#include <gtest/gtest.h>
#include "util/appkey_path_operation.h"
#include <boost/algorithm/string/trim.hpp>

class SGAgentDeGenZkPath : public testing::Test {
 public:
  void SetUp() {
    protocol_ = "thrift";
    remoteAppkey_ = "com.sankuai.inf.logCollector";
  }

 protected:
  std::string protocol_;
  std::string remoteAppkey_;
};

TEST_F(SGAgentDeGenZkPath, parse_xml) {
  std::string char_with_empty = "        ";
  boost::trim(char_with_empty);
  EXPECT_TRUE("" == char_with_empty);
  std::string char_with_empty1 = "    s    ";
  boost::trim(char_with_empty1);
  EXPECT_TRUE("s" == char_with_empty1);
}

TEST_F(SGAgentDeGenZkPath, thrift_provider) {
  const char *zkPath = "/mns/sankuai/prod/com.sankuai.inf.logCollector/provider";
  std::string appkey = "";
  std::string protocol = "";
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ("thrift", protocol.c_str());
}

TEST_F(SGAgentDeGenZkPath, http_provider) {
  const char *zkPath = "/mns/sankuai/prod/com.sankuai.inf.logCollector/provider-http";
  std::string appkey = "";
  std::string protocol = "";
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ("http", protocol.c_str());
}

TEST_F(SGAgentDeGenZkPath, cellar_provider) {
  const char *zkPath = "/mns/sankuai/prod/com.sankuai.inf.logCollector/providers/cellar";
  std::string appkey = "";
  std::string protocol = "";
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ("cellar", protocol.c_str());
}

TEST_F(SGAgentDeGenZkPath, thrift_route) {
  const char *zkPath = "/mns/sankuai/prod/com.sankuai.inf.logCollector/route";
  std::string appkey = "";
  std::string protocol = "";
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ("thrift", protocol.c_str());
}

TEST_F(SGAgentDeGenZkPath, http_route) {
  const char *zkPath = "/mns/sankuai/prod/com.sankuai.inf.logCollector/route-http";
  std::string appkey = "";
  std::string protocol = "";
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ("http", protocol.c_str());
}

TEST_F(SGAgentDeGenZkPath, cellar_route) {
  const char *zkPath = "/mns/sankuai/prod/com.sankuai.inf.logCollector/providers/cellar";
  std::string appkey = "";
  std::string protocol = "";
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ("cellar", protocol.c_str());
}

TEST_F(SGAgentDeGenZkPath, invalid_path) {
  std::string appkey = "";
  std::string protocol = "";
  //empty
  const char *zkPath = "";
  EXPECT_EQ(-1, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  //not complete
  zkPath = "/mns/sankuai/prod";
  EXPECT_EQ(-2, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));

  zkPath = "/mns/sankuai/prod////";
  EXPECT_EQ(-3, sg_agent::SGAgentAppkeyPath::deGenZkPath(zkPath, appkey, protocol));
}
