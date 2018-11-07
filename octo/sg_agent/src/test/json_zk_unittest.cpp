#include <limits.h>
#include <gtest/gtest.h>
#include "util/json_zk_mgr.h"
#include "util/SGAgentErr.h"

using namespace std;

class JsonZkMgrTest : public testing::Test {
 public:
  static void SetUpTestCase() {
  }

  static void TearDownTestCase() {
  }

 protected:
};

TEST_F(JsonZkMgrTest, Json2RouteData) {
  CRouteData routeData;
  string strJson = "";
  EXPECT_EQ(ERR_JSON_TO_DATA_FAIL, JsonZkMgr::Json2RouteData(strJson, routeData));

  strJson =
      "{\"id\":\"default-yf\",\"name\":\"动态自动归组\",\"category\":1,\"appkey\":\"com.sankuai.RouteTest\",\"env\":3,\"priority\":0,\"status\":0,\"consumer\":{\"ips\":[\"10.4.*\"],\"appkeys\":[]},\"provider\":[\"10.4.*\"],\"createTime\":1463152423147,\"updateTime\":1463152423147,\"reserved\":\"route_limit:0\"}";
  EXPECT_EQ(0, JsonZkMgr::Json2RouteData(strJson, routeData));

  //缺少createTime
  strJson =
      "{\"id\":\"default-yf\",\"name\":\"动态自动归组\",\"category\":1,\"appkey\":\"com.sankuai.RouteTest\",\"env\":3,\"priority\":0,\"status\":0,\"consumer\":{\"ips\":[\"10.4.*\"],\"appkeys\":[]},\"provider\":[\"10.4.*\"],\"updateTime\":1463152423147,\"reserved\":\"route_limit:0\"}";
  EXPECT_EQ(0, JsonZkMgr::Json2RouteData(strJson, routeData));
};

