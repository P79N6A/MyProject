#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include "interface_base.h"
#include <gtest/gtest.h>
#include <stdlib.h>

//修改zk值后，等待时间，再去获得
#define SLEEP_TIME 200000 

class ZkServiceGroup: public InterfaceBase
{
    public:
        virtual void SetUp()
        {
            appkey_ = "com.sankuai.octo.tmy";
						sg_agent_handler_.init(appkey_, ip_, port_);
            zk_operator_.init(appkey_, zkserver_);

            //自定义分组1
            high.id = "dx";
            high.appkey = appkey_;
            high.name = "self define group 1";
            high.category = 0;
            high.status = 1;
            high.env = 2;
            high.updateTime = 1428414222;
            high.createTime = 1428411322;
            high.reserved = ""; 
            high.priority = 15;
            //provider
            vector<string> provider_1;
            provider_1.push_back("127.0.0.1:*");
            provider_1.push_back("127.0.0.2:*");
            high.provider = provider_1;
            //consumer
            Consumer consumer_1;
            vector<string> ips_1;
            ips_1.push_back("10.21.42.130");
            consumer_1.ips = ips_1;
            high.consumer = consumer_1;

            //自定义分组2
            low.id = "lf";
            low.appkey = appkey_;
            low.name = "self define group 2";
            low.category = 0;
            low.status = 1;
            low.env = 2;
            low.updateTime = 1428431222;
            low.createTime = 1428431222;
            low.reserved = ""; 
            low.priority = 5;
            //provider
            vector<string> provider_2;
            provider_2.push_back("10.21.40.249:*");
            low.provider = provider_2;
            //consumer
            Consumer consumer_2;
            vector<string> ips_2;
            ips_2.push_back("10.21.42.131");
            consumer_2.ips = ips_2;
            low.consumer = consumer_2;

            //动态自动归组
            dy.id = "yf";
            dy.appkey = appkey_;
            dy.name = "dynamic auto group";
            dy.category = 1;
            dy.status = 1;
            dy.env = 2;
            dy.updateTime = 1428431222;
            dy.createTime = 1428431222;
            dy.reserved = "route_limit:1"; 
            dy.priority = 0;
            //provider
            vector<string> provider_3;
            provider_3.push_back("10.21.42.132:*");
            dy.provider = provider_3;
            //consumer
            Consumer consumer_3;
            vector<string> ips_3;
            ips_3.push_back("10.21.42.129");
            consumer_3.ips = ips_3;
            dy.consumer = consumer_3;
        }

        static int findWeight(vector<SGService>& servicelist, const string &ip)
        {
            for(vector<SGService>::iterator itor = servicelist.begin(); itor != servicelist.end(); itor++)
            {
                if(itor->ip == ip)
                    return itor->weight;
            }
            return -1;
        }


    protected:
        ZkClientOperation zk_operator_;
        SGAgentHandler sg_agent_handler_;
        string appkey_;

        CRouteData high;
        CRouteData low;
        CRouteData dy;
};
/*
CRouteData ServiceGroup::high;
CRouteData ServiceGroup::low;
CRouteData ServiceGroup::dy;

ZkClientOperation ServiceGroup::zk_operator_;
SGAgentHandler ServiceGroup::sg_agent_handler_;
*/

TEST_F(ZkServiceGroup, PriorityTest)
{

    EXPECT_EQ(0, zk_operator_.setRoute(high));
    EXPECT_EQ(0, zk_operator_.setRoute(low));
    EXPECT_EQ(0, zk_operator_.setRoute(dy));

    usleep(SLEEP_TIME);

    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_EQ(13, serviceList.size());
    string ip = "127.0.0.1";
    EXPECT_EQ(5, findWeight(serviceList, ip));
};

TEST_F(ZkServiceGroup, ConsumerTest)
{
    //去掉高优先级节点的consumer里的信息
    high.consumer.ips.clear();
    EXPECT_EQ(0, zk_operator_.setRoute(high));

    usleep(SLEEP_TIME);

    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_EQ(13, serviceList.size());
    string ip = "127.0.0.3";
    EXPECT_EQ(5, findWeight(serviceList, ip));
};

TEST_F(ZkServiceGroup, ProviderTest)
{
    //去掉低优先级节点的provider里的信息
    low.consumer.ips.clear();
    EXPECT_EQ(0, zk_operator_.setRoute(low));

    usleep(SLEEP_TIME);

    vector<SGService> serviceList;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList));
    EXPECT_EQ(0, serviceList.size());
    //string ip = "127.0.0.2";
    //EXPECT_EQ(5, findWeight(serviceList, ip));
};

TEST_F(ZkServiceGroup, DeleteRoute)
{
    EXPECT_EQ(0, zk_operator_.deleteRoute(high));
    EXPECT_EQ(0, zk_operator_.deleteRoute(low));
    EXPECT_EQ(0, zk_operator_.deleteRoute(dy));
};
