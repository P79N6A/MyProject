#include <limits.h>
#include <gtest/gtest.h>
#include "../sg_agent/status_mgr.h"

using namespace std;

class StatusMgrTest : public testing::Test {
 public:
  static void SetUpTestCase() {
    statusMgr = new sg_agent::StatusMgr();

    service.appkey = "com.sankuai.octo.mnscheck";
    service.version = "mtthrift-v1.6.4";
    service.ip = "10.4.245.3";
    service.port = 5266;
    service.weight = 10;
    service.role = 0;
    service.envir = 2;
    service.fweight = 10.0;
    service.status = 2;
  }

  static void TearDownTestCase() {
  }

 protected:
  static sg_agent::StatusMgr *statusMgr;
  static SGService service;
};

sg_agent::StatusMgr *StatusMgrTest::statusMgr;
SGService StatusMgrTest::service;

TEST_F(StatusMgrTest, NeedToSwitch) {
  // 范围内版本
  bool ret = statusMgr->NeedToSwitch(service);
  EXPECT_EQ(true, ret);
  service.version = "mtthrift-v1.6.4-SNAPSHOT";
  ret = statusMgr->NeedToSwitch(service);
  EXPECT_EQ(true, ret);

  // 范围外版本
  service.version = "mtthrift-v1.7.0";
  ret = statusMgr->NeedToSwitch(service);
  EXPECT_EQ(false, ret);
  service.version = "mtthrift-v1.7.0-SNAPSHOT";
  ret = statusMgr->NeedToSwitch(service);
  EXPECT_EQ(false, ret);
  // pigeon
  service.version = "1.7.0";
  ret = statusMgr->NeedToSwitch(service);
  EXPECT_EQ(false, ret);
};

TEST_F(StatusMgrTest, SwitchStatusPortOK) {
  // 端口OK + 范围内版本
	
	int origin_port = service.port;
	service.__set_port(5268);
	std::string origin_ip = service.ip;
	service.__set_ip("127.0.0.1");

  service.status = 0;
  service.version = "mtthrift-v1.6.4";
  int ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(2, service.status);
  service.status = 2;
  ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(2, service.status);
  // 端口OK + 范围外版本
  service.status = 0;
  service.version = "mtthrift-v1.7.0-SNAPSHOT";
  ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(0, service.status);
  service.status = 2;
  service.version = "mtthrift-v1.7.0-SNAPSHOT";
  ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(2, service.status);
	service.__set_port(origin_port);
	service.__set_ip(origin_ip);
}

TEST_F(StatusMgrTest, SwitchStatusPortNotOK) {
  // 端口不OK + 范围内版本
  service.port = 5367;
  service.status = 0;
  service.version = "mtthrift-v1.6.4";
  int ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(0, service.status);

  service.status = 2;
  service.version = "mtthrift-v1.6.4";
  service.ip = "10.4.245.3";
  ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(0, service.status);



  // 端口不OK + 范围外版本
  service.status = 0;
  service.version = "mtthrift-v1.7.0-SNAPSHOT";
  ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(0, service.status);
  service.status = 2;
  service.version = "mtthrift-v1.7.0-SNAPSHOT";
  ret = statusMgr->SwitchStatus(service);
  EXPECT_EQ(2, service.status);
};
