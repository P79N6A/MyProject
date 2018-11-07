
// =====================================================================================
// 
//       Filename:  main.cpp
// 
//    Description:  learn msg
// 
//        Version:  1.0
//        Created:  2015-04-17 11时41分55秒
//       Revision:  none
// 
// =====================================================================================

#include "sg_agent/thrift_client_handler.h"
#include "sg_agent/mtconfig_server_client.h"
#include <gtest/gtest.h>
#include "util/sg_agent_def.h"

class ThriftClientHandlerTest : public testing::Test {
 public:
  virtual void SetUp() {
    param.appkey = "com.sankuai.inf.mcc_test";
    param.env = "stage";
    param.path = "/";
  }
 public:
  proc_conf_param_t param;
};

TEST_F(ThriftClientHandlerTest, MtConfigClientTest) {
  try {
    //定义获取配置返回结构体
    ConfigDataResponse confRes;
    //定义请求参数结构体
    GetMergeDataRequest reqConf;
    reqConf.__set_appkey(param.appkey);
    reqConf.__set_env(param.env);
    reqConf.__set_path(param.path);

    ThriftClientHandler *pCollector = new ThriftClientHandler();
    EXPECT_EQ(0, pCollector->init("10.21.245.72", 9001, MTCONFIG));
    void *pClient = pCollector->getClient();
    if (!pClient) {
      LOG_ERROR("get MtConfigServiceClient failed!");
      return ;
    }

    MtConfigServiceClient *ptr = reinterpret_cast<MtConfigServiceClient *>(pClient);
    ptr->getMergeData(confRes, reqConf);
  } catch (...) {
  }
}
