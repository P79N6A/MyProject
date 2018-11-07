#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <time.h>

#define SLEEP_TIME 100000

class MonoProviderRestartTest: public testing::Test
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            appkey_ = "com.sankuai.inf.chenxin";
            ip_ = "192.168.4.252";
            port_ = 5266;
            sg_agent_handler_.init(appkey_, ip_, port_);
            zk_operator_.init(appkey_, "192.168.4.252:2181");
        }

        virtual void TearDown()
        {
            sg_agent_handler_.deinit();
            zk_operator_.deinit();
            const time_t end_time = time(NULL);
            cost_time_ = end_time - start_time_;
            //EXPECT_TRUE(cost_time_ <= 20) << "UploadModule took too long.";
        }

        SGAgentHandler sg_agent_handler_;
        ZkClientOperation zk_operator_;
        time_t start_time_;
        time_t cost_time_;
        string appkey_;
        string ip_;
        int port_;
};

TEST_F(MonoProviderRestartTest, status)
{
    vector<SGService> serviceList;
    sg_agent_handler_.getServiceList(serviceList);
    EXPECT_EQ(4, serviceList.size());

    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = ip_;
    sg_service.port = 7001;
    sg_service.status = 0;
    sg_service.weight = 10;
    zk_operator_.setZNode(sg_service);

    usleep(SLEEP_TIME);

    sg_service.status = 2;
    zk_operator_.setZNode(sg_service);

    usleep(SLEEP_TIME);
    
    sg_agent_handler_.getServiceList(serviceList);
    EXPECT_EQ(4, serviceList.size());
}
