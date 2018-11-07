#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"
#include "operation_common_types.h"

class SgAgentSwitch: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            sg_agent_handler_.init(appkey_s, ip_, port_);
        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
            EXPECT_TRUE(end_time - start_time_ <= 1) << "SgAgentTimeout testcase took too long.";
        }

        SGAgentHandler sg_agent_handler_;
};

TEST_F(SgAgentSwitch, SwitchMafkaCorrect)
{ 
    //先关闭
    SwitchRequest req;
    req.__set_key(Switch::SwitchMafka);
    req.__set_value(false);
    req.__set_switchName("OpenMafka");
    req.__set_verifyCode("agent.octo.sankuai.com");
    SwitchResponse res;
    sg_agent_handler_.client_->setRemoteSwitch(res, req); 
    EXPECT_EQ(0, res.errcode);

    //再打开
    req.__set_value(true);
    sg_agent_handler_.client_->setRemoteSwitch(res, req); 
    EXPECT_EQ(0, res.errcode);
}; 

TEST_F(SgAgentSwitch, WrongVerifyCode)
{ 
    SwitchRequest req;
    req.__set_verifyCode("agent.octo");

    SwitchResponse res;
    sg_agent_handler_.client_->setRemoteSwitch(res, req); 
    EXPECT_EQ(-1, res.errcode);
}; 

TEST_F(SgAgentSwitch, OtherSwitch)
{ 
    SwitchRequest req;
    req.__set_key(Switch::SwitchMNSCache);
    req.__set_verifyCode("agent.octo.sankuai.com");

    SwitchResponse res;
    sg_agent_handler_.client_->setRemoteSwitch(res, req); 
    EXPECT_EQ(-2, res.errcode);
}; 
