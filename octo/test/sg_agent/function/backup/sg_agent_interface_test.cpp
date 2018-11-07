#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "tinyxml2.h"

class SgAgentInterfaceTest: public testing::TestWithParam<const char*>
{
protected:
    static void SetUpTestCase()
    {
        tinyxml2::XMLDocument conf;
        conf.LoadFile("./configure.xml");
        const char* agentport = conf.FirstChildElement("AgentPort")->GetText();
        int port = atoi(agentport);
        const char* agenthost = conf.FirstChildElement("AgentHost")->GetText();
        sg_agent_handler_.init("com.sankuai.inf.chenxin11", agenthost, port);
    }

    static void TearDownTestCase()
    {
        sg_agent_handler_.deinit();
    }

    virtual void SetUp()
    {
        system("/root/chenxin/zookeeper-3.4.6/bin/zkServer.sh stop zk1.cfg");
    }

    virtual void TearDown()
    {
        system("/root/chenxin/zookeeper-3.4.6/bin/zkServer.sh start zk1.cfg");

    }
    static SGAgentHandler sg_agent_handler_;
};

SgAgentInterfaceTest::sg_agent_handler_;

TEST_F(SgAgentInterfaceTest, RegistAndGetServiceTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = "com.sankuai.inf.chenxin11";
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

TEST_F(SgAgentInterfaceTest, GetServiceListAfterZkBroken)
{
    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_EQ(5, serviceList.size());
}
