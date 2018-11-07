#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "tinyxml2.h"
#include <stdlib.h>

//修改zk值后，等待时间，再去获得
#define SLEEP_TIME 500000 

class CInterfaceTest: public testing::TestWithParam<const char*>
{
    public:
        static void SetUpTestCase()
        {
        }

        static void TearDownTestCase()
        {
        }

        static bool isInServiceList(vector<SGService>& servicelist, const SGService &service)
        {
            for(vector<SGService>::iterator itor = servicelist.begin(); itor != servicelist.end(); itor++)
            {
                if(itor->ip == service.ip && itor->port == service.port 
                                          && itor->envir == service.envir
                                          && itor->status == service.status
                                          && itor->weight == service.weight
                                          && itor->version == service.version)           
               // if(*itor == service)
                    return true;
            }
            return false;
        }

    protected:
        virtual void SetUp()
        {
            appkey_ = "com.sankuai.inf.basic";

            sg_agent_handler_.init(appkey_, ip_, port_);
            zk_operator_.init(appkey_, zkserver_);
        
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

TEST_F(CInterfaceTest, CheckZkConnection) {
    EXPECT_EQ(0, zk_operator_.checkZk());
};

//数据相关测试
const char* ips[] = {"127.0.0.1",
                     "127.0.0.2",
                     "127.0.0.3",
                     "127.0.0.4"};

INSTANTIATE_TEST_CASE_P(BasicOperation, CInterfaceTest, testing::ValuesIn(ips));


TEST_P(CInterfaceTest, CreateNodeTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = GetParam();
    sg_service.port = 7001;
    sg_service.weight = 5;
    sg_service.status = 2;
    sg_service.role = 0;
    sg_service.envir = 2;
    sg_service.lastUpdateTime = 1428411222;
    sg_service.extend = ""; 

    // step 2: 调用测试接口,监测返回值
    EXPECT_EQ(0, sg_agent_handler_.registerService(sg_service));
   // EXPECT_EQ(0, zk_operator_.setZNode(sg_service));

    usleep(SLEEP_TIME);

    // step 3: 检查zk里数据是否存在
    vector<SGService> serviceList;
    sg_agent_handler_.getServiceList(serviceList);
    EXPECT_TRUE(isInServiceList(serviceList, sg_service));
};

TEST_F(CInterfaceTest, CompareServiceListTest)
{
    vector<SGService> serviceList_1;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList_1));

    vector<SGService> serviceList_2;
    zk_operator_.getZNode(serviceList_2);

    EXPECT_TRUE(serviceList_1 == serviceList_2);
};

TEST_P(CInterfaceTest, UpdateNodeTest)
{
    // step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = GetParam();
    sg_service.port = 7001;
    sg_service.weight = 8;
    //更新status
    sg_service.status = 0;
    sg_service.role = 0;
    sg_service.envir = 2;
    sg_service.lastUpdateTime = 1428411222;
    sg_service.extend = ""; 

    // step 2: 调用测试接口,监测返回值
    EXPECT_EQ(0, zk_operator_.setZNode(sg_service));

    usleep(SLEEP_TIME);

    // step 3: 检查zk里数据是否存在
    vector<SGService> serviceList;
    sg_agent_handler_.getServiceList(serviceList);
    EXPECT_TRUE(isInServiceList(serviceList, sg_service));
};

TEST_P(CInterfaceTest, DeleteZNodeTest)
{
    //step 1:  获得测试数据
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = GetParam();
    sg_service.port = 7001;
    sg_service.extend = ""; 

    //step 3: 检查zk里数据是否存在
    EXPECT_EQ(0, zk_operator_.deleteZNode(sg_service));
};
