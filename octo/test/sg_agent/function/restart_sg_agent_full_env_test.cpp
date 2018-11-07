#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <time.h>
#include "interface_base.h"

class RestartAgentFullEnv: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            appkey = "com.sankuai.cellar.configchenxintest";

            oservice.appkey = appkey;
            cout << "appkey = " << oservice.appkey << endl;
            oservice.ip = "127.0.0.1";
            oservice.port = 5266;
            oservice.weight = 10;
            oservice.role = 0;
            oservice.envir = 2;
            oservice.fweight = 10.0;
            oservice.lastUpdateTime = start_time_;
            oservice.status = 2;
            sg_agent_handler_.init(appkey, ip_, port_);
        }

        virtual void TearDown()
        {
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
        std::string appkey;
};

TEST_F(RestartAgentFullEnv, switchEnvToStage)
{
   SGAgentHandler close_handler_;
   close_handler_.init(appkey, ip_, port_);
   EXPECT_EQ(3, close_handler_.client_->getEnv());
   try {
        close_handler_.client_->switchEnv("stage","agent.octo.sankuai.com");
   } catch (...) {

   }
   sleep(4);
};

TEST_F(RestartAgentFullEnv, oldThrift)
{
    srand(time(NULL));
    oservice.extend = 'A'+ rand() % 26;
    oservice.serverType = 0;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    vector<SGService> serviceList;
    sg_agent_handler_.getServiceList(serviceList); 
    EXPECT_EQ(2, serviceList.size());
};

TEST_F(RestartAgentFullEnv, newThrift)
{
    oservice.protocol = "thrift";
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    ProtocolRequest request;
    request.remoteAppkey = appkey;
    request.protocol = "thrift";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);
    
    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(2, response.servicelist.size());
};

TEST_F(RestartAgentFullEnv, cellar)
{
    oservice.protocol = "cellar";
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    ProtocolRequest request;
    request.remoteAppkey = appkey;
    request.protocol = "cellar";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);
    
    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_EQ(3, response.servicelist.size());

};

TEST_F(RestartAgentFullEnv, returnEnvToProd)
{
   SGAgentHandler close_handler_;
   close_handler_.init(appkey, ip_, port_);
   EXPECT_EQ(3, close_handler_.client_->getEnv());
   try {
        close_handler_.client_->switchEnv("prod","agent.octo.sankuai.com");
   } catch (...) {

   }
   sleep(2);
};
