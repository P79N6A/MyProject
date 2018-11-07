#include <gtest/gtest.h>
#include "comm/log4cplus.h"
#include "sgagent_common_types.h"
#include "util/sg_agent_def.h"

using namespace sg_agent;

class SGAgentUnifiedProtocol : public testing::Test {
 public:
  int _unifiedProtocol(SGService &service) {
    if (service.protocol.empty()) {
      if (THRIFT_TYPE == service.serverType) {
        service.protocol = "thrift";
      } else if (HTTP_TYPE == service.serverType) {
        service.protocol = "http";
      } else {
        LOG_ERROR("Appkey: " << service.appkey << " serverType: " << service.serverType << " is wrong!");
        return -1;
      }
    }
    return 0;
  }

  void SetUp() {
  }

 protected:
};

TEST_F(SGAgentUnifiedProtocol, old_thrift) {
  SGService service;
  EXPECT_EQ(0, _unifiedProtocol(service));
  EXPECT_EQ(0, service.serverType);
  EXPECT_STREQ("thrift", service.protocol.c_str());
}

TEST_F(SGAgentUnifiedProtocol, new_thrift) {
  SGService service;
  service.protocol = "thrift";
  EXPECT_EQ(0, _unifiedProtocol(service));
  EXPECT_EQ(0, service.serverType);
  EXPECT_STREQ("thrift", service.protocol.c_str());
}

TEST_F(SGAgentUnifiedProtocol, old_http) {
  SGService service;
  service.serverType = 1;
  EXPECT_EQ(0, _unifiedProtocol(service));
  EXPECT_EQ(1, service.serverType);
  EXPECT_STREQ("http", service.protocol.c_str());
}

TEST_F(SGAgentUnifiedProtocol, new_http) {
  SGService service;
  service.protocol = "http";
  EXPECT_EQ(0, _unifiedProtocol(service));
  EXPECT_STREQ("http", service.protocol.c_str());
}

TEST_F(SGAgentUnifiedProtocol, tair) {
  SGService service;
  service.serverType = 2;
  service.protocol = "tair";
  EXPECT_EQ(0, _unifiedProtocol(service));
  EXPECT_STREQ("tair", service.protocol.c_str());
}

TEST_F(SGAgentUnifiedProtocol, wrong_case) {
  SGService service;
  service.serverType = 2;
  ASSERT_GT(0, _unifiedProtocol(service));
}
