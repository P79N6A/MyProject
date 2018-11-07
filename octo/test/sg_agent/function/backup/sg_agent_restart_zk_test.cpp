#include "test_sg_agent.h"
#include "interface_base.h"

const char* ips[] = {"127.0.0.3",
                     "127.0.0.1",
                     "127.0.0.6",
                     "127.0.0.7"};

INSTANTIATE_TEST_CASE_P(RegistZNode, SgAgentInterfaceTest, testing::ValuesIn(ips));

TEST_P(SgAgentInterfaceTest, RegistAndGetServiceTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = "com.sankuai.inf.testcase";
    sg_service.ip = GetParam();
    sg_service.port = 7001;
    sg_service.weight = 5;
    sg_service.status = 3;
    sg_service.role = 0;
    sg_service.envir = 2;
    sg_service.lastUpdateTime = 1428411222;
    sg_service.extend = ""; 
    
    // step 2: 调用测试接口,监测返回值
    EXPECT_EQ(0, sg_agent_handler_.registerService(sg_service));
    
    // step 3: 检查zk里数据是否存在
    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    
    EXPECT_EQ(4, serviceList.size());
};
