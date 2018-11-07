#include <limits.h>
#include "interface_base.h"
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>

class SgAgentGetMultiServiceListLength: public InterfaceBase
{
protected:
    virtual void SetUp()
    {
        request.protocol = "thrift";
        request.localAppkey = "chenxin_multi_appkey_test";
    }

    virtual void TearDown()
    {
    }

    ProtocolRequest request;
};

TEST_F(SgAgentGetMultiServiceListLength, old_MoreThan_0_node)
{
    SGAgentHandler sg_agent_handler_;
    sg_agent_handler_.init(appkey_s, ip_, port_);
    
    vector<SGService> serviceList_1;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList_1));
    EXPECT_LE(0, serviceList_1.size());

    sg_agent_handler_.deinit();
};

TEST_F(SgAgentGetMultiServiceListLength, new_MoreThan_0_node)
{
    SGAgentHandler sg_agent_handler_;
    sg_agent_handler_.init(appkey_s, ip_, port_);

    request.remoteAppkey = appkey_s;
    
    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_LE(0, response.servicelist.size());
    
    sg_agent_handler_.deinit();
};

TEST_F(SgAgentGetMultiServiceListLength, old_MoreThan_30_node)
{
    SGAgentHandler sg_agent_handler_;
    
    sg_agent_handler_.init("com.sankuai.waimai.contract", ip_, port_);
    
    vector<SGService> serviceList_2;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList_2));
    EXPECT_LE(30, serviceList_2.size());
    sg_agent_handler_.deinit();
};

TEST_F(SgAgentGetMultiServiceListLength, new_MoreThan_30_node)
{
    SGAgentHandler sg_agent_handler_;
    sg_agent_handler_.init(appkey_s, ip_, port_);
    
    request.remoteAppkey = "com.sankuai.waimai.contract";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_LE(30, response.servicelist.size());
    sg_agent_handler_.deinit();
};

TEST_F(SgAgentGetMultiServiceListLength, old_MoreThan_100_node)
{
    SGAgentHandler sg_agent_handler_;
    
    sg_agent_handler_.init("com.sankuai.inf.sg_sentinel", ip_, port_);
    
    vector<SGService> serviceList_3;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList_3));
    EXPECT_LE(10, serviceList_3.size());
    sg_agent_handler_.deinit();
};

TEST_F(SgAgentGetMultiServiceListLength, new_MoreThan_100_node)
{
    SGAgentHandler sg_agent_handler_;
    sg_agent_handler_.init(appkey_s, ip_, port_);
    
    request.remoteAppkey = "com.sankuai.inf.sg_sentinel";

    ProtocolResponse response;
    vector<SGService> servicelist;
    response.__set_servicelist(servicelist);

    sg_agent_handler_.client_->getServiceListByProtocol(response, request);
    EXPECT_EQ(0, response.errcode);
    EXPECT_LE(10, response.servicelist.size());
    sg_agent_handler_.deinit();
};

/*
TEST_F(SgAgentGetMultiServiceListLength, old_MoreThan_2000_node)
{
    SGAgentHandler sg_agent_handler_;
    
    sg_agent_handler_.init("com.sankuai.inf.sg_agent", ip_, port_);
    
    vector<SGService> serviceList_4;
    EXPECT_EQ(0, sg_agent_handler_.getServiceList(serviceList_4));
    EXPECT_LE(2000, serviceList_4.size());
    sg_agent_handler_.deinit();
};
*/
