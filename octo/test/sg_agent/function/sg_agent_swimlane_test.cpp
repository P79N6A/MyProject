#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgAgentSwimlane: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            string appkey = "com.sankuai.inf.swimlaneTest";
            start_time_ = time(NULL);
            ip_ = "127.0.0.1";
            sg_agent_handler_.init(appkey, ip_, port_);

            oservice.appkey = appkey;
            oservice.ip = "10.4.241.165";
            oservice.port = 5266;
            oservice.weight = 10;
            oservice.status = 2;
            oservice.role = 0;
            oservice.envir = 2;
            oservice.fweight = 0.10;
            oservice.__set_swimlane("swimlane");

            request.localAppkey = "chenxin_test";
            request.remoteAppkey = appkey;
        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
            EXPECT_TRUE(end_time - start_time_ <= 1) << "SgAgentSwimlane testcase took too long.";
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
        ProtocolRequest request;
};

TEST_F(SgAgentSwimlane, get_thrift_swimlane)
{
    std::string protocol = "thrift";
    oservice.protocol = protocol;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    usleep(5000);

    request.protocol = protocol;
    request.__set_swimlane("swimlane");

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);
    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(2, response.servicelist.size());
};


TEST_F(SgAgentSwimlane, thrift_mainlane) {
    std::string protocol = "thrift";
    request.protocol = protocol;

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);
    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(1, response.servicelist.size());
};

TEST_F(SgAgentSwimlane, get_http_protocol)
{
    request.protocol = "http";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(1, response.servicelist.size());
};

TEST_F(SgAgentSwimlane, get_cellar_protocol)
{
    request.protocol = "cellar";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(1, response.servicelist.size());
};

TEST_F(SgAgentSwimlane, get_wrong_swimlane)
{
    std::string protocol = "thrift";
    request.protocol = protocol;
    request.__set_swimlane("wrong");

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);
    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    //Go Back to mainlane
    EXPECT_EQ(1, response.servicelist.size());
};
