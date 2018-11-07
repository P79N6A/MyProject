#include <limits.h>
#include <gtest/gtest.h>
#include "util/sgagent_filter.h"
#include "SGAgent.h"
#include "util/sg_agent_def.h"

using namespace std;

const std::string Appkey = "com.sankuai.octo.routeCheck";
class FilterCommonTest : public testing::Test {
 public:
  static void SetUpTestCase() {
    insertSvr(baseSvrList, "10.1.102.14");
    insertSvr(baseSvrList, "10.1.102.48");

    autoIdcRoute.category = 1;
    autoIdcRoute.priority = 0;
    autoIdcRoute.status = 1;
    routeList.push_back(autoIdcRoute);
  }

  static void TearDownTestCase() {
  }

 protected:
  static void insertSvr(std::vector<SGService> &svrList,
                        std::string ip, double fweight = 10, int status = 2, int role = 0);
  static std::vector<SGService> baseSvrList;

  static CRouteData autoIdcRoute;
  static std::vector<CRouteData> routeList;
};

std::vector<SGService> FilterCommonTest::baseSvrList;

CRouteData FilterCommonTest::autoIdcRoute;
std::vector<CRouteData> FilterCommonTest::routeList;

void FilterCommonTest::insertSvr(std::vector<SGService> &svrList,
                                 std::string ip, double fweight, int status, int role) {
  SGService svr;
  svr.ip = ip;
  svr.port = 5266;
  svr.status = status;
  svr.role = role;
  svr.appkey = Appkey;
  svr.weight = 10;
  svr.fweight = fweight;
  svrList.push_back(svr);
}


TEST_F(FilterCommonTest, filterWeight) {
  std::vector<SGService> svrList = baseSvrList;
  insertSvr(svrList, "10.16.102.14");
  insertSvr(svrList, "10.32.100.100");
  insertSvr(svrList, "10.32.4.5");
  insertSvr(svrList, "10.4.101.11", 0.01, 2, 1);
  insertSvr(svrList, "10.4.101.10", 0.001, 2, 1);
  insertSvr(svrList, "10.4.101.10", 0.00001, 2, 1);

  int ret = sg_agent::SGAgent_filter::FilterWeight(svrList, sg_agent::IdcThresHold);

  EXPECT_EQ(0, ret);
  EXPECT_EQ(5, svrList.size());

  insertSvr(svrList, "10.4.101.11", 0.01, 2, 1);
  insertSvr(svrList, "10.4.101.10", 0.001, 2, 1);
  insertSvr(svrList, "10.4.101.10", 0.00001, 2, 1);

  ret = sg_agent::SGAgent_filter::FilterWeight(svrList, sg_agent::RegionThresHold);

  EXPECT_EQ(0, ret);
  EXPECT_EQ(7, svrList.size());
};

TEST_F(FilterCommonTest, getValue) {
  std::string reserved = "route_limit:0";
  std::string value = sg_agent::SGAgent_filter::getValue(reserved, sg_agent::LIMIT_KEY, "|", ":");
  EXPECT_EQ(sg_agent::UNLIMIT_VALUE, value);

  reserved = "route_limit:1";
  value = sg_agent::SGAgent_filter::getValue(reserved, sg_agent::LIMIT_KEY, "|", ":");
  EXPECT_EQ(sg_agent::LIMIT_VALUE, value);

  reserved = "kv:xx|route_limit:1";
  value = sg_agent::SGAgent_filter::getValue(reserved, sg_agent::LIMIT_KEY, "|", ":");
  EXPECT_EQ(sg_agent::LIMIT_VALUE, value);

  reserved = "route_limit:1|kv:xx";
  value = sg_agent::SGAgent_filter::getValue(reserved, sg_agent::LIMIT_KEY, "|", ":");
  EXPECT_EQ(sg_agent::LIMIT_VALUE, value);

  reserved = "kv:xx|route_limit:1|kv1:xx";
  value = sg_agent::SGAgent_filter::getValue(reserved, sg_agent::LIMIT_KEY, "|", ":");
  EXPECT_EQ(sg_agent::LIMIT_VALUE, value);
}
