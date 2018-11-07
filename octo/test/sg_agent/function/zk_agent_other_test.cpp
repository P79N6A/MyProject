#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "tinyxml2.h"
#include <stdlib.h>

//修改zk值后，等待时间，再去获得
#define SLEEP_TIME 1000000 

class ZkOtherTest: public testing::Test
{
    public:
        static void SetUpTestCase()
        {
            tinyxml2::XMLDocument conf;
            conf.LoadFile("configure.xml");
            const char* agentport = conf.FirstChildElement("AgentPort")->GetText();
            int port = atoi(agentport);
            const char* agenthost = conf.FirstChildElement("AgentHost")->GetText();

            sg_agent_handler_.init(appkey_, agenthost, port);

            const char* zkserver = conf.FirstChildElement("ZkServer")->GetText();
            zk_operator_.init(appkey_, zkserver);
        }

        static void TearDownTestCase()
        {
            zk_operator_.deinit();
            sg_agent_handler_.deinit();
        }

        static bool isInServiceList(vector<SGService>& servicelist, const SGService &service)
        {
            for(vector<SGService>::iterator itor = servicelist.begin(); itor != servicelist.end(); itor++)
            {
                if(itor->ip == service.ip && itor->port == service.port 
                                          && itor->lastUpdateTime == service.lastUpdateTime 
                                          && itor->extend == service.extend
                                          && itor->envir == service.envir
                                          && itor->status == service.status
                                          && itor->weight == service.weight
                                          && itor->role == service.role
                                          && itor->appkey == service.appkey
                                          && itor->version == service.version)           
                //if(*itor == service)
                    return true;
            }
            return false;
        }

    protected:
        virtual void SetUp()
        {
            front.appkey = appkey_;
            front.ip = "127.0.1.1";
            front.port = 7001;
            front.weight = 20;
            front.status = 2;
            front.role = 0; 
            front.envir = 2;
            front.lastUpdateTime = 1428411222;
            front.extend = ""; 

            back.appkey = appkey_;
            back.ip = "127.0.1.2";
            back.port = 7001;
            back.weight = 5;
            back.status = 2;
            back.role = 1; //backup节点
            back.envir = 2;
            back.lastUpdateTime = 1428411222;
            back.extend = ""; 
        }

        virtual void TearDown()
        {
        }

        static ZkClientOperation zk_operator_;
        static SGAgentHandler sg_agent_handler_;
        static string appkey_;

        SGService front;
        SGService back;
};

ZkClientOperation ZkOtherTest::zk_operator_;
SGAgentHandler ZkOtherTest::sg_agent_handler_;
string ZkOtherTest::appkey_ = "com.sankuai.inf.other";
TEST_F(ZkOtherTest, ProviderValueTest)
{
    EXPECT_EQ(0, sg_agent_handler_.registerService(front));

    //查看provider的lastModifiedTime值是否改变
    CProviderNode provider;
    provider.lastModifiedTime = 0;
    EXPECT_EQ(0, zk_operator_.getProvider(provider));
    EXPECT_NE(0, provider.lastModifiedTime);
};


TEST_F(ZkOtherTest, FilterBackupNodeTest)
{
    EXPECT_EQ(0, zk_operator_.setZNode(back));

    usleep(SLEEP_TIME);

    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_FALSE(isInServiceList(serviceList, back));
};

TEST_F(ZkOtherTest, OnlyBackupNodeTest)
{
    //使主节点不可用
    front.status = 3;

    EXPECT_EQ(0, zk_operator_.setZNode(front));

    usleep(SLEEP_TIME);

    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_TRUE(isInServiceList(serviceList, back));
};

TEST_F(ZkOtherTest, DeleteNodes)
{
    EXPECT_EQ(0, zk_operator_.deleteZNode(front));
    EXPECT_EQ(0, zk_operator_.deleteZNode(back));
};
