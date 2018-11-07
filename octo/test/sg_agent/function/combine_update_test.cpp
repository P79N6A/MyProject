#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "tinyxml2.h"

#define SLEEP_TIME 10000 

class Update: public testing::TestWithParam< ::std::tr1::tuple<int, int> >
{
    protected:
        virtual void SetUp()
        {
            tinyxml2::XMLDocument conf;
            conf.LoadFile("configure.xml");
            const char* agentport = conf.FirstChildElement("AgentPort")->GetText();
            int port = atoi(agentport);
            const char* agenthost = conf.FirstChildElement("AgentHost")->GetText();
            appkey_ = conf.FirstChildElement("BasicAppkey")->GetText();
            sg_agent_handler_.init(appkey_, agenthost, port);

            const char* zkserver = conf.FirstChildElement("ZkServer")->GetText();
            zk_operator_.init(appkey_, zkserver);
        }

        virtual void TearDown()
        {
            zk_operator_.deinit();
            sg_agent_handler_.deinit();
        }

        ZkClientOperation zk_operator_;
        SGAgentHandler sg_agent_handler_;
        string appkey_;
};


INSTANTIATE_TEST_CASE_P(UpdateZkTest, Update, testing::Combine(testing::Values(0, 2), testing::Values(7001, 7002)));

TEST_P(Update, updateStatusTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.weight = 20;
    sg_service.status = ::std::tr1::get<0>(GetParam());
    sg_service.port = ::std::tr1::get<1>(GetParam());
    sg_service.ip = "192.168.172.66";
    sg_service.role = 0;
    sg_service.envir = 2;
    sg_service.lastUpdateTime = 1428411222;
    sg_service.extend = ""; 

     // step 2: 调用测试接口,监测返回值
    EXPECT_EQ(0, zk_operator_.setZNode(sg_service));

    usleep(SLEEP_TIME);

     // step 3: 检查zk里数据是否存在
    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    //EXPECT_EQ(std::tr1::get<0>(GetParam()), serviceList[0].status);
    //EXPECT_EQ(std::tr1::get<1>(GetParam()), serviceList[0].weight);
};
