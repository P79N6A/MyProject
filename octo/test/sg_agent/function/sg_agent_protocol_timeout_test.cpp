#include <limits.h>
#include <gtest/gtest.h>
#include "test_sg_agent.h"
#include "interface_base.h"
#include "common_define.h"
#include <iostream>

class SgAgentProtocolTimeout: public InterfaceBase
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

            srand(time(NULL));
            oservice.extend = 'A'+ rand() % 26;
            oservice.port = port_;
            oservice.weight = 10;
            oservice.status = 2;
            oservice.role = 0;
            oservice.envir = 2;

            sg_agent_handler_.init(appkey, ip_, port_);

            request.remoteAppkey = appkey;

            vector<SGService> servicelist;
            response.__set_servicelist(servicelist);
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

        ProtocolRequest request;
        ProtocolResponse response;
};

TEST_F(SgAgentProtocolTimeout, thrift_add_node)
{
    protocol = "thrift";
    oservice.protocol = protocol;
    request.protocol = protocol;
    std::cout<< oservice.extend << std::endl;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    sleep(5);
    bool is_exist = false;
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	for(std::vector<SGService>::iterator iter = response.servicelist.begin(); response.servicelist.end() != iter; ++iter){
	   if(ip_ == iter->ip && port_ == iter->port){
	     is_exist = true;
	     break;
	   }
	}
    }
    EXPECT_TRUE(is_exist);
};

TEST_F(SgAgentProtocolTimeout, thrift_update_node)
{
    protocol = "thrift";
    oservice.protocol = protocol;
    request.protocol = protocol;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    time_t start_time_ = time(NULL);
    time_t end_time = time(NULL);
    do
    {
        sleep(5);
        response.servicelist.clear();
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
        EXPECT_EQ(0, response.errcode);
        end_time = time(NULL);
    }while(response.servicelist.empty() || oservice.extend != response.servicelist.front().extend);

    sg_agent_handler_.PrintServiceList(response.servicelist);
};

TEST_F(SgAgentProtocolTimeout, thrift_delete_node)
{
    //对指定节点清理
    string cmd = "curl -X POST -H \"Content-Type:application/json\" -d ' [{\"appkey\":\"" + appkey + "\",\"ip\":\"" + localIp + "\",\"port\":5266,\"envir\":3,\"protocol\":\"thrift\"}]' mns.inf.test.sankuai.com/api/providers/delete";
    cout << cmd << endl;
    system(cmd.c_str());

    sleep(5);

    request.protocol = "thrift";
    bool is_exist = false;
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	is_exist = false;
	for(std::vector<SGService>::iterator iter = response.servicelist.begin(); response.servicelist.end() != iter; ++iter){
	   if(localIp == iter->ip && 5266 == iter->port){
	     is_exist = true;
	     continue;   
	   }
	}
    }
    EXPECT_FALSE(is_exist);
};

TEST_F(SgAgentProtocolTimeout, http_add_node)
{
    protocol = "http";
    oservice.protocol = protocol;
    request.protocol = protocol;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    //测试100ms, 注册获取成功
    usleep(utimeout * 1000);

    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
        if (!response.servicelist.empty()) {
            break;
        }
    }
};

TEST_F(SgAgentProtocolTimeout, http_update_node)
{
    protocol = "http";
    oservice.protocol = protocol;
    request.protocol = protocol;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    time_t start_time_ = time(NULL);
    time_t end_time = time(NULL);
    do
    {
        usleep(8000);
        response.servicelist.clear();
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
        EXPECT_EQ(0, response.errcode);
        end_time = time(NULL);
    }while(response.servicelist.empty() || oservice.extend != response.servicelist.front().extend);

    sg_agent_handler_.PrintServiceList(response.servicelist);
};

TEST_F(SgAgentProtocolTimeout, http_delete_node)
{
    //对指定节点清理
    string cmd = "curl -X POST -H \"Content-Type:application/json\" -d ' [{\"appkey\":\"" + appkey + "\",\"ip\":\"" + localIp + "\",\"port\":5266,\"envir\":3,\"protocol\":\"http\"}]' mns.inf.test.sankuai.com/api/providers/delete";
    cout << cmd << endl;
    system(cmd.c_str());

    //测试100ms, 注册获取成功
    usleep(utimeout * 1000);

    request.protocol = "http";
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
        if (response.servicelist.empty()) {
            break;
        }
    }
    EXPECT_EQ(0, response.servicelist.size());
};

TEST_F(SgAgentProtocolTimeout, cellar_add_node)
{
    protocol = "cellar";
    oservice.protocol = protocol;
    request.protocol = protocol;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    //测试100ms, 注册获取成功
    usleep(utimeout * 1000);

    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
        if (!response.servicelist.empty()) {
            break;
        }
    }
};

TEST_F(SgAgentProtocolTimeout, cellar_update_node)
{
    protocol = "cellar";
    oservice.protocol = protocol;
    request.protocol = protocol;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    time_t start_time_ = time(NULL);
    time_t end_time = time(NULL);
    do
    {
        usleep(8000);
        response.servicelist.clear();
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
        EXPECT_EQ(0, response.errcode);
        end_time = time(NULL);
    }while(response.servicelist.empty() || oservice.extend != response.servicelist.front().extend);
    EXPECT_TRUE(end_time - start_time_ < stimeout);

    sg_agent_handler_.PrintServiceList(response.servicelist);
};

TEST_F(SgAgentProtocolTimeout, cellar_delete_node)
{
    //对指定节点清理
    string cmd = "curl -X POST -H \"Content-Type:application/json\" -d ' [{\"appkey\":\"" + appkey + "\",\"ip\":\"" + localIp + "\",\"port\":5266,\"envir\":3,\"protocol\":\"cellar\"}]' mns.inf.test.sankuai.com/api/providers/delete";
    cout << cmd << endl;
    system(cmd.c_str());

    //测试100ms, 注册获取成功
    usleep(utimeout * 1000);

    request.protocol = "cellar";
    bool is_exist = false;
    for (int i = 0; i < retrytimes; ++i) {
        sg_agent_handler_.client_->getServiceListByProtocol(response, request);
	is_exist = false;
	for(std::vector<SGService>::iterator iter = response.servicelist.begin(); response.servicelist.end() != iter; ++iter){
	  if(localIp == iter->ip && 5266 == iter->port){
	    is_exist = true;
	    continue;
	  }
	}
    }
    EXPECT_FALSE(is_exist);
};

