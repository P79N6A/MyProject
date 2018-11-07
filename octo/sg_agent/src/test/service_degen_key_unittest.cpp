#include <gtest/gtest.h>
#include "util/appkey_path_operation.h"

class SGAgentDeGenService : public testing::Test {
 public:
  void SetUp() {
    protocol_ = "thrift";
    remoteAppkey_ = "com.sankuai.inf.logCollector";
  }

 protected:
  std::string protocol_;
  std::string remoteAppkey_;
};

TEST_F(SGAgentDeGenService, normal) {
  std::string key = protocol_ + "+" + remoteAppkey_;
  std::string appkey;
  std::string protocol;
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::_deGenKey(key, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ(protocol_.c_str(), protocol.c_str());
}

TEST_F(SGAgentDeGenService, appkey_empty) {
  std::string key = protocol_ + "+";
  std::string appkey;
  std::string protocol;
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::_deGenKey(key, appkey, protocol));

  EXPECT_STREQ("", appkey.c_str());
  EXPECT_STREQ(protocol_.c_str(), protocol.c_str());
}

TEST_F(SGAgentDeGenService, thrift_empty) {
  std::string key = "+" + remoteAppkey_;
  std::string appkey;
  std::string protocol;
  EXPECT_EQ(0, sg_agent::SGAgentAppkeyPath::_deGenKey(key, appkey, protocol));

  EXPECT_STREQ(remoteAppkey_.c_str(), appkey.c_str());
  EXPECT_STREQ("", protocol.c_str());
}

TEST_F(SGAgentDeGenService, invalid_key) {
  std::string key = protocol_ + remoteAppkey_;
  std::string appkey;
  std::string protocol;
  EXPECT_EQ(-1, sg_agent::SGAgentAppkeyPath::_deGenKey(key, appkey, protocol));
}
