#include <limits.h>
#include <gtest/gtest.h>
#include "util/sgagent_filter.h"
#include "comm/log4cplus.h"

using namespace std;

class Swimlane : public testing::Test {
 public:
  static void SetUpTestCase() {
    /**
    * @Brief 构造SGService类型
    */
    SGService sg_1;
    sg_1.ip = "192.168.3.163";
    sg_1.port = 8920;
    sg_1.__set_swimlane("sw_1");
    sg_1.appkey = appkey_;

    SGService sg_2;
    sg_2.ip = "192.168.4.163";
    sg_2.port = 8920;
    sg_2.__set_swimlane("sw_2");
    sg_2.appkey = appkey_;

    SGService sg_3;
    sg_3.ip = "192.168.5.163";
    sg_3.port = 8920;
    sg_3.appkey = appkey_;
    serviceList.push_back(sg_1);
    serviceList.push_back(sg_2);
    serviceList.push_back(sg_3);
  }

  static void TearDownTestCase() {
  }
 protected:
  static string appkey_;
  static vector<SGService> serviceList;
};

string Swimlane::appkey_ = "com.sankuai.inf.logCollector";
vector<SGService> Swimlane::serviceList;

TEST_F(Swimlane, FilterOneSwimlane) {
  EXPECT_EQ(3, serviceList.size());

  std::vector<SGService> svrList;
  EXPECT_EQ(0, sg_agent::SGAgent_filter::FilterSwimlane(&svrList, serviceList, "sw_1"));
  EXPECT_EQ(1, svrList.size());
}

TEST_F(Swimlane, FilterNoSwimlane) {
  EXPECT_EQ(3, serviceList.size());

  std::vector<SGService> srvList;
  EXPECT_EQ(0, sg_agent::SGAgent_filter::FilterSwimlane(&srvList, serviceList, ""));
	LOG_INFO("the node number after FilterSwimlane is " << srvList.size());
  EXPECT_EQ(1, srvList.size());
}

TEST_F(Swimlane, FilterWrongSwimlane) {
  EXPECT_EQ(3, serviceList.size());

  std::vector<SGService> svrList;
  EXPECT_EQ(-1, sg_agent::SGAgent_filter::FilterSwimlane(&svrList, serviceList, "sw"));
  EXPECT_EQ(0, svrList.size());
}

TEST_F(Swimlane, EraseSwimlane) {
  EXPECT_EQ(3, serviceList.size());

  sg_agent::SGAgent_filter::DeleteNodeWithSwimlane(serviceList);
  EXPECT_EQ(1, serviceList.size());
}
