#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgAgentBackup: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
//            appkey = "com.sankuai.inf.octo.colosseomnsc";
						appkey="com.sankuai.mns.backuptest";
						sg_agent_handler_.init(appkey, ip_, port_);

            oservice.appkey = appkey;
        }

        virtual void TearDown()
        {
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
        std::string appkey;
};

TEST_F(SgAgentBackup, old_GetServiceList)
{
    vector<SGService> serviceList;
    sg_agent_handler_.client_->getServiceList(serviceList, appkey, appkey);
    sg_agent_handler_.PrintServiceList(serviceList);
    EXPECT_EQ(1, serviceList.size());
};

TEST_F(SgAgentBackup, http_GetServiceList)
{
    vector<SGService> serviceList;
    sg_agent_handler_.client_->getHttpServiceList(serviceList, appkey, appkey);
    EXPECT_EQ(2, serviceList.size());
};

TEST_F(SgAgentBackup, new_GetServiceList)
{
    ProtocolRequest request;
    request.remoteAppkey = appkey;
    request.protocol = "thrift";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(1, response.servicelist.size());

    sg_agent_handler_.PrintServiceList(response.servicelist);
};

TEST_F(SgAgentBackup, tair_GetServiceList)
{
    ProtocolRequest request;
    request.remoteAppkey = appkey;
    request.protocol = "tair";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(2, response.servicelist.size());

    sg_agent_handler_.PrintServiceList(response.servicelist);
};
