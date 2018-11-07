#include <limits.h>
#include <gtest/gtest.h>
#include "test_sg_agent.h"
#include "interface_base.h"
#include "common_define.h"

class SgAgentTimeout: public InterfaceBase
{
    public:
        virtual void SetUp()
        {
            appkey = "com.sankuai.mns.timeout.test";
            oservice.appkey = appkey;

            char ip[INET_ADDRSTRLEN];
            char mask[INET_ADDRSTRLEN];
            sg_agent_handler_.getIntranet(ip, mask);
            localIp = ip;

            oservice.ip = localIp;
            oservice.port = 5266;
            oservice.weight = 10;
            oservice.status = 2;
            oservice.role = 0;
            oservice.envir = 2;

            sg_agent_handler_.init(appkey, ip_, port_);
        }

        virtual void TearDown()
        {
        }

    public:
        SGAgentHandler sg_agent_handler_;
        SGService oservice;
        string appkey;
        string protocol;
        string localIp;
};

TEST_F(SgAgentTimeout, serviceList_add_node)
{
    srand(time(NULL));
    oservice.extend = 'A'+ rand() % 26;
    oservice.serverType = 0;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    //测试100ms, 注册获取成功
    usleep(utimeout * 1000);

    vector<SGService> serviceList;
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.getServiceList(serviceList); 
        if (!serviceList.empty()) {
            break;
        }
        usleep(8000);
    }
    EXPECT_EQ(1, serviceList.size());
    //EXPECT_STREQ(oservice.extend.c_str(), serviceList.front().extend.c_str());
};

TEST_F(SgAgentTimeout, serviceList_update_node)
{
    srand(time(NULL));
    oservice.extend = 'A'+ rand() % 26;
    oservice.serverType = 0;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    time_t start_time_ = time(NULL);
    time_t end_time = time(NULL);
    vector<SGService> serviceList;
    do
    {
        serviceList.clear();
        sg_agent_handler_.getServiceList(serviceList); 
        usleep(8000);
        end_time = time(NULL);
    }while(serviceList.empty() || oservice.extend != serviceList.front().extend);
    EXPECT_TRUE(end_time - start_time_ <= stimeout);
};

TEST_F(SgAgentTimeout, serviceList_delete_node)
{
    //对指定节点清理
    string cmd = "curl -X POST -H \"Content-Type:application/json\" -d ' [{\"appkey\":\"" + appkey + "\",\"ip\":\"" + localIp + "\",\"port\":5266,\"envir\":3,\"protocol\":\"thrift\"}]' mns.inf.test.sankuai.com/api/providers/delete";
    cout << cmd << endl;
    system(cmd.c_str());

    //测试100ms, 注册获取成功
    usleep(2*utimeout * 1000);

    vector<SGService> serviceList;
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.getServiceList(serviceList); 
        if (serviceList.empty()) {
            break;
        }
    }
    EXPECT_EQ(0, serviceList.size());
};

TEST_F(SgAgentTimeout, http_serviceList_add_node)
{
    srand(time(NULL));
    oservice.extend = 'A'+ rand() % 26;
    oservice.serverType = 1;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    //测试100ms, 注册获取成功
    usleep(utimeout * 1000);

    vector<SGService> serviceList;
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getHttpServiceList(serviceList, appkey, appkey); 
        if (!serviceList.empty()) {
            break;
        }
    }
    EXPECT_EQ(1, serviceList.size());
};

TEST_F(SgAgentTimeout, http_serviceList_update_node)
{
    srand(time(NULL));
    oservice.extend = 'A'+ rand() % 26;
    oservice.serverType = 1;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    time_t start_time_ = time(NULL);
    time_t end_time = time(NULL);
    vector<SGService> serviceList;
    do
    {
        serviceList.clear();
        sg_agent_handler_.client_->getHttpServiceList(serviceList, appkey, appkey); 
        usleep(8000);
        end_time = time(NULL);
    }while(serviceList.empty() || oservice.extend != serviceList.front().extend);
    EXPECT_TRUE(end_time - start_time_ <= stimeout);
};

TEST_F(SgAgentTimeout, http_serviceList_delete_node)
{
    //对指定节点清理
    string cmd = "curl -X POST -H \"Content-Type:application/json\" -d ' [{\"appkey\":\"" + appkey + "\",\"ip\":\"" + localIp + "\",\"port\":5266,\"envir\":3,\"protocol\":\"http\"}]' mns.inf.test.sankuai.com/api/providers/delete";
    cout << cmd << endl;
    system(cmd.c_str());

    //测试100ms, 注册获取成功
    usleep(2* utimeout * 1000);

    vector<SGService> serviceList;
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getHttpServiceList(serviceList, appkey, appkey); 
        if (serviceList.empty()) {
            break;
        }
    }
    EXPECT_EQ(0, serviceList.size());
};


