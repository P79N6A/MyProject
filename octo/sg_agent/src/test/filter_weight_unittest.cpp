#include <limits.h>
#include <gtest/gtest.h>
#include "../util/sgagent_filter.h"

using namespace std;

class ServiceGroup : public testing::Test {
 public:
  static void SetUpTestCase() {
    /**
    * @Brief 构造SGService类型
    */
    SGService sg_1;
    sg_1.ip = "192.168.3.163";
    sg_1.port = 8920;
    //sg_1.weight = 10;
    sg_1.appkey = appkey_;

    SGService sg_2;
    sg_2.ip = "192.168.4.163";
    sg_2.port = 8920;
    //sg_2.weight = 10;
    sg_2.appkey = appkey_;

    SGService sg_3;
    sg_3.ip = "192.168.5.163";
    sg_3.port = 8920;
    //sg_3.weight = 10;
    sg_3.appkey = appkey_;
    serviceList.push_back(sg_1);
    serviceList.push_back(sg_2);
    serviceList.push_back(sg_3);
  }

  static void TearDownTestCase() {
  }

  static bool isInServiceList(vector<SGService> &servicelist, const SGService &service) {
    for (vector<SGService>::iterator itor = servicelist.begin(); itor != servicelist.end(); itor++) {
      if (*itor == service)
        return true;
    }
    return false;
  }

 protected:
  static string appkey_;
  static vector<SGService> serviceList;
};

string ServiceGroup::appkey_ = "com.sankuai.inf.logCollector";
vector<SGService> ServiceGroup::serviceList;

TEST_F(ServiceGroup, filterWeightTest) {
  sg_agent::SGAgent_filter::FilterWeight(serviceList);

  EXPECT_EQ(3, serviceList.size());
};

