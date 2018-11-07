#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <stdlib.h>

#define SLEEP_TIME 500000 

class RestartZk: public testing::Test
{
    public:
        static void SetUpTestCase()
        {
            system("/opt/meituan/apps/zookeeper/bin/zkServer.sh restart zk1.cfg");
            sleep(5);
            sg_agent_handler_.init(appkey_, ip_, port_);
            zk_operator_.init(appkey_, "192.168.4.252:2181");
        }
        static void TearDownTestCase()
        {
            zk_operator_.deinit();
            sg_agent_handler_.deinit();
        }

    protected:
        virtual void SetUp()
        {
        }

        virtual void TearDown()
        {
        }

        static bool isInServiceList(vector<SGService>& servicelist, const SGService &service)
        {
            for(vector<SGService>::iterator itor = servicelist.begin(); itor != servicelist.end(); itor++)
            {
                if(*itor == service)
                    return true;
            }
            return false;
        }

        static ZkClientOperation zk_operator_;
        static SGAgentHandler sg_agent_handler_;
        static string appkey_;
        static string ip_;
        static int port_;
};

string RestartZk::appkey_ = "com.sankuai.inf.testcase";
ZkClientOperation RestartZk::zk_operator_;
SGAgentHandler RestartZk::sg_agent_handler_;
string RestartZk::ip_ = "192.168.4.252";
int RestartZk::port_ = 5266;

TEST_F(RestartZk, CheckZkConnection) {
    EXPECT_EQ(0, zk_operator_.checkZk());
};

TEST_F(RestartZk, CreateZNodeTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = ip_;
    sg_service.port = port_;
    sg_service.weight = 10;
    sg_service.status = 3;
    sg_service.role = 0;
    sg_service.envir = 2;
    sg_service.lastUpdateTime = 1428411222;
    sg_service.extend = ""; 

    // step 2: 调用测试接口,监测返回值
    EXPECT_EQ(0, sg_agent_handler_.registerService(sg_service));

    usleep(SLEEP_TIME);

    // step 3: 检查zk里数据是否存在
    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_TRUE(isInServiceList(serviceList, sg_service));
};

TEST_F(RestartZk, UpdateZNodeTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = ip_;
    sg_service.port = port_;
    sg_service.weight = 10;
    sg_service.status = 3;
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
    EXPECT_TRUE(isInServiceList(serviceList, sg_service));
};

TEST_F(RestartZk, DeleteZNodeTest)
{
    //step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = ip_;
    sg_service.port = port_;
    sg_service.extend = ""; 

    //step 3: 检查zk里数据是否存在
    EXPECT_EQ(0, zk_operator_.deleteZNode(sg_service));
};
