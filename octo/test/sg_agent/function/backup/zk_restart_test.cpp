#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "tinyxml2.h"

#include <stdlib.h>

class CInterfaceTest: public testing::TestWithParam<const char*>
{
protected:
    virtual void SetUp()
    {
        tinyxml2::XMLDocument conf;
        conf.LoadFile("configure.xml");
        const char* agentport = conf.FirstChildElement("AgentPort")->GetText();
        int port = atoi(agentport);
        const char* agenthost = conf.FirstChildElement("AgentHost")->GetText();
        sg_agent_handler_.init(agenthost, port);
        const char* appkey = conf.FirstChildElement("ProviderAppkey")->GetText();
        zk_operator_.init(appkey);

        //system("/root/chenxin/zookeeper-3.4.6/bin/zkServer.sh restart zk1.cfg");
    }

    virtual void TearDown()
    {
        zk_operator_.deinit();
        sg_agent_handler_.deinit();
    }

    ZkClientOperation zk_operator_;
    SGAgentHandler sg_agent_handler_;
};

TEST_F(CInterfaceTest, CheckZkConnection) {
  EXPECT_EQ(0, zk_operator_.checkZk());
};

const char* ips[] = {"127.0.0.3",
                   //  "127.0.0.1",
                   //  "127.0.0.6",
                     "127.0.0.7"};


INSTANTIATE_TEST_CASE_P(AllOperation, CInterfaceTest, testing::ValuesIn(ips));

TEST_P(CInterfaceTest, CreateZNodeTest)
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

/*

TEST_P(CInterfaceTest, UpdateZNodeTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = "com.sankuai.inf.testcase";
    sg_service.ip = GetParam();
    sg_service.port = 7001;
    sg_service.weight = 15;
    sg_service.status = 3;
    sg_service.role = 0;
    sg_service.envir = 2;
    sg_service.lastUpdateTime = 1428411222;
    sg_service.extend = ""; 
    
    // step 2: 调用测试接口,监测返回值
    EXPECT_EQ(0, zk_operator_.setZNode(sg_service));
    
    // step 3: 检查zk里数据是否存在
    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_EQ(sg_service, serviceList.back());
};

TEST_P(CInterfaceTest, DeleteZNodeTest)
{
    //step 1:  获得测试数据
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
    
    //step 3: 检查zk里数据是否存在
    EXPECT_EQ(0, zk_operator_.deleteZNode(sg_service));
};
*/
