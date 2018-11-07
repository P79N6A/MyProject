#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgAgentRegion: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            appkey = "com.sankuai.mns.regiontest";
            sg_agent_handler_.init(appkey, ip_, port_);

            oservice.appkey = appkey;
            oservice.status = 2;
        }

        virtual void TearDown()
        {
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
        std::string appkey;
};

TEST_F(SgAgentRegion, old_GetServiceList)
{
    vector<SGService> serviceList;
    sg_agent_handler_.client_->getServiceList(serviceList, appkey, appkey);
    sg_agent_handler_.PrintServiceList(serviceList);
};

TEST_F(SgAgentRegion, new_GetServiceList)
{
    ProtocolRequest request;
    request.remoteAppkey = appkey;
    request.protocol = "thrift";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);

    sg_agent_handler_.PrintServiceList(response.servicelist);
};

TEST_F(SgAgentRegion, cellar_GetServiceList)
{
    ProtocolRequest request;
    request.remoteAppkey = appkey;
    request.protocol = "cellar";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    for (int i = 0; i < 3; ++i) {
      sg_agent_handler_.client_->getServiceListByProtocol(response, request);
      if (0 == response.errcode) {
        break;
      }
    }
    EXPECT_EQ(0, response.errcode);

    sg_agent_handler_.PrintServiceList(response.servicelist);
};
